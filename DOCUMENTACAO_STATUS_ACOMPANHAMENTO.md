# Documentação da Página de Status e Acompanhamento

## Visão Geral

A página de status (`status.html`) é responsável por exibir o acompanhamento em tempo real do processo de adesão do colaborador, permitindo visualizar o progresso, status atual, histórico de eventos e ações disponíveis.

---

## Estrutura da Página

### Layout Principal

```html
<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <!-- Header do Status -->
            <div class="status-header">
                <div class="status-icon">
                    <i class="fas fa-clock text-warning" id="statusIcon"></i>
                </div>
                <div class="status-info">
                    <h2 id="statusTitle">Aguardando Aprovação</h2>
                    <p id="statusDescription">Seu processo está sendo analisado pelo RH</p>
                </div>
            </div>
            
            <!-- Barra de Progresso -->
            <div class="progress-section">
                <div class="progress-container">
                    <div class="progress-bar-custom">
                        <div class="progress-fill" id="progressFill"></div>
                    </div>
                    <div class="progress-steps">
                        <div class="step-item completed" data-step="iniciado">
                            <div class="step-circle">
                                <i class="fas fa-check"></i>
                            </div>
                            <span>Iniciado</span>
                        </div>
                        <div class="step-item active" data-step="analise">
                            <div class="step-circle">
                                <i class="fas fa-search"></i>
                            </div>
                            <span>Em Análise</span>
                        </div>
                        <div class="step-item" data-step="aprovacao">
                            <div class="step-circle">
                                <i class="fas fa-user-check"></i>
                            </div>
                            <span>Aprovação</span>
                        </div>
                        <div class="step-item" data-step="finalizado">
                            <div class="step-circle">
                                <i class="fas fa-flag-checkered"></i>
                            </div>
                            <span>Finalizado</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
```

---

## Componentes da Interface

### 1. Header de Status

#### 1.1 Ícone de Status
```css
.status-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 2.5rem;
    margin-right: 20px;
}

.status-icon.warning {
    background: linear-gradient(135deg, #ffeaa7, #fdcb6e);
    color: #e17055;
}

.status-icon.success {
    background: linear-gradient(135deg, #00b894, #00cec9);
    color: white;
}

.status-icon.danger {
    background: linear-gradient(135deg, #e17055, #d63031);
    color: white;
}

.status-icon.info {
    background: linear-gradient(135deg, #74b9ff, #0984e3);
    color: white;
}
```

#### 1.2 Estados do Ícone

| Status | Ícone | Classe CSS | Cor |
|--------|-------|------------|-----|
| Aguardando Aprovação | `fas fa-clock` | `warning` | Laranja |
| Aprovado | `fas fa-check-circle` | `success` | Verde |
| Rejeitado | `fas fa-times-circle` | `danger` | Vermelho |
| Em Análise | `fas fa-search` | `info` | Azul |
| Finalizado | `fas fa-flag-checkered` | `success` | Verde |

### 2. Barra de Progresso Customizada

#### 2.1 Estrutura CSS
```css
.progress-container {
    position: relative;
    margin: 40px 0;
}

.progress-bar-custom {
    height: 8px;
    background: #e9ecef;
    border-radius: 4px;
    position: relative;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #00b894, #00cec9);
    border-radius: 4px;
    transition: width 0.8s ease-in-out;
    position: relative;
}

.progress-fill::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
    animation: shimmer 2s infinite;
}

@keyframes shimmer {
    0% { transform: translateX(-100%); }
    100% { transform: translateX(100%); }
}
```

#### 2.2 Cálculo de Progresso
```javascript
function atualizarProgresso(status) {
    const progressMap = {
        'INICIADO': 25,
        'EM_ANDAMENTO': 50,
        'AGUARDANDO_APROVACAO': 75,
        'APROVADO': 90,
        'FINALIZADO': 100,
        'REJEITADO': 75,
        'CANCELADO': 0
    };
    
    const progresso = progressMap[status] || 0;
    $('#progressFill').css('width', progresso + '%');
    
    // Atualizar steps visuais
    $('.step-item').each(function(index) {
        const stepProgress = (index + 1) * 25;
        if (progresso >= stepProgress) {
            $(this).addClass('completed').removeClass('active');
        } else if (progresso >= stepProgress - 12.5) {
            $(this).addClass('active').removeClass('completed');
        } else {
            $(this).removeClass('completed active');
        }
    });
}
```

### 3. Cards de Informação

