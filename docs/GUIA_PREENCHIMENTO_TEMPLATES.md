# 📋 Guia de Preenchimento de Templates

Este guia fornece instruções práticas para preencher os templates de cada módulo com dados que façam sentido para o sistema, tornando-o funcional rapidamente.

## 🏠 Dashboard

### Dados Necessários:
- **Indicadores Financeiros**: Adicione pelo menos 5 transações financeiras recentes
- **Gráficos de Vendas**: Cadastre no mínimo 10 vendas com valores variados
- **Alertas**: Configure 3 tipos de alertas (estoque baixo, contas a vencer, tarefas pendentes)

### Como Implementar:
```java
// No DashboardController.java
@GetMapping
public String dashboard(Model model) {
    // Dados financeiros
    List<IndicadorFinanceiro> indicadores = new ArrayList<>();
    indicadores.add(new IndicadorFinanceiro("Receita Mensal", "R$ 45.780,00", "up", "15%"));
    indicadores.add(new IndicadorFinanceiro("Despesas", "R$ 23.450,00", "down", "8%"));
    indicadores.add(new IndicadorFinanceiro("Lucro", "R$ 22.330,00", "up", "22%"));
    model.addAttribute("indicadores", indicadores);
    
    // Vendas recentes para gráfico
    model.addAttribute("vendasMensais", vendaService.obterVendasUltimosMeses(6));
    
    // Alertas
    model.addAttribute("alertas", alertaService.obterAlertasAtivos());
    
    return "dashboard/index";
}
```

## 👥 Clientes

### Dados Necessários:
- **Clientes**: Cadastre 20 clientes com dados completos (nome, email, telefone, endereço)
- **Categorias**: Crie 5 categorias de clientes (VIP, Regular, Novo, Inativo, Corporativo)
- **Histórico**: Adicione histórico de interações para pelo menos 10 clientes

### Como Implementar:
```sql
-- Inserir dados de exemplo no banco
INSERT INTO clientes (nome, email, telefone, endereco, categoria, data_cadastro) VALUES
('Empresa ABC Ltda', 'contato@empresaabc.com.br', '(11) 3456-7890', 'Av. Paulista, 1000, São Paulo, SP', 'Corporativo', '2023-01-15'),
('João Silva', 'joao.silva@email.com', '(11) 98765-4321', 'Rua das Flores, 123, São Paulo, SP', 'VIP', '2023-02-20'),
('Maria Oliveira', 'maria.oliveira@email.com', '(21) 98765-1234', 'Av. Atlântica, 500, Rio de Janeiro, RJ', 'Regular', '2023-03-10');
```

## 📦 Produtos

### Dados Necessários:
- **Produtos**: Cadastre 30 produtos com descrições, preços e imagens
- **Categorias**: Crie 8 categorias de produtos
- **Estoque**: Configure níveis de estoque para todos os produtos

### Como Implementar:
```html
<!-- No template produtos/form.html -->
<form th:action="@{/produtos/salvar}" method="post" th:object="${produto}" enctype="multipart/form-data">
    <div class="form-group">
        <label for="nome">Nome do Produto</label>
        <input type="text" class="form-control" id="nome" th:field="*{nome}" required>
    </div>
    
    <div class="form-group">
        <label for="descricao">Descrição</label>
        <textarea class="form-control" id="descricao" th:field="*{descricao}" rows="3"></textarea>
    </div>
    
    <div class="form-group">
        <label for="preco">Preço (R$)</label>
        <input type="number" step="0.01" class="form-control" id="preco" th:field="*{preco}" required>
    </div>
    
    <div class="form-group">
        <label for="categoria">Categoria</label>
        <select class="form-control" id="categoria" th:field="*{categoria.id}" required>
            <option value="">Selecione uma categoria</option>
            <option th:each="cat : ${categorias}" th:value="${cat.id}" th:text="${cat.nome}"></option>
        </select>
    </div>
    
    <div class="form-group">
        <label for="estoque">Quantidade em Estoque</label>
        <input type="number" class="form-control" id="estoque" th:field="*{quantidadeEstoque}" required>
    </div>
    
    <div class="form-group">
        <label for="imagem">Imagem do Produto</label>
        <input type="file" class="form-control-file" id="imagem" name="imagemFile">
    </div>
    
    <button type="submit" class="btn btn-primary">Salvar</button>
</form>
```

