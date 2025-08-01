package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Colaborador.StatusColaborador;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ColaboradorService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Colaborador> listarAtivos() {
        return colaboradorRepository.findByAtivoTrue();
    }

    // Método findAll que retorna todos os colaboradores
    public List<Colaborador> findAll() {
        return colaboradorRepository.findAll();
    }

    public Colaborador findById(Long id) {
        return colaboradorRepository.findById(id).orElse(null);
    }

    public Colaborador salvar(Colaborador colaborador) {
        return colaboradorRepository.save(colaborador);
    }

    public List<Colaborador> listarTodos() {
        return colaboradorRepository.findAll();
    }

    public Page<Colaborador> listarTodosPaginado(Pageable pageable) {
        return colaboradorRepository.findAll(pageable);
    }

    public void excluir(Long id) {
        Colaborador col = findById(id);
        if (col != null) {
            col.setStatus(StatusColaborador.INATIVO);
            col.setAtivo(false);
            salvar(col);

            // Buscar usuário pelo colaborador
            Optional<Usuario> usuarioOpt = usuarioRepository.findByColaborador_Id(col.getId());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setStatus(Usuario.Status.DEMITIDO);
                usuario.setDataDesligamento(LocalDate.now());
                usuarioRepository.save(usuario);
            }
            // Se não encontrar usuário, apenas ignora - opcional: logar esse caso
        }
    }

}