#### 3.1 Dados do Colaborador
```html
<div class="info-card">
    <h5><i class="fas fa-user me-2"></i>Dados do Colaborador</h5>
    <div class="row">
        <div class="col-md-6">
            <div class="info-row">
                <span class="info-label">Nome:</span>
                <span class="info-value" id="colaboradorNome">-</span>
            </div>
            <div class="info-row">
                <span class="info-label">CPF:</span>
                <span class="info-value" id="colaboradorCpf">-</span>
            </div>
        </div>
        <div class="col-md-6">
            <div class="info-row">
                <span class="info-label">Cargo:</span>
                <span class="info-value" id="colaboradorCargo">-</span>
            </div>
            <div class="info-row">
                <span class="info-label">Departamento:</span>
                <span class="info-value" id="colaboradorDepartamento">-</span>
            </div>
        </div>
    </div>
</div>
```

#### 3.2 Informações do Processo
```html
<div class="info-card">
    <h5><i class="fas fa-info-circle me-2"></i>Informações do Processo</h5>
    <div class="row">
        <div class="col-md-4">
            <div class="info-row">
                <span class="info-label">Protocolo:</span>
                <span class="info-value" id="protocoloProcesso">-</span>
            </div>
        </div>
        <div class="col-md-4">
            <div class="info-row">
                <span class="info-label">Data de Submissão:</span>
                <span class="info-value" id="dataSubmissao">-</span>
            </div>
        </div>
        <div class="col-md-4">
            <div class="info-row">
                <span class="info-label">Última Atualização:</span>
                <span class="info-value" id="ultimaAtualizacao">-</span>
            </div>
        </div>
    </div>
</div>
```

### 4. Etapas do Processo

#### 4.1 Lista de Etapas
```html
<div class="info-card">
    <h5><i class="fas fa-list-ol me-2"></i>Etapas do Processo</h5>
    <div class="process-steps" id="processSteps">
        <!-- Etapas serão carregadas dinamicamente -->
    </div>
</div>
```

#### 4.2 Template de Etapa
```javascript
function criarEtapaHtml(etapa) {
    const statusClass = {
        'CONCLUIDO': 'completed',
        'EM_ANDAMENTO': 'active',
        'PENDENTE': 'pending',
        'REJEITADO': 'rejected'
    };
    
    const statusIcon = {
        'CONCLUIDO': 'fas fa-check-circle text-success',
        'EM_ANDAMENTO': 'fas fa-clock text-warning',
        'PENDENTE': 'fas fa-circle text-muted',
        'REJEITADO': 'fas fa-times-circle text-danger'
    };
    
    return `
        <div class="process-step ${statusClass[etapa.status] || 'pending'}">
            <div class="step-indicator">
                <i class="${statusIcon[etapa.status] || 'fas fa-circle text-muted'}"></i>
            </div>
            <div class="step-content">
                <h6>${etapa.nome}</h6>
                <p class="step-description">${etapa.descricao || ''}</p>
                ${etapa.dataFinalizacao ? `<small class="text-muted">Concluído em: ${formatarData(etapa.dataFinalizacao)}</small>` : ''}
                ${etapa.dataInicio && etapa.status === 'EM_ANDAMENTO' ? `<small class="text-muted">Iniciado em: ${formatarData(etapa.dataInicio)}</small>` : ''}
            </div>
        </div>
    `;
}
```

### 5. Mensagens de Status

#### 5.1 Tipos de Alerta
```css
.custom-alert {
    padding: 20px;
    border-radius: 8px;
    margin: 20px 0;
    border-left: 4px solid;
}

.custom-alert.info {
    background: #e3f2fd;
    border-color: #2196f3;
    color: #1565c0;
}

.custom-alert.warning {
    background: #fff3e0;
    border-color: #ff9800;
    color: #ef6c00;
}

.custom-alert.success {
    background: #e8f5e8;
    border-color: #4caf50;
    color: #2e7d32;
}

.custom-alert.danger {
    background: #ffebee;
    border-color: #f44336;
    color: #c62828;
}
```

