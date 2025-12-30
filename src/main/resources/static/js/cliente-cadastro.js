document.addEventListener('DOMContentLoaded', () => {
  // Referências dos campos
  const tipoCliente = document.getElementById('tipoCliente');
  const nomeFantasia = document.getElementById('nomeFantasia');
  const inscricaoEstadual = document.getElementById('inscricaoEstadual');
  const inscricaoMunicipal = document.getElementById('inscricaoMunicipal');
  const cep = document.getElementById('cep');
  const logradouro = document.getElementById('logradouro');
  const bairro = document.getElementById('bairro');
  const cidade = document.getElementById('cidade');
  const estado = document.getElementById('estado');
  const telefone = document.getElementById('telefone');
  const celular = document.getElementById('celular');
  const cpfCnpj = document.getElementById('cpfCnpj');
  const dataNascimento = document.getElementById('dataNascimento');
  const rg = document.getElementById('rg');
  const nacionalidade = document.getElementById('nacionalidade');
  const estadoCivil = document.getElementById('estadoCivil');
  const profissao = document.getElementById('profissao');
  const nomeMae = document.getElementById('nomeMae');
  const nomePai = document.getElementById('nomePai');

  // Atualiza campos habilitados conforme tipo de cliente (PF/PJ)
  function atualizarCamposPorTipo() {
    if (!tipoCliente) return;
    const tipo = tipoCliente.value;

    if (tipo === 'PJ') {
      nomeFantasia.disabled = false;
      inscricaoEstadual.disabled = false;
      inscricaoMunicipal.disabled = false;
    } else {
      nomeFantasia.disabled = true;
      nomeFantasia.value = '';
      inscricaoEstadual.disabled = true;
      inscricaoEstadual.value = '';
      inscricaoMunicipal.disabled = true;
      inscricaoMunicipal.value = '';
    }

    const pfEnabled = tipo === 'PF';
    if (dataNascimento) dataNascimento.disabled = !pfEnabled;
    if (rg) rg.disabled = !pfEnabled;
    if (nacionalidade) nacionalidade.disabled = !pfEnabled;
    if (estadoCivil) estadoCivil.disabled = !pfEnabled;
    if (profissao) profissao.disabled = !pfEnabled;
    if (nomeMae) nomeMae.disabled = !pfEnabled;
    if (nomePai) nomePai.disabled = !pfEnabled;

    if (!pfEnabled) {
      if (dataNascimento) dataNascimento.value = '';
      if (rg) rg.value = '';
      if (nacionalidade) nacionalidade.value = '';
      if (estadoCivil) estadoCivil.value = '';
      if (profissao) profissao.value = '';
      if (nomeMae) nomeMae.value = '';
      if (nomePai) nomePai.value = '';
    }
  }

  // Máscara CPF/CNPJ dinâmica (formato visual)
  function mascaraCpfCnpj() {
    let valor = cpfCnpj.value.replace(/\D/g, '');

    if (valor.length <= 11) {
      // CPF: 000.000.000-00
      valor = valor.substring(0, 11);
      if (valor.length > 9) {
        cpfCnpj.value = valor.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
      } else if (valor.length > 6) {
        cpfCnpj.value = valor.replace(/(\d{3})(\d{3})(\d{1,3})/, "$1.$2.$3");
      } else if (valor.length > 3) {
        cpfCnpj.value = valor.replace(/(\d{3})(\d{1,3})/, "$1.$2");
      } else {
        cpfCnpj.value = valor;
      }
    } else {
      // CNPJ: 00.000.000/0000-00
      valor = valor.substring(0, 14);
      if (valor.length > 12) {
        cpfCnpj.value = valor.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
      } else if (valor.length > 8) {
        cpfCnpj.value = valor.replace(/(\d{2})(\d{3})(\d{3})(\d{1,4})/, "$1.$2.$3/$4");
      } else if (valor.length > 5) {
        cpfCnpj.value = valor.replace(/(\d{2})(\d{3})(\d{1,3})/, "$1.$2.$3");
      } else if (valor.length > 2) {
        cpfCnpj.value = valor.replace(/(\d{2})(\d{1,3})/, "$1.$2");
      } else {
        cpfCnpj.value = valor;
      }
    }
  }

  // Máscara para telefone e celular
  function mascaraTelefone(e) {
    let v = e.target.value.replace(/\D/g, '');

    if (v.length > 10) {
      // celular (99) 99999-9999
      v = v.replace(/^(\d{2})(\d{5})(\d{4}).*/, '($1) $2-$3');
    } else if (v.length > 5) {
      // telefone fixo (99) 9999-9999
      v = v.replace(/^(\d{2})(\d{4})(\d{0,4}).*/, '($1) $2-$3');
    } else if (v.length > 2) {
      v = v.replace(/^(\d{2})(\d*)/, '($1) $2');
    } else {
      v = v.replace(/^(\d*)/, '($1');
    }

    e.target.value = v;
  }

  // Busca endereço via ViaCEP ao perder foco do campo CEP
  function configurarBuscaCep() {
    if (!cep) return;

    cep.addEventListener('blur', () => {
      const cepLimpo = cep.value.replace(/\D/g, '');
      if (cepLimpo.length !== 8) return;

      fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`)
        .then(res => res.json())
        .then(data => {
          if (data.erro) {
            alert('CEP não encontrado.');
            return;
          }
          if (logradouro) logradouro.value = data.logradouro || '';
          if (bairro) bairro.value = data.bairro || '';
          if (cidade) cidade.value = data.localidade || '';
          if (estado) estado.value = data.uf || '';
        })
        .catch(() => alert('Erro ao buscar o endereço pelo CEP.'));
    });
  }

  // Validação visual simples para campos obrigatórios
  function validarCamposObrigatorios() {
    document.querySelectorAll('input[required], select[required]').forEach(campo => {
      campo.addEventListener('invalid', () => {
        campo.classList.add('input-invalido');
      });
      campo.addEventListener('input', () => {
        campo.classList.remove('input-invalido');
      });
    });
  }

  // Inicialização
  function init() {
    atualizarCamposPorTipo();

    if (tipoCliente) {
      tipoCliente.addEventListener('change', atualizarCamposPorTipo);
    }

    if (cpfCnpj) cpfCnpj.addEventListener('input', mascaraCpfCnpj);
    if (telefone) telefone.addEventListener('input', mascaraTelefone);
    if (celular) celular.addEventListener('input', mascaraTelefone);

    configurarBuscaCep();
    validarCamposObrigatorios();
  }

  init();
});
