# Documentação Técnica - Estrutura das Páginas HTML

## Arquitetura Frontend

### Estrutura de Diretórios
```
templates/rh/colaboradores/adesao/
├── inicio.html          # 517 linhas - Formulário de dados pessoais
├── documentos.html      # 747 linhas - Sistema de upload de documentos
├── beneficios.html      # 928 linhas - Seleção de benefícios
├── revisao.html         # 801 linhas - Revisão final e termos
└── status.html          # 585 linhas - Acompanhamento de status
```

### Padrões de Desenvolvimento

#### 1. Estrutura HTML Comum
Todas as páginas seguem a mesma estrutura base:

```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Título da Página - Portal CEO</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    
    <style>
        /* CSS customizado específico da página */
    </style>
</head>
<body>
    <div th:replace="~{fragments/header :: header}"></div>
    
    <!-- Conteúdo da página -->
    
    <div th:replace="~{fragments/footer :: footer}"></div>
    
    <!-- Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>
    
    <script>
        /* JavaScript específico da página */
    </script>
</body>
</html>
```

#### 2. Componente Indicador de Etapas
Todas as páginas do processo incluem o mesmo indicador visual:

```html
<div class="step-indicator">
    <div class="step completed|active|pending">
        <div class="step-number">1|<i class="fas fa-check"></i></div>
        <span>Nome da Etapa</span>
    </div>
    <div class="step-line"></div>
    <!-- Repetir para cada etapa -->
</div>
```

**Estados do Indicador**:
- `completed`: Etapa finalizada (ícone de check verde)
- `active`: Etapa atual (número destacado em azul)
- `pending`: Etapa pendente (número em cinza)

#### 3. Sistema de CSS Customizado

**Variáveis de Cores**:
```css
:root {
    --primary-color: #0d6efd;
    --success-color: #198754;
    --warning-color: #ffc107;
    --danger-color: #dc3545;
    --light-bg: #f8f9fa;
}
```

**Classes Reutilizáveis**:
- `.step-indicator`: Container do indicador de etapas
- `.form-section`: Seções do formulário com espaçamento padronizado
- `.upload-area`: Área de drag-and-drop para uploads
- `.benefit-card`: Cards de benefícios com toggle
- `.review-card`: Cards de revisão com botão de editar
- `.loading-overlay`: Overlay de carregamento

### Análise Detalhada por Página

## 1. inicio.html (517 linhas)

### Estrutura do Formulário
```html
<form id="formDadosPessoais" class="needs-validation" novalidate>
    <!-- Dados Pessoais -->
    <div class="form-section">
        <h4>Dados Pessoais</h4>
        <div class="row">
            <!-- Campos de input organizados em grid -->
        </div>
    </div>
    
    <!-- Dados Profissionais -->
    <div class="form-section">
        <h4>Dados Profissionais</h4>
        <!-- Campos profissionais -->
    </div>
    
    <!-- Endereço -->
    <div class="form-section">
        <h4>Endereço</h4>
        <!-- Campos de endereço -->
    </div>
    
    <!-- Observações -->
    <div class="form-section">
        <h4>Observações</h4>
        <!-- Campo de texto livre -->
    </div>
</form>
```

### Funcionalidades JavaScript
- **Máscaras de Input**: CPF, telefone, CEP usando jQuery Mask
- **Validação de CPF**: Algoritmo de validação completo
- **Integração ViaCEP**: Preenchimento automático de endereço
- **Validação de Formulário**: Bootstrap validation + custom validation
- **Envio AJAX**: Submissão assíncrona com feedback visual

### APIs Integradas
- `https://viacep.com.br/ws/{cep}/json/` - Consulta de CEP
- `POST /rh/colaboradores/adesao/dados-pessoais` - Envio dos dados

## 2. documentos.html (747 linhas)

### Sistema de Upload
```html
<div class="upload-section">
    <div class="upload-area" data-tipo="rg">
        <div class="upload-content">
            <i class="fas fa-cloud-upload-alt"></i>
            <p>Arraste o arquivo ou clique para selecionar</p>
            <input type="file" class="upload-input" accept=".pdf,.jpg,.jpeg,.png">
        </div>
        <div class="upload-progress" style="display: none;">
            <div class="progress">
                <div class="progress-bar"></div>
            </div>
        </div>
        <div class="upload-preview" style="display: none;">
            <!-- Preview do arquivo -->
        </div>
    </div>
</div>
```

### Funcionalidades de Upload
- **Drag & Drop**: Suporte nativo para arrastar arquivos
- **Preview**: Visualização de imagens e PDFs
- **Progress Bar**: Indicador de progresso durante upload
- **Validação**: Tipo de arquivo e tamanho (máx 5MB)
- **Remoção**: Possibilidade de remover arquivos enviados

### Estados dos Documentos
- `pending`: Aguardando upload
- `uploading`: Upload em progresso
- `uploaded`: Upload concluído
- `error`: Erro no upload

## 3. beneficios.html (928 linhas)

### Estrutura de Benefícios
```html
<div class="benefit-card" data-beneficio="plano-saude">
    <div class="benefit-header">
        <div class="benefit-info">
            <div class="benefit-icon">
                <i class="fas fa-heartbeat"></i>
            </div>
            <div>
                <h5>Plano de Saúde</h5>
                <p>Assistência médica completa</p>
            </div>
        </div>
        <div class="benefit-toggle">
            <input type="checkbox" class="benefit-switch">
            <span class="slider"></span>
        </div>
    </div>
    
    <div class="benefit-details" style="display: none;">
        <div class="plan-options">
            <!-- Opções de planos -->
        </div>
        <div class="dependents-section">
            <!-- Seção de dependentes -->
        </div>
    </div>
</div>
```

