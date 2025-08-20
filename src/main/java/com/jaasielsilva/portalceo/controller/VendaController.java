package com.jaasielsilva.portalceo.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.jaasielsilva.portalceo.dto.VendaPdvRequest;
import com.jaasielsilva.portalceo.dto.VendaPdvResponse;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.VendaService;
import com.jaasielsilva.portalceo.service.FormaPagamentoService;
import com.jaasielsilva.portalceo.service.CaixaService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.model.FormaPagamento;
import com.jaasielsilva.portalceo.model.Caixa;
import com.jaasielsilva.portalceo.model.Usuario;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;
    
    @Autowired
    private FormaPagamentoService formaPagamentoService;
    
    @Autowired
    private CaixaService caixaService;
    
    @Autowired
    private UsuarioService usuarioService;

    // 🧾 LISTAR VENDAS
    @GetMapping
    public String listar(@RequestParam(name = "cpfCnpj", required = false) String cpfCnpj, Model model) {
        var vendasFiltradas = vendaService.buscarPorCpfCnpj(cpfCnpj);
        model.addAttribute("vendas", vendasFiltradas);
        model.addAttribute("cpfCnpj", cpfCnpj);
        model.addAttribute("totalVendas", vendasFiltradas.size());
        model.addAttribute("valorTotalFormatado", vendaService.formatarValorTotal(vendasFiltradas));
        return "vendas/lista";
    }

    // ➕ FORMULÁRIO DE NOVA VENDA
    @GetMapping("/nova")
    public String novaVenda(Model model) {
        model.addAttribute("venda", new Venda());
        model.addAttribute("clientes", clienteService.buscarTodos());
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "vendas/cadastro";
    }

    // 🏪 PDV - PONTO DE VENDA
    @GetMapping("/pdv")
    public String pdv(Model model) {
        try {
            // Verificar se há caixa aberto
            if (!caixaService.existeCaixaAberto()) {
                model.addAttribute("erro", "Não há caixa aberto. Abra o caixa antes de realizar vendas.");
                return "vendas/caixa-fechado";
            }
            
            model.addAttribute("clientes", clienteService.buscarTodos());
            model.addAttribute("formasPagamento", formaPagamentoService.buscarAtivas());
            model.addAttribute("resumoDia", vendaService.obterResumoVendasDia());
            
            return "vendas/pdv";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar PDV: " + e.getMessage());
            return "error";
        }
    }
    


    // 🔍 BUSCAR PRODUTO POR EAN (API REST para PDV)
    @GetMapping("/api/produto/{ean}")
    @ResponseBody
    public ResponseEntity<?> buscarProdutoPorEan(@PathVariable String ean) {
        try {
            var produto = produtoService.buscarPorEan(ean);
            if (produto.isPresent()) {
                return ResponseEntity.ok(produto.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao buscar produto: " + e.getMessage());
        }
    }

    // 💳 PROCESSAR VENDA PDV (API REST)
    @PostMapping("/api/processar")
    @ResponseBody
    public ResponseEntity<VendaPdvResponse> processarVendaPdv(@RequestBody VendaPdvRequest request) {
        try {
            // Obter usuário logado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Criar nova venda
            Venda venda = new Venda();
            venda.setFormaPagamento(request.getFormaPagamento());
            venda.setObservacoes(request.getObservacoes());
            venda.setUsuario(usuario);
            venda.setDesconto(request.getDesconto() != null ? request.getDesconto() : BigDecimal.ZERO);
            venda.setValorPago(request.getValorPago());
            venda.setParcelas(request.getParcelas() != null ? request.getParcelas() : 1);

            // Buscar cliente se informado
            if (request.getClienteId() != null) {
                Optional<Cliente> cliente = clienteService.buscarPorId(request.getClienteId());
                cliente.ifPresent(venda::setCliente);
            }

            // Criar itens da venda
            List<VendaItem> itens = new ArrayList<>();

            for (VendaPdvRequest.ItemRequest itemRequest : request.getItens()) {
                Optional<Produto> produto = produtoService.buscarPorEan(itemRequest.getEan());
                if (produto.isPresent()) {
                    VendaItem item = new VendaItem();
                    item.setProduto(produto.get());
                    item.setQuantidade(itemRequest.getQuantidade());
                    item.setPrecoUnitario(produto.get().getPreco());
                    item.setVenda(venda);
                    itens.add(item);
                }
            }

            venda.setItens(itens);

            // Processar venda usando o service específico do PDV
            Venda vendaSalva = vendaService.processarVendaPdv(venda);

            // Retornar resposta
            VendaPdvResponse response = new VendaPdvResponse();
            response.setId(vendaSalva.getId());
            response.setNumeroVenda(vendaSalva.getNumeroVenda());
            response.setTotal(vendaSalva.getTotal());
            response.setTroco(vendaSalva.getTroco());
            response.setDataVenda(vendaSalva.getDataVenda());
            response.setSucesso(true);
            response.setMensagem("Venda processada com sucesso!");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            VendaPdvResponse response = new VendaPdvResponse();
            response.setSucesso(false);
            response.setMensagem(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            VendaPdvResponse response = new VendaPdvResponse();
            response.setSucesso(false);
            response.setMensagem("Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ SALVAR VENDA
    @PostMapping("/salvar")
    public String salvarVenda(@ModelAttribute Venda venda, @RequestParam Long clienteId, Model model) {
        Cliente cliente = clienteService.buscarPorId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        venda.setCliente(cliente);

        if (venda.getDataVenda() == null) {
            venda.setDataVenda(LocalDateTime.now());
        }

        BigDecimal total = BigDecimal.ZERO;
        for (VendaItem item : venda.getItens()) {
            item.setVenda(venda);
            Produto produto = produtoService.buscarPorId(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
            item.setProduto(produto);

            BigDecimal subtotal = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotal);
        }

        venda.setTotal(total);
        Venda vendaSalva = vendaService.salvar(venda);
        return "redirect:/vendas";
    }

    // 🧾 GERAR PDF COM QR CODE E IMPOSTOS
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> gerarPdfVenda(@PathVariable Long id) throws DocumentException, IOException {
        Venda venda = vendaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Cabeçalho
        document.add(new Paragraph("---------------------------------------------------", titulo));
        document.add(new Paragraph("              NOTA FISCAL DE VENDA", titulo));
        document.add(new Paragraph("---------------------------------------------------\n", titulo));
        document.add(new Paragraph("Número da Venda: " + String.format("%06d", venda.getId()), normal));
        document.add(new Paragraph("Data da Emissão: " + dtf.format(venda.getDataVenda()) + "\n", normal));

        // Cliente
        Cliente c = venda.getCliente();
        document.add(new Paragraph("Cliente:", titulo));
        document.add(new Paragraph("Nome/Razão Social: " + c.getNome(), normal));
        document.add(new Paragraph("CPF/CNPJ: " + c.getCpfCnpj(), normal));
        document.add(new Paragraph("Endereço: " + c.getLogradouro() + ", " + c.getNumero() +
                (c.getComplemento() != null ? " - " + c.getComplemento() : ""), normal));
        document.add(new Paragraph("Bairro: " + c.getBairro(), normal));
        document.add(new Paragraph("Cidade/UF: " + c.getCidade() + "/" + c.getEstado(), normal));
        document.add(new Paragraph("CEP: " + c.getCep(), normal));
        document.add(new Paragraph("Telefone: " + c.getTelefone(), normal));
        document.add(new Paragraph("E-mail: " + c.getEmail(), normal));
        document.add(new Paragraph("\n---------------------------------------------------", titulo));
        document.add(new Paragraph("Itens da Venda:", titulo));
        document.add(new Paragraph("---------------------------------------------------", titulo));

        // Itens da Venda
        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new int[] { 2, 6, 3, 3 });

        tabela.addCell("Qtde");
        tabela.addCell("Descrição");
        tabela.addCell("V. Unit.");
        tabela.addCell("Subtotal");

        for (VendaItem item : venda.getItens()) {
            tabela.addCell(String.valueOf(item.getQuantidade()));
            tabela.addCell(item.getProduto().getNome());
            tabela.addCell(nf.format(item.getPrecoUnitario()));
            tabela.addCell(nf.format(item.getSubtotal()));
        }

        document.add(tabela);
        document.add(new Paragraph("---------------------------------------------------", titulo));
        document.add(new Paragraph("Forma de Pagamento: " + venda.getFormaPagamento(), normal));
        document.add(new Paragraph("Status: " + venda.getStatus(), normal));
        document.add(new Paragraph("Observações: " + (venda.getObservacoes() == null ? "---" : venda.getObservacoes()),
                normal));
        document.add(new Paragraph("---------------------------------------------------", titulo));

        // Cálculo dos impostos usando BigDecimal para precisão
        BigDecimal total = venda.getTotal();
        BigDecimal icms = total.multiply(BigDecimal.valueOf(0.18));
        BigDecimal ipi = total.multiply(BigDecimal.valueOf(0.05));
        BigDecimal desconto = BigDecimal.ZERO; // Ajuste se quiser aplicar desconto
        BigDecimal valorFinal = total.add(icms).add(ipi).subtract(desconto);

        document.add(new Paragraph("Totais e Impostos:", titulo));
        document.add(new Paragraph("Subtotal: " + nf.format(total), normal));
        document.add(new Paragraph("ICMS (18%): " + nf.format(icms), normal));
        document.add(new Paragraph("IPI (5%): " + nf.format(ipi), normal));
        document.add(new Paragraph("Desconto: " + nf.format(desconto), normal));
        document.add(new Paragraph("Valor Final: " + nf.format(valorFinal), titulo));

        document.add(new Paragraph("---------------------------------------------------\n", titulo));

        // Emitente
        document.add(new Paragraph("Emitente: JS Móveis Ltda.", normal));
        document.add(new Paragraph("CNPJ: 12.345.789/0001-90", normal));
        document.add(new Paragraph("Endereço: Rua Exemplo, 123 - Cidade - Estado", normal));
        document.add(new Paragraph("Telefone: (21) 3231-3322\n", normal));

        // QR Code
        try {
            ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode("https://seudominio.com/nota/" + venda.getId(),
                    BarcodeFormat.QR_CODE, 100, 100);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrBaos);

            Image qrImage = Image.getInstance(qrBaos.toByteArray());
            // Posiciona no canto inferior direito da página
            qrImage.setAbsolutePosition(document.right() - 100, document.bottom() + 20);
            qrImage.scaleToFit(80, 80);
            document.add(qrImage);
        } catch (WriterException e) {
            throw new RuntimeException("Erro ao gerar QR Code", e);
        }

        document.add(new Paragraph("\nObrigado pela preferência!", titulo));
        document.add(new Paragraph("---------------------------------------------------", titulo));

        document.close();

        byte[] pdfBytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=venda_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
