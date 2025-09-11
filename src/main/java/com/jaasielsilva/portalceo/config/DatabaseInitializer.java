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
import com.jaasielsilva.portalceo.repository.BeneficioRepository;
import com.jaasielsilva.portalceo.repository.PlanoSaudeRepository;
import com.jaasielsilva.portalceo.repository.CategoriaRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.repository.VendaRepository;
import com.jaasielsilva.portalceo.service.FormaPagamentoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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
        criarVendaTeste(produto1, 2, new BigDecimal("1200.00"));
        criarVendaTeste(produto2, 1, new BigDecimal("2500.00"));
        criarVendaTeste(produto3, 5, new BigDecimal("50.00"));
        criarVendaTeste(produto4, 3, new BigDecimal("120.00"));
        criarVendaTeste(produto5, 1, new BigDecimal("800.00"));
        criarVendaTeste(produto6, 4, new BigDecimal("80.00"));
        
        // Criar mais algumas vendas para ter dados variados
        criarVendaTeste(produto1, 1, new BigDecimal("1200.00"));
        criarVendaTeste(produto3, 10, new BigDecimal("50.00"));
        criarVendaTeste(produto5, 2, new BigDecimal("800.00"));
    }
    
    private Categoria criarCategoria(String nome) {
        if (categoriaRepository.findByNome(nome).isPresent()) {
            return categoriaRepository.findByNome(nome).get();
        }
        
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
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
    
    private void criarVendaTeste(Produto produto, int quantidade, BigDecimal precoUnitario) {
        Venda venda = new Venda();
        venda.setDataVenda(LocalDateTime.now().minusDays((long)(Math.random() * 30)));
        venda.setStatus("FINALIZADA");
        venda.setFormaPagamento("DINHEIRO");
        
        VendaItem item = new VendaItem();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(precoUnitario);
        item.setVenda(venda);
        
        List<VendaItem> itens = new ArrayList<>();
        itens.add(item);
        venda.setItens(itens);
        
        BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        venda.setSubtotal(subtotal);
        venda.setTotal(subtotal);
        
        vendaRepository.save(venda);
    }
}
