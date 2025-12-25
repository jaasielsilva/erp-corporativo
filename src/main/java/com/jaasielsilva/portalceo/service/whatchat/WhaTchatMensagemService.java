package com.jaasielsilva.portalceo.service.whatchat;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.whatchat.ChatConversa;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagem;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemDirecao;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemTipo;
import com.jaasielsilva.portalceo.repository.whatchat.ChatMensagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhaTchatMensagemService {

    private final ChatMensagemRepository mensagemRepository;
    private final WhaTchatConversaService conversaService;
    private final WhatsAppProviderClient whatsAppProviderClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.upload.whatchat.dir:uploads/whatchat}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<ChatMensagem> listarMensagens(Long conversaId) {
        conversaService.buscarPorId(conversaId);
        return mensagemRepository.findByConversa_IdOrderByDataMensagemAscIdAsc(conversaId);
    }

    @Transactional(readOnly = true)
    public ChatMensagem buscarPorId(Long mensagemId) {
        return mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new IllegalArgumentException("Mensagem não encontrada"));
    }

    @Transactional
    public ChatMensagem registrarRecebidaTexto(ChatConversa conversa, String whatsappMessageId, String texto,
            Long epochSeconds, String payloadBruto) {
        if (whatsappMessageId != null && mensagemRepository.findByWhatsappMessageId(whatsappMessageId).isPresent()) {
            return mensagemRepository.findByWhatsappMessageId(whatsappMessageId).orElseThrow();
        }

        ChatMensagem m = new ChatMensagem();
        m.setConversa(conversa);
        m.setDirecao(ChatMensagemDirecao.RECEBIDA);
        m.setTipo(ChatMensagemTipo.TEXTO);
        m.setTexto(texto);
        m.setWhatsappMessageId(whatsappMessageId);
        m.setDataMensagem(epochSeconds != null
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault())
                : LocalDateTime.now());
        m.setPayloadBruto(payloadBruto);

        ChatMensagem salvo = mensagemRepository.save(m);
        conversaService.marcarUltimaMensagem(conversa, salvo.getDataMensagem());
        publicarAtualizacao(conversa.getId());
        return salvo;
    }

    @Transactional
    public ChatMensagem registrarRecebidaMidia(ChatConversa conversa,
            String whatsappMessageId,
            ChatMensagemTipo tipo,
            String mediaId,
            String mimeType,
            String fileName,
            Long epochSeconds,
            String payloadBruto) {
        if (whatsappMessageId != null && mensagemRepository.findByWhatsappMessageId(whatsappMessageId).isPresent()) {
            return mensagemRepository.findByWhatsappMessageId(whatsappMessageId).orElseThrow();
        }

        WhatsAppMediaDownloadResult midia = whatsAppProviderClient.baixarMidia(mediaId);

        Path dir = Paths.get(uploadDir, "conversas", String.valueOf(conversa.getId()));
        try {
            Files.createDirectories(dir);
            String baseName = fileName != null && !fileName.isBlank() ? fileName : "arquivo";
            String sanitized = baseName.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
            String unique = UUID.randomUUID() + "_" + sanitized;
            Path destino = dir.resolve(unique);
            Files.write(destino, midia.bytes());

            ChatMensagem m = new ChatMensagem();
            m.setConversa(conversa);
            m.setDirecao(ChatMensagemDirecao.RECEBIDA);
            m.setTipo(tipo);
            m.setWhatsappMessageId(whatsappMessageId);
            m.setMediaId(mediaId);
            m.setMediaMimeType(midia.mimeType() != null ? midia.mimeType() : mimeType);
            m.setMediaFileName(sanitized);
            m.setMediaSha256(midia.sha256());
            m.setMediaCaminho(destino.toString());
            m.setDataMensagem(epochSeconds != null
                    ? LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault())
                    : LocalDateTime.now());
            m.setPayloadBruto(payloadBruto);

            ChatMensagem salvo = mensagemRepository.save(m);
            conversaService.marcarUltimaMensagem(conversa, salvo.getDataMensagem());
            publicarAtualizacao(conversa.getId());
            return salvo;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao salvar mídia recebida", e);
        }
    }

    @Transactional
    public ChatMensagem enviarTexto(Long conversaId, String texto, Usuario usuario) {
        ChatConversa conversa = conversaService.buscarPorId(conversaId);
        String waMsgId = whatsAppProviderClient.enviarTexto(conversa.getWaId(), texto);

        ChatMensagem m = new ChatMensagem();
        m.setConversa(conversa);
        m.setDirecao(ChatMensagemDirecao.ENVIADA);
        m.setTipo(ChatMensagemTipo.TEXTO);
        m.setTexto(texto);
        m.setWhatsappMessageId(waMsgId);
        m.setEnviadaPor(usuario);
        m.setDataMensagem(LocalDateTime.now());

        ChatMensagem salvo = mensagemRepository.save(m);
        conversa.setAtendente(usuario);
        conversaService.marcarUltimaMensagem(conversa, salvo.getDataMensagem());
        publicarAtualizacao(conversaId);
        return salvo;
    }

    @Transactional
    public ChatMensagem enviarArquivo(Long conversaId, MultipartFile arquivo, ChatMensagemTipo tipo, Usuario usuario) {
        ChatConversa conversa = conversaService.buscarPorId(conversaId);
        String waMsgId;
        if (tipo == ChatMensagemTipo.IMAGEM) {
            waMsgId = whatsAppProviderClient.enviarImagem(conversa.getWaId(), arquivo);
        } else if (tipo == ChatMensagemTipo.DOCUMENTO) {
            waMsgId = whatsAppProviderClient.enviarDocumento(conversa.getWaId(), arquivo);
        } else {
            throw new IllegalArgumentException("Tipo inválido para arquivo");
        }

        Path dir = Paths.get(uploadDir, "conversas", String.valueOf(conversa.getId()));
        try {
            Files.createDirectories(dir);
            String original = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "arquivo";
            String sanitized = original.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
            String unique = UUID.randomUUID() + "_" + sanitized;
            Path destino = dir.resolve(unique);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            ChatMensagem m = new ChatMensagem();
            m.setConversa(conversa);
            m.setDirecao(ChatMensagemDirecao.ENVIADA);
            m.setTipo(tipo);
            m.setWhatsappMessageId(waMsgId);
            m.setEnviadaPor(usuario);
            m.setDataMensagem(LocalDateTime.now());
            m.setMediaMimeType(arquivo.getContentType());
            m.setMediaFileName(sanitized);
            m.setMediaCaminho(destino.toString());

            ChatMensagem salvo = mensagemRepository.save(m);
            conversa.setAtendente(usuario);
            conversaService.marcarUltimaMensagem(conversa, salvo.getDataMensagem());
            publicarAtualizacao(conversaId);
            return salvo;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao salvar arquivo enviado", e);
        }
    }

    private void publicarAtualizacao(Long conversaId) {
        messagingTemplate.convertAndSend("/topic/whatchat/conversas", "updated");
        messagingTemplate.convertAndSend("/topic/whatchat/conversas/" + conversaId, "updated");
    }
}
