# 🧠 Painel do CEO - ERP Corporativo

Sistema ERP completo para gestão empresarial desenvolvido com Spring Boot, Thymeleaf, MySQL e HTML/CSS puro. Ideal para controle administrativo de pequenas e médias empresas, com foco em modularidade, usabilidade e escalabilidade.

---

## 🖥️ Interface Administrativa

A estrutura base do sistema inclui uma sidebar lateral responsiva e funcional com as seguintes seções:

```html
<!-- src/main/resources/templates/components/sidebar.html -->
<div class="sidebar" th:fragment="sidebar">
  <div>
    <div class="user-panel">
      <img th:src="@{${usuario.fotoPerfil}}" alt="Foto do usuário" class="user-avatar" />
      <div class="user-name" th:text="${usuario.nome}">Nome do Usuário</div>
    </div>

    <nav>
      <ul>
        <li><a href="/dashboard"><i class="fas fa-tachometer-alt"></i> Dashboard</a></li>
        <li><a href="/usuarios/novo"><i class="fas fa-user-friends"></i> Usuários</a></li>
        <li><a href="/permissoes"><i class="fas fa-user-shield"></i> Perfis e Permissões</a></li>
        <li><a href="/clientes"><i class="fas fa-address-book"></i> Clientes</a></li>
        <li><a href="/fornecedores"><i class="fas fa-handshake"></i> Fornecedores</a></li>
        <li><a href="/produtos"><i class="fas fa-boxes"></i> Produtos</a></li>
        <li><a href="/estoque"><i class="fas fa-warehouse"></i> Estoque</a></li>
        <li><a href="/vendas"><i class="fas fa-cash-register"></i> Vendas</a></li>
        <li th:if="${isAdmin}"><a href="/financeiro"><i class="fas fa-wallet"></i> Financeiro</a></li>
        <li><a href="/contas-pagar"><i class="fas fa-money-check-alt"></i> Contas a Pagar</a></li>
        <li><a href="/contas-receber"><i class="fas fa-file-invoice-dollar"></i> Contas a Receber</a></li>
        <li><a href="/transferencias"><i class="fas fa-exchange-alt"></i> Transferências</a></li>
        <li><a href="/servicos"><i class="fas fa-tools"></i> Serviços</a></li>
        <li><a href="/agenda"><i class="fas fa-calendar-check"></i> Agenda</a></li>
        <li><a href="/projetos"><i class="fas fa-tasks"></i> Projetos</a></li>
        <li><a href="/relatorios"><i class="fas fa-chart-pie"></i> Relatórios</a></li>
        <li><a href="/perfil"><i class="fas fa-user-cog"></i> Meu Perfil</a></li>
        <li><a href="/configuracoes"><i class="fas fa-cogs"></i> Configurações</a></li>
        <li><a href="/contato"><i class="fas fa-envelope"></i> Contato</a></li>
        <li><a href="/logout"><i class="fas fa-power-off"></i> Sair</a></li>
      </ul>
    </nav>
  </div>
</div>
