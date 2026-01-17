I will update the `src/main/resources/templates/dashboard/index.html` file to reflect the requested changes:

1. **Update Header**:

   * Change the main title from "Dashboard Executivo" to **"Dashboard Gerencial"**.

   * Add the subtitles/sections **"Visão Geral"** and **"Panorama Geral (Centro de Comando)"** to the header area.

2. **Update Cards (KPIs)**:

   * **Keep existing dynamic cards** (Faturamento, Inadimplência, Clientes, etc.) as requested ("MANTER OS QUE JA TEM").

   * **Add new static cards** with the provided fictitious data, using the existing `stat-card` design ("NO NOVO MODELO") to ensure visual consistency.

   * I will assign appropriate FontAwesome icons to each new card to match the current style.

   **New Cards List:**

   * **Novos Contatos**: 8 (Icon: `fa-address-book`)

   * **A Pagar (Médicos)**: R$ 12.450 (Icon: `fa-file-invoice-dollar`)

   * **Aguardando Laudo**: 5 (Icon: `fa-file-medical`)

   * **Pendências (Docs)**: 42 (Icon: `fa-file-contract`)

   * **Contratos a Assinar**: 12 (Icon: `fa-file-signature`)

   * **A Receber (Seg)**: R$ 450k (Icon: `fa-hand-holding-usd`)

   * **Processos Ativos**: 1.240 (Icon: `fa-briefcase`)

   * **Contencioso / Civil**: 85 (Icon: `fa-gavel`)

   * **Arquivados (Deferidos)**: 3.450 (Icon: `fa-archive`)

   * **Prazos Críticos (48h)**: 15 (Icon: `fa-hourglass-half`)

   * **Proc/Func (Dia)**: 12 (Icon: `fa-chart-bar`)

3. **Layout**:

   * All cards will be displayed in the main grid (`stats-row`), mixing the existing real-data cards with the new fictitious ones (or appending them), ensuring a unified "Centro de Comando" view.

