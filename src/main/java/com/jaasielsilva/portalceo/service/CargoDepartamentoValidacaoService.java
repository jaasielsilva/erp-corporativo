package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.CargoDepartamentoAssociacao;
import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.repository.CargoDepartamentoAssociacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CargoDepartamentoValidacaoService {
    
    @Autowired
    private CargoDepartamentoAssociacaoRepository associacaoRepository;
    
    /**
     * Busca todos os cargos válidos para um departamento
     */
    public List<Cargo> buscarCargosValidosParaDepartamento(Departamento departamento) {
        return associacaoRepository.findCargosByDepartamentoAndAtivoTrue(departamento);
    }
    
    /**
     * Busca todos os departamentos válidos para um cargo
     */
    public List<Departamento> buscarDepartamentosValidosParaCargo(Cargo cargo) {
        return associacaoRepository.findDepartamentosByCargoAndAtivoTrue(cargo);
    }
    
    /**
     * Valida se uma associação cargo-departamento é permitida
     */
    public boolean isAssociacaoValida(Cargo cargo, Departamento departamento) {
        // Se não há associações definidas, permite qualquer combinação (flexibilidade)
        List<CargoDepartamentoAssociacao> todasAssociacoes = associacaoRepository.findAllAtivasOrderByDepartamentoAndCargo();
        if (todasAssociacoes.isEmpty()) {
            return true;
        }
        
        // Verifica se existe uma associação específica
        return associacaoRepository.findByCargoAndDepartamentoAndAtivoTrue(cargo, departamento).isPresent();
    }
    
    /**
     * Cria uma nova associação cargo-departamento
     */
    public CargoDepartamentoAssociacao criarAssociacao(Cargo cargo, Departamento departamento, 
                                                       boolean obrigatorio, String observacoes) {
        // Verifica se já existe
        Optional<CargoDepartamentoAssociacao> existente = associacaoRepository
            .findByCargoAndDepartamentoAndAtivoTrue(cargo, departamento);
        
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Associação já existe entre este cargo e departamento");
        }
        
        CargoDepartamentoAssociacao associacao = new CargoDepartamentoAssociacao();
        associacao.setCargo(cargo);
        associacao.setDepartamento(departamento);
        associacao.setObrigatorio(obrigatorio);
        associacao.setObservacoes(observacoes);
        associacao.setAtivo(true);
        
        return associacaoRepository.save(associacao);
    }
    
    /**
     * Valida se um cargo pode ser usado em qualquer departamento
     */
    public boolean isCargoFlexivel(Cargo cargo) {
        return !associacaoRepository.hasAssociacaoObrigatoria(cargo);
    }
    
    /**
     * Busca associações obrigatórias para um cargo
     */
    public List<CargoDepartamentoAssociacao> buscarAssociacoesObrigatorias(Cargo cargo) {
        return associacaoRepository.findByCargoAndObrigatorioTrueAndAtivoTrue(cargo);
    }
    
    /**
     * Desativa uma associação
     */
    public void desativarAssociacao(Long id) {
        CargoDepartamentoAssociacao associacao = associacaoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Associação não encontrada"));
        
        associacao.setAtivo(false);
        associacaoRepository.save(associacao);
    }
    
    /**
     * Busca todas as associações ativas
     */
    public List<CargoDepartamentoAssociacao> buscarTodasAssociacoesAtivas() {
        return associacaoRepository.findAllAtivasOrderByDepartamentoAndCargo();
    }
    
    /**
     * Valida uma associação com mensagem de erro detalhada
     */
    public String validarAssociacaoComMensagem(Cargo cargo, Departamento departamento) {
        if (isAssociacaoValida(cargo, departamento)) {
            return null; // Válida
        }
        
        List<Departamento> departamentosValidos = buscarDepartamentosValidosParaCargo(cargo);
        if (departamentosValidos.isEmpty()) {
            return "Este cargo pode ser usado em qualquer departamento";
        }
        
        StringBuilder sb = new StringBuilder("Este cargo só é válido para os departamentos: ");
        for (int i = 0; i < departamentosValidos.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(departamentosValidos.get(i).getNome());
        }
        
        return sb.toString();
    }
}