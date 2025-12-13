// Máscaras de entrada para formulário de edição de colaborador
document.addEventListener('DOMContentLoaded', function() {
    console.log('colaborador-edit-form.js carregado');
    
    // Aplicar máscaras nos campos
    aplicarMascaras();
    
    // Configurar busca de CEP
    configurarBuscaCEP();
    
    // Configurar validações em tempo real
    configurarValidacoes();
    
    // Configurar conversão de salário antes do envio
    configurarConversaoSalario();
    
    // Configurar conversão de datas
    configurarConversaoDatas();
    
    // Configurar validações client-side
    configurarValidacoesClientSide();
    
    // Melhorar UX do formulário
    melhorarUXFormulario();
});

function aplicarMascaras() {
    // Máscara para CPF (readonly, mas mantém formatação)
    const cpfInput = document.getElementById('cpf');
    if (cpfInput && cpfInput.value) {
        cpfInput.value = formatarCPF(cpfInput.value);
    }
    
    // Máscara para RG
    const rgInput = document.getElementById('rg');
    if (rgInput) {
        rgInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 9) {
                value = value.replace(/(\d{2})(\d{3})(\d{3})(\d{1})/, '$1.$2.$3-$4');
            }
            e.target.value = value;
        });
    }
    
    // Máscara para telefone
    const telefoneInput = document.getElementById('telefone');
    if (telefoneInput) {
        telefoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 11) {
                if (value.length <= 10) {
                    value = value.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
                } else {
                    value = value.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
                }
            }
            e.target.value = value;
        });
    }
    
    // Máscara para CEP
    const cepInput = document.getElementById('cep');
    if (cepInput) {
        cepInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 8) {
                value = value.replace(/(\d{5})(\d{3})/, '$1-$2');
            }
            e.target.value = value;
        });
    }
    
    // Máscara para salário
    const salarioInput = document.getElementById('salario');
    if (salarioInput) {
        salarioInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^\d,]/g, '');
            
            // Remove vírgulas extras
            const parts = value.split(',');
            if (parts.length > 2) {
                value = parts[0] + ',' + parts.slice(1).join('');
            }
            
            // Limita casas decimais
            if (parts[1] && parts[1].length > 2) {
                parts[1] = parts[1].substring(0, 2);
                value = parts.join(',');
            }
            
            // Adiciona pontos para milhares
            if (parts[0]) {
                parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.');
                value = parts.join(',');
            }
            
            e.target.value = 'R$ ' + value;
        });
    }
}

function configurarBuscaCEP() {
    const cepInput = document.getElementById('cep');
    const loadingIndicator = document.getElementById('cep-loading');
    
    if (cepInput) {
        cepInput.addEventListener('blur', function() {
            const cep = this.value.replace(/\D/g, '');
            
            if (cep.length === 8) {
                loadingIndicator.style.display = 'block';
                
                fetch(`https://viacep.com.br/ws/${cep}/json/`)
                    .then(response => response.json())
                    .then(data => {
                        if (!data.erro) {
                            document.getElementById('logradouro').value = data.logradouro || '';
                            document.getElementById('bairro').value = data.bairro || '';
                            document.getElementById('cidade').value = data.localidade || '';
                            document.getElementById('estado').value = data.uf || '';
                            
                            // Foca no campo número
                            document.getElementById('numero').focus();
                        } else {
                            alert('CEP não encontrado!');
                        }
                    })
                    .catch(error => {
                        console.error('Erro ao buscar CEP:', error);
                        alert('Erro ao buscar CEP. Verifique sua conexão.');
                    })
                    .finally(() => {
                        loadingIndicator.style.display = 'none';
                    });
            }
        });
    }
}

function configurarValidacoes() {
    // Validação de email em tempo real
    const emailInput = document.getElementById('email');
    if (emailInput) {
        emailInput.addEventListener('blur', function() {
            const email = this.value;
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            
            if (email && !emailRegex.test(email)) {
                this.style.borderColor = '#e74c3c';
                this.style.boxShadow = '0 0 5px rgba(231, 76, 60, 0.3)';
            } else {
                this.style.borderColor = '';
                this.style.boxShadow = '';
            }
        });
    }
    
    // Validação de campos obrigatórios
    const camposObrigatorios = ['nome', 'email', 'departamento', 'dataAdmissao'];
    
    camposObrigatorios.forEach(campo => {
        const input = document.getElementById(campo);
        if (input) {
            input.addEventListener('blur', function() {
                if (!this.value.trim()) {
                    this.style.borderColor = '#e74c3c';
                    this.style.boxShadow = '0 0 5px rgba(231, 76, 60, 0.3)';
                } else {
                    this.style.borderColor = '';
                    this.style.boxShadow = '';
                }
            });
        }
    });
}

