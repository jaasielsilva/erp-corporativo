package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.AuditoriaRhLog;
import com.jaasielsilva.portalceo.repository.AuditoriaRhLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaRhLogService {

    @Autowired
    private AuditoriaRhLogRepository repository;

    public AuditoriaRhLog registrar(String categoria, String acao, String recurso, String usuario, String ip, String detalhes, Boolean sucesso) {
        AuditoriaRhLog log = new AuditoriaRhLog();
        log.setCategoria(categoria);
        log.setAcao(acao);
        log.setRecurso(recurso);
        log.setUsuario(usuario);
        log.setIpOrigem(ip);
        log.setDetalhes(detalhes);
        log.setSucesso(sucesso != null ? sucesso : Boolean.TRUE);
        return repository.save(log);
    }

    public Page<AuditoriaRhLog> listar(String categoria, String usuario, String recurso, LocalDateTime inicio, LocalDateTime fim, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return repository.pesquisar(categoria, usuario, recurso, inicio, fim, pageable);
    }
}

