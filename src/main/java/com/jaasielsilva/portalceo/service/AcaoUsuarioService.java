package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.AcaoUsuarioDTO;
import com.jaasielsilva.portalceo.repository.AcaoUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AcaoUsuarioService {

    @Autowired
    private AcaoUsuarioRepository acaoUsuarioRepository;

    public Page<AcaoUsuarioDTO> buscarUltimasAcoes(int pagina, int tamanho) {
        return acaoUsuarioRepository.buscarUltimasAcoes(PageRequest.of(pagina, tamanho));
    }
}
