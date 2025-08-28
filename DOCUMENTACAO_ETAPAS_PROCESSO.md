# Documentação Detalhada das Etapas do Processo

## Visão Geral do Fluxo

```
Início → Dados Pessoais → Documentos → Benefícios → Revisão → Finalização → Status
```

### Controle de Navegação
- **SessionId**: Identificador único para cada processo
- **Validação de Etapas**: Não é possível pular etapas
- **Persistência**: Dados salvos a cada etapa
- **Retorno**: Possível voltar para etapas anteriores

---

## ETAPA 1: Dados Pessoais (inicio.html)

### Objetivo
Coletar informações pessoais, profissionais e de endereço do novo colaborador.

### Seções do Formulário

#### 1.1 Dados Pessoais
```html
<div class="form-section">
    <h4><i class="fas fa-user me-2"></i>Dados Pessoais</h4>
    <div class="row">
        <div class="col-md-6">
            <label>Nome Completo *</label>
            <input type="text" id="nome" required>
        </div>
        <div class="col-md-6">
            <label>Gênero *</label>
            <select id="genero" required>
                <option value="M">Masculino</option>
                <option value="F">Feminino</option>
                <option value="O">Outro</option>
            </select>
        </div>
        <div class="col-md-6">
            <label>CPF *</label>
            <input type="text" id="cpf" data-mask="000.000.000-00" required>
        </div>
        <div class="col-md-6">
            <label>RG *</label>
            <input type="text" id="rg" required>
        </div>
        <div class="col-md-6">
            <label>Data de Nascimento *</label>
            <input type="date" id="dataNascimento" required>
        </div>
        <div class="col-md-6">
            <label>Estado Civil *</label>
            <select id="estadoCivil" required>
                <option value="solteiro">Solteiro(a)</option>
                <option value="casado">Casado(a)</option>
                <option value="divorciado">Divorciado(a)</option>
                <option value="viuvo">Viúvo(a)</option>
            </select>
        </div>
        <div class="col-md-6">
            <label>E-mail *</label>
            <input type="email" id="email" required>
        </div>
        <div class="col-md-6">
            <label>Telefone *</label>
            <input type="text" id="telefone" data-mask="(00) 00000-0000" required>
        </div>
    </div>
</div>
```

#### 1.2 Dados Profissionais
```html
<div class="form-section">
    <h4><i class="fas fa-briefcase me-2"></i>Dados Profissionais</h4>
    <div class="row">
        <div class="col-md-6">
            <label>Cargo *</label>
            <input type="text" id="cargo" required>
        </div>
        <div class="col-md-6">
            <label>Departamento *</label>
            <select id="departamento" required>
                <option value="ti">Tecnologia da Informação</option>
                <option value="rh">Recursos Humanos</option>
                <option value="financeiro">Financeiro</option>
                <option value="comercial">Comercial</option>
                <option value="operacional">Operacional</option>
            </select>
        </div>
        <div class="col-md-6">
            <label>Data de Admissão *</label>
            <input type="date" id="dataAdmissao" required>
        </div>
        <div class="col-md-6">
            <label>Salário *</label>
            <input type="text" id="salario" data-mask="#.##0,00" data-mask-reverse="true" required>
        </div>
        <div class="col-md-6">
            <label>Tipo de Contrato *</label>
            <select id="tipoContrato" required>
                <option value="clt">CLT</option>
                <option value="pj">Pessoa Jurídica</option>
                <option value="estagio">Estágio</option>
                <option value="terceirizado">Terceirizado</option>
            </select>
        </div>
        <div class="col-md-6">
            <label>Carga Horária *</label>
            <select id="cargaHoraria" required>
                <option value="20h">20 horas</option>
                <option value="30h">30 horas</option>
                <option value="40h">40 horas</option>
                <option value="44h">44 horas</option>
            </select>
        </div>
        <div class="col-12">
            <label>Supervisor Direto</label>
            <input type="text" id="supervisor">
        </div>
    </div>
</div>
```

