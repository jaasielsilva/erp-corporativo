/**
 * JavaScript para funcionalidades avançadas do formulário de colaboradores
 * Inclui máscaras de input e busca automática de CEP
 */

// Aguarda o carregamento completo da página
document.addEventListener('DOMContentLoaded', function() {
    initializeFormMasks();
    initializeCepSearch();
    initializeFormValidation();
});

/**
 * Inicializa as máscaras de input
 */
function initializeFormMasks() {
    // Máscara para CPF
    const cpfInput = document.getElementById('cpf');
    if (cpfInput) {
        cpfInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = value.replace(/(\d{3})(\d)/, '$1.$2');
            value = value.replace(/(\d{3})(\d)/, '$1.$2');
            value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            e.target.value = value;
        });
    }

    // Máscara para RG
    const rgInput = document.getElementById('rg');
    if (rgInput) {
        rgInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = value.replace(/(\d{2})(\d)/, '$1.$2');
            value = value.replace(/(\d{3})(\d)/, '$1.$2');
            value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            e.target.value = value;
        });
    }

    // Máscara para telefone
    const telefoneInput = document.getElementById('telefone');
    if (telefoneInput) {
        telefoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 10) {
                value = value.replace(/(\d{2})(\d)/, '($1) $2');
                value = value.replace(/(\d{4})(\d)/, '$1-$2');
            } else {
                value = value.replace(/(\d{2})(\d)/, '($1) $2');
                value = value.replace(/(\d{5})(\d)/, '$1-$2');
            }
            e.target.value = value;
        });
    }

    // Máscara para CEP
    const cepInput = document.getElementById('cep');
    if (cepInput) {
        cepInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = value.replace(/(\d{5})(\d)/, '$1-$2');
            e.target.value = value;
        });
    }

    // Máscara para salário
    const salarioInput = document.getElementById('salario');
    if (salarioInput) {
        salarioInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = (value / 100).toFixed(2);
            value = value.replace('.', ',');
            value = value.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
            e.target.value = 'R$ ' + value;
        });
        
        // Converter para número antes do envio do formulário
        const form = salarioInput.closest('form');
        if (form) {
            form.addEventListener('submit', function() {
                let currencyValue = salarioInput.value;
                if (currencyValue) {
                    // Remove R$, espaços, pontos e substitui vírgula por ponto
                    let numericValue = currencyValue
                        .replace(/R\$\s?/g, '')
                        .replace(/\./g, '')
                        .replace(',', '.');
                    salarioInput.value = numericValue;
                }
            });
        }
    }
}

/**
 * Inicializa a busca automática de CEP
 */
function initializeCepSearch() {
    const cepInput = document.getElementById('cep');
    if (!cepInput) return;

    cepInput.addEventListener('blur', function() {
        const cep = this.value.replace(/\D/g, '');
        
        if (cep.length === 8) {
            searchCep(cep);
        }
    });
}

/**
 * Busca informações do CEP na API ViaCEP
 * @param {string} cep - CEP para busca
 */
function searchCep(cep) {
    const loadingIndicator = showLoadingIndicator();
    
    fetch(`https://viacep.com.br/ws/${cep}/json/`)
        .then(response => response.json())
        .then(data => {
            hideLoadingIndicator(loadingIndicator);
            
            if (data.erro) {
                showCepError('CEP não encontrado');
                return;
            }
            
            fillAddressFields(data);
            showCepSuccess('Endereço encontrado!');
        })
        .catch(error => {
            hideLoadingIndicator(loadingIndicator);
            showCepError('Erro ao buscar CEP. Verifique sua conexão.');
            console.error('Erro na busca do CEP:', error);
        });
}

/**
 * Preenche os campos de endereço com os dados do CEP
 * @param {Object} data - Dados retornados da API
 */
