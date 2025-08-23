# Painel do CEO - ERP Corporativo

Sistema ERP completo para gestão empresarial desenvolvido com **Spring Boot**, **Thymeleaf**, **MySQL** e **HTML/CSS puro**.  
> Ideal para controle administrativo de pequenas e médias empresas, com foco em **modularidade, usabilidade e escalabilidade**.

---
## Interface Administrativa

O sistema possui uma interface web administrativa moderna, com estrutura modular e responsiva.  
A navegação principal é feita por uma sidebar lateral que permanece consistente em todas as páginas, facilitando o acesso rápido aos principais módulos.

## Tecnologias e Arquitetura

- **Backend:** Spring Boot (Java 17+), Spring Security, Spring Data JPA  
- **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript (com potencial para frameworks futuros)  
- **Banco de Dados:** MySQL  
- **Gerenciamento de Dependências:** Maven ou Gradle  
- **Controle de Versão:** Git  

## Funcionalidades de E-mail (Passo a passo para iniciantes)

O sistema possui suporte ao envio automático de e-mails, como por exemplo para resetar a senha de um usuário.

### Configuração SMTP (no application.properties)

No arquivo localizado em `src/main/resources/application.properties`, adicione a configuração do seu servidor SMTP:

```properties
spring.mail.host=smtp.seuservidor.com            # Endereço do servidor SMTP (ex: smtp.gmail.com)
spring.mail.port=587                             # Porta (geralmente 587 para TLS)
spring.mail.username=seu-email@dominio.com      # E-mail que enviará as mensagens
spring.mail.password=sua-senha                   # Senha do e-mail (ou senha de app)
spring.mail.properties.mail.smtp.auth=true      # Habilita autenticação SMTP
spring.mail.properties.mail.smtp.starttls.enable=true  # Habilita criptografia TLS
```

**Dica para Gmail:**  
Se for utilizar o Gmail, é necessário ativar a verificação em duas etapas e gerar uma senha de aplicativo no painel de segurança do Google para usar no lugar da senha normal.

## Modularidade e Escalabilidade

O sistema é dividido em módulos independentes que facilitam:

- Manutenção e evolução de funcionalidades  
- Adição de novos módulos sem impacto no núcleo  
- Testes unitários e integração focados por módulo  

## Segurança

- Controle de acesso baseado em perfis e permissões

## Organização dos Templates Thymeleaf

Componentes reutilizáveis (header, sidebar, footer) para manter consistência visual e facilitar atualizações globais.

## Estrutura dos Templates Thymeleaf