### Sistema de Cálculo
- **Cálculo Dinâmico**: Atualização em tempo real dos valores
- **Dependentes**: Adição/remoção dinâmica para plano de saúde
- **Resumo**: Exibição detalhada dos custos
- **Validação**: Verificação de planos selecionados

## 4. revisao.html (801 linhas)

### Cards de Revisão
```html
<div class="review-card">
    <div class="review-header">
        <div class="d-flex align-items-center">
            <div class="review-icon">
                <i class="fas fa-user"></i>
            </div>
            <div>
                <h4>Dados Pessoais</h4>
                <small>Informações básicas do colaborador</small>
            </div>
        </div>
        <a href="#" class="btn-edit" onclick="editarDadosPessoais()">
            <i class="fas fa-edit"></i>Editar
        </a>
    </div>
    
    <div id="dadosPessoais">
        <!-- Dados carregados via JavaScript -->
    </div>
</div>
```

### Termos e Condições
```html
<div class="terms-section">
    <div class="form-check">
        <input class="form-check-input" type="checkbox" id="aceitarTermos" required>
        <label class="form-check-label" for="aceitarTermos">
            <strong>Li e aceito os termos e condições</strong>
        </label>
    </div>
    <!-- Outros checkboxes obrigatórios -->
</div>
```

## 5. status.html (585 linhas)

### Indicador de Status
```html
<div class="status-header">
    <div class="status-icon" id="status-icon">
        <i class="fas fa-clock"></i>
    </div>
    <h2 id="status-titulo">Processo em Andamento</h2>
    <p id="status-descricao">Descrição do status atual</p>
</div>

<div class="progress-section">
    <div class="progress progress-custom">
        <div class="progress-bar progress-bar-custom" id="barra-progresso"></div>
    </div>
</div>
```

### Timeline de Etapas
```html
<div id="lista-etapas">
    <div class="etapa-item">
        <div class="etapa-icon etapa-concluida">
            <i class="fas fa-check"></i>
        </div>
        <div class="flex-grow-1">
            <h6>Nome da Etapa</h6>
            <small>Descrição da etapa</small>
        </div>
    </div>
</div>
```

## Padrões de JavaScript

### 1. Estrutura de Scripts
```javascript
$(document).ready(function() {
    // Inicialização
    inicializar();
    
    // Configurar eventos
    configurarEventos();
    
    // Carregar dados se necessário
    carregarDados();
});

function inicializar() {
    // Configurações iniciais
}

function configurarEventos() {
    // Event listeners
}

function carregarDados() {
    // Carregamento de dados via AJAX
}
```

### 2. Padrão de Chamadas AJAX
```javascript
function chamarAPI(url, method, data, successCallback, errorCallback) {
    $.ajax({
        url: url,
        method: method,
        data: data,
        success: function(response) {
            if (response.success) {
                successCallback(response);
            } else {
                mostrarErro(response.message);
            }
        },
        error: function(xhr) {
            const response = xhr.responseJSON;
            errorCallback(response?.message || 'Erro interno do servidor');
        }
    });
}
```

### 3. Funções Utilitárias Comuns
```javascript
function mostrarErro(mensagem) {
    // Exibir alert de erro
}

function mostrarSucesso(mensagem) {
    // Exibir alert de sucesso
}

function formatarMoeda(valor) {
    // Formatar valor monetário
}

function formatarData(data) {
    // Formatar data para pt-BR
}

function validarCPF(cpf) {
    // Validação de CPF
}
```

## Responsividade e Acessibilidade

### Breakpoints Bootstrap
- **xs**: < 576px (Mobile)
- **sm**: ≥ 576px (Mobile landscape)
- **md**: ≥ 768px (Tablet)
- **lg**: ≥ 992px (Desktop)
- **xl**: ≥ 1200px (Large desktop)

### Classes Responsivas Utilizadas
- `col-12 col-md-6 col-lg-4`: Grid responsivo
- `d-none d-md-block`: Visibilidade condicional
- `text-center text-md-start`: Alinhamento responsivo

### Acessibilidade
- **ARIA Labels**: `aria-label`, `aria-describedby`
- **Roles**: `role="alert"`, `role="status"`
- **Navegação por Teclado**: `tabindex`, `focus`
- **Screen Readers**: Textos alternativos e descrições

## Performance e Otimização

### Carregamento de Recursos
- **CDN**: Bootstrap e jQuery via CDN
- **Lazy Loading**: Imagens carregadas sob demanda
- **Minificação**: CSS e JS customizados minificados

### Otimizações JavaScript
- **Debounce**: Para eventos de input frequentes
- **Cache**: Resultados de APIs quando apropriado
- **Event Delegation**: Para elementos dinâmicos

### Métricas de Performance
- **inicio.html**: ~517 linhas, ~25KB
- **documentos.html**: ~747 linhas, ~35KB
- **beneficios.html**: ~928 linhas, ~42KB
- **revisao.html**: ~801 linhas, ~38KB
- **status.html**: ~585 linhas, ~28KB

## Manutenibilidade

### Convenções de Nomenclatura
- **IDs**: camelCase (`formDadosPessoais`)
- **Classes CSS**: kebab-case (`step-indicator`)
- **Funções JS**: camelCase (`carregarDados`)
- **Variáveis**: camelCase (`sessionId`)

### Comentários e Documentação
- Comentários em português
- Seções claramente delimitadas
- Funções documentadas com propósito
- TODOs marcados quando necessário

### Modularização
- CSS organizado por componentes
- JavaScript separado por funcionalidade
- Reutilização de componentes comuns
- Separação clara entre lógica e apresentação

---

**Versão**: 1.0  
**Data**: Janeiro 2025  
**Última Atualização**: Análise completa da estrutura HTML