#### 1.3 Endereço
```html
<div class="form-section">
    <h4><i class="fas fa-map-marker-alt me-2"></i>Endereço</h4>
    <div class="row">
        <div class="col-md-3">
            <label>CEP *</label>
            <input type="text" id="cep" data-mask="00000-000" required>
        </div>
        <div class="col-md-7">
            <label>Logradouro *</label>
            <input type="text" id="logradouro" required>
        </div>
        <div class="col-md-2">
            <label>Número *</label>
            <input type="text" id="numero" required>
        </div>
        <div class="col-md-6">
            <label>Complemento</label>
            <input type="text" id="complemento">
        </div>
        <div class="col-md-6">
            <label>Bairro *</label>
            <input type="text" id="bairro" required>
        </div>
        <div class="col-md-8">
            <label>Cidade *</label>
            <input type="text" id="cidade" required>
        </div>
        <div class="col-md-4">
            <label>Estado *</label>
            <select id="estado" required>
                <option value="AC">Acre</option>
                <option value="AL">Alagoas</option>
                <!-- Todos os estados brasileiros -->
            </select>
        </div>
    </div>
</div>
```

### Funcionalidades JavaScript

#### 1.4 Máscaras de Input
```javascript
$(document).ready(function() {
    // Aplicar máscaras
    $('#cpf').mask('000.000.000-00');
    $('#telefone').mask('(00) 00000-0000');
    $('#cep').mask('00000-000');
    $('#salario').mask('#.##0,00', {reverse: true});
});
```

#### 1.5 Validação de CPF
```javascript
function validarCPF(cpf) {
    cpf = cpf.replace(/[^\d]/g, '');
    
    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
        return false;
    }
    
    let soma = 0;
    for (let i = 0; i < 9; i++) {
        soma += parseInt(cpf.charAt(i)) * (10 - i);
    }
    
    let resto = 11 - (soma % 11);
    let digito1 = resto < 2 ? 0 : resto;
    
    if (parseInt(cpf.charAt(9)) !== digito1) {
        return false;
    }
    
    soma = 0;
    for (let i = 0; i < 10; i++) {
        soma += parseInt(cpf.charAt(i)) * (11 - i);
    }
    
    resto = 11 - (soma % 11);
    let digito2 = resto < 2 ? 0 : resto;
    
    return parseInt(cpf.charAt(10)) === digito2;
}
```

