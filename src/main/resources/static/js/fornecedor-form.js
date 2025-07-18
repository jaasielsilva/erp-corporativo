  document.addEventListener('DOMContentLoaded', function () {
    // Aplicar máscaras
    VMasker(document.querySelector("[name='cnpj']")).maskPattern("99.999.999/9999-99");
    VMasker(document.querySelector("[name='telefone']")).maskPattern("(99) 9999-9999");
    VMasker(document.querySelector("[name='celular']")).maskPattern("(99) 99999-9999");
    VMasker(document.querySelector("[name='cep']")).maskPattern("99999-999");

    // Auto preencher endereço via ViaCEP
    const cepInput = document.querySelector("[name='cep']");
    cepInput.addEventListener("blur", function () {
      const cep = this.value.replace(/\D/g, '');
      if (cep.length !== 8) return;

      fetch(`https://viacep.com.br/ws/${cep}/json/`)
        .then(res => res.json())
        .then(data => {
          if (data.erro) return;

          document.querySelector("[name='rua']").value = data.logradouro || '';
          document.querySelector("[name='bairro']").value = data.bairro || '';
          document.querySelector("[name='cidade']").value = data.localidade || '';
          document.querySelector("[name='estado']").value = data.uf || '';
        })
        .catch(err => console.error("Erro ao buscar CEP:", err));
    });

    // Validação: pelo menos um telefone deve ser preenchido
    const form = document.querySelector("form");
    form.addEventListener("submit", function (e) {
      const tel = document.querySelector("[name='telefone']").value.trim();
      const cel = document.querySelector("[name='celular']").value.trim();

      if (!tel && !cel) {
        e.preventDefault();
        alert("Preencha pelo menos Telefone ou Celular.");
      }
    });
  });