function fillAddressFields(data) {
    const fields = {
        'logradouro': data.logradouro,
        'bairro': data.bairro,
        'cidade': data.localidade,
        'estado': data.uf
    };
    
    Object.keys(fields).forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field && fields[fieldId]) {
            field.value = fields[fieldId];
            field.classList.add('auto-filled');
            
            // Remove a classe após a animação
            setTimeout(() => {
                field.classList.remove('auto-filled');
            }, 2000);
        }
    });
    
    // Foca no campo número
    const numeroField = document.getElementById('numero');
    if (numeroField) {
        numeroField.focus();
    }
}

/**
 * Mostra indicador de carregamento
 */
function showLoadingIndicator() {
    const cepInput = document.getElementById('cep');
    const indicator = document.createElement('div');
    indicator.className = 'cep-loading';
    indicator.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Buscando...';
    
    cepInput.parentNode.appendChild(indicator);
    return indicator;
}

/**
 * Remove indicador de carregamento
 */
function hideLoadingIndicator(indicator) {
    if (indicator && indicator.parentNode) {
        indicator.parentNode.removeChild(indicator);
    }
}

/**
 * Mostra mensagem de erro do CEP
 */
function showCepError(message) {
    showCepMessage(message, 'error');
}

/**
 * Mostra mensagem de sucesso do CEP
 */
function showCepSuccess(message) {
    showCepMessage(message, 'success');
}

/**
 * Mostra mensagem relacionada ao CEP
 */
function showCepMessage(message, type) {
    // Remove mensagens anteriores
    const existingMessages = document.querySelectorAll('.cep-message');
    existingMessages.forEach(msg => msg.remove());
    
    const cepInput = document.getElementById('cep');
    const messageDiv = document.createElement('div');
    messageDiv.className = `cep-message cep-message-${type}`;
    messageDiv.textContent = message;
    
    cepInput.parentNode.appendChild(messageDiv);
    
    // Remove a mensagem após 3 segundos
    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.parentNode.removeChild(messageDiv);
        }
    }, 3000);
}

/**
 * Inicializa validações do formulário
 */
function initializeFormValidation() {
    // Validação de CPF
    const cpfInput = document.getElementById('cpf');
    if (cpfInput) {
        cpfInput.addEventListener('blur', function() {
            const cpf = this.value.replace(/\D/g, '');
            if (cpf.length === 11 && !isValidCPF(cpf)) {
                this.classList.add('invalid');
                showFieldError(this, 'CPF inválido');
            } else {
                this.classList.remove('invalid');
                hideFieldError(this);
            }
        });
    }
    
    // Validação de email
    const emailInput = document.getElementById('email');
    if (emailInput) {
        emailInput.addEventListener('blur', function() {
            if (this.value && !isValidEmail(this.value)) {
                this.classList.add('invalid');
                showFieldError(this, 'Email inválido');
            } else {
                this.classList.remove('invalid');
                hideFieldError(this);
            }
        });
    }
}

/**
 * Valida CPF
 * @param {string} cpf - CPF para validação
 * @returns {boolean} - True se válido
 */
function isValidCPF(cpf) {
    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
        return false;
    }
    
    let sum = 0;
    for (let i = 0; i < 9; i++) {
        sum += parseInt(cpf.charAt(i)) * (10 - i);
    }
    
    let remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) remainder = 0;
    if (remainder !== parseInt(cpf.charAt(9))) return false;
    
    sum = 0;
    for (let i = 0; i < 10; i++) {
        sum += parseInt(cpf.charAt(i)) * (11 - i);
    }
    
    remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) remainder = 0;
    
    return remainder === parseInt(cpf.charAt(10));
}

/**
 * Valida email
 * @param {string} email - Email para validação
 * @returns {boolean} - True se válido
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Mostra erro em campo específico
 */
function showFieldError(field, message) {
    hideFieldError(field);
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.textContent = message;
    
    field.parentNode.appendChild(errorDiv);
}

/**
 * Remove erro de campo específico
 */
function hideFieldError(field) {
    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }
}