#### 1.6 Integração ViaCEP
```javascript
$('#cep').on('blur', function() {
    const cep = $(this).val().replace(/\D/g, '');
    
    if (cep.length === 8) {
        $.getJSON(`https://viacep.com.br/ws/${cep}/json/`, function(data) {
            if (!data.erro) {
                $('#logradouro').val(data.logradouro);
                $('#bairro').val(data.bairro);
                $('#cidade').val(data.localidade);
                $('#estado').val(data.uf);
            }
        });
    }
});
```

#### 1.7 Envio do Formulário
```javascript
function enviarDadosPessoais() {
    if (!validarFormulario()) {
        return;
    }
    
    const dados = {
        nome: $('#nome').val(),
        genero: $('#genero').val(),
        cpf: $('#cpf').val(),
        rg: $('#rg').val(),
        dataNascimento: $('#dataNascimento').val(),
        estadoCivil: $('#estadoCivil').val(),
        email: $('#email').val(),
        telefone: $('#telefone').val(),
        cargo: $('#cargo').val(),
        departamento: $('#departamento').val(),
        dataAdmissao: $('#dataAdmissao').val(),
        salario: $('#salario').val(),
        tipoContrato: $('#tipoContrato').val(),
        cargaHoraria: $('#cargaHoraria').val(),
        supervisor: $('#supervisor').val(),
        cep: $('#cep').val(),
        logradouro: $('#logradouro').val(),
        numero: $('#numero').val(),
        complemento: $('#complemento').val(),
        bairro: $('#bairro').val(),
        cidade: $('#cidade').val(),
        estado: $('#estado').val()
    };
    
    $.ajax({
        url: '/rh/colaboradores/adesao/dados-pessoais',
        method: 'POST',
        data: dados,
        success: function(response) {
            if (response.success) {
                window.location.href = `/rh/colaboradores/adesao/documentos?sessionId=${response.sessionId}`;
            } else {
                mostrarErro(response.message);
            }
        },
        error: function(xhr) {
            mostrarErro('Erro ao salvar dados pessoais');
        }
    });
}
```

---

## ETAPA 2: Documentos (documentos.html)

### Objetivo
Upload e validação de documentos obrigatórios e opcionais.

### Documentos Obrigatórios
1. **RG (Registro Geral)**
2. **CPF (Cadastro de Pessoa Física)**
3. **Comprovante de Residência**

### Documentos Opcionais
1. **Carteira de Trabalho**
2. **Título de Eleitor**
3. **Certificado de Reservista**
4. **Comprovante de Escolaridade**
5. **Certidão de Nascimento/Casamento**

### Sistema de Upload

#### 2.1 Área de Upload
```html
<div class="upload-section">
    <div class="upload-area" data-tipo="rg">
        <div class="upload-content">
            <i class="fas fa-cloud-upload-alt upload-icon"></i>
            <h5>RG (Registro Geral)</h5>
            <p>Arraste o arquivo aqui ou clique para selecionar</p>
            <small>Formatos aceitos: PDF, JPG, JPEG, PNG (máx. 5MB)</small>
            <input type="file" class="upload-input" accept=".pdf,.jpg,.jpeg,.png">
        </div>
        
        <div class="upload-progress" style="display: none;">
            <div class="progress">
                <div class="progress-bar" role="progressbar"></div>
            </div>
            <small class="progress-text">Enviando...</small>
        </div>
        
        <div class="upload-preview" style="display: none;">
            <div class="preview-content">
                <img class="preview-image" style="display: none;">
                <div class="preview-pdf" style="display: none;">
                    <i class="fas fa-file-pdf fa-3x text-danger"></i>
                    <p class="preview-filename"></p>
                </div>
            </div>
            <div class="preview-actions">
                <button class="btn btn-sm btn-outline-danger" onclick="removerArquivo('rg')">
                    <i class="fas fa-trash"></i> Remover
                </button>
            </div>
        </div>
    </div>
</div>
```

#### 2.2 Funcionalidades de Upload

**Drag & Drop**:
```javascript
$('.upload-area').on('dragover', function(e) {
    e.preventDefault();
    $(this).addClass('drag-over');
});

$('.upload-area').on('dragleave', function(e) {
    e.preventDefault();
    $(this).removeClass('drag-over');
});

