package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.ClienteDTO;
import com.jaasielsilva.portalceo.dto.ProdutoDTO;
import com.jaasielsilva.portalceo.dto.VendaDTO;
import com.jaasielsilva.portalceo.dto.VendaFormDTO;
import com.jaasielsilva.portalceo.dto.VendaItemFormDTO;
import com.jaasielsilva.portalceo.mapper.VendaMapper;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.VendaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendas")
public class VendasController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private VendaMapper vendaMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("titulo", "Dashboard de Vendas");
        
        // Dados para o dashboard
        BigDecimal totalVendas = vendaService.getTotalVendas();
        long totalVendasCount = vendaService.contarTotalVendas();
        String crescimentoVendas = vendaService.calcularCrescimentoVendas();
        String crescimentoFaturamento = vendaService.calcularCrescimentoFaturamento();
        
        model.addAttribute("totalVendas", totalVendas);
        model.addAttribute("totalVendasCount", totalVendasCount);
        model.addAttribute("crescimentoVendas", crescimentoVendas);
        model.addAttribute("crescimentoFaturamento", crescimentoFaturamento);
        
        return "vendas/dashboard";
    }

    @GetMapping("")
    public String lista(
            @RequestParam(defaultValue = "") String cliente,
            @RequestParam(defaultValue = "") String dataInicio,
            @RequestParam(defaultValue = "") String dataFim,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        model.addAttribute("titulo", "Lista de Vendas");
        
        Pageable pageable = PageRequest.of(page, 10);
        
        // Converter datas se fornecidas
        LocalDate inicio = null;
        LocalDate fim = null;
        
        if (!dataInicio.isEmpty()) {
            try {
                inicio = LocalDate.parse(dataInicio, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                // Ignorar data inválida
            }
        }
        
        if (!dataFim.isEmpty()) {
            try {
                fim = LocalDate.parse(dataFim, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                // Ignorar data inválida
            }
        }
        
        // Obter vendas com filtros
        List<Venda> vendas;
        if (inicio != null || fim != null || !cliente.isEmpty() || !status.isEmpty()) {
            // TODO: Implementar filtro real no serviço
            vendas = vendaService.listarTodas();
        } else {
            vendas = vendaService.listarTodas();
        }
        
        // Converter para DTOs
        List<VendaDTO> vendaDTOs = vendas.stream()
                .map(vendaMapper::toDTO)
                .collect(Collectors.toList());
        
        model.addAttribute("vendas", vendaDTOs);
        model.addAttribute("cliente", cliente);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("status", status);
        
        return "vendas/lista";
    }

    @GetMapping("/pdv")
    public String pdv(Model model) {
        model.addAttribute("titulo", "PDV");
        
        // Obter produtos ativos
        List<Produto> produtos = produtoService.listarTodosProdutos().stream()
                .filter(Produto::isAtivo)
                .collect(Collectors.toList());
        
        model.addAttribute("produtos", produtos);
        
        return "vendas/pdv";
    }

    @GetMapping("/caixa")
    public String caixa(Model model) {
        model.addAttribute("titulo", "Caixa");
        return "vendas/caixa";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("titulo", "Nova Venda");
        
        // Obter clientes ativos
        List<Cliente> clientes = clienteService.listarClientesAtivos();
        
        // Adicionar cliente "não identificado" no início da lista
        Cliente clienteNaoIdentificado = new Cliente();
        clienteNaoIdentificado.setId(0L);
        clienteNaoIdentificado.setNome("Cliente Não Identificado");
        clientes.add(0, clienteNaoIdentificado);
        
        // Converter para DTOs
        List<ClienteDTO> clienteDTOs = clientes.stream()
                .map(cliente -> {
                    ClienteDTO dto = new ClienteDTO();
                    dto.setId(cliente.getId());
                    dto.setNome(cliente.getNome());
                    dto.setCpfCnpj(cliente.getCpfCnpj());
                    dto.setEmail(cliente.getEmail());
                    dto.setTelefone(cliente.getTelefone());
                    return dto;
                })
                .collect(Collectors.toList());
        
        // Obter produtos ativos
        List<Produto> produtos = produtoService.listarTodosProdutos().stream()
                .filter(Produto::isAtivo)
                .collect(Collectors.toList());
        
        model.addAttribute("clientes", clienteDTOs);
        model.addAttribute("produtos", produtos);
        model.addAttribute("vendaForm", new VendaFormDTO());
        
        return "vendas/novo";
    }

    @GetMapping("/clientes")
    public String clientes(
            @RequestParam(defaultValue = "") String nome,
            @RequestParam(defaultValue = "") String cpfCnpj,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        model.addAttribute("titulo", "Clientes");
        
        // Obter clientes com filtros
        List<Cliente> clientes;
        if (nome.isEmpty() && cpfCnpj.isEmpty() && status.isEmpty()) {
            clientes = clienteService.listarTodos();
        } else {
            // TODO: Implementar filtro real no serviço
            clientes = clienteService.listarTodos();
        }
        
        // Converter para DTOs
        List<ClienteDTO> clienteDTOs = clientes.stream()
                .map(cliente -> {
                    ClienteDTO dto = new ClienteDTO();
                    dto.setId(cliente.getId());
                    dto.setNome(cliente.getNome());
                    dto.setCpfCnpj(cliente.getCpfCnpj());
                    dto.setEmail(cliente.getEmail());
                    dto.setTelefone(cliente.getTelefone());
                    dto.setStatus(cliente.getStatus());
                    dto.setAtivo(cliente.getAtivo());
                    return dto;
                })
                .collect(Collectors.toList());
        
        model.addAttribute("clientes", clienteDTOs);
        model.addAttribute("nome", nome);
        model.addAttribute("cpfCnpj", cpfCnpj);
        model.addAttribute("status", status);
        
        return "vendas/clientes";
    }

    @GetMapping("/produtos")
    public String produtos(Model model) {
        model.addAttribute("titulo", "Produtos");
        return "vendas/produtos";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("titulo", "Relatórios");
        return "vendas/relatorios";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("titulo", "Configurações");
        return "vendas/configuracoes";
    }

    // Endpoints para operações CRUD

    @PostMapping("/salvar")
    public String salvarVenda(@ModelAttribute VendaFormDTO vendaForm) {
        try {
            Venda venda = new Venda();
            
            // Buscar cliente (se não for cliente não identificado)
            if (vendaForm.getClienteId() != null && vendaForm.getClienteId() > 0) {
                Cliente cliente = clienteService.findById(vendaForm.getClienteId());
                venda.setCliente(cliente);
            } else {
                // Verificar se já existe cliente "não identificado"
                Optional<Cliente> clienteNaoIdentificadoOpt = clienteService.buscarPorNomeExato("Cliente Não Identificado");
                Cliente clienteNaoIdentificado;
                
                if (clienteNaoIdentificadoOpt.isPresent()) {
                    clienteNaoIdentificado = clienteNaoIdentificadoOpt.get();
                } else {
                    // Criar cliente "não identificado"
                    clienteNaoIdentificado = new Cliente();
                    clienteNaoIdentificado.setNome("Cliente Não Identificado");
                    clienteNaoIdentificado.setCpfCnpj("000.000.000-00");
                    clienteNaoIdentificado.setEmail("nao.identificado@exemplo.com");
                    clienteNaoIdentificado.setTelefone("(00) 00000-0000");
                    clienteNaoIdentificado.setStatus("Ativo");
                    clienteNaoIdentificado.setAtivo(true);
                    clienteNaoIdentificado = clienteService.salvar(clienteNaoIdentificado);
                }
                
                venda.setCliente(clienteNaoIdentificado);
            }
            
            // Definir data da venda
            if (vendaForm.getDataVenda() != null) {
                venda.setDataVenda(vendaForm.getDataVenda().atStartOfDay());
            } else {
                venda.setDataVenda(LocalDateTime.now());
            }
            
            // Definir forma de pagamento e parcelas
            venda.setFormaPagamento(vendaForm.getFormaPagamento());
            venda.setParcelas(vendaForm.getParcelas());
            venda.setValorPago(vendaForm.getValorPago());
            
            // Criar itens da venda
            for (VendaItemFormDTO itemForm : vendaForm.getItens()) {
                if (itemForm.getProdutoId() != null && itemForm.getQuantidade() > 0) {
                    Produto produto = produtoService.buscarPorId(itemForm.getProdutoId()).orElse(null);
                    if (produto != null) {
                        VendaItem item = new VendaItem();
                        item.setProduto(produto);
                        item.setQuantidade(itemForm.getQuantidade());
                        item.setPrecoUnitario(itemForm.getPrecoUnitario() != null ? itemForm.getPrecoUnitario() : produto.getPreco());
                        item.setVenda(venda);
                        venda.getItens().add(item);
                    }
                }
            }
            
            // Calcular totais
            BigDecimal subtotal = venda.getItens().stream()
                    .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            venda.setSubtotal(subtotal);
            venda.setDesconto(vendaForm.getDesconto() != null ? vendaForm.getDesconto() : BigDecimal.ZERO);
            venda.setTotal(subtotal.subtract(venda.getDesconto()));
            
            // Calcular troco se pagamento em dinheiro
            if ("Dinheiro".equalsIgnoreCase(venda.getFormaPagamento()) && venda.getValorPago() != null) {
                BigDecimal troco = venda.getValorPago().subtract(venda.getTotal());
                venda.setTroco(troco.compareTo(BigDecimal.ZERO) > 0 ? troco : BigDecimal.ZERO);
            }
            
            // Salvar venda
            vendaService.salvar(venda);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Adicionar tratamento de erro adequado
        }
        
        return "redirect:/vendas";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        // TODO: Implementar obtenção de detalhes
        model.addAttribute("titulo", "Detalhes da Venda");
        return "vendas/detalhes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        // TODO: Implementar obtenção de dados para edição
        model.addAttribute("titulo", "Editar Venda");
        return "vendas/editar";
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id, @ModelAttribute VendaDTO vendaDTO) {
        // TODO: Implementar atualização
        return "redirect:/vendas/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        // TODO: Implementar exclusão
        return "redirect:/vendas";
    }
    
    // Endpoint REST para buscar detalhes do cliente
    @GetMapping("/cliente/{id}")
    @ResponseBody
    public ResponseEntity<ClienteDTO> getCliente(@PathVariable Long id) {
        try {
            Cliente cliente = clienteService.findById(id);
            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }
            
            ClienteDTO dto = new ClienteDTO();
            dto.setId(cliente.getId());
            dto.setNome(cliente.getNome());
            dto.setCpfCnpj(cliente.getCpfCnpj());
            dto.setEmail(cliente.getEmail());
            dto.setTelefone(cliente.getTelefone());
            dto.setStatus(cliente.getStatus());
            dto.setAtivo(cliente.getAtivo());
            
            // Adicionar informações de endereço
            dto.setLogradouro(cliente.getLogradouro());
            dto.setNumero(cliente.getNumero());
            dto.setComplemento(cliente.getComplemento());
            dto.setBairro(cliente.getBairro());
            dto.setCidade(cliente.getCidade());
            dto.setEstado(cliente.getEstado());
            dto.setCep(cliente.getCep());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint REST para buscar detalhes do produto
    @GetMapping("/produto/{id}")
    @ResponseBody
    public ResponseEntity<ProdutoDTO> getProduto(@PathVariable Long id) {
        try {
            Produto produto = produtoService.buscarPorId(id).orElse(null);
            if (produto == null) {
                return ResponseEntity.notFound().build();
            }
            
            ProdutoDTO dto = new ProdutoDTO();
            dto.setId(produto.getId());
            dto.setNome(produto.getNome());
            dto.setEan(produto.getEan());
            dto.setPreco(produto.getPreco());
            dto.setEstoque(produto.getEstoque());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint REST para salvar cliente
    @PostMapping("/cliente/salvar")
    @ResponseBody
    public ResponseEntity<ClienteDTO> salvarCliente(@RequestBody ClienteDTO clienteDTO) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNome(clienteDTO.getNome());
            cliente.setCpfCnpj(clienteDTO.getCpfCnpj());
            cliente.setEmail(clienteDTO.getEmail());
            cliente.setTelefone(clienteDTO.getTelefone());
            cliente.setStatus("Ativo");
            cliente.setAtivo(true);
            
            // Endereço
            cliente.setLogradouro(clienteDTO.getLogradouro());
            cliente.setNumero(clienteDTO.getNumero());
            cliente.setComplemento(clienteDTO.getComplemento());
            cliente.setBairro(clienteDTO.getBairro());
            cliente.setCidade(clienteDTO.getCidade());
            cliente.setEstado(clienteDTO.getEstado());
            cliente.setCep(clienteDTO.getCep());
            
            Cliente clienteSalvo = clienteService.salvar(cliente);
            
            ClienteDTO dto = new ClienteDTO();
            dto.setId(clienteSalvo.getId());
            dto.setNome(clienteSalvo.getNome());
            dto.setCpfCnpj(clienteSalvo.getCpfCnpj());
            dto.setEmail(clienteSalvo.getEmail());
            dto.setTelefone(clienteSalvo.getTelefone());
            dto.setStatus(clienteSalvo.getStatus());
            dto.setAtivo(clienteSalvo.getAtivo());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint REST para atualizar cliente
    @PutMapping("/cliente/{id}")
    @ResponseBody
    public ResponseEntity<ClienteDTO> atualizarCliente(@PathVariable Long id, @RequestBody ClienteDTO clienteDTO) {
        try {
            Cliente cliente = clienteService.findById(id);
            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }
            
            cliente.setNome(clienteDTO.getNome());
            cliente.setCpfCnpj(clienteDTO.getCpfCnpj());
            cliente.setEmail(clienteDTO.getEmail());
            cliente.setTelefone(clienteDTO.getTelefone());
            
            // Endereço
            cliente.setLogradouro(clienteDTO.getLogradouro());
            cliente.setNumero(clienteDTO.getNumero());
            cliente.setComplemento(clienteDTO.getComplemento());
            cliente.setBairro(clienteDTO.getBairro());
            cliente.setCidade(clienteDTO.getCidade());
            cliente.setEstado(clienteDTO.getEstado());
            cliente.setCep(clienteDTO.getCep());
            
            Cliente clienteAtualizado = clienteService.salvar(cliente);
            
            ClienteDTO dto = new ClienteDTO();
            dto.setId(clienteAtualizado.getId());
            dto.setNome(clienteAtualizado.getNome());
            dto.setCpfCnpj(clienteAtualizado.getCpfCnpj());
            dto.setEmail(clienteAtualizado.getEmail());
            dto.setTelefone(clienteAtualizado.getTelefone());
            dto.setStatus(clienteAtualizado.getStatus());
            dto.setAtivo(clienteAtualizado.getAtivo());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}