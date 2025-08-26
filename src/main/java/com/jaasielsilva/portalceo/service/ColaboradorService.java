package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.exception.CargoNotFoundException;
import com.jaasielsilva.portalceo.exception.ColaboradorNotFoundException;
import com.jaasielsilva.portalceo.exception.DepartamentoNotFoundException;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Colaborador.StatusColaborador;
import com.jaasielsilva.portalceo.model.ColaboradorBeneficio;
import com.jaasielsilva.portalceo.model.HistoricoColaborador;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;
import com.jaasielsilva.portalceo.repository.HistoricoColaboradorRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private HistoricoColaboradorRepository historicoRepository;

    public List<Colaborador> listarAtivos() {
        return colaboradorRepository.findByAtivoTrue();
    }

    public long contarAtivos() {
        return colaboradorRepository.findByAtivoTrue().size();
    }

    public long contarContratacaosPorPeriodo(int meses) {
        LocalDate dataInicio = LocalDate.now().minusMonths(meses);
        return colaboradorRepository.countContratacoesPorPeriodo(dataInicio);
    }

    /** Buscar colaborador por ID */
    public Colaborador buscarPorId(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado: " + id));
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
     * Busca colaboradores que podem ser supervisores, excluindo um colaborador
     * específico
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
                                .orElseThrow(() -> new DepartamentoNotFoundException(
                                        colaborador.getDepartamento().getId())));
                logger.debug("Departamento associado: {}", colaborador.getDepartamento().getNome());
            } else {
                colaborador.setDepartamento(null);
            }

            // Buscar e associar Supervisor
            if (colaborador.getSupervisor() != null && colaborador.getSupervisor().getId() != null) {
                colaborador.setSupervisor(
                        colaboradorRepository.findById(colaborador.getSupervisor().getId())
                                .orElseThrow(
                                        () -> new ColaboradorNotFoundException(colaborador.getSupervisor().getId())));
                logger.debug("Supervisor associado: {}", colaborador.getSupervisor().getNome());
            } else {
                colaborador.setSupervisor(null);
            }

            // Validar e associar benefícios
            if (colaborador.getBeneficios() != null) {
                for (ColaboradorBeneficio cb : colaborador.getBeneficios()) {
                    // Associar o colaborador ao benefício
                    cb.setColaborador(colaborador);

                    // Garantir que o Benefício já existe no banco
                    if (cb.getBeneficio() == null || cb.getBeneficio().getId() == null) {
                        throw new RuntimeException(
                                "Benefício " + (cb.getBeneficio() != null ? cb.getBeneficio().getNome() : "null")
                                        + " ainda não está salvo no banco.");
                    }
                }
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

    /**
     * Lista colaboradores otimizado para AJAX (apenas dados essenciais) com cache
     */
    @Cacheable(value = "colaboradoresAjax", unless = "#result.size() == 0")
    public List<com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO> listarParaAjax() {
        logger.debug("Buscando colaboradores para AJAX (com cache)");
        try {
            List<com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO> colaboradores = colaboradorRepository.findColaboradoresForAjax();
            logger.debug("Encontrados {} colaboradores ativos para AJAX", colaboradores.size());
            return colaboradores;
        } catch (Exception e) {
            logger.error("Erro ao buscar colaboradores para AJAX: {}", e.getMessage(), e);
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

    // metodo para calcular o tempo na empresa
    public String calcularTempoNaEmpresa(LocalDate dataAdmissao) {
        if (dataAdmissao == null)
            return "";
        Period periodo = Period.between(dataAdmissao, LocalDate.now());
        return periodo.getYears() + " ano(s) e " + periodo.getMonths() + " mês(es)";
    }

    @Transactional(readOnly = true)
    public Colaborador findByIdComBeneficios(Long id) {
        Colaborador colaborador = colaboradorRepository.findById(id)
                .orElseThrow(() -> new ColaboradorNotFoundException(id));

        // força carregamento da lista de benefícios (LAZY)
        if (colaborador.getBeneficios() != null) {
            colaborador.getBeneficios().size();
        }

        return colaborador;
    }

    public void registrarPromocao(Colaborador colaborador, String cargoNovo, BigDecimal novoSalario) {
        registrarPromocao(colaborador, cargoNovo, novoSalario, null);
    }

    public void registrarPromocao(Colaborador colaborador, String cargoNovo, BigDecimal novoSalario, String descricaoPersonalizada) {
        HistoricoColaborador historico = new HistoricoColaborador();
        historico.setColaborador(colaborador);
        historico.setEvento("Promoção");
        
        String descricao = descricaoPersonalizada != null && !descricaoPersonalizada.trim().isEmpty() 
            ? descricaoPersonalizada 
            : "Promovido para " + cargoNovo;
        historico.setDescricao(descricao);
        
        historico.setCargoAnterior(colaborador.getCargo().getNome());
        historico.setCargoNovo(cargoNovo);
        historico.setSalarioAnterior(colaborador.getSalario());
        historico.setSalarioNovo(novoSalario);
        historico.setDepartamentoAnterior(colaborador.getDepartamento().getNome());
        historico.setDepartamentoNovo(colaborador.getDepartamento().getNome());
        historico.setDataRegistro(LocalDateTime.now());


        historicoRepository.save(historico);
    }


}
