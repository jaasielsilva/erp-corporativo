package com.jaasielsilva.portalceo.controller;

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
    public String listarClientes(@RequestParam(value = "busca", required = false) String busca, Model model) {
    List<Cliente> clientes;

    if (busca == null || busca.trim().isEmpty()) {
        // Busca todos os clientes
        clientes = clienteService.buscarTodos(); 
    } else {
        // Busca clientes pelo nome ou email (ignora case)
        clientes = clienteService.buscarPorNomeOuEmail(busca.trim());
    }

    // Adiciona a lista de clientes no model para o Thymeleaf
    model.addAttribute("clientes", clientes);

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

    return "clientes/lista";
}

    @GetMapping("/cadastro")
    public String novoCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/cadastro";
    }

    // metodo pra editar o cliente 
    @GetMapping("/{id}/detalhes")
    public String verDetalhesCliente(@PathVariable Long id, Model model) {
    Optional<Cliente> clienteOpt = clienteService.buscarPorId(id);
    
    if (clienteOpt.isEmpty()) {
        // Cliente não encontrado, redireciona para lista com erro
        return "redirect:/clientes?erro=Cliente não encontrado";
    }

    Cliente cliente = clienteOpt.get();
    model.addAttribute("cliente", cliente);
    return "clientes/detalhes"; 
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
        return "clientes/editar";
    }

    @PostMapping("/{id}/editar")
public String atualizarCliente(@PathVariable Long id, @ModelAttribute Cliente clienteAtualizado, Principal principal) {
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
        @RequestHeader("X-Matricula") String matriculaInformada
    ) {
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
