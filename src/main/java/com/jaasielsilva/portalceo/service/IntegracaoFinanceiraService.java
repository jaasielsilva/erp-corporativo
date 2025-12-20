package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.FolhaPagamentoRepository;
import com.jaasielsilva.portalceo.repository.ContaPagarRepository;
import com.jaasielsilva.portalceo.repository.AuditoriaRhLogRepository;
import com.jaasielsilva.portalceo.dto.financeiro.IntegracaoFolhaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class IntegracaoFinanceiraService {

    @Autowired
    private FolhaPagamentoRepository folhaPagamentoRepository;

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private AuditoriaRhLogRepository auditoriaRhLogRepository;

    @Transactional
    public IntegracaoFolhaDTO enviarFolhaParaFinanceiro(Long folhaId, Usuario usuarioResponsavel) throws Exception {
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new RuntimeException("Folha não encontrada"));

        if (folha.getStatus() != FolhaPagamento.StatusFolha.FECHADA) {
            throw new RuntimeException("A folha precisa estar FECHADA antes de enviar para o financeiro. Certifique-se de que todas as validações foram concluídas.");
        }

        // Validação de Valores (Tolerância Zero)
        validarTotais(folha);

        // Geração de Hash de Integridade
        String hash = gerarHashIntegridade(folha);
        folha.setHashIntegridade(hash);

        // Criação da Conta a Pagar (Previsão)
        ContaPagar conta = new ContaPagar();
        conta.setDescricao("Folha de Pagamento - " + folha.getMesReferencia() + "/" + folha.getAnoReferencia());
        conta.setValorOriginal(folha.getTotalLiquido());
        conta.setDataVencimento(folha.getDataProcessamento().plusDays(5)); // Exemplo: Vence em 5 dias
        conta.setDataEmissao(folha.getDataProcessamento());
        conta.setCategoria(ContaPagar.CategoriaContaPagar.SALARIOS);
        conta.setStatus(ContaPagar.StatusContaPagar.PENDENTE);
        conta.setFolhaPagamento(folha);
        conta.setObservacoes("Integrado via Sistema RH. Hash: " + hash);
        
        contaPagarRepository.save(conta);

        // Atualiza Status da Folha
        folha.setStatus(FolhaPagamento.StatusFolha.ENVIADA_FINANCEIRO);
        folhaPagamentoRepository.save(folha);

        // Auditoria
        registrarAuditoria(usuarioResponsavel, "INTEGRACAO_FINANCEIRA", "Folha " + folhaId + " enviada para financeiro.");

        // Retorna DTO
        IntegracaoFolhaDTO dto = new IntegracaoFolhaDTO();
        dto.setFolhaId(folha.getId());
        dto.setValorTotal(folha.getTotalLiquido());
        dto.setHashIntegridade(hash);
        dto.setDataVencimento(conta.getDataVencimento());
        dto.setUsuarioResponsavel(usuarioResponsavel.getEmail());
        dto.setTipoFolha(folha.getTipoFolha());

        return dto;
    }

    private void validarTotais(FolhaPagamento folha) {
        BigDecimal calculado = folha.getTotalBruto()
                .subtract(folha.getTotalDescontos());
        
        // Comparação com tolerância zero (usando compareTo)
        if (calculado.compareTo(folha.getTotalLiquido()) != 0) {
            throw new RuntimeException("Divergência de valores detectada! Processo abortado.");
        }
    }

    private String gerarHashIntegridade(FolhaPagamento folha) throws Exception {
        String data = folha.getId() + ":" + folha.getTotalLiquido() + ":" + folha.getMesReferencia() + "/" + folha.getAnoReferencia();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void registrarAuditoria(Usuario usuario, String acao, String detalhes) {
        AuditoriaRhLog log = new AuditoriaRhLog();
        log.setUsuario(usuario != null ? usuario.getEmail() : "SISTEMA");
        log.setAcao(acao);
        log.setCategoria("INTEGRACAO");
        log.setDetalhes(detalhes);
        log.setCriadoEm(LocalDateTime.now());
        auditoriaRhLogRepository.save(log);
    }
}