## 👨‍💼 RH

### Dados Necessários:
- **Colaboradores**: Cadastre 15 colaboradores com dados completos
- **Cargos**: Crie 10 cargos com descrições e faixas salariais
- **Departamentos**: Configure 6 departamentos
- **Benefícios**: Adicione 5 tipos de benefícios

### Como Implementar:
```java
// No CommandLineRunner para dados iniciais
@Bean
CommandLineRunner carregarDadosIniciais(
        DepartamentoRepository departamentoRepository,
        CargoRepository cargoRepository,
        ColaboradorRepository colaboradorRepository,
        BeneficioRepository beneficioRepository) {
    
    return args -> {
        // Departamentos
        Departamento rh = new Departamento("Recursos Humanos", "Gestão de pessoas");
        Departamento ti = new Departamento("Tecnologia da Informação", "Suporte e desenvolvimento");
        departamentoRepository.saveAll(Arrays.asList(rh, ti));
        
        // Cargos
        Cargo analista = new Cargo("Analista de RH", "Analista de recursos humanos", new BigDecimal("4500.00"));
        Cargo desenvolvedor = new Cargo("Desenvolvedor Java", "Desenvolvedor backend", new BigDecimal("8500.00"));
        cargoRepository.saveAll(Arrays.asList(analista, desenvolvedor));
        
        // Benefícios
        Beneficio vr = new Beneficio("Vale Refeição", "VR", new BigDecimal("500.00"));
        Beneficio vt = new Beneficio("Vale Transporte", "VT", new BigDecimal("220.00"));
        Beneficio planoSaude = new Beneficio("Plano de Saúde", "Amil", new BigDecimal("350.00"));
        beneficioRepository.saveAll(Arrays.asList(vr, vt, planoSaude));
        
        // Colaboradores
        Colaborador joao = new Colaborador();
        joao.setNome("João Silva");
        joao.setCpf("123.456.789-00");
        joao.setEmail("joao.silva@empresa.com");
        joao.setTelefone("(11) 98765-4321");
        joao.setDataNascimento(LocalDate.of(1985, 5, 15));
        joao.setDataAdmissao(LocalDate.of(2022, 3, 10));
        joao.setCargo(desenvolvedor);
        joao.setDepartamento(ti);
        joao.setSalario(new BigDecimal("8500.00"));
        
        Colaborador maria = new Colaborador();
        maria.setNome("Maria Oliveira");
        maria.setCpf("987.654.321-00");
        maria.setEmail("maria.oliveira@empresa.com");
        maria.setTelefone("(11) 91234-5678");
        maria.setDataNascimento(LocalDate.of(1990, 8, 22));
        maria.setDataAdmissao(LocalDate.of(2021, 11, 5));
        maria.setCargo(analista);
        maria.setDepartamento(rh);
        maria.setSalario(new BigDecimal("4500.00"));
        
        colaboradorRepository.saveAll(Arrays.asList(joao, maria));
    };
}
```

## 💰 Financeiro

### Dados Necessários:
- **Contas a Pagar**: Cadastre 15 contas com vencimentos variados
- **Contas a Receber**: Adicione 20 contas a receber
- **Categorias**: Configure 10 categorias financeiras
- **Formas de Pagamento**: Adicione 5 formas de pagamento

