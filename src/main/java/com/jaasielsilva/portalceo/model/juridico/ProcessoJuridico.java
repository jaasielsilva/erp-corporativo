package com.jaasielsilva.portalceo.model.juridico;

import com.jaasielsilva.portalceo.model.Cliente;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class ProcessoJuridico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String numero;
    
    @Enumerated(EnumType.STRING)
    private TipoAcaoJuridica tipo; // Alterado de String para Enum
    
    private String tribunal;
    private String parte; // Pode ser a parte contrária
    private String assunto;
    @Enumerated(EnumType.STRING)
    private StatusProcesso status;
    private LocalDate dataAbertura;
    private BigDecimal valorCausa;
    private LocalDateTime dataUltimaMovimentacao;
    
    @Column(columnDefinition = "TEXT")
    private String documentosPendentes;
    private LocalDate dataPendencia;
    private LocalDate dataAnalise;
    private LocalDate dataDocsRecebidos;
    private LocalDate dataContrato;

    public enum StatusProcesso { EM_ANDAMENTO, SUSPENSO, ENCERRADO, PENDENTE_DOCS, PENDENTE_CONTRATO, PENDENTE_PAGAMENTO, PENDENTE_SEGURADORA }
    
    public enum TipoAcaoJuridica {
        TRABALHISTA("Trabalhista"),
        CIVIL("Cível"),
        PREVIDENCIARIA("Previdenciária"),
        SEGURADORA("Seguradora"),
        OUTROS("Outros");

        private final String descricao;

        TipoAcaoJuridica(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public TipoAcaoJuridica getTipo() { return tipo; }
    public void setTipo(TipoAcaoJuridica tipo) { this.tipo = tipo; }
    public String getTribunal() { return tribunal; }
    public void setTribunal(String tribunal) { this.tribunal = tribunal; }
    public String getParte() { return parte; }
    public void setParte(String parte) { this.parte = parte; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public StatusProcesso getStatus() { return status; }
    public void setStatus(StatusProcesso status) { this.status = status; }
    public LocalDate getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDate dataAbertura) { this.dataAbertura = dataAbertura; }
    public BigDecimal getValorCausa() { return valorCausa; }
    public void setValorCausa(BigDecimal valorCausa) { this.valorCausa = valorCausa; }
    public LocalDateTime getDataUltimaMovimentacao() { return dataUltimaMovimentacao; }
    public void setDataUltimaMovimentacao(LocalDateTime dataUltimaMovimentacao) { this.dataUltimaMovimentacao = dataUltimaMovimentacao; }

    public String getDocumentosPendentes() { return documentosPendentes; }
    public void setDocumentosPendentes(String documentosPendentes) { this.documentosPendentes = documentosPendentes; }
    public LocalDate getDataPendencia() { return dataPendencia; }
    public void setDataPendencia(LocalDate dataPendencia) { this.dataPendencia = dataPendencia; }

    public LocalDate getDataAnalise() { return dataAnalise; }
    public void setDataAnalise(LocalDate dataAnalise) { this.dataAnalise = dataAnalise; }
    public LocalDate getDataDocsRecebidos() { return dataDocsRecebidos; }
    public void setDataDocsRecebidos(LocalDate dataDocsRecebidos) { this.dataDocsRecebidos = dataDocsRecebidos; }
    public LocalDate getDataContrato() { return dataContrato; }
    public void setDataContrato(LocalDate dataContrato) { this.dataContrato = dataContrato; }

    public String getTipoDescricao() {
        return tipo != null ? tipo.getDescricao() : "";
    }
}
