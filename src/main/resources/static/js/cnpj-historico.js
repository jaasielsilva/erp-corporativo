(() => {
  const toastEl = document.getElementById('toast');
  const toastBody = document.getElementById('toast-body');
  const toast = new bootstrap.Toast(toastEl);
  const input = document.getElementById('cnpj-hist');
  const btn = document.getElementById('btn-filtrar');
  const fromEl = document.getElementById('from-date');
  const toEl = document.getElementById('to-date');
  const tbody = document.getElementById('hist-body');

  function showToast(msg) { toastBody.textContent = msg; toast.show(); }
  function sanitize(v) { return (v || '').replace(/[^0-9]/g, ''); }

  async function load() {
    const cnpj = sanitize(input.value);
    if (!cnpj || cnpj.length !== 14) { showToast('Informe um CNPJ válido'); return; }
    const params = new URLSearchParams();
    params.set('cnpj', cnpj);
    if (fromEl.value && toEl.value) { params.set('from', fromEl.value); params.set('to', toEl.value); }
    try {
      const resp = await fetch(`/cadastros/historico?${params.toString()}`);
      if (!resp.ok) { showToast('Erro ao carregar histórico'); return; }
      const data = await resp.json();
      tbody.innerHTML = data.map(row => {
        const dt = row.consultedAt ? new Date(row.consultedAt) : null;
        const dtText = dt ? dt.toLocaleString('pt-BR') : '';
        const cnae = [row.cnaePrincipalCodigo, row.cnaePrincipalDescricao].filter(Boolean).join(' - ');
        const end = [row.logradouro, row.numero, row.complemento, row.bairro, row.municipio, row.uf, row.cep].filter(Boolean).join(', ');
        return `<tr>
          <td>${dtText}</td>
          <td>${row.razaoSocial || ''}</td>
          <td>${row.nomeFantasia || ''}</td>
          <td>${row.situacaoCadastral || ''}</td>
          <td>${cnae}</td>
          <td>${end}</td>
        </tr>`;
      }).join('');
      showToast(`Carregado ${data.length} registro(s)`);
    } catch { showToast('Falha ao carregar'); }
  }

  btn.addEventListener('click', load);
})();

