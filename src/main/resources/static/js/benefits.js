/**
 * JavaScript para Modal de Adesão de Benefícios
 * Sistema ERP Corporativo - Módulo RH
 */

// Variáveis globais
let currentStep = 1;
let selectedBenefit = null;
let benefitData = {};

// Configurações dos benefícios
const benefitConfig = {
    'plano-saude': {
        title: 'Plano de Saúde',
        icon: 'fas fa-heartbeat',
        formId: 'form-plano-saude',
        endpoint: '/rh/beneficios/adesao',
        requiredFields: ['colaboradorId', 'planoId', 'tipoAdesao', 'dataVigencia']
    },
    'vale-refeicao': {
        title: 'Vale Refeição',
        icon: 'fas fa-utensils',
        formId: 'form-vale-refeicao',
        endpoint: '/rh/beneficios/vale-refeicao/salvar',
        requiredFields: ['colaboradorId', 'valorMensal', 'dataInicio']
    },
    'vale-transporte': {
        title: 'Vale Transporte',
        icon: 'fas fa-bus',
        formId: 'form-vale-transporte',
        endpoint: '/rh/beneficios/vale-transporte/salvar',
        requiredFields: ['colaboradorId', 'valorMensal', 'dataInicio']
    },
    'vale-alimentacao': {
        title: 'Vale Alimentação',
        icon: 'fas fa-shopping-cart',
        formId: 'form-vale-alimentacao',
        endpoint: '/rh/beneficios/vale-alimentacao/salvar',
        requiredFields: ['colaboradorId', 'valorMensal', 'dataInicio']
    }
};

/**
 * Inicializa o modal de benefícios
 */
function initBenefitsModal() {
    // Event listeners para cards de benefícios
    document.querySelectorAll('.benefit-card').forEach(card => {
        card.addEventListener('click', function() {
            selectBenefit(this.dataset.benefit);
        });
    });

    // Event listener para cálculo automático do valor total (plano de saúde)
    const planoSelect = document.getElementById('planoSaudeId');
    const dependentesInput = document.getElementById('quantidadeDependentes');
    
    if (planoSelect) {
        planoSelect.addEventListener('change', calculateTotalValue);
    }
    
    if (dependentesInput) {
        dependentesInput.addEventListener('input', calculateTotalValue);
    }

    // Configurar data mínima como hoje
    setMinimumDates();
}

/**
 * Abre o modal de adesão
 */
function abrirModalAdesao() {
    resetModal();
    $('#modalAdesao').modal('show');
}

/**
 * Fecha o modal de adesão
 */
function fecharModalAdesao() {
    $('#modalAdesao').modal('hide');
    resetModal();
}

/**
 * Reseta o modal para o estado inicial
 */
