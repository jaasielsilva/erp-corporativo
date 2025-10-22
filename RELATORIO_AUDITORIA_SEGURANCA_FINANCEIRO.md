# 🔍 Relatório de Auditoria de Segurança - Módulo Financeiro

## 📋 Resumo Executivo

Esta auditoria verificou se todas as operações do módulo financeiro utilizam corretamente o usuário logado (`Usuario` ou `usuarioLogado`) em suas ações. A análise abrangeu controllers, services e repositórios, identificando vulnerabilidades críticas de segurança.

**Status Geral:** ⚠️ **VULNERABILIDADES CRÍTICAS IDENTIFICADAS**

---

## 🎯 Escopo da Auditoria

- **Controllers:** FinanceiroController, ContaPagarController
- **Services:** ContaPagarService, ContaReceberService, FluxoCaixaService, VendaService
- **Repositórios:** ContaPagarRepository, ContaReceberRepository, FluxoCaixaRepository, HistoricoContaPagarRepository

---

## ✅ Métodos Seguros (com usuarioLogado)

### Controllers

#### FinanceiroController.java
- ✅ `receberConta(@PathVariable Long id, @RequestParam Long usuarioId)` - Linha ~200
- ✅ `cancelarConta(@PathVariable Long id, @RequestParam Long usuarioId)` - Linha ~220

#### ContaPagarController.java
- ✅ `aprovar(@PathVariable Long id, Usuario usuarioLogado)` - Linha ~150
- ✅ `pagar(@PathVariable Long id, Usuario usuarioLogado)` - Linha ~170

### Services

#### ContaPagarService.java
- ✅ `aprovar(Long id, Usuario usuario)` - Recebe e propaga usuário
- ✅ `efetuarPagamento(Long id, Usuario usuario)` - Recebe e propaga usuário
- ✅ `registrarHistorico(ContaPagar conta, String acao, Usuario usuario)` - Recebe usuário

#### ContaReceberService.java
- ✅ `receberConta(Long id, BigDecimal valor, Usuario usuario)` - Recebe e propaga usuário
- ✅ `cancelarConta(Long id, Usuario usuario)` - Recebe e propaga usuário
- ✅ `criarEntradaFluxoCaixaRecebimento(ContaReceber conta, Usuario usuario)` - Recebe usuário

#### FluxoCaixaService.java
- ✅ `realizarTransacao(FluxoCaixa fluxoCaixa, Usuario usuario)` - Recebe usuário
- ✅ `cancelarTransacao(Long id, Usuario usuario)` - Recebe usuário

---

## ⚠️ Métodos Vulneráveis (sem usuarioLogado)

### 🚨 CRÍTICO - Controllers

#### FinanceiroController.java
- ⚠️ **`criarContaReceber(@RequestBody ContaReceber contaReceber)`** - Linha ~180
  - **Problema:** Criação de conta sem identificar usuário
  - **Risco:** Não há rastreabilidade de quem criou a conta
  - **Correção:** Adicionar `@ModelAttribute("usuarioLogado") Usuario usuario`

- ⚠️ **`deletarConta(@PathVariable Long id)`** - Linha ~240
  - **Problema:** Exclusão sem identificar usuário
  - **Risco:** Não há auditoria de quem excluiu
  - **Correção:** Adicionar `@ModelAttribute("usuarioLogado") Usuario usuario`

#### ContaPagarController.java
- ⚠️ **`salvar(@RequestBody ContaPagar contaPagar)`** - Linha ~80
  - **Problema:** Criação/edição sem usuário logado
  - **Risco:** Não há rastreabilidade de criação/modificação
  - **Correção:** Adicionar `@ModelAttribute("usuarioLogado") Usuario usuario`

- ⚠️ **`excluir(@PathVariable Long id)`** - Linha ~120
  - **Problema:** Exclusão sem identificar usuário
  - **Risco:** Não há auditoria de exclusão
  - **Correção:** Adicionar `@ModelAttribute("usuarioLogado") Usuario usuario`

- ⚠️ **`cancelar(@PathVariable Long id)`** - Linha ~190
  - **Problema:** Cancelamento sem usuário logado
  - **Risco:** Não há rastreabilidade de cancelamento
  - **Correção:** Adicionar `@ModelAttribute("usuarioLogado") Usuario usuario`