```

templates/
├── components/
│   ├── header.html
│   ├── sidebar.html
│   └── footer.html
│
├── dashboard/
│   ├── index.html
│   ├── notificacoes.html
│   ├── estatisticas.html
│   └── alertas.html
│
├── usuarios/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── detalhes.html
│   ├── permissoes.html
│   └── resetar-senha.html
│
├── permissoes/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   └── atribuir.html
│
├── clientes/
│   ├── geral/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   │   └── detalhes.html
│   ├── contratos/
│   │   ├── listar.html
│   │   └── detalhes.html
│   ├── historico/
│   │   ├── interacoes.html
│   │   └── pedidos.html
│   └── avancado/
│       ├── busca.html
│       └── relatorios.html
│
├── fornecedores/
│   ├── geral/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   ├── contratos/
│   │   ├── listar.html
│   │   └── detalhes.html
│   ├── avaliacoes/
│   │   ├── listar.html
│   │   └── nova.html
│   └── pagamentos/
│       ├── listar.html
│       └── historico.html
│
├── produtos/
│   ├── geral/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   │   └── detalhes.html
│   ├── categorias/
│   │   ├── listar.html
│   │   └── nova.html
│   ├── precificacao/
│   │   ├── definir.html
│   │   └── simulador.html
│   ├── integracoes/
│   │   ├── importacao.html
│   │   └── exportacao.html
│   └── fornecedores/
│       └── vincular.html
│
├── estoque/
│   ├── inventario/
│   │   ├── listar.html
│   │   └── gerar.html
│   ├── movimentacao/
│   │   ├── entrada.html
│   │   ├── saida.html
│   │   └── ajustes.html
│   ├── transferencias/
│   │   ├── nova.html
│   │   ├── listar.html
│   │   └── historico.html
│   ├── alertas/
│   │   ├── baixa.html
│   │   └── vencimento.html
│   ├── auditoria/
│   │   └── registros.html
│   └── relatorios/
│       └── gerais.html
│
├── vendas/
│   ├── pedidos/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   │   └── detalhes.html
│   ├── relatorios/
│   │   ├── mensal.html
│   │   ├── anual.html
│   ├── comissoes/
│   │   ├── vendedores.html
│   │   └── calculo.html
│   ├── devolucoes/
│   │   ├── listar.html
│   │   └── registrar.html
│   ├── dashboards/
│   │   └── indicadores.html
│   └── faturamento/
│       └── listar.html
│
├── compras/
│   ├── pedidos/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   ├── fornecedores/
│   │   └── vincular.html
│   ├── historico/
│   │   └── movimentacoes.html
│   ├── aprovacoes/
│   │   └── pendentes.html
│   └── relatorios/
│       └── geral.html
│
├── financeiro/
│   ├── dashboard/
│   │   └── index.html
│   ├── contas-pagar/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   │   ├── agendamentos.html
│   │   ├── comprovantes.html
│   │   └── categorias.html
│   ├── contas-receber/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   │   ├── boletos.html
│   │   ├── notificacoes.html
│   │   └── categorias.html
│   ├── fluxo-caixa/
│   │   ├── visualizar.html
│   │   ├── entradas.html
│   │   ├── saidas.html
│   │   └── relatorio.html
│   ├── balanco/
│   │   ├── patrimonial.html
│   │   ├── dre.html
│   │   └── consolidado.html
│   ├── orcamentos/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   │   └── acompanhamento.html
│   ├── despesas/
│   │   ├── listar.html
│   │   ├── nova.html
│   │   ├── editar.html
│   │   ├── fixas.html
│   │   ├── variaveis.html
│   │   └── categorias.html
│   ├── receitas/
│   │   ├── listar.html
│   │   ├── nova.html
│   │   ├── editar.html
│   │   └── recorrentes.html
│   ├── pagamentos/
│   │   ├── listar.html
│   │   ├── efetuar.html
│   │   ├── comprovante.html
│   │   └── historico.html
│   ├── conciliacao/
│   │   ├── bancarias.html
│   │   ├── cartao.html
│   │   └── relatorio.html
│   └── relatorios/
│       ├── gerais.html
│       ├── por-periodo.html
│       ├── por-categoria.html
│       └── personalizados.html
│
├── rh/
│   ├── colaboradores/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   │   ├── ficha.html
│   │   ├── documentos.html
│   │   └── historico.html
│   ├── folha-pagamento/
│   │   ├── gerar.html
│   │   ├── holerite.html
│   │   ├── descontos.html
│   │   └── relatorios.html
│   ├── beneficios/
│   │   ├── plano-saude.html
│   │   ├── vale-transporte.html
│   │   ├── vale-refeicao.html
│   │   └── adesao.html
│   ├── ponto-escalas/
│   │   ├── registros.html
│   │   ├── correcoes.html
│   │   ├── escalas.html
│   │   └── relatorios.html
│   ├── ferias/
│   │   ├── solicitar.html
│   │   ├── aprovar.html
│   │   ├── planejamento.html
│   │   └── calendario.html
│   ├── avaliacao/
│   │   ├── periodicidade.html
│   │   ├── feedbacks.html
│   │   └── relatorios.html
│   ├── treinamentos/
│   │   ├── cadastro.html
│   │   ├── inscricao.html
│   │   └── certificado.html
│   ├── recrutamento/
│   │   ├── vagas.html
│   │   ├── triagem.html
│   │   ├── entrevistas.html
│   │   └── historico.html
│   └── relatorios/
│       ├── turnover.html
│       ├── absenteismo.html
│       ├── headcount.html
│       └── indicadores.html
│
├── transferencias/
│   ├── listar.html
│   ├── novo.html
│   └── historico.html
│
├── servicos/
│   ├── geral/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   └── editar.html
│   ├── contratos/
│   │   └── listar.html
│   └── faturamento/
│       └── listar.html
│
├── agenda/
│   ├── calendario.html
│   ├── eventos.html
│   ├── novo-evento.html
│   ├── lembretes.html
│   └── configuracoes.html
│
├── projetos/
│   ├── geral/
│   │   ├── listar.html
│   │   ├── novo.html
│   │   ├── editar.html
│   ├── tarefas/
│   │   ├── listar.html
│   │   └── atribuicoes.html
│   ├── equipes/
│   │   └── membros.html
│   ├── cronograma/
│   │   └── visualizar.html
│   └── relatorios/
│       └── desempenho.html
│
├── relatorios/
│   ├── vendas.html
│   ├── financeiro.html
│   ├── estoque.html
│   ├── rh.html
│   ├── clientes.html
│   └── customizados.html
│
├── perfil/
│   ├── editar.html
│   ├── senha.html
│   └── configuracoes.html
│
├── configuracoes/
│   ├── geral.html
│   ├── usuario.html
│   ├── notificacoes.html
│   ├── seguranca.html
│   └── backup.html
│
├── contato/
│   ├── formulario.html
│   └── historico.html
│
└── login/
    ├── login.html
    └── recuperar-senha.html
```

