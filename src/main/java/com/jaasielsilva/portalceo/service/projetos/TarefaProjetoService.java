package com.jaasielsilva.portalceo.service.projetos;

import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import com.jaasielsilva.portalceo.repository.projetos.TarefaProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TarefaProjetoService {

    @Autowired
    private TarefaProjetoRepository tarefaRepository;

    @Transactional(readOnly = true)
    public List<TarefaProjeto> listarTodas() {
        return tarefaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TarefaProjeto> listarPorProjeto(Long projetoId) {
        return tarefaRepository.findByProjetoId(projetoId);
    }

    @Transactional(readOnly = true)
    public List<TarefaProjeto> buscarPorFiltro(String q, TarefaProjeto.StatusTarefa status) {
        String termo = (q != null && !q.isBlank()) ? q : null;
        if (termo != null && status != null) {
            return tarefaRepository.findByStatusAndNomeContainingIgnoreCase(status, termo);
        }
        if (termo != null) {
            return tarefaRepository.findByNomeContainingIgnoreCase(termo);
        }
        if (status != null) {
            return tarefaRepository.findByStatus(status);
        }
        return tarefaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long countPendentes() {
        return tarefaRepository.countByStatus(TarefaProjeto.StatusTarefa.PENDENTE);
    }

    @Transactional(readOnly = true)
    public long countEmAndamento() {
        return tarefaRepository.countByStatus(TarefaProjeto.StatusTarefa.EM_ANDAMENTO);
    }

    @Transactional(readOnly = true)
    public long countConcluidas() {
        return tarefaRepository.countByStatus(TarefaProjeto.StatusTarefa.CONCLUIDA);
    }

    @Transactional
    public TarefaProjeto salvar(TarefaProjeto tarefa) {
        if (tarefa.getStatus() == null) tarefa.setStatus(TarefaProjeto.StatusTarefa.PENDENTE);
        if (tarefa.getPrioridade() == null) tarefa.setPrioridade(TarefaProjeto.Prioridade.MEDIA);
        tarefa.setAtivo(true);
        return tarefaRepository.save(tarefa);
    }

    @Transactional(readOnly = true)
    public Optional<TarefaProjeto> buscarPorId(Long id) {
        return tarefaRepository.findById(id);
    }

    @Transactional
    public TarefaProjeto atualizar(TarefaProjeto tarefa) {
        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public void atribuir(Long tarefaId, Long colaboradorId, Long projetoId) {
        tarefaRepository.findById(tarefaId).ifPresent(t -> {
            if (!t.getProjeto().getId().equals(projetoId)) {
                throw new IllegalArgumentException("A tarefa não pertence ao projeto especificado.");
            }
            com.jaasielsilva.portalceo.model.Colaborador col = new com.jaasielsilva.portalceo.model.Colaborador();
            col.setId(colaboradorId);
            t.setAtribuidaA(col);
            tarefaRepository.save(t);
        });
    }

    @Transactional(readOnly = true)
    public List<TarefaProjeto> listarPorColaborador(Long colaboradorId) {
        return tarefaRepository.findByAtribuidaAId(colaboradorId);
    }
}