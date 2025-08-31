package com.jaasielsilva.portalceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude.StatusAdesao;
import com.jaasielsilva.portalceo.repository.AdesaoPlanoSaudeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AdesaoPlanoSaudeService {

    @Autowired
    private AdesaoPlanoSaudeRepository repository;


    /** Salva ou atualiza uma adesão */
    @Transactional
    public AdesaoPlanoSaude salvar(AdesaoPlanoSaude adesao) {
        return repository.save(adesao);
    }

    /** Busca uma adesão pelo ID */
    public AdesaoPlanoSaude buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adesão não encontrada: " + id));
    }

    /** Deleta uma adesão pelo ID */
    public void deletar(Long id) {
        repository.deleteById(id);
    }

    /** Lista todas as adesões de um colaborador */
    public List<AdesaoPlanoSaude> listarPorColaborador(Long colaboradorId) {
        return repository.findByColaboradorId(colaboradorId);
    }

    /** Lista todas as adesões ativas */
    public List<AdesaoPlanoSaude> listarAtivos() {
        return repository.findByStatus(StatusAdesao.ATIVA);

    }

    /** Atualiza os valores das adesões ativas de acordo com o plano */
    @Transactional
    public void atualizarValoresAdesoesAtivas() {
        List<AdesaoPlanoSaude> adesoesAtivas = repository.findByStatus("ATIVA");
        for (AdesaoPlanoSaude adesao : adesoesAtivas) {
            adesao.calcularValores(); // Recalcula valor mensal titular, dependentes e total
        }
    }

    /** Conta todos os titulares */
    public Long contarTitulares() {
        return repository.countTitulares();
    }

    /** Conta todos os dependentes */
    public Long contarDependentes() {
        return repository.countDependentes();
    }

    /** Total de beneficiários (titulares + dependentes) */
    public Long contarTotalBeneficiarios() {
        return contarTitulares() + contarDependentes();
    }

    /** Calcula o custo mensal total de todas as adesões ativas */
    public BigDecimal calcularCustoMensalTotal() {
        return repository.findAll().stream()
                .filter(AdesaoPlanoSaude::isAtiva)
                .map(AdesaoPlanoSaude::getValorTotalAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Calcula o custo da empresa (80% do total) */
    public BigDecimal calcularCustoEmpresa() {
        return calcularCustoMensalTotal().multiply(BigDecimal.valueOf(0.8));
    }

    /** Calcula o custo dos colaboradores (20% do total) */
    public BigDecimal calcularCustoColaboradores() {
        return calcularCustoMensalTotal().multiply(BigDecimal.valueOf(0.2));
    }

    /** Calcula o desconto dos colaboradores como percentual (20%) */
    public BigDecimal calcularDescontoColaboradoresPercentual() {
        return calcularCustoMensalTotal().multiply(BigDecimal.valueOf(0.2));
    }

    /** Retorna todas as adesões de plano de saúde paginadas */
    public Page<AdesaoPlanoSaude> listarTodosPaginado(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Retorna todas as adesões de plano de saúde com status ATIVA
     */
    public List<AdesaoPlanoSaude> listarTodosAtivos() {
        return repository.findByStatus(AdesaoPlanoSaude.StatusAdesao.ATIVA);
    }

    /**
     * Retorna todas as adesões de plano de saúde independente do status
     */
    public List<AdesaoPlanoSaude> listarTodos() {
        return repository.findAll();
    }

    /** Cancela uma adesão */
    @Transactional
    public void cancelarAdesao(Long id, String motivo) {
        AdesaoPlanoSaude adesao = buscarPorId(id);

        if (adesao.getStatus() == StatusAdesao.CANCELADA) {
            throw new IllegalArgumentException("Esta adesão já está cancelada");
        }

        if (adesao.getStatus() != StatusAdesao.ATIVA && adesao.getStatus() != StatusAdesao.PENDENTE) {
            throw new IllegalArgumentException("Apenas adesões ativas ou pendentes podem ser canceladas");
        }

        adesao.setStatus(StatusAdesao.CANCELADA);
        adesao.setDataCancelamento(LocalDate.now());

        if (motivo != null && !motivo.trim().isEmpty()) {
            String observacaoAtual = adesao.getObservacoes() != null ? adesao.getObservacoes() : "";
            String novaObservacao = observacaoAtual.isEmpty() ? "Cancelamento: " + motivo
                    : observacaoAtual + " | Cancelamento: " + motivo;
            adesao.setObservacoes(novaObservacao);
        }

        repository.save(adesao);
    }

    /** Exclui uma adesão logicamente (marca como INATIVO) */
    @Transactional
    public void excluir(Long id) {
        AdesaoPlanoSaude adesao = buscarPorId(id);
        adesao.setStatus(StatusAdesao.INATIVO);
        repository.save(adesao);
    }
}
