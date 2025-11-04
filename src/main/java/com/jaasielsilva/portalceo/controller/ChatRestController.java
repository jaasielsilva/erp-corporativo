package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.ConversaDTO;
import com.jaasielsilva.portalceo.model.Conversa;
import com.jaasielsilva.portalceo.model.Mensagem;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.repository.ConversaRepository;
import com.jaasielsilva.portalceo.repository.MensagemRepository;
import com.jaasielsilva.portalceo.repository.ParticipanteConversaRepository;
import com.jaasielsilva.portalceo.service.ChatService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller para o Sistema de Chat Interno
 * Fornece APIs para gerenciamento de conversas, mensagens e usuários
 * 
 * @author Sistema ERP
 * @version 2.0
 */
@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private ParticipanteConversaRepository participanteConversaRepository;

    // ==================== CONVERSAS ====================

    /**
     * Lista todas as conversas do usuário logado
     */
    @GetMapping("/conversas")
    public ResponseEntity<List<ConversaDTO>> listarConversas(Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }
            List<ConversaDTO> conversas = chatService.buscarConversasDoUsuarioDTO(usuario.getId(), usuario);
            return ResponseEntity.ok(conversas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cria uma nova conversa individual
     */
    @PostMapping("/conversas/individual")
    public ResponseEntity<Conversa> criarConversaIndividual(
            @RequestParam Long destinatarioId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            Conversa conversa = chatService.criarConversaIndividual(usuario.getId(), destinatarioId);
            return ResponseEntity.ok(conversa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cria uma nova conversa em grupo
     */
    @PostMapping("/conversas/grupo")
    public ResponseEntity<Conversa> criarConversaGrupo(
            @RequestBody Map<String, Object> request,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            String titulo = (String) request.get("titulo");
            @SuppressWarnings("unchecked")
            List<Long> participantesIds = (List<Long>) request.get("participantes");

            if (titulo == null || titulo.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Conversa conversa = chatService.criarConversaGrupo(titulo, usuario.getId(), participantesIds);
            return ResponseEntity.ok(conversa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cria uma nova conversa (individual ou grupo)
     */
    @PostMapping("/conversas/nova")
    public ResponseEntity<Conversa> criarNovaConversa(
            @RequestBody Map<String, Object> request,
            Authentication auth) {
        try {

            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                System.out.println("[ERROR] Usuário não encontrado: " + auth.getName());
                return ResponseEntity.badRequest().build();
            }

            String type = (String) request.get("type");

            if ("individual".equals(type)) {
                Long participantId = Long.valueOf(request.get("participantId").toString());

                Conversa conversa = chatService.criarConversaIndividual(usuario.getId(), participantId);

                return ResponseEntity.ok(conversa);
            } else if ("grupo".equals(type)) {
                String titulo = (String) request.get("titulo");
                @SuppressWarnings("unchecked")
                List<Long> participantesIds = (List<Long>) request.get("participantes");

                if (titulo == null || titulo.trim().isEmpty()) {
                    System.out.println("[ERROR] Título do grupo não informado");
                    return ResponseEntity.badRequest().build();
                }

                Conversa conversa = chatService.criarConversaGrupo(titulo, usuario.getId(), participantesIds);
                return ResponseEntity.ok(conversa);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca uma conversa específica
     */
    @GetMapping("/conversas/{conversaId}")
    public ResponseEntity<Conversa> buscarConversa(
            @PathVariable Long conversaId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);

            // Verificar se usuário é participante
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return chatService.buscarConversaPorId(conversaId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== MENSAGENS ====================

    /**
     * Lista mensagens de uma conversa
     */
    @GetMapping(value = "/conversas/{conversaId}/mensagens", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Mensagem>> listarMensagens(
            @PathVariable Long conversaId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);

            // Verificar se usuário é participante
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Mensagem> mensagens = chatService.buscarMensagensConversa(conversaId);

            // Atualizar última visualização
            chatService.atualizarUltimaVisualizacao(conversaId, usuario.getId());

            return ResponseEntity.ok(mensagens);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Envia uma nova mensagem
     */
    @PostMapping("/conversas/{conversaId}/mensagens")
    public ResponseEntity<Mensagem> enviarMensagem(
            @PathVariable Long conversaId,
            @RequestParam String conteudo,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            Mensagem mensagem = chatService.enviarMensagem(conversaId, usuario.getId(), conteudo);
            return ResponseEntity.ok(mensagem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Envia uma mensagem com arquivo anexado
     */
    @PostMapping("/conversas/{conversaId}/mensagens/upload")
    public ResponseEntity<Mensagem> enviarMensagemComArquivo(
            @PathVariable Long conversaId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "conteudo", required = false, defaultValue = "") String conteudo,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar se usuário é participante
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Determinar tipo da mensagem baseado no arquivo
            Mensagem.TipoMensagem tipo = determinarTipoArquivo(arquivo);
            
            // Se conteúdo está vazio, usar nome do arquivo como conteúdo
            if (conteudo == null || conteudo.trim().isEmpty()) {
                conteudo = "📎 " + arquivo.getOriginalFilename();
            }

            Mensagem mensagem = chatService.enviarMensagemComArquivo(
                conversaId, usuario.getId(), conteudo, arquivo, tipo);
            
            return ResponseEntity.ok(mensagem);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Determina o tipo da mensagem baseado na extensão do arquivo
     */
    private Mensagem.TipoMensagem determinarTipoArquivo(MultipartFile arquivo) {
        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null) {
            return Mensagem.TipoMensagem.ARQUIVO;
        }
        
        String extensao = nomeArquivo.toLowerCase();
        if (extensao.endsWith(".jpg") || extensao.endsWith(".jpeg") || 
            extensao.endsWith(".png") || extensao.endsWith(".gif") || 
            extensao.endsWith(".webp") || extensao.endsWith(".svg")) {
            return Mensagem.TipoMensagem.IMAGEM;
        }
        
        return Mensagem.TipoMensagem.ARQUIVO;
    }

    /**
     * Marca mensagem como lida
     */
    

    // metodo teste de ler mensagem
    @PutMapping("/conversas/{conversaId}/marcar-lidas")
    public ResponseEntity<Void> marcarMensagensDaConversaComoLidas(
            @PathVariable Long conversaId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            chatService.marcarMensagensDaConversaComoLidas(conversaId, usuario.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== USUÁRIOS ====================

    /**
     * Retorna dados do usuário atual logado
     */
    @GetMapping("/usuario-atual")
    public ResponseEntity<Usuario> usuarioAtual(Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista usuários disponíveis para chat
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios(Authentication auth) {
        try {
            Usuario usuarioLogado = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            List<Usuario> usuarios = usuarioService.findAll().stream()
                    .filter(u -> !u.getId().equals(usuarioLogado.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca usuários por nome
     */
    @GetMapping("/usuarios/buscar")
    public ResponseEntity<List<Usuario>> buscarUsuarios(
            @RequestParam String termo,
            Authentication auth) {
        try {
            List<Usuario> usuarios = usuarioService.buscarPorNomeOuEmail(termo);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista usuários online
     */
    @GetMapping("/usuarios/online")
    public ResponseEntity<List<Usuario>> listarUsuariosOnline(Authentication auth) {
        try {
            List<Usuario> usuariosOnline = usuarioService.buscarUsuariosOnline();
            return ResponseEntity.ok(usuariosOnline);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== PARTICIPANTES ====================

    /**
     * Adiciona participante a uma conversa em grupo
     */
    @PostMapping("/conversas/{conversaId}/participantes")
    public ResponseEntity<Void> adicionarParticipante(
            @PathVariable Long conversaId,
            @RequestBody Map<String, Long> request,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            Long novoParticipanteId = request.get("usuarioId");

            // Verificar se usuário pode adicionar participantes (deve ser admin/criador)
            if (!chatService.podeGerenciarParticipantes(conversaId, usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            chatService.adicionarParticipante(conversaId, novoParticipanteId,
                    com.jaasielsilva.portalceo.model.ParticipanteConversa.TipoParticipante.MEMBRO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove participante de uma conversa em grupo
     */
    @DeleteMapping("/conversas/{conversaId}/participantes/{usuarioId}")
    public ResponseEntity<Void> removerParticipante(
            @PathVariable Long conversaId,
            @PathVariable Long usuarioId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar se usuário pode remover participantes (deve ser admin/criador)
            if (!chatService.podeGerenciarParticipantes(conversaId, usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            chatService.removerParticipante(conversaId, usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista participantes de uma conversa
     */
    @GetMapping("/conversas/{conversaId}/participantes")
    public ResponseEntity<List<com.jaasielsilva.portalceo.model.ParticipanteConversa>> listarParticipantes(
            @PathVariable Long conversaId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);

            // Verificar se usuário é participante
            if (!chatService.isParticipante(conversaId, usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(chatService.buscarParticipantesAtivos(conversaId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== CONVERSAS POR TIPO ====================

    /**
     * Busca conversas individuais do usuário
     */
    @GetMapping("/conversas/individuais")
    public ResponseEntity<List<Conversa>> buscarConversasIndividuais(Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Conversa> conversas = chatService.buscarConversasIndividuaisDoUsuario(usuario.getId());
            return ResponseEntity.ok(conversas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca conversas em grupo do usuário
     */
    @GetMapping("/conversas/grupos")
    public ResponseEntity<List<Conversa>> buscarConversasGrupo(Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Conversa> conversas = chatService.buscarConversasGrupoDoUsuario(usuario.getId());
            return ResponseEntity.ok(conversas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== ESTATÍSTICAS ====================

    /**
     * Conta mensagens não lidas do usuário
     */
    @GetMapping("/mensagens/nao-lidas/count")
    public ResponseEntity<Long> contarMensagensNaoLidas(Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            long count = chatService.contarMensagensNaoLidas(usuario.getId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== UTILITÁRIOS ====================

    /**
     * Verifica status de saúde do chat
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Chat Service",
                "timestamp", System.currentTimeMillis()));
    }

    // ==================== DEPARTAMENTOS ====================

    /**
     * Lista departamentos com estatísticas de chat (participantes, mensagens e não lidas)
     */
    @GetMapping("/departamentos")
    public ResponseEntity<List<Map<String, Object>>> listarDepartamentos(Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Departamento> departamentos = departamentoService.listarTodos();

            List<Map<String, Object>> resultado = departamentos.stream().map(dept -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", dept.getId());
                dto.put("nome", dept.getNome());

                long participantes = 0L;
                long mensagens = 0L;
                long naoLidas = 0L;

                // Se já existe conversa para o departamento, calcular estatísticas
                var conversaOpt = conversaRepository.findByDepartamentoId(dept.getId());
                if (conversaOpt.isPresent()) {
                    Long conversaId = conversaOpt.get().getId();
                    Long participantesCount = participanteConversaRepository.countParticipantesAtivosByConversaId(conversaId);
                    participantes = participantesCount != null ? participantesCount : 0L;
                    mensagens = mensagemRepository.countMensagensPorConversa(conversaId);
                    naoLidas = mensagemRepository.countMensagensNaoLidasPorConversaEUsuario(conversaId, usuario.getId());
                }

                dto.put("participantes", participantes);
                dto.put("mensagens", mensagens);
                dto.put("mensagensNaoLidas", naoLidas);
                return dto;
            }).toList();

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cria ou retorna a conversa de um departamento específico
     */
    @PostMapping("/departamentos/{departamentoId}/conversa")
    public ResponseEntity<Conversa> criarOuBuscarConversaDepartamento(
            @PathVariable Long departamentoId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            Conversa conversa = chatService.criarConversaDepartamento(departamentoId, usuario.getId());
            return ResponseEntity.ok(conversa);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}