#### 5.2 Mensagens por Status
```javascript
const mensagensStatus = {
    'AGUARDANDO_APROVACAO': {
        tipo: 'info',
        titulo: 'Aguardando Aprovação',
        descricao: 'Seu processo está sendo analisado pelo RH. Você será notificado quando houver atualizações.'
    },
    'APROVADO': {
        tipo: 'success',
        titulo: 'Processo Aprovado',
        descricao: 'Parabéns! Seu processo foi aprovado. Os benefícios serão ativados conforme cronograma.'
    },
    'REJEITADO': {
        tipo: 'danger',
        titulo: 'Processo Rejeitado',
        descricao: 'Seu processo foi rejeitado. Verifique os comentários e faça as correções necessárias.'
    },
    'FINALIZADO': {
        tipo: 'success',
        titulo: 'Processo Finalizado',
        descricao: 'Seu processo foi concluído com sucesso. Bem-vindo à empresa!'
    }
};
```

### 6. Histórico de Eventos

#### 6.1 Timeline de Eventos
```html
<div class="info-card" id="historicoSection" style="display: none;">
    <h5>
        <i class="fas fa-history me-2"></i>Histórico do Processo
        <button class="btn btn-sm btn-outline-secondary float-end" onclick="toggleHistorico()">
            <i class="fas fa-eye"></i> Mostrar
        </button>
    </h5>
    <div class="timeline" id="timelineHistorico">
        <!-- Eventos serão carregados dinamicamente -->
    </div>
</div>
```

#### 6.2 Item de Timeline
```javascript
function criarEventoHistorico(evento) {
    return `
        <div class="timeline-item">
            <div class="timeline-marker">
                <i class="fas fa-circle"></i>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <h6>${evento.evento}</h6>
                    <small class="text-muted">${formatarDataHora(evento.data)}</small>
                </div>
                <p class="timeline-description">${evento.descricao || ''}</p>
                <small class="timeline-user">Por: ${evento.usuario}</small>
            </div>
        </div>
    `;
}
```

---

## Funcionalidades JavaScript

### 1. Inicialização da Página

```javascript
$(document).ready(function() {
    // Extrair sessionId da URL
    const urlParams = new URLSearchParams(window.location.search);
    sessionId = urlParams.get('sessionId');
    
    if (!sessionId) {
        mostrarErro('Sessão não encontrada');
        return;
    }
    
    // Carregar status inicial
    carregarStatus();
    
    // Configurar atualização automática
    iniciarAtualizacaoAutomatica();
    
    // Configurar notificações do navegador
    solicitarPermissaoNotificacao();
});
```

### 2. Carregamento de Status

```javascript
function carregarStatus() {
    mostrarLoading(true);
    
    $.ajax({
        url: `/rh/colaboradores/adesao/api/status/${sessionId}`,
        method: 'GET',
        success: function(response) {
            if (response.success) {
                atualizarInterface(response.data);
                ultimoStatus = response.data.status;
            } else {
                mostrarErro(response.message);
            }
        },
        error: function(xhr) {
            if (xhr.status === 404) {
                mostrarErro('Processo não encontrado');
            } else {
                mostrarErro('Erro ao carregar status');
            }
        },
        complete: function() {
            mostrarLoading(false);
        }
    });
}
```

### 3. Atualização da Interface

```javascript
function atualizarInterface(dados) {
    // Atualizar header de status
    atualizarHeaderStatus(dados.status, dados.mensagem);
    
    // Atualizar progresso
    atualizarProgresso(dados.status);
    
    // Atualizar dados do colaborador
    if (dados.colaborador) {
        $('#colaboradorNome').text(dados.colaborador.nome);
        $('#colaboradorCpf').text(dados.colaborador.cpf);
        $('#colaboradorCargo').text(dados.colaborador.cargo);
        $('#colaboradorDepartamento').text(dados.colaborador.departamento);
    }
    
    // Atualizar informações do processo
    $('#protocoloProcesso').text(dados.protocolo || '-');
    $('#dataSubmissao').text(formatarData(dados.dataSubmissao));
    $('#ultimaAtualizacao').text(formatarDataHora(dados.ultimaAtualizacao));
    
    // Atualizar etapas
    if (dados.etapas) {
        atualizarEtapas(dados.etapas);
    }
    
    // Atualizar histórico
    if (dados.historico) {
        atualizarHistorico(dados.historico);
    }
    
    // Mostrar mensagem de status
    if (dados.mensagem) {
        mostrarMensagemStatus(dados.mensagem);
    }
}
```

### 4. Atualização Automática

