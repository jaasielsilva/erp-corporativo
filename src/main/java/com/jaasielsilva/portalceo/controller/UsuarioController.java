package com.jaasielsilva.portalceo.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.Genero;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.service.UsuarioService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping
    public String listarUsuarios(@RequestParam(value = "busca", required = false) String busca, Model model) {
        List<Usuario> usuarios;
        if (busca == null || busca.isEmpty()) {
            usuarios = usuarioService.buscarTodos();
        } else {
            usuarios = usuarioService.buscarPorNomeOuEmail(busca);
        }
        model.addAttribute("usuarios", usuarios);
        return "usuarios/listar"; // ou "usuarios/lista" — o que você usar
    }

    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        adicionarAtributosComuns(model);
        return "usuarios/cadastro";
    }

    @GetMapping("/index")
    public String mostrarEstatisticasUsuarios(Model model) {
        model.addAttribute("usuario", new Usuario());

        EstatisticasUsuariosDTO stats = usuarioService.buscarEstatisticas();
        model.addAttribute("totalUsuarios", stats.getTotalUsuarios());
        model.addAttribute("ativos", stats.getTotalAtivos());
        model.addAttribute("administradores", stats.getTotalAdministradores());
        model.addAttribute("bloqueados", stats.getTotalBloqueados());

        return "usuarios/index";
    }

    @GetMapping("/listar")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.buscarTodos());
        return "usuarios/listar";
    }

    @PostMapping("/cadastrar")
    public String cadastrarUsuario(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult,
            @RequestParam("confirmSenha") String confirmSenha,
            @RequestParam("perfilId") Long perfilId,
            Model model) {

        if (!usuario.getSenha().equals(confirmSenha)) {
            bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
        }

        if (bindingResult.hasErrors()) {
            adicionarAtributosComuns(model);
            model.addAttribute("usuario", usuario);
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
            model.addAttribute("usuario", usuario);
            return "usuarios/cadastro";
        }

        return "redirect:/usuarios/listar";
    }

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

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/usuarios/listar";
        }
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", usuarioOpt.get());
        }
        adicionarAtributosComuns(model);
        return "usuarios/editar";
    }

    @PostMapping("/{id}/editar")
    public String salvarEdicaoUsuario(
            @PathVariable Long id,
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult,
            @RequestParam("confirmSenha") String confirmSenha,
            @RequestParam("perfilId") Long perfilId,
            Model model) {

        if (!usuario.getSenha().equals(confirmSenha)) {
            bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
        }

        if (bindingResult.hasErrors()) {
            adicionarAtributosComuns(model);
            return "usuarios/editar";
        }

        try {
            if (usuario.getDataDesligamento() != null) {
                usuarioService.excluirUsuario(id);
                return "redirect:/usuarios/listar";
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

        return "redirect:/usuarios/listar";
    }

    @GetMapping("/editar")
    public String mostrarFormularioBuscaPorCpf() {
        return "usuarios/editar";  // formulário para digitar CPF
    }

    @PostMapping("/editar")
    public String buscarUsuarioPorCpf(@RequestParam("cpf") String cpf, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCpf(cpf);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("erro", "Usuário com CPF " + cpf + " não encontrado.");
            return "usuarios/editar";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        adicionarAtributosComuns(model);

        return "usuarios/editarcadastro"; // formulário completo para editar dados do usuário
    }

    @PostMapping("/{id}/excluir")
    public String excluirUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.excluirUsuario(id);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário excluído com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir usuário: " + e.getMessage());
        }
        return "redirect:/usuarios/listar";
    }

    private void adicionarAtributosComuns(Model model) {
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("generos", Genero.values());
        model.addAttribute("status", Usuario.Status.values());
        model.addAttribute("niveis", NivelAcesso.values());
        model.addAttribute("cargos", cargoRepository.findAll());
        model.addAttribute("departamentos", departamentoRepository.findAll());
    }

    // Endpoint AJAX para validar matrícula
    @GetMapping("/validar-matricula")
    @ResponseBody
    public Map<String, Boolean> validar(@RequestParam String matricula) {
        boolean autorizado = usuarioService.usuarioTemPermissaoParaExcluir(matricula);
        return Map.of("autorizado", autorizado);
    }

    // Endpoint pra verificar detalhes do usuario
    @GetMapping("/{id}/detalhes")
    public String exibirDetalhesUsuario(@PathVariable Long id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/usuarios/listar"; // redireciona se não encontrar
        }

        model.addAttribute("usuario", usuarioOpt.get());
        return "usuarios/detalhes"; // nome do arquivo HTML na pasta templates/usuarios
    }

    // ENDPOINT PARA GERAR PDF 
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

            Paragraph dadosPessoais = new Paragraph("Dados Pessoais", secaoFont);
            dadosPessoais.setSpacingAfter(8f);
            document.add(dadosPessoais);

            document.add(new Paragraph("Nome: " + usuario.getNome(), textoFont));
            document.add(new Paragraph("Email: " + usuario.getEmail(), textoFont));
            document.add(new Paragraph("Telefone: " + usuario.getTelefone(), textoFont));
            document.add(new Paragraph("Data de Nascimento: " + (usuario.getDataNascimento() != null ? usuario.getDataNascimento().toString() : "N/A"), textoFont));
            document.add(new Paragraph("Gênero: " + (usuario.getGenero() != null ? usuario.getGenero().name() : "N/A"), textoFont));
            document.add(new Paragraph("CPF: " + usuario.getCpf(), textoFont));
            document.add(Chunk.NEWLINE);

            Paragraph endereco = new Paragraph("Endereço", secaoFont);
            endereco.setSpacingAfter(8f);
            document.add(endereco);

            String enderecoCompleto = String.format("%s, %s - %s (%s)",
                    usuario.getEndereco(),
                    usuario.getCidade(),
                    usuario.getEstado(),
                    usuario.getCep());
            document.add(new Paragraph(enderecoCompleto, textoFont));
            document.add(Chunk.NEWLINE);

            Paragraph infoProfissional = new Paragraph("Informações Profissionais", secaoFont);
            infoProfissional.setSpacingAfter(8f);
            document.add(infoProfissional);

            document.add(new Paragraph("Departamento: " + (usuario.getDepartamento() != null ? usuario.getDepartamento().getNome() : "N/A"), textoFont));
            document.add(new Paragraph("Cargo: " + (usuario.getCargo() != null ? usuario.getCargo().getNome() : "N/A"), textoFont));
            document.add(new Paragraph("Nível de Acesso: " + (usuario.getNivelAcesso() != null ? usuario.getNivelAcesso().name() : "N/A"), textoFont));
            document.add(new Paragraph("Status: " + usuario.getStatus(), textoFont));
            document.add(new Paragraph("Data de Admissão: " + (usuario.getDataAdmissao() != null ? usuario.getDataAdmissao().toString() : "N/A"), textoFont));
            if (usuario.getDataDesligamento() != null) {
                document.add(new Paragraph("Data de Desligamento: " + usuario.getDataDesligamento().toString(), textoFont));
            }
            document.add(Chunk.NEWLINE);

            Paragraph perfis = new Paragraph("Perfis de Acesso", secaoFont);
            perfis.setSpacingAfter(8f);
            document.add(perfis);

            StringBuilder perfisStr = new StringBuilder();
            if (usuario.getPerfis() != null && !usuario.getPerfis().isEmpty()) {
                usuario.getPerfis().forEach(perfil -> perfisStr.append(perfil.getNome()).append(", "));
                if (perfisStr.length() > 2) {
                    perfisStr.setLength(perfisStr.length() - 2); // remove última vírgula
                }
            } else {
                perfisStr.append("N/A");
            }
            document.add(new Paragraph(perfisStr.toString(), textoFont));

            document.close();

        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF", e);
        }
    }

}
