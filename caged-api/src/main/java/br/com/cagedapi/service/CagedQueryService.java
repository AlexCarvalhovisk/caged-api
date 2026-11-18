package br.com.cagedapi.service;

import br.com.cagedapi.exception.ArquivoCagedNaoEncontradoException;
import br.com.cagedapi.model.FiltroCaged;
import br.com.cagedapi.model.Movimentacao;
import br.com.cagedapi.model.ResumoMovimentacao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CagedQueryService {

    private static final Comparator<ResumoMovimentacao> ORDEM =
            Comparator.comparingInt(ResumoMovimentacao::competencia)
                    .thenComparingInt(ResumoMovimentacao::municipio)
                    .thenComparing(ResumoMovimentacao::secao)
                    .thenComparing(ResumoMovimentacao::sexo)
                    .thenComparing(ResumoMovimentacao::raca)
                    .thenComparingInt(ResumoMovimentacao::idade);

    private final CagedDataService dados;

    public CagedQueryService(CagedDataService dados) {
        this.dados = dados;
    }

    public List<?> consultarMes(String ano, String mes, FiltroCaged filtro, boolean agregado) {
        List<Movimentacao> movimentacoes = dados.ler(ano, mes, filtro::aceita);
        if (agregado) {
            return agregar(movimentacoes);
        }
        return movimentacoes;
    }

    public List<?> consultarAno(String ano, FiltroCaged filtro, boolean agregado) {
        List<Movimentacao> todas = new ArrayList<>();
        boolean encontrouAlgum = false;

        for (int m = 1; m <= 12; m++) {
            try {
                todas.addAll(dados.ler(ano, "%02d".formatted(m), filtro::aceita));
                encontrouAlgum = true;
            } catch (ArquivoCagedNaoEncontradoException e) {
                // mês futuro, ainda não publicado pelo MTE: ignora
            }
        }

        if (!encontrouAlgum) {
            throw new ArquivoCagedNaoEncontradoException(
                    "Nenhum dado encontrado no FTP do MTE para o ano " + ano + ".");
        }
        if (agregado) {
            return agregar(todas);
        }
        return todas;
    }

    // -------------------------------------------------------------- agregação

    private record ChaveResumo(int competencia, int municipio, String secao, String sexo, String raca, int idade) {
    }

    private List<ResumoMovimentacao> agregar(List<Movimentacao> movimentacoes) {
        Map<ChaveResumo, List<Movimentacao>> grupos = movimentacoes.stream()
                .collect(Collectors.groupingBy(m -> new ChaveResumo(
                        m.competencia(), m.municipio(), m.secao(), m.sexo(), m.raca(), m.idade())));

        return grupos.entrySet().stream()
                .map(e -> resumir(e.getKey(), e.getValue()))
                .sorted(ORDEM)
                .toList();
    }

    private ResumoMovimentacao resumir(ChaveResumo k, List<Movimentacao> lista) {
        long admissoes = lista.stream().filter(m -> m.saldo() == 1).count();
        long desligamentos = lista.stream().filter(m -> m.saldo() == -1).count();
        long saldo = lista.stream().mapToLong(Movimentacao::saldo).sum();

        double[] salarios = lista.stream()
                .map(Movimentacao::salario)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sorted()
                .toArray();

        double media = salarios.length == 0 ? 0 : Arrays.stream(salarios).average().orElse(0);

        return new ResumoMovimentacao(
                k.competencia(), k.municipio(), k.secao(), k.sexo(), k.raca(), k.idade(),
                admissoes, desligamentos, saldo,
                arredondar(media), arredondar(mediana(salarios)));
    }

    private static double mediana(double[] ordenado) {
        int n = ordenado.length;
        if (n == 0) return 0;
        return n % 2 == 1 ? ordenado[n / 2] : (ordenado[n / 2 - 1] + ordenado[n / 2]) / 2.0;
    }

    private static double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