function configurarConversaoSalario() {
    const form = document.querySelector('form');
    const salarioInput = document.getElementById('salario');
    
    if (form && salarioInput) {
        // Formatar salário ao carregar a página se já houver valor
        if (salarioInput.value && !salarioInput.value.includes('R$')) {
            const valorNumerico = parseFloat(salarioInput.value);
            if (!isNaN(valorNumerico)) {
                salarioInput.value = 'R$ ' + valorNumerico.toLocaleString('pt-BR', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                });
            }
        }
        
        form.addEventListener('submit', function(e) {
            console.log('=== FORMULÁRIO SENDO ENVIADO ===');
            console.log('Action:', form.action);
            console.log('Method:', form.method);
            
            // Converte o salário para formato numérico antes do envio
            let salarioValue = salarioInput.value;
            
            if (salarioValue && salarioValue.trim() !== '') {
                // Remove R$, espaços e pontos (separadores de milhares)
                salarioValue = salarioValue.replace(/R\$\s?/g, '')
                                         .replace(/\./g, '')
                                         .replace(/,/g, '.');
                
                // Verifica se é um número válido
                if (isNaN(parseFloat(salarioValue))) {
                    salarioValue = '0';
                }
            } else {
                salarioValue = '0';
            }
            
            // Atualiza o valor do input original diretamente
            salarioInput.value = salarioValue;
            
            console.log('Salário convertido:', salarioValue);
            console.log('Formulário será enviado...');
            
            // Validação final antes do envio
            if (!validarFormularioCompleto()) {
                e.preventDefault();
                return false;
            }
        });
    } else {
        console.error('Form ou salarioInput não encontrados!');
    }
}

function configurarConversaoDatas() {
    const form = document.querySelector('form');
    const dataAdmissaoInput = document.getElementById('dataAdmissao');
    const dataNascimentoInput = document.getElementById('dataNascimento');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            // Converter data de admissão se necessário
            if (dataAdmissaoInput && dataAdmissaoInput.value) {
                const dataAdmissao = dataAdmissaoInput.value;
                if (dataAdmissao.includes('/')) {
                    // Converter de dd/mm/yyyy para yyyy-mm-dd
                    const partes = dataAdmissao.split('/');
                    if (partes.length === 3) {
                        const dataISO = `${partes[2]}-${partes[1].padStart(2, '0')}-${partes[0].padStart(2, '0')}`;
                        dataAdmissaoInput.value = dataISO;
                    }
                }
            }
            
            // Converter data de nascimento se necessário
            if (dataNascimentoInput && dataNascimentoInput.value) {
                const dataNascimento = dataNascimentoInput.value;
                if (dataNascimento.includes('/')) {
                    // Converter de dd/mm/yyyy para yyyy-mm-dd
                    const partes = dataNascimento.split('/');
                    if (partes.length === 3) {
                        const dataISO = `${partes[2]}-${partes[1].padStart(2, '0')}-${partes[0].padStart(2, '0')}`;
                        dataNascimentoInput.value = dataISO;
                    }
                }
            }
        });
    }
}

function formatarCPF(cpf) {
    const numericCPF = cpf.replace(/\D/g, '');
    return numericCPF.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

// Configurar validações client-side
function configurarValidacoesClientSide() {
    const form = document.getElementById('colaboradorForm');
    if (!form) return;
    
    // Validação de CPF
    const cpfInput = document.getElementById('cpf');
    if (cpfInput) {
        cpfInput.addEventListener('blur', function() {
            validarCPF(this.value, this);
        });
    }
    
    // Validação de email
    const emailInput = document.getElementById('email');
    if (emailInput) {
        emailInput.addEventListener('blur', function() {
            validarEmail(this.value, this);
        });
    }
    
    // Validação de data de admissão
    const dataAdmissaoInput = document.getElementById('dataAdmissao');
    if (dataAdmissaoInput) {
        dataAdmissaoInput.addEventListener('change', function() {
            validarDataAdmissao(this.value, this);
        });
    }
    
    // Validação de salário
    const salarioInput = document.getElementById('salario');
    if (salarioInput) {
        salarioInput.addEventListener('blur', function() {
            validarSalario(this.value, this);
        });
    }
}

// Melhorar UX do formulário
function melhorarUXFormulario() {
    // Adicionar indicadores de carregamento
    const form = document.getElementById('colaboradorForm');
    if (form) {
        form.addEventListener('submit', function() {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';
            }
        });
    }
    
    // Adicionar tooltips informativos
    adicionarTooltips();
    
    // Configurar auto-save (opcional)
    // configurarAutoSave();
}

