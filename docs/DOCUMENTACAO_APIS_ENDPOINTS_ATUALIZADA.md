# 🔌 DOCUMENTAÇÃO COMPLETA - APIs e Endpoints
## Sistema ERP Corporativo - Módulo RH

**Versão**: 2.0.0  
**Data**: Setembro 2025  
**Status**: ✅ **TOTALMENTE FUNCIONAL**

---

## 📖 Índice

1. [Visão Geral](#visão-geral)
2. [Autenticação](#autenticação)
3. [Processo de Adesão](#processo-de-adesão)
4. [APIs de Documentos](#apis-de-documentos)
5. [APIs de Benefícios](#apis-de-benefícios)
6. [APIs de Workflow](#apis-de-workflow)
7. [Códigos de Resposta](#códigos-de-resposta)
8. [Exemplos de Uso](#exemplos-de-uso)

---

## 🎯 Visão Geral

Este documento descreve todas as APIs e endpoints disponíveis no módulo RH do ERP Corporativo, com foco especial no processo de adesão de colaboradores que foi **completamente implementado e testado**.

### 📋 Status de Implementação

- ✅ **Processo de Adesão**: 100% funcional (4 etapas)
- ✅ **Sistema de Documentos**: Upload, validação e armazenamento
- ✅ **Gestão de Benefícios**: Cálculos e seleção
- ✅ **Workflow de Aprovação**: Controle de estados
- ✅ **Logs e Auditoria**: Rastreamento completo

### 🔧 Padrões de Resposta

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Operação realizada com sucesso",
    "data": {
        // Dados específicos da operação
    }
}
```

**Resposta de Erro**:
```json
{
    "success": false,
    "message": "Descrição do erro",
    "errors": {
        "campo": ["Mensagem de erro específica"]
    }
}
```

### 📤 Headers Padrão
```
Content-Type: application/json
X-Requested-With: XMLHttpRequest
CSRF-Token: {token}
```

---

## 🔐 Autenticação

Todas as APIs utilizam autenticação baseada em sessão do Spring Security. O usuário deve estar logado no sistema para acessar os endpoints.

### Verificação de Autenticação
```javascript
// Verificação automática via interceptor
$.ajaxSetup({
    beforeSend: function(xhr) {
        xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
    }
});
```

---

## 🚀 Processo de Adesão

### 🎯 Etapa 1: Dados Pessoais

#### POST /rh/colaboradores/adesao/dados-pessoais

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Salva os dados pessoais do colaborador e inicia o processo de adesão.

**Parâmetros**:
```json
{
    "nome": "string (required, max:255)",
    "genero": "string (required, enum: M|F|O)",
    "cpf": "string (required, format: 000.000.000-00)",
    "rg": "string (required, max:20)",
    "dataNascimento": "date (required, format: YYYY-MM-DD)",
    "estadoCivil": "string (required, enum: SOLTEIRO|CASADO|DIVORCIADO|VIUVO)",
    "email": "email (required, max:255)",
    "telefone": "string (required, format: (00) 00000-0000)",
    "cargo": "string (required, max:100)",
    "departamento": "string (required)",
    "dataAdmissao": "date (required, format: YYYY-MM-DD)",
    "salario": "decimal (required, min:0)",
    "tipoContrato": "string (required, enum: CLT|PJ|ESTAGIO|TERCEIRIZADO)",
    "cargaHoraria": "integer (required, min:1, max:60)",
    "supervisor": "string (optional, max:255)",
    "cep": "string (required, format: 00000-000)",
    "logradouro": "string (required, max:255)",
    "numero": "string (required, max:10)",
    "complemento": "string (optional, max:100)",
    "bairro": "string (required, max:100)",
    "cidade": "string (required, max:100)",
    "estado": "string (required, size:2)"
}
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Dados pessoais salvos com sucesso",
    "data": {
        "sessionId": "bb6e4334-a916-4a0e-9a8f-e6ef6b9509bb",
        "etapaAtual": "DOCUMENTOS",
        "proximaEtapa": "/rh/colaboradores/adesao/documentos"
    }
}
```

**Validações Implementadas**:
- ✅ CPF: Formato e dígitos verificadores
- ✅ Email: Formato RFC e unicidade
- ✅ Idade: Mínimo 16 anos
- ✅ Data admissão: Não pode ser futura
- ✅ CEP: Integração com ViaCEP

### 🎯 Etapa 2: Documentos

#### POST /api/rh/colaboradores/adesao/documentos/upload

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Upload de documentos com validação completa.

**Content-Type**: `multipart/form-data`

**Parâmetros**:
```
arquivo: File (required, max:10MB, types: pdf|jpg|jpeg|png)
tipo: string (required, enum: rg|cpf|endereco)
sessionId: string (required, uuid)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Documento enviado com sucesso",
    "data": {
        "tipo": "rg",
        "nomeArquivo": "rg_documento.pdf",
        "tamanho": "1.2MB",
        "caminhoArquivo": "/uploads/session-id/rg_documento.pdf",
        "dataUpload": "2025-09-04T15:30:00"
    }
}
```

#### GET /api/rh/colaboradores/adesao/documentos/status

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Verifica status dos documentos obrigatórios.

**Parâmetros**:
```
sessionId: string (required, query parameter)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "documentosObrigatoriosCompletos": true,
    "totalDocumentos": 3,
    "documentosObrigatorios": 3,
    "documentosOpcionais": 0,
    "podeProximaEtapa": true
}
```

#### DELETE /api/rh/colaboradores/adesao/documentos/remover/{tipo}

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Remove documento enviado.

**Parâmetros**:
```
tipo: string (path parameter, enum: rg|cpf|endereco)
sessionId: string (required, query parameter)
```

#### GET /api/rh/colaboradores/adesao/documentos/visualizar/{tipo}

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Visualiza documento enviado.

#### POST /rh/colaboradores/adesao/documentos

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Finaliza etapa de documentos e avança para benefícios.

### 🎯 Etapa 3: Benefícios

#### GET /api/rh/colaboradores/adesao/beneficios/disponiveis

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Lista benefícios disponíveis para seleção.

**Resposta de Sucesso**:
```json
{
    "success": true,
    "beneficios": [
        {
            "id": "plano-saude",
            "nome": "Plano de Saúde",
            "descricao": "Cobertura médica e hospitalar",
            "opcoes": [
                {
                    "id": "basico",
                    "nome": "Básico",
                    "valor": 300.00
                },
                {
                    "id": "premium", 
                    "nome": "Premium",
                    "valor": 600.00
                }
            ]
        },
        {
            "id": "vale-refeicao",
            "nome": "Vale Refeição",
            "opcoes": [
                {
                    "id": "vale-350",
                    "valor": 350.00
                },
                {
                    "id": "vale-500", 
                    "valor": 500.00
                }
            ]
        }
    ]
}
```

#### GET /api/rh/colaboradores/adesao/beneficios/resumo

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Gera resumo dos benefícios selecionados com cálculos.

**Parâmetros**:
```
sessionId: string (required, query parameter)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "resumo": {
        "totalMensal": 1450.00,
        "totalAnual": 17400.00,
        "quantidadeItens": 3,
        "beneficios": [
            {
                "tipo": "Plano de Saúde",
                "nome": "Plano de Saúde - Omint",
                "valorTitular": 600.00,
                "dependentes": 0,
                "valorDependentes": 0.00,
                "valorTotal": 600.00
            },
            {
                "tipo": "Vale Refeição", 
                "valor": 500.00
            },
            {
                "tipo": "Vale Transporte",
                "valor": 350.00
            }
        ]
    }
}
```

#### POST /rh/colaboradores/adesao/beneficios

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Salva seleção de benefícios e avança para revisão.

### 🎯 Etapa 4: Revisão e Finalização

#### GET /rh/colaboradores/adesao/revisao/{sessionId}

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Carrega dados completos para revisão final.

**Resposta de Sucesso**:
```json
{
    "success": true,
    "data": {
        "dadosPessoais": {
            "nome": "João Silva Santos",
            "cpf": "123.456.789-00",
            "email": "joao.silva@email.com",
            "cargo": "Desenvolvedor",
            "salario": 5000.00
        },
        "documentos": [
            {
                "tipo": "RG",
                "nomeArquivo": "rg_documento.pdf",
                "status": "enviado"
            }
        ],
        "beneficios_calculo": {
            "custoTotal": 1450.00,
            "itens": [
                {
                    "nome": "Plano de Saúde - Premium",
                    "valor": 600.00
                }
            ]
        }
    }
}
```

#### POST /rh/colaboradores/adesao/finalizar

**✅ IMPLEMENTADO E FUNCIONAL**

**Descrição**: Finaliza processo de adesão e cria colaborador.

**Parâmetros**:
```json
{
    "sessionId": "string (required)",
    "aceitarTermos": "boolean (required, must be true)",
    "autorizarDesconto": "boolean (required, must be true)",
    "confirmarDados": "boolean (required, must be true)"
}
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "colaboradorId": 123,
    "message": "Adesão finalizada com sucesso! Aguardando aprovação.",
    "protocolo": "bb6e4334-a916-4a0e-9a8f-e6ef6b9509bb",
    "redirectUrl": "/rh/colaboradores/adesao/status/bb6e4334-a916-4a0e-9a8f-e6ef6b9509bb"
}
```

---

## 📄 APIs de Documentos

### Funcionalidades Implementadas

- ✅ **Upload com Progress Bar**: Barra de progresso durante upload
- ✅ **Validação de Tipo**: PDF, JPG, JPEG, PNG
- ✅ **Validação de Tamanho**: Máximo 10MB
- ✅ **Preview de Arquivos**: Visualização de imagens e PDFs
- ✅ **Sincronização com DTO**: Validação automática para finalização

### Endpoints Detalhados

```
POST   /api/rh/colaboradores/adesao/documentos/upload           ✅ Upload
DELETE /api/rh/colaboradores/adesao/documentos/remover/{tipo}   ✅ Remover  
GET    /api/rh/colaboradores/adesao/documentos/status           ✅ Status
GET    /api/rh/colaboradores/adesao/documentos/visualizar/{tipo} ✅ Visualizar
DELETE /api/rh/colaboradores/adesao/documentos/limpar           ✅ Limpar sessão
```

---

## 🎁 APIs de Benefícios

### Sistema de Cálculos

**✅ IMPLEMENTADO E FUNCIONAL**

#### Planos de Saúde
- **Busca por Código**: Sistema identifica planos por código (`premium`, `basico`)
- **Cálculo Automático**: Valor titular + dependentes
- **Percentual Empresa**: 80% empresa, 20% colaborador para titular
- **Dependentes**: 100% colaborador

#### Vales (Refeição/Transporte)
- **Extração de Valores**: IDs como `vale-500` extraem valor R$ 500,00
- **Validação**: Apenas valores predefinidos são aceitos
- **Flexibilidade**: Sistema adaptável a novos valores

### Endpoints Detalhados

```
GET    /api/rh/colaboradores/adesao/beneficios/disponiveis      ✅ Listar disponíveis
GET    /api/rh/colaboradores/adesao/beneficios/resumo           ✅ Resumo com cálculos
POST   /api/rh/colaboradores/adesao/beneficios/calcular         ✅ Calcular totais
GET    /api/rh/colaboradores/adesao/beneficios/sessao           ✅ Benefícios da sessão
POST   /rh/colaboradores/adesao/beneficios                      ✅ Salvar seleção
```

---

## 🔄 APIs de Workflow

### ProcessoAdesao

**✅ IMPLEMENTADO E FUNCIONAL**

O sistema mantém controle completo do workflow através da entidade `ProcessoAdesao`:

#### Estados do Processo
- `EM_ANDAMENTO`: Colaborador preenchendo dados
- `AGUARDANDO_APROVACAO`: Enviado para RH
- `APROVADO`: Aprovado pelo RH
- `REJEITADO`: Rejeitado com motivo

#### Etapas do Processo
- `DADOS_PESSOAIS` → `DOCUMENTOS` → `BENEFICIOS` → `REVISAO` → `FINALIZADO`

### Endpoints de Workflow

```
GET    /rh/colaboradores/adesao/debug/session/{sessionId}       ✅ Debug completo
POST   /rh/colaboradores/adesao/cancelar                        ✅ Cancelar processo
GET    /rh/colaboradores/adesao/status/{sessionId}              ✅ Status atual
```

---

## 📊 Códigos de Resposta

### HTTP Status Codes

- **200 OK**: Operação bem-sucedida
- **400 Bad Request**: Dados inválidos ou faltando
- **401 Unauthorized**: Não autenticado
- **403 Forbidden**: Sem permissão
- **404 Not Found**: Recurso não encontrado
- **500 Internal Server Error**: Erro interno do servidor

### Códigos de Erro Específicos

```json
{
    "success": false,
    "message": "Documento obrigatório não encontrado",
    "errorCode": "DOCUMENTO_OBRIGATORIO_FALTANDO",
    "details": {
        "documentosFaltando": ["RG", "CPF"]
    }
}
```

---

## 💡 Exemplos de Uso

### Fluxo Completo JavaScript

```javascript
// 1. Salvar dados pessoais
function salvarDadosPessoais() {
    $.ajax({
        url: '/rh/colaboradores/adesao/dados-pessoais',
        method: 'POST',
        data: $('#formDadosPessoais').serialize(),
        success: function(response) {
            if (response.success) {
                window.location.href = 
                    `/rh/colaboradores/adesao/documentos?sessionId=${response.data.sessionId}`;
            }
        }
    });
}

// 2. Upload de documento
function uploadDocumento(arquivo, tipo, sessionId) {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    formData.append('tipo', tipo);
    formData.append('sessionId', sessionId);
    
    $.ajax({
        url: '/api/rh/colaboradores/adesao/documentos/upload',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.success) {
                mostrarSucesso('Documento enviado com sucesso!');
                verificarDocumentosObrigatorios();
            }
        }
    });
}

