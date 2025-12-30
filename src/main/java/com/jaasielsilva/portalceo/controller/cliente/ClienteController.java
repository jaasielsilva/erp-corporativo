package com.jaasielsilva.portalceo.controller.cliente;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.service.ClienteService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.service.UsuarioService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    @Autowired
    public ClienteController(ClienteService clienteService, UsuarioService usuarioService) {
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarClientes(@RequestParam(value = "busca", required = false) String busca,
                                 @RequestParam(value = "status", required = false) String status,
                                 @RequestParam(value = "origem", required = false) String origem,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        var clientesPage = clienteService.listarPaginado(busca, status, origem, page, size);
        model.addAttribute("clientes", clientesPage.getContent());
        model.addAttribute("currentPage", clientesPage.getNumber());
        model.addAttribute("totalPages", clientesPage.getTotalPages());
        model.addAttribute("totalElements", clientesPage.getTotalElements());
        model.addAttribute("hasPrevious", clientesPage.hasPrevious());
        model.addAttribute("hasNext", clientesPage.hasNext());
        model.addAttribute("busca", busca);
        model.addAttribute("statusFiltro", status);
        model.addAttribute("origemFiltro", origem);

        // Estatísticas para exibir na tela
        model.addAttribute("totalClientes", clienteService.contarTotal());
        model.addAttribute("ativos", clienteService.contarAtivos());
        model.addAttribute("inativos", clienteService.contarInativos());
        model.addAttribute("pessoasJuridicas", clienteService.contarPessoasJuridicas());
        model.addAttribute("ativosUltimos30", clienteService.contarAtivosUltimos30Dias());
        model.addAttribute("clientesPF", clienteService.contarClientesPF());
        model.addAttribute("clientesPJ", clienteService.contarPessoasJuridicas());
        model.addAttribute("clientesPendentes", clienteService.contarPendentes());
        model.addAttribute("clientesInativos90", clienteService.contarInativosMais90Dias());
        model.addAttribute("clientesFidelizados", clienteService.contarFidelizados());

        return "clientes/geral/listar";
    }

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarAjax(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "origem", required = false) String origem) {
        try {
            var pagina = clienteService.listarPaginado(q, status, origem, Math.max(page, 0), Math.min(Math.max(size, 1), 100));

            java.util.List<java.util.Map<String, Object>> content = pagina.getContent().stream().map(c -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", c.getId());
                m.put("nome", c.getNome());
                m.put("tipoCliente", c.getTipoCliente());
                m.put("email", c.getEmail());
                m.put("telefone", c.getTelefone());
                m.put("status", c.getStatus());
                m.put("origem", c.getOrigem());
                return m;
            }).collect(java.util.stream.Collectors.toList());

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("content", content);
            resp.put("currentPage", pagina.getNumber());
            resp.put("totalPages", pagina.getTotalPages());
            resp.put("totalElements", pagina.getTotalElements());
            resp.put("hasPrevious", pagina.hasPrevious());
            resp.put("hasNext", pagina.hasNext());
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Cache-Control", "public, max-age=90");
            return new org.springframework.http.ResponseEntity<>(resp, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("erro", "Falha ao carregar clientes: " + e.getMessage()));
        }
    }

    @GetMapping("/cadastro")
    public String novoCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/geral/cadastro";
    }

    // Método para visualizar detalhes do cliente
    @GetMapping({ "/{id}/detalhes" })
    public String verDetalhesCliente(@PathVariable(required = false) Long id, Model model) {
        if (id == null) {
            // Caso ninguém tenha passado ID, redireciona para a lista
            return "redirect:/clientes";
        }

        Optional<Cliente> clienteOpt = clienteService.buscarPorId(id);

        if (clienteOpt.isEmpty()) {
            // Cliente não encontrado, redireciona para lista com mensagem de erro
            return "redirect:/clientes?erro=Cliente não encontrado";
        }

        Cliente cliente = clienteOpt.get();
        model.addAttribute("cliente", cliente);

        // Usa o template geral de detalhes
        return "clientes/geral/detalhes";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Cliente cliente) {
        clienteService.salvar(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        var cliente = clienteService.buscarPorId(id);
        model.addAttribute("cliente", cliente.orElse(new Cliente()));
        return "clientes/geral/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizarCliente(@PathVariable Long id, @ModelAttribute Cliente clienteAtualizado,
            Principal principal) {
        Cliente clienteExistente = clienteService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Copia os campos do objeto atualizado para o objeto existente
        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setNomeFantasia(clienteAtualizado.getNomeFantasia());
        clienteExistente.setTipoCliente(clienteAtualizado.getTipoCliente());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setTelefone(clienteAtualizado.getTelefone());
        clienteExistente.setCelular(clienteAtualizado.getCelular());
        clienteExistente.setCpfCnpj(clienteAtualizado.getCpfCnpj());
        clienteExistente.setInscricaoEstadual(clienteAtualizado.getInscricaoEstadual());
        clienteExistente.setInscricaoMunicipal(clienteAtualizado.getInscricaoMunicipal());
        clienteExistente.setLogradouro(clienteAtualizado.getLogradouro());
        clienteExistente.setNumero(clienteAtualizado.getNumero());
        clienteExistente.setComplemento(clienteAtualizado.getComplemento());
        clienteExistente.setBairro(clienteAtualizado.getBairro());
        clienteExistente.setCidade(clienteAtualizado.getCidade());
        clienteExistente.setEstado(clienteAtualizado.getEstado());
        clienteExistente.setCep(clienteAtualizado.getCep());
        clienteExistente.setStatus(clienteAtualizado.getStatus());
        clienteExistente.setPessoaContato(clienteAtualizado.getPessoaContato());
        clienteExistente.setObservacoes(clienteAtualizado.getObservacoes());

        Usuario usuarioAutenticado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));

        clienteExistente.setEditadoPor(usuarioAutenticado);
        clienteExistente.setDataUltimaEdicao(java.time.LocalDateTime.now());

        clienteService.salvar(clienteExistente);

        return "redirect:/clientes";
    }

    @PostMapping("/{id}/excluir")
    public ResponseEntity<?> excluirCliente(
            @PathVariable Long id,
            @RequestHeader("X-Matricula") String matriculaInformada) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailLogado = auth.getName();

        Optional<Usuario> usuarioLogadoOpt = usuarioService.buscarPorEmail(emailLogado);
        if (usuarioLogadoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuarioLogado = usuarioLogadoOpt.get();

        if (usuarioLogado.getNivelAcesso() != NivelAcesso.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas administradores podem excluir clientes."));
        }

        if (!usuarioLogado.getMatricula().equalsIgnoreCase(matriculaInformada)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Matrícula incorreta para este usuário."));
        }

        boolean excluido = clienteService.excluirLogicamente(id, usuarioLogado);
        if (!excluido) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Cliente não encontrado."));
        }

        return ResponseEntity.ok(Map.of("mensagem", "Cliente excluído logicamente com sucesso."));
    }

    @GetMapping("/filtro")
    @ResponseBody
    public List<Cliente> filtrarPorStatus(@RequestParam("status") String status) {
        return switch (status.toLowerCase()) {
            case "ativo" -> clienteService.buscarAtivos();
            case "inativo" -> clienteService.buscarInativos();
            case "pendente" -> clienteService.buscarPendentes();
            default -> clienteService.buscarTodos();
        };
    }
}