// Validação completa do formulário
function validarFormularioCompleto() {
    let isValid = true;
    const errors = [];
    
    // Validar campos obrigatórios
    const requiredFields = [
        { id: 'nome', name: 'Nome' },
        { id: 'cpf', name: 'CPF' },
        { id: 'email', name: 'Email' },
        { id: 'cargo', name: 'Cargo' },
        { id: 'departamento', name: 'Departamento' },
        { id: 'dataAdmissao', name: 'Data de Admissão' },
        { id: 'salario', name: 'Salário' }
    ];
    
    requiredFields.forEach(field => {
        const input = document.getElementById(field.id);
        if (input && !input.value.trim()) {
            isValid = false;
            errors.push(`${field.name} é obrigatório`);
            marcarCampoInvalido(input);
        }
    });
    
    // Validações específicas
    const cpfInput = document.getElementById('cpf');
    if (cpfInput && cpfInput.value && !validarCPF(cpfInput.value)) {
        isValid = false;
        errors.push('CPF inválido');
    }
    
    const emailInput = document.getElementById('email');
    if (emailInput && emailInput.value && !validarEmail(emailInput.value)) {
        isValid = false;
        errors.push('Email inválido');
    }
    
    const dataAdmissaoInput = document.getElementById('dataAdmissao');
    if (dataAdmissaoInput && dataAdmissaoInput.value && !validarDataAdmissao(dataAdmissaoInput.value)) {
        isValid = false;
        errors.push('Data de admissão não pode ser futura');
    }
    
    // Exibir erros se houver
    if (!isValid) {
        exibirErrosValidacao(errors);
    }
    
    return isValid;
}

// Validar CPF
function validarCPF(cpf, inputElement = null) {
    cpf = cpf.replace(/[^\d]/g, '');
    
    if (cpf.length !== 11) {
        if (inputElement) marcarCampoInvalido(inputElement, 'CPF deve ter 11 dígitos');
        return false;
    }
    
    // Verificar se todos os dígitos são iguais
    if (/^(\d)\1{10}$/.test(cpf)) {
        if (inputElement) marcarCampoInvalido(inputElement, 'CPF inválido');
        return false;
    }
    
    // Validar dígitos verificadores
    let soma = 0;
    for (let i = 0; i < 9; i++) {
        soma += parseInt(cpf.charAt(i)) * (10 - i);
    }
    let resto = 11 - (soma % 11);
    let dv1 = resto < 2 ? 0 : resto;
    
    if (parseInt(cpf.charAt(9)) !== dv1) {
        if (inputElement) marcarCampoInvalido(inputElement, 'CPF inválido');
        return false;
    }
    
    soma = 0;
    for (let i = 0; i < 10; i++) {
        soma += parseInt(cpf.charAt(i)) * (11 - i);
    }
    resto = 11 - (soma % 11);
    let dv2 = resto < 2 ? 0 : resto;
    
    if (parseInt(cpf.charAt(10)) !== dv2) {
        if (inputElement) marcarCampoInvalido(inputElement, 'CPF inválido');
        return false;
    }
    
    if (inputElement) marcarCampoValido(inputElement);
    return true;
}

// Validar email
function validarEmail(email, inputElement = null) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const isValid = emailRegex.test(email);
    
    if (inputElement) {
        if (isValid) {
            marcarCampoValido(inputElement);
        } else {
            marcarCampoInvalido(inputElement, 'Email inválido');
        }
    }
    
    return isValid;
}

