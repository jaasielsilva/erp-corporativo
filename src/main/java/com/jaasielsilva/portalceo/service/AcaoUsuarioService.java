package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.AcaoUsuarioDTO;
import com.jaasielsilva.portalceo.model.AcaoUsuario;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.AcaoUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AcaoUsuarioService {

    @Autowired
    private AcaoUsuarioRepository acaoUsuarioRepository;

    public Page<AcaoUsuarioDTO> buscarUltimasAcoes(int pagina, int tamanho) {
        return acaoUsuarioRepository.buscarUltimasAcoes(PageRequest.of(pagina, tamanho));
    }

    /**
     * Registra uma ação de usuário para auditoria.
     * @param acao Texto ou código da ação (ex.: IMPERSONACAO_INICIO)
     * @param usuario Usuário alvo da ação
     * @param responsavel Usuário responsável que realizou a ação
     * @param ip IP de origem
     */
    public void registrarAcao(String acao, Usuario usuario, Usuario responsavel, String ip) {
        AcaoUsuario registro = new AcaoUsuario();
        registro.setData(LocalDateTime.now());
        registro.setAcao(acao);
        registro.setUsuario(usuario);
        registro.setResponsavel(responsavel);
        registro.setIp(ip);
        acaoUsuarioRepository.save(registro);
    }
}