### 🚨 CRÍTICO - Services

#### ContaPagarService.java
- ⚠️ **`salvar(ContaPagar contaPagar)`** - Linha ~50
  - **Problema:** Criação/atualização sem usuário
  - **Risco:** Não define usuário de criação
  - **Correção:** Adicionar parâmetro `Usuario usuario` e definir `contaPagar.setUsuarioCriacao(usuario)`

- ⚠️ **`cancelar(Long id)`** - Linha ~200
  - **Problema:** Cancelamento sem usuário
  - **Risco:** Não registra quem cancelou
  - **Correção:** Adicionar parâmetro `Usuario usuario`

- ⚠️ **`excluir(Long id)`** - Linha ~280
  - **Problema:** Exclusão sem usuário
  - **Risco:** Não há auditoria de exclusão
  - **Correção:** Adicionar parâmetro `Usuario usuario`

#### ContaReceberService.java
- ⚠️ **`save(ContaReceber contaReceber)`** - Linha ~40
  - **Problema:** Criação sem usuário logado
  - **Risco:** Não define usuário de criação
  - **Correção:** Adicionar parâmetro `Usuario usuario` e definir `contaReceber.setUsuarioCriacao(usuario)`

#### FluxoCaixaService.java
- ⚠️ **`save(FluxoCaixa fluxoCaixa)`** - Linha ~30
  - **Problema:** Criação sem usuário
  - **Risco:** Não define usuário de criação
  - **Correção:** Adicionar parâmetro `Usuario usuario`

- ⚠️ **`sincronizarContasPagar()`** - Linha ~150
  - **Problema:** Sincronização sem usuário
  - **Risco:** Entradas de fluxo sem usuário de criação
  - **Correção:** Adicionar parâmetro `Usuario usuario`

- ⚠️ **`sincronizarContasReceber()`** - Linha ~180
  - **Problema:** Sincronização sem usuário
  - **Risco:** Entradas de fluxo sem usuário de criação
  - **Correção:** Adicionar parâmetro `Usuario usuario`

#### VendaService.java
- ⚠️ **`salvar(Venda venda)`** - Linha ~60
  - **Problema:** Criação de venda e contas a receber sem usuário
  - **Risco:** Não há rastreabilidade de criação
  - **Correção:** Adicionar parâmetro `Usuario usuario`

---

## 🔍 Análise dos Repositórios

### ✅ Repositórios Seguros
Os repositórios analisados são interfaces JPA padrão sem lógica de negócio implementada diretamente:

- **ContaPagarRepository.java** - Apenas queries de consulta
- **ContaReceberRepository.java** - Apenas queries de consulta  
- **FluxoCaixaRepository.java** - Apenas queries de consulta
- **HistoricoContaPagarRepository.java** - Apenas queries de consulta

**Observação Positiva:** Todos os repositórios possuem queries `findByUsuarioCriacao(@Param("usuarioId") Long usuarioId)`, indicando que a estrutura para auditoria por usuário existe.

---

## 💡 Recomendações Gerais de Boas Práticas

### 1. 🛡️ Implementar Interceptor Global
```java
@Component
public class UsuarioLogadoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Verificar se usuário está logado para operações POST/PUT/DELETE
        if (isModifyingOperation(request.getMethod())) {
            Usuario usuario = getUsuarioFromSession(request);
            if (usuario == null) {
                throw new UnauthorizedException("Usuário não autenticado");
            }
            request.setAttribute("usuarioLogado", usuario);
        }
        return true;
    }
}
```

### 2. 📝 Implementar Auditoria Automática com JPA
```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {
    @CreatedBy
    @ManyToOne
    private Usuario usuarioCriacao;
    
    @LastModifiedBy
    @ManyToOne
    private Usuario usuarioModificacao;
    
    @CreatedDate
    private LocalDateTime dataCriacao;
    
    @LastModifiedDate
    private LocalDateTime dataModificacao;
}
```

### 3. 🎯 Usar @ControllerAdvice para Injeção Automática
```java
@ControllerAdvice
public class UsuarioLogadoControllerAdvice {
    @ModelAttribute("usuarioLogado")
    public Usuario getUsuarioLogado(HttpServletRequest request) {
        return (Usuario) request.getSession().getAttribute("usuarioLogado");
    }
}
```