# 📚 DOCUMENTAÇÃO: NÍVEIS DE ACESSO E CONTROLE DE VISIBILIDADE

> Baseado na estrutura da sua `sidebar.html` com Thymeleaf

## 🎯 Objetivo

Este documento define:
- Os **perfis de usuário**
- As **permissões de acesso**
- Como o sistema **exibe ou oculta funcionalidades** com base no nível do usuário

O controle é feito via **variáveis Thymeleaf** injetadas no `Model` pelo backend, com base no perfil do usuário logado.

---

## 🔐 Perfis de Usuário (Roles)

| Perfil | Role Spring Security | Descrição |
|-------|------------------------|---------|
| **Master** | `ROLE_MASTER` | Máximo controle. Configurações globais, segurança, backup |
| **Admin** | `ROLE_ADMIN` | Gestão de usuários, permissões, configurações |
| **Gerencial** | `ROLE_GERENCIAL` | Acesso a relatórios, metas, RH, financeiro (visão geral) |
| **Vendas** | `ROLE_VENDAS` | Acesso ao módulo Comercial (clientes, vendas, contratos) |
| **Estoque** | `ROLE_ESTOQUE` | Controle de produtos, estoque, categorias |
| **Financeiro** | `ROLE_FINANCEIRO` | Acesso ao módulo Financeiro |
| **RH** | `ROLE_RH` | Acesso completo ao módulo Recursos Humanos |
| **TI** | `ROLE_TI` | Gestão de sistemas, suporte, segurança da informação |
| **Marketing** | `ROLE_MARKETING` | Campanhas, leads, eventos |
| **Jurídico** | `ROLE_JURIDICO` | Contratos, processos, compliance |
| **Usuário Comum** | `ROLE_USUARIO` | Acesso pessoal: chat, pedidos, serviços, favoritos |

---

## 🧩 Variáveis de Controle (Thymeleaf)

Essas variáveis são **injetadas no `Model`** no backend (nos controllers) com base no perfil do usuário.

