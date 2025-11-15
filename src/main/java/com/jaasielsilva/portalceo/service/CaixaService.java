package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Caixa;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.CaixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CaixaService {

    @Autowired
    private CaixaRepository caixaRepository;

    // Abrir caixa
    public Caixa abrirCaixa(BigDecimal valorInicial, Usuario usuario, String observacoes) {
        // Verificar se já existe caixa aberto
        if (existeCaixaAberto()) {
            throw new IllegalStateException("Já existe um caixa aberto. Feche o caixa atual antes de abrir um novo.");
        }

        Caixa caixa = new Caixa();
        caixa.setDataAbertura(LocalDateTime.now());
        caixa.setValorInicial(valorInicial);
        caixa.setUsuarioAbertura(usuario);
        caixa.setObservacoes(observacoes);
        caixa.setStatus("ABERTO");
        
        return caixaRepository.save(caixa);
    }

    // Fechar caixa
    public Caixa fecharCaixa(Long caixaId, BigDecimal valorFinal, Usuario usuario, String observacoes) {
        Optional<Caixa> caixaOpt = caixaRepository.findById(caixaId);
        if (caixaOpt.isEmpty()) {
            throw new IllegalArgumentException("Caixa não encontrado");
        }

        Caixa caixa = caixaOpt.get();
        if (!"ABERTO".equals(caixa.getStatus())) {
            throw new IllegalStateException("Caixa já está fechado");
        }

        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setValorFinal(valorFinal);
        caixa.setUsuarioFechamento(usuario);
        caixa.setStatus("FECHADO");
        
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            String obsExistente = caixa.getObservacoes() != null ? caixa.getObservacoes() : "";
            caixa.setObservacoes(obsExistente + "\nFechamento: " + observacoes);
        }
        
        return caixaRepository.save(caixa);
    }

    // Buscar caixa aberto
    public Optional<Caixa> buscarCaixaAberto() {
        return caixaRepository.findFirstByStatusOrderByDataAberturaDesc("ABERTO");
    }

    // Verificar se existe caixa aberto
    public boolean existeCaixaAberto() {
        return caixaRepository.existsByStatus("ABERTO");
    }

    

    // Buscar por ID
    public Optional<Caixa> buscarPorId(Long id) {
        return caixaRepository.findById(id);
    }

    // Buscar caixas por período
    public List<Caixa> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return caixaRepository.findByPeriodo(inicio, fim);
    }

    // Buscar caixas fechados por período
    public List<Caixa> buscarCaixasFechadosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return caixaRepository.findCaixasFechadosPorPeriodo(inicio, fim);
    }

    // Buscar últimos caixas
    public List<Caixa> buscarUltimosCaixas() {
        return caixaRepository.findUltimosCaixas();
    }

    // Relatório de vendas por período
    public BigDecimal calcularTotalVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        Double total = caixaRepository.somarVendasPorPeriodo(inicio, fim);
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }

    // Obter resumo do caixa atual
    public Optional<CaixaResumo> obterResumoCaixaAtual() {
        Optional<Caixa> caixaOpt = buscarCaixaAberto();
        if (caixaOpt.isPresent()) {
            Caixa caixa = caixaOpt.get();
            return Optional.of(new CaixaResumo(
                caixa.getId(),
                caixa.getDataAbertura(),
                caixa.getValorInicial(),
                caixa.getTotalVendas(),
                caixa.getQuantidadeVendas(),
                caixa.getTotalDinheiro(),
                caixa.getTotalCartao(),
                caixa.getTotalPix(),
                caixa.calcularTotalEsperado()
            ));
        }
        return Optional.empty();
    }

    // Classe interna para resumo do caixa
    public static class CaixaResumo {
        private Long id;
        private LocalDateTime dataAbertura;
        private BigDecimal valorInicial;
        private BigDecimal totalVendas;
        private Integer quantidadeVendas;
        private BigDecimal totalDinheiro;
        private BigDecimal totalCartao;
        private BigDecimal totalPix;
        private BigDecimal totalEsperado;

        public CaixaResumo(Long id, LocalDateTime dataAbertura, BigDecimal valorInicial, 
                          BigDecimal totalVendas, Integer quantidadeVendas, BigDecimal totalDinheiro,
                          BigDecimal totalCartao, BigDecimal totalPix, BigDecimal totalEsperado) {
            this.id = id;
            this.dataAbertura = dataAbertura;
            this.valorInicial = valorInicial;
            this.totalVendas = totalVendas;
            this.quantidadeVendas = quantidadeVendas;
            this.totalDinheiro = totalDinheiro;
            this.totalCartao = totalCartao;
            this.totalPix = totalPix;
            this.totalEsperado = totalEsperado;
        }

        // Getters
        public Long getId() { return id; }
        public LocalDateTime getDataAbertura() { return dataAbertura; }
        public BigDecimal getValorInicial() { return valorInicial; }
        public BigDecimal getTotalVendas() { return totalVendas; }
        public Integer getQuantidadeVendas() { return quantidadeVendas; }
        public BigDecimal getTotalDinheiro() { return totalDinheiro; }
        public BigDecimal getTotalCartao() { return totalCartao; }
        public BigDecimal getTotalPix() { return totalPix; }
        public BigDecimal getTotalEsperado() { return totalEsperado; }
    }
}