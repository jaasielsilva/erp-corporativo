document.addEventListener('DOMContentLoaded', function () {
  if (window.jQuery && typeof window.jQuery.fn.mask === 'function') {
    window.jQuery("[name='cnpj']").mask('00.000.000/0000-00');
    window.jQuery("[name='cep']").mask('00000-000');
    window.jQuery("[name='telefone']").mask('(00) 0000-0000');
    window.jQuery("[name='celular']").mask('(00) 00000-0000');
  }

  const cepInput = document.querySelector("[name='cep']");
  if (cepInput) {
    cepInput.addEventListener("blur", function () {
      const cep = this.value.replace(/\D/g, '');
      if (cep.length !== 8) return;

      fetch(`https://viacep.com.br/ws/${cep}/json/`)
        .then(res => res.json())
        .then(data => {
          if (data && data.erro) {
            if (typeof window.Swal !== 'undefined') {
              Swal.fire({ icon: 'warning', title: 'CEP não encontrado', text: 'Verifique o CEP digitado.' });
            }
            return;
          }

          const rua = document.querySelector("[name='rua']");
          const bairro = document.querySelector("[name='bairro']");
          const cidade = document.querySelector("[name='cidade']");
          const estado = document.querySelector("[name='estado']");

          if (rua) rua.value = (data && data.logradouro) || '';
          if (bairro) bairro.value = (data && data.bairro) || '';
          if (cidade) cidade.value = (data && data.localidade) || '';
          if (estado) estado.value = (data && data.uf) || '';
        })
        .catch(() => {
          if (typeof window.Swal !== 'undefined') {
            Swal.fire({ icon: 'error', title: 'Erro', text: 'Não foi possível consultar o CEP agora.' });
          }
        });
    });
  }

  const form = document.getElementById("formFornecedor") || document.querySelector("form");
  if (form) {
    form.addEventListener("submit", function (e) {
      const tel = (document.querySelector("[name='telefone']") || {}).value || '';
      const cel = (document.querySelector("[name='celular']") || {}).value || '';

      if (!tel.trim() && !cel.trim()) {
        e.preventDefault();
        if (typeof window.Swal !== 'undefined') {
          Swal.fire({ icon: 'warning', title: 'Atenção', text: 'Preencha pelo menos Telefone ou Celular.' });
        }
      }
    });
  }
});
