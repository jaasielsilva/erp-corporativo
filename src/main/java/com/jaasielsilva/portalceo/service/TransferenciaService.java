package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.financeiro.TransferenciaDTO;
import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.ContaBancariaRepository;
import com.jaasielsilva.portalceo.repository.FluxoCaixaRepository;
import com.jaasielsilva.portalceo.repository.TransferenciaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferenciaService {

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private FluxoCaixaRepository fluxoCaixaRepository;

    @Transactional
    public void realizarTransferencia(TransferenciaDTO dto, Usuario usuario) {
        // 1. Validar Contas
        ContaBancaria origem = contaBancariaRepository.findById(dto.getContaOrigemId())
                .orElseThrow(() -> new EntityNotFoundException("Conta de origem não encontrada"));
        
        ContaBancaria destino = contaBancariaRepository.findById(dto.getContaDestinoId())
                .orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada"));

        if (origem.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("A conta de origem e destino não podem ser iguais");
        }

        // 2. Validar Saldo
        if (origem.getSaldo().compareTo(dto.getValor()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na conta de origem");
        }

        // 3. Atualizar Saldos
        origem.setSaldo(origem.getSaldo().subtract(dto.getValor()));
        destino.setSaldo(destino.getSaldo().add(dto.getValor()));
        
        contaBancariaRepository.save(origem);
        contaBancariaRepository.save(destino);

        // 4. Registrar Transferência
        Transferencia transferencia = new Transferencia();
        transferencia.setContaOrigem(origem);
        transferencia.setContaDestino(destino);
        transferencia.setValor(dto.getValor());
        transferencia.setDataTransferencia(dto.getDataTransferencia());
        transferencia.setDescricao(dto.getDescricao());
        transferencia.setObservacoes(dto.getObservacoes());
        transferencia.setUsuarioCriacao(usuario); // Assumindo que BaseEntity tem esse campo
        
        transferencia = transferenciaRepository.save(transferencia);

        // 5. Registrar Movimentos no Fluxo de Caixa
        
        // Saída da Origem
        FluxoCaixa saida = new FluxoCaixa();
        saida.setDescricao("Transferência para " + destino.getNome() + " - " + dto.getDescricao());
        saida.setValor(dto.getValor());
        saida.setData(dto.getDataTransferencia());
        saida.setTipoMovimento(FluxoCaixa.TipoMovimento.SAIDA);
        saida.setCategoria(FluxoCaixa.CategoriaFluxo.TRANSFERENCIA); // Assumindo que existe, se não, usar OUTRAS_DESPESAS
        saida.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        saida.setContaBancaria(origem);
        saida.setTransferencia(transferencia);
        saida.setUsuarioCriacao(usuario);
        saida.setDataCriacao(LocalDateTime.now());
        
        fluxoCaixaRepository.save(saida);

        // Entrada no Destino
        FluxoCaixa entrada = new FluxoCaixa();
        entrada.setDescricao("Transferência de " + origem.getNome() + " - " + dto.getDescricao());
        entrada.setValor(dto.getValor());
        entrada.setData(dto.getDataTransferencia());
        entrada.setTipoMovimento(FluxoCaixa.TipoMovimento.ENTRADA);
        entrada.setCategoria(FluxoCaixa.CategoriaFluxo.TRANSFERENCIA); // Assumindo que existe
        entrada.setStatus(FluxoCaixa.StatusFluxo.REALIZADO);
        entrada.setContaBancaria(destino);
        entrada.setTransferencia(transferencia);
        entrada.setUsuarioCriacao(usuario);
        entrada.setDataCriacao(LocalDateTime.now());
        
        fluxoCaixaRepository.save(entrada);
    }
}