### Como Implementar:
```java
// No FinanceiroController.java
@GetMapping("/contas-pagar")
public String listarContasPagar(Model model) {
    // Se não houver dados, criar alguns exemplos
    if (contaPagarRepository.count() == 0) {
        List<ContaPagar> exemplos = new ArrayList<>();
        
        exemplos.add(new ContaPagar("Aluguel", new BigDecimal("3500.00"), 
            LocalDate.now().plusDays(5), "Mensal", "Imobiliária XYZ"));
            
        exemplos.add(new ContaPagar("Energia Elétrica", new BigDecimal("850.00"), 
            LocalDate.now().plusDays(10), "Mensal", "Companhia Elétrica"));
            
        exemplos.add(new ContaPagar("Internet", new BigDecimal("250.00"), 
            LocalDate.now().plusDays(15), "Mensal", "Provedor Net"));
            
        contaPagarRepository.saveAll(exemplos);
    }
    
    model.addAttribute("contas", contaPagarRepository.findAll());
    return "financeiro/contas-pagar/listar";
}
```

## 🛒 Vendas

### Dados Necessários:
- **Vendas**: Registre 30 vendas com itens variados
- **Clientes**: Associe vendas a clientes existentes
- **Formas de Pagamento**: Use diferentes formas de pagamento
- **Status**: Configure diferentes status (Concluída, Pendente, Cancelada)

