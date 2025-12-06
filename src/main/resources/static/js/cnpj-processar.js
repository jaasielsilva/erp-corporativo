(() => {
  const btn = document.getElementById('btn-processar');
  const btnPause = document.getElementById('btn-pause');
  const btnResume = document.getElementById('btn-resume');
  const btnCancel = document.getElementById('btn-cancel');
  const btnExportar = document.getElementById('btn-exportar');
  const statusLabel = document.getElementById('status-label');
  const toastEl = document.getElementById('toast');
  const toastBody = document.getElementById('toast-body');
  const toast = new bootstrap.Toast(toastEl);
  let timer = null;
  const progressBar = document.getElementById('progress-bar');
  const statusDetails = document.getElementById('status-details');
  const reportPanel = document.getElementById('report-panel');
  const invalidList = document.getElementById('invalid-list');
  const errorList = document.getElementById('error-list');
  const concurrencyRange = document.getElementById('concurrency');
  const concurrencyLabel = document.getElementById('concurrency-label');
  const btnSanitizarDry = document.getElementById('btn-sanitizar-dry');
  const btnSanitizarApply = document.getElementById('btn-sanitizar-apply');
  let lastInvalidSamples = [];
  let lastErrorSamples = [];

  function showToast(msg) {
    toastBody.textContent = msg;
    toast.show();
  }

  async function iniciar() {
    try {
      const concurrency = Number(concurrencyRange.value || 4);
      btn.disabled = true; btn.classList.add('disabled');
      statusLabel.textContent = 'Iniciando...';
      statusLabel.className = 'badge bg-info';
      const resp = await fetch(`/utilidades/processar?concurrency=${concurrency}`, { method: 'POST' });
      if (resp.status === 202) {
        const body = await resp.json().catch(() => ({ message: 'Processamento iniciado', protocol: null }));
        const protoMsg = body.protocol ? ` — Protocolo: ${body.protocol}` : '';
        showToast(`Processamento iniciado${protoMsg}`);
        statusLabel.textContent = 'Processando...';
        statusLabel.className = 'badge bg-warning';
        iniciarPolling();
      } else {
        showToast('Falha ao iniciar processamento');
      }
    } catch (e) {
      showToast('Erro ao iniciar processamento');
    }
    finally { btn.disabled = false; btn.classList.remove('disabled'); }
  }

  async function consultarStatus() {
    try {
      const resp = await fetch('/utilidades/processamento-status');
      const data = await resp.json();
      if (data.running) {
        statusLabel.textContent = `Processando (${data.processed})`;
        statusLabel.className = 'badge bg-warning';
      } else {
        statusLabel.textContent = `Concluído (${data.processed}) | Inválidos: ${data.invalidCount} | Erros: ${data.errorCount}`;
        statusLabel.className = 'badge bg-success';
        pararPolling();
      }
      const started = data.startedAt ? new Date(data.startedAt) : null;
      const finished = data.finishedAt ? new Date(data.finishedAt) : null;
      const duration = data.durationMs != null ? Math.round(data.durationMs/1000) : null;
      statusDetails.innerHTML = `Início: ${started ? started.toLocaleString('pt-BR') : '-'} | Fim: ${finished ? finished.toLocaleString('pt-BR') : '-'} | Duração: ${duration != null ? duration + 's' : '-'}`;
      const pct = Math.min(100, Math.max(0, data.running ? 0 : 100));
      progressBar.style.width = `${pct}%`;
      progressBar.textContent = `${pct}%`;
      if (!data.running) showToast(`Processamento concluído — Sucessos: ${data.processed} | Inválidos: ${data.invalidCount} | Erros: ${data.errorCount}`);
      if (Array.isArray(data.invalidSamples) || Array.isArray(data.errorSamples)) {
        reportPanel.style.display = 'block';
        lastInvalidSamples = Array.isArray(data.invalidSamples) ? data.invalidSamples : [];
        lastErrorSamples = Array.isArray(data.errorSamples) ? data.errorSamples : [];
        invalidList.innerHTML = lastInvalidSamples.map(x => `<li class="list-group-item">${x || ''}</li>`).join('');
        errorList.innerHTML = lastErrorSamples.map(x => `<li class="list-group-item">${x || ''}</li>`).join('');
      }
    } catch (e) {
      // ignora erros de polling
    }
  }

  function iniciarPolling() {
    pararPolling();
    timer = setInterval(consultarStatus, 3000);
  }

  function pararPolling() {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  btn.addEventListener('click', iniciar);
  btnPause.addEventListener('click', async () => { try { await fetch('/utilidades/processar/pause', { method: 'POST' }); showToast('Pausado'); } catch (e) {} });
  btnResume.addEventListener('click', async () => { try { await fetch('/utilidades/processar/resume', { method: 'POST' }); showToast('Retomado'); } catch (e) {} });
  btnCancel.addEventListener('click', async () => { try { await fetch('/utilidades/processar/cancel', { method: 'POST' }); showToast('Cancelado'); pararPolling(); } catch (e) {} });
  concurrencyRange.addEventListener('input', () => { concurrencyLabel.textContent = concurrencyRange.value; });
  btnSanitizarDry.addEventListener('click', async () => {
    try {
      const resp = await fetch('/utilidades/sanitizar-cnpj?apply=false', { method: 'POST' });
      const data = await resp.json();
      showToast(`Simulação: total ${data.total}, normalizados ${data.normalized}, inválidos ${data.invalid}`);
    } catch { showToast('Falha na simulação de sanitização'); }
  });
  btnSanitizarApply.addEventListener('click', async () => {
    try {
      const resp = await fetch('/utilidades/sanitizar-cnpj?apply=true', { method: 'POST' });
      const data = await resp.json();
      showToast(`Aplicado: total ${data.total}, normalizados ${data.normalized}, inválidos ${data.invalid}`);
    } catch { showToast('Falha ao aplicar sanitização'); }
  });

  if (btnExportar) {
    btnExportar.addEventListener('click', () => {
      const rows = [];
      rows.push(['Tipo','Valor']);
      lastInvalidSamples.forEach(v => rows.push(['invalido', v || '']));
      lastErrorSamples.forEach(v => rows.push(['erro', v || '']));
      const csv = rows.map(r => r.map(x => '"' + String(x).replace(/"/g,'""') + '"').join(',')).join('\n');
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = 'pre-relatorio-cnpjs.csv';
      a.click();
      URL.revokeObjectURL(a.href);
    });
  }

  document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => { try { new bootstrap.Tooltip(el); } catch {} });
})();
