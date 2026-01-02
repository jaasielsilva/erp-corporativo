package com.jaasielsilva.portalceo.juridico.previdenciario.documentos.entity;

import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "juridico_previd_documento_processo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_previdenciario_id", nullable = false)
    private ProcessoPrevidenciario processoPrevidenciario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 40)
    private TipoDocumentoProcesso tipoDocumento;

    @Column(name = "caminho_arquivo", nullable = false, length = 500)
    private String caminhoArquivo;

    @Column(name = "nome_original", length = 255)
    private String nomeOriginal;

    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por_id", nullable = false)
    private Usuario enviadoPor;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    @PrePersist
    public void prePersist() {
        if (dataUpload == null) {
            dataUpload = LocalDateTime.now();
        }
    }
}