### Como Implementar:
```html
<!-- No template vendas/pdv.html -->
<div class="row">
    <div class="col-md-8">
        <div class="card">
            <div class="card-header">
                <h5>Produtos</h5>
            </div>
            <div class="card-body">
                <div class="form-group">
                    <input type="text" id="busca-produto" class="form-control" placeholder="Buscar produto...">
                </div>
                
                <div class="row" id="lista-produtos">
                    <!-- Produtos serão carregados via AJAX -->
                </div>
            </div>
        </div>
    </div>
    
    <div class="col-md-4">
        <div class="card">
            <div class="card-header">
                <h5>Carrinho</h5>
            </div>
            <div class="card-body">
                <div id="itens-carrinho">
                    <!-- Itens do carrinho -->
                </div>
                
                <hr>
                
                <div class="form-group">
                    <label for="cliente">Cliente</label>
                    <select id="cliente" class="form-control">
                        <option value="">Selecione um cliente</option>
                        <!-- Clientes serão carregados via AJAX -->
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="forma-pagamento">Forma de Pagamento</label>
                    <select id="forma-pagamento" class="form-control">
                        <option value="dinheiro">Dinheiro</option>
                        <option value="cartao_credito">Cartão de Crédito</option>
                        <option value="cartao_debito">Cartão de Débito</option>
                        <option value="pix">PIX</option>
                        <option value="boleto">Boleto</option>
                    </select>
                </div>
                
                <div class="total">
                    <h4>Total: R$ <span id="valor-total">0,00</span></h4>
                </div>
                
                <button id="finalizar-venda" class="btn btn-success btn-block">Finalizar Venda</button>
            </div>
        </div>
    </div>
</div>

<script>
    // Código JavaScript para carregar produtos, adicionar ao carrinho e finalizar venda
    $(document).ready(function() {
        // Carregar produtos
        $.ajax({
            url: '/api/produtos',
            method: 'GET',
            success: function(data) {
                let html = '';
                data.forEach(function(produto) {
                    html += `
                        <div class="col-md-4 mb-3">
                            <div class="card produto-item" data-id="${produto.id}" data-nome="${produto.nome}" data-preco="${produto.preco}">
                                <img src="${produto.imagemUrl || '/img/produto-sem-imagem.jpg'}" class="card-img-top" alt="${produto.nome}">
                                <div class="card-body">
                                    <h5 class="card-title">${produto.nome}</h5>
                                    <p class="card-text">R$ ${produto.preco.toFixed(2)}</p>
                                    <button class="btn btn-primary btn-sm adicionar-produto">Adicionar</button>
                                </div>
                            </div>
                        </div>
                    `;
                });
                $('#lista-produtos').html(html);
            }
        });
        
        // Carregar clientes
        $.ajax({
            url: '/api/clientes',
            method: 'GET',
            success: function(data) {
                let html = '<option value="">Selecione um cliente</option>';
                data.forEach(function(cliente) {
                    html += `<option value="${cliente.id}">${cliente.nome}</option>`;
                });
                $('#cliente').html(html);
            }
        });
        
        // Lógica para adicionar produtos ao carrinho
        $(document).on('click', '.adicionar-produto', function() {
            const produto = $(this).closest('.produto-item');
            const id = produto.data('id');
            const nome = produto.data('nome');
            const preco = produto.data('preco');
            
            // Verificar se o produto já está no carrinho
            const itemExistente = $(`#item-carrinho-${id}`);
            if (itemExistente.length > 0) {
                // Incrementar quantidade
                const qtdElement = itemExistente.find('.quantidade');
                let qtd = parseInt(qtdElement.text()) + 1;
                qtdElement.text(qtd);
                
                // Atualizar subtotal
                const subtotalElement = itemExistente.find('.subtotal');
                subtotalElement.text((qtd * preco).toFixed(2));
            } else {
                // Adicionar novo item
                const html = `
                    <div class="item-carrinho" id="item-carrinho-${id}" data-id="${id}" data-preco="${preco}">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <div>
                                <span class="nome-produto">${nome}</span>
                                <br>
                                <small>R$ ${preco.toFixed(2)} x <span class="quantidade">1</span></small>
                            </div>
                            <div>
                                <span class="subtotal">${preco.toFixed(2)}</span>
                                <button class="btn btn-sm btn-danger remover-item ml-2">X</button>
                            </div>
                        </div>
                    </div>
                `;
                $('#itens-carrinho').append(html);
            }
            
            atualizarTotal();
        });
        
        // Remover item do carrinho
        $(document).on('click', '.remover-item', function() {
            $(this).closest('.item-carrinho').remove();
            atualizarTotal();
        });
        
        // Atualizar total
        function atualizarTotal() {
            let total = 0;
            $('.item-carrinho').each(function() {
                const preco = $(this).data('preco');
                const quantidade = parseInt($(this).find('.quantidade').text());
                total += preco * quantidade;
            });
            $('#valor-total').text(total.toFixed(2));
        }
        
        // Finalizar venda
        $('#finalizar-venda').click(function() {
            const clienteId = $('#cliente').val();
            const formaPagamento = $('#forma-pagamento').val();
            
            if (!clienteId) {
                alert('Selecione um cliente!');
                return;
            }
            
            const itens = [];
            $('.item-carrinho').each(function() {
                itens.push({
                    produtoId: $(this).data('id'),
                    quantidade: parseInt($(this).find('.quantidade').text()),
                    valorUnitario: $(this).data('preco')
                });
            });
            
            if (itens.length === 0) {
                alert('Adicione pelo menos um produto!');
                return;
            }
            
            // Enviar dados da venda
            $.ajax({
                url: '/api/vendas',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    clienteId: clienteId,
                    formaPagamento: formaPagamento,
                    itens: itens
                }),
                success: function(response) {
                    alert('Venda finalizada com sucesso!');
                    // Limpar carrinho
                    $('#itens-carrinho').empty();
                    $('#cliente').val('');
                    $('#forma-pagamento').val('dinheiro');
                    atualizarTotal();
                    
                    // Redirecionar para comprovante
                    window.location.href = `/vendas/comprovante/${response.id}`;
                },
                error: function() {
                    alert('Erro ao finalizar venda. Tente novamente.');
                }
            });
        });
    });