// 3. Calcular benefícios
function calcularBeneficios(sessionId) {
    $.ajax({
        url: '/api/rh/colaboradores/adesao/beneficios/resumo',
        method: 'GET',
        data: { sessionId: sessionId },
        success: function(response) {
            if (response.success) {
                preencherResumoBeneficios(response.resumo);
            }
        }
    });
}

// 4. Finalizar adesão
function finalizarAdesao(sessionId) {
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
            if (response.success) {
                $('#protocoloAdesao').text(response.protocolo);
                $('#successModal').modal('show');
            }
        }
    });
}
```

### Integração com Formulários

```javascript
// Máscaras de input
$('#cpf').mask('000.000.000-00');
$('#telefone').mask('(00) 00000-0000'); 
$('#cep').mask('00000-000');

// Integração ViaCEP
$('#cep').blur(function() {
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

// Validação de formulário
$('#formDadosPessoais').validate({
    rules: {
        nome: { required: true, minlength: 3 },
        cpf: { required: true, cpf: true },
        email: { required: true, email: true },
        dataNascimento: { required: true, date: true }
    },
    messages: {
        nome: {
            required: "Nome é obrigatório",
            minlength: "Nome deve ter pelo menos 3 caracteres"
        },
        cpf: {
            required: "CPF é obrigatório",
            cpf: "CPF inválido"
        }
    }
});
```

---

## 🔍 Debug e Monitoramento

### Endpoint de Debug

**GET /rh/colaboradores/adesao/debug/session/{sessionId}**

Retorna informações completas para diagnóstico:

```json
{
    "success": true,
    "sessionExists": true,
    "hasCompleteData": true,
    "currentStage": "REVISAO",
    "processStatus": "EM_ANDAMENTO", 
    "hasBenefits": true,
    "benefitsCount": 3,
    "hasWorkflowProcess": true,
    "workflowStage": "REVISAO",
    "workflowStatus": "EM_ANDAMENTO",
    "isFinalizeable": true
}
```

### Logs Implementados

O sistema possui logs detalhados em todos os pontos críticos:

```java
// Exemplo de logs no processo
logger.info("=== SINCRONIZAÇÃO DE DOCUMENTOS ====");
logger.info("SessionId: {}", sessionId);
logger.info("Documentos encontrados no sistema: {}", documentosEnviados.size());
logger.info("Pode finalizar? {}", dadosAdesao.isDocumentosObrigatoriosCompletos());
```

---

## ✅ Status Final das APIs

### 🎯 100% Implementado e Funcional

- ✅ **Processo de Adesão Completo**: 4 etapas funcionais
- ✅ **Validações Robustas**: Client-side e server-side
- ✅ **Upload de Documentos**: Com preview e validação
- ✅ **Cálculos de Benefícios**: Automáticos e precisos
- ✅ **Workflow de Estados**: Controle completo
- ✅ **Logs e Auditoria**: Rastreamento detalhado
- ✅ **Tratamento de Erros**: Respostas padronizadas
- ✅ **Sincronização de Dados**: Entre camadas

### 🚀 Melhorias Futuras

- 📧 **Notificações por Email**: Confirmações automáticas
- 📱 **WebSocket**: Atualizações em tempo real
- 🔐 **API Keys**: Autenticação para integração externa
- 📊 **Métricas**: Endpoints de analytics
- 🔄 **Versionamento**: API versioning strategy

---

**APIs e Endpoints - ERP Corporativo**  
**Desenvolvimento**: Jasiel Silva  
**Última Atualização**: Setembro 2025  
**Status**: ✅ PRODUÇÃO - TOTALMENTE FUNCIONAL