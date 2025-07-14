package com.jaasielsilva.portalceo.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import com.jaasielsilva.portalceo.service.UsuarioService;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PerfilRepository perfilRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;

    public UsuarioController(UsuarioService usuarioService,
                             PerfilRepository perfilRepository,
                             CargoRepository cargoRepository,
                             DepartamentoRepository departamentoRepository) {
        this.usuarioService = usuarioService;
        this.perfilRepository = perfilRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
    }

    // ===============================
    // BLOCO: PÁGINA PRINCIPAL E LISTAGEM
    // ===============================

    /**
     * Exibe a lista de usuários, com opção de busca por nome ou email.
     */
    @GetMapping
    public String listarUsuarios(@RequestParam(value = "busca", required = false) String busca, Model model) {
        List<Usuario> usuarios = (busca == null || busca.isEmpty())
                ? usuarioService.buscarTodos()
                : usuarioService.buscarPorNomeOuEmail(busca);
        model.addAttribute("usuarios", usuarios);
        return "usuarios/listar";
    }

    /**
     * Exibe as estatísticas gerais dos usuários no sistema.
     */
    @GetMapping("/index")
    public String mostrarEstatisticasUsuarios(Model model) {
        EstatisticasUsuariosDTO stats = usuarioService.buscarEstatisticas();
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("totalUsuarios", stats.getTotalUsuarios());
        model.addAttribute("ativos", stats.getTotalAtivos());
        model.addAttribute("administradores", stats.getTotalAdministradores());
        model.addAttribute("bloqueados", stats.getTotalBloqueados());
        return "usuarios/index";
    }

    // ===============================
    // BLOCO: CADASTRO E EDIÇÃO DE USUÁRIOS
    // ===============================

    /**
     * Exibe o formulário para cadastro de novo usuário.
     */
    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        adicionarAtributosComuns(model);
        return "usuarios/cadastro";
    }

    /**
     * Processa o cadastro de um novo usuário.
     * Valida senhas e associa perfil selecionado.
     */
    @PostMapping("/cadastrar")
    public String cadastrarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,
                                   BindingResult bindingResult,
                                   @RequestParam("confirmSenha") String confirmSenha,
                                   @RequestParam("perfilId") Long perfilId,
                                   Model model) {

        if (!usuario.getSenha().equals(confirmSenha)) {
            bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
        }

        if (bindingResult.hasErrors()) {
            adicionarAtributosComuns(model);
            return "usuarios/cadastro";
        }

        try {
            usuario.setPerfis(Set.of(perfilRepository.findById(perfilId)
                    .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"))));

            if (usuario.getStatus() != Usuario.Status.DEMITIDO) {
                usuario.setDataDesligamento(null);
            }

            usuarioService.salvarUsuario(usuario);

        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            adicionarAtributosComuns(model);
            return "usuarios/cadastro";
        }

        return "redirect:/dashboard";
    }

    /**
     * Exibe o formulário para edição de usuário existente.
     */
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
    Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
    if (usuarioOpt.isEmpty()) {
        return "redirect:/usuarios";
    }
    model.addAttribute("usuario", usuarioOpt.get());
    adicionarAtributosComuns(model);
    return "usuarios/editar";
}


    /**
     * Teste para disparar erro HTTP 400 Bad Request.
     */
    @GetMapping("/test-error/400")
    public void error400() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request Teste");
    }

    /**
     * Teste para disparar erro HTTP 401 Unauthorized.
     */
    @GetMapping("/test-error/401")
    public void error401() {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized Teste");
    }

    /**
     * Teste para disparar erro HTTP 403 Forbidden.
     */
    @GetMapping("/test-error/403")
    public void error403() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden Teste");
    }

    /**
     * Teste para disparar erro HTTP 404 Not Found.
     */
    @GetMapping("/test-error/404")
    public void error404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found Teste");
    }

    /**
     * Teste para disparar erro HTTP 500 Internal Server Error.
     */
    @GetMapping("/test-error/500")
    public void error500() {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error Teste");
    }

    /**
     * Salva as alterações feitas no usuário após edição.
     * Se for data de desligamento, exclui o usuário.
     */
    @PostMapping("/{id}/editar")