```javascript
let intervalId;
const INTERVALO_ATUALIZACAO = 30000; // 30 segundos

function iniciarAtualizacaoAutomatica() {
    intervalId = setInterval(function() {
        carregarStatus();
    }, INTERVALO_ATUALIZACAO);
}

function pararAtualizacaoAutomatica() {
    if (intervalId) {
        clearInterval(intervalId);
        intervalId = null;
    }
}

// Parar atualização quando a página não estiver visível
document.addEventListener('visibilitychange', function() {
    if (document.hidden) {
        pararAtualizacaoAutomatica();
    } else {
        iniciarAtualizacaoAutomatica();
    }
});
```

### 5. Notificações do Navegador

```javascript
function solicitarPermissaoNotificacao() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }
}

function enviarNotificacao(titulo, mensagem) {
    if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(titulo, {
            body: mensagem,
            icon: '/assets/img/logo-notification.png',
            tag: 'adesao-status'
        });
    }
}

// Verificar mudança de status para notificar
let ultimoStatus = null;

function verificarMudancaStatus(novoStatus) {
    if (ultimoStatus && ultimoStatus !== novoStatus) {
        const mensagens = {
            'APROVADO': 'Seu processo foi aprovado!',
            'REJEITADO': 'Seu processo foi rejeitado. Verifique os comentários.',
            'FINALIZADO': 'Seu processo foi finalizado com sucesso!'
        };
        
        if (mensagens[novoStatus]) {
            enviarNotificacao('Adesão de Colaborador', mensagens[novoStatus]);
        }
    }
    ultimoStatus = novoStatus;
}
```

### 6. Ações Disponíveis

#### 6.1 Atualizar Status
```javascript
function atualizarStatusManual() {
    $('#btnAtualizarStatus').prop('disabled', true)
        .html('<i class="fas fa-spinner fa-spin"></i> Atualizando...');
    
    carregarStatus();
    
    setTimeout(() => {
        $('#btnAtualizarStatus').prop('disabled', false)
            .html('<i class="fas fa-sync-alt"></i> Atualizar Status');
    }, 2000);
}
```

#### 6.2 Imprimir Comprovante
```javascript
function imprimirComprovante() {
    const conteudo = `
        <div style="text-align: center; font-family: Arial, sans-serif;">
            <h2>Comprovante de Adesão</h2>
            <p><strong>Protocolo:</strong> ${$('#protocoloProcesso').text()}</p>
            <p><strong>Colaborador:</strong> ${$('#colaboradorNome').text()}</p>
            <p><strong>CPF:</strong> ${$('#colaboradorCpf').text()}</p>
            <p><strong>Data de Submissão:</strong> ${$('#dataSubmissao').text()}</p>
            <p><strong>Status:</strong> ${$('#statusTitle').text()}</p>
            <hr>
            <p style="font-size: 12px; color: #666;">
                Este comprovante foi gerado em ${new Date().toLocaleString('pt-BR')}
            </p>
        </div>
    `;
    
    const janela = window.open('', '_blank');
    janela.document.write(conteudo);
    janela.document.close();
    janela.print();
}
```

#### 6.3 Voltar ao Início
```javascript
function voltarInicio() {
    if (confirm('Deseja realmente voltar ao início? Isso iniciará um novo processo.')) {
        window.location.href = '/rh/colaboradores/adesao';
    }
}
```

---

## Responsividade e Acessibilidade

### 1. Design Responsivo

```css
/* Mobile First */
@media (max-width: 768px) {
    .status-header {
        flex-direction: column;
        text-align: center;
    }
    
    .status-icon {
        margin: 0 0 15px 0;
    }
    
    .progress-steps {
        flex-direction: column;
        gap: 10px;
    }
    
    .step-item {
        flex-direction: row;
        align-items: center;
    }
    
    .info-card {
        margin-bottom: 15px;
    }
}

@media (max-width: 576px) {
    .container-fluid {
        padding: 10px;
    }
    
    .info-row {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .info-label {
        font-weight: bold;
        margin-bottom: 5px;
    }
}
```

### 2. Acessibilidade

```html
<!-- ARIA Labels -->
<div class="progress-bar-custom" role="progressbar" 
     aria-valuenow="75" aria-valuemin="0" aria-valuemax="100"
     aria-label="Progresso do processo de adesão">
</div>

<!-- Screen Reader Support -->
<span class="sr-only">Status atual: Aguardando aprovação</span>

<!-- Keyboard Navigation -->
<button class="btn btn-primary" tabindex="0" 
        onkeypress="if(event.key==='Enter') atualizarStatusManual()">
    Atualizar Status
</button>
```

---

## Performance e Otimização