$('.upload-area').on('drop', function(e) {
    e.preventDefault();
    $(this).removeClass('drag-over');
    
    const files = e.originalEvent.dataTransfer.files;
    if (files.length > 0) {
        const tipo = $(this).data('tipo');
        uploadArquivo(files[0], tipo);
    }
});
```

**Upload com Progress Bar**:
```javascript
function uploadArquivo(arquivo, tipo) {
    if (!validarArquivo(arquivo)) {
        return;
    }
    
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    formData.append('tipo', tipo);
    formData.append('sessionId', sessionId);
    
    const uploadArea = $(`.upload-area[data-tipo="${tipo}"]`);
    uploadArea.find('.upload-content').hide();
    uploadArea.find('.upload-progress').show();
    
    $.ajax({
        url: '/api/rh/colaboradores/adesao/documentos/upload',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        xhr: function() {
            const xhr = new window.XMLHttpRequest();
            xhr.upload.addEventListener('progress', function(e) {
                if (e.lengthComputable) {
                    const percentComplete = (e.loaded / e.total) * 100;
                    uploadArea.find('.progress-bar').css('width', percentComplete + '%');
                    uploadArea.find('.progress-text').text(`Enviando... ${Math.round(percentComplete)}%`);
                }
            }, false);
            return xhr;
        },
        success: function(response) {
            if (response.success) {
                mostrarPreview(arquivo, tipo, response.data);
                verificarDocumentosObrigatorios();
            } else {
                mostrarErroUpload(tipo, response.message);
            }
        },
        error: function() {
            mostrarErroUpload(tipo, 'Erro ao enviar arquivo');
        }
    });
}
```

**Validação de Arquivo**:
```javascript
function validarArquivo(arquivo) {
    const tiposPermitidos = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    const tamanhoMaximo = 5 * 1024 * 1024; // 5MB
    
    if (!tiposPermitidos.includes(arquivo.type)) {
        mostrarErro('Tipo de arquivo não permitido. Use PDF, JPG, JPEG ou PNG.');
        return false;
    }
    
    if (arquivo.size > tamanhoMaximo) {
        mostrarErro('Arquivo muito grande. Tamanho máximo: 5MB.');
        return false;
    }
    
    return true;
}
```

**Preview de Arquivo**:
```javascript
function mostrarPreview(arquivo, tipo, dadosResposta) {
    const uploadArea = $(`.upload-area[data-tipo="${tipo}"]`);
    uploadArea.find('.upload-progress').hide();
    uploadArea.find('.upload-preview').show();
    
    if (arquivo.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = function(e) {
            uploadArea.find('.preview-image').attr('src', e.target.result).show();
            uploadArea.find('.preview-pdf').hide();
        };
        reader.readAsDataURL(arquivo);
    } else {
        uploadArea.find('.preview-image').hide();
        uploadArea.find('.preview-pdf').show();
        uploadArea.find('.preview-filename').text(arquivo.name);
    }
}
```

#### 2.3 Verificação de Documentos Obrigatórios
```javascript
function verificarDocumentosObrigatorios() {
    $.ajax({
        url: '/api/rh/colaboradores/adesao/documentos/status',
        method: 'GET',
        data: { sessionId: sessionId },
        success: function(response) {
            if (response.success) {
                const documentosObrigatorios = ['rg', 'cpf', 'comprovante_residencia'];
                const todosEnviados = documentosObrigatorios.every(doc => 
                    response.documentos.some(d => d.tipo === doc)
                );
                
                $('#btnProximaEtapa').prop('disabled', !todosEnviados);
                
                if (todosEnviados) {
                    mostrarSucesso('Todos os documentos obrigatórios foram enviados!');
                }
            }
        }
    });
}
```

---

## ETAPA 3: Benefícios (beneficios.html)

### Objetivo
Seleção de benefícios corporativos e planos disponíveis.

### Benefícios Disponíveis

#### 3.1 Plano de Saúde
```html
<div class="benefit-card" data-beneficio="plano-saude">
    <div class="benefit-header">
        <div class="benefit-info">
            <div class="benefit-icon">
                <i class="fas fa-heartbeat"></i>
            </div>
            <div>
                <h5>Plano de Saúde</h5>
                <p>Assistência médica completa para você e sua família</p>
            </div>
        </div>
        <div class="benefit-toggle">
            <input type="checkbox" class="benefit-switch" data-beneficio="plano-saude">
            <span class="slider"></span>
        </div>
    </div>
    
    <div class="benefit-details" style="display: none;">
        <div class="plan-options">
            <div class="plan-option" data-plano="basico">
                <input type="radio" name="plano-saude" value="basico" id="saude-basico">
                <label for="saude-basico">
                    <div class="plan-info">
                        <h6>Plano Básico</h6>
                        <p>Consultas, exames básicos</p>
                    </div>
                    <div class="plan-price">R$ 150,00</div>
                </label>
            </div>
            
            <div class="plan-option" data-plano="intermediario">
                <input type="radio" name="plano-saude" value="intermediario" id="saude-intermediario">
                <label for="saude-intermediario">
                    <div class="plan-info">
                        <h6>Plano Intermediário</h6>
                        <p>Consultas, exames, internações</p>
                    </div>
                    <div class="plan-price">R$ 250,00</div>
                </label>
            </div>
            
            <div class="plan-option" data-plano="premium">
                <input type="radio" name="plano-saude" value="premium" id="saude-premium">
                <label for="saude-premium">
                    <div class="plan-info">
                        <h6>Plano Premium</h6>
                        <p>Cobertura completa, rede ampliada</p>
                    </div>
                    <div class="plan-price">R$ 400,00</div>
                </label>
            </div>
        </div>
        
        <div class="dependents-section">
            <h6>Dependentes</h6>
            <p>Adicione dependentes ao seu plano (+R$ 100,00 cada)</p>
            <div class="dependents-list" id="dependentes-saude">
                <!-- Dependentes serão adicionados dinamicamente -->
            </div>
            <button type="button" class="btn btn-outline-primary btn-sm" onclick="adicionarDependente('plano-saude')">
                <i class="fas fa-plus"></i> Adicionar Dependente
            </button>
        </div>
    </div>
