document.addEventListener('DOMContentLoaded', () => {
  const btnAdicionarProduto = document.getElementById('btnAdicionarProduto');
  const inputEAN = document.getElementById('ean');
  const produtoEncontrado = document.getElementById('produtoEncontrado');
  const tabelaProdutosBody = document.querySelector('#tabelaProdutos tbody');
  const inputTotal = document.getElementById('total');
  const spanFormatado = document.getElementById('total-formatado');
  const btnSalvar = document.querySelector('button[type="submit"]');
  const selectCliente = document.getElementById('cliente');

  // Formata número em moeda BRL
  function formatarMoeda(valor) {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  // Atualiza o total na tabela e nos campos
  function atualizarResumoTotal() {
    let total = 0;
    tabelaProdutosBody.querySelectorAll('tr').forEach(tr => {
      const subtotalText = tr.querySelector('.subtotal').textContent;
      const subtotalNum = parseFloat(subtotalText.replace('R$', '').replace(/\./g, '').replace(',', '.'));
      if (!isNaN(subtotalNum)) total += subtotalNum;
    });
    inputTotal.value = total.toFixed(2);
    if (spanFormatado) {
      spanFormatado.textContent = formatarMoeda(total);
    }
    atualizarBotaoSalvar();
  }

  // Reindexa inputs para o Spring identificar corretamente os itens na lista
  function reindexarItens() {
    tabelaProdutosBody.querySelectorAll('tr').forEach((tr, idx) => {
      tr.querySelectorAll('input').forEach(input => {
        // Substitui o índice dentro dos colchetes itens[0], itens[1], ...
        input.name = input.name.replace(/itens\[\d+\]/, `itens[${idx}]`);
      });
    });
  }

  // Remove uma linha da tabela e atualiza tudo
  function removerLinha(tr) {
    tr.remove();
    reindexarItens();
    atualizarResumoTotal();
    produtoEncontrado.textContent = '';
  }

  // Função que adiciona o produto na tabela (carrinho)
  function adicionarProdutoNaTabela(produto) {
    // Verifica se produto já está na tabela para não duplicar
    if ([...tabelaProdutosBody.children].some(row => row.dataset.ean === produto.ean)) {
      alert('Produto já adicionado na lista.');
      return;
    }

    const preco = parseFloat(produto.preco || produto.precoVenda || produto.precoUnitario || 0);
    const tr = document.createElement('tr');
    tr.dataset.ean = produto.ean;
    const index = tabelaProdutosBody.children.length;

    tr.innerHTML = `
      <td>
        ${produto.nome}
        <input type="hidden" name="itens[${index}].produto.id" value="${produto.id}" />
      </td>
      <td>${produto.ean}</td>
      <td>
        <input type="number" name="itens[${index}].quantidade" min="1" max="${produto.estoque}" value="1" class="quantidade" />
      </td>
      <td>
        ${formatarMoeda(preco)}
        <input type="hidden" name="itens[${index}].precoUnitario" value="${preco.toFixed(2)}" />
      </td>
      <td class="subtotal">${formatarMoeda(preco)}</td>
      <td><button type="button" class="btnRemover"><i class="fas fa-trash-alt"></i></button></td>
    `;

    tabelaProdutosBody.appendChild(tr);

    const inputQuantidade = tr.querySelector('.quantidade');
    const tdSubtotal = tr.querySelector('.subtotal');

    // Atualiza subtotal e total quando muda a quantidade
    inputQuantidade.addEventListener('input', () => {
      let qty = parseInt(inputQuantidade.value);
      if (isNaN(qty) || qty < 1) qty = 1;
      if (qty > produto.estoque) {
        qty = produto.estoque;
        alert('Quantidade maior que o estoque disponível.');
      }
      inputQuantidade.value = qty;
      tdSubtotal.textContent = formatarMoeda(qty * preco);
      atualizarResumoTotal();
    });

    // Remove o produto da tabela
    tr.querySelector('.btnRemover').addEventListener('click', () => removerLinha(tr));

    atualizarResumoTotal();
  }

  // Busca produto pelo EAN via API REST (backend)
  function buscarProdutoEAN(ean) {
    fetch(`/api/produtos/buscar-por-ean?ean=${encodeURIComponent(ean)}`)
      .then(res => {
        if (!res.ok) return res.text().then(msg => { throw new Error(msg) });
        return res.json();
      })
      .then(produto => {
        adicionarProdutoNaTabela(produto);
        produtoEncontrado.style.color = 'green';
        produtoEncontrado.textContent = `Produto adicionado: ${produto.nome}`;
        inputEAN.value = '';
        inputEAN.focus();
      })
      .catch(err => {
        produtoEncontrado.style.color = 'red';
        produtoEncontrado.textContent = err.message || 'Produto não encontrado.';
      });
  }

  // Valida se o botão salvar pode ser habilitado (cliente selecionado + produtos no carrinho)
  function atualizarBotaoSalvar() {
    const temProdutos = tabelaProdutosBody.children.length > 0;
    const clienteSelecionado = selectCliente.value !== "";
    btnSalvar.disabled = !(temProdutos && clienteSelecionado);
  }

  // Eventos
  btnAdicionarProduto.addEventListener('click', () => {
    const ean = inputEAN.value.trim();
    if (!ean) {
      alert('Digite ou escaneie o código de barras.');
      return;
    }
    buscarProdutoEAN(ean);
  });

  inputEAN.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      btnAdicionarProduto.click();
    }
  });

  inputEAN.addEventListener('focus', () => {
    produtoEncontrado.textContent = '';
  });

  selectCliente.addEventListener('change', atualizarBotaoSalvar);

  // Inicializa botão salvar
  atualizarBotaoSalvar();
  atualizarResumoTotal();
});
