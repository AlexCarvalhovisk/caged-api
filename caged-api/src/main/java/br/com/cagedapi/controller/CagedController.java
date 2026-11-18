package br.com.cagedapi.controller;

import br.com.cagedapi.model.FiltroCaged;
import br.com.cagedapi.service.CagedQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CagedController {

    private final CagedQueryService service;

    public CagedController(CagedQueryService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of("status",
                "API CAGED RS online! Acesse http://127.0.0.1:8000/swagger-ui.html para testar.");
    }

    @Operation(summary = "Movimentações do CAGED em um mês")
    @GetMapping("/rs/movimentacoes")
    public List<?> consultarMes(
            @Parameter(description = "Ano com 4 dígitos (ex: 2026)")
            @RequestParam(defaultValue = "2026") String ano,
            @Parameter(description = "Mês com 2 dígitos (01 a 12)")
            @RequestParam(defaultValue = "01") String mes,
            @Parameter(description = "432300 = Viamão (0 = todo o RS)")
            @RequestParam(defaultValue = "${caged.cod-viamao}") Integer municipio,
            @Parameter(description = "Homem ou Mulher (vazio/todos = sem filtro)")
            @RequestParam(required = false) String sexo,
            @Parameter(description = "Branca, Preta, Parda, Amarela, Indígena ou Negra (Preta + Parda)")
            @RequestParam(required = false) String raca,
            @Parameter(description = "Idade mínima")
            @RequestParam(name = "idade_min", required = false) Integer idadeMin,
            @Parameter(description = "Idade máxima")
            @RequestParam(name = "idade_max", required = false) Integer idadeMax,
            @Parameter(description = "Salário mínimo em R$")
            @RequestParam(name = "salario_min", required = false) Double salarioMin,
            @Parameter(description = "Salário máximo em R$")
            @RequestParam(name = "salario_max", required = false) Double salarioMax,
            @Parameter(description = "true = agrupa os dados (recomendado para Power BI)")
            @RequestParam(defaultValue = "true") boolean agregado) {

        FiltroCaged filtro = new FiltroCaged(municipio, sexo, raca, idadeMin, idadeMax, salarioMin, salarioMax);
        return service.consultarMes(ano, mes, filtro, agregado);
    }

    @Operation(summary = "Movimentações do CAGED consolidadas no ano (meses já publicados)")
    @GetMapping("/rs/movimentacoes-ano")
    public List<?> consultarAno(
            @Parameter(description = "Ano completo para consolidar (ex: 2026)")
            @RequestParam(defaultValue = "2026") String ano,
            @Parameter(description = "432300 = Viamão (0 = todo o RS)")
            @RequestParam(defaultValue = "${caged.cod-viamao}") Integer municipio,
            @Parameter(description = "Homem ou Mulher (todos = sem filtro)")
            @RequestParam(defaultValue = "Homem") String sexo,
            @Parameter(description = "Branca, Preta, Parda, Amarela, Indígena ou Negra (Preta + Parda); todos = sem filtro")
            @RequestParam(defaultValue = "Preta") String raca,
            @Parameter(description = "Idade mínima")
            @RequestParam(name = "idade_min", required = false) Integer idadeMin,
            @Parameter(description = "Idade máxima")
            @RequestParam(name = "idade_max", required = false) Integer idadeMax,
            @Parameter(description = "Salário mínimo em R$")
            @RequestParam(name = "salario_min", required = false) Double salarioMin,
            @Parameter(description = "Salário máximo em R$")
            @RequestParam(name = "salario_max", required = false) Double salarioMax,
            @Parameter(description = "true = agrupa os dados (recomendado para Power BI)")
            @RequestParam(defaultValue = "true") boolean agregado) {

        FiltroCaged filtro = new FiltroCaged(municipio, sexo, raca, idadeMin, idadeMax, salarioMin, salarioMax);
        return service.consultarAno(ano, filtro, agregado);
    }
}
