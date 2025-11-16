package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolhaPagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(FolhaPagamentoService.class);

    @Autowired
    private FolhaPagamentoRepository folhaPagamentoRepository;

    @Autowired
    private HoleriteRepository holeriteRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private ValeTransporteRepository valeTransporteRepository;

    @Autowired
    private ValeRefeicaoRepository valeRefeicaoRepository;

    @Autowired
    private AdesaoPlanoSaudeRepository adesaoPlanoSaudeRepository;

    /**
     * Lista todas as folhas de pagamento ordenadas por ano e mês
     */
    public List<FolhaPagamento> listarTodas() {
        return folhaPagamentoRepository.findAll();
    }

    /**
     * Busca folhas de pagamento por ano
     */
    public List<FolhaPagamento> buscarPorAno(Integer ano) {
        return folhaPagamentoRepository.findByAnoReferenciaOrderByMesReferenciaDesc(ano);
    }

    /**
     * Busca folha de pagamento por mês e ano
     */
    public Optional<FolhaPagamento> buscarPorMesAno(Integer mes, Integer ano) {
        return folhaPagamentoRepository.findFolhaByMesAno(mes, ano);
    }

    /**
     * Busca folha de pagamento por ID
     */
    public Optional<FolhaPagamento> buscarPorId(Long id) {
        return folhaPagamentoRepository.findById(id);
    }

    /**
     * Verifica se já existe folha para o mês/ano
     */
    public boolean existeFolhaPorMesAno(Integer mes, Integer ano) {
        return folhaPagamentoRepository.existsByMesReferenciaAndAnoReferencia(mes, ano);
    }

    /**
     * Gera folha de pagamento para um mês/ano específico
     */
    @Transactional
    public FolhaPagamento gerarFolhaPagamento(Integer mes, Integer ano, Usuario usuarioProcessamento) {
        logger.info("Iniciando geração de folha de pagamento para {}/{}", mes, ano);

        // Verificar se já existe folha para este período
        if (existeFolhaPorMesAno(mes, ano)) {
            throw new IllegalStateException("Já existe folha de pagamento para " + mes + "/" + ano);
        }

        // Criar nova folha de pagamento
        FolhaPagamento folha = new FolhaPagamento();
        folha.setMesReferencia(mes);
        folha.setAnoReferencia(ano);
        folha.setUsuarioProcessamento(usuarioProcessamento);
        folha.setDataProcessamento(LocalDate.now());
        folha.setStatus(FolhaPagamento.StatusFolha.EM_PROCESSAMENTO);

        // Inicializar totais
        BigDecimal totalBruto = BigDecimal.ZERO;
        BigDecimal totalDescontos = BigDecimal.ZERO;
        BigDecimal totalInss = BigDecimal.ZERO;
        BigDecimal totalIrrf = BigDecimal.ZERO;
        BigDecimal totalFgts = BigDecimal.ZERO;

        folha.setTotalBruto(totalBruto);
        folha.setTotalDescontos(totalDescontos);
        folha.setTotalLiquido(totalBruto.subtract(totalDescontos));
        folha.setTotalInss(totalInss);
        folha.setTotalIrrf(totalIrrf);
        folha.setTotalFgts(totalFgts);

        // Salvar folha primeiro para obter ID
        folha = folhaPagamentoRepository.save(folha);

        // Buscar colaboradores ativos
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        List<Holerite> holerites = new ArrayList<>();

        for (Colaborador colaborador : colaboradores) {
            try {
                Holerite holerite = gerarHolerite(colaborador, folha, mes, ano);
                holerites.add(holerite);

                // Somar aos totais da folha
                totalBruto = totalBruto.add(holerite.getTotalProventos());
                totalDescontos = totalDescontos.add(holerite.getTotalDescontos());
                totalInss = totalInss.add(holerite.getDescontoInss());
                totalIrrf = totalIrrf.add(holerite.getDescontoIrrf());
                totalFgts = totalFgts.add(holerite.getDescontoFgts());

            } catch (Exception e) {
                logger.error("Erro ao gerar holerite para colaborador {}: {}", colaborador.getNome(), e.getMessage());
                // Continua processando outros colaboradores
            }
        }

        // Atualizar totais da folha
        folha.setTotalBruto(totalBruto);
        folha.setTotalDescontos(totalDescontos);
        folha.setTotalLiquido(totalBruto.subtract(totalDescontos));
        folha.setTotalInss(totalInss);
        folha.setTotalIrrf(totalIrrf);
        folha.setTotalFgts(totalFgts);
        folha.setStatus(FolhaPagamento.StatusFolha.PROCESSADA);

        folha = folhaPagamentoRepository.save(folha);

        logger.info("Folha de pagamento {}/{} gerada com sucesso. {} holerites criados.", mes, ano, holerites.size());
        return folha;
    }

    /**
     * Gera holerite individual para um colaborador
     */
    private Holerite gerarHolerite(Colaborador colaborador, FolhaPagamento folha, Integer mes, Integer ano) {
        Holerite holerite = new Holerite();
        holerite.setColaborador(colaborador);
        holerite.setFolhaPagamento(folha);
        holerite.setSalarioBase(colaborador.getSalario());

        // Calcular dados de ponto (dias e horas trabalhadas)
        calcularDadosPonto(holerite, colaborador, mes, ano);

        // Calcular proventos
        calcularProventos(holerite, colaborador, mes, ano);

        // Calcular descontos
        calcularDescontos(holerite, colaborador, mes, ano);

        return holeriteRepository.save(holerite);
    }

    /**
     * Calcula dados de ponto para o holerite
     */
    private void calcularDadosPonto(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano) {
        // Obter número de dias úteis do mês
        YearMonth yearMonth = YearMonth.of(ano, mes);
        int diasUteis = calcularDiasUteis(yearMonth);
        
        holerite.setDiasTrabalhados(diasUteis);
        holerite.setHorasTrabalhadas(diasUteis * 8); // 8 horas por dia
        holerite.setFaltas(0); // Implementar lógica de faltas futuramente
        holerite.setAtrasos(0); // Implementar lógica de atrasos futuramente

        // Buscar registros de ponto do colaborador para calcular horas extras
        // Por enquanto, assumir 0 horas extras
        BigDecimal horasExtras = calcularHorasExtras(colaborador, mes, ano);
        BigDecimal valorHoraExtra = calcularValorHoraExtra(holerite.getSalarioBase());
        holerite.setHorasExtras(horasExtras.multiply(valorHoraExtra));
    }

    /**
     * Calcula proventos do holerite
     */
    private void calcularProventos(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano) {
        // Proventos básicos já definidos
        // Adicionar outros proventos conforme necessário
        holerite.setAdicionalNoturno(BigDecimal.ZERO);
        holerite.setAdicionalPericulosidade(BigDecimal.ZERO);
        holerite.setAdicionalInsalubridade(BigDecimal.ZERO);
        holerite.setComissoes(BigDecimal.ZERO);
        holerite.setBonificacoes(BigDecimal.ZERO);

        // Buscar benefícios do colaborador
        calcularBeneficios(holerite, colaborador, mes, ano);
    }

    /**
     * Calcula benefícios do colaborador
     */
    private void calcularBeneficios(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano) {
        // Vale Transporte
        Optional<ValeTransporte> valeTransporte = valeTransporteRepository
                .findByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano);
        if (valeTransporte.isPresent() && valeTransporte.get().isAtivo()) {
            holerite.setValeTransporte(valeTransporte.get().getValorSubsidioEmpresa());
        } else {
            holerite.setValeTransporte(BigDecimal.ZERO);
        }

        // Vale Refeição
        Optional<ValeRefeicao> valeRefeicao = valeRefeicaoRepository
                .findByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano);
        if (valeRefeicao.isPresent() && valeRefeicao.get().isAtivo()) {
            holerite.setValeRefeicao(valeRefeicao.get().getValorSubsidioEmpresa());
        } else {
            holerite.setValeRefeicao(BigDecimal.ZERO);
        }

        // Auxílio Saúde
        Optional<AdesaoPlanoSaude> planoSaude = adesaoPlanoSaudeRepository
                .findAdesaoAtivaByColaborador(colaborador.getId());
        if (planoSaude.isPresent()) {
            // Usar o método correto para calcular subsídio da empresa
            BigDecimal subsidio = planoSaude.get().getValorSubsidioEmpresa();
            holerite.setAuxilioSaude(subsidio);
        } else {
            holerite.setAuxilioSaude(BigDecimal.ZERO);
        }
    }

    /**
     * Calcula descontos do holerite
     */
    private void calcularDescontos(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano) {
        BigDecimal salarioBruto = holerite.getSalarioBase().add(holerite.getHorasExtras());

        // INSS
        holerite.setDescontoInss(calcularInss(salarioBruto));

        // IRRF (calculado após INSS)
        BigDecimal baseIrrf = salarioBruto.subtract(holerite.getDescontoInss());
        holerite.setDescontoIrrf(calcularIrrf(baseIrrf));

        // FGTS (8% sobre salário bruto)
        holerite.setDescontoFgts(salarioBruto.multiply(BigDecimal.valueOf(0.08)).setScale(2, RoundingMode.HALF_UP));

        // Descontos de benefícios
        calcularDescontosBeneficios(holerite, colaborador, mes, ano);

        holerite.setOutrosDescontos(BigDecimal.ZERO);
    }

    /**
     * Calcula descontos de benefícios
     */
    private void calcularDescontosBeneficios(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano) {
        // Desconto Vale Transporte
        Optional<ValeTransporte> valeTransporte = valeTransporteRepository
                .findByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano);
        if (valeTransporte.isPresent() && valeTransporte.get().isAtivo()) {
            holerite.setDescontoValeTransporte(valeTransporte.get().getValorDesconto());
        } else {
            holerite.setDescontoValeTransporte(BigDecimal.ZERO);
        }

        // Desconto Vale Refeição
        Optional<ValeRefeicao> valeRefeicao = valeRefeicaoRepository
                .findByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano);
        if (valeRefeicao.isPresent() && valeRefeicao.get().isAtivo()) {
            holerite.setDescontoValeRefeicao(valeRefeicao.get().getValorDesconto());
        } else {
            holerite.setDescontoValeRefeicao(BigDecimal.ZERO);
        }

        // Desconto Plano de Saúde
        Optional<AdesaoPlanoSaude> planoSaude = adesaoPlanoSaudeRepository
                .findAdesaoAtivaByColaborador(colaborador.getId());
        if (planoSaude.isPresent()) {
            // Usar o método correto para calcular desconto do colaborador
            BigDecimal desconto = planoSaude.get().getValorDesconto();
            holerite.setDescontoPlanoSaude(desconto);
        } else {
            holerite.setDescontoPlanoSaude(BigDecimal.ZERO);
        }
    }

    /**
     * Calcula INSS baseado na tabela atual (2024)
     */
    private BigDecimal calcularInss(BigDecimal salarioBruto) {
        if (salarioBruto.compareTo(BigDecimal.valueOf(1412.00)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.075)).setScale(2, RoundingMode.HALF_UP);
        } else if (salarioBruto.compareTo(BigDecimal.valueOf(2666.68)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.09)).setScale(2, RoundingMode.HALF_UP);
        } else if (salarioBruto.compareTo(BigDecimal.valueOf(4000.03)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        } else if (salarioBruto.compareTo(BigDecimal.valueOf(7786.02)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.14)).setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(1090.04); // Teto do INSS 2024
        }
    }

    /**
     * Calcula IRRF baseado na tabela atual (2024)
     */
    private BigDecimal calcularIrrf(BigDecimal baseCalculo) {
        if (baseCalculo.compareTo(BigDecimal.valueOf(2112.00)) <= 0) {
            return BigDecimal.ZERO; // Isento
        } else if (baseCalculo.compareTo(BigDecimal.valueOf(2826.65)) <= 0) {
            return baseCalculo.multiply(BigDecimal.valueOf(0.075)).subtract(BigDecimal.valueOf(158.40))
                    .setScale(2, RoundingMode.HALF_UP);
        } else if (baseCalculo.compareTo(BigDecimal.valueOf(3751.05)) <= 0) {
            return baseCalculo.multiply(BigDecimal.valueOf(0.15)).subtract(BigDecimal.valueOf(370.40))
                    .setScale(2, RoundingMode.HALF_UP);
        } else if (baseCalculo.compareTo(BigDecimal.valueOf(4664.68)) <= 0) {
            return baseCalculo.multiply(BigDecimal.valueOf(0.225)).subtract(BigDecimal.valueOf(651.73))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return baseCalculo.multiply(BigDecimal.valueOf(0.275)).subtract(BigDecimal.valueOf(884.96))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcula horas extras do colaborador no mês
     */
    private BigDecimal calcularHorasExtras(Colaborador colaborador, Integer mes, Integer ano) {
        // Por enquanto retorna 0, implementar lógica de ponto depois
        return BigDecimal.ZERO;
    }

    /**
     * Calcula valor da hora extra (salário base / 220 * 1.5)
     */
    private BigDecimal calcularValorHoraExtra(BigDecimal salarioBase) {
        return salarioBase.divide(BigDecimal.valueOf(220), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(1.5))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula dias úteis do mês
     */
    private int calcularDiasUteis(YearMonth yearMonth) {
        // Implementação simplificada - assumir 22 dias úteis por mês
        // Futuramente pode ser melhorada para considerar feriados
        return 22;
    }

    /**
     * Fecha uma folha de pagamento
     */
    @Transactional
    public FolhaPagamento fecharFolha(Long folhaId, Usuario usuario) {
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new IllegalArgumentException("Folha de pagamento não encontrada"));

        if (folha.getStatus() != FolhaPagamento.StatusFolha.PROCESSADA) {
            throw new IllegalStateException("Só é possível fechar folhas que estão processadas");
        }

        folha.setStatus(FolhaPagamento.StatusFolha.FECHADA);
        folha.setDataFechamento(LocalDate.now());
        
        return folhaPagamentoRepository.save(folha);
    }

    /**
     * Cancela uma folha de pagamento
     */
    @Transactional
    public void cancelarFolha(Long folhaId, Usuario usuario) {
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new IllegalArgumentException("Folha de pagamento não encontrada"));

        if (folha.getStatus() == FolhaPagamento.StatusFolha.FECHADA) {
            throw new IllegalStateException("Não é possível cancelar folha já fechada");
        }

        folha.setStatus(FolhaPagamento.StatusFolha.CANCELADA);
        folhaPagamentoRepository.save(folha);

        // Excluir holerites associados
        List<Holerite> holerites = holeriteRepository.findByFolhaPagamento(folha);
        holeriteRepository.deleteAll(holerites);

        logger.info("Folha de pagamento {}/{} cancelada por {}", folha.getMesReferencia(), 
                    folha.getAnoReferencia(), usuario.getEmail());
    }

    /**
     * Busca folhas recentes (últimos 2 anos)
     */
    public List<FolhaPagamento> buscarFolhasRecentes() {
        Integer anoInicio = LocalDate.now().getYear() - 2;
        return folhaPagamentoRepository.findFolhasRecentes(anoInicio);
    }
}