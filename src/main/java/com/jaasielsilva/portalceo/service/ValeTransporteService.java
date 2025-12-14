package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.ResumoValeTransporteDTO;
import com.jaasielsilva.portalceo.dto.ValeTransporteListDTO;
import com.jaasielsilva.portalceo.dto.ConfiguracaoValeTransporteDTO;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ValeTransporte;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.ValeTransporteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;

@Service
public class ValeTransporteService {

    private static final Logger logger = LoggerFactory.getLogger(ValeTransporteService.class);

    private final ValeTransporteRepository repository;
    
    @Autowired
    private ColaboradorRepository colaboradorRepository;

    public ValeTransporteService(ValeTransporteRepository repository) {
        this.repository = repository;
    }

    public List<ValeTransporte> listarTodos() {
        return repository.findAll();
    }

    public ValeTransporte salvar(ValeTransporte vt) {
        logger.info("Salvando vale transporte para colaborador: {}", 
                    vt.getColaborador() != null ? vt.getColaborador().getNome() : "<sem colaborador>");
        return repository.save(vt);
    }

    public boolean existeValeAtivoNoPeriodo(Colaborador colaborador, Integer mes, Integer ano) {
        if (colaborador == null || mes == null || ano == null) return false;
        try {
            return repository.existsByColaboradorAndMesReferenciaAndAnoReferenciaAndStatus(colaborador, mes, ano, ValeTransporte.StatusValeTransporte.ATIVO);
        } catch (Exception e) {
            logger.warn("Falha ao verificar existência de VT ativo para {} {}/{}: {}", colaborador.getNome(), mes, ano, e.getMessage());
            return false;
        }
    }

    @Transactional
    public int suspenderDuplicadosAtivos(Integer mes, Integer ano, String motivo) {
        List<ValeTransporte> ativos = repository.findByMesReferenciaAndAnoReferenciaAndStatus(mes, ano, ValeTransporte.StatusValeTransporte.ATIVO);
        java.util.Map<Long, List<ValeTransporte>> porColaborador = ativos.stream().collect(java.util.stream.Collectors.groupingBy(v -> v.getColaborador().getId()));
        int suspensos = 0;
        for (var entry : porColaborador.entrySet()) {
            List<ValeTransporte> lista = entry.getValue();
            if (lista.size() <= 1) continue;
            lista.sort(java.util.Comparator.<ValeTransporte>comparingLong(v -> v.getId()).reversed());
            ValeTransporte manter = lista.get(0);
            for (int i = 1; i < lista.size(); i++) {
                ValeTransporte v = lista.get(i);
                v.setStatus(ValeTransporte.StatusValeTransporte.SUSPENSO);
                v.setObservacoes((v.getObservacoes() != null ? v.getObservacoes() + "; " : "") + "Suspenso por limpeza de duplicados" + (motivo != null && !motivo.isBlank() ? (" - " + motivo) : ""));
                repository.save(v);
                suspensos++;
            }
            logger.info("Mantido VT ativo ID {} para colaborador {} e suspensos {} duplicados", manter.getId(), manter.getColaborador().getNome(), lista.size()-1);
        }
        return suspensos;
    }