</script>
```

## 📊 Relatórios

### Dados Necessários:
- **Filtros**: Configure filtros por período, categoria, departamento
- **Gráficos**: Adicione gráficos de barras, linhas e pizza
- **Exportação**: Implemente exportação para PDF e Excel

### Como Implementar:
```java
// No RelatorioController.java
@GetMapping("/vendas")
public String relatorioVendas(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
        @RequestParam(required = false) Long vendedorId,
        Model model) {
    
    if (dataInicio == null) {
        dataInicio = LocalDate.now().withDayOfMonth(1); // Primeiro dia do mês atual
    }
    
    if (dataFim == null) {
        dataFim = LocalDate.now(); // Dia atual
    }
    
    List<Venda> vendas = vendaService.buscarPorPeriodo(dataInicio, dataFim, vendedorId);
    
    // Dados para gráfico de vendas por dia
    Map<LocalDate, BigDecimal> vendasPorDia = new LinkedHashMap<>();
    vendas.forEach(venda -> {
        LocalDate data = venda.getDataVenda().toLocalDate();
        BigDecimal valorAtual = vendasPorDia.getOrDefault(data, BigDecimal.ZERO);
        vendasPorDia.put(data, valorAtual.add(venda.getValorTotal()));
    });
    
    // Dados para gráfico de vendas por forma de pagamento
    Map<String, BigDecimal> vendasPorFormaPagamento = new LinkedHashMap<>();
    vendas.forEach(venda -> {
        String formaPagamento = venda.getFormaPagamento();
        BigDecimal valorAtual = vendasPorFormaPagamento.getOrDefault(formaPagamento, BigDecimal.ZERO);
        vendasPorFormaPagamento.put(formaPagamento, valorAtual.add(venda.getValorTotal()));
    });
    
    model.addAttribute("vendas", vendas);
    model.addAttribute("dataInicio", dataInicio);
    model.addAttribute("dataFim", dataFim);
    model.addAttribute("vendedorId", vendedorId);
    model.addAttribute("vendedores", usuarioService.buscarVendedores());
    model.addAttribute("vendasPorDia", vendasPorDia);
    model.addAttribute("vendasPorFormaPagamento", vendasPorFormaPagamento);
    
    return "relatorios/vendas";
}

