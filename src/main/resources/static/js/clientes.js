// clientes.js

document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('searchInput');
  let clienteIdParaExcluir = null;

  // Busca instantânea - filtra linhas da tabela
  searchInput.addEventListener('input', () => {
    const filter = searchInput.value.toLowerCase();
    const rows = document.querySelectorAll('#clientesTable tbody tr.cliente-row');
    rows.forEach(row => {
      const text = row.textContent.toLowerCase();
      row.style.display = text.includes(filter) ? '' : 'none';
    });
  });

  // Função para abrir modal
  function abrirModalExcluir(id) {
    clienteIdParaExcluir = id;
    const modal = document.getElementById('modal-excluir');
    modal.classList.remove('hidden');

    document.getElementById('matriculaInput').value = '';
    document.getElementById('msgErroMatricula').classList.add('hidden');
    document.getElementById('msgErroMatricula').textContent = '';
    document.getElementById('msgSucessoExcluir').classList.add('hidden');
    document.getElementById('msgSucessoExcluir').textContent = '';
    modal.querySelector('.btn-submit').disabled = false;
    document.getElementById('matriculaInput').focus();
  }

  // Função para fechar modal
  function fecharModal() {
    clienteIdParaExcluir = null;
    const modal = document.getElementById('modal-excluir');
    modal.classList.add('hidden');

    document.getElementById('matriculaInput').value = '';
    document.getElementById('msgErroMatricula').classList.add('hidden');
    document.getElementById('msgErroMatricula').textContent = '';
    document.getElementById('msgSucessoExcluir').classList.add('hidden');
    document.getElementById('msgSucessoExcluir').textContent = '';
  }

  // Função para validar e excluir cliente
  async function validarEExcluir() {
    const matricula = document.getElementById('matriculaInput').value.trim();
    const msgErro = document.getElementById('msgErroMatricula');
    const msgSucesso = document.getElementById('msgSucessoExcluir');

    msgErro.classList.add('hidden');
    msgErro.textContent = '';
    msgSucesso.classList.add('hidden');
    msgSucesso.textContent = '';

    if (!matricula) {
      msgErro.textContent = 'Digite a matrícula do administrador.';
      msgErro.classList.remove('hidden');
      return;
    }

    try {
      const excluirResponse = await fetch('/clientes/' + clienteIdParaExcluir + '/deletar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Matricula': matricula
        }
      });

      if (excluirResponse.ok) {
        msgSucesso.textContent = 'Cliente excluído com sucesso!';
        msgSucesso.classList.remove('hidden');

        document.querySelector('#modal-excluir .btn-submit').disabled = true;

        setTimeout(() => {
          fecharModal();
          window.location.href = '/clientes';
        }, 1500);

      } else {
        let errorText = 'Erro ao excluir cliente. Contate o ADMINISTRADOR DO SISTEMA.';
        try {
          const errorData = await excluirResponse.json();
          errorText = errorData.erro || errorText;
        } catch {}

        msgErro.textContent = errorText;
        msgErro.classList.remove('hidden');
      }
    } catch (error) {
      msgErro.textContent = 'Erro de rede ou servidor: ' + error.message;
      msgErro.classList.remove('hidden');
    }
  }

  // Exponha as funções no window para acesso global
  window.abrirModalExcluir = abrirModalExcluir;
  window.fecharModal = fecharModal;
  window.validarEExcluir = validarEExcluir;
});
// clientes.js

document.addEventListener('DOMContentLoaded', () => {
  // Busca instantânea
  const searchInput = document.getElementById('searchInput');
  const rows = document.querySelectorAll('#clientesTable tbody tr.cliente-row');
  searchInput.addEventListener('input', () => {
    const filter = searchInput.value.toLowerCase();
    rows.forEach(row => {
      const text = row.textContent.toLowerCase();
      row.style.display = text.includes(filter) ? '' : 'none';
    });
  });

  // Filtro rápido por status
  const filterButtons = document.querySelectorAll('.btn-filter');
  filterButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const status = btn.getAttribute('data-status');
      rows.forEach(row => {
        if(status === 'todos' || row.dataset.status === status){
          row.style.display = '';
        } else {
          row.style.display = 'none';
        }
      });
    });
  });

  // Gráfico de status dos clientes com Chart.js
  const ctx = document.getElementById('clientesStatusChart').getContext('2d');
  const chart = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: ['Ativos', 'Inativos', 'Pendentes'],
      datasets: [{
        label: 'Clientes',
        data: [/*[[${ativos}]], [[${inativos}]], [[${clientesPendentes}]]*/], // passar via Thymeleaf
        backgroundColor: ['#28a745', '#dc3545', '#ffc107'],
        hoverOffset: 20
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {font: {size: 14}}
        }
      }
    }
  });

  // Expor as funções do modal etc, se necessário (reusar código anterior)
  // ...
});
document.addEventListener('DOMContentLoaded', () => {
  const btnToggle = document.getElementById('btnToggleDarkMode');
  const body = document.body;

  // Carregar preferência salva no localStorage
  if (localStorage.getItem('darkMode') === 'enabled') {
    body.classList.add('dark-mode');
  }

  btnToggle.addEventListener('click', () => {
    body.classList.toggle('dark-mode');

    // Salvar ou remover preferência
    if (body.classList.contains('dark-mode')) {
      localStorage.setItem('darkMode', 'enabled');
    } else {
      localStorage.removeItem('darkMode');
    }
  });
});
