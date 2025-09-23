# Documentação do Sistema de Adesão de Colaboradores

## Visão Geral

O Sistema de Adesão de Colaboradores é uma aplicação web desenvolvida para automatizar e gerenciar o processo de onboarding de novos funcionários. O sistema permite que colaboradores preencham seus dados pessoais, enviem documentos obrigatórios, selecionem benefícios e acompanhem o status de aprovação de forma digital e integrada.

## Arquitetura do Sistema

### Estrutura de Arquivos
```
templates/rh/colaboradores/adesao/
├── inicio.html          # Etapa 1: Dados Pessoais
├── documentos.html      # Etapa 2: Upload de Documentos
├── beneficios.html      # Etapa 3: Seleção de Benefícios
├── revisao.html         # Etapa 4: Revisão Final
└── status.html          # Acompanhamento do Status
```

### Tecnologias Utilizadas
- **Frontend**: HTML5, CSS3, JavaScript (jQuery)
- **Framework CSS**: Bootstrap 5.3.0
- **Ícones**: Font Awesome 6.4.0
- **Template Engine**: Thymeleaf
- **APIs**: RESTful APIs para comunicação backend
- **Validações**: JavaScript client-side e server-side

## Fluxo do Processo

O processo de adesão segue um fluxo sequencial de 4 etapas principais:

```
1. Dados Pessoais → 2. Documentos → 3. Benefícios → 4. Revisão → Status
```

### Controle de Sessão
- Cada processo é identificado por um `sessionId` único
- O `sessionId` é passado entre as páginas via URL parameters
- Permite retomar o processo em qualquer etapa
- Validação de sessão em cada página

## Etapas Detalhadas

### 1. Dados Pessoais (inicio.html)

**Objetivo**: Coletar informações pessoais e profissionais do colaborador.

**Seções do Formulário**:
- **Dados Pessoais**: Nome, Gênero, CPF, RG, Data de Nascimento, Email, Telefone, Estado Civil
- **Dados Profissionais**: Cargo, Departamento, Data de Admissão, Salário, Tipo de Contrato, Carga Horária, Supervisor
- **Endereço**: CEP, Logradouro, Número, Complemento, Bairro, Cidade, Estado
- **Observações**: Campo livre para informações adicionais

**Funcionalidades**:
- Máscaras de input para CPF, telefone e CEP
- Integração com API ViaCEP para preenchimento automático do endereço
- Validação de campos obrigatórios
- Envio via AJAX para `/rh/colaboradores/adesao/dados-pessoais`
- Redirecionamento automático para próxima etapa

**Validações**:
- CPF válido
- Email válido
- Campos obrigatórios preenchidos
- Data de nascimento válida
- CEP válido

### 2. Documentos (documentos.html)

**Objetivo**: Upload e validação de documentos obrigatórios e opcionais.

**Documentos Obrigatórios**:
- RG (Registro Geral)
- CPF (Cadastro de Pessoa Física)
- Comprovante de Residência

**Documentos Opcionais**:
- Carteira de Trabalho
- Título de Eleitor
- Certificado de Reservista
- Comprovante de Escolaridade
- Certidão de Nascimento/Casamento

**Funcionalidades**:
- Upload via drag-and-drop ou seleção de arquivo
- Preview de imagens e PDFs
- Barra de progresso durante upload
- Validação de tipo de arquivo (PDF, JPG, JPEG, PNG)
- Validação de tamanho (máximo 5MB)
- Remoção de arquivos enviados
- Resumo de documentos enviados

**APIs Utilizadas**:
- `POST /api/rh/colaboradores/adesao/documentos/upload` - Upload de documento
- `DELETE /api/rh/colaboradores/adesao/documentos/remover/{tipo}` - Remover documento
- `GET /api/rh/colaboradores/adesao/documentos/status` - Status dos documentos
- `POST /rh/colaboradores/adesao/documentos` - Finalizar etapa

### 3. Benefícios (beneficios.html)

**Objetivo**: Seleção de benefícios corporativos e planos.

**Benefícios Disponíveis**:

1. **Plano de Saúde**:
   - Básico: R$ 150,00
   - Intermediário: R$ 250,00
   - Premium: R$ 400,00
   - Opção de incluir dependentes (+R$ 100,00 cada)

2. **Vale Refeição**:
   - R$ 300,00
   - R$ 500,00

3. **Vale Transporte**:
   - Básico: R$ 150,00
   - Intermediário: R$ 250,00
   - Premium: R$ 350,00

**Funcionalidades**:
- Toggle para ativar/desativar benefícios
- Seleção de planos para cada benefício
- Adição/remoção de dependentes (plano de saúde)
- Cálculo automático do valor total mensal
- Resumo detalhado dos benefícios selecionados

