let clienteIdParaExcluir = null;

// Abrir modal de exclusão
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

// Fechar modal
function fecharModal() {
  clienteIdParaExcluir = null;
  const modal = document.getElementById('modal-excluir');
  modal.classList.add('hidden');
  limparMensagens();
}

// Limpar mensagens de erro/sucesso do modal
function limparMensagens() {
  const msgErro = document.getElementById('msgErroMatricula');
  const msgSucesso = document.getElementById('msgSucessoExcluir');

  msgErro.classList.add('hidden');
  msgErro.textContent = '';
  msgErro.setAttribute('aria-live', 'polite');

  msgSucesso.classList.add('hidden');
  msgSucesso.textContent = '';
  msgSucesso.setAttribute('aria-live', 'polite');

  document.getElementById('btnConfirmarExcluir').disabled = false;
}

// Confirma exclusão com verificação de matrícula e permissão
async function validarEExcluir() {
  const matriculaInput = document.getElementById('matriculaInput');
  const matricula = matriculaInput.value.trim();
  const msgErro = document.getElementById('msgErroMatricula');
  const msgSucesso = document.getElementById('msgSucessoExcluir');
  const btnConfirmar = document.getElementById('btnConfirmarExcluir');

  msgErro.classList.add('hidden');
  msgErro.textContent = '';
  msgSucesso.classList.add('hidden');
  msgSucesso.textContent = '';

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
        window.location.href = '/clientes';
      }, 1500);
    } else {
      let errorText = 'Erro ao excluir cliente. Contate o ADMINISTRADOR DO SISTEMA.';
      try {
        const errorData = await response.json();
        if (errorData && errorData.erro) errorText = errorData.erro;
      } catch {}

      msgErro.textContent = errorText;
      msgErro.classList.remove('hidden');
      btnConfirmar.disabled = false;
    }
  } catch (error) {
    msgErro.textContent = 'Erro na comunicação com o servidor, tente novamente.';
    msgErro.classList.remove('hidden');
    btnConfirmar.disabled = false;
  }
}

// Filtra os clientes por status
function filtrarPorStatus(status) {
  const linhas = document.querySelectorAll('.cliente-row');
  linhas.forEach(linha => {
    const statusCliente = linha.dataset.status;
    if (status === 'todos' || statusCliente === status) {
      linha.style.display = '';
    } else {
      linha.style.display = 'none';
    }
  });
}

// Filtra os clientes por nome, email ou telefone
function filtrarPorTexto(termo) {
  const linhas = document.querySelectorAll('.cliente-row');
  linhas.forEach(linha => {
    const nome = linha.querySelector('td:nth-child(1)').innerText.toLowerCase();
    const email = linha.querySelector('td:nth-child(3)').innerText.toLowerCase();
    const telefone = linha.querySelector('td:nth-child(4)').innerText.toLowerCase();

    if (
      nome.includes(termo) ||
      email.includes(termo) ||
      telefone.includes(termo)
    ) {
      linha.style.display = '';
    } else {
      linha.style.display = 'none';
    }
  });
}

// Evento de inicialização
document.addEventListener("DOMContentLoaded", () => {
  // Modal exclusão
  document.getElementById('btnCancelarExcluir')?.addEventListener('click', fecharModal);
  document.getElementById('btnConfirmarExcluir')?.addEventListener('click', validarEExcluir);

  // Filtro por status
  document.querySelectorAll('.btn-filter').forEach(btn => {
    btn.addEventListener('click', () => {
      const status = btn.getAttribute('data-status');
      filtrarPorStatus(status);
    });
  });

  // Busca por nome/email/telefone
  const searchInput = document.getElementById('searchInput');
  searchInput?.addEventListener('input', () => {
    const termo = searchInput.value.toLowerCase();
    filtrarPorTexto(termo);
  });
});