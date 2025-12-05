# Documentação de APIs e Endpoints

## Visão Geral

Este documento detalha todas as APIs e endpoints utilizados no sistema de adesão de colaboradores, incluindo métodos HTTP, parâmetros, respostas e exemplos de uso.

---

## Estrutura Base das APIs

### Padrões de Resposta

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

### Headers Padrão
```
Content-Type: application/json
X-Requested-With: XMLHttpRequest
CSRF-Token: {token}
```

---

## ETAPA 1: Dados Pessoais

### POST /rh/colaboradores/adesao/dados-pessoais

**Descrição**: Salva os dados pessoais do colaborador e inicia o processo de adesão.

**Método**: `POST`

**Parâmetros**:
```json
{
  "nome": "string (required, max:255)",
  "sexo": "string (required, enum: MASCULINO|FEMININO|OUTRO)",
  "cpf": "string (required, digits only, length: 11)",
  "rg": "string (optional, max:20)",
  "dataNascimento": "string (required, format: YYYY-MM-DD)",
  "estadoCivil": "string (optional, enum: solteiro|casado|divorciado|viuvo)",
  
  "email": "string (required, max:255)",
  "telefone": "string (required, format: (00) 00000-0000)",
  
  "cargoId": "integer (required)",
  "departamentoId": "integer (required)",
  "supervisorId": "integer (optional)",
  
  "dataAdmissao": "string (required, format: YYYY-MM-DD)",
  "salario": "decimal (required, min:0)",
  "tipoContrato": "string (optional, enum: clt|pj|estagio|terceirizado)",
  "cargaHoraria": "string (optional, enum: 20h|30h|40h|44h)",
  
  "cep": "string (required, format: 00000-000)",
  "logradouro": "string (required, max:255)",
  "numero": "string (optional, max:10)",
  "complemento": "string (optional, max:100)",
  "bairro": "string (optional, max:100)",
  "cidade": "string (required, max:100)",
  "estado": "string (required, size:2)",
  
  "pais": "string (optional, max:100)"
}

```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Dados pessoais salvos com sucesso",
    "data": {
        "sessionId": "uuid-v4",
        "etapaAtual": "documentos",
        "proximaEtapa": "/rh/colaboradores/adesao/documentos"
    }
}
```

**Validações**:
- CPF deve ser válido (algoritmo de validação)
- Email deve ser único no sistema
- Data de nascimento deve ser maior que 16 anos
- Data de admissão não pode ser futura
- CEP deve existir (integração com ViaCEP)

**Exemplo de Uso**:
```javascript
$.ajax({
    url: '/rh/colaboradores/adesao/dados-pessoais',
    method: 'POST',
    data: {
        nome: 'João Silva Santos',
        genero: 'M',
        cpf: '123.456.789-00',
        // ... outros campos
    },
    success: function(response) {
        if (response.success) {
            window.location.href = `/rh/colaboradores/adesao/documentos?sessionId=${response.data.sessionId}`;
        }
    }
});
```

---

## ETAPA 2: Documentos

### POST /api/rh/colaboradores/adesao/documentos/upload

**Descrição**: Upload de documentos obrigatórios e opcionais.

**Método**: `POST`

**Content-Type**: `multipart/form-data`

**Parâmetros**:
```
archivo: File (required, max:5MB, types: pdf|jpg|jpeg|png)
(tipo): string (required, enum: rg|cpf|comprovante_residencia|carteira_trabalho|titulo_eleitor|certificado_reservista|comprovante_escolaridade|certidao_nascimento_casamento)
sessionId: string (required, uuid)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Documento enviado com sucesso",
    "data": {
        "id": 123,
        "tipo": "rg",
        "nomeArquivo": "rg_joao_silva.pdf",
        "tamanho": "1.2MB",
        "url": "/storage/documentos/rg_joao_silva.pdf",
        "dataUpload": "2025-01-27T10:30:00Z"
    }
}
```

**Validações**:
- Arquivo deve ter extensão permitida
- Tamanho máximo de 5MB
- Tipo MIME deve corresponder à extensão
- SessionId deve ser válido e ativo
- Não permitir upload duplicado do mesmo tipo

### GET /api/rh/colaboradores/adesao/documentos/status

**Descrição**: Verifica o status dos documentos enviados.

**Método**: `GET`

**Parâmetros**:
```
sessionId: string (required, query parameter)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "data": {
        "documentos": [
            {
                "id": 123,
                "tipo": "rg",
                "nomeArquivo": "rg_joao_silva.pdf",
                "tamanho": "1.2MB",
                "status": "enviado",
                "dataUpload": "2025-01-27T10:30:00Z"
            }
        ],
        "obrigatorios": {
            "rg": true,
            "cpf": true,
            "comprovante_residencia": false
        },
        "podeProximaEtapa": false
    }
}
```

### DELETE /api/rh/colaboradores/adesao/documentos/{id}

**Descrição**: Remove um documento enviado.
