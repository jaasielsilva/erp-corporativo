package com.jaasielsilva.portalceo.juridico.previdenciario.historico.service;

import com.jaasielsilva.portalceo.juridico.previdenciario.historico.entity.HistoricoProcesso;
import com.jaasielsilva.portalceo.juridico.previdenciario.historico.repository.HistoricoProcessoRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoProcessoService {

    private final HistoricoProcessoRepository historicoRepository;

    @Transactional
    public HistoricoProcesso registrar(ProcessoPrevidenciario processo,
            String evento,
            Usuario usuario,
            String observacao) {
        HistoricoProcesso h = new HistoricoProcesso();
        h.setProcessoPrevidenciario(processo);
        h.setEvento(evento);
        h.setUsuario(usuario);
        h.setObservacao(observacao);
        return historicoRepository.save(h);
    }

    @Transactional(readOnly = true)
    public List<HistoricoProcesso> listarPorProcesso(Long processoId) {
        return historicoRepository.findByProcessoPrevidenciario_IdOrderByDataEventoDesc(processoId);
    }
}