    public ValeTransporte buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vale Transporte não encontrado: " + id));
    }

    public void deletar(Long id) {
        logger.info("Deletando vale transporte ID: {}", id);
        repository.deleteById(id);
    }

    /**
     * Lista vales transporte com informações dos colaboradores
     */
    public List<ValeTransporteListDTO> listarComColaboradores() {
        logger.debug("Buscando lista completa de vales transporte");
        
        List<ValeTransporte> vales = repository.findAll();
        return vales.stream().map(this::convertToListDTO).collect(Collectors.toList());
    }

    /**
     * Filtra vales transporte por mês/ano
     */
    public List<ValeTransporteListDTO> listarPorMesAno(Integer mes, Integer ano) {
        logger.debug("Buscando vales transporte para {}/{}", mes, ano);
        
        List<ValeTransporte> vales = repository.findByMesReferenciaAndAnoReferenciaOrderByColaborador_Nome(mes, ano);
        return vales.stream().map(this::convertToListDTO).collect(Collectors.toList());
    }

    /**
     * Filtra vales transporte por status
     */
    public List<ValeTransporteListDTO> listarPorStatus(ValeTransporte.StatusValeTransporte status) {
        logger.debug("Buscando vales transporte com status: {}", status);
        
        List<ValeTransporte> vales = repository.findByStatusOrderByColaborador_Nome(status);
        return vales.stream().map(this::convertToListDTO).collect(Collectors.toList());
    }

    public Page<ValeTransporteListDTO> listarPaginado(Integer mes, Integer ano, String status,
                                                      Long colaboradorId, Long departamentoId, String q, int page, int size, org.springframework.data.domain.Sort sort) {
        ValeTransporte.StatusValeTransporte statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = ValeTransporte.StatusValeTransporte.valueOf(status.toUpperCase()); } catch (Exception ignore) {}
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100), sort == null ? org.springframework.data.domain.Sort.by("id").descending() : sort);
        return repository.listarPaginado(mes, ano, statusEnum, colaboradorId, departamentoId, q, pageable);
    }

    /**
     * Gera resumo estatístico mensal
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResumoValeTransporteDTO gerarResumoMensal(Integer mes, Integer ano) {
        logger.debug("Gerando resumo mensal para {}/{}", mes, ano);
        
        ResumoValeTransporteDTO resumo = new ResumoValeTransporteDTO();
        resumo.setMesReferencia(mes);
        resumo.setAnoReferencia(ano);
        
        // Estatísticas de colaboradores
        long totalColaboradores = colaboradorRepository.count();
        long colaboradoresAtivos = colaboradorRepository.countByAtivoTrue();
        
        resumo.setTotalColaboradores((int) totalColaboradores);
        resumo.setTotalColaboradoresAtivos((int) colaboradoresAtivos);
        
        // Estatísticas financeiras
        Double custoTotal = repository.sumValorTotalByMesAno(mes, ano);
        Double totalDesconto = repository.sumValorDescontoByMesAno(mes, ano);
        
        resumo.setCustoTotalMes(custoTotal != null ? BigDecimal.valueOf(custoTotal) : BigDecimal.ZERO);
        resumo.setTotalDescontoColaboradores(totalDesconto != null ? BigDecimal.valueOf(totalDesconto) : BigDecimal.ZERO);
        resumo.setTotalSubsidioEmpresa(resumo.getCustoTotalMes().subtract(resumo.getTotalDescontoColaboradores()));
        
        // Estatísticas de vales por status (sem carregar todos os registros)
        Long cntAtivos = repository.countByMesAnoAndStatus(mes, ano, ValeTransporte.StatusValeTransporte.ATIVO);
        Long cntSuspensos = repository.countByMesAnoAndStatus(mes, ano, ValeTransporte.StatusValeTransporte.SUSPENSO);
        Long cntCancelados = repository.countByMesAnoAndStatus(mes, ano, ValeTransporte.StatusValeTransporte.CANCELADO);
        resumo.setTotalValesAtivos(cntAtivos != null ? cntAtivos.intValue() : 0);
        resumo.setTotalValesSuspensos(cntSuspensos != null ? cntSuspensos.intValue() : 0);
        resumo.setTotalValesCancelados(cntCancelados != null ? cntCancelados.intValue() : 0);
        
        return resumo;
    }

    /**
     * Gera resumo para o mês atual
     */
    public ResumoValeTransporteDTO gerarResumoAtual() {
        LocalDate hoje = LocalDate.now();
        return gerarResumoMensal(hoje.getMonthValue(), hoje.getYear());
    }

    /**
     * Processa cálculo mensal em lote para todos os colaboradores ativos
     */
    @Transactional
    public int calcularValesMensais(Integer mes, Integer ano, ConfiguracaoValeTransporteDTO config, com.jaasielsilva.portalceo.model.Usuario usuarioCriacao) {
        logger.info("Iniciando cálculo mensal para {}/{} com {} dias úteis", 
                    mes, ano, config.getDiasUteisPadrao());
        
        List<Colaborador> colaboradoresAtivos = colaboradorRepository.findByAtivoTrue();
        int processados = 0;
        
        for (Colaborador colaborador : colaboradoresAtivos) {
            // Verifica se já existe vale para este mês/ano
            if (!repository.existsByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano)) {
                ValeTransporte vale = new ValeTransporte();
                vale.setColaborador(colaborador);
                vale.setMesReferencia(mes);
                vale.setAnoReferencia(ano);
                vale.setDiasUteis(config.getDiasUteisPadrao());
                vale.setValorPassagem(config.getValorPassagem());
                vale.setViagensDia(2); // Padrão ida e volta
                vale.setStatus(ValeTransporte.StatusValeTransporte.ATIVO);
                vale.setDataAdesao(LocalDate.now());
                if (usuarioCriacao != null) { vale.setUsuarioCriacao(usuarioCriacao); }
                
                repository.save(vale);
                processados++;
            }
        }
        
        logger.info("Processamento concluído: {} vales transporte criados", processados);
        return processados;
    }

    /**
     * Suspende um vale transporte
     */
    @Transactional
    public void suspender(Long id, String motivo) {
        ValeTransporte vale = buscarPorId(id);
        vale.setStatus(ValeTransporte.StatusValeTransporte.SUSPENSO);
        if (motivo != null && !motivo.trim().isEmpty()) {
            vale.setObservacoes((vale.getObservacoes() != null ? vale.getObservacoes() + "; " : "") + 
                              "Suspenso: " + motivo);
        }
        repository.save(vale);
        logger.info("Vale transporte suspenso: ID {} - Motivo: {}", id, motivo);
    }

    /**
     * Reativa um vale transporte suspenso
     */
    @Transactional
    public void reativar(Long id) {
        ValeTransporte vale = buscarPorId(id);
        vale.setStatus(ValeTransporte.StatusValeTransporte.ATIVO);
        repository.save(vale);
        logger.info("Vale transporte reativado: ID {}", id);
    }

    /**
     * Cancela definitivamente um vale transporte
     */
    @Transactional
    public void cancelar(Long id, String motivo) {
        ValeTransporte vale = buscarPorId(id);
        vale.setStatus(ValeTransporte.StatusValeTransporte.CANCELADO);
        vale.setDataCancelamento(LocalDate.now());
        if (motivo != null && !motivo.trim().isEmpty()) {
            vale.setObservacoes((vale.getObservacoes() != null ? vale.getObservacoes() + "; " : "") + 
                              "Cancelado: " + motivo);
        }
        repository.save(vale);
        logger.info("Vale transporte cancelado: ID {} - Motivo: {}", id, motivo);
    }

    /**
     * Converte ValeTransporte para DTO de listagem
     */
    private ValeTransporteListDTO convertToListDTO(ValeTransporte vale) {
        ValeTransporteListDTO dto = new ValeTransporteListDTO();
        dto.setId(vale.getId());
        dto.setNomeColaborador(vale.getColaborador().getNome());
        dto.setMatriculaColaborador(vale.getColaborador().getId().toString());
        dto.setDepartamento(vale.getColaborador().getDepartamento() != null ? 
                           vale.getColaborador().getDepartamento().getNome() : "N/A");
        dto.setLinhaOnibus(vale.getLinhaOnibus());
        dto.setViagensDia(vale.getViagensDia());
        dto.setDiasUteis(vale.getDiasUteis());
        dto.setValorTotalMes(vale.getValorTotalMes());
        dto.setValorDesconto(vale.getValorDesconto());
        dto.setValorSubsidioEmpresa(vale.getValorSubsidioEmpresa());
        dto.setStatus(vale.getStatus());
        dto.setDataAdesao(vale.getDataAdesao());
        dto.setEnderecoOrigem(vale.getEnderecoOrigem());
        dto.setEnderecoDestino(vale.getEnderecoDestino());
        
        return dto;
    }
}
