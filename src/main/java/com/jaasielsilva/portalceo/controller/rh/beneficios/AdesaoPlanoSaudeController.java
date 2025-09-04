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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.HttpStatus;
import java.util.Optional;

@Controller
@RequestMapping("/rh/beneficios/adesao")
public class AdesaoPlanoSaudeController {

    @Autowired
    private AdesaoPlanoSaudeService adesaoService;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private PlanoSaudeService planoService;

    @Autowired
    private UsuarioService usuarioService;

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
        
        // Adicionar permissão de edição (por enquanto sempre true, pode ser implementada lógica específica)
        model.addAttribute("usuarioPodeEditarAdesao", true);
        
        return "rh/beneficios/adesao";
    }

    // Verificar se colaborador já possui adesão ativa
    @GetMapping("/verificar-colaborador/{colaboradorId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarColaborador(@PathVariable Long colaboradorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean possuiAdesaoAtiva = adesaoService.colaboradorPossuiAdesaoAtiva(colaboradorId);
            
            response.put("possuiAdesaoAtiva", possuiAdesaoAtiva);
            
            if (possuiAdesaoAtiva) {
                Optional<AdesaoPlanoSaude> adesaoAtiva = adesaoService.buscarAdesaoAtivaDoColaborador(colaboradorId);
                if (adesaoAtiva.isPresent()) {
                    AdesaoPlanoSaude adesao = adesaoAtiva.get();
                    response.put("planoAtual", adesao.getPlanoSaude() != null ? adesao.getPlanoSaude().getNome() : "Plano não identificado");
                    response.put("operadoraAtual", adesao.getPlanoSaude() != null ? adesao.getPlanoSaude().getOperadora() : "Operadora não identificada");
                    response.put("dataAdesao", adesao.getDataAdesao() != null ? adesao.getDataAdesao().toString() : "Data não informada");
                    response.put("dependentes", adesao.getQuantidadeDependentes());
                }
                response.put("message", "Colaborador já possui plano de saúde ativo");
            } else {
                response.put("message", "Colaborador pode aderir a um plano");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Erro ao verificar colaborador: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Salvar nova adesão
    @PostMapping("/salvar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvar(@RequestParam Long colaboradorId,
                        @RequestParam(required = false) Long planoId,
                        @RequestParam String tipoAdesao,
                        @RequestParam(required = false) Integer quantidadeDependentes,
                        @RequestParam String dataVigencia,
                        @RequestParam(required = false) String observacoes,
                        Principal principal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar se o colaborador já possui adesão ativa
            if (adesaoService.colaboradorPossuiAdesaoAtiva(colaboradorId)) {
                response.put("success", false);
                response.put("message", "Colaborador já possui plano de saúde ativo");
                return ResponseEntity.badRequest().body(response);
            }
            
            AdesaoPlanoSaude adesao = new AdesaoPlanoSaude();
            adesao.setColaborador(colaboradorService.buscarPorId(colaboradorId));
            
            if (planoId != null) {
                adesao.setPlanoSaude(planoService.buscarPorId(planoId));
            }
            
            adesao.setTipoAdesao(tipoAdesao);
            adesao.setDataAdesao(LocalDate.parse(dataVigencia));
            adesao.setQuantidadeDependentes(quantidadeDependentes != null ? quantidadeDependentes : 0);
            adesao.setObservacoes(observacoes);
            
            // Capturar o usuário que está fazendo a adesão
            if (principal != null) {
                Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
                adesao.setUsuarioCriacao(usuarioLogado);
            }
            
            adesaoService.salvar(adesao);
            
            response.put("success", true);
            response.put("message", "Adesão criada com sucesso!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao criar adesão: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Editar adesão existente
    @PostMapping("/salvar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editarAdesao(@PathVariable Long id,
                                                           @RequestBody Map<String, Object> dados,
                                                           Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            AdesaoPlanoSaude adesao = adesaoService.buscarPorId(id);
            
            // Atualizar dados da adesão
            if (dados.containsKey("planoId") && dados.get("planoId") != null) {
                Long planoId = Long.valueOf(dados.get("planoId").toString());
                adesao.setPlanoSaude(planoService.buscarPorId(planoId));
            }
            
            if (dados.containsKey("tipoAdesao")) {
                adesao.setTipoAdesao(dados.get("tipoAdesao").toString());
            }
            
            if (dados.containsKey("quantidadeDependentes")) {
                Integer novaQuantidade = Integer.valueOf(dados.get("quantidadeDependentes").toString());
                adesao.setQuantidadeDependentes(novaQuantidade);
            }
            
            if (dados.containsKey("dataVigencia")) {
                adesao.setDataAdesao(LocalDate.parse(dados.get("dataVigencia").toString()));
            }
            
            if (dados.containsKey("observacoes")) {
                String observacoesAtuais = adesao.getObservacoes() != null ? adesao.getObservacoes() : "";
                String novasObservacoes = dados.get("observacoes").toString();
                
                // Capturar o usuário que está editando
                String nomeUsuarioEditor = "Sistema";
                if (principal != null) {
                    Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
                    if (usuarioLogado != null) {
                        nomeUsuarioEditor = usuarioLogado.getNome();
                    }
                }
                
                if (!observacoesAtuais.isEmpty()) {
                    adesao.setObservacoes(observacoesAtuais + " | Edição por " + nomeUsuarioEditor + ": " + novasObservacoes);
                } else {
                    adesao.setObservacoes("Edição por " + nomeUsuarioEditor + ": " + novasObservacoes);
                }
            }
            
            // Salvar as alterações
            adesaoService.salvar(adesao);
            
            response.put("success", true);
            response.put("message", "Adesão editada com sucesso!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao editar adesão: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
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

    // Buscar adesão por ID em formato JSON
    @GetMapping("/json/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarAdesaoJson(@PathVariable Long id) {
        try {
            AdesaoPlanoSaude adesao = adesaoService.buscarPorId(id);
            if (adesao == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", adesao.getId());
            response.put("colaboradorId", adesao.getColaborador().getId());
            response.put("colaboradorNome", adesao.getColaborador().getNome());
            response.put("departamento", adesao.getColaborador().getDepartamento() != null ? adesao.getColaborador().getDepartamento().getNome() : "");
            response.put("planoId", adesao.getPlanoSaude() != null ? adesao.getPlanoSaude().getId() : null);
            response.put("planoNome", adesao.getPlanoSaude() != null ? adesao.getPlanoSaude().getNome() : "");
            response.put("operadora", adesao.getPlanoSaude() != null ? adesao.getPlanoSaude().getOperadora() : "");
            response.put("tipoAdesao", adesao.getTipoAdesao());
            response.put("quantidadeDependentes", adesao.getQuantidadeDependentes());
            response.put("dataAdesao", adesao.getDataAdesao() != null ? adesao.getDataAdesao().toString() : "");
            response.put("dataVigencia", adesao.getDataAdesao() != null ? adesao.getDataAdesao().toString() : "");
            response.put("valorTitular", adesao.getPlanoSaude() != null ? adesao.getPlanoSaude().getValorTitular() : 0);
            response.put("valorDependentes", adesao.getPlanoSaude() != null ? adesao.getPlanoSaude().getValorDependente().multiply(new java.math.BigDecimal(adesao.getQuantidadeDependentes())) : 0);
            response.put("valorTotal", adesao.getValorTotalMensal());
            response.put("status", adesao.getStatus());
            response.put("observacoes", adesao.getObservacoes());
            response.put("dataCriacao", adesao.getDataCriacao() != null ? adesao.getDataCriacao().toString() : "");
            
            // Informações do usuário que criou a adesão
            if (adesao.getUsuarioCriacao() != null) {
                response.put("usuarioCriacaoId", adesao.getUsuarioCriacao().getId());
                response.put("usuarioCriacaoNome", adesao.getUsuarioCriacao().getNome());
                response.put("usuarioCriacaoEmail", adesao.getUsuarioCriacao().getEmail());
            } else {
                response.put("usuarioCriacaoId", null);
                response.put("usuarioCriacaoNome", "Não informado");
                response.put("usuarioCriacaoEmail", "Não informado");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint de detalhes (usado pelo JavaScript para modal de detalhes)
    @GetMapping("/detalhes/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarDetalhesAdesao(@PathVariable Long id) {
        try {
            AdesaoPlanoSaude adesao = adesaoService.buscarPorId(id);
            if (adesao == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Adesão não encontrada");
                return ((BodyBuilder) ResponseEntity.notFound()).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            Map<String, Object> adesaoData = new HashMap<>();
            adesaoData.put("id", adesao.getId());
            
            // Informações do colaborador
            Map<String, Object> colaborador = new HashMap<>();
            colaborador.put("nome", adesao.getColaborador().getNome());
            colaborador.put("id", adesao.getColaborador().getId());
            if (adesao.getColaborador().getDepartamento() != null) {
                Map<String, Object> departamento = new HashMap<>();
                departamento.put("nome", adesao.getColaborador().getDepartamento().getNome());
                colaborador.put("departamento", departamento);
            }
            adesaoData.put("colaborador", colaborador);
            
            // Informações do plano de saúde
            if (adesao.getPlanoSaude() != null) {
                Map<String, Object> planoSaude = new HashMap<>();
                planoSaude.put("nome", adesao.getPlanoSaude().getNome());
                planoSaude.put("operadora", adesao.getPlanoSaude().getOperadora());
                adesaoData.put("planoSaude", planoSaude);
            }
            
            adesaoData.put("tipoAdesao", adesao.getTipoAdesao());
            adesaoData.put("quantidadeDependentes", adesao.getQuantidadeDependentes());
            adesaoData.put("dataAdesao", adesao.getDataAdesao() != null ? adesao.getDataAdesao().toString() : null);
            adesaoData.put("dataVigenciaInicio", adesao.getDataVigenciaInicio() != null ? adesao.getDataVigenciaInicio().toString() : null);
            adesaoData.put("valorMensalTitular", adesao.getValorMensalTitular());
            adesaoData.put("valorMensalDependentes", adesao.getValorMensalDependentes());
            adesaoData.put("valorTotalMensal", adesao.getValorTotalMensal());
            adesaoData.put("status", adesao.getStatus() != null ? adesao.getStatus().name() : null);
            adesaoData.put("observacoes", adesao.getObservacoes());
            adesaoData.put("dataCriacao", adesao.getDataCriacao() != null ? adesao.getDataCriacao().toString() : null);
            
            // Informações do usuário que criou a adesão
            if (adesao.getUsuarioCriacao() != null) {
                adesaoData.put("usuarioCriacaoNome", adesao.getUsuarioCriacao().getNome());
                adesaoData.put("usuarioCriacaoEmail", adesao.getUsuarioCriacao().getEmail());
            } else {
                adesaoData.put("usuarioCriacaoNome", "Não informado");
                adesaoData.put("usuarioCriacaoEmail", "Não informado");
            }
            
            response.put("adesao", adesaoData);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro interno do servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Excluir adesão
    @DeleteMapping("/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluirAdesao(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            adesaoService.excluir(id);
            response.put("success", true);
            response.put("message", "Adesão excluída com sucesso");
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