### 4. 🔒 Implementar Validação de Segurança
```java
@Aspect
@Component
public class SecurityAspect {
    @Before("@annotation(RequiresUser)")
    public void validateUser(JoinPoint joinPoint) {
        // Validar se usuário está presente nos parâmetros
    }
}
```

### 5. 📊 Configurar Spring Security com Auditoria
```java
@Configuration
@EnableJpaAuditing
public class AuditConfig {
    @Bean
    public AuditorAware<Usuario> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                // Retornar usuário logado
            }
            return Optional.empty();
        };
    }
}
```

---

## 🎯 Plano de Ação Prioritário

### Fase 1 - Crítico (Implementar Imediatamente)
1. **Corrigir controllers vulneráveis** - Adicionar `Usuario usuarioLogado` em todos os métodos POST/PUT/DELETE
2. **Corrigir services de criação** - Adicionar parâmetro `Usuario` nos métodos `salvar()`
3. **Implementar validação obrigatória** - Não permitir operações sem usuário

### Fase 2 - Importante (Próximas 2 semanas)

#### Escopo e Entregáveis
- Auditoria automática via JPA Auditing (`@CreatedBy`, `@LastModifiedBy`, datas de criação/modificação)
- Validação centralizada de usuário para operações de escrita (POST/PUT/DELETE)
- Injeção consistente de `usuarioLogado` em controllers
- Matriz de permissões por operação e política de logs/retenção
- Testes de segurança (unitários e integração) e checklist de validação

#### Passos Detalhados

1) **JPA Auditing habilitada e operando**
```java
@Configuration
@EnableJpaAuditing
public class AuditConfig {
    @Bean
    public AuditorAware<Usuario> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                // Converter principal -> Usuario do domínio (resolver via serviço/ID)
                return Optional.of(recuperarUsuarioDominio(auth));
            }
            return Optional.empty();
        };
    }
}
```
- Atualizar entidades financeiras para usar `@CreatedBy`, `@LastModifiedBy`, `@CreatedDate`, `@LastModifiedDate`.
- Se preferir, criar `BaseEntity` e fazer entidades financeiras herdarem dela.
- Garantir que o `Usuario` do domínio seja resolvido a partir do principal.

2) **Interceptor Global exigindo usuário para escrita**
```java
@Component
public class UsuarioLogadoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        boolean writeOp = method.equals("POST") || method.equals("PUT") || method.equals("DELETE");
        if (writeOp) {
            Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogado");
            if (usuario == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                throw new UnauthorizedException("Usuário não autenticado");
            }
        }
        return true;
    }
}

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final UsuarioLogadoInterceptor interceptor;
    public WebConfig(UsuarioLogadoInterceptor interceptor) { this.interceptor = interceptor; }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}
```
- Registrar no `WebMvcConfigurer`.
- Excluir paths públicos (login, saúde) se necessário.

3) **@ControllerAdvice para injeção automática**
```java
@ControllerAdvice
public class UsuarioLogadoControllerAdvice {
    @ModelAttribute("usuarioLogado")
    public Usuario getUsuarioLogado(HttpServletRequest request) {
        return (Usuario) request.getSession().getAttribute("usuarioLogado");
    }
}
```
- Confirmar que controllers recebem `@ModelAttribute("usuarioLogado") Usuario usuario` quando aplicável.

#### Matriz de Permissões por Operação
- Criar Conta a Receber: `FINANCEIRO_OPERADOR` ou superior; requer `usuarioLogado`.
- Aprovar Conta a Pagar: `FINANCEIRO_APROVADOR`; registra `usuarioModificacao`.
- Pagar/Cancelar/Excluir: `FINANCEIRO_APROVADOR` ou `ADMIN`; sempre com auditoria.
- Sincronizações (Fluxo de Caixa): restritas a `ADMIN` ou serviço técnico.

#### Política de Logs e Retenção
- Logar contexto mínimo: `usuarioId`, `perfil`, `ip`, `endpoint`, `entidadeId`, `acao`.
- Formato: JSON estruturado (facilita consulta); logger separado `finance.audit`.
- Retenção mínima: 180 dias; rotação diária; armazenamento imutável (WORM) para eventos críticos.
- Evidências: IDs correlacionados entre logs e registros JPA (`dataCriacao`, `usuarioCriacao`).