public String salvarEdicaoUsuario(@PathVariable Long id,
                                  @Valid @ModelAttribute("usuario") Usuario usuario,
                                  BindingResult bindingResult,
                                  @RequestParam("confirmSenha") String confirmSenha,
                                  @RequestParam("perfilId") Long perfilId,
                                  Model model,
                                  Principal principal) {

    // Busca o usuário original no banco para garantir que dataAdmissao não seja alterada
    Usuario usuarioBanco = usuarioService.buscarPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    usuario.setDataAdmissao(usuarioBanco.getDataAdmissao());

    if (!usuario.getSenha().equals(confirmSenha)) {
        bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
    }

    if (bindingResult.hasErrors()) {
        adicionarAtributosComuns(model);
        return "usuarios/editar";
    }

    try {
        if (usuario.getDataDesligamento() != null) {
            String emailLogado = principal.getName();
            Usuario usuarioLogado = usuarioService.buscarPorEmail(emailLogado)
                                        .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado"));

            usuarioService.excluirUsuario(id, usuarioLogado.getMatricula());
            return "redirect:/usuarios";
        }

        usuario.setId(id);
        usuario.setPerfis(Set.of(perfilRepository.findById(perfilId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"))));

        usuarioService.salvarUsuario(usuario);

    } catch (Exception e) {
        model.addAttribute("erro", e.getMessage());
        adicionarAtributosComuns(model);
        return "usuarios/editar";
    }

    return "redirect:/usuarios";
}

    /**
     * Busca usuário por CPF e redireciona para página de edição.
     */
    @PostMapping("/editar")
    public String buscarUsuarioPorCpf(@RequestParam("cpf") String cpf, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCpf(cpf);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("erro", "Usuário com CPF " + cpf + " não encontrado.");
            return "usuarios/editar";
        }
        model.addAttribute("usuario", usuarioOpt.get());
        adicionarAtributosComuns(model);
        return "usuarios/editarcadastro";
    }

    /**
     * Exibe formulário para busca por CPF.
     */
    @GetMapping("/editar")
    public String mostrarFormularioBuscaPorCpf() {
        return "usuarios/editar";
    }

    // ===============================
    // BLOCO: EXCLUSÃO DE USUÁRIO COM TRATAMENTO DE ERRO
    // ===============================

    /**
     * Endpoint para exclusão de usuário.
     * Recebe matrícula no header para validação.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/excluir")
    public ResponseEntity<?> excluirUsuario(
            @PathVariable Long id,
            @RequestHeader(name = "X-Matricula") String matriculaSolicitante) {
        try {
            usuarioService.excluirUsuario(id, matriculaSolicitante);
            return ResponseEntity.ok(Map.of("mensagem", "Usuário excluído com sucesso."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Erro inesperado ao excluir usuário."));
        }
    }

    // ===============================
    // BLOCO: UTILITÁRIOS
    // ===============================

    /**
     * Valida se uma matrícula tem permissão para exclusão.
     */
    @GetMapping("/validar-matricula")
    @ResponseBody
    public Map<String, Boolean> validar(@RequestParam String matricula) {
        boolean autorizado = usuarioService.usuarioTemPermissaoParaExcluir(matricula);
        return Map.of("autorizado", autorizado);
    }

    /**
     * Retorna a foto do usuário.
     */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> exibirFoto(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getFotoPerfil() != null) {
            byte[] foto = usuarioOpt.get().getFotoPerfil();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(foto, headers, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Exibe detalhes completos do usuário.
     */
    @GetMapping("/{id}/detalhes")
    public String exibirDetalhesUsuario(@PathVariable Long id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isEmpty()) return "redirect:/usuarios";
        model.addAttribute("usuario", usuarioOpt.get());
        return "usuarios/detalhes";
    }

    /**
     * Exibe dados do usuário logado.
     */
    @GetMapping("/relatorio")
    public String meusDados(Model model, Principal principal) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(principal.getName());
        if (usuarioOpt.isEmpty()) return "redirect:/logout";
        model.addAttribute("usuario", usuarioOpt.get());
        return "usuarios/relatorio-usuarios";
    }

    // ===============================
    // BLOCO: RELATÓRIOS E EXPORTAÇÃO
    // ===============================

    @GetMapping("/relatorio-geral")
    public String relatorioGeral(Model model) {
        return "usuarios/relatorio-geral";
    }

    @GetMapping("/comparativo")
    public String comparativoPeriodos() {
        return "usuarios/comparativo-periodos";
    }

    @GetMapping("/comparativo-perfil")
    public String comparativoPerfis() {
        return "usuarios/comparativo-perfil";
    }

    @GetMapping("/enviar-email")
    public String enviarEmailRelatorio() {
        return "usuarios/enviar-email";
    }

    @GetMapping("/filtros-avancados")
    public String filtrosAvancados() {
        return "usuarios/filtros-avancados";
    }

    @GetMapping("/estatisticas")
    public String estatisticasUsuarios() {
        return "usuarios/estatisticas-usuarios";
    }

    @GetMapping("/ativar-desativar")
    public String ativarDesativarUsuarios() {
        return "usuarios/ativar-desativar";
    }

    @GetMapping("/alertas")
    public String alertasSeguranca() {
        return "usuarios/alertas";
    }

    @GetMapping("/exportar-excel")
    public String exportarExcel() {
        return "redirect:/usuarios/relatorio";
    }

    @GetMapping("/exportar-json")
    public String exportarJson() {
        return "redirect:/usuarios/relatorio";
    }

    @GetMapping("/resetar-senha")
    public String listarPermissoes(Model model) {
        return "usuarios/resetar-senha";
    }

    // ===============================
    // BLOCO: RELATÓRIOS PDF
    // ===============================

    /**
     * Gera PDF com informações completas do usuário.
     */
    @GetMapping("/{id}/pdf")
    public void gerarPdfUsuario(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Usuario usuario = usuarioOpt.get();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=curriculo_usuario_" + usuario.getId() + ".pdf");

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph titulo = new Paragraph("Currículo Profissional", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20f);
            document.add(titulo);

            Font secaoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font textoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Dados Pessoais", secaoFont));
            document.add(new Paragraph("Nome: " + usuario.getNome(), textoFont));
            document.add(new Paragraph("Email: " + usuario.getEmail(), textoFont));
            document.add(new Paragraph("Telefone: " + usuario.getTelefone(), textoFont));
            document.add(new Paragraph("Data de Nascimento: " + usuario.getDataNascimento(), textoFont));
            document.add(new Paragraph("CPF: " + usuario.getCpf(), textoFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Endereço", secaoFont));
            document.add(new Paragraph(usuario.getEndereco() + ", " + usuario.getCidade() + " - " +
                    usuario.getEstado() + " (" + usuario.getCep() + ")", textoFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Informações Profissionais", secaoFont));
            document.add(new Paragraph("Departamento: " + usuario.getDepartamento().getNome(), textoFont));
            document.add(new Paragraph("Cargo: " + usuario.getCargo().getNome(), textoFont));
            document.add(new Paragraph("Nível de Acesso: " + usuario.getNivelAcesso(), textoFont));
            document.add(new Paragraph("Status: " + usuario.getStatus(), textoFont));
            document.add(new Paragraph("Data de Admissão: " + usuario.getDataAdmissao(), textoFont));
            if (usuario.getDataDesligamento() != null) {
                document.add(new Paragraph("Data de Desligamento: " + usuario.getDataDesligamento(), textoFont));
            }

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Perfis de Acesso", secaoFont));

            StringBuilder perfisStr = new StringBuilder();
            usuario.getPerfis().forEach(perfil -> perfisStr.append(perfil.getNome()).append(", "));
            if (!usuario.getPerfis().isEmpty()) {
                perfisStr.setLength(perfisStr.length() - 2); // remove última vírgula
            }
            document.add(new Paragraph(perfisStr.toString(), textoFont));

            document.close();
        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF", e);
        }
    }

    // ===============================
    // BLOCO: RESETAR SENHA
    // ===============================

    /**
     * Reseta a senha do usuário pelo ID (via POST).
     * Retorna JSON com status para requisição AJAX.
     */
    @PostMapping("/{id}/resetar-senha")
    @ResponseBody
    public Map<String, String> resetarSenhaPorId(@PathVariable Long id) {
        Map<String, String> resposta = new HashMap<>();
        try {
            usuarioService.resetarSenhaPorId(id);
            resposta.put("status", "sucesso");
            resposta.put("mensagem", "Senha resetada com sucesso para o usuário " + id);
        } catch (Exception e) {
            e.printStackTrace();
            resposta.put("status", "erro");
            resposta.put("mensagem", "Erro ao resetar senha: " + e.getMessage());
        }
        return resposta;
    }

    // ===============================
    // MÉTODO AUXILIAR
    // ===============================

    /**
     * Adiciona dados comuns para formulários e listas de usuários.
     */
    private void adicionarAtributosComuns(Model model) {
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("generos", Genero.values());
        model.addAttribute("status", Usuario.Status.values());
        model.addAttribute("niveis", NivelAcesso.values());
        model.addAttribute("cargos", cargoRepository.findAll());
        model.addAttribute("departamentos", departamentoRepository.findAll());
    }

}