</div>
```

#### 3.2 Vale Refeição
```html
<div class="benefit-card" data-beneficio="vale-refeicao">
    <div class="benefit-header">
        <div class="benefit-info">
            <div class="benefit-icon">
                <i class="fas fa-utensils"></i>
            </div>
            <div>
                <h5>Vale Refeição</h5>
                <p>Auxílio alimentação mensal</p>
            </div>
        </div>
        <div class="benefit-toggle">
            <input type="checkbox" class="benefit-switch" data-beneficio="vale-refeicao">
            <span class="slider"></span>
        </div>
    </div>
    
    <div class="benefit-details" style="display: none;">
        <div class="plan-options">
            <div class="plan-option" data-plano="300">
                <input type="radio" name="vale-refeicao" value="300" id="refeicao-300">
                <label for="refeicao-300">
                    <div class="plan-info">
                        <h6>Vale R$ 300,00</h6>
                        <p>R$ 15,00 por dia útil</p>
                    </div>
                    <div class="plan-price">R$ 300,00</div>
                </label>
            </div>
            
            <div class="plan-option" data-plano="500">
                <input type="radio" name="vale-refeicao" value="500" id="refeicao-500">
                <label for="refeicao-500">
                    <div class="plan-info">
                        <h6>Vale R$ 500,00</h6>
                        <p>R$ 25,00 por dia útil</p>
                    </div>
                    <div class="plan-price">R$ 500,00</div>
                </label>
            </div>
        </div>
    </div>
</div>
```

#### 3.3 Vale Transporte
```html
<div class="benefit-card" data-beneficio="vale-transporte">
    <div class="benefit-header">
        <div class="benefit-info">
            <div class="benefit-icon">
                <i class="fas fa-bus"></i>
            </div>
            <div>
                <h5>Vale Transporte</h5>
                <p>Auxílio transporte público</p>
            </div>
        </div>
        <div class="benefit-toggle">
            <input type="checkbox" class="benefit-switch" data-beneficio="vale-transporte">
            <span class="slider"></span>
        </div>
    </div>
    
    <div class="benefit-details" style="display: none;">
        <div class="plan-options">
            <div class="plan-option" data-plano="basico">
                <input type="radio" name="vale-transporte" value="basico" id="transporte-basico">
                <label for="transporte-basico">
                    <div class="plan-info">
                        <h6>Básico</h6>
                        <p>Ônibus urbano</p>
                    </div>
                    <div class="plan-price">R$ 150,00</div>
                </label>
            </div>
            
            <div class="plan-option" data-plano="intermediario">
                <input type="radio" name="vale-transporte" value="intermediario" id="transporte-intermediario">
                <label for="transporte-intermediario">
                    <div class="plan-info">
                        <h6>Intermediário</h6>
                        <p>Ônibus + Metrô</p>
                    </div>
                    <div class="plan-price">R$ 250,00</div>
                </label>
            </div>
            
            <div class="plan-option" data-plano="premium">
                <input type="radio" name="vale-transporte" value="premium" id="transporte-premium">
                <label for="transporte-premium">
                    <div class="plan-info">
                        <h6>Premium</h6>
                        <p>Transporte completo</p>
                    </div>
                    <div class="plan-price">R$ 350,00</div>
                </label>
            </div>
        </div>
    </div>
