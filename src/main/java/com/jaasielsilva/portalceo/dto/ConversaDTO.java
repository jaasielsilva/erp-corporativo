package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Conversa;
import com.jaasielsilva.portalceo.model.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversaDTO {
    
    private Long id;
    private Long outroUsuarioId;
    private String outroUsuarioNome;
    private String outroUsuarioFoto;
    private boolean outroUsuarioOnline;
    private String ultimaMensagem;
    private LocalDateTime ultimaAtividade;
    private String ultimaAtividadeFormatada;
    private long mensagensNaoLidas;
    private boolean ativa;
    
    public static ConversaDTO fromEntity(Conversa conversa, Usuario usuarioLogado) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Para conversas individuais, pegar o outro participante
    Usuario outroUsuario = null;
    if (conversa.isIndividual()) {
        // pegar o outro participante ativo que não seja o usuário logado
        outroUsuario = conversa.getParticipantes().stream()
                .map(p -> p.getUsuario())
                .filter(u -> !u.getId().equals(usuarioLogado.getId()))
                .findFirst()
                .orElse(null);
    }

    // Última mensagem
    String ultimaMensagemTexto = "";
    if (conversa.getUltimaMensagem() != null) {
        String conteudo = conversa.getUltimaMensagem().getConteudo();
        ultimaMensagemTexto = conteudo.length() > 50 ? conteudo.substring(0, 50) + "..." : conteudo;
    }

    // Contar mensagens não lidas
    long mensagensNaoLidas = conversa.contarMensagensNaoLidas(usuarioLogado);

    // Montar DTO
    return new ConversaDTO(
            conversa.getId(),
            outroUsuario != null ? outroUsuario.getId() : null,
            outroUsuario != null ? outroUsuario.getNome() : null,
            outroUsuario != null && outroUsuario.getFotoPerfil() != null ? "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(outroUsuario.getFotoPerfil()) : null,
            outroUsuario != null && outroUsuario.isOnline(), // se tiver campo online
            ultimaMensagemTexto,
            conversa.getUltimaAtividade(),
            conversa.getUltimaAtividade() != null ? conversa.getUltimaAtividade().format(formatter) : null,
            mensagensNaoLidas,
            conversa.getAtiva()
    );
    }
}