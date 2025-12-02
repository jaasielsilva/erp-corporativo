package com.jaasielsilva.portalceo.model.cnpj;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cnpj_consultas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CnpjConsulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 14)
    private String cnpj;

    @Column(length = 255)
    private String razaoSocial;

    @Column(length = 255)
    private String nomeFantasia;

    @Column(length = 100)
    private String situacaoCadastral;

    @Column(length = 255)
    private String logradouro;
    @Column(length = 50)
    private String numero;
    @Column(length = 255)
    private String complemento;
    @Column(length = 255)
    private String bairro;
    @Column(length = 255)
    private String municipio;
    @Column(length = 10)
    private String uf;
    @Column(length = 20)
    private String cep;

    @Column(length = 20)
    private String cnaePrincipalCodigo;
    @Column(length = 255)
    private String cnaePrincipalDescricao;

    @ElementCollection
    @CollectionTable(name = "cnpj_consultas_cnaes_sec", joinColumns = @JoinColumn(name = "consulta_id"))
    private List<CnaeSecundarioEmbeddable> cnaesSecundarios;

    @Column(nullable = false)
    private LocalDateTime consultedAt;

    @Column(length = 500)
    private String source;

    @Column(length = 50)
    private String protocol;

    @PrePersist
    public void prePersist() {
        if (consultedAt == null) consultedAt = LocalDateTime.now();
    }
}

