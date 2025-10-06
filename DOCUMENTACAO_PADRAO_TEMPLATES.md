# Documentação de Padrões e Recomendações para Templates Thymeleaf

## 1. Estrutura Padrão de Templates

Todos os templates do sistema devem seguir a mesma estrutura base, conforme exemplificado na página de ajuda:

```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${titulo}">Título da Página - ERP Empresarial</title>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/style.css" rel="stylesheet">
    <link href="/css/sidebar.css" rel="stylesheet">
    <link href="/css/notifications.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
    <div class="app-container">
        <!-- Sidebar -->
        <aside th:replace="~{components/sidebar :: sidebar}"></aside>

        <!-- Main Content -->
        <main class="main-content">
            <!-- Topbar -->
            <header th:replace="~{components/topbar :: topbar}"></header>

            <!-- Conteúdo da página -->
            <section class="content-area">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-12 text-center py-5">
                            <h1 class="display-6 text-muted"><i class="fas fa-icone-aqui"></i> Título da Página</h1>
                            <p class="text-muted">Descrição ou conteúdo específico da página.</p>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Footer -->
            <footer th:replace="~{components/footer :: footer}"></footer>
        </main>
    </div>

    <script src="/js/sidebar.js" defer></script>
    <script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>
    <script th:src="@{/js/notifications.js}"></script>
</body>
</html>
```

## 2. Componentes Reutilizáveis

### 2.1 Sidebar
- Localização: `templates/components/sidebar.html`
- Fragmento: `sidebar`
- Inclusão: `<aside th:replace="~{components/sidebar :: sidebar}"></aside>`

### 2.2 Topbar
- Localização: `templates/components/topbar.html`
- Fragmento: `topbar`
- Inclusão: `<header th:replace="~{components/topbar :: topbar}"></header>`

### 2.3 Footer
- Localização: `templates/components/footer.html`
- Fragmento: `footer`
- Inclusão: `<footer th:replace="~{components/footer :: footer}"></footer>`

## 3. Padrões de Estilização

### 3.1 Títulos
- Classe: `display-6 text-muted`
- Deve incluir um ícone do Font Awesome apropriado
- Estrutura: `<h1 class="display-6 text-muted"><i class="fas fa-icone"></i> Título</h1>`

### 3.2 Conteúdo Centralizado
- Container: `container-fluid`
- Linha: `row`
- Coluna: `col-12 text-center py-5`
- Padding vertical: `py-5` (pode ser ajustado conforme necessário)

### 3.3 Textos Descritivos
- Classe: `text-muted`
- Elemento: `<p class="text-muted">Descrição</p>`

## 4. Ícones Font Awesome

Utilizar ícones relevantes para o contexto da página:
- Dashboard: `fas fa-chart-line`
- Lista: `fas fa-list`
- PDV: `fas fa-cash-register`
- Caixa: `fas fa-money-bill-wave`
- Novo: `fas fa-plus-circle`
- Clientes: `fas fa-users`
- Produtos: `fas fa-box`
- Relatórios: `fas fa-chart-bar`
- Configurações: `fas fa-cogs`
- Ajuda: `fas fa-question-circle`

## 5. Links CSS e Scripts

### 5.1 CSS Obrigatórios
```html
<link href="/css/bootstrap.min.css" rel="stylesheet">
<link href="/css/style.css" rel="stylesheet">
<link href="/css/sidebar.css" rel="stylesheet">
<link href="/css/notifications.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
```

### 5.2 Scripts Obrigatórios
```html
<script src="/js/sidebar.js" defer></script>
<script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>
<script th:src="@{/js/notifications.js}"></script>
```

## 6. Boas Práticas

### 6.1 Títulos Dinâmicos
- Utilizar `th:text="${titulo}"` para definir o título da página dinamicamente
- Definir título padrão como fallback

### 6.2 Estrutura Semântica
- Manter a estrutura de divs com classes consistentes
- Utilizar tags semânticas apropriadas (`header`, `main`, `section`, `aside`, `footer`)

### 6.3 Responsividade
- Utilizar classes do Bootstrap para responsividade
- Testar em diferentes tamanhos de tela

## 7. Implementação no Controller

Exemplo de implementação no controller:

```java
@GetMapping("/dashboard")
public String dashboard(Model model) {
    model.addAttribute("titulo", "Dashboard de Vendas");
    model.addAttribute("conteudo", "vendas/dashboard :: conteudo");
    return "layout/base";
}
```

## 8. Exemplo Completo de Template

```
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${titulo}">Dashboard - ERP Empresarial</title>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/style.css" rel="stylesheet">
    <link href="/css/sidebar.css" rel="stylesheet">
    <link href="/css/notifications.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
    <div class="app-container">
        <!-- Sidebar -->
        <aside th:replace="~{components/sidebar :: sidebar}"></aside>

        <!-- Main Content -->
        <main class="main-content">
            <!-- Topbar -->
            <header th:replace="~{components/topbar :: topbar}"></header>

            <!-- Conteúdo da página -->
            <section class="content-area">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-12 text-center py-5">
                            <h1 class="display-6 text-muted"><i class="fas fa-chart-line"></i> Dashboard</h1>
                            <p class="text-muted">Conteúdo específico do dashboard.</p>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Footer -->
            <footer th:replace="~{components/footer :: footer}"></footer>
        </main>
    </div>

    <script src="/js/sidebar.js" defer></script>
    <script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>
    <script th:src="@{/js/notifications.js}"></script>
</body>
</html>
```

## 9. Recomendações de Implementação Gradual

Para uma implementação eficiente do módulo de vendas, siga esta ordem de prioridade:

### 9.1. Lista de Vendas (/vendas)
**Prioridade: Alta** - Ponto de entrada do módulo

**Funcionalidades a implementar:**
- Exibição de vendas em uma tabela simples
- Filtros básicos (data, cliente, status)
- Links para detalhes e edição

### 9.2. Nova Venda (/vendas/novo)
**Prioridade: Alta** - Essencial para criar registros

**Funcionalidades a implementar:**
- Formulário com campos básicos (cliente, produto, quantidade, preço)
- Validação simples de campos obrigatórios
- Botão para salvar a venda

### 9.3. PDV (/vendas/pdv)
**Prioridade: Média** - Interface para vendas rápidas

**Funcionalidades a implementar:**
- Busca de produtos por código ou nome
- Adição de itens ao carrinho
- Cálculo automático de totais
- Finalização de venda

### 9.4. Clientes (/vendas/clientes)
**Prioridade: Média** - Gerenciamento da base de clientes

**Funcionalidades a implementar:**
- Listagem de clientes cadastrados
- Formulário para adicionar/editar clientes
- Dados básicos (nome, CPF/CNPJ, contato)

Esta documentação deve ser seguida para manter a consistência visual e funcional em todo o sistema ERP.