package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.service.AdesaoPlanoSaudeService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.PlanoSaudeService;

import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
    public String listar(Model model) {
        model.addAttribute("adesoes", adesaoService.listarAtivos());
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
    public String salvar(@ModelAttribute AdesaoPlanoSaude adesao) {
        adesaoService.salvar(adesao);
        return "redirect:/rh/beneficios/adesao";
    }

    // Remover adesão
    @PostMapping("/remover/{id}")
    public String remover(@PathVariable Long id) {
        adesaoService.deletar(id);
        return "redirect:/rh/beneficios/adesao";
    }

    // Endpoint para salvar adesão via JSON (AJAX)
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> criarAdesao(@RequestBody Map<String, Object> dadosAdesao) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validações básicas
            if (!dadosAdesao.containsKey("colaboradorId") || dadosAdesao.get("colaboradorId") == null) {
                response.put("success", false);
                response.put("message", "Colaborador é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosAdesao.containsKey("planoId") || dadosAdesao.get("planoId") == null) {
                response.put("success", false);
                response.put("message", "Plano de saúde é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosAdesao.containsKey("dataVigencia") || dadosAdesao.get("dataVigencia") == null) {
                response.put("success", false);
                response.put("message", "Data de vigência é obrigatória");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dadosAdesao.containsKey("tipoAdesao") || dadosAdesao.get("tipoAdesao") == null) {
                response.put("success", false);
                response.put("message", "Tipo de adesão é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }

            // Criar nova adesão
            AdesaoPlanoSaude adesao = new AdesaoPlanoSaude();

            // Buscar colaborador
            Long colaboradorId = Long.valueOf(dadosAdesao.get("colaboradorId").toString());
            var colaborador = colaboradorService.buscarPorId(colaboradorId);
            if (colaborador == null) {
                response.put("success", false);
                response.put("message", "Colaborador não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            adesao.setColaborador(colaborador);

            // Buscar plano de saúde
            Long planoId = Long.valueOf(dadosAdesao.get("planoId").toString());
            var plano = planoService.buscarPorId(planoId);
            if (plano == null) {
                response.put("success", false);
                response.put("message", "Plano de saúde não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            adesao.setPlanoSaude(plano);

            // Quantidade de dependentes
            Integer qtdDependentes = dadosAdesao.containsKey("quantidadeDependentes")
                    ? Integer.valueOf(dadosAdesao.get("quantidadeDependentes").toString())
                    : 0;
            if (qtdDependentes < 0)
                qtdDependentes = 0;
            if (qtdDependentes > 10)
                qtdDependentes = 10;
            adesao.setQuantidadeDependentes(qtdDependentes);

            // Data de vigência
            String dataStr = dadosAdesao.get("dataVigencia").toString();
            LocalDate dataVigencia = LocalDate.parse(dataStr);
            adesao.setDataVigenciaInicio(dataVigencia);

            // Tipo de adesão
            adesao.setTipoAdesao(dadosAdesao.get("tipoAdesao").toString());

            // Observações
            if (dadosAdesao.containsKey("observacoes")) {
                adesao.setObservacoes(dadosAdesao.get("observacoes").toString());
            }

            // Data da adesão
            adesao.setDataAdesao(LocalDate.now());

            // Salvar adesão
            AdesaoPlanoSaude adesaoSalva = adesaoService.salvar(adesao);

            response.put("success", true);
            response.put("adesaoId", adesaoSalva.getId());
            response.put("valorTotal", adesaoSalva.getValorTotalMensal());
            response.put("message", "Adesão salva com sucesso!");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Erro numérico: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "Data inválida. Formato esperado: YYYY-MM-DD");
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro interno: " + e.getMessage());
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
            // TODO: Implementar entidade e service específicos para ValeRefeicao
            
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