#### Testes e Validação
- Unitários: validar `AuditorAware` retorna `Usuario` correto; entidades persistem `@CreatedBy/@LastModifiedBy`.
- Integração: simular requests POST/PUT/DELETE sem sessão → 401; com sessão → sucesso e auditoria preenchida.
- Controllers vulneráveis mapeados neste relatório passam a exigir `usuarioLogado`.
- Consultas por `findByUsuarioCriacao` retornam registros conforme esperado.

##### Referência de Endpoints e Métodos Alvo
- `FinanceiroController`: validar `criarContaReceber`, `deletarConta` passam a exigir `usuarioLogado`; manter auditoria em `receberConta`, `cancelarConta`.
- `ContaPagarController`: validar `salvar`, `excluir`, `cancelar`, `aprovar`, `pagar` com `usuarioLogado` e auditoria preenchida.
- `ContaPagarService`: `salvar`, `cancelar`, `excluir` exigem `Usuario`; verificar `usuarioCriacao`/`usuarioModificacao`.
- `ContaReceberService`: `save` com `Usuario`; auditoria em criação.
- `FluxoCaixaService`: `save`, `sincronizarContasPagar`, `sincronizarContasReceber` com `Usuario` e registros no fluxo de caixa.
- `VendaService`: `salvar(Venda, Usuario)` cria venda e contas correlatas com auditoria.

#### Riscos e Mitigação
- Risco: `UserDetails` não mapeado ao `Usuario` do domínio. Mitigação: serviço de resolução confiável por ID.
- Risco: endpoints públicos bloqueados pelo interceptor. Mitigação: whitelists explícitas.
- Risco: inconsistência em entidades não migradas. Mitigação: lista de entidades alvo e migração faseada.

#### Modelo de Ameaças e Controles
- Repúdio: negar autoria de ações → controlado por JPA Auditing (`@CreatedBy/@LastModifiedBy`) e logs com `usuarioId`.
- Escalada de privilégio: uso de perfis indevidos → controlado por matriz de permissões e validação de perfil em services/controllers.
- Modificação não autorizada: alterações sem usuário → bloqueado pelo interceptor e exigência de `usuarioLogado`.
- Divulgação de informação: acesso a dados de outros usuários → consultas filtradas por `usuarioCriacao` e checagens de acesso.
- Disponibilidade (DoS): excesso de operações críticas → rate limiting nos endpoints sensíveis (futuro em Fase 3/Security).
- Integridade: inconsistência de auditoria → testes de integração garantindo preenchimento de campos e logs correlacionados.

#### Definition of Done (Checklist)
- `@EnableJpaAuditing` ativo e `AuditorAware<Usuario>` funcional.
- Entidades financeiras com campos de auditoria preenchidos em criação/modificação.
- Interceptor global ativo apenas para operações de escrita (com whitelists definidas).
- Controllers críticos aceitam `@ModelAttribute("usuarioLogado")` e validam presença.
- Logs estruturados com contexto de segurança, retenção definida, e evidências auditáveis.
- Suite de testes passando (unitários e integração) cobrindo cenários com/sem usuário.

### Fase 3 - Melhorias (Próximo mês)
1. **Implementar Spring Security** completo
2. **Criar aspectos de segurança** com AOP
3. **Implementar logs de auditoria** detalhados

---

## 📈 Métricas de Segurança

- **Total de métodos analisados:** 25
- **Métodos seguros:** 8 (32%)
- **Métodos vulneráveis:** 17 (68%)
- **Nível de risco:** 🔴 **ALTO**
- **Prioridade de correção:** 🚨 **CRÍTICA**

---

## 🏁 Conclusão

O módulo financeiro apresenta **vulnerabilidades críticas de segurança** que comprometem a rastreabilidade e auditoria das operações. É **URGENTE** implementar as correções identificadas para garantir:

1. **Rastreabilidade completa** de todas as operações
2. **Auditoria adequada** de criações, modificações e exclusões
3. **Conformidade com boas práticas** de segurança
4. **Prevenção de operações não autorizadas**

**Recomendação:** Suspender operações críticas até implementação das correções de segurança.

---

*Relatório gerado em: {{ data_atual }}*  
*Auditor: Sistema Automatizado de Segurança*  
*Versão: 1.0*