**APIs Utilizadas**:
- `GET /api/rh/colaboradores/adesao/beneficios/disponiveis` - Benefícios disponíveis
- `GET /api/rh/colaboradores/adesao/beneficios/sessao` - Benefícios já selecionados
- `POST /api/rh/colaboradores/adesao/beneficios/calcular` - Calcular total
- `POST /rh/colaboradores/adesao/beneficios` - Salvar seleção

### 4. Revisão (revisao.html)

**Objetivo**: Revisão final de todos os dados antes da submissão.

**Seções de Revisão**:
- **Dados Pessoais**: Resumo das informações coletadas
- **Documentos**: Lista de documentos enviados com status
- **Benefícios**: Resumo dos benefícios selecionados com valores
- **Total Mensal**: Valor total dos benefícios

**Funcionalidades**:
- Visualização completa de todos os dados
- Links para editar cada seção
- Termos e condições obrigatórios:
  - Aceitar termos e condições
  - Autorizar desconto em folha
  - Confirmar veracidade dos dados
- Finalização do processo
- Modal de sucesso com protocolo

**APIs Utilizadas**:
- `GET /rh/colaboradores/adesao/revisao/{sessionId}` - Dados para revisão
- `GET /api/rh/colaboradores/adesao/beneficios/resumo` - Resumo de benefícios
- `POST /rh/colaboradores/adesao/finalizar` - Finalizar processo
- `POST /rh/colaboradores/adesao/cancelar` - Cancelar processo

### 5. Status (status.html)

**Objetivo**: Acompanhamento do status de aprovação do processo.

**Status Possíveis**:
- `AGUARDANDO_APROVACAO`: Processo em análise (75% progresso)
- `APROVADO`: Processo aprovado (100% progresso)
- `REJEITADO`: Processo rejeitado (50% progresso)

**Funcionalidades**:
- Indicador visual de progresso
- Timeline das etapas concluídas
- Atualização automática a cada 30 segundos
- Botão de atualização manual
- Informações de contato do RH
- Impressão de comprovante
- Notificações do navegador (se permitido)

**APIs Utilizadas**:
- `GET /rh/colaboradores/adesao/api/status/{sessionId}` - Status atual do processo

## Componentes Visuais

### Indicador de Etapas
Todas as páginas do processo incluem um indicador visual das etapas:
- Etapas concluídas: ícone de check verde
- Etapa atual: número destacado em azul
- Etapas pendentes: número em cinza

### Responsividade
- Layout responsivo usando Bootstrap Grid
- Adaptação para dispositivos móveis
- Componentes otimizados para touch

### Feedback Visual
- Alertas de sucesso e erro
- Loading spinners durante operações
- Animações CSS para melhor UX
- Progress bars para uploads

## Segurança e Validações

### Client-side
- Validação de formatos de arquivo
- Validação de tamanho de arquivo
- Máscaras de input
- Validação de campos obrigatórios

### Server-side
- Validação de sessão
- Sanitização de dados
- Validação de tipos MIME
- Controle de acesso

## Integração com Backend

### Endpoints Principais
```
POST /rh/colaboradores/adesao/dados-pessoais
POST /api/rh/colaboradores/adesao/documentos/upload
DELETE /api/rh/colaboradores/adesao/documentos/remover/{tipo}
GET /api/rh/colaboradores/adesao/documentos/status
POST /rh/colaboradores/adesao/documentos
GET /api/rh/colaboradores/adesao/beneficios/disponiveis
POST /api/rh/colaboradores/adesao/beneficios/calcular
POST /rh/colaboradores/adesao/beneficios
GET /rh/colaboradores/adesao/revisao/{sessionId}
POST /rh/colaboradores/adesao/finalizar
GET /rh/colaboradores/adesao/api/status/{sessionId}
```

### Formato de Resposta
Todas as APIs seguem o padrão:
```json
{
  "success": boolean,
  "message": "string",
  "data": object
}
```

## Melhorias Futuras

1. **Notificações por Email**: Envio automático de emails em cada etapa
2. **Assinatura Digital**: Implementação de assinatura eletrônica
3. **Integração com RH**: Sincronização com sistema de RH existente
4. **Dashboard Administrativo**: Interface para gestão de processos
5. **Relatórios**: Geração de relatórios de adesões
6. **Auditoria**: Log completo de ações do usuário
7. **Multi-idioma**: Suporte a múltiplos idiomas
8. **PWA**: Transformar em Progressive Web App

## Considerações Técnicas

### Performance
- Lazy loading de imagens
- Compressão de arquivos CSS/JS
- Cache de APIs quando apropriado
- Otimização de imagens

### Acessibilidade
- Labels apropriados para screen readers
- Navegação por teclado
- Contraste adequado de cores
- Textos alternativos para imagens

### Manutenibilidade
- Código modular e reutilizável
- Comentários em português
- Padrões de nomenclatura consistentes
- Separação clara entre lógica e apresentação

---

**Versão**: 1.0  
**Data**: Janeiro 2025  
**Autor**: Sistema ERP Corporativo  
**Status**: Documentação Completa