function resetModal() {
    currentStep = 1;
    selectedBenefit = null;
    benefitData = {};
    
    // Mostrar etapa 1 e esconder etapa 2
    document.getElementById('step-benefit-type').style.display = 'block';
    document.getElementById('step-form').style.display = 'none';
    
    // Remover seleção dos cards
    document.querySelectorAll('.benefit-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Esconder todos os formulários específicos e reabilitar campos
    document.querySelectorAll('.benefit-form').forEach(form => {
        form.style.display = 'none';
        // Reabilitar todos os campos para permitir reset correto
        const requiredFields = form.querySelectorAll('[required]');
        requiredFields.forEach(field => {
            field.disabled = false;
        });
    });
    
    // Limpar formulário
    const form = document.getElementById('formAdesao');
    if (form) {
        form.reset();
        clearValidation();
    }
    
    // Esconder botão salvar
    document.getElementById('btnSalvarAdesao').style.display = 'none';
}

/**
 * Seleciona um tipo de benefício
 */
function selectBenefit(benefitType) {
    selectedBenefit = benefitType;
    
    // Atualizar visual dos cards
    document.querySelectorAll('.benefit-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    document.querySelector(`[data-benefit="${benefitType}"]`).classList.add('selected');
    
    // Aguardar um pouco para a animação e então avançar
    setTimeout(() => {
        nextStep();
    }, 300);
}

/**
 * Avança para a próxima etapa
 */
function nextStep() {
    if (currentStep === 1 && selectedBenefit) {
        currentStep = 2;
        showFormStep();
    }
}

/**
 * Volta para a etapa anterior
 */
function voltarEtapa() {
    if (currentStep === 2) {
        currentStep = 1;
        
        // Esconder etapa 2
        document.getElementById('step-form').style.display = 'none';
        
        // Esconder todos os formulários e desabilitar campos obrigatórios
        document.querySelectorAll('.benefit-form').forEach(form => {
            form.style.display = 'none';
            const requiredFields = form.querySelectorAll('[required]');
            requiredFields.forEach(field => {
                field.disabled = true;
            });
        });
        
        // Mostrar etapa 1
        document.getElementById('step-benefit-type').style.display = 'block';
        
        // Esconder botão salvar
        document.getElementById('btnSalvarAdesao').style.display = 'none';
    }
}

/**
 * Mostra o formulário da etapa 2
 */
function showFormStep() {
    const config = benefitConfig[selectedBenefit];
    
    if (!config) {
        console.error('Configuração não encontrada para o benefício:', selectedBenefit);
        return;
    }
    
    // Atualizar título
    document.getElementById('selected-benefit-title').textContent = config.title;
    
    // Definir tipo de benefício no campo hidden
    document.getElementById('tipoBeneficio').value = selectedBenefit;
    
    // Esconder etapa 1 e mostrar etapa 2
    document.getElementById('step-benefit-type').style.display = 'none';
    document.getElementById('step-form').style.display = 'block';
    
    // Esconder todos os formulários e desabilitar campos obrigatórios
    const allForms = document.querySelectorAll('.benefit-form');
    allForms.forEach(form => {
        form.style.display = 'none';
        // Desabilitar campos obrigatórios em formulários ocultos
        const requiredFields = form.querySelectorAll('[required]');
        requiredFields.forEach(field => {
            field.disabled = true;
        });
    });
    
    // Mostrar formulário específico e habilitar seus campos
    const activeForm = document.getElementById(config.formId);
    activeForm.style.display = 'block';
    
    // Habilitar campos obrigatórios no formulário ativo
    const activeRequiredFields = activeForm.querySelectorAll('[required]');
    activeRequiredFields.forEach(field => {
        field.disabled = false;
    });
    
    // Mostrar botão salvar
    document.getElementById('btnSalvarAdesao').style.display = 'flex';
    
    // Focar no primeiro campo
    const firstInput = document.querySelector('#step-form .form-control');
    if (firstInput) {
        firstInput.focus();
    }
}

/**
 * Calcula o valor total para plano de saúde
 */
function calculateTotalValue() {
    const planoSelect = document.getElementById('planoSaudeId');
    const dependentesInput = document.getElementById('quantidadeDependentes');
    const valorTotalInput = document.getElementById('valorTotal');
    
    if (!planoSelect || !dependentesInput || !valorTotalInput) {
        return;
    }
    
    const selectedOption = planoSelect.options[planoSelect.selectedIndex];
    const valorBase = selectedOption.dataset.valor || 0;
    const quantidadeDependentes = parseInt(dependentesInput.value) || 0;
    
    // Calcular valor total (valor base + 50% por dependente)
    const valorDependentes = valorBase * 0.5 * quantidadeDependentes;
    const valorTotal = parseFloat(valorBase) + valorDependentes;
    
    valorTotalInput.value = valorTotal.toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

/**
 * Define datas mínimas como hoje
 */
function setMinimumDates() {
    const today = new Date().toISOString().split('T')[0];
    const dateInputs = [
        'dataVigencia',
        'dataInicioVR',
        'dataInicioVT',
        'dataInicioVA'
    ];
    
    dateInputs.forEach(inputId => {
        const input = document.getElementById(inputId);
        if (input) {
            input.min = today;
            if (!input.value) {
                input.value = today;
            }
        }
    });
}

/**
 * Valida o formulário antes do envio
 */
function validateForm() {
    const config = benefitConfig[selectedBenefit];
    if (!config) {
        return false;
    }
    
    let isValid = true;
    const errors = [];
    
    // Validar campos obrigatórios
    config.requiredFields.forEach(fieldName => {
        const field = document.querySelector(`[name="${fieldName}"]`);
        if (field) {
            const value = field.value.trim();
            if (!value) {
                isValid = false;
                field.classList.add('is-invalid');
                errors.push(`O campo ${getFieldLabel(fieldName)} é obrigatório.`);
            } else {
                field.classList.remove('is-invalid');
                field.classList.add('is-valid');
            }
        }
    });
    
    // Validações específicas
    if (selectedBenefit === 'plano-saude') {
        const dependentes = document.getElementById('quantidadeDependentes');
        if (dependentes && (parseInt(dependentes.value) < 0 || parseInt(dependentes.value) > 10)) {
            isValid = false;
            dependentes.classList.add('is-invalid');
            errors.push('A quantidade de dependentes deve estar entre 0 e 10.');
        }
    }
    
    // Validar valores monetários
    const valorInputs = document.querySelectorAll('input[type="number"][step="0.01"]');
    valorInputs.forEach(input => {
        if (input.value && parseFloat(input.value) <= 0) {
            isValid = false;
            input.classList.add('is-invalid');
            errors.push('Os valores devem ser maiores que zero.');
        }
    });
    
    // Mostrar erros se houver
    if (errors.length > 0) {
        showNotification('Erro de Validação', errors.join('\n'), 'error');
    }
    
    return isValid;
}

/**
 * Obtém o label de um campo
 */
function getFieldLabel(fieldName) {
    const labels = {
        'colaboradorId': 'Colaborador',
        'planoId': 'Plano de Saúde',
        'tipoAdesao': 'Tipo de Adesão',
        'dataVigencia': 'Data de Vigência',
        'valorMensal': 'Valor Mensal',
        'dataInicio': 'Data de Início'
    };
    
    return labels[fieldName] || fieldName;
}

/**
 * Limpa validações visuais
 */
function clearValidation() {
    document.querySelectorAll('.form-control').forEach(input => {
        input.classList.remove('is-valid', 'is-invalid');
    });
}

/**
 * Salva a adesão
 */
function salvarAdesao() {
    if (!validateForm()) {
        return;
    }
    
    const config = benefitConfig[selectedBenefit];
    const form = document.getElementById('formAdesao');
    const formData = new FormData(form);
    
    // Converter FormData para objeto JSON
    const data = {};
    for (let [key, value] of formData.entries()) {
        data[key] = value;
    }
    
    // Adicionar tipo de benefício
    data.tipoBeneficio = selectedBenefit;
    
    // Converter valores numéricos
    if (data.colaboradorId) data.colaboradorId = parseInt(data.colaboradorId);
    if (data.planoId) data.planoId = parseInt(data.planoId);
    if (data.quantidadeDependentes) data.quantidadeDependentes = parseInt(data.quantidadeDependentes) || 0;
    if (data.valorMensal) data.valorMensal = parseFloat(data.valorMensal) || 0;
    
    // Processar checkbox
    data.processarImediatamente = form.querySelector('#processarImediatamente')?.checked || false;
    
    console.log('Enviando dados da adesão:', data);
    
    // Mostrar loading
    const btn = document.getElementById('btnSalvarAdesao');
    btn.classList.add('loading');
    btn.disabled = true;
    
    // Enviar dados
    fetch(config.endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            showNotification('Sucesso', 'Adesão salva com sucesso!', 'success');
            fecharModalAdesao();
            
            // Recarregar a página ou atualizar a tabela
            setTimeout(() => {
                location.reload();
            }, 1500);
        } else {
            throw new Error(data.message || 'Erro ao salvar adesão');
        }
    })
    .catch(error => {
        console.error('Erro ao salvar adesão:', error);
        showNotification('Erro', error.message || 'Erro ao salvar adesão. Tente novamente.', 'error');
    })
    .finally(() => {
        // Remover loading
        btn.classList.remove('loading');
        btn.disabled = false;
    });
}

/**
 * Mostra notificação
 */
function showNotification(title, message, type = 'info') {
    // Implementação básica - pode ser melhorada com uma biblioteca de notificações
    const alertClass = type === 'success' ? 'alert-success' : 
                      type === 'error' ? 'alert-danger' : 'alert-info';
    
    const notification = document.createElement('div');
    notification.className = `alert ${alertClass} alert-dismissible fade show position-fixed`;
    notification.style.cssText = `
        top: 20px;
        right: 20px;
        z-index: 9999;
        min-width: 300px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.15);
    `;
    
    notification.innerHTML = `
        <strong>${title}</strong><br>
        ${message.replace(/\n/g, '<br>')}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(notification);
    
    // Auto remover após 5 segundos
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
    
    // Event listener para fechar
    notification.querySelector('.btn-close').addEventListener('click', () => {
        notification.remove();
    });
}

/**
 * Event listeners para fechar modal
 */
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar modal
    initBenefitsModal();
    
    // Fechar modal ao clicar fora
    document.getElementById('modalAdesao').addEventListener('click', function(e) {
        if (e.target === this) {
            fecharModalAdesao();
        }
    });
    
    // Fechar modal com ESC
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && document.getElementById('modalAdesao').style.display === 'block') {
            fecharModalAdesao();
        }
    });
});

// Exportar funções para uso global
window.abrirModalAdesao = abrirModalAdesao;
window.fecharModalAdesao = fecharModalAdesao;
window.salvarAdesao = salvarAdesao;
window.voltarEtapa = voltarEtapa;