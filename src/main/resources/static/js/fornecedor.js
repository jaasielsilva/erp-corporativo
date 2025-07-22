        let fornecedorIdParaInativar = null;

function abrirModalInativar(id) {
  fornecedorIdParaInativar = id;
  const modal = document.getElementById('modal-inativar');
  modal.style.display = 'flex'; // mostra o modal

  // limpa campos e mensagens
  document.getElementById('matriculaInput').value = '';
  document.getElementById('msgErroMatricula').textContent = '';
  document.getElementById('msgSucessoInativar').textContent = '';
}

function fecharModalInativar() {
  fornecedorIdParaInativar = null;
  const modal = document.getElementById('modal-inativar');
  modal.style.display = 'none'; // esconde o modal

  // limpa campos e mensagens
  document.getElementById('matriculaInput').value = '';
  document.getElementById('msgErroMatricula').textContent = '';
  document.getElementById('msgSucessoInativar').textContent = '';
}

async function validarEInativar() {
  const matricula = document.getElementById('matriculaInput').value.trim();
  const msgErro = document.getElementById('msgErroMatricula');
  const msgSucesso = document.getElementById('msgSucessoInativar');

  // limpa mensagens anteriores
  msgErro.textContent = '';
  msgSucesso.textContent = '';

  if (!matricula) {
    msgErro.textContent = 'Digite a matrícula do administrador.';
    return;
  }

  try {
    const response = await fetch(`/fornecedores/${fornecedorIdParaInativar}/inativar`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Matricula': matricula
      }
    });

    if (response.ok) {
      msgSucesso.textContent = 'Fornecedor inativado com sucesso!';
      setTimeout(() => {
        fecharModalInativar();
        window.location.reload(); // recarrega a página para atualizar a lista
      }, 1500);
    } else {
      const data = await response.json();
      msgErro.textContent = data.erro || 'Erro ao inativar fornecedor.';
    }
  } catch (error) {
    msgErro.textContent = 'Erro de rede ou servidor.';
  }
}

// Opcional: fechar modal se clicar fora do conteúdo
window.onclick = function(event) {
  const modal = document.getElementById('modal-inativar');
  if (event.target === modal) {
    fecharModalInativar();
  }
};
