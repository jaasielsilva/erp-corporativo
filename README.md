# 🧠 Painel do CEO - ERP Corporativo

Sistema ERP completo para gestão empresarial desenvolvido com Spring Boot, Thymeleaf, MySQL e HTML/CSS puro.  
Ideal para controle administrativo de pequenas e médias empresas, com foco em modularidade, usabilidade e escalabilidade.

---

## 🖥️ Interface Administrativa

O sistema possui uma interface web administrativa moderna, com estrutura modular e responsiva.  
A navegação principal é feita por uma **sidebar lateral** que permanece consistente em todas as páginas, facilitando o acesso rápido aos principais módulos.

---

## 🛠️ Tecnologias e Arquitetura

- **Backend:** Spring Boot (Java 17+), Spring Security, Spring Data JPA  
- **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript (com potencial para frameworks futuros)  
- **Banco de Dados:** MySQL  
- **Gerenciamento de Dependências:** Maven ou Gradle  
- **Controle de Versão:** Git  

---
## 📧 Funcionalidades de E-mail
O sistema possui suporte ao envio automático de e-mails, como para resetar a senha de um usuário

configuração (substitua pelos dados reais do seu servidor de e-mail):
# Configuração SMTP para envio de e-mails
spring.mail.host=smtp.seuservidor.com      # endereço do servidor SMTP
spring.mail.port=587                       # porta (geralmente 587 para TLS)
spring.mail.username=seu-email@dominio.com # seu login de e-mail
spring.mail.password=sua-senha             # sua senha do e-mail
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

Incluir a dependência do Spring Boot Starter Mail
No arquivo pom.xml (para Maven), certifique-se de ter essa dependência para usar o serviço de e-mail:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

---

## 🧩 Modularidade e Escalabilidade

O sistema é dividido em módulos independentes que facilitam:

- Manutenção e evolução de funcionalidades  
- Adição de novos módulos sem impacto no núcleo  
- Testes unitários e integração focados por módulo  

---

## 🔐 Segurança

- Controle de acesso baseado em perfis e permissões
  
---

## 📂 Organização dos Templates Thymeleaf

Componentes reutilizáveis (header, sidebar, footer) para manter consistência visual e facilitar atualizações globais.  


## 📁 Estrutura dos Templates Thymeleaf

```plaintext
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
---

## 📦 Como Rodar o Projeto


Breve passo a passo para iniciar o sistema localmente:



# Clonar o repositório
git clone https://github.com/seu-usuario/painel-do-ceo.git

# Entrar na pasta do projeto
cd painel-do-ceo

# Configurar o banco de dados (MySQL)
# Criar schema e ajustar application.properties conforme seu ambiente


## Build e run com Maven
./mvnw spring-boot:run

## ou Executar o projeto
./mvnw clean package
java -jar target/painel-do-ceo-0.0.1-SNAPSHOT.jar
 
