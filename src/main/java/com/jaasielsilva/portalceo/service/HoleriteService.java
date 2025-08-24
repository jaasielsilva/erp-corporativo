package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class HoleriteService {

    private static final Logger logger = LoggerFactory.getLogger(HoleriteService.class);

    @Autowired
    private HoleriteRepository holeriteRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private FolhaPagamentoRepository folhaPagamentoRepository;

    /**
     * Busca holerite por ID
     */
    public Optional<Holerite> buscarPorId(Long id) {
        return holeriteRepository.findById(id);
    }

    /**
     * Lista todos os holerites de um colaborador
     */
    public List<Holerite> listarPorColaborador(Long colaboradorId) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        
        return holeriteRepository.findByColaboradorOrderByFolhaPagamento_AnoReferenciaDescFolhaPagamento_MesReferenciaDesc(colaborador);
    }

    /**
     * Lista holerites de um colaborador por ano
     */
    public List<Holerite> listarPorColaboradorEAno(Long colaboradorId, Integer ano) {
        return holeriteRepository.findByColaboradorAndAno(colaboradorId, ano);
    }

    /**
     * Lista todos os holerites de uma folha de pagamento
     */
    public List<Holerite> listarPorFolha(Long folhaId) {
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new IllegalArgumentException("Folha de pagamento não encontrada"));
        
        return holeriteRepository.findByFolhaPagamento(folha);
    }

    /**
     * Lista holerites por mês e ano
     */
    public List<Holerite> listarPorMesAno(Integer mes, Integer ano) {
        return holeriteRepository.findByMesAno(mes, ano);
    }

    /**
     * Lista holerites por departamento e folha
     */
    public List<Holerite> listarPorDepartamentoEFolha(Long departamentoId, Long folhaId) {
        return holeriteRepository.findByDepartamentoAndFolha(departamentoId, folhaId);
    }

    /**
     * Lista holerites de colaboradores ativos por folha
     */
    public List<Holerite> listarPorFolhaColaboradoresAtivos(Long folhaId) {
        return holeriteRepository.findByFolhaAndColaboradorAtivo(folhaId);
    }

    /**
     * Busca holerite específico de um colaborador em uma folha
     */
    public Optional<Holerite> buscarPorColaboradorEFolha(Long colaboradorId, Long folhaId) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new IllegalArgumentException("Folha de pagamento não encontrada"));
        
        return holeriteRepository.findByColaboradorAndFolhaPagamento(colaborador, folha);
    }

    /**
     * Verifica se existe holerite para um colaborador em uma folha
     */
    public boolean existeHoleritePorColaboradorEFolha(Long colaboradorId, Long folhaId) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new IllegalArgumentException("Folha de pagamento não encontrada"));
        
        return holeriteRepository.existsByColaboradorAndFolhaPagamento(colaborador, folha);
    }

    /**
     * Calcula totais de uma folha de pagamento
     */
    public ResumoFolhaDTO calcularResumoFolha(Long folhaId) {
        Double totalLiquido = holeriteRepository.sumSalarioLiquidoByFolha(folhaId);
        Double totalProventos = holeriteRepository.sumTotalProventosByFolha(folhaId);
        Double totalDescontos = holeriteRepository.sumTotalDescontosByFolha(folhaId);
        Long quantidadeHolerites = holeriteRepository.countByFolha(folhaId);

        ResumoFolhaDTO resumo = new ResumoFolhaDTO();
        resumo.setTotalLiquido(totalLiquido != null ? BigDecimal.valueOf(totalLiquido) : BigDecimal.ZERO);
        resumo.setTotalProventos(totalProventos != null ? BigDecimal.valueOf(totalProventos) : BigDecimal.ZERO);
        resumo.setTotalDescontos(totalDescontos != null ? BigDecimal.valueOf(totalDescontos) : BigDecimal.ZERO);
        resumo.setQuantidadeHolerites(quantidadeHolerites != null ? quantidadeHolerites : 0L);

        return resumo;
    }

    /**
     * Gera descrição do período do holerite para exibição
     */
    public String gerarDescricaoPeriodo(Holerite holerite) {
        if (holerite == null || holerite.getFolhaPagamento() == null) {
            return "";
        }

        FolhaPagamento folha = holerite.getFolhaPagamento();
        String mes = formatarMes(folha.getMesReferencia());
        return mes + "/" + folha.getAnoReferencia();
    }

    /**
     * Formata mês para exibição
     */
    private String formatarMes(Integer mes) {
        return switch (mes) {
            case 1 -> "Janeiro";
            case 2 -> "Fevereiro";
            case 3 -> "Março";
            case 4 -> "Abril";
            case 5 -> "Maio";
            case 6 -> "Junho";
            case 7 -> "Julho";
            case 8 -> "Agosto";
            case 9 -> "Setembro";
            case 10 -> "Outubro";
            case 11 -> "Novembro";
            case 12 -> "Dezembro";
            default -> mes.toString();
        };
    }

    /**
     * Gera número do holerite formatado
     */
    public String gerarNumeroHolerite(Holerite holerite) {
        if (holerite == null || holerite.getId() == null) {
            return "";
        }

        return String.format("%06d", holerite.getId());
    }

    /**
     * Verifica se holerite pode ser editado
     */
    public boolean podeEditarHolerite(Holerite holerite) {
        if (holerite == null || holerite.getFolhaPagamento() == null) {
            return false;
        }

        FolhaPagamento.StatusFolha status = holerite.getFolhaPagamento().getStatus();
        return status == FolhaPagamento.StatusFolha.EM_PROCESSAMENTO 
            || status == FolhaPagamento.StatusFolha.PROCESSADA;
    }

    /**
     * Salva ou atualiza um holerite
     */
    @Transactional
    public Holerite salvar(Holerite holerite) {
        if (!podeEditarHolerite(holerite)) {
            throw new IllegalStateException("Holerite não pode ser editado - folha já está fechada ou cancelada");
        }

        logger.info("Salvando holerite para colaborador: {} - {}", 
                   holerite.getColaborador().getNome(), gerarDescricaoPeriodo(holerite));

        return holeriteRepository.save(holerite);
    }

    /**
     * Exclui um holerite
     */
    @Transactional
    public void excluir(Long holeriteId) {
        Holerite holerite = holeriteRepository.findById(holeriteId)
                .orElseThrow(() -> new IllegalArgumentException("Holerite não encontrado"));

        if (!podeEditarHolerite(holerite)) {
            throw new IllegalStateException("Holerite não pode ser excluído - folha já está fechada ou cancelada");
        }

        logger.info("Excluindo holerite ID: {} - Colaborador: {}", 
                   holeriteId, holerite.getColaborador().getNome());

        holeriteRepository.delete(holerite);
    }

    /**
     * Busca último holerite de um colaborador
     */
    public Optional<Holerite> buscarUltimoHolerite(Long colaboradorId) {
        List<Holerite> holerites = listarPorColaborador(colaboradorId);
        
        return holerites.isEmpty() ? Optional.empty() : Optional.of(holerites.get(0));
    }

    /**
     * Calcula média salarial de um colaborador nos últimos N meses
     */
    public BigDecimal calcularMediaSalarial(Long colaboradorId, int meses) {
        List<Holerite> holerites = listarPorColaborador(colaboradorId);
        
        if (holerites.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Limitar aos últimos N meses
        List<Holerite> holeritesFiltrados = holerites.stream()
                .limit(meses)
                .toList();

        BigDecimal soma = holeritesFiltrados.stream()
                .map(Holerite::getSalarioLiquido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return soma.divide(BigDecimal.valueOf(holeritesFiltrados.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Gera data de referência formatada para o holerite
     */
    public String gerarDataReferencia(Holerite holerite) {
        if (holerite == null || holerite.getFolhaPagamento() == null) {
            return "";
        }

        FolhaPagamento folha = holerite.getFolhaPagamento();
        LocalDate dataRef = LocalDate.of(folha.getAnoReferencia(), folha.getMesReferencia(), 1);
        
        return dataRef.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }

    /**
     * DTO para resumo da folha de pagamento
     */
    public static class ResumoFolhaDTO {
        private BigDecimal totalLiquido;
        private BigDecimal totalProventos;
        private BigDecimal totalDescontos;
        private Long quantidadeHolerites;

        // Getters e Setters
        public BigDecimal getTotalLiquido() { return totalLiquido; }
        public void setTotalLiquido(BigDecimal totalLiquido) { this.totalLiquido = totalLiquido; }

        public BigDecimal getTotalProventos() { return totalProventos; }
        public void setTotalProventos(BigDecimal totalProventos) { this.totalProventos = totalProventos; }

        public BigDecimal getTotalDescontos() { return totalDescontos; }
        public void setTotalDescontos(BigDecimal totalDescontos) { this.totalDescontos = totalDescontos; }

        public Long getQuantidadeHolerites() { return quantidadeHolerites; }
        public void setQuantidadeHolerites(Long quantidadeHolerites) { this.quantidadeHolerites = quantidadeHolerites; }
    }
}