</div>
```

### Funcionalidades JavaScript

#### 3.4 Toggle de Benefícios
```javascript
$('.benefit-switch').on('change', function() {
    const beneficio = $(this).data('beneficio');
    const card = $(`.benefit-card[data-beneficio="${beneficio}"]`);
    const details = card.find('.benefit-details');
    
    if ($(this).is(':checked')) {
        details.slideDown();
        card.addClass('selected');
    } else {
        details.slideUp();
        card.removeClass('selected');
        // Limpar seleções
        card.find('input[type="radio"]').prop('checked', false);
        card.find('.plan-option').removeClass('selected');
    }
    
    atualizarResumo();
});
```

#### 3.5 Seleção de Planos
```javascript
$('.plan-option input[type="radio"]').on('change', function() {
    const planOption = $(this).closest('.plan-option');
    planOption.siblings().removeClass('selected');
    planOption.addClass('selected');
    
    atualizarResumo();
});
```

#### 3.6 Gerenciamento de Dependentes
```javascript
function adicionarDependente(beneficio) {
    const dependenteId = Date.now();
    const html = `
        <div class="dependent-item" data-id="${dependenteId}">
            <div class="row align-items-center">
                <div class="col-md-4">
                    <input type="text" class="form-control form-control-sm" 
                           placeholder="Nome do dependente" required>
                </div>
                <div class="col-md-3">
                    <select class="form-select form-select-sm" required>
                        <option value="">Parentesco</option>
                        <option value="conjuge">Cônjuge</option>
                        <option value="filho">Filho(a)</option>
                        <option value="pai">Pai</option>
                        <option value="mae">Mãe</option>
                        <option value="outro">Outro</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <input type="date" class="form-control form-control-sm" 
                           placeholder="Data de nascimento" required>
                </div>
                <div class="col-md-2">
                    <button type="button" class="btn btn-outline-danger btn-sm" 
                            onclick="removerDependente(${dependenteId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
    
    $(`#dependentes-${beneficio}`).append(html);
    atualizarResumo();
}

function removerDependente(dependenteId) {
    $(`.dependent-item[data-id="${dependenteId}"]`).remove();
    atualizarResumo();
}
```

#### 3.7 Cálculo e Resumo
```javascript
function atualizarResumo() {
    const beneficiosSelecionados = coletarBeneficiosSelecionados();
    
    $.ajax({
        url: '/api/rh/colaboradores/adesao/beneficios/calcular',
        method: 'POST',
        data: {
            sessionId: sessionId,
            beneficios: JSON.stringify(beneficiosSelecionados)
        },
        success: function(response) {
            if (response.success) {
                mostrarResumoCalculado(response.resumo);
            }
        }
    });
}

function coletarBeneficiosSelecionados() {
    const beneficios = {};
    
    $('.benefit-switch:checked').each(function() {
        const beneficio = $(this).data('beneficio');
        const card = $(`.benefit-card[data-beneficio="${beneficio}"]`);
        const planoSelecionado = card.find('input[type="radio"]:checked').val();
        
        if (planoSelecionado) {
            beneficios[beneficio] = {
                plano: planoSelecionado,
                dependentes: []
            };
            
            // Coletar dependentes se for plano de saúde
            if (beneficio === 'plano-saude') {
                card.find('.dependent-item').each(function() {
                    const nome = $(this).find('input[type="text"]').val();
                    const parentesco = $(this).find('select').val();
                    const dataNascimento = $(this).find('input[type="date"]').val();
                    
                    if (nome && parentesco && dataNascimento) {
                        beneficios[beneficio].dependentes.push({
                            nome: nome,
                            parentesco: parentesco,
                            dataNascimento: dataNascimento
                        });
                    }
                });
            }
        }
    });
    
    return beneficios;
}

