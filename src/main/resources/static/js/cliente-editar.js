let clienteIdParaExcluir = null;

function abrirModalExcluir(id) {
  clienteIdParaExcluir = id;
  const modal = document.getElementById('modal-excluir');
  modal.classList.remove('hidden');

  const matriculaInput = document.getElementById('matriculaInput');
  matriculaInput.value = '';
  matriculaInput.focus();

  limparMensagens();
  document.getElementById('btnConfirmarExcluir').disabled = false;
}

function fecharModal() {
  clienteIdParaExcluir = null;
  document.getElementById('modal-excluir').classList.add('hidden');
  limparMensagens();
}

function limparMensagens() {
  const msgErro = document.getElementById('msgErroMatricula');
  const msgSucesso = document.getElementById('msgSucessoExcluir');

  msgErro.classList.add('hidden');
  msgErro.textContent = '';
  msgSucesso.classList.add('hidden');
  msgSucesso.textContent = '';
  document.getElementById('btnConfirmarExcluir').disabled = false;
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('btnCancelarExcluir').addEventListener('click', fecharModal);
  document.getElementById('btnConfirmarExcluir').addEventListener('click', validarEExcluir);
});

async function validarEExcluir() {
  const matriculaInput = document.getElementById('matriculaInput');
  const matricula = matriculaInput.value.trim();
  const msgErro = document.getElementById('msgErroMatricula');
  const msgSucesso = document.getElementById('msgSucessoExcluir');
  const btnConfirmar = document.getElementById('btnConfirmarExcluir');

  limparMensagens();

  if (!matricula) {
    msgErro.textContent = 'Digite a matrícula do administrador.';
    msgErro.classList.remove('hidden');
    matriculaInput.focus();
    return;
  }

  if (!clienteIdParaExcluir) {
    msgErro.textContent = 'Cliente não selecionado para exclusão.';
    msgErro.classList.remove('hidden');
    return;
  }

  btnConfirmar.disabled = true;

  try {
    const data = new URLSearchParams();
    data.append('matriculaAdmin', matricula);

    const response = await fetch(`/clientes/${clienteIdParaExcluir}/excluir`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: data.toString()
    });

    if (response.ok) {
      msgSucesso.textContent = 'Cliente excluído com sucesso!';
      msgSucesso.classList.remove('hidden');

      setTimeout(() => {
        fecharModal();
        window.location.reload(); // Atualiza a página
      }, 1500);
    } else {
      const contentType = response.headers.get("content-type");
      let errorText = 'Erro ao excluir cliente.';

      if (contentType && contentType.includes("application/json")) {
        const errorData = await response.json();
        if (errorData.erro) errorText = errorData.erro;
      }

      msgErro.textContent = errorText;
      msgErro.classList.remove('hidden');
      btnConfirmar.disabled = false;
    }
  } catch (err) {
    msgErro.textContent = 'Erro na comunicação com o servidor.';
    msgErro.classList.remove('hidden');
    btnConfirmar.disabled = false;
  }
}
