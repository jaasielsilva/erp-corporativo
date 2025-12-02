(() => {
  const input = document.getElementById('cnpj-input');
  const btn = document.getElementById('btn-consultar');
  const toastEl = document.getElementById('toast');
  const toastBody = document.getElementById('toast-body');
  const toast = new bootstrap.Toast(toastEl);

  function showToast(msg) {
    toastBody.textContent = msg;
    toast.show();
  }

  function sanitize(cnpj) {
    return (cnpj || '').replace(/[^0-9]/g, '');
  }

  function formatCNPJ(s) {
    const v = sanitize(s).slice(0, 14);
    let r = '';
    for (let i = 0; i < v.length; i++) {
      r += v[i];
      if (i === 1) r += '.';
      else if (i === 4) r += '.';
      else if (i === 7) r += '/';
      else if (i === 11) r += '-';
    }
    return r;
  }

  function rawIndexFromCaret(masked, caret) {
    let count = 0;
    for (let i = 0; i < Math.min(caret, masked.length); i++) {
      if (/\d/.test(masked[i])) count++;
    }
    return count;
  }

  function caretFromRawIndex(masked, rawIdx) {
    if (rawIdx <= 0) return 0;
    let count = 0;
    for (let i = 0; i < masked.length; i++) {
      if (/\d/.test(masked[i])) {
        count++;
        if (count === rawIdx) return i + 1;
      }
    }
    return masked.length;
  }

  function isValidCnpj(cnpj) {
    const s = sanitize(cnpj);
    if (!s || s.length !== 14 || /^0{14}$/.test(s)) return false;
    const calc = (base, weights) => {
      let sum = 0;
      for (let i = 0; i < weights.length; i++) sum += Number(base[i]) * weights[i];
      const mod = sum % 11;
      return mod < 2 ? 0 : 11 - mod;
    };
    const d1 = calc(s.substring(0, 12), [5,4,3,2,9,8,7,6,5,4,3,2]);
    const d2 = calc(s.substring(0, 13), [6,5,4,3,2,9,8,7,6,5,4,3,2]);
    return Number(s[12]) === d1 && Number(s[13]) === d2;
  }

  async function consultar() {
    const masked = input.value;
    if (!isValidCnpj(masked)) {
      showToast('CNPJ inválido');
      return;
    }
    const cnpj = sanitize(masked);
    try {
      const resp = await fetch(`/cadastros/consultar?cnpj=${cnpj}`);
      if (!resp.ok) {
        const text = await resp.text();
        showToast(`Erro: ${text}`);
        return;
      }
      const data = await resp.json();
      document.getElementById('rs').textContent = data.razaoSocial || '';
      document.getElementById('nf').textContent = data.nomeFantasia || '';
      document.getElementById('sit').textContent = data.situacaoCadastral || '';

      const e = data.endereco || {};
      document.getElementById('endereco').textContent = [
        e.logradouro,
        e.numero,
        e.complemento,
        e.bairro,
        e.municipio,
        e.uf,
        e.cep
      ].filter(Boolean).join(', ');

      const cnaes = [];
      if (data.cnaePrincipal) {
        cnaes.push(`Principal: ${data.cnaePrincipal.codigo || ''} - ${data.cnaePrincipal.descricao || ''}`);
      }
      if (Array.isArray(data.cnaesSecundarios)) {
        data.cnaesSecundarios.forEach(c => {
          cnaes.push(`Secundário: ${c.codigo || ''} - ${c.descricao || ''}`);
        });
      }
      document.getElementById('cnaes').innerHTML = cnaes.map(s => `<div>${s}</div>`).join('');

      document.getElementById('resultado').style.display = 'block';
      showToast('Consulta concluída');
    } catch (err) {
      showToast('Falha ao consultar');
    }
  }

  btn.addEventListener('click', consultar);
  input.addEventListener('input', (e) => {
    const prev = e.target.value;
    const caret = e.target.selectionStart || 0;
    const rawIdx = rawIndexFromCaret(prev, caret);
    const formatted = formatCNPJ(prev);
    e.target.value = formatted;
    const newCaret = caretFromRawIndex(formatted, rawIdx);
    e.target.setSelectionRange(newCaret, newCaret);
  });
})();
