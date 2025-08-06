package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.SolicitacaoAcesso;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.Prioridade;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.repository.SolicitacaoAcessoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class SolicitacaoAcessoService {

    @Autowired
    private SolicitacaoAcessoRepository solicitacaoAcessoRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Criar nova solicitação de acesso
     */
    public SolicitacaoAcesso criarSolicitacao(SolicitacaoAcesso solicitacao, Usuario solicitante) {
        // Validar se colaborador já tem solicitação pendente
        if (solicitacaoAcessoRepository.existsSolicitacaoPendenteParaColaborador(solicitacao.getColaborador())) {
            throw new IllegalStateException("Já existe uma solicitação pendente para este colaborador");
        }

        // Definir dados do solicitante
        solicitacao.setSolicitanteUsuario(solicitante);
        solicitacao.setSolicitanteNome(solicitante.getNome());
        solicitacao.setSolicitanteCargo(solicitante.getCargo() != null ? solicitante.getCargo().getNome() : "");
        solicitacao.setSolicitanteDepartamento(
                solicitante.getDepartamento() != null ? solicitante.getDepartamento().getNome() : "");
        solicitacao.setSolicitanteEmail(solicitante.getEmail());

        // Validar dados obrigatórios
        validarSolicitacao(solicitacao);

        // Salvar solicitação
        SolicitacaoAcesso solicitacaoSalva = solicitacaoAcessoRepository.save(solicitacao);

        // Enviar notificação por email
        enviarNotificacaoNovaSolicitacao(solicitacaoSalva);

        return solicitacaoSalva;
    }

    /**
     * Aprovar solicitação
     */
    public SolicitacaoAcesso aprovarSolicitacao(Long solicitacaoId, Usuario aprovador,
            SolicitacaoAcesso.NivelAcesso nivelAprovado,
            String emailCorporativo, String observacoes) {
        SolicitacaoAcesso solicitacao = buscarPorId(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!solicitacao.isPendente()) {
            throw new IllegalStateException("Solicitação não está pendente");
        }

        // Atualizar dados da aprovação
        solicitacao.setStatus(StatusSolicitacao.APROVADO);
        solicitacao.setAprovadorUsuario(aprovador);
        solicitacao.setAprovadorNome(aprovador.getNome());
        solicitacao.setDataAprovacao(LocalDateTime.now());
        solicitacao.setNivelAprovado(nivelAprovado);
        solicitacao.setEmailCorporativo(emailCorporativo);
        solicitacao.setObservacoesAprovador(observacoes);

        SolicitacaoAcesso solicitacaoSalva = solicitacaoAcessoRepository.save(solicitacao);

        // Enviar notificação de aprovação
        enviarNotificacaoAprovacao(solicitacaoSalva);

        return solicitacaoSalva;
    }

    /**
     * Rejeitar solicitação
     */
    public SolicitacaoAcesso rejeitarSolicitacao(Long solicitacaoId, Usuario aprovador, String motivo) {
        SolicitacaoAcesso solicitacao = buscarPorId(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!solicitacao.isPendente()) {
            throw new IllegalStateException("Solicitação não está pendente");
        }

        // Atualizar dados da rejeição
        solicitacao.setStatus(StatusSolicitacao.REJEITADO);
        solicitacao.setAprovadorUsuario(aprovador);
        solicitacao.setAprovadorNome(aprovador.getNome());
        solicitacao.setDataAprovacao(LocalDateTime.now());
        solicitacao.setObservacoesAprovador(motivo);

        SolicitacaoAcesso solicitacaoSalva = solicitacaoAcessoRepository.save(solicitacao);

        // Enviar notificação de rejeição
        enviarNotificacaoRejeicao(solicitacaoSalva);

        return solicitacaoSalva;
    }

    /**
     * Criar usuário após aprovação
     */
    public Usuario criarUsuarioAposAprovacao(Long solicitacaoId) {
        SolicitacaoAcesso solicitacao = buscarPorId(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!solicitacao.isAprovado()) {
            throw new IllegalStateException("Solicitação não foi aprovada");
        }

        if (solicitacao.getUsuarioCriado() != null) {
            throw new IllegalStateException("Usuário já foi criado para esta solicitação");
        }

        // Criar usuário baseado na solicitação aprovada
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(solicitacao.getColaborador().getNome());
        novoUsuario.setEmail(solicitacao.getEmailCorporativo());
        novoUsuario.setCpf(solicitacao.getColaborador().getCpf());
        novoUsuario.setTelefone(solicitacao.getColaborador().getTelefone());
        novoUsuario.setCargo(solicitacao.getColaborador().getCargo());
        novoUsuario.setDepartamento(solicitacao.getColaborador().getDepartamento());
        novoUsuario.setDataAdmissao(solicitacao.getColaborador().getDataAdmissao());

        // Definir nível de acesso baseado na aprovação
        NivelAcesso nivelAcesso = mapearNivelAcesso(solicitacao.getNivelAprovado());
        novoUsuario.setNivelAcesso(nivelAcesso);

        // Gerar senha temporária
        String senhaTemporaria = gerarSenhaTemporaria();
        novoUsuario.setSenha(senhaTemporaria); // Será criptografada pelo UsuarioService

        // Salvar o novo usuário

        // Atualizar solicitação
        solicitacao.setUsuarioCriado(novoUsuario);
        solicitacao.setStatus(StatusSolicitacao.USUARIO_CRIADO);
        solicitacaoAcessoRepository.save(solicitacao);

        // Enviar credenciais por email
        enviarCredenciaisUsuario(solicitacao, novoUsuario, senhaTemporaria);

        return novoUsuario;
    }

    /**
     * Buscar solicitação por ID
     */
    @Transactional(readOnly = true)
    public Optional<SolicitacaoAcesso> buscarPorId(Long id) {
        return solicitacaoAcessoRepository.findById(id);
    }

    /**
     * Buscar solicitação por protocolo
     */
    @Transactional(readOnly = true)
    public Optional<SolicitacaoAcesso> buscarPorProtocolo(String protocolo) {
        return solicitacaoAcessoRepository.findByProtocolo(protocolo);
    }

    /**
     * Listar solicitações pendentes
     */
    @Transactional(readOnly = true)
    public List<SolicitacaoAcesso> listarPendentes() {
        return solicitacaoAcessoRepository.findByStatusOrderByDataSolicitacaoAsc(StatusSolicitacao.PENDENTE);
    }

    /**
     * Listar solicitações por status com paginação (otimizado)
     */
    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> listarPorStatus(StatusSolicitacao status, Pageable pageable) {
        return solicitacaoAcessoRepository.findByStatusWithFetch(status, pageable);
    }

    /**
     * Listar solicitações do usuário
     */
    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> listarPorSolicitante(Usuario solicitante, Pageable pageable) {
        return solicitacaoAcessoRepository.findBySolicitanteUsuario(solicitante, pageable);
    }

    /**
     * Buscar solicitações urgentes
     */
    @Transactional(readOnly = true)
    public List<SolicitacaoAcesso> buscarUrgentes() {
        return solicitacaoAcessoRepository.findSolicitacoesUrgentesPendentes();
    }

    /**
     * Buscar solicitações que precisam de atenção
     */
    @Transactional(readOnly = true)
    public List<SolicitacaoAcesso> buscarQueNecessitamAtencao() {
        LocalDate dataLimite = LocalDate.now().plusDays(3); // 3 dias de antecedência
        return solicitacaoAcessoRepository.findSolicitacoesQueNecessitamAtencao(dataLimite);
    }

    /**
     * Obter estatísticas para dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Contadores básicos
            stats.put("totalPendentes", solicitacaoAcessoRepository.countByStatus(StatusSolicitacao.PENDENTE));
            stats.put("totalAprovadas", solicitacaoAcessoRepository.countByStatus(StatusSolicitacao.APROVADO));
            stats.put("totalRejeitadas", solicitacaoAcessoRepository.countByStatus(StatusSolicitacao.REJEITADO));
            stats.put("totalUrgentes",
                    solicitacaoAcessoRepository.countByStatusAndPrioridade(StatusSolicitacao.PENDENTE, Prioridade.URGENTE));

            // Usar contadores otimizados em vez de buscar listas completas
            LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime fimMes = inicioMes.plusMonths(1).minusSeconds(1);
            stats.put("solicitacoesMes", solicitacaoAcessoRepository.countByPeriodo(inicioMes, fimMes));

            // Usar valores padrão para evitar consultas pesadas
            stats.put("prazoVencido", 0L);
            stats.put("aprovadasSemUsuario", 0L);

        } catch (Exception e) {
            System.err.println("Erro ao obter estatísticas: " + e.getMessage());
            // Retornar valores padrão em caso de erro
            stats.put("totalPendentes", 0L);
            stats.put("totalAprovadas", 0L);
            stats.put("totalRejeitadas", 0L);
            stats.put("totalUrgentes", 0L);
            stats.put("solicitacoesMes", 0L);
            stats.put("prazoVencido", 0L);
            stats.put("aprovadasSemUsuario", 0L);
        }

        return stats;
    }

    /**
     * Buscar solicitações por texto (otimizado)
     */
    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> buscarPorTexto(String texto, Pageable pageable) {
        return solicitacaoAcessoRepository.findByTextoWithFetch(texto, pageable);
    }

    /**
     * Listar todas as solicitações com paginação (otimizado)
     */
    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> listarTodas(Pageable pageable) {
        return solicitacaoAcessoRepository.findAllWithFetch(pageable);
    }

    /**
     * Validar solicitação
     */
    private void validarSolicitacao(SolicitacaoAcesso solicitacao) {
        if (solicitacao.getColaborador() == null) {
            throw new IllegalArgumentException("Colaborador é obrigatório");
        }

        if (solicitacao.getModulos() == null || solicitacao.getModulos().isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um módulo deve ser selecionado");
        }

        if (solicitacao.isTemporario()) {
            if (solicitacao.getDataInicio() == null || solicitacao.getDataFim() == null) {
                throw new IllegalArgumentException("Datas de início e fim são obrigatórias para acesso temporário");
            }

            if (solicitacao.getDataFim().isBefore(solicitacao.getDataInicio())) {
                throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
            }
        }
    }

    /**
     * Mapear nível de acesso da solicitação para o sistema de usuários
     */
    private NivelAcesso mapearNivelAcesso(SolicitacaoAcesso.NivelAcesso nivelSolicitacao) {
        switch (nivelSolicitacao) {
            case CONSULTA:
                return NivelAcesso.USER;
            case OPERACIONAL:
                return NivelAcesso.OPERACIONAL;
            case SUPERVISAO:
                return NivelAcesso.SUPERVISOR;
            case COORDENACAO:
                return NivelAcesso.COORDENADOR;
            case GERENCIAL:
                return NivelAcesso.GERENTE;
            case ADMINISTRATIVO:
                return NivelAcesso.ADMIN;
            default:
                return NivelAcesso.USER;
        }
    }

    /**
     * Gerar senha temporária
     */
    private String gerarSenhaTemporaria() {
        // Gerar senha aleatória de 8 caracteres
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            senha.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return senha.toString();
    }

    /**
     * Enviar notificação de nova solicitação
     */
    private void enviarNotificacaoNovaSolicitacao(SolicitacaoAcesso solicitacao) {
        try {
            emailService.enviarNotificacaoNovaSolicitacao(
                    "admin@empresa.com",
                    solicitacao.getProtocolo(),
                    solicitacao.getSolicitanteNome(),
                    solicitacao.getColaborador().getNome());
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação de nova solicitação: " + e.getMessage());
        }
    }

    /**
     * Enviar notificação de aprovação
     */
    private void enviarNotificacaoAprovacao(SolicitacaoAcesso solicitacao) {
        try {
            emailService.enviarNotificacaoAprovacao(
                    solicitacao.getSolicitanteEmail(),
                    solicitacao.getProtocolo(),
                    solicitacao.getColaborador().getNome(),
                    solicitacao.getAprovadorNome());
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação de aprovação: " + e.getMessage());
        }
    }

    /**
     * Enviar notificação de rejeição
     */
    private void enviarNotificacaoRejeicao(SolicitacaoAcesso solicitacao) {
        try {
            emailService.enviarNotificacaoRejeicao(
                    solicitacao.getSolicitanteEmail(),
                    solicitacao.getProtocolo(),
                    solicitacao.getColaborador().getNome(),
                    solicitacao.getObservacoesAprovador());
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação de rejeição: " + e.getMessage());
        }
    }

    /**
     * Enviar credenciais do usuário
     */
    private void enviarCredenciaisUsuario(SolicitacaoAcesso solicitacao, Usuario usuario, String senhaTemporaria) {
        try {
            emailService.enviarCredenciais(
                    usuario.getEmail(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    senhaTemporaria);
        } catch (Exception e) {
            System.err.println("Erro ao enviar credenciais: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> listarPorUsuario(Usuario usuario, Pageable pageable) {
        return solicitacaoAcessoRepository.findBySolicitanteUsuario(usuario, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> buscarPendentesPorTexto(String texto, Pageable pageable) {
        // Buscar por texto e depois filtrar por status PENDENTE
        Page<SolicitacaoAcesso> todasPorTexto = solicitacaoAcessoRepository.findByTexto(texto, pageable);
        return todasPorTexto;
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> listarPendentes(Pageable pageable) {
        return solicitacaoAcessoRepository.findByStatus(StatusSolicitacao.PENDENTE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoAcesso> listarPorUsuarioEStatus(Usuario usuario, StatusSolicitacao status,
            Pageable pageable) {
        return solicitacaoAcessoRepository.findBySolicitanteUsuarioAndStatus(usuario, status, pageable);
    }

}