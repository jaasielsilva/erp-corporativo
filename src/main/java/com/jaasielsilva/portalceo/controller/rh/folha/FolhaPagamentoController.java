package com.jaasielsilva.portalceo.controller.rh.folha;

import com.jaasielsilva.portalceo.model.FolhaPagamento;
import com.jaasielsilva.portalceo.model.Holerite;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.FolhaPagamentoService;
import com.jaasielsilva.portalceo.service.HoleriteService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.ResumoFolhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rh/folha-pagamento")
public class FolhaPagamentoController {

    @Autowired
    private FolhaPagamentoService folhaPagamentoService;

    @Autowired
    private HoleriteService holeriteService;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ResumoFolhaService resumoFolhaService;

    /**
     * Página principal da folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping
    public String index(Model model) {
        model.addAttribute("folhasRecentes", folhaPagamentoService.buscarFolhasRecentes());
        
        // Dados do mês atual
        LocalDate hoje = LocalDate.now();
        Optional<FolhaPagamento> folhaAtual = folhaPagamentoService.buscarPorMesAno(hoje.getMonthValue(), hoje.getYear());
        model.addAttribute("folhaAtual", folhaAtual.orElse(null));
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        
        return "rh/folha-pagamento/index";
    }

    /**
     * Formulário para gerar nova folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @GetMapping("/gerar")
    public String gerar(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        
        // Verificar se já existe folha para o mês atual
        LocalDate hoje = LocalDate.now();
        boolean existeFolha = folhaPagamentoService.existeFolhaPorMesAno(hoje.getMonthValue(), hoje.getYear());
        model.addAttribute("existeFolhaAtual", existeFolha);
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        YearMonth ym = YearMonth.of(hoje.getYear(), hoje.getMonthValue());
        model.addAttribute("diasMesAtual", ym.lengthOfMonth());
        List<com.jaasielsilva.portalceo.dto.ColaboradorResumoFolhaDTO> colaboradoresResumo = colaboradorService
                .listarAtivos()
                .stream()
                .map(c -> resumoFolhaService.criarResumo(c, ym))
                .collect(Collectors.toList());
        model.addAttribute("colaboradoresResumo", colaboradoresResumo);
        
        return "rh/folha-pagamento/gerar";
    }

    /**
     * Processa geração da folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping("/processar")
    public String processar(@RequestParam Integer mes, 
                           @RequestParam Integer ano,
                           RedirectAttributes redirectAttributes) {
        try {
            // Obter usuário logado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            FolhaPagamento folha = folhaPagamentoService.gerarFolhaPagamento(mes, ano, usuario);
            
            redirectAttributes.addFlashAttribute("mensagem", 
                "Folha de pagamento " + mes + "/" + ano + " gerada com sucesso!");
            
            return "redirect:/rh/folha-pagamento/visualizar/" + folha.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao gerar folha: " + e.getMessage());
            return "redirect:/rh/folha-pagamento/gerar";
        }
    }

    /**
     * Visualiza uma folha de pagamento específica
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/visualizar/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        Optional<FolhaPagamento> folhaOpt = folhaPagamentoService.buscarPorId(id);
        if (folhaOpt.isEmpty()) {
            return "redirect:/rh/folha-pagamento";
        }
        
        FolhaPagamento folha = folhaOpt.get();
        model.addAttribute("folha", folha);
        model.addAttribute("holerites", holeriteService.listarPorFolha(id));
        model.addAttribute("resumo", holeriteService.calcularResumoFolha(id));
        
        return "rh/folha-pagamento/visualizar";
    }

    /**
     * Lista todas as folhas de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) Integer ano, Model model) {
        if (ano != null) {
            model.addAttribute("folhas", folhaPagamentoService.buscarPorAno(ano));
            model.addAttribute("anoSelecionado", ano);
        } else {
            model.addAttribute("folhas", folhaPagamentoService.listarTodas());
        }
        
        return "rh/folha-pagamento/listar";
    }

    /**
     * Fecha uma folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping("/fechar/{id}")
    public String fechar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            folhaPagamentoService.fecharFolha(id, usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Folha de pagamento fechada com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao fechar folha: " + e.getMessage());
        }
        
        return "redirect:/rh/folha-pagamento/visualizar/" + id;
    }

    /**
     * Cancela uma folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            folhaPagamentoService.cancelarFolha(id, usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Folha de pagamento cancelada com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar folha: " + e.getMessage());
        }
        
        return "redirect:/rh/folha-pagamento";
    }

    /**
     * Exibe holerite individual
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/holerite/{id}")
    public String holerite(@PathVariable Long id, Model model) {
        Optional<Holerite> holeriteOpt = holeriteService.buscarPorId(id);
        if (holeriteOpt.isEmpty()) {
            return "redirect:/rh/folha-pagamento";
        }
        
        Holerite holerite = holeriteOpt.get();
        model.addAttribute("holerite", holerite);
        model.addAttribute("numeroHolerite", holeriteService.gerarNumeroHolerite(holerite));
        model.addAttribute("periodoReferencia", holeriteService.gerarDescricaoPeriodo(holerite));
        model.addAttribute("dataReferencia", holeriteService.gerarDataReferencia(holerite));
        
        return "rh/folha-pagamento/holerite";
    }

    /**
     * Lista holerites por colaborador
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/holerites/colaborador/{colaboradorId}")
    public String holeritesPorColaborador(@PathVariable Long colaboradorId, Model model) {
        try {
            model.addAttribute("holerites", holeriteService.listarPorColaborador(colaboradorId));
            model.addAttribute("colaborador", colaboradorService.buscarPorId(colaboradorId));
            return "rh/folha-pagamento/holerites-colaborador";
        } catch (Exception e) {
            return "redirect:/rh/folha-pagamento";
        }
    }

    /**
     * Página de descontos
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/descontos")
    public String descontos(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/folha-pagamento/descontos";
    }

    /**
     * Relatórios da folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("folhasRecentes", folhaPagamentoService.buscarFolhasRecentes());
        
        // Dados do mês atual para resumo
        LocalDate hoje = LocalDate.now();
        Optional<FolhaPagamento> folhaAtual = folhaPagamentoService.buscarPorMesAno(hoje.getMonthValue(), hoje.getYear());
        if (folhaAtual.isPresent()) {
            model.addAttribute("resumoAtual", holeriteService.calcularResumoFolha(folhaAtual.get().getId()));
        }
        
        return "rh/folha-pagamento/relatorios";
    }
    /**
     * Exporta o holerite em PDF
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/holerite/{id}/pdf")
    public void holeritePdf(@PathVariable Long id, HttpServletResponse response) throws java.io.IOException {
        Optional<Holerite> holeriteOpt = holeriteService.buscarPorId(id);
        if (holeriteOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Holerite h = holeriteOpt.get();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=holerite_" + id + ".pdf");

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph pTitulo = new Paragraph("Holerite", titulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingAfter(10f);
            document.add(pTitulo);

            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Colaborador: " + h.getColaborador().getNome(), normal));
            document.add(new Paragraph("Departamento: " + (h.getColaborador().getDepartamento()!=null ? h.getColaborador().getDepartamento().getNome() : "-"), normal));
            document.add(new Paragraph("Cargo: " + (h.getColaborador().getCargo()!=null ? h.getColaborador().getCargo().getNome() : "-"), normal));
            document.add(new Paragraph("Período: " + holeriteService.gerarDescricaoPeriodo(h), normal));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Proventos", titulo));
            document.add(new Paragraph("Salário Base: R$ " + h.getSalarioBase(), normal));
            document.add(new Paragraph("Horas Extras: R$ " + h.getHorasExtras(), normal));
            document.add(new Paragraph("Auxílio Saúde: R$ " + h.getAuxilioSaude(), normal));
            document.add(new Paragraph("Vale Refeição: R$ " + h.getValeRefeicao(), normal));
            document.add(new Paragraph("Vale Transporte: R$ " + h.getValeTransporte(), normal));
            document.add(new Paragraph("Total Proventos: R$ " + h.getTotalProventos(), normal));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Descontos", titulo));
            document.add(new Paragraph("INSS: R$ " + h.getDescontoInss(), normal));
            document.add(new Paragraph("IRRF: R$ " + h.getDescontoIrrf(), normal));
            document.add(new Paragraph("FGTS: R$ " + h.getDescontoFgts(), normal));
            document.add(new Paragraph("Plano de Saúde: R$ " + h.getDescontoPlanoSaude(), normal));
            document.add(new Paragraph("Desconto VT: R$ " + h.getDescontoValeTransporte(), normal));
            document.add(new Paragraph("Desconto VR: R$ " + h.getDescontoValeRefeicao(), normal));
            document.add(new Paragraph("Outros Descontos: R$ " + h.getOutrosDescontos(), normal));
            document.add(new Paragraph("Total Descontos: R$ " + h.getTotalDescontos(), normal));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Salário Líquido: R$ " + h.getSalarioLiquido(), titulo));
            document.close();
        } catch (DocumentException e) {
            throw new java.io.IOException("Erro ao gerar PDF", e);
        }
    }
}