function mostrarResumoCalculado(resumo) {
    let html = '';
    let total = 0;
    
    resumo.itens.forEach(item => {
        html += `
            <div class="summary-item">
                <div class="summary-info">
                    <strong>${item.nome}</strong>
                    <small>${item.descricao}</small>
                </div>
                <div class="summary-price">R$ ${item.valor.toFixed(2).replace('.', ',')}</div>
            </div>
        `;
        total += item.valor;
    });
    
    $('#resumo-beneficios').html(html);
    $('#total-mensal').text(`R$ ${total.toFixed(2).replace('.', ',')}`);
}
```

#### 3.8 Envio dos Benefícios
```javascript
function enviarBeneficios() {
    const beneficiosSelecionados = coletarBeneficiosSelecionados();
    
    // Validar se pelo menos um benefício foi selecionado
    if (Object.keys(beneficiosSelecionados).length === 0) {
        mostrarErro('Selecione pelo menos um benefício.');
        return;
    }
    
    // Validar se todos os benefícios selecionados têm plano escolhido
    for (const beneficio in beneficiosSelecionados) {
        if (!beneficiosSelecionados[beneficio].plano) {
            mostrarErro(`Selecione um plano para ${beneficio.replace('-', ' ')}.`);
            return;
        }
    }
    
    $('#btnProximaEtapa').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Salvando...');
    
    $.ajax({
        url: '/rh/colaboradores/adesao/beneficios',
        method: 'POST',
        data: {
            sessionId: sessionId,
            beneficios: JSON.stringify(beneficiosSelecionados)
        },
        success: function(response) {
            if (response.success) {
                mostrarSucesso('Benefícios salvos com sucesso!');
                setTimeout(() => {
                    window.location.href = `/rh/colaboradores/adesao/revisao?sessionId=${sessionId}`;
                }, 1500);
            } else {
                mostrarErro(response.message);
                $('#btnProximaEtapa').prop('disabled', false).html('<i class="fas fa-arrow-right"></i> Próxima Etapa');
            }
        },
        error: function() {
            mostrarErro('Erro ao salvar benefícios.');
            $('#btnProximaEtapa').prop('disabled', false).html('<i class="fas fa-arrow-right"></i> Próxima Etapa');
        }
    });
}
```

---

## ETAPA 4: Revisão (revisao.html)

### Objetivo
Revisão final de todos os dados coletados antes da submissão definitiva.

### Seções de Revisão

#### 4.1 Dados Pessoais
```javascript
function preencherDadosRevisao() {
    if (dadosRevisao.dadosPessoais) {
        const dados = dadosRevisao.dadosPessoais;
        let html = `
            <div class="row">
                <div class="col-md-6">
                    <div class="info-row">
                        <span class="info-label">Nome Completo:</span>
                        <span class="info-value">${dados.nome || '-'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">CPF:</span>
                        <span class="info-value">${dados.cpf || '-'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">E-mail:</span>
                        <span class="info-value">${dados.email || '-'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Telefone:</span>
                        <span class="info-value">${dados.telefone || '-'}</span>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="info-row">
                        <span class="info-label">Cargo:</span>
                        <span class="info-value">${dados.cargo || '-'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Departamento:</span>
                        <span class="info-value">${dados.departamento || '-'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Salário:</span>
                        <span class="info-value">R$ ${formatarMoeda(dados.salario) || '-'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Endereço:</span>
                        <span class="info-value">${dados.endereco || '-'}</span>
                    </div>
                </div>
            </div>
        `;
        
        $('#dadosPessoais').html(html);
    }
}
```

#### 4.2 Documentos Enviados
```javascript
if (dadosRevisao.documentos) {
    let html = '';
    dadosRevisao.documentos.forEach(doc => {
        html += `
            <div class="document-item uploaded">
                <div>
                    <i class="fas fa-file-alt me-2 text-success"></i>
                    <strong>${doc.tipo}</strong>
                    <br>
                    <small class="text-muted">${doc.nomeArquivo} (${doc.tamanho})</small>
                </div>
                <div>
                    <span class="badge bg-success">
                        <i class="fas fa-check me-1"></i>Enviado
                    </span>
                </div>
            </div>
        `;
    });
    
    $('#documentosEnviados').html(html);
}
```

#### 4.3 Termos e Condições
```html
<div class="terms-section">
    <h5><i class="fas fa-file-contract me-2"></i>Termos e Condições</h5>
    
    <div class="form-check mb-3">
        <input class="form-check-input" type="checkbox" id="aceitarTermos" required>
        <label class="form-check-label" for="aceitarTermos">
            <strong>Li e aceito os termos e condições</strong> do processo de adesão
        </label>
    </div>
    
    <div class="form-check mb-3">
        <input class="form-check-input" type="checkbox" id="autorizarDesconto" required>
        <label class="form-check-label" for="autorizarDesconto">
            <strong>Autorizo o desconto</strong> dos valores dos benefícios em folha de pagamento
        </label>
    </div>
    
    <div class="form-check mb-3">
        <input class="form-check-input" type="checkbox" id="confirmarDados" required>
        <label class="form-check-label" for="confirmarDados">
            <strong>Confirmo que todos os dados</strong> informados são verdadeiros
        </label>
    </div>
