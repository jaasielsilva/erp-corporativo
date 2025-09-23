package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ValeRefeicao;
import com.jaasielsilva.portalceo.repository.ValeRefeicaoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class ValeRefeicaoService {

    private static final Logger logger = LoggerFactory.getLogger(ValeRefeicaoService.class);

    @Autowired
    private ValeRefeicaoRepository valeRefeicaoRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    /**
     * Lista todos os vales refeição
     */
    public List<ValeRefeicao> listarTodos() {
        return valeRefeicaoRepository.findAll();
    }

    /**
     * Lista vales refeição ativos
     */
    public List<ValeRefeicao> listarAtivos() {
        return valeRefeicaoRepository.findByStatus(ValeRefeicao.StatusValeRefeicao.ATIVO);
    }

    /**
     * Busca vale refeição por ID
     */
    public Optional<ValeRefeicao> buscarPorId(Long id) {
        return valeRefeicaoRepository.findById(id);
    }

    /**
     * Lista vales refeição de um colaborador
     */
    public List<ValeRefeicao> listarPorColaborador(Long colaboradorId) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        
        return valeRefeicaoRepository.findByColaboradorOrderByAnoReferenciaDescMesReferenciaDesc(colaborador);
    }

    /**
     * Busca vale refeição ativo de um colaborador
     */
    public Optional<ValeRefeicao> buscarAtivoDoColaborador(Long colaboradorId) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        
        return valeRefeicaoRepository.findByColaboradorAndStatus(colaborador, ValeRefeicao.StatusValeRefeicao.ATIVO);
    }

    /**
     * Busca vale refeição de um colaborador por mês/ano
     */
    public Optional<ValeRefeicao> buscarPorColaboradorMesAno(Long colaboradorId, Integer mes, Integer ano) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        
        return valeRefeicaoRepository.findByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano);
    }

    /**
     * Lista vales por mês e ano
     */
    public List<ValeRefeicao> listarPorMesAno(Integer mes, Integer ano) {
        return valeRefeicaoRepository.findByMesReferenciaAndAnoReferencia(mes, ano);
    }

    /**
     * Salva ou atualiza um vale refeição
     */
    @Transactional
    public ValeRefeicao salvar(ValeRefeicao valeRefeicao) {
        logger.info("Salvando vale refeição para colaborador: {} - {}/{}", 
                   valeRefeicao.getColaborador().getNome(), 
                   valeRefeicao.getMesReferencia(), 
                   valeRefeicao.getAnoReferencia());

        return valeRefeicaoRepository.save(valeRefeicao);
    }

    /**
     * Ativa vale refeição para um colaborador
     */
    @Transactional
    public ValeRefeicao ativarVale(Long colaboradorId, BigDecimal valorDiario, 
                                  ValeRefeicao.TipoVale tipo, String operadora) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        // Verificar se já tem vale ativo
        Optional<ValeRefeicao> valeAtivo = buscarAtivoDoColaborador(colaboradorId);
        if (valeAtivo.isPresent()) {
            throw new IllegalStateException("Colaborador já possui vale refeição ativo");
        }

        ValeRefeicao novoVale = new ValeRefeicao();
        novoVale.setColaborador(colaborador);
        novoVale.setValorDiario(valorDiario);
        novoVale.setTipo(tipo);
        novoVale.setOperadora(operadora);
        novoVale.setStatus(ValeRefeicao.StatusValeRefeicao.ATIVO);
        novoVale.setDataAdesao(LocalDate.now());

        // Gerar vale para o mês atual
        LocalDate hoje = LocalDate.now();
        gerarValeParaMes(novoVale, hoje.getMonthValue(), hoje.getYear());

        return salvar(novoVale);
    }

    /**
     * Cancela vale refeição de um colaborador
     */
    @Transactional
    public ValeRefeicao cancelarVale(Long valeId, String motivo) {
        ValeRefeicao vale = valeRefeicaoRepository.findById(valeId)
                .orElseThrow(() -> new IllegalArgumentException("Vale refeição não encontrado"));

        vale.setStatus(ValeRefeicao.StatusValeRefeicao.CANCELADO);
        vale.setDataCancelamento(LocalDate.now());
        vale.setObservacoes(motivo);

        logger.info("Vale refeição cancelado para colaborador: {} - Motivo: {}", 
                   vale.getColaborador().getNome(), motivo);

        return salvar(vale);
    }

    /**
     * Suspende vale refeição temporariamente
     */
    @Transactional
    public ValeRefeicao suspenderVale(Long valeId, String motivo) {
        ValeRefeicao vale = valeRefeicaoRepository.findById(valeId)
                .orElseThrow(() -> new IllegalArgumentException("Vale refeição não encontrado"));

        vale.setStatus(ValeRefeicao.StatusValeRefeicao.SUSPENSO);
        vale.setObservacoes(motivo);

        logger.info("Vale refeição suspenso para colaborador: {} - Motivo: {}", 
                   vale.getColaborador().getNome(), motivo);

        return salvar(vale);
    }

    /**
     * Reativa vale refeição suspenso
     */
    @Transactional
    public ValeRefeicao reativarVale(Long valeId) {
        ValeRefeicao vale = valeRefeicaoRepository.findById(valeId)
                .orElseThrow(() -> new IllegalArgumentException("Vale refeição não encontrado"));

        if (vale.getStatus() != ValeRefeicao.StatusValeRefeicao.SUSPENSO) {
            throw new IllegalStateException("Só é possível reativar vales suspensos");
        }

        vale.setStatus(ValeRefeicao.StatusValeRefeicao.ATIVO);

        logger.info("Vale refeição reativado para colaborador: {}", vale.getColaborador().getNome());

        return salvar(vale);
    }

    /**
     * Gera vales refeição para todos os colaboradores ativos em um mês
     */
    @Transactional
    public int gerarValesDoMes(Integer mes, Integer ano, BigDecimal valorDiarioPadrao) {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        int gerados = 0;

        for (Colaborador colaborador : colaboradores) {
            try {
                // Verificar se já existe vale para este mês/ano
                Optional<ValeRefeicao> valeExistente = buscarPorColaboradorMesAno(colaborador.getId(), mes, ano);
                if (valeExistente.isPresent()) {
                    continue; // Já existe, pular
                }

                // Verificar se tem vale ativo
                Optional<ValeRefeicao> valeAtivo = buscarAtivoDoColaborador(colaborador.getId());
                if (valeAtivo.isEmpty()) {
                    continue; // Não tem vale ativo, pular
                }

                ValeRefeicao valeBase = valeAtivo.get();
                ValeRefeicao novoVale = new ValeRefeicao();
                novoVale.setColaborador(colaborador);
                novoVale.setMesReferencia(mes);
                novoVale.setAnoReferencia(ano);
                novoVale.setValorDiario(valeBase.getValorDiario());
                novoVale.setTipo(valeBase.getTipo());
                novoVale.setOperadora(valeBase.getOperadora());
                novoVale.setStatus(ValeRefeicao.StatusValeRefeicao.ATIVO);
                novoVale.setDataAdesao(valeBase.getDataAdesao());

                gerarValeParaMes(novoVale, mes, ano);
                salvar(novoVale);
                gerados++;

            } catch (Exception e) {
                logger.error("Erro ao gerar vale refeição para colaborador {}: {}", 
                           colaborador.getNome(), e.getMessage());
            }
        }

        logger.info("Gerados {} vales refeição para {}/{}", gerados, mes, ano);
        return gerados;
    }

    /**
     * Gera dados do vale para um mês específico
     */
    private void gerarValeParaMes(ValeRefeicao vale, Integer mes, Integer ano) {
        vale.setMesReferencia(mes);
        vale.setAnoReferencia(ano);

        // Calcular dias úteis do mês
        int diasUteis = calcularDiasUteis(YearMonth.of(ano, mes));
        vale.setDiasUteis(diasUteis);

        // Os cálculos de valores serão feitos automaticamente pelo @PrePersist/@PreUpdate
    }

    /**
     * Calcula dias úteis do mês (simplificado)
     */
    private int calcularDiasUteis(YearMonth yearMonth) {
        // Implementação simplificada - assumir 22 dias úteis por mês
        // Futuramente pode ser melhorada para considerar feriados
        return 22;
    }

    /**
     * Exclui um vale refeição
     */
    @Transactional
    public void excluir(Long id) {
        ValeRefeicao vale = valeRefeicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vale refeição não encontrado"));

        logger.info("Excluindo vale refeição ID: {} - Colaborador: {}", 
                   id, vale.getColaborador().getNome());

        valeRefeicaoRepository.delete(vale);
    }

    /**
     * Atualiza valor diário de todos os vales ativos
     */
    @Transactional
    public int atualizarValorDiario(BigDecimal novoValor) {
        List<ValeRefeicao> valesAtivos = listarAtivos();
        int atualizados = 0;

        for (ValeRefeicao vale : valesAtivos) {
            vale.setValorDiario(novoValor);
            salvar(vale);
            atualizados++;
        }

        logger.info("Valor diário atualizado para {} vales refeição", atualizados);
        return atualizados;
    }

    /**
     * Calcula total gasto com vales refeição no mês
     */
    public BigDecimal calcularTotalGastoMes(Integer mes, Integer ano) {
        List<ValeRefeicao> vales = listarPorMesAno(mes, ano);
        
        return vales.stream()
                .filter(v -> v.getStatus() == ValeRefeicao.StatusValeRefeicao.ATIVO)
                .map(ValeRefeicao::getValorTotalMes)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula total de subsídio da empresa no mês
     */
    public BigDecimal calcularSubsidioEmpresaMes(Integer mes, Integer ano) {
        List<ValeRefeicao> vales = listarPorMesAno(mes, ano);
        
        return vales.stream()
                .filter(v -> v.getStatus() == ValeRefeicao.StatusValeRefeicao.ATIVO)
                .map(ValeRefeicao::getValorSubsidioEmpresa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}