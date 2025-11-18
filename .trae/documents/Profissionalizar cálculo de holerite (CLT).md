## Diagnóstico atual
- INSS: cálculo simplificado aplicando alíquota única sobre o salário bruto com teto (FolhaPagamentoService.java:305–317). CLT exige cálculo progressivo por faixas.
- IRRF: faixas e dedução fixa estão modeladas (FolhaPagamentoService.java:322–337), porém base de cálculo desconsidera dedução por dependentes, pensão alimentícia, outras deduções legais.
- FGTS: calculado como “desconto” de 8% (FolhaPagamentoService.java:260), mas FGTS é depósito patronal (não deve reduzir salário líquido do colaborador).
- VT/VR/Plano: aplicado conforme registros, mas sem limites legais (VT limitado a 6% do salário base) e políticas corporativas parametrizadas.
- Horas extras, dias úteis, atrasos/faltas: simplificados/placeholder.

## Proposta técnica
1. Criar motor de cálculo no app:
- Novo serviço `HoleriteCalculoService` responsável por todo o cálculo CLT.
- Ler tabelas parametrizadas de INSS/IRRF/deduções do banco (data-driven), sem procedure.
- Expor APIs puras e testáveis: `calcularProventos`, `calcularDescontos`, `calcularLiquido`.

2. Tabelas de referência no banco:
- `tabela_inss`: faixas, alíquota, parcela a deduzir (ou cálculo progressivo por faixa).
- `tabela_irrf`: faixas, alíquota, dedução fixa, dedução por dependente vigente.
- `parametros_beneficios`: regras VT (limite 6%), VR, plano de saúde (co-participação), política de adicionais.
- `feriados_nacionais`: para cálculo de dias úteis reais.

3. Correções de regras CLT
- INSS: cálculo progressivo por faixa com teto; considerar 13º (módulo específico).
- IRRF: base = salário bruto + proventos − INSS − pensão − dependentes (valor vigente) − outras deduções legais; aplicar faixa e dedução.
- FGTS: remover de “descontos” do colaborador; registrar como `fgts_deposito_patronal` no resumo/relatórios.
- VT: limitar desconto a 6% do salário-base; subsidiar excedente se aplicável.
- VR/Plano: parametrizar co-participações e subsídios.
- Adicionais: noturno, periculosidade, insalubridade por política.

4. Integrações e refactor
- Substituir chamadas de `FolhaPagamentoService.calcular*` por `HoleriteCalculoService`.
- Atualizar `ResumoFolhaService` para novos campos (FGTS patronal separado).
- Ajustar templates: remover FGTS de área “Descontos” e apresentar em seção informativa.

5. Testes e validação
- Unit tests cobrindo faixas INSS/IRRF, dependentes, VT 6%, casos borda (teto INSS, transições IRRF).
- Testes de integração para geração da folha/holerites.
- Scripts de migração de dados de parâmetros (seed das tabelas).

## Alternativa (procedure MySQL)
- Não recomendado para regra complexa e evolutiva. Melhor manter regra no app e parâmetros no banco. Procedures dificultam versionamento/testes, aumentam acoplamento ao SGBD.

## Entregáveis
- Serviço de cálculo CLT com tabelas parametrizadas.
- Correções de FGTS e VT.
- Templates ajustados (PDF e Web).
- Suite de testes cobrindo regras.

## Próximos passos
- Implementar `HoleriteCalculoService`, criar tabelas e seeds, refatorar `FolhaPagamentoService`, ajustar templates e adicionar testes. Confirma para começar a implementação?