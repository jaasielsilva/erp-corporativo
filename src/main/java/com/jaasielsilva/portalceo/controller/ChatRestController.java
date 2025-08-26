package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.ConversaDTO;
import com.jaasielsilva.portalceo.model.Conversa;
import com.jaasielsilva.portalceo.model.Mensagem;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ChatService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
            System.out.println("[DEBUG] Criando nova conversa - Request: " + request);
            
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (usuario == null) {
                System.out.println("[ERROR] Usuário não encontrado: " + auth.getName());
                return ResponseEntity.badRequest().build();
            }
            
            String type = (String) request.get("type");
            System.out.println("[DEBUG] Tipo de conversa: " + type);
            
            if ("individual".equals(type)) {
                Long participantId = Long.valueOf(request.get("participantId").toString());
                System.out.println("[DEBUG] Criando conversa individual com participante: " + participantId);
                
                Conversa conversa = chatService.criarConversaIndividual(usuario.getId(), participantId);
                System.out.println("[DEBUG] Conversa criada com sucesso: " + conversa.getId());
                
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
                System.out.println("[ERROR] Tipo de conversa inválido: " + type);
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Erro ao criar conversa: " + e.getMessage());
            e.printStackTrace();
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
    @GetMapping("/conversas/{conversaId}/mensagens")
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
     * Marca mensagem como lida
     */
    @PutMapping("/mensagens/{mensagemId}/lida")
    public ResponseEntity<Void> marcarComoLida(
            @PathVariable Long mensagemId,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            chatService.marcarMensagemComoLida(mensagemId, usuario.getId());
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
            "timestamp", System.currentTimeMillis()
        ));
    }
}