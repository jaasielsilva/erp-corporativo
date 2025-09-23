package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.repository.PlanoSaudeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanoSaudeService {

    private final PlanoSaudeRepository planoSaudeRepository;

    public PlanoSaudeService(PlanoSaudeRepository planoSaudeRepository) {
        this.planoSaudeRepository = planoSaudeRepository;
    }

    /**
     * Retorna todos os planos de saúde que estão ativos, ordenados pelo nome.
     * Útil para exibir uma lista completa de planos disponíveis no sistema.
     */
    public List<PlanoSaude> listarTodosAtivos() {
        return planoSaudeRepository.findByAtivoTrueOrderByNome();
    }

    /**
     * Retorna todas as operadoras distintas de planos de saúde que estão ativos.
     * Útil para preencher filtros ou selects de operadora na interface.
     */
    public List<String> listarOperadorasAtivas() {
        return planoSaudeRepository.findDistinctOperadoras();
    }

    /**
     * Filtra os planos de acordo com o tipo (Básico, Intermediário, Premium, Executivo)
     * e retorna apenas os planos ativos, ordenados pelo valor do titular.
     * Útil para mostrar planos específicos por categoria.
     */
    public List<PlanoSaude> filtrarPorTipo(PlanoSaude.TipoPlano tipo) {
        return planoSaudeRepository.findByTipoAndAtivoTrueOrderByValorTitular(tipo);
    }

    /**
     * Filtra os planos cujo valor do titular esteja dentro da faixa informada.
     * Útil para pesquisar planos de acordo com o orçamento ou custo desejado.
     */
    public List<PlanoSaude> filtrarPorFaixaValor(Double valorMin, Double valorMax) {
        return planoSaudeRepository.findByFaixaValor(valorMin, valorMax);
    }

    /**
     * Verifica se já existe um plano de saúde com o código informado.
     * Útil para evitar duplicidade ao cadastrar novos planos.
     */
    public boolean existePorCodigo(String codigo) {
        return planoSaudeRepository.existsByCodigo(codigo);
    }

    /** Buscar plano de saúde por ID */
    public PlanoSaude buscarPorId(Long id) {
        return planoSaudeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de saúde não encontrado: " + id));
    }
}
