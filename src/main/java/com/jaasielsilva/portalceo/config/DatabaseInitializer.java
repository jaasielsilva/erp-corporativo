package com.jaasielsilva.portalceo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;
import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.repository.BeneficioRepository;
import com.jaasielsilva.portalceo.repository.PlanoSaudeRepository;
import com.jaasielsilva.portalceo.repository.CategoriaRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.repository.VendaRepository;
import com.jaasielsilva.portalceo.repository.ChamadoRepository;
import com.jaasielsilva.portalceo.service.FormaPagamentoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private BeneficioRepository beneficioRepository;
    
    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;
    
    @Autowired
    private FormaPagamentoService formaPagamentoService;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private VendaRepository vendaRepository;
    
    @Autowired
    private ChamadoRepository chamadoRepository;

    // Aqui você já deve ter injeções de UserRepository, RoleRepository etc.
    // @Autowired
    // private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        // DESABILITADO - Planos de Saúde são criados no SecurityConfig.java
        // Manter somente os benefícios genéricos se necessário
        
        // ===== Populando Benefícios =====
        List<String> planos = List.of(
            "Plano de Saúde",
            "Vale Transporte",
            "Vale Refeição"
        );

        for (String nome : planos) {
            if (!beneficioRepository.existsByNome(nome)) {
                Beneficio b = new Beneficio();
                b.setNome(nome);
                beneficioRepository.save(b);
            }
        }
        
        System.out.println("DatabaseInitializer: Benefícios genéricos criados. Planos de Saúde são gerenciados pelo SecurityConfig.");
        
        // ===== Inicializando Formas de Pagamento =====
        formaPagamentoService.inicializarFormasPadrao();
        System.out.println("DatabaseInitializer: Formas de pagamento inicializadas.");
        
        // ===== Criando dados de teste para vendas =====
        criarDadosTesteVendas();
        System.out.println("DatabaseInitializer: Dados de teste para vendas criados.");
        
        // ===== Criando dados de teste para chamados =====
        criarDadosTesteChamados();
        System.out.println("DatabaseInitializer: Dados de teste para chamados criados.");
    }
    
    private void criarDadosTesteVendas() {
    // Verificar se já existem dados
    if (vendaRepository.count() > 0) {
        return;
    }

    // Criar categorias
    Categoria categoria1 = criarCategoria("Eletrônicos");
    Categoria categoria2 = criarCategoria("Roupas");
    Categoria categoria3 = criarCategoria("Casa e Jardim");
    Categoria categoria4 = criarCategoria("Livros");

    // Criar produtos
    Produto produto1 = criarProduto("Smartphone", new BigDecimal("1200.00"), categoria1);
    Produto produto2 = criarProduto("Notebook", new BigDecimal("2500.00"), categoria1);
    Produto produto3 = criarProduto("Camiseta", new BigDecimal("50.00"), categoria2);
    Produto produto4 = criarProduto("Calça Jeans", new BigDecimal("120.00"), categoria2);
    Produto produto5 = criarProduto("Mesa de Jantar", new BigDecimal("800.00"), categoria3);
    Produto produto6 = criarProduto("Livro de Programação", new BigDecimal("80.00"), categoria4);

    // Criar vendas de teste
    criarVendaTeste(produto1, 2);
    criarVendaTeste(produto2, 1);
    criarVendaTeste(produto3, 5);
    criarVendaTeste(produto4, 3);
    criarVendaTeste(produto5, 1);
    criarVendaTeste(produto6, 4);

    // Criar mais algumas vendas para ter dados variados
    criarVendaTeste(produto1, 1);
    criarVendaTeste(produto3, 10);
    criarVendaTeste(produto5, 2);
}

private Categoria criarCategoria(String nome) {
    return categoriaRepository.findByNome(nome).orElseGet(() -> {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    });
}

private Produto criarProduto(String nome, BigDecimal preco, Categoria categoria) {
    Produto produto = new Produto();
    produto.setNome(nome);
    produto.setPreco(preco);
    produto.setCategoria(categoria);
    produto.setEstoque(100);
    produto.setMinimoEstoque(10);
    produto.setUnidadeMedida("UN");
    produto.setAtivo(true);
    return produtoRepository.save(produto);
}

private void criarVendaTeste(Produto produto, int quantidade) {
    // Criar a venda
    Venda venda = new Venda();
    venda.setDataVenda(LocalDateTime.now().minusDays((long)(Math.random() * 30)));
    venda.setStatus("FINALIZADA");
    venda.setFormaPagamento("DINHEIRO");

    // Criar item da venda e associar bidirecionalmente
    VendaItem item = new VendaItem();
    item.setProduto(produto);
    item.setQuantidade(quantidade);
    item.setPrecoUnitario(produto.getPreco());
    item.setVenda(venda); // associa o item à venda

    venda.getItens().add(item); // adiciona o item à lista da venda

    // Calcular subtotal e total
    venda.setSubtotal(item.getSubtotal());
    venda.setTotal(venda.calcularTotal());

    // Salvar a venda (cascade salva o item automaticamente)
    vendaRepository.save(venda);
}