### 1. Lazy Loading
```javascript
// Carregar histórico apenas quando solicitado
function toggleHistorico() {
    const historico = $('#historicoSection');
    if (historico.is(':visible')) {
        historico.slideUp();
    } else {
        if (!historicoCarregado) {
            carregarHistorico();
        }
        historico.slideDown();
    }
}
```

### 2. Cache de Dados
```javascript
let cacheStatus = null;
let ultimaAtualizacaoCache = null;

function carregarStatusComCache() {
    const agora = new Date().getTime();
    const CACHE_DURATION = 10000; // 10 segundos
    
    if (cacheStatus && ultimaAtualizacaoCache && 
        (agora - ultimaAtualizacaoCache) < CACHE_DURATION) {
        atualizarInterface(cacheStatus);
        return;
    }
    
    carregarStatus();
}
```

### 3. Debounce para Atualizações
```javascript
let timeoutAtualizacao;

function atualizarComDebounce() {
    clearTimeout(timeoutAtualizacao);
    timeoutAtualizacao = setTimeout(() => {
        carregarStatus();
    }, 1000);
}
```

---

## Tratamento de Erros

### 1. Estados de Erro
```javascript
function mostrarEstadoErro(tipo, mensagem) {
    const errorStates = {
        'network': {
            icon: 'fas fa-wifi',
            title: 'Erro de Conexão',
            description: 'Verifique sua conexão com a internet'
        },
        'session': {
            icon: 'fas fa-user-times',
            title: 'Sessão Expirada',
            description: 'Sua sessão expirou. Faça login novamente'
        },
        'server': {
            icon: 'fas fa-server',
            title: 'Erro do Servidor',
            description: 'Tente novamente em alguns minutos'
        }
    };
    
    const state = errorStates[tipo] || errorStates['server'];
    
    $('#statusIcon').attr('class', state.icon + ' text-danger');
    $('#statusTitle').text(state.title);
    $('#statusDescription').text(mensagem || state.description);
}
```

### 2. Retry Automático
```javascript
let tentativasReconexao = 0;
const MAX_TENTATIVAS = 3;

function tentarReconexao() {
    if (tentativasReconexao < MAX_TENTATIVAS) {
        tentativasReconexao++;
        setTimeout(() => {
            carregarStatus();
        }, 5000 * tentativasReconexao); // Backoff exponencial
    } else {
        mostrarEstadoErro('network', 'Não foi possível conectar ao servidor');
    }
}
```

---

## Integração com Backend

### 1. WebSocket (Opcional)
```javascript
// Para atualizações em tempo real
if ('WebSocket' in window) {
    const ws = new WebSocket(`ws://localhost:8080/ws/adesao/${sessionId}`);
    
    ws.onmessage = function(event) {
        const data = JSON.parse(event.data);
        if (data.type === 'status_update') {
            atualizarInterface(data.payload);
        }
    };
    
    ws.onerror = function() {
        // Fallback para polling
        iniciarAtualizacaoAutomatica();
    };
}
```

### 2. Server-Sent Events
```javascript
// Alternativa ao WebSocket
if ('EventSource' in window) {
    const eventSource = new EventSource(`/rh/colaboradores/adesao/events/${sessionId}`);
    
    eventSource.onmessage = function(event) {
        const data = JSON.parse(event.data);
        atualizarInterface(data);
    };
    
    eventSource.onerror = function() {
        eventSource.close();
        iniciarAtualizacaoAutomatica();
    };
}
```

---

## Métricas e Analytics

### 1. Tracking de Eventos
```javascript
function trackEvent(evento, dados = {}) {
    // Google Analytics ou similar
    if (typeof gtag !== 'undefined') {
        gtag('event', evento, {
            event_category: 'Adesao_Status',
            session_id: sessionId,
            ...dados
        });
    }
    
    // Analytics interno
    $.post('/api/analytics/track', {
        evento: evento,
        sessionId: sessionId,
        timestamp: new Date().toISOString(),
        dados: dados
    });
}

// Exemplos de uso
trackEvent('status_page_loaded');
trackEvent('status_updated', { novo_status: 'APROVADO' });
trackEvent('comprovante_impresso');
```

### 2. Tempo na Página
```javascript
let tempoInicio = new Date().getTime();

window.addEventListener('beforeunload', function() {
    const tempoTotal = new Date().getTime() - tempoInicio;
    trackEvent('tempo_na_pagina', { duracao_ms: tempoTotal });
});
```

---

**Versão**: 1.0  
**Data**: Janeiro 2025  
**Status**: Documentação Completa da Página de Status