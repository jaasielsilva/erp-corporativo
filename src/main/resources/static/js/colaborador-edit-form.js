// Máscaras de entrada para formulário de edição de colaborador
document.addEventListener('DOMContentLoaded', function() {
    // Aplicar máscaras nos campos
    aplicarMascaras();
    
    // Configurar busca de CEP
    configurarBuscaCEP();
    
    // Configurar validações em tempo real
    configurarValidacoes();
    
    // Configurar conversão de salário antes do envio
    configurarConversaoSalario();
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
        form.addEventListener('submit', function(e) {
            // Converte o salário para formato numérico antes do envio
            let salarioValue = salarioInput.value;
            
            // Remove R$, espaços e pontos (separadores de milhares)
            salarioValue = salarioValue.replace(/R\$\s?/g, '')
                                     .replace(/\./g, '')
                                     .replace(/,/g, '.');
            
            // Cria um input hidden com o valor numérico
            const hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = 'salario';
            hiddenInput.value = salarioValue;
            
            // Remove o name do input original para evitar conflito
            salarioInput.removeAttribute('name');
            
            // Adiciona o input hidden ao formulário
            form.appendChild(hiddenInput);
        });
    }
}

function formatarCPF(cpf) {
    const numericCPF = cpf.replace(/\D/g, '');
    return numericCPF.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

// Função para confirmar desligamento
function confirmarDesligamento() {
    if (confirm('Tem certeza que deseja desligar este colaborador? Esta ação não pode ser desfeita.')) {
        // Aqui seria feita a requisição para desligar o colaborador
        alert('Funcionalidade de desligamento será implementada.');
    }
}