</div>
```

#### 4.4 Finalização do Processo
```javascript
function finalizarAdesao() {
    // Verificar se todos os termos foram aceitos
    if (!$('#aceitarTermos').is(':checked') || 
        !$('#autorizarDesconto').is(':checked') || 
        !$('#confirmarDados').is(':checked')) {
        mostrarErro('Você deve aceitar todos os termos para finalizar a adesão.');
        return;
    }
    
    // Mostrar loading
    $('#loadingOverlay').show();
    
    $.ajax({
        url: '/rh/colaboradores/adesao/finalizar',
        method: 'POST',
        data: {
            sessionId: sessionId,
            aceitarTermos: true,
            autorizarDesconto: true,
            confirmarDados: true
        },
        success: function(response) {
            $('#loadingOverlay').hide();
            
            if (response.success) {
                $('#protocoloAdesao').text(response.protocolo);
                $('#successModal').modal('show');
            } else {
                mostrarErro(response.message || 'Erro ao finalizar adesão');
            }
        },
        error: function(xhr) {
            $('#loadingOverlay').hide();
            const response = xhr.responseJSON;
            mostrarErro(response?.message || 'Erro interno do servidor');
        }
    });
}
```

---

## Validações e Controles

### Validações Client-side
1. **Campos Obrigatórios**: Verificação em tempo real
2. **Formatos**: CPF, email, telefone, CEP
3. **Arquivos**: Tipo, tamanho, extensão
4. **Benefícios**: Planos selecionados para benefícios ativos
5. **Termos**: Aceitação obrigatória de todos os termos

### Validações Server-side
1. **Sessão**: Validação de sessionId em cada etapa
2. **Dados**: Sanitização e validação de todos os campos
3. **Arquivos**: Validação de MIME type e conteúdo
4. **Integridade**: Verificação de dados entre etapas
5. **Segurança**: Prevenção de ataques e validação de acesso

### Controle de Fluxo
1. **Sequencial**: Não é possível pular etapas
2. **Persistência**: Dados salvos a cada etapa
3. **Retorno**: Possível voltar para etapas anteriores
4. **Timeout**: Sessão expira após inatividade
5. **Recuperação**: Possível retomar processo interrompido

---

**Versão**: 1.0  
**Data**: Janeiro 2025  
**Status**: Documentação Completa das Etapas