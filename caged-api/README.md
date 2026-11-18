# API CAGED - RS (Viamão)

Java 21 + Spring Boot 3.4.

## Executar
    mvn spring-boot:run
    # ou: mvn clean package && java -jar target/caged-api-1.0.0.jar

- API: http://127.0.0.1:8000
- Documentação interativa: http://127.0.0.1:8000/swagger-ui.html

## Exemplos
    /rs/movimentacoes?ano=2026&mes=03&sexo=Homem&raca=Negra
    /rs/movimentacoes-ano?ano=2026&sexo=Homem&raca=Negra
    /rs/movimentacoes-ano?ano=2026&sexo=todos&raca=todos&municipio=0   (todo o RS, sem filtros)

## Power BI (Obter Dados > Consulta em Branco > Editor Avançado)
    let
        Fonte = Json.Document(Web.Contents(
            "http://127.0.0.1:8000/rs/movimentacoes-ano",
            [Query = [ano = "2026", sexo = "Homem", raca = "Negra"],
             Timeout = #duration(0, 0, 15, 0)])),
        Tabela = Table.FromList(Fonte, Splitter.SplitByNothing(), null, null, ExtraValues.Error),
        Expandido = Table.ExpandRecordColumn(Tabela, "Column1",
            {"competencia", "municipio", "secao", "sexo", "raca", "idade",
             "admissoes", "desligamentos", "saldo", "salarioMedio", "salarioMediano"})
    in
        Expandido

Dica: na primeira vez cada mês é baixado do FTP (pode demorar). Chame a URL
no navegador antes para aquecer o cache (pasta `dados_rs/`).
