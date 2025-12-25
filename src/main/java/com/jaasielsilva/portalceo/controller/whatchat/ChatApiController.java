package com.jaasielsilva.portalceo.controller.whatchat;

import com.jaasielsilva.portalceo.dto.whatchat.ChatConversaResumoDTO;
import com.jaasielsilva.portalceo.dto.whatchat.ChatMensagemDTO;
import com.jaasielsilva.portalceo.dto.whatchat.LinkClienteRequest;
import com.jaasielsilva.portalceo.dto.whatchat.LinkProcessoPrevidenciarioRequest;
import com.jaasielsilva.portalceo.dto.whatchat.SendTextRequest;
import com.jaasielsilva.portalceo.dto.whatchat.UpdateStatusRequest;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.service.ProcessoPrevidenciarioService;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.whatchat.ChatConversa;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagem;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemTipo;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import com.jaasielsilva.portalceo.repository.whatchat.ChatConversaRepository;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.whatchat.WhaTchatConversaService;
import com.jaasielsilva.portalceo.service.whatchat.WhaTchatMensagemService;
import com.jaasielsilva.portalceo.service.whatchat.WhaTchatProcessoIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@RestController("whaTchatChatApiController")
@RequestMapping("/api/whatchat")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL','ROLE_JURIDICO')")
public class ChatApiController {

    private final WhaTchatConversaService conversaService;
    private final WhaTchatMensagemService mensagemService;
    private final WhaTchatProcessoIntegrationService processoIntegrationService;
    private final ChatConversaRepository conversaRepository;
    private final ClienteRepository clienteRepository;
    private final ProcessoPrevidenciarioService processoPrevidenciarioService;
    private final UsuarioService usuarioService;

    @GetMapping("/conversas")
    public ResponseEntity<List<ChatConversaResumoDTO>> listarConversas() {
        List<ChatConversa> conversas = conversaService.listar();
        return ResponseEntity.ok(conversas.stream().map(this::toResumo).toList());
    }

    @GetMapping("/conversas/{conversaId}/mensagens")
    public ResponseEntity<List<ChatMensagemDTO>> listarMensagens(@PathVariable Long conversaId) {
        List<ChatMensagem> mensagens = mensagemService.listarMensagens(conversaId);
        return ResponseEntity.ok(mensagens.stream().map(this::toMensagem).toList());
    }

    @PostMapping("/conversas/{conversaId}/mensagens/texto")
    public ResponseEntity<ChatMensagemDTO> enviarTexto(@PathVariable Long conversaId, @RequestBody SendTextRequest req,
            Authentication auth) {
        Usuario u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        ChatMensagem m = mensagemService.enviarTexto(conversaId, req.getTexto(), u);
        return ResponseEntity.ok(toMensagem(m));
    }

    @PostMapping("/conversas/{conversaId}/mensagens/arquivo")
    public ResponseEntity<ChatMensagemDTO> enviarArquivo(
            @PathVariable Long conversaId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipo") ChatMensagemTipo tipo,
            Authentication auth) {
        Usuario u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        ChatMensagem m = mensagemService.enviarArquivo(conversaId, arquivo, tipo, u);
        return ResponseEntity.ok(toMensagem(m));
    }

    @GetMapping("/mensagens/{mensagemId}/arquivo")
    public ResponseEntity<FileSystemResource> baixarArquivo(@PathVariable Long mensagemId) {
        ChatMensagem m = mensagemService.buscarPorId(mensagemId);
        if (m.getMediaCaminho() == null || m.getMediaCaminho().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(Path.of(m.getMediaCaminho()));
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        String mime = m.getMediaMimeType() != null ? m.getMediaMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String filename = m.getMediaFileName() != null ? m.getMediaFileName() : "arquivo";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/conversas/{conversaId}/status")
    public ResponseEntity<ChatConversaResumoDTO> atualizarStatus(@PathVariable Long conversaId,
            @RequestBody UpdateStatusRequest req) {
        ChatConversa c = conversaService.atualizarStatus(conversaId, req.getStatus());
        return ResponseEntity.ok(toResumo(c));
    }

    @PostMapping("/conversas/{conversaId}/vincular/cliente")
    public ResponseEntity<ChatConversaResumoDTO> vincularCliente(@PathVariable Long conversaId,
            @RequestBody LinkClienteRequest req) {
        ChatConversa conversa = conversaService.buscarPorId(conversaId);
        conversa.setCliente(clienteRepository.findById(req.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado")));
        ChatConversa salvo = conversaRepository.save(conversa);
        return ResponseEntity.ok(toResumo(salvo));
    }

    @PostMapping("/conversas/{conversaId}/vincular/processo-previdenciario")
    public ResponseEntity<ChatConversaResumoDTO> vincularProcesso(@PathVariable Long conversaId,
            @RequestBody LinkProcessoPrevidenciarioRequest req) {
        ChatConversa conversa = conversaService.buscarPorId(conversaId);
        conversa.setProcessoPrevidenciario(
                processoPrevidenciarioService.buscarPorId(req.getProcessoPrevidenciarioId()));
        ChatConversa salvo = conversaRepository.save(conversa);
        return ResponseEntity.ok(toResumo(salvo));
    }

    @PostMapping("/conversas/{conversaId}/criar-processo-previdenciario")
    public ResponseEntity<?> criarProcessoPrevidenciario(@PathVariable Long conversaId, Authentication auth) {
        Usuario u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        var processo = processoIntegrationService.criarProcessoPrevidenciario(conversaId, u);
        return ResponseEntity.ok(java.util.Map.of("processoPrevidenciarioId", processo.getId()));
    }

    private ChatConversaResumoDTO toResumo(ChatConversa c) {
        ChatConversaResumoDTO dto = new ChatConversaResumoDTO();
        dto.setId(c.getId());
        dto.setWaId(c.getWaId());
        dto.setNomeContato(c.getNomeContato());
        dto.setStatus(c.getStatus());
        dto.setUltimaMensagemEm(c.getUltimaMensagemEm());
        dto.setClienteId(c.getCliente() != null ? c.getCliente().getId() : null);
        dto.setClienteNome(c.getCliente() != null ? c.getCliente().getNome() : null);
        dto.setProcessoPrevidenciarioId(
                c.getProcessoPrevidenciario() != null ? c.getProcessoPrevidenciario().getId() : null);
        dto.setAtendenteNome(c.getAtendente() != null ? c.getAtendente().getNome() : null);
        return dto;
    }

    private ChatMensagemDTO toMensagem(ChatMensagem m) {
        ChatMensagemDTO dto = new ChatMensagemDTO();
        dto.setId(m.getId());
        dto.setConversaId(m.getConversa() != null ? m.getConversa().getId() : null);
        dto.setDirecao(m.getDirecao());
        dto.setTipo(m.getTipo());
        dto.setTexto(m.getTexto());
        dto.setDataMensagem(m.getDataMensagem());
        dto.setMediaFileName(m.getMediaFileName());
        dto.setMediaMimeType(m.getMediaMimeType());
        dto.setMediaDownloadUrl(
                m.getMediaCaminho() != null ? "/api/whatchat/mensagens/" + m.getId() + "/arquivo" : null);
        dto.setEnviadaPorNome(m.getEnviadaPor() != null ? m.getEnviadaPor().getNome() : null);
        return dto;
    }
}
