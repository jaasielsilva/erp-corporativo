package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContabilidadeService {

    private final ContaContabilRepository contaContabilRepository;
    private final LancamentoContabilRepository lancamentoContabilRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final FluxoCaixaRepository fluxoCaixaRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final ContaPagarRepository contaPagarRepository;

    public void seedPlanoContasBaseSeNecessario() {
        criarContaSeNaoExistir("CAIXA", "Caixa", ContaContabil.TipoConta.ATIVO, ContaContabil.GrupoConta.CAIXA_BANCOS);
        criarContaSeNaoExistir("CAPITAL_SOCIAL", "Capital Social", ContaContabil.TipoConta.PATRIMONIO_LIQUIDO, ContaContabil.GrupoConta.CAPITAL_SOCIAL);
        criarContaSeNaoExistir("RESULTADO_EXERCICIO", "Resultado do Exercício", ContaContabil.TipoConta.PATRIMONIO_LIQUIDO, ContaContabil.GrupoConta.RESULTADO_EXERCICIO);
        criarContaSeNaoExistir("CONTAS_RECEBER", "Clientes (Contas a Receber)", ContaContabil.TipoConta.ATIVO, ContaContabil.GrupoConta.CONTAS_RECEBER);
        criarContaSeNaoExistir("CONTAS_PAGAR", "Fornecedores (Contas a Pagar)", ContaContabil.TipoConta.PASSIVO, ContaContabil.GrupoConta.CONTAS_PAGAR);
        criarContaSeNaoExistir("IMOBILIZADO", "Imobilizado", ContaContabil.TipoConta.ATIVO, ContaContabil.GrupoConta.IMOBILIZADO);
        criarContaSeNaoExistir("FINANCIAMENTOS", "Financiamentos", ContaContabil.TipoConta.PASSIVO, ContaContabil.GrupoConta.FINANCIAMENTOS);
        criarContaSeNaoExistir("OBRIGACOES_TRIBUTARIAS", "Obrigações Tributárias", ContaContabil.TipoConta.PASSIVO, ContaContabil.GrupoConta.OBRIGACOES_TRIBUTARIAS);
        criarContaSeNaoExistir("RECEITA_VENDAS", "Receita de Vendas", ContaContabil.TipoConta.RECEITA, ContaContabil.GrupoConta.RECEITA_VENDAS);
        criarContaSeNaoExistir("RECEITA_SERVICOS", "Receita de Serviços", ContaContabil.TipoConta.RECEITA, ContaContabil.GrupoConta.RECEITA_SERVICOS);
        criarContaSeNaoExistir("OUTRAS_RECEITAS", "Outras Receitas", ContaContabil.TipoConta.RECEITA, ContaContabil.GrupoConta.OUTRAS_RECEITAS);
        criarContaSeNaoExistir("DESPESAS_OPERACIONAIS", "Despesas Operacionais", ContaContabil.TipoConta.DESPESA, ContaContabil.GrupoConta.DESPESAS_OPERACIONAIS);
        criarContaSeNaoExistir("DESPESAS_FINANCEIRAS", "Despesas Financeiras", ContaContabil.TipoConta.DESPESA, ContaContabil.GrupoConta.DESPESAS_FINANCEIRAS);
        criarContaSeNaoExistir("IMPOSTOS_SOBRE_VENDAS", "Impostos sobre Vendas", ContaContabil.TipoConta.DESPESA, ContaContabil.GrupoConta.IMPOSTOS_SOBRE_VENDAS);
        criarContaSeNaoExistir("CUSTOS", "Custos", ContaContabil.TipoConta.DESPESA, ContaContabil.GrupoConta.CUSTOS);
    }

    public ContaContabil getConta(String codigo) {
        return contaContabilRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalStateException("Conta contábil não encontrada: " + codigo));
    }

    public ContaContabil getOuCriarContaBancaria(ContaBancaria contaBancaria) {
        if (contaBancaria == null) {
            seedPlanoContasBaseSeNecessario();
            return getConta("CAIXA");
        }
        return contaContabilRepository.findByContaBancaria(contaBancaria)
                .orElseGet(() -> {
                    ContaContabil c = new ContaContabil();
                    c.setCodigo("CAIXA_BANCOS_" + contaBancaria.getId());
                    c.setNome(contaBancaria.getNome());
                    c.setTipo(ContaContabil.TipoConta.ATIVO);
                    c.setGrupo(ContaContabil.GrupoConta.CAIXA_BANCOS);
                    c.setContaBancaria(contaBancaria);
                    c.setAtiva(true);
                    return contaContabilRepository.save(c);
                });
    }

    public Optional<ContaBancaria> buscarContaBancariaInicial(String nomeConta) {
        if (nomeConta != null && !nomeConta.isBlank()) {
            return contaBancariaRepository.findFirstByNomeIgnoreCase(nomeConta);
        }
        List<ContaBancaria> ativas = contaBancariaRepository.findByAtivoTrue();
        return ativas.stream().findFirst();
    }

    public void registrarCapitalSocialInicial(BigDecimal valor, String nomeContaBancaria) {
        if (valor == null || valor.signum() <= 0) return;
        if (lancamentoContabilRepository.existsByChaveIdempotencia("CAPITAL_SOCIAL_INICIAL")) return;

        seedPlanoContasBaseSeNecessario();

        ContaBancaria contaBancaria = buscarContaBancariaInicial(nomeContaBancaria)
                .orElseThrow(() -> new IllegalStateException("Conta bancária inicial não encontrada"));

        contaBancaria.setSaldo(contaBancaria.getSaldo().add(valor).setScale(2, RoundingMode.HALF_UP));
        contaBancariaRepository.save(contaBancaria);

        FluxoCaixa fluxo = new FluxoCaixa();
        fluxo.setDescricao("Aporte inicial - Capital Social");
        fluxo.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        fluxo.setData(LocalDate.now());
        fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.ENTRADA);
        fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.INVESTIMENTO);
        fluxo.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        fluxo.setContaBancaria(contaBancaria);
        fluxo.setDataCriacao(LocalDateTime.now());
        fluxoCaixaRepository.save(fluxo);

        ContaContabil caixa = getOuCriarContaBancaria(contaBancaria);
        ContaContabil capital = getConta("CAPITAL_SOCIAL");

        criarLancamento(
                LocalDate.now(),
                "Aporte inicial - Capital Social",
                "CAPITAL_SOCIAL_INICIAL",
                "SETUP",
                null,
                List.of(
                        new ItemLancamento(caixa, valor, BigDecimal.ZERO),
                        new ItemLancamento(capital, BigDecimal.ZERO, valor)
                )
        );
    }

    public void registrarCompraImobilizadoAVista(LocalDate data, String descricao, BigDecimal valor, Long contaBancariaId) {
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Valor inválido");
        seedPlanoContasBaseSeNecessario();
        ContaBancaria contaBancaria = contaBancariaRepository.findById(contaBancariaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta bancária não encontrada"));

        contaBancaria.setSaldo(contaBancaria.getSaldo().subtract(valor).setScale(2, RoundingMode.HALF_UP));
        contaBancariaRepository.save(contaBancaria);

        FluxoCaixa fluxo = new FluxoCaixa();
        fluxo.setDescricao("Compra imobilizado: " + (descricao != null ? descricao : ""));
        fluxo.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        fluxo.setData(data != null ? data : LocalDate.now());
        fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.SAIDA);
        fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.INVESTIMENTO);
        fluxo.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        fluxo.setContaBancaria(contaBancaria);
        fluxo.setDataCriacao(LocalDateTime.now());
        fluxoCaixaRepository.save(fluxo);

        ContaContabil caixa = getOuCriarContaBancaria(contaBancaria);
        ContaContabil imobilizado = getConta("IMOBILIZADO");

        criarLancamento(
                data != null ? data : LocalDate.now(),
                "Compra imobilizado: " + (descricao != null ? descricao : ""),
                null,
                "IMOBILIZADO_COMPRA_AVISTA",
                null,
                List.of(
                        new ItemLancamento(imobilizado, valor, BigDecimal.ZERO),
                        new ItemLancamento(caixa, BigDecimal.ZERO, valor)
                )
        );
    }

    public void registrarCompraImobilizadoFinanciada(LocalDate data, String descricao, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Valor inválido");
        seedPlanoContasBaseSeNecessario();
        ContaContabil imobilizado = getConta("IMOBILIZADO");
        ContaContabil financiamentos = getConta("FINANCIAMENTOS");

        criarLancamento(
                data != null ? data : LocalDate.now(),
                "Compra imobilizado financiada: " + (descricao != null ? descricao : ""),
                null,
                "IMOBILIZADO_COMPRA_FINANCIADA",
                null,
                List.of(
                        new ItemLancamento(imobilizado, valor, BigDecimal.ZERO),
                        new ItemLancamento(financiamentos, BigDecimal.ZERO, valor)
                )
        );
    }

    public void registrarPagamentoFinanciamento(LocalDate data, String descricao, BigDecimal principal, BigDecimal juros, Long contaBancariaId) {
        BigDecimal p = principal != null ? principal : BigDecimal.ZERO;
        BigDecimal j = juros != null ? juros : BigDecimal.ZERO;
        BigDecimal total = p.add(j);
        if (total.signum() <= 0) throw new IllegalArgumentException("Valor inválido");
        seedPlanoContasBaseSeNecessario();

        ContaBancaria contaBancaria = contaBancariaRepository.findById(contaBancariaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta bancária não encontrada"));

        contaBancaria.setSaldo(contaBancaria.getSaldo().subtract(total).setScale(2, RoundingMode.HALF_UP));
        contaBancariaRepository.save(contaBancaria);

        FluxoCaixa fluxo = new FluxoCaixa();
        fluxo.setDescricao("Pagamento financiamento: " + (descricao != null ? descricao : ""));
        fluxo.setValor(total.setScale(2, RoundingMode.HALF_UP));
        fluxo.setData(data != null ? data : LocalDate.now());
        fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.SAIDA);
        fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.FINANCIAMENTO);
        fluxo.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        fluxo.setContaBancaria(contaBancaria);
        fluxo.setDataCriacao(LocalDateTime.now());
        fluxoCaixaRepository.save(fluxo);

        ContaContabil caixa = getOuCriarContaBancaria(contaBancaria);
        ContaContabil financiamentos = getConta("FINANCIAMENTOS");
        ContaContabil despesasFinanceiras = getConta("DESPESAS_FINANCEIRAS");

        List<ItemLancamento> itens = new ArrayList<>();
        if (p.signum() > 0) itens.add(new ItemLancamento(financiamentos, p, BigDecimal.ZERO));
        if (j.signum() > 0) itens.add(new ItemLancamento(despesasFinanceiras, j, BigDecimal.ZERO));
        itens.add(new ItemLancamento(caixa, BigDecimal.ZERO, total));

        criarLancamento(
                data != null ? data : LocalDate.now(),
                "Pagamento financiamento: " + (descricao != null ? descricao : ""),
                null,
                "FINANCIAMENTO_PAGAMENTO",
                null,
                itens
        );
    }

    public void registrarReceitaCompetencia(ContaReceber contaReceber) {
        if (contaReceber == null) return;
        if (contaReceber.getId() == null) return;
        seedPlanoContasBaseSeNecessario();
        String chave = "CR_COMP_" + contaReceber.getId();
        if (lancamentoContabilRepository.existsByChaveIdempotencia(chave)) return;

        ContaContabil contasReceber = getConta("CONTAS_RECEBER");
        ContaContabil receita = switch (contaReceber.getTipo()) {
            case VENDA -> getConta("RECEITA_VENDAS");
            case SERVICO -> getConta("RECEITA_SERVICOS");
            default -> getConta("OUTRAS_RECEITAS");
        };

        BigDecimal valor = contaReceber.getValorTotal();

        criarLancamento(
                contaReceber.getDataEmissao() != null ? contaReceber.getDataEmissao() : LocalDate.now(),
                "Reconhecimento receita (competência) - " + contaReceber.getDescricao(),
                chave,
                "CONTA_RECEBER",
                contaReceber.getId(),
                List.of(
                        new ItemLancamento(contasReceber, valor, BigDecimal.ZERO),
                        new ItemLancamento(receita, BigDecimal.ZERO, valor)
                )
        );
    }

    public void registrarRecebimento(ContaReceber contaReceber, BigDecimal valorRecebido, ContaBancaria contaBancaria) {
        registrarRecebimento(contaReceber, valorRecebido, contaBancaria, null);
    }

    public void registrarRecebimento(ContaReceber contaReceber, BigDecimal valorRecebido, ContaBancaria contaBancaria, Long fluxoCaixaId) {
        if (contaReceber == null || contaReceber.getId() == null) return;
        if (valorRecebido == null || valorRecebido.signum() <= 0) return;
        seedPlanoContasBaseSeNecessario();
        String chave = fluxoCaixaId != null
                ? "CR_REC_" + contaReceber.getId() + "_FC_" + fluxoCaixaId
                : "CR_REC_" + contaReceber.getId() + "_" + System.currentTimeMillis();

        ContaContabil contasReceber = getConta("CONTAS_RECEBER");
        ContaContabil caixa = getOuCriarContaBancaria(contaBancaria);

        criarLancamento(
                LocalDate.now(),
                "Recebimento - " + contaReceber.getDescricao(),
                chave,
                "CONTA_RECEBER_RECEBIMENTO",
                contaReceber.getId(),
                List.of(
                        new ItemLancamento(caixa, valorRecebido, BigDecimal.ZERO),
                        new ItemLancamento(contasReceber, BigDecimal.ZERO, valorRecebido)
                )
        );
    }

    public void registrarDespesaCompetencia(ContaPagar contaPagar) {
        if (contaPagar == null || contaPagar.getId() == null) return;
        seedPlanoContasBaseSeNecessario();
        String chave = "CP_COMP_" + contaPagar.getId();
        if (lancamentoContabilRepository.existsByChaveIdempotencia(chave)) return;

        ContaContabil contasPagar = getConta("CONTAS_PAGAR");
        ContaContabil debito = switch (contaPagar.getTipo()) {
            case INVESTIMENTO -> getConta("IMOBILIZADO");
            case FINANCIAMENTO -> getConta("FINANCIAMENTOS");
            case TRIBUTARIA -> getConta("IMPOSTOS_SOBRE_VENDAS");
            default -> getConta("DESPESAS_OPERACIONAIS");
        };

        BigDecimal valor = contaPagar.getValorTotal();

        criarLancamento(
                contaPagar.getDataEmissao() != null ? contaPagar.getDataEmissao() : LocalDate.now(),
                "Reconhecimento despesa/obrigação (competência) - " + contaPagar.getDescricao(),
                chave,
                "CONTA_PAGAR",
                contaPagar.getId(),
                List.of(
                        new ItemLancamento(debito, valor, BigDecimal.ZERO),
                        new ItemLancamento(contasPagar, BigDecimal.ZERO, valor)
                )
        );
    }

    public void registrarPagamentoContaPagar(ContaPagar contaPagar, BigDecimal valorPago, ContaBancaria contaBancaria) {
        registrarPagamentoContaPagar(contaPagar, valorPago, contaBancaria, null);
    }

    public void registrarPagamentoContaPagar(ContaPagar contaPagar, BigDecimal valorPago, ContaBancaria contaBancaria, Long fluxoCaixaId) {
        if (contaPagar == null || contaPagar.getId() == null) return;
        if (valorPago == null || valorPago.signum() <= 0) return;
        seedPlanoContasBaseSeNecessario();
        String chave = fluxoCaixaId != null
                ? "CP_PAG_" + contaPagar.getId() + "_FC_" + fluxoCaixaId
                : "CP_PAG_" + contaPagar.getId() + "_" + System.currentTimeMillis();

        ContaContabil contasPagar = getConta("CONTAS_PAGAR");
        ContaContabil caixa = getOuCriarContaBancaria(contaBancaria);

        criarLancamento(
                LocalDate.now(),
                "Pagamento - " + contaPagar.getDescricao(),
                chave,
                "CONTA_PAGAR_PAGAMENTO",
                contaPagar.getId(),
                List.of(
                        new ItemLancamento(contasPagar, valorPago, BigDecimal.ZERO),
                        new ItemLancamento(caixa, BigDecimal.ZERO, valorPago)
                )
        );
    }

    public void sincronizarLancamentosAte(LocalDate ate) {
        if (ate == null) return;
        seedPlanoContasBaseSeNecessario();

        List<ContaReceber> contasReceber = contaReceberRepository.findByDataEmissaoLessThanEqual(ate);
        for (ContaReceber conta : contasReceber) {
            if (conta.getStatus() != ContaReceber.StatusContaReceber.CANCELADA) {
                registrarReceitaCompetencia(conta);
            }
        }

        List<ContaPagar> contasPagar = contaPagarRepository.findByDataEmissaoLessThanEqual(ate);
        for (ContaPagar conta : contasPagar) {
            if (conta.getStatus() != ContaPagar.StatusContaPagar.CANCELADA) {
                registrarDespesaCompetencia(conta);
            }
        }

        List<FluxoCaixa> fluxosRealizados = fluxoCaixaRepository.findByStatusAndDataBetweenOrderByData(
                FluxoCaixa.StatusFluxo.REALIZADO,
                LocalDate.of(1900, 1, 1),
                ate
        );

        for (FluxoCaixa fluxo : fluxosRealizados) {
            if (fluxo.getContaReceber() != null) {
                registrarRecebimento(fluxo.getContaReceber(), fluxo.getValor(), fluxo.getContaBancaria(), fluxo.getId());
            } else if (fluxo.getContaPagar() != null) {
                registrarPagamentoContaPagar(fluxo.getContaPagar(), fluxo.getValor(), fluxo.getContaBancaria(), fluxo.getId());
            }
        }
    }

    private void criarContaSeNaoExistir(String codigo, String nome, ContaContabil.TipoConta tipo, ContaContabil.GrupoConta grupo) {
        if (contaContabilRepository.existsByCodigo(codigo)) return;
        ContaContabil c = new ContaContabil();
        c.setCodigo(codigo);
        c.setNome(nome);
        c.setTipo(tipo);
        c.setGrupo(grupo);
        c.setAtiva(true);
        contaContabilRepository.save(c);
    }

    private void criarLancamento(LocalDate data, String descricao, String chaveIdempotencia, String referenciaTipo, Long referenciaId, List<ItemLancamento> itens) {
        if (itens == null || itens.isEmpty()) throw new IllegalArgumentException("Itens obrigatórios");
        if (chaveIdempotencia != null && lancamentoContabilRepository.existsByChaveIdempotencia(chaveIdempotencia)) return;

        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;

        for (ItemLancamento item : itens) {
            BigDecimal d = normalizar(item.debito);
            BigDecimal c = normalizar(item.credito);
            if (d.signum() < 0 || c.signum() < 0) throw new IllegalArgumentException("Valores negativos não permitidos");
            if (d.signum() == 0 && c.signum() == 0) throw new IllegalArgumentException("Item com valor zerado");
            if (d.signum() > 0 && c.signum() > 0) throw new IllegalArgumentException("Item não pode ter débito e crédito simultaneamente");
            totalDebitos = totalDebitos.add(d);
            totalCreditos = totalCreditos.add(c);
        }

        if (totalDebitos.setScale(2, RoundingMode.HALF_UP).compareTo(totalCreditos.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("Lançamento desequilibrado (débito != crédito)");
        }

        LancamentoContabil lancamento = new LancamentoContabil();
        lancamento.setData(data != null ? data : LocalDate.now());
        lancamento.setDescricao(descricao != null && !descricao.isBlank() ? descricao : "Lançamento contábil");
        lancamento.setChaveIdempotencia(chaveIdempotencia);
        lancamento.setReferenciaTipo(referenciaTipo);
        lancamento.setReferenciaId(referenciaId);
        lancamento.setStatus(LancamentoContabil.StatusLancamento.LANCADO);

        List<LancamentoContabilItem> itensEntity = new ArrayList<>();
        for (ItemLancamento item : itens) {
            LancamentoContabilItem li = new LancamentoContabilItem();
            li.setLancamento(lancamento);
            li.setContaContabil(item.conta);
            li.setDebito(normalizar(item.debito));
            li.setCredito(normalizar(item.credito));
            itensEntity.add(li);
        }
        lancamento.setItens(itensEntity);

        lancamentoContabilRepository.save(lancamento);
    }

    private BigDecimal normalizar(BigDecimal v) {
        return (v != null ? v : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private record ItemLancamento(ContaContabil conta, BigDecimal debito, BigDecimal credito) {
    }
}
