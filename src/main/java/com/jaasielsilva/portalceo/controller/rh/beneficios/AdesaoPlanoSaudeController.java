package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.service.AdesaoPlanoSaudeService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.PlanoSaudeService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.model.Usuario;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.security.Principal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/rh/beneficios/adesao")
public class AdesaoPlanoSaudeController {

    @Autowired
    private AdesaoPlanoSaudeService adesaoService;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private PlanoSaudeService planoService;

    // Listar adesões
    @GetMapping
    public String listar(Model model, Principal principal) {

        // Lista de adesões, colaboradores e planos ativos
        model.addAttribute("adesoes", adesaoService.listarTodos());
        model.addAttribute("colaboradores", colaboradorService.listarTodos());
        model.addAttribute("plano_de_saude", planoService.listarTodosAtivos());

        // Map para enviar valores de plano para o JS
        var valoresPlanosJson = planoService.listarTodosAtivos()
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getId().toString(),
                        p -> new Object() {
                            public final double titular = p.getValorTitular().doubleValue();
                            public final double dependente = p.getValorDependente().doubleValue();
                        }));
        model.addAttribute("valoresPlanosJson", valoresPlanosJson);
        return "rh/beneficios/adesao";
    }

    // Salvar nova adesão
    @PostMapping("/salvar")
    public String salvar(@RequestParam Long colaboradorId,
                        @RequestParam(required = false) Long planoId,
                        @RequestParam String tipoAdesao,
                        @RequestParam(required = false) Integer quantidadeDependentes,
                        @RequestParam String dataVigencia,
                        @RequestParam(required = false) String observacoes) {
        
        AdesaoPlanoSaude adesao = new AdesaoPlanoSaude();
        adesao.setColaborador(colaboradorService.buscarPorId(colaboradorId));
        
        if (planoId != null) {
            adesao.setPlanoSaude(planoService.buscarPorId(planoId));
        }
        
        adesao.setTipoAdesao(tipoAdesao);
        adesao.setDataAdesao(LocalDate.parse(dataVigencia));
        adesao.setQuantidadeDependentes(quantidadeDependentes != null ? quantidadeDependentes : 0);
        adesao.setObservacoes(observacoes);
        
        adesaoService.salvar(adesao);
        return "redirect:/rh/beneficios/adesao";
    }

    // Cancelar adesão
    @PostMapping("/cancelar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelarAdesao(@PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> dados) {
        Map<String, Object> response = new HashMap<>();

        try {
            String motivo = dados != null && dados.containsKey("motivo") ? (String) dados.get("motivo") : null;
            adesaoService.cancelarAdesao(id, motivo);

            response.put("success", true);
            response.put("message", "Adesão cancelada com sucesso");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Endpoint para salvar Vale Refeição
    @PostMapping("/vale-refeicao/salvar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarValeRefeicao(@RequestBody Map<String, Object> dadosVale) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validações básicas
            if (!dadosVale.containsKey("colaboradorId") || dadosVale.get("colaboradorId") == null) {
                response.put("success", false);
                response.put("message", "Colaborador é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosVale.containsKey("valorMensal") || dadosVale.get("valorMensal") == null) {
                response.put("success", false);
                response.put("message", "Valor mensal é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosVale.containsKey("dataInicio") || dadosVale.get("dataInicio") == null) {
                response.put("success", false);
                response.put("message", "Data de início é obrigatória");
                return ResponseEntity.badRequest().body(response);
            }

            // Buscar colaborador
            Long colaboradorId = Long.valueOf(dadosVale.get("colaboradorId").toString());
            var colaborador = colaboradorService.buscarPorId(colaboradorId);
            if (colaborador == null) {
                response.put("success", false);
                response.put("message", "Colaborador não encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            // Simular salvamento do vale refeição

            response.put("success", true);
            response.put("message", "Vale Refeição salvo com sucesso!");
            response.put("tipo", "vale-refeicao");
            response.put("colaborador", colaborador.getNome());
            response.put("valor", dadosVale.get("valorMensal"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao salvar Vale Refeição: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint para salvar Vale Transporte
    @PostMapping("/vale-transporte/salvar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarValeTransporte(@RequestBody Map<String, Object> dadosVale) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validações básicas
            if (!dadosVale.containsKey("colaboradorId") || dadosVale.get("colaboradorId") == null) {
                response.put("success", false);
                response.put("message", "Colaborador é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosVale.containsKey("valorMensal") || dadosVale.get("valorMensal") == null) {
                response.put("success", false);
                response.put("message", "Valor mensal é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosVale.containsKey("dataInicio") || dadosVale.get("dataInicio") == null) {
                response.put("success", false);
                response.put("message", "Data de início é obrigatória");
                return ResponseEntity.badRequest().body(response);
            }

            // Buscar colaborador
            Long colaboradorId = Long.valueOf(dadosVale.get("colaboradorId").toString());
            var colaborador = colaboradorService.buscarPorId(colaboradorId);
            if (colaborador == null) {
                response.put("success", false);
                response.put("message", "Colaborador não encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            // Simular salvamento do vale transporte
            // TODO: Implementar entidade e service específicos para ValeTransporte

            response.put("success", true);
            response.put("message", "Vale Transporte salvo com sucesso!");
            response.put("tipo", "vale-transporte");
            response.put("colaborador", colaborador.getNome());
            response.put("valor", dadosVale.get("valorMensal"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao salvar Vale Transporte: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint para salvar Vale Alimentação
    @PostMapping("/vale-alimentacao/salvar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarValeAlimentacao(@RequestBody Map<String, Object> dadosVale) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validações básicas
            if (!dadosVale.containsKey("colaboradorId") || dadosVale.get("colaboradorId") == null) {
                response.put("success", false);
                response.put("message", "Colaborador é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosVale.containsKey("valorMensal") || dadosVale.get("valorMensal") == null) {
                response.put("success", false);
                response.put("message", "Valor mensal é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosVale.containsKey("dataInicio") || dadosVale.get("dataInicio") == null) {
                response.put("success", false);
                response.put("message", "Data de início é obrigatória");
                return ResponseEntity.badRequest().body(response);
            }

            // Buscar colaborador
            Long colaboradorId = Long.valueOf(dadosVale.get("colaboradorId").toString());
            var colaborador = colaboradorService.buscarPorId(colaboradorId);
            if (colaborador == null) {
                response.put("success", false);
                response.put("message", "Colaborador não encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            // Simular salvamento do vale alimentação
            // TODO: Implementar entidade e service específicos para ValeAlimentacao

            response.put("success", true);
            response.put("message", "Vale Alimentação salvo com sucesso!");
            response.put("tipo", "vale-alimentacao");
            response.put("colaborador", colaborador.getNome());
            response.put("valor", dadosVale.get("valorMensal"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao salvar Vale Alimentação: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