private void criarDadosTesteChamados() {
    // Verificar se já existem dados
    if (chamadoRepository.count() > 0) {
        return;
    }

    // Criar chamados de exemplo com diferentes status e prioridades
    List<Chamado> chamados = Arrays.asList(
        criarChamado("Sistema lento", "O sistema está muito lento para carregar as páginas", 
                    Chamado.Prioridade.ALTA, Chamado.StatusChamado.ABERTO, "TI", "joao.silva@empresa.com"),
        
        criarChamado("Erro no login", "Não consigo fazer login no sistema", 
                    Chamado.Prioridade.URGENTE, Chamado.StatusChamado.EM_ANDAMENTO, "TI", "maria.santos@empresa.com"),
        
        criarChamado("Impressora não funciona", "A impressora do 2º andar não está funcionando", 
                    Chamado.Prioridade.MEDIA, Chamado.StatusChamado.ABERTO, "Infraestrutura", "carlos.oliveira@empresa.com"),
        
        criarChamado("Solicitação de acesso", "Preciso de acesso ao módulo financeiro", 
                    Chamado.Prioridade.MEDIA, Chamado.StatusChamado.RESOLVIDO, "Segurança", "ana.costa@empresa.com"),
        
        criarChamado("Backup falhou", "O backup automático falhou na madrugada", 
                    Chamado.Prioridade.ALTA, Chamado.StatusChamado.EM_ANDAMENTO, "TI", "pedro.alves@empresa.com"),
        
        criarChamado("Email não funciona", "Não estou recebendo emails desde ontem", 
                    Chamado.Prioridade.ALTA, Chamado.StatusChamado.ABERTO, "TI", "lucia.ferreira@empresa.com"),
        
        criarChamado("Instalação de software", "Preciso instalar o Adobe Photoshop", 
                    Chamado.Prioridade.BAIXA, Chamado.StatusChamado.FECHADO, "TI", "rafael.lima@empresa.com"),
        
        criarChamado("Problema na rede", "Internet instável no setor comercial", 
                    Chamado.Prioridade.MEDIA, Chamado.StatusChamado.RESOLVIDO, "Infraestrutura", "fernanda.rocha@empresa.com")
    );

    chamadoRepository.saveAll(chamados);
}

private Chamado criarChamado(String assunto, String descricao, Chamado.Prioridade prioridade, 
                            Chamado.StatusChamado status, String categoria, String solicitanteEmail) {
    Chamado chamado = new Chamado();
    chamado.setAssunto(assunto);
    chamado.setDescricao(descricao);
    chamado.setPrioridade(prioridade);
    chamado.setStatus(status);
    chamado.setCategoria(categoria);
    chamado.setSolicitanteEmail(solicitanteEmail);
    chamado.setSolicitanteNome(extrairNomeDaEmail(solicitanteEmail));
    chamado.setDataAbertura(LocalDateTime.now().minusDays((long)(Math.random() * 7))); // Últimos 7 dias
    
    // Definir técnico responsável e datas para chamados em andamento/resolvidos
    if (status == Chamado.StatusChamado.EM_ANDAMENTO || status == Chamado.StatusChamado.RESOLVIDO || status == Chamado.StatusChamado.FECHADO) {
        chamado.setTecnicoResponsavel("Técnico TI");
        chamado.setDataInicioAtendimento(chamado.getDataAbertura().plusHours(1));
    }
    
    if (status == Chamado.StatusChamado.RESOLVIDO || status == Chamado.StatusChamado.FECHADO) {
        chamado.setDataResolucao(chamado.getDataAbertura().plusHours(2 + (long)(Math.random() * 24)));
        if (Math.random() > 0.5) {
            chamado.setAvaliacao((int)(Math.random() * 5) + 1); // Avaliação de 1 a 5
        }
    }
    
    // Calcular SLA baseado na prioridade
    switch (prioridade) {
        case URGENTE:
            chamado.setSlaVencimento(chamado.getDataAbertura().plusHours(8));
            break;
        case ALTA:
            chamado.setSlaVencimento(chamado.getDataAbertura().plusHours(24));
            break;
        case MEDIA:
            chamado.setSlaVencimento(chamado.getDataAbertura().plusHours(48));
            break;
        case BAIXA:
            chamado.setSlaVencimento(chamado.getDataAbertura().plusHours(72));
            break;
    }
    
    return chamado;
}

private String extrairNomeDaEmail(String email) {
    String nome = email.split("@")[0];
    return nome.replace(".", " ").toUpperCase();
}

}
