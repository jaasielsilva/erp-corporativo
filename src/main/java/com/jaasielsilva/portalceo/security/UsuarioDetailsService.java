package com.jaasielsilva.portalceo.security;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.PermissaoRepository;
import com.jaasielsilva.portalceo.model.Permissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    private static final Logger log = LoggerFactory.getLogger(UsuarioDetailsService.class);

    private final com.github.benmanes.caffeine.cache.Cache<String, Set<SimpleGrantedAuthority>> authoritiesCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .maximumSize(10000)
                    .expireAfterWrite(java.time.Duration.ofMinutes(3))
                    .build();

    public void evictAuthorities(String email) {
        if (email != null) {
            authoritiesCache.invalidate(email);
        }
    }

    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    long t0 = System.nanoTime();
    // Tentar buscar por email primeiro (com perfis pré-carregados)
    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailWithPerfis(username);

    // Se não encontrar por email, tentar por matrícula
    if (usuarioOpt.isEmpty()) {
        usuarioOpt = usuarioRepository.findByMatriculaWithPerfis(username);
    }
    long tLookup = System.nanoTime();

    if (usuarioOpt.isEmpty()) {
        throw new UsernameNotFoundException("Usuário não encontrado com email ou matrícula: " + username);
    }

    Usuario usuario = usuarioOpt.get();

    // Validação do status do usuário com mensagens específicas
    switch (usuario.getStatus()) {
        case DEMITIDO -> throw new DisabledException("Usuário demitido não pode acessar o sistema.");
        case INATIVO -> throw new LockedException("Usuário inativo não pode fazer login.");
        case BLOQUEADO -> throw new LockedException("Usuário bloqueado não pode fazer login.");
    }

    Set<SimpleGrantedAuthority> authorities = authoritiesCache.getIfPresent(usuario.getEmail());
        if (authorities == null) {
            var built = new java.util.HashSet<SimpleGrantedAuthority>();
            if ((usuario.getId() != null && usuario.getId() == 1L) ||
                (usuario.getNivelAcesso() != null && usuario.getNivelAcesso().name().equalsIgnoreCase("MASTER"))) {
                built.add(new SimpleGrantedAuthority("ROLE_MASTER"));
                built.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                built.add(new SimpleGrantedAuthority("ROLE_USER"));
                built.add(new SimpleGrantedAuthority("CHAMADO_CRIAR"));
                built.add(new SimpleGrantedAuthority("CHAMADO_VISUALIZAR"));
                built.add(new SimpleGrantedAuthority("CHAMADO_ATRIBUIR"));
                built.add(new SimpleGrantedAuthority("CHAMADO_INICIAR"));
                built.add(new SimpleGrantedAuthority("CHAMADO_RESOLVER"));
                built.add(new SimpleGrantedAuthority("CHAMADO_FECHAR"));
                built.add(new SimpleGrantedAuthority("CHAMADO_REABRIR"));
                built.add(new SimpleGrantedAuthority("CHAMADO_AVALIAR"));
                built.add(new SimpleGrantedAuthority("TECNICO_ATENDER_CHAMADOS"));
                built.add(new SimpleGrantedAuthority("TECNICO_GERENCIAR_PROPRIOS_CHAMADOS"));
                built.add(new SimpleGrantedAuthority("ADMIN_GERENCIAR_USUARIOS"));
                built.add(new SimpleGrantedAuthority("FINANCEIRO_VER_SALDO"));
                built.add(new SimpleGrantedAuthority("FINANCEIRO_VER_EXTRATO"));
                built.add(new SimpleGrantedAuthority("FINANCEIRO_PAGAR"));
                built.add(new SimpleGrantedAuthority("FINANCEIRO_CONFIGURAR"));
                built.add(new SimpleGrantedAuthority("MENU_DASHBOARD"));
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES"));
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES_NOVO"));
                built.add(new SimpleGrantedAuthority("CLIENTE_EDITAR")); // Adicionado
                built.add(new SimpleGrantedAuthority("CLIENTE_EXCLUIR")); // Adicionado
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES_CONTRATOS_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES_HISTORICO_INTERACOES"));
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES_HISTORICO_PEDIDOS"));
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES_AVANCADO_BUSCA"));
                built.add(new SimpleGrantedAuthority("MENU_CLIENTES_AVANCADO_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_VENDAS"));
                built.add(new SimpleGrantedAuthority("MENU_VENDAS_DASHBOARD"));
                built.add(new SimpleGrantedAuthority("MENU_VENDAS_PDV"));
                built.add(new SimpleGrantedAuthority("MENU_VENDAS_PEDIDOS"));
                built.add(new SimpleGrantedAuthority("MENU_VENDAS_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_COMPRAS"));
                built.add(new SimpleGrantedAuthority("MENU_ESTOQUE"));
                built.add(new SimpleGrantedAuthority("MENU_ESTOQUE_PRODUTOS"));
                built.add(new SimpleGrantedAuthority("MENU_ESTOQUE_CATEGORIAS"));
                built.add(new SimpleGrantedAuthority("MENU_ESTOQUE_CATEGORIAS_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_ESTOQUE_CATEGORIAS_NOVO"));
                built.add(new SimpleGrantedAuthority("MENU_RH"));
                built.add(new SimpleGrantedAuthority("MENU_RH_COLABORADORES_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_RH_COLABORADORES_NOVO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_COLABORADORES_ADESAO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FOLHA"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FOLHA_INICIO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FOLHA_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FOLHA_GERAR"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FOLHA_HOLERITE"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FOLHA_DESCONTOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FOLHA_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_BENEFICIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_BENEFICIOS_PLANO_SAUDE"));
                built.add(new SimpleGrantedAuthority("MENU_RH_BENEFICIOS_VALE_TRANSPORTE"));
                built.add(new SimpleGrantedAuthority("MENU_RH_BENEFICIOS_VALE_REFEICAO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_BENEFICIOS_ADESAO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_WORKFLOW"));
                built.add(new SimpleGrantedAuthority("MENU_RH_WORKFLOW_APROVACAO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_WORKFLOW_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_PONTO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_PONTO_REGISTROS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_PONTO_CORRECOES"));
                built.add(new SimpleGrantedAuthority("MENU_RH_PONTO_ESCALAS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_PONTO_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FERIAS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FERIAS_SOLICITAR"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FERIAS_APROVAR"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FERIAS_PLANEJAMENTO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_FERIAS_CALENDARIO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AVALIACAO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AVALIACAO_PERIODICIDADE"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AVALIACAO_FEEDBACKS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AVALIACAO_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_TREINAMENTOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_TREINAMENTOS_CADASTRO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_TREINAMENTOS_INSCRICAO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_TREINAMENTOS_CERTIFICADO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO_CANDIDATOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO_VAGAS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO_TRIAGEM"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO_ENTREVISTAS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO_HISTORICO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO_PIPELINE"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RECRUTAMENTO_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RELATORIOS_TURNOVER"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RELATORIOS_ABSENTEISMO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RELATORIOS_HEADCOUNT"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RELATORIOS_INDICADORES"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RELATORIOS_ADMISSOES_DEMISSOES"));
                built.add(new SimpleGrantedAuthority("MENU_RH_RELATORIOS_FERIAS_BENEFICIOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_CONFIGURACOES"));
                built.add(new SimpleGrantedAuthority("MENU_RH_CONFIGURACOES_INICIO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_CONFIGURACOES_POLITICAS_FERIAS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_CONFIGURACOES_PONTO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_CONFIGURACOES_INTEGRACOES"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AUDITORIA"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AUDITORIA_INICIO"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AUDITORIA_ACESSOS"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AUDITORIA_ALTERACOES"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AUDITORIA_EXPORTACOES"));
                built.add(new SimpleGrantedAuthority("MENU_RH_AUDITORIA_REVISOES"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO_DASHBOARD"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO_CONTAS_BANCARIAS"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO_CONTAS_PAGAR"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO_CONTAS_RECEBER"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO_FLUXO_CAIXA"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO_TRANSFERENCIAS"));
                built.add(new SimpleGrantedAuthority("MENU_FINANCEIRO_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_MARKETING"));
                built.add(new SimpleGrantedAuthority("MENU_MARKETING_DASHBOARD"));
                built.add(new SimpleGrantedAuthority("MENU_MARKETING_CAMPANHAS"));
                built.add(new SimpleGrantedAuthority("MENU_MARKETING_LEADS"));
                built.add(new SimpleGrantedAuthority("MENU_MARKETING_EVENTOS"));
                built.add(new SimpleGrantedAuthority("MENU_MARKETING_MATERIAIS"));
                built.add(new SimpleGrantedAuthority("MENU_TI"));
                built.add(new SimpleGrantedAuthority("MENU_TI_DASHBOARD"));
                built.add(new SimpleGrantedAuthority("MENU_TI_SISTEMAS"));
                built.add(new SimpleGrantedAuthority("MENU_TI_SUPORTE"));
                built.add(new SimpleGrantedAuthority("MENU_TI_BACKUP"));
                built.add(new SimpleGrantedAuthority("MENU_TI_SEGURANCA"));
                built.add(new SimpleGrantedAuthority("MENU_TI_AUDITORIA"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_DASHBOARD"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_CLIENTES"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_PREVIDENCIARIO"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_PREVIDENCIARIO_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_PREVIDENCIARIO_NOVO"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_WHATCHAT"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_CONTRATOS"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_PROCESSOS"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_PROCESSOS_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_PROCESSOS_NOVO"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_COMPLIANCE"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_DOCUMENTOS"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_AUDITORIA"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_AUDITORIA_INICIO"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_AUDITORIA_ACESSOS"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_AUDITORIA_ALTERACOES"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_AUDITORIA_EXPORTACOES"));
                built.add(new SimpleGrantedAuthority("MENU_JURIDICO_AUDITORIA_REVISOES"));
                built.add(new SimpleGrantedAuthority("MENU_CADASTROS"));
                built.add(new SimpleGrantedAuthority("MENU_UTILIDADES"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_GERAL"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_GERAL_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_GERAL_NOVO"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_TAREFAS"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_TAREFAS_LISTAR"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_TAREFAS_ATRIBUICOES"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_EQUIPES_CADASTRAR"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_EQUIPES_MEMBROS"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_CRONOGRAMA"));
                built.add(new SimpleGrantedAuthority("MENU_PROJETOS_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_SERVICOS_SOLICITACOES_GERENCIAR"));
                built.add(new SimpleGrantedAuthority("MENU_SERVICOS_SOLICITACOES_DASHBOARD"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN_USUARIOS"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN_GESTAO_ACESSO"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN_GESTAO_ACESSO_PERFIS"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN_GESTAO_ACESSO_PERMISSOES"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN_RELATORIOS"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN_CONFIGURACOES"));
                built.add(new SimpleGrantedAuthority("MENU_ADMIN_METAS"));
                built.add(new SimpleGrantedAuthority("MENU_PESSOAL"));
                built.add(new SimpleGrantedAuthority("MENU_PESSOAL_MEUS_PEDIDOS"));
                built.add(new SimpleGrantedAuthority("MENU_PESSOAL_MEUS_SERVICOS"));
                built.add(new SimpleGrantedAuthority("MENU_PESSOAL_FAVORITOS"));
                built.add(new SimpleGrantedAuthority("MENU_PESSOAL_RECOMENDACOES"));
                built.add(new SimpleGrantedAuthority("MENU_AJUDA"));
                built.add(new SimpleGrantedAuthority("MENU_DOCS_GERENCIAL"));
                built.add(new SimpleGrantedAuthority("MENU_DOCS_PESSOAL"));
                try {
                    var allPerms = permissaoRepository.findAll();
                    for (Permissao p : allPerms) {
                        if (p != null && p.getNome() != null) {
                            built.add(new SimpleGrantedAuthority(p.getNome()));
                        }
                    }
                    built.add(new SimpleGrantedAuthority("DASHBOARD_EXECUTIVO_VISUALIZAR"));
                    built.add(new SimpleGrantedAuthority("DASHBOARD_OPERACIONAL_VISUALIZAR"));
                    built.add(new SimpleGrantedAuthority("DASHBOARD_FINANCEIRO_VISUALIZAR"));
                } catch (Exception ignore) {}
        } else {
            built.addAll(
                usuario.getPerfis().stream()
                        .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.getNome()))
                        .collect(Collectors.toSet())
            );
            built.addAll(
                usuario.getPerfis().stream()
                        .filter(p -> p.getPermissoes() != null)
                        .flatMap(p -> p.getPermissoes().stream())
                        .map(perm -> new SimpleGrantedAuthority(perm.getNome()))
                        .collect(Collectors.toSet())
            );
        }
        authoritiesCache.put(usuario.getEmail(), built);
        authorities = built;
    }

    long tAuthorities = System.nanoTime();
    if (log.isDebugEnabled()) {
        long lookupMs = (tLookup - t0) / 1_000_000;
        long authBuildMs = (tAuthorities - tLookup) / 1_000_000;
        log.debug("Login timing: lookup={}ms, authorities={}ms for username={}", lookupMs, authBuildMs, username);
    }

    return new org.springframework.security.core.userdetails.User(
            usuario.getEmail(),
            usuario.getSenha(),
            true,
            true,
            true,
            true,
            authorities
    );
}

}
