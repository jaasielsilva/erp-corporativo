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


    // metodo pra imprimir o comprovante PDF da comprarealizada
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> gerarPdfVenda(@PathVariable Long id) throws DocumentException {
    Venda venda = vendaService.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));

    Document document = new Document();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PdfWriter.getInstance(document, baos);
    document.open();

    Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);

    document.add(new Paragraph("Detalhes da Venda", titulo));
    document.add(new Paragraph("Cliente: " + venda.getCliente().getNome(), normal));
    document.add(new Paragraph("Data da Venda: " + venda.getDataVenda().toString(), normal));
    document.add(new Paragraph("Forma de Pagamento: " + venda.getFormaPagamento(), normal));
    document.add(new Paragraph("Status: " + venda.getStatus(), normal));
    document.add(new Paragraph("Observações: " + (venda.getObservacoes() == null ? "" : venda.getObservacoes()), normal));
    document.add(new Paragraph("\n"));

    PdfPTable tabela = new PdfPTable(5);
    tabela.setWidthPercentage(100);
    tabela.setWidths(new int[]{4, 2, 2, 2, 2});

    tabela.addCell(new PdfPCell(new Paragraph("Produto", titulo)));
    tabela.addCell(new PdfPCell(new Paragraph("EAN", titulo)));
    tabela.addCell(new PdfPCell(new Paragraph("Quantidade", titulo)));
    tabela.addCell(new PdfPCell(new Paragraph("Preço Unit.", titulo)));
    tabela.addCell(new PdfPCell(new Paragraph("Subtotal", titulo)));

    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    for (VendaItem item : venda.getItens()) {
        tabela.addCell(new PdfPCell(new Paragraph(item.getProduto().getNome(), normal)));
        tabela.addCell(new PdfPCell(new Paragraph(item.getProduto().getEan(), normal)));
        tabela.addCell(new PdfPCell(new Paragraph(String.valueOf(item.getQuantidade()), normal)));
        tabela.addCell(new PdfPCell(new Paragraph(nf.format(item.getPrecoUnitario()), normal)));
        tabela.addCell(new PdfPCell(new Paragraph(nf.format(item.getSubtotal()), normal)));
    }

    document.add(tabela);
    document.add(new Paragraph("\n"));
    document.add(new Paragraph("Total da Venda: " + nf.format(venda.getTotal()), titulo));

    document.close();

    byte[] pdfBytes = baos.toByteArray();

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=venda_" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
}
}