| Variável Thymeleaf | Quando é `true` | Função |
|--------------------|------------------|--------|
| `isAdmin` | `ROLE_ADMIN` ou `ROLE_MASTER` | Libera configurações, gestão de acesso |
| `isMaster` | `ROLE_MASTER` | Acesso total, incluindo segurança e backup |
| `isGerencial` | `ROLE_GERENCIAL`, `ADMIN`, `MASTER` | Mostra relatórios, metas, benefícios |
| `podeAcessarVendas` | `ROLE_VENDAS`, `GERENCIAL`, `ADMIN`, `MASTER` | Mostra Clientes, Vendas, Contratos |
| `podeGerenciarVendas` | `ROLE_VENDAS`, `ADMIN`, `MASTER` | Permite criar/editar clientes e vendas |
| `podeAcessarEstoque` | `ROLE_ESTOQUE`, `GERENCIAL`, `ADMIN`, `MASTER` | Mostra Produtos, Estoque, Categorias |
| `podeAcessarFinanceiro` | `ROLE_FINANCEIRO`, `GERENCIAL`, `ADMIN`, `MASTER` | Mostra Contas, Fluxo de Caixa, Transferências |
| `podeAcessarRH` | `ROLE_RH`, `GERENCIAL`, `ADMIN`, `MASTER` | Mostra RH (exceto folha e recrutamento) |
| `podeGerenciarRH` | `ROLE_RH`, `ADMIN`, `MASTER` | Libera Folha, Recrutamento, Relatórios RH |
| `podeAcessarTI` | `ROLE_TI`, `ADMIN`, `MASTER` | Mostra Sistemas, Suporte, Segurança |
| `podeAcessarMarketing` | `ROLE_MARKETING`, `GERENCIAL`, `ADMIN`, `MASTER` | Mostra Campanhas, Leads |
| `podeAcessarJuridico` | `ROLE_JURIDICO`, `ADMIN`, `MASTER` | Mostra Contratos, Processos |
| `podeGerenciarUsuarios` | `ROLE_ADMIN`, `ROLE_MASTER` | Libera gestão de usuários e solicitações |
| `nivelAcesso` | `String` | Exibe o nome do perfil (ex: "Gerencial") |

---

## 👤 Perfil por Perfil: O Que Cada Usuário Vê

### 1. **Master** (`ROLE_MASTER`)
✅ **Tudo liberado**

- Todos os módulos: Vendas, Estoque, Financeiro, RH, TI, Jurídico, Marketing
- Gestão de usuários e permissões
- Configurações do sistema
- Backup e segurança
- Relatórios completos
- Metas e indicadores
- Pode aprovar férias, corrigir ponto, gerar folha

> 🔐 *Acesso total. Ideal para fundadores ou CIOs.*

---

### 2. **Admin** (`ROLE_ADMIN`)
✅ **Quase tudo liberado, exceto backup e segurança (opcional)**

- Todos os módulos de gestão
- Pode gerenciar usuários, perfis, permissões
- Pode configurar o sistema
- Pode gerar relatórios e metas
- Pode gerenciar RH (folha, recrutamento)
- Não pode fazer backup (se restringido)

> 🛠️ *Ideal para gestores de TI ou RH que precisam administrar o sistema.*

---

### 3. **Gerencial** (`ROLE_GERENCIAL`)
✅ **Visão geral do negócio**

- Dashboard completo
- Relatórios (vendas, financeiro, RH, estoque)
- Metas e indicadores
- Benefícios, férias (aprovar), ponto (corrigir)
- Acesso a Clientes, Vendas, Estoque, Financeiro (leitura)
- Pode ver documentos gerenciais

❌ **Não pode:**
- Gerenciar usuários
- Alterar configurações do sistema
- Fazer backup

> 📊 *Ideal para gerentes de área que precisam de visão estratégica.*

---

### 4. **Vendas** (`ROLE_VENDAS`)
✅ **Módulo Comercial completo**

