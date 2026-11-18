package br.com.cagedapi.model;

/**
 * Filtros da consulta.
 * - municipio: 0 (ou null) = todo o RS
 * - sexo / raca: null, vazio ou "todos" = sem filtro
 * - raca "Negra" = Preta + Parda (critério do IBGE para população negra)
 */
public record FiltroCaged(
        Integer municipio,
        String sexo,
        String raca,
        Integer idadeMin,
        Integer idadeMax,
        Double salarioMin,
        Double salarioMax) {

    public boolean aceita(Movimentacao m) {
        if (municipio != null && municipio > 0 && m.municipio() != municipio) return false;
        if (ativo(sexo) && !m.sexo().equalsIgnoreCase(sexo.trim())) return false;
        if (ativo(raca) && !aceitaRaca(m.raca())) return false;
        if (idadeMin != null && m.idade() < idadeMin) return false;
        if (idadeMax != null && m.idade() > idadeMax) return false;
        if (salarioMin != null && (m.salario() == null || m.salario() < salarioMin)) return false;
        if (salarioMax != null && (m.salario() == null || m.salario() > salarioMax)) return false;
        return true;
    }

    private boolean aceitaRaca(String descricao) {
        String r = raca.trim();
        if (r.equalsIgnoreCase("Negra") || r.equalsIgnoreCase("Negro") || r.equalsIgnoreCase("Negros")) {
            return descricao.equals("Preta") || descricao.equals("Parda");
        }
        return descricao.equalsIgnoreCase(r);
    }

    private static boolean ativo(String valor) {
        return valor != null
                && !valor.isBlank()
                && !valor.equalsIgnoreCase("todos")
                && !valor.equalsIgnoreCase("todas");
    }
}
