package br.com.cagedapi.model;

/**
 * Uma linha do CAGED (já filtrada para o RS), com sexo e raça/cor descritos.
 * Salário é null quando ausente no arquivo original.
 */
public record Movimentacao(
        int competencia,
        int municipio,
        String secao,
        int saldo,
        String sexo,
        String raca,
        int idade,
        Double salario) {
}