- Clientes (listar, novo, editar)
- Vendas, contratos, pedidos
- Histórico de interações
- Busca avançada e relatórios de clientes

❌ **Não pode:**
- Acessar RH, Financeiro, Estoque
- Ver configurações ou relatórios gerais

> 💼 *Ideal para vendedores e supervisores comerciais.*

---

### 5. **Estoque** (`ROLE_ESTOQUE`)
✅ **Controle total de produtos e estoque**

- Produtos (CRUD)
- Movimentações de entrada/saída
- Inventário e ajustes
- Categorias
- Alertas de baixa

❌ **Não pode:**
- Acessar vendas, financeiro, RH
- Criar clientes ou fornecedores

> 📦 *Ideal para almoxarifados e logística.*

---

### 6. **Financeiro** (`ROLE_FINANCEIRO`)
✅ **Módulo Financeiro completo**

- Contas a pagar/receber
- Fluxo de caixa
- Transferências
- Conciliação bancária
- Orçamentos

❌ **Não pode:**
- Acessar RH, vendas, estoque (exceto leitura se necessário)
- Gerenciar usuários

> 💰 *Ideal para contadores e financeiros.*

---

### 7. **RH** (`ROLE_RH`)
✅ **Acesso completo ao módulo RH**

- Colaboradores (CRUD)
- Folha de pagamento (gerar, holerite)
- Ponto, férias, benefícios
- Treinamentos, recrutamento
- Relatórios (turnover, absenteísmo)

❌ **Não pode:**
- Acessar financeiro (exceto folha)
- Alterar configurações do sistema

> 👔 *Ideal para departamentos de pessoas.*

---

### 8. **TI** (`ROLE_TI`)
✅ **Gestão de tecnologia**

- Sistemas, suporte, backup
- Segurança da informação
- Gestão de usuários (suporte)

❌ **Não pode:**
- Acessar financeiro, RH, vendas
- Ver dados sensíveis

> 💻 *Ideal para equipe de TI.*

---

### 9. **Marketing** (`ROLE_MARKETING`)
✅ **Campanhas e leads**

- Campanhas publicitárias
- Gestão de leads
- Eventos e materiais

❌ **Não pode:**
- Acessar financeiro, RH, estoque

> 📣 *Ideal para equipe de marketing.*

---

### 10. **Jurídico** (`ROLE_JURIDICO`)
✅ **Contratos e processos**

- Contratos com clientes/fornecedores
- Processos judiciais
- Compliance
- Documentos legais

❌ **Não pode:**
- Acessar financeiro, RH, vendas

> ⚖️ *Ideal para advogados e jurídicos.*

---

### 11. **Usuário Comum** (`ROLE_USUARIO`)
✅ **Acesso pessoal**

- Chat interno
- Meus pedidos e serviços
- Favoritos e recomendações
- Documentos pessoais
- Central de ajuda

❌ **Não pode:**
- Acessar módulos administrativos
- Ver relatórios ou configurações

> 🧑‍💻 *Ideal para colaboradores que usam o sistema para solicitações e comunicação.*

---

## 🧠 Como o Backend Injeta as Variáveis

No seu controller, use `@ModelAttribute` para injetar as variáveis com base no usuário logado:

