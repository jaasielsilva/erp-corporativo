package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados para validação de transições de status de chamados
 * Garante que apenas transições válidas sejam permitidas
 */
@Component
public class ChamadoStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(ChamadoStateMachine.class);

    /**
     * Mapa de transições válidas
     * Chave: Status atual
     * Valor: Set de status permitidos como destino
     */
    private static final Map<StatusChamado, Set<StatusChamado>> TRANSICOES_VALIDAS = Map.of(
        StatusChamado.ABERTO, Set.of(
            StatusChamado.EM_ANDAMENTO, 
            StatusChamado.FECHADO  // Permite fechar diretamente se não precisar de atendimento
        ),
        StatusChamado.EM_ANDAMENTO, Set.of(
            StatusChamado.RESOLVIDO,
            StatusChamado.ABERTO  // Permite voltar para aberto se precisar de mais informações
        ),
        StatusChamado.RESOLVIDO, Set.of(
            StatusChamado.FECHADO,
            StatusChamado.ABERTO  // Permite reabrir se cliente não concordar com resolução
        ),
        StatusChamado.FECHADO, Set.of(
            StatusChamado.ABERTO  // Permite reabrir chamado fechado
        )
    );

    /**
     * Valida se uma transição de status é permitida
     * 
     * @param statusAtual Status atual do chamado
     * @param novoStatus Status desejado
     * @throws IllegalStateException se a transição não for válida
     */
    public void validarTransicao(StatusChamado statusAtual, StatusChamado novoStatus) {
        logger.debug("Validando transição de {} para {}", statusAtual, novoStatus);

        if (statusAtual == null) {
            throw new IllegalArgumentException("Status atual não pode ser nulo");
        }

        if (novoStatus == null) {
            throw new IllegalArgumentException("Novo status não pode ser nulo");
        }

        // Se o status é o mesmo, não há transição
        if (statusAtual.equals(novoStatus)) {
            logger.debug("Status permanece o mesmo: {}", statusAtual);
            return;
        }

        Set<StatusChamado> statusPermitidos = TRANSICOES_VALIDAS.get(statusAtual);
        
        if (statusPermitidos == null || !statusPermitidos.contains(novoStatus)) {
            String mensagem = String.format(
                "Transição de %s para %s não é permitida. Transições válidas: %s",
                statusAtual.getDescricao(),
                novoStatus.getDescricao(),
                statusPermitidos != null ? statusPermitidos : "nenhuma"
            );
            
            logger.warn("Transição inválida: {}", mensagem);
            throw new IllegalStateException(mensagem);
        }

        logger.debug("Transição válida de {} para {}", statusAtual, novoStatus);
    }

    /**
     * Retorna os status válidos para transição a partir do status atual
     * 
     * @param statusAtual Status atual do chamado
     * @return Set de status válidos para transição
     */
    public Set<StatusChamado> getTransicoesValidas(StatusChamado statusAtual) {
        if (statusAtual == null) {
            return Set.of();
        }
        
        return TRANSICOES_VALIDAS.getOrDefault(statusAtual, Set.of());
    }

    /**
     * Verifica se uma transição específica é válida sem lançar exceção
     * 
     * @param statusAtual Status atual do chamado
     * @param novoStatus Status desejado
     * @return true se a transição é válida, false caso contrário
     */
    public boolean isTransicaoValida(StatusChamado statusAtual, StatusChamado novoStatus) {
        try {
            validarTransicao(statusAtual, novoStatus);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retorna uma descrição das regras de transição para um status
     * 
     * @param status Status para obter as regras
     * @return String descritiva das transições possíveis
     */
    public String getRegrasTransicao(StatusChamado status) {
        Set<StatusChamado> transicoes = getTransicoesValidas(status);
        
        if (transicoes.isEmpty()) {
            return "Nenhuma transição disponível";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("De ").append(status.getDescricao()).append(" pode ir para: ");
        
        transicoes.forEach(s -> sb.append(s.getDescricao()).append(", "));
        
        // Remove a última vírgula
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        
        return sb.toString();
    }
}