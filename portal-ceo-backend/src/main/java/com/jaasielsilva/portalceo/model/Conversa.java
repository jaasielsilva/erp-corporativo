package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidade que representa uma conversa no sistema de chat interno
 * Pode ser individual, em grupo ou por departamento
 */
@Entity
@Table(name = "conversas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo")
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoConversa tipo;

    @Column(name = "usuario1_id")
    private Long usuario1Id;

    @Column(name = "usuario2_id")
    private Long usuario2Id;

    @Column(name = "criado_por", nullable = false)
    private Long criadoPor;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(name = "ultima_atividade")
    private LocalDateTime ultimaAtividade;

    // Enum para tipos de conversa
    public enum TipoConversa {
        INDIVIDUAL("Individual"),
        GRUPO("Grupo"),
        DEPARTAMENTO("Departamento");
        
        private final String descricao;
        
        TipoConversa(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }

    // Relacionamentos
    @OneToMany(mappedBy = "conversa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private List<Mensagem> mensagens = new ArrayList<>();

    @OneToMany(mappedBy = "conversa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private Set<ParticipanteConversa> participantes = new HashSet<>();

    // Métodos de conveniência para usuários
    public Usuario getOutroUsuario(Long usuarioAtualId) {
        if (isIndividual()) {
            if (usuario1Id != null && usuario1Id.equals(usuarioAtualId)) {
                return participantes.stream()
                    .filter(p -> p.getUsuario().getId().equals(usuario2Id))
                    .map(ParticipanteConversa::getUsuario)
                    .findFirst().orElse(null);
            } else if (usuario2Id != null && usuario2Id.equals(usuarioAtualId)) {
                return participantes.stream()
                    .filter(p -> p.getUsuario().getId().equals(usuario1Id))
                    .map(ParticipanteConversa::getUsuario)
                    .findFirst().orElse(null);
            }
        }
        return null; // Para conversas em grupo, não há "outro usuário"
    }

    // Métodos de conveniência
    public void adicionarMensagem(Mensagem mensagem) {
        mensagens.add(mensagem);
        mensagem.setConversa(this);
        this.ultimaAtividade = LocalDateTime.now();
    }

    public void adicionarParticipante(ParticipanteConversa participante) {
        participantes.add(participante);
        participante.setConversa(this);
    }

    public void removerParticipante(ParticipanteConversa participante) {
        participantes.remove(participante);
        participante.setConversa(null);
    }

    public boolean isIndividual() {
        return TipoConversa.INDIVIDUAL.equals(this.tipo);
    }

    public boolean isGrupo() {
        return TipoConversa.GRUPO.equals(this.tipo) || TipoConversa.DEPARTAMENTO.equals(this.tipo);
    }

    public boolean isDepartamento() {
        return TipoConversa.DEPARTAMENTO.equals(this.tipo);
    }

    public int getNumeroParticipantes() {
        return participantes != null ? (int) participantes.stream().filter(p -> p.getAtivo()).count() : 0;
    }

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (ultimaAtividade == null) {
            ultimaAtividade = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaAtividade = LocalDateTime.now();
    }

    // Verifica se o usuário participa da conversa
    public boolean participaUsuario(Usuario usuario) {
        return participantes.stream()
                .anyMatch(p -> p.getUsuario().getId().equals(usuario.getId()) && p.getAtivo());
    }

    // Retorna o outro participante em conversas individuais
    public Usuario getOutroParticipante(Usuario usuarioAtual) {
        if (!isIndividual()) {
            return null; // Não aplicável para conversas em grupo
        }
        return participantes.stream()
                .map(ParticipanteConversa::getUsuario)
                .filter(u -> !u.getId().equals(usuarioAtual.getId()))
                .findFirst()
                .orElse(null);
    }

    // Métodos estáticos para criação de conversas
    public static Conversa criarConversaIndividual(Long usuario1Id, Long usuario2Id, Long criadoPor) {
        Conversa conversa = new Conversa();
        LocalDateTime agora = LocalDateTime.now();
        conversa.setTipo(TipoConversa.INDIVIDUAL);
        conversa.setUsuario1Id(usuario1Id);
        conversa.setUsuario2Id(usuario2Id);
        conversa.setCriadoPor(criadoPor);
        conversa.setDataCriacao(agora);
        conversa.setCriadaEm(agora);
        conversa.setUltimaAtividade(agora);
        conversa.setAtiva(true);
        return conversa;
    }

    public static Conversa criarConversaGrupo(String titulo, Long criadoPor) {
        Conversa conversa = new Conversa();
        LocalDateTime agora = LocalDateTime.now();
        conversa.setTipo(TipoConversa.GRUPO);
        conversa.setTitulo(titulo);
        conversa.setCriadoPor(criadoPor);
        conversa.setDataCriacao(agora);
        conversa.setCriadaEm(agora);
        conversa.setUltimaAtividade(agora);
        conversa.setAtiva(true);
        return conversa;
    }

    public static Conversa criarConversaDepartamento(String titulo, Long criadoPor) {
        Conversa conversa = new Conversa();
        LocalDateTime agora = LocalDateTime.now();
        conversa.setTipo(TipoConversa.DEPARTAMENTO);
        conversa.setTitulo(titulo);
        conversa.setCriadoPor(criadoPor);
        conversa.setDataCriacao(agora);
        conversa.setCriadaEm(agora);
        conversa.setUltimaAtividade(agora);
        conversa.setAtiva(true);
        return conversa;
    }

    // Método para obter o nome/título da conversa
    public String getNomeConversa() {
        if (isIndividual()) {
            return "Conversa Individual";
        } else {
            return titulo != null ? titulo : "Conversa em Grupo";
        }
    }

    // Método para atualizar a última atividade
    public void atualizarUltimaAtividade() {
        this.ultimaAtividade = LocalDateTime.now();
    }

    // Método para contar mensagens não lidas de um usuário
    public long contarMensagensNaoLidas(Usuario usuario) {
        if (mensagens == null)
            return 0;

        return mensagens.stream()
                .filter(m -> !m.isLida())
                .filter(m -> !m.getRemetente().getId().equals(usuario.getId()))
                .count();
    }

    // Método para obter a última mensagem
    public Mensagem getUltimaMensagem() {
        if (mensagens == null || mensagens.isEmpty()) {
            return null;
        }
        return mensagens.get(0); // Como está ordenado por dataEnvio DESC
    }
}