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
    "genero": "string (required, enum: M|F|O)",
    "cpf": "string (required, format: 000.000.000-00)",
    "rg": "string (required, max:20)",
    "dataNascimento": "date (required, format: YYYY-MM-DD)",
    "estadoCivil": "string (required, enum: solteiro|casado|divorciado|viuvo)",
    "email": "email (required, max:255)",
    "telefone": "string (required, format: (00) 00000-0000)",
    "cargo": "string (required, max:100)",
    "departamento": "string (required, enum: ti|rh|financeiro|comercial|operacional)",
    "dataAdmissao": "date (required, format: YYYY-MM-DD)",
    "salario": "decimal (required, min:0)",
    "tipoContrato": "string (required, enum: clt|pj|estagio|terceirizado)",
    "cargaHoraria": "string (required, enum: 20h|30h|40h|44h)",
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
tipo: string (required, enum: rg|cpf|comprovante_residencia|carteira_trabalho|titulo_eleitor|certificado_reservista|comprovante_escolaridade|certidao_nascimento_casamento)
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

**Método**: `DELETE`

**Parâmetros**:
```
id: integer (required, path parameter)
sessionId: string (required, query parameter)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Documento removido com sucesso"
}
```

---

## ETAPA 3: Benefícios

### GET /api/rh/colaboradores/adesao/beneficios/disponiveis

**Descrição**: Lista todos os benefícios disponíveis para seleção.

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
        "beneficios": [
            {
                "id": "plano-saude",
                "nome": "Plano de Saúde",
                "descricao": "Assistência médica completa",
                "icone": "fas fa-heartbeat",
                "obrigatorio": false,
                "planos": [
                    {
                        "id": "basico",
                        "nome": "Plano Básico",
                        "descricao": "Consultas, exames básicos",
                        "valor": 150.00,
                        "valorDependente": 100.00
                    },
                    {
                        "id": "intermediario",
                        "nome": "Plano Intermediário",
                        "descricao": "Consultas, exames, internações",
                        "valor": 250.00,
                        "valorDependente": 150.00
                    },
                    {
                        "id": "premium",
                        "nome": "Plano Premium",
                        "descricao": "Cobertura completa, rede ampliada",
                        "valor": 400.00,
                        "valorDependente": 200.00
                    }
                ],
                "permiteDependendes": true
            },
            {
                "id": "vale-refeicao",
                "nome": "Vale Refeição",
                "descricao": "Auxílio alimentação mensal",
                "icone": "fas fa-utensils",
                "obrigatorio": false,
                "planos": [
                    {
                        "id": "300",
                        "nome": "Vale R$ 300,00",
                        "descricao": "R$ 15,00 por dia útil",
                        "valor": 300.00
                    },
                    {
                        "id": "500",
                        "nome": "Vale R$ 500,00",
                        "descricao": "R$ 25,00 por dia útil",
                        "valor": 500.00
                    }
                ],
                "permiteDependendes": false
            }
        ]
    }
}
```

### GET /api/rh/colaboradores/adesao/beneficios/sessao

**Descrição**: Recupera benefícios já selecionados na sessão atual.

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
        "beneficiosSelecionados": {
            "plano-saude": {
                "plano": "intermediario",
                "dependentes": [
                    {
                        "nome": "Maria Silva",
                        "parentesco": "conjuge",
                        "dataNascimento": "1990-05-15"
                    }
                ]
            },
            "vale-refeicao": {
                "plano": "500",
                "dependentes": []
            }
        }
    }
}
```

### POST /api/rh/colaboradores/adesao/beneficios/calcular

**Descrição**: Calcula o valor total dos benefícios selecionados.

**Método**: `POST`

**Parâmetros**:
```json
{
    "sessionId": "string (required)",
    "beneficios": "string (required, JSON encoded)"
}
```

**Exemplo de beneficios**:
```json
{
    "plano-saude": {
        "plano": "intermediario",
        "dependentes": [
            {
                "nome": "Maria Silva",
                "parentesco": "conjuge",
                "dataNascimento": "1990-05-15"
            }
        ]
    },
    "vale-refeicao": {
        "plano": "500",
        "dependentes": []
    }
}
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "data": {
        "resumo": {
            "itens": [
                {
                    "beneficio": "plano-saude",
                    "nome": "Plano de Saúde - Intermediário",
                    "descricao": "Titular",
                    "valor": 250.00
                },
                {
                    "beneficio": "plano-saude",
                    "nome": "Plano de Saúde - Intermediário",
                    "descricao": "Dependente: Maria Silva",
                    "valor": 150.00
                },
                {
                    "beneficio": "vale-refeicao",
                    "nome": "Vale Refeição",
                    "descricao": "R$ 500,00 mensal",
                    "valor": 500.00
                }
            ],
            "totalMensal": 900.00,
            "totalAnual": 10800.00
        }
    }
}
```

