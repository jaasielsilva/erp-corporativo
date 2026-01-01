# Cadastro do Cliente

## Objetivo
- Cadastrar novos clientes com dados completos, garantindo validação, anexos e confirmação de criação.

## Pré-requisitos
- Acesso ao módulo Clientes.
- Documentos do cliente digitalizados (RG, comprovante de residência).

## Fluxo Passo a Passo
1. Acessar Clientes → Novo Cliente.
2. Preencher os campos obrigatórios.
3. Validar os dados (formatos, obrigatoriedade).
4. Anexar documentos.
5. Salvar e confirmar o cadastro.

## Campos Obrigatórios
- Nome completo
- CPF/CNPJ
- Endereço completo (logradouro, número, bairro, cidade, estado, CEP)
- Contatos: telefone e email

## Campos Opcionais
- Celular
- Pessoa para contato
- Observações
- Dados bancários (Agência, Conta, Chave Pix) para pagamentos futuros

## Validações
- CPF/CNPJ: formato válido
- Email: formato válido
- CEP: formato 00000-000
- Estado (UF): 2 letras
- Telefone/Celular: apenas dígitos e máscaras

## Anexos
- RG (frente/verso)
- Comprovante de residência (até 90 dias)
- Formatos aceitos: PDF, JPG, PNG
- Tamanho máximo recomendado: 10 MB por arquivo

## Template de Formulário (referência)
```
Nome completo*                  [__________________________]
Tipo de cliente*                [ PF | PJ ]
Nome Fantasia (PJ)              [__________________________]
CPF/CNPJ*                       [__________________________]
Email*                          [__________________________]
Telefone*                       [__________________________]
Celular                         [__________________________]
Pessoa para contato             [__________________________]

Endereço
  CEP*                          [_____-___]
  Logradouro*                   [__________________________]
  Número*                       [____________]
  Complemento                   [__________________________]
  Bairro*                       [__________________________]
  Cidade*                       [__________________________]
  Estado (UF)*                  [__]

Dados Bancários (opcional)
  Agência                       [____________________]
  Conta                         [____________________]
  Chave Pix                     [____________________]

Anexos
  RG                            [+ Selecionar arquivo]
  Comprovante de residência     [+ Selecionar arquivo]

[ Salvar ]   [ Cancelar ]
```

## Esquema Visual (wireframe)
```
┌───────────────────────────────────────────────┐
│ Clientes > Cadastrar Novo Cliente             │
├───────────────────────────────────────────────┤
│   Dados Pessoais                              │
│   [Nome*] [Tipo* PF/PJ] [CPF/CNPJ*]           │
│   [Email*] [Telefone*] [Celular]              │
│                                               │
│   Endereço                                    │
│   [CEP*] [Logradouro*] [Número*] [Comp]       │
│   [Bairro*] [Cidade*] [UF*]                   │
│                                               │
│   Bancários (opcional)                        │
│   [Agência] [Conta] [Chave Pix]               │
│                                               │
│   Anexos                                      │
│   [RG] [Comprovante]                          │
│                                               │
│ [Salvar] [Cancelar]                           │
└───────────────────────────────────────────────┘
```

## Instruções para Salvar e Confirmar
- Clique em Salvar.
- Verifique mensagem de sucesso.
- Revise o cadastro na lista de clientes.
- Se faltarem anexos, edite o cliente e adicione-os.

## Observações Importantes
- Campos com asterisco (*) são obrigatórios.
- Para PJ, preencha Nome Fantasia e Inscrições quando aplicável.
- Mantenha dados bancários atualizados para pagamentos.

