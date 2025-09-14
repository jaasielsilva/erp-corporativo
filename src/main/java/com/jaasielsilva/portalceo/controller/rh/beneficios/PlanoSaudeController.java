package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.service.AdesaoPlanoSaudeService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.PlanoSaudeService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/rh/beneficios/plano-saude")
public class PlanoSaudeController {

    @Autowired
    private PlanoSaudeService planoSaudeService;

    @Autowired
    private AdesaoPlanoSaudeService adesaoPlanoSaudeService;

    @Autowired
    private ColaboradorService colaboradorService;

    @GetMapping
    public String listar(Model model) {
         List<PlanoSaude> planos = planoSaudeService.listarTodosAtivos();
        model.addAttribute("planos", planos);
        model.addAttribute("custoMensal", adesaoPlanoSaudeService.calcularCustoMensalTotal());
        model.addAttribute("custoEmpresa", adesaoPlanoSaudeService.calcularCustoEmpresa());
        model.addAttribute("descontoColaboradores", adesaoPlanoSaudeService.calcularDescontoColaboradoresPercentual());
        model.addAttribute("totalBeneficiarios", adesaoPlanoSaudeService.contarTotalBeneficiarios());
        model.addAttribute("titulares", adesaoPlanoSaudeService.contarTitulares());
        model.addAttribute("dependentes", adesaoPlanoSaudeService.contarDependentes());
        return "rh/beneficios/plano-de-saude/plano-saude";
    }

   @PostMapping("/adesao/salvar")
@ResponseBody
public String salvarAdesao(@RequestParam Long colaboradorId,
                           @RequestParam(required = false) Long planoSaudeId,
                           @RequestParam String dataInicio,
                           @RequestParam(required = false) Integer quantidadeDependentes,
                           @RequestParam(required = false) String observacoes) {

    AdesaoPlanoSaude adesao = new AdesaoPlanoSaude();
    adesao.setColaborador(colaboradorService.buscarPorId(colaboradorId));

    if (planoSaudeId != null) {
        adesao.setPlanoSaude(planoSaudeService.buscarPorId(planoSaudeId));
    }

    adesao.setDataAdesao(LocalDate.parse(dataInicio));
    adesao.setQuantidadeDependentes(quantidadeDependentes != null ? quantidadeDependentes : 0);
    adesao.setObservacoes(observacoes);

    adesaoPlanoSaudeService.salvar(adesao);
    return "Adesão salva com sucesso!";
}

    // Editar adesão existente
    @PostMapping("/adesao/editar/{id}")
    @ResponseBody
    public String editarAdesao(@PathVariable Long id,
                              @RequestParam Integer quantidadeDependentes,
                              @RequestParam(required = false) String observacoes) {
        
        try {
            AdesaoPlanoSaude adesao = adesaoPlanoSaudeService.buscarPorId(id);
            
            // Salvar valores antigos para histórico
            Integer dependentesAntigos = adesao.getQuantidadeDependentes();
            
            // Atualizar quantidade de dependentes
            adesao.setQuantidadeDependentes(quantidadeDependentes);
            
            // Adicionar observação sobre a alteração
            String observacaoAtual = adesao.getObservacoes() != null ? adesao.getObservacoes() : "";
            String novaObservacao = "Edição: dependentes alterados de " + dependentesAntigos + " para " + quantidadeDependentes;
            
            if (observacoes != null && !observacoes.trim().isEmpty()) {
                novaObservacao += " | " + observacoes;
            }
            
            if (!observacaoAtual.isEmpty()) {
                adesao.setObservacoes(observacaoAtual + " | " + novaObservacao);
            } else {
                adesao.setObservacoes(novaObservacao);
            }
            
            // Salvar as alterações
            adesaoPlanoSaudeService.salvar(adesao);
            
            return "Adesão editada com sucesso!";
            
        } catch (Exception e) {
            return "Erro ao editar adesão: " + e.getMessage();
        }
    }

    // API para buscar uma adesão específica por ID
    @GetMapping("/api/adesao/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarAdesaoApi(@PathVariable Long id) {
        try {
            AdesaoPlanoSaude adesao = adesaoPlanoSaudeService.buscarPorId(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", adesao.getId());
            response.put("colaborador", Map.of(
                "nome", adesao.getColaborador().getNome(),
                "id", adesao.getColaborador().getId()
            ));
            response.put("planoSaude", Map.of(
                "nome", adesao.getPlanoSaude().getNome(),
                "operadora", adesao.getPlanoSaude().getOperadora(),
                "id", adesao.getPlanoSaude().getId()
            ));
            response.put("quantidadeDependentes", adesao.getQuantidadeDependentes());
            response.put("valorTotalMensal", adesao.getValorColaboradorAtual());
            response.put("dataAdesao", adesao.getDataAdesao().toString());
            response.put("status", adesao.getStatus());
            response.put("observacoes", adesao.getObservacoes());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API ADICIONAL: Endpoint compatível com o JavaScript existente
    @GetMapping("/api/rh/beneficios/plano-saude/adesao/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarAdesaoApiCompatible(@PathVariable Long id) {
        return buscarAdesaoApi(id);
    }

    // API para listar todas as adesões
    @GetMapping("/api/adesoes/listar")
    @ResponseBody
    public List<AdesaoPlanoSaude> listarAdesoesApi() {
        return adesaoPlanoSaudeService.listarTodosAtivos();
    }

    // API ADICIONAL: Endpoint compatível com o JavaScript existente
    @GetMapping("/api/rh/beneficios/plano-saude/adesoes/listar")
    @ResponseBody
    public List<AdesaoPlanoSaude> listarAdesoesApiCompatible() {
        return listarAdesoesApi();
    }

    // DEBUG: Endpoint para verificar planos no banco
    @GetMapping("/debug/planos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugPlanos() {
        try {
            List<PlanoSaude> planos = planoSaudeService.listarTodosAtivos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalPlanos", planos.size());
            response.put("planos", planos);
            
            // Log detalhado
            System.out.println("=== DEBUG PLANOS DE SAÚDE ===");
            System.out.println("Total de planos encontrados: " + planos.size());
            
            for (PlanoSaude plano : planos) {
                System.out.println("Plano: " + plano.getNome() + " | ID: " + plano.getId() + 
                                 " | Código: " + plano.getCodigo() + " | Valor: R$ " + plano.getValorTitular());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Erro no debug de planos: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

