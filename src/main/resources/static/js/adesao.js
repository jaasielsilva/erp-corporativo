
/**
 * JavaScript para Modal de Adesão de Benefícios
 * Sistema ERP Corporativo - Módulo RH
 */

// Variáveis globais
let valoresPlanosJson = {};

// Inicialização quando o documento estiver pronto
document.addEventListener('DOMContentLoaded', function() {
    // Capturar valores dos planos do Thymeleaf (se disponível)
    if (typeof window.valoresPlanosJson !== 'undefined') {
        valoresPlanosJson = window.valoresPlanosJson;
    }
    
    // Event listeners
    setupEventListeners();
});

function setupEventListeners() {
    // Listener para mudança no select de plano
    const planoSelect = document.getElementById('planoSaude');
    if (planoSelect) {
        planoSelect.addEventListener('change', calcularValores);
    }
    
    // Listener para mudança na quantidade de dependentes
    const dependentesInput = document.getElementById('dependentes');
    if (dependentesInput) {
        dependentesInput.addEventListener('input', calcularValores);
    }
    
    // Listener para submit do formulário
    const formAdesao = document.getElementById('formAdesao');
    if (formAdesao) {
        formAdesao.addEventListener('submit', function(e) {
            e.preventDefault();
            salvarAdesao();
        });
    }
}

function calcularValores() {
    const planoSelect = document.getElementById('planoSaude');
    const dependentesInput = document.getElementById('dependentes');
    const resumoDiv = document.getElementById('resumoValores');
    
    if (!planoSelect || !dependentesInput || !resumoDiv) return;
    
    const planoId = planoSelect.value;
    const qtdDependentes = parseInt(dependentesInput.value) || 0;
    
    if (!planoId) {
        resumoDiv.style.display = 'none';
        return;
    }
    
    // Obter valores do plano selecionado
    const selectedOption = planoSelect.options[planoSelect.selectedIndex];
    const valorTitular = parseFloat(selectedOption.getAttribute('data-valor-titular')) || 0;
    const valorDependente = parseFloat(selectedOption.getAttribute('data-valor-dependente')) || 0;
    
    // Calcular valores
    const valorDependentes = valorDependente * qtdDependentes;
    const valorTotal = valorTitular + valorDependentes;
    
    // Atualizar interface
    document.getElementById('valorTitular').textContent = formatarMoeda(valorTitular);
    document.getElementById('valorDependentes').textContent = formatarMoeda(valorDependentes);
    document.getElementById('valorTotal').textContent = formatarMoeda(valorTotal);
    
    // Mostrar resumo
    resumoDiv.style.display = 'block';
}

function formatarMoeda(valor) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(valor);
}

function salvarAdesao() {
    const form = document.getElementById('formAdesao');
    if (!form) return;
    
    // Validar formulário
    if (!validarFormulario()) {
        return;
    }
    
    // Coletar dados do formulário
    const dadosAdesao = {
        colaboradorId: document.getElementById('colaborador').value,
        planoId: document.getElementById('planoSaude').value,
        tipoAdesao: document.getElementById('tipoAdesao').value,
        dataVigencia: document.getElementById('dataVigencia').value,
        quantidadeDependentes: parseInt(document.getElementById('dependentes').value) || 0,
        observacoes: document.getElementById('observacoes').value || ''
    };
    
    // Mostrar loading
    mostrarLoading(true);
    
    // Enviar via AJAX
    fetch('/rh/beneficios/adesao', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(dadosAdesao)
    })
    .then(response => response.json())
    .then(data => {
        mostrarLoading(false);
        
        if (data.success) {
            mostrarNotificacao('Adesão salva com sucesso!', 'success');
            fecharModalAdesao();
            // Recarregar a página para atualizar a lista
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            mostrarNotificacao(data.message || 'Erro ao salvar adesão', 'error');
        }
    })
    .catch(error => {
        mostrarLoading(false);
        console.error('Erro:', error);
        mostrarNotificacao('Erro interno do servidor', 'error');
    });
}

function validarFormulario() {
    const campos = [
        { id: 'colaborador', nome: 'Colaborador' },
        { id: 'planoSaude', nome: 'Plano de Saúde' },
        { id: 'tipoAdesao', nome: 'Tipo de Adesão' },
        { id: 'dataVigencia', nome: 'Data de Vigência' }
    ];
    
    for (const campo of campos) {
        const elemento = document.getElementById(campo.id);
        if (!elemento || !elemento.value.trim()) {
            mostrarNotificacao(`${campo.nome} é obrigatório`, 'error');
            elemento?.focus();
            return false;
        }
    }
    
    // Validar data de vigência (não pode ser no passado)
    const dataVigencia = new Date(document.getElementById('dataVigencia').value);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    if (dataVigencia < hoje) {
        mostrarNotificacao('Data de vigência não pode ser no passado', 'error');
        document.getElementById('dataVigencia').focus();
        return false;
    }
    
    return true;
}

function mostrarLoading(mostrar) {
    const submitBtn = document.querySelector('#formAdesao button[type="submit"]');
    if (!submitBtn) return;
    
    if (mostrar) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';
    } else {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Salvar';
    }
}

function mostrarNotificacao(mensagem, tipo) {
    // Verificar se existe função de notificação global
    if (typeof showNotification === 'function') {
        showNotification(mensagem, tipo);
    } else {
        // Fallback para alert
        alert(mensagem);
    }
}

function abrirModalAdesao() {
    const modal = document.getElementById('modalAdesao');
    if (modal) {
        modal.style.display = 'block';
        
        // Limpar formulário
        limparFormulario();
        
        // Focar no primeiro campo
        const colaboradorSelect = document.getElementById('colaborador');
        if (colaboradorSelect) {
            colaboradorSelect.focus();
        }
    }
}

function fecharModalAdesao() {
    const modal = document.getElementById('modalAdesao');
    if (modal) {
        modal.style.display = 'none';
        limparFormulario();
    }
}

function limparFormulario() {
    const form = document.getElementById('formAdesao');
    if (form) {
        form.reset();
        
        // Esconder resumo de valores
        const resumoDiv = document.getElementById('resumoValores');
        if (resumoDiv) {
            resumoDiv.style.display = 'none';
        }
    }
}

function verDetalhesAdesao(id) {
    if (id && id > 0) {
        // Implementar visualização de detalhes
        window.location.href = `/rh/beneficios/adesao/${id}`;
    } else {
        mostrarNotificacao('ID da adesão inválido', 'error');
    }
}

// Fechar modal clicando fora dele
window.onclick = function(event) {
    const modal = document.getElementById('modalAdesao');
    if (event.target === modal) {
        fecharModalAdesao();
    }
};

// Fechar modal com ESC
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        fecharModalAdesao();
    }
});

