# Criação de Processo Jurídico

## Objetivo
- Registrar um novo processo com dados processuais, tipo de ação e anexos.

## Fluxo Passo a Passo
1. Acessar Jurídico → Novo Processo.
2. Selecionar o Tipo de Ação.
3. Preencher dados processuais:
   - Número do processo
   - Vara/Órgão competente
   - Partes envolvidas
   - Honorários advocatícios
4. Upload de documentos:
   - Petição inicial
   - Contratos
   - Provas
5. Salvar e revisar na lista.

## Campos Obrigatórios
- Tipo de ação*
- Número do processo*
- Vara/Órgão competente*
- Partes envolvidas*

## Campos Opcionais
- Honorários
- Observações
- Etiquetas/categorias

## Validações
- Número do processo: padrão CNJ (0000000-00.0000.0.00.0000) quando aplicável
- Tamanho máximo dos arquivos: 25 MB por documento
- Formatos aceitos: PDF, DOCX, JPG, PNG

## Upload de Documentos
- Petição inicial: PDF preferencial
- Contratos: PDF ou DOCX
- Provas: PDFs, imagens, vídeos (ver limites de tamanho)
- Nomenclatura clara (ver documento de requisitos)

## Modelo de Formulário
```
Tipo de Ação*                 [ Selecionar ]
Número do Processo*           [__________________________]
Vara/Órgão Competente*        [__________________________]
Partes Envolvidas*            [__________________________]
Honorários                    [R$ _____.__]
Observações                   [__________________________]

Documentos
  Petição inicial             [+ Selecionar arquivo]
  Contratos                   [+ Selecionar arquivo]
  Provas                      [+ Selecionar arquivo]

[ Salvar Processo ]   [ Cancelar ]
```

## Esquema Visual (wireframe)
```
┌───────────────────────────────────────────────┐
│ Jurídico > Novo Processo                      │
├───────────────────────────────────────────────┤
│   Tipo de Ação* [▼]                           │
│   Número* [____________________________]      │
│   Vara/Órgão* [_______________________]       │
│   Partes* [____________________________]      │
│   Honorários [R$ ______]                      │
│   Observações [_______________________]       │
│                                               │
│   Documentos                                  │
│   [Petição] [Contratos] [Provas]              │
│                                               │
│ [Salvar Processo] [Cancelar]                  │
└───────────────────────────────────────────────┘
```

## Modelos de Documentos
- Consulte [documentos-necessarios.md](./documentos-necessarios.md) para exemplos.

## Checklist de Criação
- [ ] Tipo de ação selecionado
- [ ] Número CNJ validado
- [ ] Vara/Órgão preenchido
- [ ] Partes envolvidas cadastradas
- [ ] Documentos anexados
- [ ] Honorários revisados

