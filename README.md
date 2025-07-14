# Painel do CEO - ERP Corporativo

Sistema ERP completo para gestão empresarial desenvolvido com Spring Boot, Thymeleaf, MySQL e HTML/CSS puro.  
Ideal para controle administrativo de pequenas e médias empresas, com foco em modularidade, usabilidade e escalabilidade.

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
│   ├── header.html            # Cabeçalho reutilizável (topbar)
│   ├── sidebar.html           # Menu lateral (sidebar) principal
│   └── footer.html            # Rodapé comum
│
├── dashboard/
│   ├── index.html             # Tela principal do dashboard
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
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── detalhes.html
│   ├── historico.html
│   ├── busca-avancada.html
│   └── contratos.html
│
├── fornecedores/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── contratos.html
│   ├── avaliacoes.html
│   └── pagamentos.html
│
├── produtos/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── categorias.html
│   ├── detalhes.html
│   ├── importacao.html
│   ├── exportacao.html
│   ├── precificacao.html
│   └── fornecedores.html
│
├── estoque/
│   ├── inventario.html
│   ├── entrada.html
│   ├── saida.html
│   ├── ajustes.html
│   ├── transferencias.html
│   ├── alertas.html
│   ├── relatorios.html
│   └── auditoria.html
│
├── vendas/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── detalhes.html
│   ├── relatorio-mensal.html
│   ├── relatorio-anual.html
│   ├── devolucoes.html
│   ├── comissoes.html
│   ├── dashboards.html
│   └── faturamento.html
│
├── compras/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── fornecedores.html
│   ├── historico.html
│   ├── relatorios.html
│   ├── pedidos.html
│   └── aprovacoes.html
│
├── financeiro/
│   ├── dashboard.html
│   ├── contas-pagar.html
│   ├── contas-receber.html
│   ├── fluxo-caixa.html
│   ├── balanco.html
│   ├── receitas.html
│   ├── despesas.html
│   ├── orcamentos.html
│   ├── pagamentos.html
│   ├── conciliacao.html
│   └── relatorios.html
│
├── transferencias/
│   ├── listar.html
│   ├── novo.html
│   └── historico.html
│
├── servicos/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── contratos.html
│   └── faturamento.html
│
├── agenda/
│   ├── calendario.html
│   ├── eventos.html
│   ├── novo-evento.html
│   ├── lembretes.html
│   └── configuracoes.html
│
├── projetos/
│   ├── listar.html
│   ├── novo.html
│   ├── editar.html
│   ├── tarefas.html
│   ├── equipes.html
│   ├── cronograma.html
│   └── relatorios.html
│
├── relatorios/
│   ├── vendas.html
│   ├── financeiro.html
│   ├── estoque.html
│   ├── rh.html
│   ├── clientes.html
│   └── customizados.html
│
├── rh/
│   ├── colaboradores.html
│   ├── folha-pagamento.html
│   ├── beneficios.html
│   ├── horarios.html
│   ├── ferias.html
│   ├── treinamentos.html
│   ├── avaliacao-desempenho.html
│   ├── recrutamento.html
│   ├── relatorios.html
│   └── ponto-eletronico.html
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