@GetMapping("/vendas/pdf")
public void gerarRelatorioPdf(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
        @RequestParam(required = false) Long vendedorId,
        HttpServletResponse response) throws Exception {
    
    List<Venda> vendas = vendaService.buscarPorPeriodo(dataInicio, dataFim, vendedorId);
    
    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=relatorio-vendas.pdf");
    
    // Gerar PDF com JasperReports ou outra biblioteca
    relatorioService.gerarRelatorioPdfVendas(vendas, response.getOutputStream());
}
```

## 📱 Chat

### Dados Necessários:
- **Usuários**: Utilize usuários existentes
- **Mensagens**: Adicione algumas mensagens iniciais
- **Grupos**: Crie 3 grupos de chat (Geral, Vendas, Suporte)

### Como Implementar:
```java
// No ChatController.java
@GetMapping
public String chat(Model model, Principal principal) {
    String username = principal.getName();
    Usuario usuarioAtual = usuarioService.buscarPorUsername(username);
    
    // Buscar conversas do usuário
    List<Conversa> conversas = conversaService.buscarConversasDoUsuario(usuarioAtual.getId());
    
    // Se não houver conversas, criar algumas de exemplo
    if (conversas.isEmpty()) {
        // Buscar alguns usuários para criar conversas
        List<Usuario> usuarios = usuarioService.buscarTodos();
        usuarios.remove(usuarioAtual);
        
        // Criar conversa com o primeiro usuário
        if (!usuarios.isEmpty()) {
            Usuario outroUsuario = usuarios.get(0);
            Conversa novaConversa = new Conversa();
            novaConversa.setTipo(TipoConversa.INDIVIDUAL);
            novaConversa.setParticipantes(new HashSet<>(Arrays.asList(usuarioAtual, outroUsuario)));
            
            Conversa conversaSalva = conversaService.salvar(novaConversa);
            
            // Adicionar algumas mensagens
            Mensagem msg1 = new Mensagem();
            msg1.setConversa(conversaSalva);
            msg1.setRemetente(usuarioAtual);
            msg1.setConteudo("Olá! Como posso ajudar?");
            msg1.setDataEnvio(LocalDateTime.now().minusMinutes(30));
            mensagemService.salvar(msg1);
            
            Mensagem msg2 = new Mensagem();
            msg2.setConversa(conversaSalva);
            msg2.setRemetente(outroUsuario);
            msg2.setConteudo("Preciso de informações sobre o último pedido.");
            msg2.setDataEnvio(LocalDateTime.now().minusMinutes(25));
            mensagemService.salvar(msg2);
            
            conversas = conversaService.buscarConversasDoUsuario(usuarioAtual.getId());
        }
    }
    
    model.addAttribute("conversas", conversas);
    model.addAttribute("usuarioAtual", usuarioAtual);
    
    return "chat/index";
}
```

## 📝 Contratos

### Dados Necessários:
- **Modelos**: Crie 5 modelos de contratos
- **Clientes**: Associe contratos a clientes existentes
- **Status**: Configure diferentes status (Ativo, Pendente, Encerrado)

### Como Implementar:
```java
// No ContratosController.java
@GetMapping
public String listarContratos(Model model) {
    // Se não houver contratos, criar alguns exemplos
    if (contratoRepository.count() == 0) {
        List<Cliente> clientes = clienteRepository.findAll();
        if (!clientes.isEmpty()) {
            List<Contrato> exemplos = new ArrayList<>();
            
            // Contrato 1
            Contrato contrato1 = new Contrato();
            contrato1.setNumero("CONT-2023-001");
            contrato1.setCliente(clientes.get(0));
            contrato1.setTipo("Prestação de Serviços");
            contrato1.setDataInicio(LocalDate.now().minusMonths(2));
            contrato1.setDataFim(LocalDate.now().plusMonths(10));
            contrato1.setValor(new BigDecimal("12000.00"));
            contrato1.setStatus("Ativo");
            
            // Contrato 2
            Contrato contrato2 = new Contrato();
            contrato2.setNumero("CONT-2023-002");
            contrato2.setCliente(clientes.size() > 1 ? clientes.get(1) : clientes.get(0));
            contrato2.setTipo("Fornecimento de Produtos");
            contrato2.setDataInicio(LocalDate.now().minusMonths(1));
            contrato2.setDataFim(LocalDate.now().plusMonths(5));
            contrato2.setValor(new BigDecimal("8500.00"));
            contrato2.setStatus("Ativo");
            
            // Contrato 3
            Contrato contrato3 = new Contrato();
            contrato3.setNumero("CONT-2023-003");
            contrato3.setCliente(clientes.size() > 2 ? clientes.get(2) : clientes.get(0));
            contrato3.setTipo("Consultoria");
            contrato3.setDataInicio(LocalDate.now().minusMonths(3));
            contrato3.setDataFim(LocalDate.now().minusDays(15));
            contrato3.setValor(new BigDecimal("5000.00"));
            contrato3.setStatus("Encerrado");
            
            exemplos.add(contrato1);
            exemplos.add(contrato2);
            exemplos.add(contrato3);
            
            contratoRepository.saveAll(exemplos);
        }
    }
    
    model.addAttribute("contratos", contratoRepository.findAll());
    return "contratos/listar";
}
```

## 🔑 Dicas Gerais para Templates

### 1. Utilize o Layout Base
```html
<!-- Em qualquer template -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{base/layout}">
<head>
    <title>Título da Página</title>
</head>
<body>
    <div layout:fragment="content">
        <!-- Conteúdo específico da página -->
        <h1>Título da Seção</h1>
        
        <!-- Seu conteúdo aqui -->
    </div>
