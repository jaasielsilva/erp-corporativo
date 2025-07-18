package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.VendaService;
import com.lowagie.text.Cell;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    // 🧾 LISTAR VENDAS
    @GetMapping
    public String listar(
            @RequestParam(name = "cpfCnpj", required = false) String cpfCnpj,
            Model model) {

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

    // ✅ SALVAR VENDA COM PRODUTOS DINÂMICOS DO FORMULÁRIO
    @PostMapping("/salvar")
    public String salvarVenda(@ModelAttribute Venda venda,
                         @RequestParam Long clienteId,
                         Model model) {

    Cliente cliente = clienteService.buscarPorId(clienteId)
        .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

    venda.setCliente(cliente);

    if (venda.getDataVenda() == null) {
        venda.setDataVenda(LocalDateTime.now());
    }

    // Vincula cada item à venda e calcula total
    BigDecimal total = BigDecimal.ZERO;
    for (VendaItem item : venda.getItens()) {
        item.setVenda(venda);

        // Para garantir: busca o produto completo no BD pelo id
        Produto produto = produtoService.buscarPorId(item.getProduto().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        item.setProduto(produto);

        // Somar subtotal: precoUnitario * quantidade
        BigDecimal subtotal = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        total = total.add(subtotal);
    }

    venda.setTotal(total);

    vendaService.salvar(venda);

    return "redirect:/vendas";
}


    @GetMapping("/{id}/pdf")
public ResponseEntity<byte[]> gerarPdfVenda(@PathVariable Long id) throws DocumentException {
    Venda venda = vendaService.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));

    Document document = new Document();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfWriter.getInstance(document, baos);
    document.open();

    // Fonts
    Font tituloGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);
    Font pequeno = FontFactory.getFont(FontFactory.HELVETICA, 10);

    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // Linha de separação (usando Paragraph)
    Paragraph linhaSeparadora = new Paragraph("---------------------------------------------------", normal);
    linhaSeparadora.setAlignment(Element.ALIGN_CENTER);

    // Cabeçalho
    document.add(linhaSeparadora);
    Paragraph cabecalho = new Paragraph("NOTA FISCAL DE VENDA", tituloGrande);
    cabecalho.setAlignment(Element.ALIGN_CENTER);
    document.add(cabecalho);
    document.add(linhaSeparadora);

    // Número e data da venda
    document.add(new Paragraph("Número da Venda: " + venda.getId(), normal));
    document.add(new Paragraph("Data da Emissão: " + venda.getDataVenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normal));
    document.add(new Paragraph("\n"));

    // Dados do Cliente
    Cliente cliente = venda.getCliente();
    document.add(new Paragraph("Cliente:", titulo));
    document.add(new Paragraph("Nome/Razão Social: " + cliente.getNome(), normal));
    document.add(new Paragraph("CPF/CNPJ: " + cliente.getCpfCnpj(), normal));
    document.add(new Paragraph("Endereço: " + cliente.getLogradouro() + ", " + cliente.getNumero() + (cliente.getComplemento() != null ? " - " + cliente.getComplemento() : ""), normal));
    document.add(new Paragraph("Bairro: " + cliente.getBairro(), normal));
    document.add(new Paragraph("Cidade/UF: " + cliente.getCidade() + "/" + cliente.getEstado(), normal));
    document.add(new Paragraph("CEP: " + cliente.getCep(), normal));
    document.add(new Paragraph("Telefone: " + cliente.getTelefone(), normal));
    document.add(new Paragraph("E-mail: " + cliente.getEmail(), normal));
    document.add(linhaSeparadora);

    // Itens da venda - tabela simples com 4 colunas (Qtde, Descrição, Valor Unit., Total)
    PdfPTable tabela = new PdfPTable(4);
    tabela.setWidthPercentage(100);
    tabela.setWidths(new float[]{1.5f, 6f, 3f, 3f});

    // Cabeçalho da tabela
    tabela.addCell(new PdfPCell(new Paragraph("Qtde", titulo)));
    tabela.addCell(new PdfPCell(new Paragraph("Descrição", titulo)));
    tabela.addCell(new PdfPCell(new Paragraph("Valor Unitário", titulo)));
    tabela.addCell(new PdfPCell(new Paragraph("Total", titulo)));

    // Itens
    for (VendaItem item : venda.getItens()) {
        tabela.addCell(new PdfPCell(new Paragraph(String.valueOf(item.getQuantidade()), normal)));
        tabela.addCell(new PdfPCell(new Paragraph(item.getProduto().getNome(), normal)));
        tabela.addCell(new PdfPCell(new Paragraph(nf.format(item.getPrecoUnitario()), normal)));
        tabela.addCell(new PdfPCell(new Paragraph(nf.format(item.getSubtotal()), normal)));
    }

    document.add(tabela);
    document.add(new Paragraph("\n"));

    // Forma de pagamento, status e observações
    document.add(new Paragraph("Forma de Pagamento: " + venda.getFormaPagamento(), normal));
    document.add(new Paragraph("Status: " + venda.getStatus(), normal));
    document.add(new Paragraph("Observações: " + (venda.getObservacoes() == null ? "---" : venda.getObservacoes()), normal));
    document.add(linhaSeparadora);

    // Total da venda
    Paragraph totalVenda = new Paragraph("Valor Total da Venda: " + nf.format(venda.getTotal()), titulo);
    totalVenda.setAlignment(Element.ALIGN_CENTER);
    document.add(totalVenda);
    document.add(linhaSeparadora);

    // Dados do emitente fixos
    document.add(new Paragraph("Emitente: Seu Nome ou Empresa Ltda.", normal));
    document.add(new Paragraph("CNPJ Emitente: XX.XXX.XXX/0001-XX", normal));
    document.add(new Paragraph("Endereço Emitente: Rua Exemplo, 123 - Cidade - Estado", normal));
    document.add(new Paragraph("Telefone Emitente: (XX) XXXX-XXXX", normal));
    document.add(linhaSeparadora);

    // Agradecimento
    Paragraph agradecimento = new Paragraph("Obrigado pela preferência!", titulo);
    agradecimento.setAlignment(Element.ALIGN_CENTER);
    document.add(agradecimento);
    document.add(linhaSeparadora);

    document.close();

    byte[] pdfBytes = baos.toByteArray();

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=venda_" + venda.getId() + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }
}