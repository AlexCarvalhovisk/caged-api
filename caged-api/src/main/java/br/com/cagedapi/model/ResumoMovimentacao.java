package br.com.cagedapi.model;

public record ResumoMovimentacao(
        int competencia,
        int municipio,
        String secao,
        String sexo,
        String raca,
        int idade,
        long admissoes,
        long desligamentos,
        long saldo,
        double salarioMedio,
        double salarioMediano) {
}
