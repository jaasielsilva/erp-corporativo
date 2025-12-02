(() => {
  const btn = document.getElementById('btn-processar');
  const statusLabel = document.getElementById('status-label');
  const toastEl = document.getElementById('toast');
  const toastBody = document.getElementById('toast-body');
  const toast = new bootstrap.Toast(toastEl);
  let timer = null;

  function showToast(msg) {
    toastBody.textContent = msg;
    toast.show();
  }

  async function iniciar() {
    try {
      const resp = await fetch('/utilidades/processar', { method: 'POST' });
      if (resp.status === 202) {
        showToast('Processamento iniciado');
        statusLabel.textContent = 'Processando...';
        statusLabel.className = 'badge bg-warning';
        iniciarPolling();
      } else {
        showToast('Falha ao iniciar processamento');
      }
    } catch (e) {
      showToast('Erro ao iniciar processamento');
    }
  }

  async function consultarStatus() {
    try {
      const resp = await fetch('/utilidades/processamento-status');
      const text = await resp.text();
      if (text.startsWith('EM_EXECUCAO')) {
        const count = text.split(':')[1] || '0';
        statusLabel.textContent = `Processando (${count})`;
        statusLabel.className = 'badge bg-warning';
      } else {
        const count = text.split(':')[1] || '0';
        statusLabel.textContent = `Concluído (${count})`;
        statusLabel.className = 'badge bg-success';
        showToast('Processamento concluído');
        pararPolling();
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
})();

