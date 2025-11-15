package com.jaasielsilva.portalceo.model.juridico;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class NaoConformidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String titulo;
    private String descricao;
    private String severidade;
    private LocalDate dataDeteccao;
    private boolean resolvida;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getSeveridade() { return severidade; }
    public void setSeveridade(String severidade) { this.severidade = severidade; }
    public LocalDate getDataDeteccao() { return dataDeteccao; }
    public void setDataDeteccao(LocalDate dataDeteccao) { this.dataDeteccao = dataDeteccao; }
    public boolean isResolvida() { return resolvida; }
    public void setResolvida(boolean resolvida) { this.resolvida = resolvida; }
}