// Validar data de admissão
function validarDataAdmissao(data, inputElement = null) {
    const dataAdmissao = new Date(data);
    const hoje = new Date();
    hoje.setHours(23, 59, 59, 999); // Final do dia atual
    
    const isValid = dataAdmissao <= hoje;
    
    if (inputElement) {
        if (isValid) {
            marcarCampoValido(inputElement);
        } else {
            marcarCampoInvalido(inputElement, 'Data de admissão não pode ser futura');
        }
    }
    
    return isValid;
}

// Validar salário
function validarSalario(salario, inputElement = null) {
    const salarioNumerico = parseFloat(salario.replace(/[^\d,]/g, '').replace(',', '.'));
    const isValid = salarioNumerico > 0;
    
    if (inputElement) {
        if (isValid) {
            marcarCampoValido(inputElement);
        } else {
            marcarCampoInvalido(inputElement, 'Salário deve ser maior que zero');
        }
    }
    
    return isValid;
}

// Marcar campo como inválido
function marcarCampoInvalido(input, mensagem = '') {
    input.classList.remove('is-valid');
    input.classList.add('is-invalid');
    
    // Remover feedback anterior
    const feedbackAnterior = input.parentNode.querySelector('.invalid-feedback');
    if (feedbackAnterior) {
        feedbackAnterior.remove();
    }
    
    // Adicionar nova mensagem de erro
    if (mensagem) {
        const feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        feedback.textContent = mensagem;
        input.parentNode.appendChild(feedback);
    }
}

// Marcar campo como válido
function marcarCampoValido(input) {
    input.classList.remove('is-invalid');
    input.classList.add('is-valid');
    
    // Remover mensagem de erro
    const feedback = input.parentNode.querySelector('.invalid-feedback');
    if (feedback) {
        feedback.remove();
    }
}

// Exibir erros de validação
function exibirErrosValidacao(errors) {
    const alertContainer = document.createElement('div');
    alertContainer.className = 'alert alert-danger alert-dismissible fade show';
    alertContainer.innerHTML = `
        <strong>Erros de validação:</strong>
        <ul class="mb-0">
            ${errors.map(error => `<li>${error}</li>`).join('')}
        </ul>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Inserir no topo do formulário
    const form = document.getElementById('colaboradorForm');
    if (form) {
        form.insertBefore(alertContainer, form.firstChild);
        
        // Auto-remover após 5 segundos
        setTimeout(() => {
            if (alertContainer.parentNode) {
                alertContainer.remove();
            }
        }, 5000);
    }
}

// Adicionar tooltips informativos
function adicionarTooltips() {
    const tooltips = {
        'cpf': 'CPF deve conter 11 dígitos válidos',
        'email': 'Email deve ter formato válido (exemplo@dominio.com)',
        'dataAdmissao': 'Data não pode ser futura',
        'salario': 'Valor deve ser maior que zero',
        'cargaHoraria': 'Carga horária semanal (1-60 horas)'
    };
    
    Object.keys(tooltips).forEach(fieldId => {
        const input = document.getElementById(fieldId);
        if (input) {
            input.setAttribute('title', tooltips[fieldId]);
            input.setAttribute('data-bs-toggle', 'tooltip');
        }
    });
    
    // Inicializar tooltips do Bootstrap se disponível
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
}

// Função para confirmar desligamento
function confirmarDesligamento() {
    const idInput = document.querySelector('input[name="id"]') || document.getElementById('id');
    const colaboradorId = idInput ? idInput.value : null;

    if (!colaboradorId) {
        alert('ID do colaborador não encontrado.');
        return;
    }

    const confirmou = confirm('Tem certeza que deseja desligar este colaborador? Esta ação não pode ser desfeita.');
    if (!confirmou) return;

    const texto = prompt('Digite DESLIGAR para confirmar:');
    if (!texto || texto.trim().toUpperCase() !== 'DESLIGAR') {
        alert('Confirmação inválida. Digite exatamente DESLIGAR.');
        return;
    }

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/rh/colaboradores/desativar/${colaboradorId}`;

    const inputConfirm = document.createElement('input');
    inputConfirm.type = 'hidden';
    inputConfirm.name = 'confirmarDesligamento';
    inputConfirm.value = 'true';
    form.appendChild(inputConfirm);

    const inputTexto = document.createElement('input');
    inputTexto.type = 'hidden';
    inputTexto.name = 'confirmarTexto';
    inputTexto.value = 'DESLIGAR';
    form.appendChild(inputTexto);

    document.body.appendChild(form);
    form.submit();
}
