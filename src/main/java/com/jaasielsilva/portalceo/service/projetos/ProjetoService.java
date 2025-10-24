package com.jaasielsilva.portalceo.service.projetos;

import com.jaasielsilva.portalceo.model.projetos.Projeto;
import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import com.jaasielsilva.portalceo.repository.projetos.ProjetoRepository;
import com.jaasielsilva.portalceo.repository.projetos.TarefaProjetoRepository; // Importar TarefaProjetoRepository
import com.jaasielsilva.portalceo.repository.projetos.EquipeProjetoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjetoService {

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private TarefaProjetoRepository tarefaProjetoRepository; // Injetar TarefaProjetoRepository

    @Autowired
    private UsuarioRepository usuarioRepository; // Injetado

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EquipeProjetoRepository equipeRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    // Removido: @Autowired
    // Removido: private UsuarioRepository usuarioRepository; // Adicionado temporariamente para diagnóstico

    @Transactional(readOnly = true)
    public List<Projeto> buscarTodosAtivos() {
        return projetoRepository.findAllActiveWithEquipeAndMembros();
    }

    @Transactional
    public Projeto salvar(Projeto projeto) {
        // Garante que o projeto esteja ativo ao ser salvo
        projeto.setAtivo(true);
        // Remove a atribuição temporária de usuarioCriacao, pois não é mais necessária
        // if (projeto.getUsuarioCriacao() == null) {
        //     usuarioRepository.findById(1L).ifPresent(projeto::setUsuarioCriacao);
        // }

        Projeto salvo = projetoRepository.save(projeto);

        if (salvo.getTarefas() != null) {
            for (TarefaProjeto tarefa : salvo.getTarefas()) {
                tarefa.setProjeto(salvo); // Garante que a tarefa esteja associada ao projeto salvo
                tarefaProjetoRepository.save(tarefa); // Salva cada tarefa individualmente
            }
        }

        System.out.println("ID do Projeto salvo no Service: " + (salvo.getId() != null ? salvo.getId() : "NULO"));
        return salvo;
    }

    // Novo método para salvar projeto com equipe e gerente
    @Transactional
    public Projeto salvarComEquipe(Projeto projeto) {
        if (projeto.getEquipe() != null && projeto.getEquipe().getId() == null) {
            projeto.setEquipe(equipeRepository.save(projeto.getEquipe()));
        }
        if (projeto.getResponsavel() != null && projeto.getResponsavel().getId() == null) {
            projeto.setResponsavel(colaboradorRepository.save(projeto.getResponsavel()));
        }

        if (projeto.getStatus() == null) projeto.setStatus(Projeto.StatusProjeto.EM_ANDAMENTO);
        if (projeto.getProgresso() == null) projeto.setProgresso(0);
        projeto.setAtivo(true);

        return projetoRepository.save(projeto);
    }

    @Transactional(readOnly = true)
    public Optional<Projeto> buscarPorId(Long id) {
        return projetoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Projeto> buscarPorIdComEquipeMembros(Long id) {
        return projetoRepository.findByIdWithEquipeAndMembros(id);
    }

    @Transactional
    public Projeto atualizar(Projeto projeto) {
        return projetoRepository.save(projeto);
    }

    @Transactional
    public void arquivar(Long id) {
        projetoRepository.findById(id).ifPresent(p -> {
            p.setAtivo(false);
            projetoRepository.save(p);
        });
    }

    @Transactional(readOnly = true)
    public long countEmAndamento() {
        return projetoRepository.countEmAndamento();
    }

    @Transactional(readOnly = true)
    public long countConcluidos() {
        return projetoRepository.countConcluidos();
    }

    @Transactional(readOnly = true)
    public long countAtrasados() {
        return projetoRepository.countAtrasados();
    }
}