```java
@ModelAttribute
public void addAttributes(Model model, Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("nivelAcesso", usuario.getRole().getDisplayName());

        boolean isAdmin = usuario.hasRole("ADMIN");
        boolean isMaster = usuario.hasRole("MASTER");
        boolean isGerencial = usuario.hasRole("GERENCIAL") || isAdmin || isMaster;

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isMaster", isMaster);
        model.addAttribute("isGerencial", isGerencial);
        model.addAttribute("podeAcessarVendas", usuario.podeAcessarVendas());
        model.addAttribute("podeGerenciarVendas", usuario.podeGerenciarVendas());
        model.addAttribute("podeAcessarEstoque", usuario.podeAcessarEstoque());
        model.addAttribute("podeAcessarFinanceiro", usuario.podeAcessarFinanceiro());
        model.addAttribute("podeAcessarRH", usuario.podeAcessarRH());
        model.addAttribute("podeGerenciarRH", usuario.podeGerenciarRH());
        model.addAttribute("podeAcessarTI", usuario.hasRole("TI") || isAdmin || isMaster);
        model.addAttribute("podeAcessarMarketing", usuario.podeAcessarMarketing() || isGerencial);
        model.addAttribute("podeAcessarJuridico", usuario.hasRole("JURIDICO") || isAdmin || isMaster);
        model.addAttribute("podeGerenciarUsuarios", isAdmin || isMaster);
    }
}




## Como Rodar o Projeto

1. Clone o repositório  
```bash
git clone https://github.com/seu-usuario/painel-do-ceo.git
```

2. Acesse a pasta do projeto  
```bash
cd painel-do-ceo
```

3. Configure o banco de dados MySQL  
Certifique que o MySQL está rodando e execute no console:  
```sql
CREATE DATABASE painel_ceo;
```

4. Configure o arquivo `application.properties` para conectar ao banco:  
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/painel_ceo
spring.datasource.username=seu_usuario_mysql
spring.datasource.password=sua_senha_mysql
```

5. (Opcional) Configuração de e-mail SMTP para envio de senha  
```properties
spring.mail.host=smtp.seuservidor.com
spring.mail.port=587
spring.mail.username=seu-email@dominio.com
spring.mail.password=sua-senha
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

6. Rodar o projeto com Maven (modo rápido)  
```bash
./mvnw spring-boot:run
```

7. Ou gerar o .jar e executar manualmente  
```bash
./mvnw clean package
java -jar target/painel-do-ceo-0.0.1-SNAPSHOT.jar
```

Acesse o sistema em: [http://localhost:8080](http://localhost:8080)

## Contato e Contribuição

Para dúvidas, sugestões ou contribuições, fique à vontade para abrir uma issue ou enviar um pull request no repositório.

Obrigado por usar o Painel do CEO!

---

## Controllers recomendados (baseados nas pastas principais)

| Pasta / Módulo  | Controller sugerido      | Justificativa                                   |
|-----------------|-------------------------|------------------------------------------------|
| dashboard       | DashboardController      | Controla a página principal e suas subpáginas  |
| usuarios        | UsuarioController        | CRUD e funcionalidades relacionadas a usuários|
| permissoes      | PermissaoController      | Gestão de perfis e permissões                   |
| clientes        | ClienteController        | Gestão de clientes                              |
| fornecedores    | FornecedorController     | Gestão de fornecedores                          |
| produtos        | ProdutoController        | Produtos e seus subrecursos                     |
| estoque         | EstoqueController        | Controle de inventário, entradas, saídas, ajustes |
| vendas          | VendaController          | Vendas, relatórios, comissões                   |
| compras         | CompraController         | Compras, pedidos, histórico                      |
| financeiro      | FinanceiroController     | Contas a pagar/receber, fluxo de caixa, balanço|
| transferencias  | TransferenciaController  | Transferências bancárias ou internas            |
| servicos        | ServicoController        | Serviços, contratos, faturamento                 |
| agenda          | AgendaController         | Calendário, eventos, lembretes                   |
| projetos        | ProjetoController        | Projetos, tarefas, equipes                       |
| relatorios      | RelatorioController      | Relatórios variados                              |
| rh              | RhController             | Recursos Humanos, folha, benefícios, treinamentos|
| perfil          | PerfilController         | Edição de perfil, senha, configurações pessoais |
| configuracoes   | ConfiguracaoController   | Configurações gerais do sistema                  |
| contato         | ContatoController        | Formulários de contato e histórico               |
| login           | LoginController          | Login e recuperação de senha                      |
