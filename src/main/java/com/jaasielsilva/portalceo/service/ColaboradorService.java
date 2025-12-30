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
import java.util.ArrayList;
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

    @Autowired
    private RhRelatorioService rhRelatorioService;

    @org.springframework.cache.annotation.Cacheable(value = "colaboradoresAtivosList", unless = "#result == null || #result.isEmpty()")
    public List<Colaborador> listarAtivos() {
        return colaboradorRepository.findByAtivoTrue();
    }

    @org.springframework.cache.annotation.Cacheable(value = "colaboradoresSelecao", unless = "#result == null || #result.isEmpty()")
    public List<Colaborador> listarAtivosParaSelecao() {
        return colaboradorRepository.findBasicInfoForSelection();
    }

    public Page<Colaborador> listarAtivosPaginado(Pageable pageable) {
        return colaboradorRepository.findByAtivoTrue(pageable);
    }

    @org.springframework.cache.annotation.Cacheable(value = "colaboradoresAtivosCount")
    public long contarAtivos() {
        return colaboradorRepository.countByAtivoTrue();
    }

    public long contarCltAtivos() {
        return colaboradorRepository.countByAtivoTrueAndTipoContratoIgnoreCase("CLT");
    }

    public long contarPjAtivos() {
        return colaboradorRepository.countByAtivoTrueAndTipoContratoIgnoreCase("PJ");
    }

    public long contarEstagiariosAtivos() {
        return colaboradorRepository.countByAtivoTrueAndTipoContratoContainingIgnoreCase("ESTAG");
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
        return colaboradorRepository.findPotentialSupervisorsBasic();
    }

    /**
     * Busca colaboradores que podem ser supervisores, excluindo um colaborador
     * específico
     */
    public List<Colaborador> buscarSupervisoresPotenciais(Long excludeId) {
        if (excludeId == null) {
            return buscarSupervisoresPotenciais();
        }
        return colaboradorRepository.findPotentialSupervisorsBasicExcluding(excludeId);
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
    public List<com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO> listarParaAjax() {
        logger.debug("Buscando colaboradores para AJAX (com cache)");
        try {
            List<com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO> colaboradores = colaboradorRepository
                    .findColaboradoresForAjax();
            logger.debug("Encontrados {} colaboradores ativos para AJAX", colaboradores.size());
            return colaboradores;
        } catch (Exception e) {
            logger.error("Erro ao buscar colaboradores para AJAX: {}", e.getMessage(), e);
            throw e;
        }
    }

    public org.springframework.data.domain.Page<com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO> buscarParaAjax(
            String q,
            org.springframework.data.domain.Pageable pageable) {
        try {
            return colaboradorRepository.findColaboradoresForAjax(q, pageable);
        } catch (Exception e) {
            logger.error("Erro ao buscar colaboradores paginados para AJAX: {}", e.getMessage(), e);
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

    public void registrarPromocao(Colaborador colaborador, String cargoNovo, BigDecimal novoSalario,
            String descricaoPersonalizada) {
        HistoricoColaborador historico = new HistoricoColaborador();
        historico.setColaborador(colaborador);
        historico.setEvento("Promoção");

        String descricao = descricaoPersonalizada != null && !descricaoPersonalizada.trim().isEmpty()
                ? descricaoPersonalizada
                : "Promovido para " + cargoNovo;
        historico.setDescricao(descricao);

        historico.setCargoAnterior(colaborador.getCargo() != null ? colaborador.getCargo().getNome() : null);
        historico.setCargoNovo(cargoNovo);
        historico.setSalarioAnterior(colaborador.getSalario());
        historico.setSalarioNovo(novoSalario);
        historico.setDepartamentoAnterior(
                colaborador.getDepartamento() != null ? colaborador.getDepartamento().getNome() : null);
        historico.setDepartamentoNovo(
                colaborador.getDepartamento() != null ? colaborador.getDepartamento().getNome() : null);
        historico.setDataRegistro(LocalDateTime.now());

        historicoRepository.save(historico);
    }

    /**
     * Registra evento de desligamento no histórico do colaborador
     */
    public void registrarDesligamento(Colaborador colaborador, Usuario responsavel) {
        HistoricoColaborador historico = new HistoricoColaborador();
        historico.setColaborador(colaborador);
        historico.setEvento("Desligamento");
        String nomeResp = responsavel != null ? responsavel.getNome() : "Sistema";
        historico.setDescricao("Desligamento do colaborador confirmado por " + nomeResp);
        historico.setCargoAnterior(colaborador.getCargo() != null ? colaborador.getCargo().getNome() : null);
        historico.setCargoNovo(null);
        historico.setSalarioAnterior(colaborador.getSalario());
        historico.setSalarioNovo(null);
        historico.setDepartamentoAnterior(
                colaborador.getDepartamento() != null ? colaborador.getDepartamento().getNome() : null);
        historico.setDepartamentoNovo(null);
        historico.setDataRegistro(LocalDateTime.now());
        historicoRepository.save(historico);
        try { rhRelatorioService.invalidateTurnoverCache(); } catch (Exception ignore) {}
    }

    /**
     * Registra entradas de histórico para cada campo alterado do colaborador.
     */
    public void registrarAlteracoes(Colaborador original, Colaborador atualizado, Usuario responsavel, String ipOrigem, String endpoint) {
        LocalDateTime agora = LocalDateTime.now();
        List<HistoricoColaborador> entradas = new ArrayList<>();

        // Campos simples
        registrarEntradaSeAlterado("Nome", original.getNome(), atualizado.getNome(), original, responsavel, ipOrigem, endpoint, agora, entradas);
        registrarEntradaSeAlterado("Email", original.getEmail(), atualizado.getEmail(), original, responsavel, ipOrigem, endpoint, agora, entradas);
        registrarEntradaSeAlterado("Tipo de Contrato", original.getTipoContrato(), atualizado.getTipoContrato(), original, responsavel, ipOrigem, endpoint, agora, entradas);
        registrarEntradaSeAlterado("Carga Horária", original.getCargaHoraria(), atualizado.getCargaHoraria(), original, responsavel, ipOrigem, endpoint, agora, entradas);
        registrarEntradaSeAlterado("Ativo", original.getAtivo(), atualizado.getAtivo(), original, responsavel, ipOrigem, endpoint, agora, entradas);

        // Enum de status
        registrarEntradaSeAlterado("Status", original.getStatus() != null ? original.getStatus().name() : null,
                atualizado.getStatus() != null ? atualizado.getStatus().name() : null,
                original, responsavel, ipOrigem, endpoint, agora, entradas);

        // Cargo
        String cargoAnterior = original.getCargo() != null ? original.getCargo().getNome() : null;
        String cargoNovo = atualizado.getCargo() != null ? atualizado.getCargo().getNome() : null;
        registrarEntradaSeAlterado("Cargo", cargoAnterior, cargoNovo, original, responsavel, ipOrigem, endpoint, agora, entradas);

        // Departamento
        String deptAnterior = original.getDepartamento() != null ? original.getDepartamento().getNome() : null;
        String deptNovo = atualizado.getDepartamento() != null ? atualizado.getDepartamento().getNome() : null;
        registrarEntradaSeAlterado("Departamento", deptAnterior, deptNovo, original, responsavel, ipOrigem, endpoint, agora, entradas);

        // Supervisor
        String supervisorAnterior = original.getSupervisor() != null ? original.getSupervisor().getNome() : null;
        String supervisorNovo = atualizado.getSupervisor() != null ? atualizado.getSupervisor().getNome() : null;
        registrarEntradaSeAlterado("Supervisor", supervisorAnterior, supervisorNovo, original, responsavel, ipOrigem, endpoint, agora, entradas);

        // Salário (comparação por valor)
        String salarioAnteriorStr = original.getSalario() != null ? original.getSalario().toPlainString() : null;
        String salarioNovoStr = atualizado.getSalario() != null ? atualizado.getSalario().toPlainString() : null;
        boolean salarioAlterado = (original.getSalario() == null && atualizado.getSalario() != null)
                || (original.getSalario() != null && atualizado.getSalario() == null)
                || (original.getSalario() != null && atualizado.getSalario() != null
                    && original.getSalario().compareTo(atualizado.getSalario()) != 0);
        if (salarioAlterado) {
            HistoricoColaborador h = criarEntradaBase("Salário", salarioAnteriorStr, salarioNovoStr, original, responsavel, ipOrigem, endpoint, agora);
            entradas.add(h);
        }

        // Benefícios (adicionado/removido/alterado)
        List<ColaboradorBeneficio> origBenefs = original.getBeneficios() != null ? original.getBeneficios() : java.util.Collections.emptyList();
        List<ColaboradorBeneficio> novoBenefs = atualizado.getBeneficios() != null ? atualizado.getBeneficios() : java.util.Collections.emptyList();

        java.util.Map<Long, ColaboradorBeneficio> mapOrig = new java.util.HashMap<>();
        for (ColaboradorBeneficio cb : origBenefs) {
            if (cb.getBeneficio() != null && cb.getBeneficio().getId() != null) {
                mapOrig.put(cb.getBeneficio().getId(), cb);
            }
        }

        java.util.Map<Long, ColaboradorBeneficio> mapNovo = new java.util.HashMap<>();
        for (ColaboradorBeneficio cb : novoBenefs) {
            if (cb.getBeneficio() != null && cb.getBeneficio().getId() != null) {
                mapNovo.put(cb.getBeneficio().getId(), cb);
            }
        }

        for (java.util.Map.Entry<Long, ColaboradorBeneficio> e : mapNovo.entrySet()) {
            Long benId = e.getKey();
            ColaboradorBeneficio novo = e.getValue();
            ColaboradorBeneficio orig = mapOrig.get(benId);
            String nome = novo.getBeneficio() != null ? novo.getBeneficio().getNome() : String.valueOf(benId);
            if (orig == null) {
                HistoricoColaborador h = new HistoricoColaborador();
                h.setColaborador(original);
                h.setEvento("Alteração de Benefício");
                h.setDescricao("Benefício adicionado: " + nome);
                h.setCampoAlterado("Benefício");
                h.setValorAnterior(null);
                h.setValorNovo(resumoBeneficio(novo));
                h.setUsuarioResponsavel(responsavel);
                h.setIpOrigem(ipOrigem);
                h.setEndpoint(endpoint);
                h.setDataRegistro(agora);
                entradas.add(h);
            } else {
                boolean valorMudou = (orig.getValor() == null && novo.getValor() != null)
                        || (orig.getValor() != null && novo.getValor() == null)
                        || (orig.getValor() != null && novo.getValor() != null && orig.getValor().compareTo(novo.getValor()) != 0);
                boolean statusMudou = (orig.getStatus() == null && novo.getStatus() != null)
                        || (orig.getStatus() != null && novo.getStatus() == null)
                        || (orig.getStatus() != null && novo.getStatus() != null && !orig.getStatus().equals(novo.getStatus()));
                if (valorMudou || statusMudou) {
                    HistoricoColaborador h = new HistoricoColaborador();
                    h.setColaborador(original);
                    h.setEvento("Alteração de Benefício");
                    h.setDescricao("Benefício alterado: " + nome);
                    h.setCampoAlterado("Benefício");
                    h.setValorAnterior(resumoBeneficio(orig));
                    h.setValorNovo(resumoBeneficio(novo));
                    h.setUsuarioResponsavel(responsavel);
                    h.setIpOrigem(ipOrigem);
                    h.setEndpoint(endpoint);
                    h.setDataRegistro(agora);
                    entradas.add(h);
                }
            }
        }

        for (java.util.Map.Entry<Long, ColaboradorBeneficio> e : mapOrig.entrySet()) {
            Long benId = e.getKey();
            if (!mapNovo.containsKey(benId)) {
                ColaboradorBeneficio orig = e.getValue();
                String nome = orig.getBeneficio() != null ? orig.getBeneficio().getNome() : String.valueOf(benId);
                HistoricoColaborador h = new HistoricoColaborador();
                h.setColaborador(original);
                h.setEvento("Alteração de Benefício");
                h.setDescricao("Benefício removido: " + nome);
                h.setCampoAlterado("Benefício");
                h.setValorAnterior(resumoBeneficio(orig));
                h.setValorNovo(null);
                h.setUsuarioResponsavel(responsavel);
                h.setIpOrigem(ipOrigem);
                h.setEndpoint(endpoint);
                h.setDataRegistro(agora);
                entradas.add(h);
            }
        }

        // Persistir todas entradas
        for (HistoricoColaborador h : entradas) {
            historicoRepository.save(h);
        }
    }

    private void registrarEntradaSeAlterado(String campo, Object anterior, Object novo,
                                             Colaborador colaborador, Usuario responsavel,
                                             String ipOrigem, String endpoint, LocalDateTime data,
                                             List<HistoricoColaborador> entradas) {
        String antStr = anterior != null ? String.valueOf(anterior) : null;
        String novoStr = novo != null ? String.valueOf(novo) : null;
        boolean mudou = (antStr == null && novoStr != null) || (antStr != null && novoStr == null)
                || (antStr != null && !antStr.equals(novoStr));
        if (mudou) {
            HistoricoColaborador h = criarEntradaBase(campo, antStr, novoStr, colaborador, responsavel, ipOrigem, endpoint, data);
            entradas.add(h);
        }
    }

    private HistoricoColaborador criarEntradaBase(String campo, String anterior, String novo,
                                                  Colaborador colaborador, Usuario responsavel,
                                                  String ipOrigem, String endpoint, LocalDateTime data) {
        HistoricoColaborador h = new HistoricoColaborador();
        h.setColaborador(colaborador);
        h.setEvento("Alteração de Cadastro");
        h.setDescricao("Alteração de " + campo);
        h.setCampoAlterado(campo);
        h.setValorAnterior(anterior);
        h.setValorNovo(novo);
        h.setUsuarioResponsavel(responsavel);
        h.setIpOrigem(ipOrigem);
        h.setEndpoint(endpoint);
        h.setDataRegistro(data);
        return h;
    }

    private String resumoBeneficio(ColaboradorBeneficio cb) {
        String nome = cb.getBeneficio() != null ? cb.getBeneficio().getNome() : "-";
        String valor = cb.getValor() != null ? cb.getValor().toPlainString() : "-";
        String status = cb.getStatus() != null ? cb.getStatus().name() : "-";
        return "nome=" + nome + ", valor=" + valor + ", status=" + status;
    }

    public void registrarCriacao(Colaborador colaborador, Usuario responsavel, String ipOrigem, String endpoint) {
        LocalDateTime agora = LocalDateTime.now();
        HistoricoColaborador h = new HistoricoColaborador();
        h.setColaborador(colaborador);
        h.setEvento("Criação de Cadastro");
        h.setDescricao("Cadastro inicial do colaborador");
        h.setCampoAlterado("Cadastro");
        h.setValorAnterior(null);
        h.setValorNovo(null);
        h.setUsuarioResponsavel(responsavel);
        h.setIpOrigem(ipOrigem);
        h.setEndpoint(endpoint);
        h.setDataRegistro(agora);
        historicoRepository.save(h);

        if (colaborador.getBeneficios() != null) {
            for (ColaboradorBeneficio cb : colaborador.getBeneficios()) {
                if (cb.getBeneficio() != null) {
                    HistoricoColaborador hb = new HistoricoColaborador();
                    hb.setColaborador(colaborador);
                    hb.setEvento("Alteração de Benefício");
                    hb.setDescricao("Benefício adicionado: " + cb.getBeneficio().getNome());
                    hb.setCampoAlterado("Benefício");
                    hb.setValorAnterior(null);
                    hb.setValorNovo(resumoBeneficio(cb));
                    hb.setUsuarioResponsavel(responsavel);
                    hb.setIpOrigem(ipOrigem);
                    hb.setEndpoint(endpoint);
                    hb.setDataRegistro(agora);
                    historicoRepository.save(hb);
                }
            }
        }
    }

    public Optional<Colaborador> buscarPorUsuario(Usuario usuario) {
        return colaboradorRepository.findByUsuario(usuario);
    }

    public Optional<Colaborador> buscarPorMatriculaUsuario(String matricula) {
        return colaboradorRepository.findByUsuarioMatricula(matricula);
    }

    /**
     * Verifica se já existe um colaborador com o CPF informado
     */
    public boolean existeByCpf(String cpf) {
        return colaboradorRepository.existsByCpf(cpf);
    }

    /**
     * Verifica se já existe um colaborador com o email informado
     */
    public boolean existeByEmail(String email) {
        return colaboradorRepository.existsByEmail(email);
    }

    @Cacheable(value = "rhHeadcountTiposContrato")
    public java.util.List<String> listarTiposContratoAtivosDistinct() {
        return colaboradorRepository.findDistinctTipoContratoAtivo();
    }

    @Transactional
    public Colaborador corrigirCpf(Long colaboradorId, String novoCpf, Usuario responsavel, String ipOrigem, String endpoint) {
        Colaborador col = findById(colaboradorId);
        String atual = col.getCpf();
        String digits = novoCpf != null ? novoCpf.replaceAll("[^0-9]", "") : null;
        if (digits == null || digits.length() != 11) {
            throw new com.jaasielsilva.portalceo.exception.BusinessValidationException("CPF inválido");
        }
        if (!isValidCPF(digits)) {
            throw new com.jaasielsilva.portalceo.exception.BusinessValidationException("CPF inválido");
        }
        String formatted = formatCPF(digits);
        if (existeByCpf(formatted) && !formatted.equals(atual)) {
            throw new com.jaasielsilva.portalceo.exception.BusinessValidationException("CPF já cadastrado no sistema");
        }

        col.setCpf(formatted);
        Colaborador salvo = colaboradorRepository.save(col);

        HistoricoColaborador h = new HistoricoColaborador();
        h.setColaborador(salvo);
        h.setEvento("Correção de CPF");
        h.setDescricao("Correção de CPF iniciada por usuário autorizado");
        h.setCampoAlterado("CPF");
        h.setValorAnterior(atual);
        h.setValorNovo(formatted);
        h.setUsuarioResponsavel(responsavel);
        h.setIpOrigem(ipOrigem);
        h.setEndpoint(endpoint);
        h.setDataRegistro(LocalDateTime.now());
        historicoRepository.save(h);

        return salvo;
    }

    private boolean isValidCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;
            return Character.getNumericValue(cpf.charAt(9)) == firstDigit &&
                   Character.getNumericValue(cpf.charAt(10)) == secondDigit;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatCPF(String digits) {
        return digits.substring(0,3) + "." + digits.substring(3,6) + "." + digits.substring(6,9) + "-" + digits.substring(9);
    }
}
