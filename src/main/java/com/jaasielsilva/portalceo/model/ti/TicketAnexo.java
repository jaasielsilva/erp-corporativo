package com.jaasielsilva.portalceo.model.ti;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_anexo")
public class TicketAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O ticket é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketSuporte ticket;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "O nome do arquivo é obrigatório")
    @Size(min = 1, max = 255, message = "O nome deve ter entre 1 e 255 caracteres")
    @Column(nullable = false, length = 255)
    private String nomeArquivo;

    @Column(length = 255)
    private String nomeOriginal;

    @NotNull(message = "O caminho do arquivo é obrigatório")
    @Column(nullable = false, length = 500)
    private String caminhoArquivo;

    @Column(length = 100)
    private String tipoMime;

    @Column
    private Long tamanhoBytes = 0L;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataUpload;

    @Column
    private Boolean ativo = true;

    @PrePersist
    public void onPrePersist() {
        if (dataUpload == null) {
            dataUpload = LocalDateTime.now();
        }
        if (ativo == null) {
            ativo = true;
        }
        if (nomeOriginal == null) {
            nomeOriginal = nomeArquivo;
        }
    }

    // Business methods
    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    public boolean isImagem() {
        return tipoMime != null && tipoMime.startsWith("image/");
    }

    public boolean isDocumento() {
        return tipoMime != null && (
                tipoMime.contains("pdf") ||
                tipoMime.contains("doc") ||
                tipoMime.contains("txt") ||
                tipoMime.contains("rtf")
        );
    }

    public boolean isVideo() {
        return tipoMime != null && tipoMime.startsWith("video/");
    }

    public String getExtensao() {
        if (nomeArquivo != null && nomeArquivo.contains(".")) {
            return nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public String getTamanhoFormatado() {
        if (tamanhoBytes == null || tamanhoBytes == 0) {
            return "0 B";
        }

        String[] unidades = {"B", "KB", "MB", "GB"};
        int unidade = 0;
        double tamanho = tamanhoBytes.doubleValue();

        while (tamanho >= 1024 && unidade < unidades.length - 1) {
            tamanho /= 1024;
            unidade++;
        }

        return String.format("%.1f %s", tamanho, unidades[unidade]);
    }

    public String getIconeCSS() {
        String extensao = getExtensao();
        
        return switch (extensao) {
            case "pdf" -> "fas fa-file-pdf text-danger";
            case "doc", "docx" -> "fas fa-file-word text-primary";
            case "xls", "xlsx" -> "fas fa-file-excel text-success";
            case "ppt", "pptx" -> "fas fa-file-powerpoint text-warning";
            case "txt" -> "fas fa-file-alt text-secondary";
            case "jpg", "jpeg", "png", "gif", "bmp" -> "fas fa-file-image text-info";
            case "mp4", "avi", "mov", "wmv" -> "fas fa-file-video text-purple";
            case "mp3", "wav", "ogg" -> "fas fa-file-audio text-orange";
            case "zip", "rar", "7z" -> "fas fa-file-archive text-dark";
            default -> "fas fa-file text-muted";
        };
    }

    public boolean podeVisualizarInline() {
        return isImagem() || (tipoMime != null && tipoMime.equals("application/pdf"));
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }
}