</body>
</html>
```

### 2. Adicione Dados de Exemplo via JavaScript
```javascript
// Para páginas que precisam de dados dinâmicos
$(document).ready(function() {
    // Verificar se há dados na tabela
    if ($('#tabela-dados tbody tr').length <= 1) {
        // Adicionar dados de exemplo
        const dadosExemplo = [
            { id: 1, nome: 'Exemplo 1', valor: 'R$ 100,00', status: 'Ativo' },
            { id: 2, nome: 'Exemplo 2', valor: 'R$ 200,00', status: 'Pendente' },
            { id: 3, nome: 'Exemplo 3', valor: 'R$ 300,00', status: 'Concluído' }
        ];
        
        let html = '';
        dadosExemplo.forEach(item => {
            html += `
                <tr>
                    <td>${item.id}</td>
                    <td>${item.nome}</td>
                    <td>${item.valor}</td>
                    <td><span class="badge ${getBadgeClass(item.status)}">${item.status}</span></td>
                    <td>
                        <a href="#" class="btn btn-sm btn-info">Detalhes</a>
                        <a href="#" class="btn btn-sm btn-warning">Editar</a>
                        <a href="#" class="btn btn-sm btn-danger">Excluir</a>
                    </td>
                </tr>
            `;
        });
        
        $('#tabela-dados tbody').html(html);
    }
    
    function getBadgeClass(status) {
        switch(status) {
            case 'Ativo': return 'badge-success';
            case 'Pendente': return 'badge-warning';
            case 'Concluído': return 'badge-info';
            default: return 'badge-secondary';
        }
    }
});
```

### 3. Formulários Padrão
```html
<!-- Template padrão para formulários -->
<form th:action="@{/modulo/salvar}" method="post" th:object="${objeto}">
    <input type="hidden" th:field="*{id}">
    
    <div class="form-group">
        <label for="nome">Nome</label>
        <input type="text" class="form-control" id="nome" th:field="*{nome}" required>
        <div class="text-danger" th:if="${#fields.hasErrors('nome')}" th:errors="*{nome}"></div>
    </div>
    
    <div class="form-group">
        <label for="descricao">Descrição</label>
        <textarea class="form-control" id="descricao" th:field="*{descricao}" rows="3"></textarea>
        <div class="text-danger" th:if="${#fields.hasErrors('descricao')}" th:errors="*{descricao}"></div>
    </div>
    
    <div class="form-group">
        <label for="categoria">Categoria</label>
        <select class="form-control" id="categoria" th:field="*{categoria.id}">
            <option value="">Selecione uma categoria</option>
            <option th:each="cat : ${categorias}" th:value="${cat.id}" th:text="${cat.nome}"></option>
        </select>
    </div>
    
    <div class="form-group">
        <label for="status">Status</label>
        <select class="form-control" id="status" th:field="*{status}">
            <option value="Ativo">Ativo</option>
            <option value="Inativo">Inativo</option>
            <option value="Pendente">Pendente</option>
        </select>
    </div>
    
    <button type="submit" class="btn btn-primary">Salvar</button>
    <a th:href="@{/modulo}" class="btn btn-secondary">Cancelar</a>
</form>
```

### 4. Tabelas de Listagem
```html
<!-- Template padrão para tabelas de listagem -->
<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5>Lista de Itens</h5>
        <a th:href="@{/modulo/novo}" class="btn btn-primary">Novo Item</a>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-striped table-hover" id="tabela-dados">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nome</th>
                        <th>Descrição</th>
                        <th>Status</th>
                        <th>Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:if="${#lists.isEmpty(itens)}">
                        <td colspan="5" class="text-center">Nenhum item encontrado</td>
                    </tr>
                    <tr th:each="item : ${itens}">
                        <td th:text="${item.id}"></td>
                        <td th:text="${item.nome}"></td>
                        <td th:text="${#strings.abbreviate(item.descricao, 50)}"></td>
                        <td>
                            <span th:class="${'badge ' + (item.status == 'Ativo' ? 'badge-success' : (item.status == 'Pendente' ? 'badge-warning' : 'badge-secondary'))}" 
                                  th:text="${item.status}"></span>
                        </td>
                        <td>
                            <a th:href="@{/modulo/{id}(id=${item.id})}" class="btn btn-sm btn-info">Detalhes</a>
                            <a th:href="@{/modulo/editar/{id}(id=${item.id})}" class="btn btn-sm btn-warning">Editar</a>
                            <button type="button" class="btn btn-sm btn-danger" 
                                    th:onclick="'confirmarExclusao(' + ${item.id} + ')'">Excluir</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal de confirmação de exclusão -->
