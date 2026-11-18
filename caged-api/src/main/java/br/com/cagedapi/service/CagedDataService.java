package br.com.cagedapi.service;

import br.com.cagedapi.config.CagedProperties;
import br.com.cagedapi.exception.ArquivoCagedNaoEncontradoException;
import br.com.cagedapi.model.Movimentacao;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Baixa o CAGEDMOV do FTP do MTE, filtra o RS e guarda um CSV enxuto em cache
 * (equivalente ao parquet da versão Python).
 */
@Service
public class CagedDataService {

    private static final Logger log = LoggerFactory.getLogger(CagedDataService.class);

    private static final Pattern ANO = Pattern.compile("\\d{4}");
    private static final Pattern MES = Pattern.compile("0[1-9]|1[0-2]");
    private static final String CABECALHO_CACHE =
            "competenciamov;uf;municipio;secao;saldomovimentacao;sexo;idade;racacor;salario";

    private final CagedProperties props;
    private final Map<String, Object> travas = new ConcurrentHashMap<>();

    public CagedDataService(CagedProperties props) {
        this.props = props;
    }

    /** Lê o mês (baixando e cacheando se necessário) e devolve só as linhas que passam no filtro. */
    public List<Movimentacao> ler(String ano, String mes, Predicate<Movimentacao> filtro) {
        Path cache = garantirCache(ano, mes);
        try (BufferedReader br = Files.newBufferedReader(cache, StandardCharsets.UTF_8)) {
            return lerLinhas(br, filtro);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------------------------------------------------------------ cache

    private Path garantirCache(String ano, String mes) {
        validar(ano, mes);
        Path pasta = Path.of(props.pastaDados());
        Path cache = pasta.resolve("CAGED_RS_%s%s.csv".formatted(ano, mes));
        if (Files.exists(cache)) {
            return cache;
        }

        Object trava = travas.computeIfAbsent(ano + mes, k -> new Object());
        synchronized (trava) {
            if (Files.exists(cache)) {
                return cache;
            }
            try {
                Files.createDirectories(pasta);
                Path arquivo7z = pasta.resolve("CAGEDMOV%s%s.7z".formatted(ano, mes));
                try {
                    baixar(ano, mes, arquivo7z);
                    converter7zParaCache(arquivo7z, cache);
                } finally {
                    Files.deleteIfExists(arquivo7z);
                }
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Falha ao obter dados do CAGED para %s/%s: %s".formatted(mes, ano, e.getMessage()), e);
            }
        }
        return cache;
    }

    private void validar(String ano, String mes) {
        if (!ANO.matcher(ano).matches() || !MES.matcher(mes).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe ano com 4 dígitos (ex: 2026) e mês com 2 dígitos (01 a 12).");
        }
    }

    // -------------------------------------------------------------------- FTP

    private void baixar(String ano, String mes, Path destino) throws IOException {
        String remoto = "%s/%s/%s%s/CAGEDMOV%s%s.7z".formatted(props.ftpDir(), ano, ano, mes, ano, mes);
        int timeoutMs = props.ftpTimeoutSegundos() * 1000;

        FTPClient ftp = new FTPClient();
        ftp.setConnectTimeout(timeoutMs);
        ftp.setDefaultTimeout(timeoutMs);
        ftp.setDataTimeout(Duration.ofMillis(timeoutMs));
        ftp.setBufferSize(1 << 20);

        try {
            log.info("Conectando ao FTP {} para baixar {}", props.ftpHost(), remoto);
            ftp.connect(props.ftpHost());
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new IOException("FTP recusou a conexão: " + ftp.getReplyString());
            }
            if (!ftp.login("anonymous", "anonymous@example.com")) {
                throw new IOException("Falha no login FTP anônimo");
            }
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(destino))) {
                if (!ftp.retrieveFile(remoto, out)) {
                    throw new ArquivoCagedNaoEncontradoException(
                            "Arquivo não encontrado para %s/%s (%s)".formatted(mes, ano, ftp.getReplyString().trim()));
                }
            }
        } finally {
            if (ftp.isConnected()) {
                try { ftp.logout(); } catch (IOException ignorado) { /* nada a fazer */ }
                try { ftp.disconnect(); } catch (IOException ignorado) { /* nada a fazer */ }
            }
        }
    }

    // --------------------------------------------------------------- 7z -> CSV

    private void converter7zParaCache(Path arquivo7z, Path cacheFinal) throws IOException {
        Path temporario = cacheFinal.resolveSibling(cacheFinal.getFileName() + ".tmp");
        Charset charset = Charset.forName(props.charset());

        try (SevenZFile sz = SevenZFile.builder().setFile(arquivo7z.toFile()).get();
             BufferedWriter out = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {

            boolean encontrou = false;
            SevenZArchiveEntry entrada;
            while ((entrada = sz.getNextEntry()) != null) {
                if (entrada.isDirectory() || !entrada.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                    continue;
                }
                encontrou = true;
                // lê direto do .7z, sem extrair o .txt gigante para o disco
                BufferedReader in = new BufferedReader(new InputStreamReader(sz.getInputStream(entrada), charset), 1 << 16);
                filtrarRs(in, out);
                break;
            }
            if (!encontrou) {
                throw new IOException("Nenhum .txt encontrado dentro de " + arquivo7z.getFileName());
            }
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(temporario);
            throw e;
        }

        Files.move(temporario, cacheFinal, StandardCopyOption.REPLACE_EXISTING);
        log.info("Cache gerado: {}", cacheFinal);
    }

    private void filtrarRs(BufferedReader in, BufferedWriter out) throws IOException {
        String cabecalho = in.readLine();
        if (cabecalho == null) {
            throw new IOException("Arquivo CAGED vazio");
        }
        Colunas c = Colunas.de(cabecalho);
        String ufRs = String.valueOf(props.ufRs());

        out.write(CABECALHO_CACHE);
        out.newLine();

        String linha;
        while ((linha = in.readLine()) != null) {
            String[] f = linha.split(";", -1);
            if (f.length <= c.maxIndice() || !f[c.uf()].trim().equals(ufRs)) {
                continue;
            }
            out.write(String.join(";",
                    f[c.competencia()], f[c.uf()], f[c.municipio()], f[c.secao()],
                    f[c.saldo()], f[c.sexo()], f[c.idade()], f[c.raca()], f[c.salario()]));
            out.newLine();
        }
    }

    // ---------------------------------------------------------------- leitura

    private List<Movimentacao> lerLinhas(BufferedReader br, Predicate<Movimentacao> filtro) throws IOException {
        String cabecalho = br.readLine();
        if (cabecalho == null) {
            return List.of();
        }
        Colunas c = Colunas.de(cabecalho);
        List<Movimentacao> resultado = new ArrayList<>();

        String linha;
        while ((linha = br.readLine()) != null) {
            String[] f = linha.split(";", -1);
            if (f.length <= c.maxIndice()) {
                continue;
            }
            Movimentacao m = converter(f, c);
            if (m != null && filtro.test(m)) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    private static Movimentacao converter(String[] f, Colunas c) {
        Integer competencia = inteiro(f[c.competencia()]);
        Integer municipio = inteiro(f[c.municipio()]);
        Integer saldo = inteiro(f[c.saldo()]);
        Integer idade = inteiro(f[c.idade()]);
        if (competencia == null || municipio == null || saldo == null || idade == null) {
            return null; // linha malformada
        }
        return new Movimentacao(
                competencia,
                municipio,
                f[c.secao()].trim(),
                saldo,
                descricaoSexo(inteiro(f[c.sexo()])),
                descricaoRaca(inteiro(f[c.raca()])),
                idade,
                decimal(f[c.salario()]));
    }

    private static String descricaoSexo(Integer codigo) {
        if (codigo == null) return "Outros";
        return switch (codigo) {
            case 1 -> "Homem";
            case 3 -> "Mulher";
            case 9 -> "Não identificado";
            default -> "Outros";
        };
    }

    private static String descricaoRaca(Integer codigo) {
        if (codigo == null) return "Outros";
        return switch (codigo) {
            case 1 -> "Branca";
            case 2 -> "Preta";
            case 3 -> "Parda";
            case 4 -> "Amarela";
            case 5 -> "Indígena";
            case 6 -> "Não informada";
            case 9 -> "Não identificado";
            default -> "Outros";
        };
    }

    private static Integer inteiro(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double decimal(String s) {
        String t = s.trim().replace(',', '.');
        if (t.isEmpty()) return null;
        try {
            return Double.valueOf(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String normalizar(String nome) {
        return Normalizer.normalize(nome.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    /** Posição de cada coluna, localizada pelo nome (sem acento) no cabeçalho. */
    private record Colunas(int competencia, int uf, int municipio, int secao,
                           int saldo, int sexo, int idade, int raca, int salario) {

        static Colunas de(String cabecalho) {
            String limpo = cabecalho.startsWith("\uFEFF") ? cabecalho.substring(1) : cabecalho;
            String[] nomes = limpo.split(";", -1);
            Map<String, Integer> pos = new HashMap<>();
            for (int i = 0; i < nomes.length; i++) {
                pos.put(normalizar(nomes[i]), i);
            }
            return new Colunas(
                    indice(pos, "competenciamov"),
                    indice(pos, "uf"),
                    indice(pos, "municipio"),
                    indice(pos, "secao"),
                    indice(pos, "saldomovimentacao"),
                    indice(pos, "sexo"),
                    indice(pos, "idade"),
                    indice(pos, "racacor"),
                    indice(pos, "salario"));
        }

        private static int indice(Map<String, Integer> pos, String nome) {
            Integer i = pos.get(nome);
            if (i == null) {
                throw new IllegalStateException("Coluna '" + nome + "' não encontrada no cabeçalho do arquivo");
            }
            return i;
        }

        int maxIndice() {
            return IntStream.of(competencia, uf, municipio, secao, saldo, sexo, idade, raca, salario).max().getAsInt();
        }
    }
}