### POST /rh/colaboradores/adesao/beneficios

**Descrição**: Salva os benefícios selecionados definitivamente.

**Método**: `POST`

**Parâmetros**:
```json
{
    "sessionId": "string (required)",
    "beneficios": "string (required, JSON encoded)"
}
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Benefícios salvos com sucesso",
    "data": {
        "totalMensal": 900.00,
        "proximaEtapa": "/rh/colaboradores/adesao/revisao"
    }
}
```

### GET /api/rh/colaboradores/adesao/beneficios/resumo

**Descrição**: Obtém resumo dos benefícios selecionados para a tela de revisão.

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
        "beneficios": [
            {
                "id": "plano-saude",
                "nome": "Plano de Saúde",
                "plano": "Intermediário",
                "valor": 250.00,
                "dependentes": [
                    {
                        "nome": "Maria Silva",
                        "parentesco": "Cônjuge",
                        "valor": 150.00
                    }
                ]
            }
        ],
        "totalMensal": 900.00
    }
}
```

---

## ETAPA 4: Revisão

### GET /rh/colaboradores/adesao/revisao/{sessionId}

**Descrição**: Carrega todos os dados para revisão final.

**Método**: `GET`

**Parâmetros**:
```
sessionId: string (required, path parameter)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "data": {
        "dadosPessoais": {
            "nome": "João Silva Santos",
            "cpf": "123.456.789-00",
            "email": "joao.silva@email.com",
            "telefone": "(11) 99999-9999",
            "cargo": "Desenvolvedor",
            "departamento": "Tecnologia da Informação",
            "salario": 5000.00,
            "endereco": "Rua das Flores, 123 - Centro - São Paulo/SP"
        },
        "documentos": [
            {
                "tipo": "RG",
                "nomeArquivo": "rg_joao_silva.pdf",
                "tamanho": "1.2MB",
                "status": "enviado"
            }
        ],
        "beneficios": {
            "totalMensal": 900.00,
            "itens": [
                {
                    "nome": "Plano de Saúde - Intermediário",
                    "valor": 400.00
                }
            ]
        },
        "etapasCompletas": {
            "dadosPessoais": true,
            "documentos": true,
            "beneficios": true
        }
    }
}
```

### POST /rh/colaboradores/adesao/finalizar

**Descrição**: Finaliza o processo de adesão.

**Método**: `POST`

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
    "message": "Adesão finalizada com sucesso",
    "data": {
        "protocolo": "ADH-2025-001234",
        "dataFinalizacao": "2025-01-27T15:30:00Z",
        "status": "AGUARDANDO_APROVACAO",
        "proximaEtapa": "/rh/colaboradores/adesao/status"
    }
}
```

### POST /rh/colaboradores/adesao/cancelar

**Descrição**: Cancela o processo de adesão.

**Método**: `POST`

**Parâmetros**:
```json
{
    "sessionId": "string (required)",
    "motivo": "string (optional)"
}
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "message": "Processo cancelado com sucesso"
}
```

---

## ETAPA 5: Status e Acompanhamento

### GET /rh/colaboradores/adesao/api/status/{sessionId}

**Descrição**: Obtém o status atual do processo de adesão.

**Método**: `GET`

**Parâmetros**:
```
sessionId: string (required, path parameter)
```

**Resposta de Sucesso**:
```json
{
    "success": true,
    "data": {
        "status": "AGUARDANDO_APROVACAO",
        "protocolo": "ADH-2025-001234",
        "dataSubmissao": "2025-01-27T15:30:00Z",
        "ultimaAtualizacao": "2025-01-27T16:45:00Z",
        "etapaAtual": "aprovacao",
        "progresso": 75,
        "colaborador": {
            "nome": "João Silva Santos",
            "cpf": "123.456.789-00",
            "cargo": "Desenvolvedor",
            "departamento": "Tecnologia da Informação"
        },
        "etapas": [
            {
                "nome": "Dados Pessoais",
                "status": "CONCLUIDO",
                "dataFinalizacao": "2025-01-27T14:00:00Z"
            },
            {
                "nome": "Documentos",
                "status": "CONCLUIDO",
                "dataFinalizacao": "2025-01-27T14:30:00Z"
            },
            {
                "nome": "Benefícios",
                "status": "CONCLUIDO",
                "dataFinalizacao": "2025-01-27T15:00:00Z"
            },
            {
                "nome": "Aprovação",
                "status": "EM_ANDAMENTO",
                "dataInicio": "2025-01-27T15:30:00Z"
            },
            {
                "nome": "Finalização",
                "status": "PENDENTE"
            }
        ],
        "mensagem": {
            "tipo": "info",
            "titulo": "Aguardando Aprovação",
            "descricao": "Seu processo está sendo analisado pelo RH. Você será notificado quando houver atualizações."
        },
        "historico": [
            {
                "data": "2025-01-27T15:30:00Z",
                "evento": "Processo submetido para aprovação",
                "usuario": "Sistema"
            },
            {
                "data": "2025-01-27T15:00:00Z",
                "evento": "Benefícios selecionados",
                "usuario": "João Silva Santos"
            }
        ]
    }
}
```

