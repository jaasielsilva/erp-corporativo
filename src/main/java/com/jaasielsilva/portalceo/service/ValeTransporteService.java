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
                    vt.getColaborador() != null ? vt.getColaborador().getNome() : "N/A");
        return repository.save(vt);
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

    /**
     * Gera resumo estatístico mensal
     */
    public ResumoValeTransporteDTO gerarResumoMensal(Integer mes, Integer ano) {
        logger.debug("Gerando resumo mensal para {}/{}", mes, ano);
        
        ResumoValeTransporteDTO resumo = new ResumoValeTransporteDTO();
        resumo.setMesReferencia(mes);
        resumo.setAnoReferencia(ano);
        
        // Estatísticas de colaboradores
        long totalColaboradores = colaboradorRepository.count();
        long colaboradoresAtivos = colaboradorRepository.findByAtivoTrue().size();
        
        resumo.setTotalColaboradores((int) totalColaboradores);
        resumo.setTotalColaboradoresAtivos((int) colaboradoresAtivos);
        
        // Estatísticas financeiras
        Double custoTotal = repository.sumValorTotalByMesAno(mes, ano);
        Double totalDesconto = repository.sumValorDescontoByMesAno(mes, ano);
        
        resumo.setCustoTotalMes(custoTotal != null ? BigDecimal.valueOf(custoTotal) : BigDecimal.ZERO);
        resumo.setTotalDescontoColaboradores(totalDesconto != null ? BigDecimal.valueOf(totalDesconto) : BigDecimal.ZERO);
        resumo.setTotalSubsidioEmpresa(resumo.getCustoTotalMes().subtract(resumo.getTotalDescontoColaboradores()));
        
        // Estatísticas de vales por status
        List<ValeTransporte> valesDoMes = repository.findByMesReferenciaAndAnoReferenciaOrderByColaborador_Nome(mes, ano);
        
        resumo.setTotalValesAtivos((int) valesDoMes.stream()
                .filter(v -> v.getStatus() == ValeTransporte.StatusValeTransporte.ATIVO).count());
        resumo.setTotalValesSuspensos((int) valesDoMes.stream()
                .filter(v -> v.getStatus() == ValeTransporte.StatusValeTransporte.SUSPENSO).count());
        resumo.setTotalValesCancelados((int) valesDoMes.stream()
                .filter(v -> v.getStatus() == ValeTransporte.StatusValeTransporte.CANCELADO).count());
        
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
    public int calcularValesMensais(Integer mes, Integer ano, ConfiguracaoValeTransporteDTO config) {
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