<div class="modal fade" id="modalConfirmacao" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Confirmar Exclusão</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Fechar">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <p>Tem certeza que deseja excluir este item?</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <a href="#" id="btn-confirmar-exclusao" class="btn btn-danger">Excluir</a>
            </div>
        </div>
    </div>
</div>

<script>
    function confirmarExclusao(id) {
        $('#btn-confirmar-exclusao').attr('href', '/modulo/excluir/' + id);
        $('#modalConfirmacao').modal('show');
    }
    
    $(document).ready(function() {
        // Inicializar DataTables se existir
        if ($.fn.DataTable) {
            $('#tabela-dados').DataTable({
                "language": {
                    "url": "/js/dataTables.portuguese-brasil.json"
                }
            });
        }
    });
</script>
```

### 5. Detalhes de Item
```html
<!-- Template padrão para visualização de detalhes -->
<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5>Detalhes do Item</h5>
        <div>
            <a th:href="@{/modulo/editar/{id}(id=${item.id})}" class="btn btn-warning">Editar</a>
            <a th:href="@{/modulo}" class="btn btn-secondary">Voltar</a>
        </div>
    </div>
    <div class="card-body">
        <div class="row">
            <div class="col-md-6">
                <p><strong>ID:</strong> <span th:text="${item.id}"></span></p>
                <p><strong>Nome:</strong> <span th:text="${item.nome}"></span></p>
                <p><strong>Descrição:</strong> <span th:text="${item.descricao}"></span></p>
            </div>
            <div class="col-md-6">
                <p><strong>Categoria:</strong> <span th:text="${item.categoria != null ? item.categoria.nome : '-'}"></span></p>
                <p><strong>Status:</strong> <span th:class="${'badge ' + (item.status == 'Ativo' ? 'badge-success' : (item.status == 'Pendente' ? 'badge-warning' : 'badge-secondary'))}" th:text="${item.status}"></span></p>
                <p><strong>Data de Criação:</strong> <span th:text="${#temporals.format(item.dataCriacao, 'dd/MM/yyyy HH:mm')}"></span></p>
            </div>
        </div>
        
        <!-- Seção para itens relacionados, se houver -->
        <div th:if="${!#lists.isEmpty(item.itensRelacionados)}" class="mt-4">
            <h6>Itens Relacionados</h6>
            <div class="table-responsive">
                <table class="table table-sm">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr th:each="relacionado : ${item.itensRelacionados}">
                            <td th:text="${relacionado.id}"></td>
                            <td th:text="${relacionado.nome}"></td>
                            <td th:text="${relacionado.status}"></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
```

## 🔄 Próximos Passos

1. **Comece pelo Módulo RH**:
   - Implemente a folha de pagamento
   - Complete o cadastro de benefícios
   - Desenvolva o controle de ponto

2. **Avance para o Financeiro**:
   - Crie as entidades de contas a pagar/receber
   - Implemente o fluxo de caixa
   - Desenvolva relatórios financeiros

3. **Complete o Módulo de Vendas**:
   - Finalize o PDV
   - Integre com estoque
   - Implemente relatórios de vendas

4. **Siga o Plano de Implementação Detalhado** para os demais módulos.