**Status Possíveis**:
- `INICIADO`: Processo iniciado
- `EM_ANDAMENTO`: Em preenchimento
- `AGUARDANDO_APROVACAO`: Submetido para análise
- `APROVADO`: Aprovado pelo RH
- `REJEITADO`: Rejeitado (necessita correções)
- `FINALIZADO`: Processo concluído
- `CANCELADO`: Cancelado pelo usuário

---

## APIs de Navegação

### GET /rh/colaboradores/adesao/dados-pessoais

**Descrição**: Carrega a página de dados pessoais.

**Parâmetros**:
```
sessionId: string (optional, query parameter)
```

### GET /rh/colaboradores/adesao/documentos

**Descrição**: Carrega a página de documentos.

**Parâmetros**:
```
sessionId: string (required, query parameter)
```

### GET /rh/colaboradores/adesao/beneficios

**Descrição**: Carrega a página de benefícios.

**Parâmetros**:
```
sessionId: string (required, query parameter)
```

### GET /rh/colaboradores/adesao/revisao

**Descrição**: Carrega a página de revisão.

**Parâmetros**:
```
sessionId: string (required, query parameter)
```

### GET /rh/colaboradores/adesao/status

**Descrição**: Carrega a página de status.

**Parâmetros**:
```
sessionId: string (required, query parameter)
```

---

## APIs de Integração Externa

### ViaCEP Integration

**URL**: `https://viacep.com.br/ws/{cep}/json/`

**Método**: `GET`

**Uso**: Preenchimento automático de endereço baseado no CEP.

**Resposta**:
```json
{
    "cep": "01310-100",
    "logradouro": "Avenida Paulista",
    "complemento": "",
    "bairro": "Bela Vista",
    "localidade": "São Paulo",
    "uf": "SP",
    "ibge": "3550308",
    "gia": "1004",
    "ddd": "11",
    "siafi": "7107"
}
```

---

## Códigos de Erro Comuns

### Códigos HTTP
- `200`: Sucesso
- `400`: Dados inválidos
- `401`: Não autorizado
- `403`: Acesso negado
- `404`: Recurso não encontrado
- `422`: Erro de validação
- `500`: Erro interno do servidor

### Códigos de Erro Específicos

```json
{
    "INVALID_SESSION": "Sessão inválida ou expirada",
    "STEP_NOT_COMPLETED": "Etapa anterior não foi concluída",
    "INVALID_CPF": "CPF inválido",
    "EMAIL_ALREADY_EXISTS": "E-mail já cadastrado no sistema",
    "FILE_TOO_LARGE": "Arquivo muito grande (máx. 5MB)",
    "INVALID_FILE_TYPE": "Tipo de arquivo não permitido",
    "REQUIRED_DOCUMENTS_MISSING": "Documentos obrigatórios não enviados",
    "NO_BENEFITS_SELECTED": "Nenhum benefício selecionado",
    "PLAN_NOT_SELECTED": "Plano não selecionado para benefício ativo",
    "TERMS_NOT_ACCEPTED": "Termos e condições não aceitos",
    "PROCESS_ALREADY_FINALIZED": "Processo já foi finalizado"
}
```

---

## Segurança e Autenticação

### CSRF Protection
Todas as requisições POST devem incluir o token CSRF:
```javascript
$.ajaxSetup({
    headers: {
        'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
    }
});
```

### Validação de Sessão
Todas as APIs validam:
- SessionId válido e ativo
- Timeout de sessão (2 horas de inatividade)
- Integridade dos dados entre etapas

### Rate Limiting
- Upload de arquivos: 10 por minuto
- APIs de cálculo: 30 por minuto
- APIs de consulta: 60 por minuto

### Sanitização
- Todos os inputs são sanitizados
- Validação de tipos de arquivo por MIME type
- Prevenção de XSS e SQL Injection

---

## Monitoramento e Logs

### Eventos Logados
- Início de processo
- Conclusão de cada etapa
- Upload de documentos
- Seleção de benefícios
- Finalização/cancelamento
- Erros e exceções

### Métricas
- Tempo médio por etapa
- Taxa de abandono por etapa
- Documentos mais rejeitados
- Benefícios mais selecionados

---

**Versão**: 1.0  
**Data**: Janeiro 2025  
**Status**: Documentação Completa das APIs