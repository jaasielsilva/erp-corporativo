package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.exception.CargoNotFoundException;
import com.jaasielsilva.portalceo.exception.ColaboradorNotFoundException;
import com.jaasielsilva.portalceo.exception.DepartamentoNotFoundException;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Colaborador.StatusColaborador;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ColaboradorService {

    private static final Logger logger = LoggerFactory.getLogger(ColaboradorService.class);

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ColaboradorValidationService validationService;
    
    public List<Colaborador> listarAtivos() {
        return colaboradorRepository.findByAtivoTrue();
    }

    // Método findAll que retorna todos os colaboradores
    public List<Colaborador> findAll() {
        return colaboradorRepository.findAll();
    }

    public Colaborador findById(Long id) {
        logger.debug("Buscando colaborador com ID: {}", id);
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new ColaboradorNotFoundException(id));
    }
    
    /**
     * Busca colaboradores que podem ser supervisores (ativos e com status ATIVO)
     */
    public List<Colaborador> buscarSupervisoresPotenciais() {
        return colaboradorRepository.findPotentialSupervisors();
    }
    
    /**
     * Busca colaboradores que podem ser supervisores, excluindo um colaborador específico
     */
    public List<Colaborador> buscarSupervisoresPotenciais(Long excludeId) {
        if (excludeId == null) {
            return buscarSupervisoresPotenciais();
        }
        return colaboradorRepository.findPotentialSupervisorsExcluding(excludeId);
    }

    @Transactional
    public Colaborador salvar(Colaborador colaborador) {
        logger.info("Iniciando salvamento do colaborador: {} (ID: {})", 
                   colaborador.getNome(), colaborador.getId());
        
        try {
            // Validar regras de negócio
            validationService.validarColaborador(colaborador);
            
            // Buscar e associar Cargo
            if (colaborador.getCargo() != null && colaborador.getCargo().getId() != null) {
                colaborador.setCargo(
                        cargoRepository.findById(colaborador.getCargo().getId())
                                .orElseThrow(() -> new CargoNotFoundException(colaborador.getCargo().getId())));
                logger.debug("Cargo associado: {}", colaborador.getCargo().getNome());
            } else {
                colaborador.setCargo(null);
            }

            // Buscar e associar Departamento
            if (colaborador.getDepartamento() != null && colaborador.getDepartamento().getId() != null) {
                colaborador.setDepartamento(
                        departamentoRepository.findById(colaborador.getDepartamento().getId())
                                .orElseThrow(() -> new DepartamentoNotFoundException(colaborador.getDepartamento().getId())));
                logger.debug("Departamento associado: {}", colaborador.getDepartamento().getNome());
            } else {
                colaborador.setDepartamento(null);
            }

            // Buscar e associar Supervisor
            if (colaborador.getSupervisor() != null && colaborador.getSupervisor().getId() != null) {
                colaborador.setSupervisor(
                        colaboradorRepository.findById(colaborador.getSupervisor().getId())
                                .orElseThrow(() -> new ColaboradorNotFoundException(colaborador.getSupervisor().getId())));
                logger.debug("Supervisor associado: {}", colaborador.getSupervisor().getNome());
            } else {
                colaborador.setSupervisor(null);
            }

            // Salvar colaborador
            Colaborador salvo = colaboradorRepository.save(colaborador);
            logger.info("Colaborador salvo com sucesso: {} (ID: {})", salvo.getNome(), salvo.getId());
            
            return salvo;
            
        } catch (Exception e) {
            logger.error("Erro ao salvar colaborador {}: {}", colaborador.getNome(), e.getMessage(), e);
            throw e;
        }
    }

    public List<Colaborador> listarTodos() {
        logger.debug("Buscando todos os colaboradores");
        try {
            List<Colaborador> colaboradores = colaboradorRepository.findBasicInfoForSelection();
            logger.debug("Encontrados {} colaboradores ativos", colaboradores.size());
            return colaboradores;
        } catch (Exception e) {
            logger.error("Erro ao buscar colaboradores: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<Colaborador> listarTodosPaginado(Pageable pageable) {
        return colaboradorRepository.findAll(pageable);
    }

    @Transactional
    public void excluir(Long id) {
        logger.info("Iniciando exclusão lógica do colaborador ID: {}", id);
        
        try {
            Colaborador colaborador = findById(id);
            
            colaborador.setStatus(StatusColaborador.INATIVO);
            colaborador.setAtivo(false);
            
            colaboradorRepository.save(colaborador);
            logger.info("Colaborador {} desativado com sucesso", colaborador.getNome());

            // Buscar e desativar usuário associado
            Optional<Usuario> usuarioOpt = usuarioRepository.findByColaborador_Id(colaborador.getId());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setStatus(Usuario.Status.DEMITIDO);
                usuario.setDataDesligamento(LocalDate.now());
                usuarioRepository.save(usuario);
                logger.info("Usuário associado {} também foi desativado", usuario.getEmail());
            } else {
                logger.debug("Nenhum usuário encontrado associado ao colaborador ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao excluir colaborador ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

}
