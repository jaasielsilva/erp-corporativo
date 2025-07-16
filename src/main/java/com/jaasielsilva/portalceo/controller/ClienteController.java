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

    private final ClienteService service;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioService usuarioService;

    // Injeção via construtor, prática recomendada para melhor testabilidade e imutabilidade
    public ClienteController(ClienteService service) {
        this.service = service;
    }

    /**
     * Endpoint para listar clientes ativos.
     * Aceita parâmetro de busca opcional para filtrar clientes por nome ou email.
     * Popula no modelo dados estatísticos para dashboard.
     * 
     * @param busca termo opcional para busca de clientes
     * @param model modelo para passar atributos para a view Thymeleaf
     * @return nome do template da lista de clientes
     */
    @GetMapping
    public String listarClientes(@RequestParam(value = "busca", required = false) String busca, Model model) {
    List<Cliente> clientes;

    if (busca == null || busca.trim().isEmpty()) {
        // Busca todos os clientes, independente do status
        clientes = service.buscarTodos();
    } else {
        // Busca clientes (ativos e inativos) cujo nome ou email contenha o termo
        clientes = service.buscarPorNomeOuEmail(busca.trim());
    }

    model.addAttribute("clientes", clientes);
    model.addAttribute("totalClientes", service.contarTotal());
    model.addAttribute("ativos", service.contarAtivos());
    model.addAttribute("inativos", service.contarInativos());
    model.addAttribute("pessoasJuridicas", service.contarPessoasJuridicas());
    model.addAttribute("ativosUltimos30", service.contarAtivosUltimos30Dias());
    model.addAttribute("clientesPF", service.contarClientesPF());
    model.addAttribute("clientesPJ", service.contarPessoasJuridicas());
    model.addAttribute("clientesPendentes", service.contarPendentes());
    model.addAttribute("clientesInativos90", service.contarInativosMais90Dias());
    model.addAttribute("clientesFidelizados", service.contarFidelizados());

    return "clientes/lista";
}


    /**
     * Exibe o formulário para cadastro de um novo cliente.
     * Adiciona um objeto Cliente vazio ao modelo para ser preenchido pelo formulário.
     * 
     * @param model modelo para passar atributos para a view
     * @return nome do template do formulário de cadastro
     */
    @GetMapping("/cadastro")
    public String novoCliente(Model model) {
    List<Cliente> clientes = service.buscarTodos(); // TODOS clientes
    model.addAttribute("cliente", new Cliente()); // objeto vazio para preencher o formulário
    return "clientes/cadastro"; // nome do template Thymeleaf
    }


    /**
     * Recebe dados do formulário para salvar ou atualizar um cliente.
     * Após salvar, redireciona para a lista de clientes.
     * 
     * @param cliente objeto Cliente preenchido pelo formulário
     * @return redirecionamento para a listagem de clientes
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Cliente cliente) {
    service.salvar(cliente); // salva novo ou atualiza existente
    return "redirect:/clientes";
    }


    /**
     * Exibe o formulário para edição de um cliente existente.
     * Caso o cliente não seja encontrado, cria um objeto Cliente vazio.
     * 
     * @param id identificador do cliente a ser editado
     * @param model modelo para passar atributos para a view
     * @return nome do template do formulário de edição
     */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
    var cliente = service.buscarPorId(id);
    model.addAttribute("cliente", cliente.orElse(new Cliente()));
    return "clientes/editar";
    }



    /**
     * Exibe os detalhes completos de um cliente específico.
     * Se o cliente não for encontrado, envia null para a view.
     * 
     * @param id identificador do cliente
     * @param model modelo para passar atributos para a view
     * @return nome do template de detalhes do cliente
     */
    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model) {
        var cliente = service.buscarPorId(id);
        model.addAttribute("cliente", cliente.orElse(null));
        return "clientes/detalhes";
    }

    /**
     * Exclui um cliente pelo seu ID.
     * Após exclusão, redireciona para a lista de clientes.
     * 
     * @param id identificador do cliente a excluir
     * @return redirecionamento para a listagem de clientes
     */
    @GetMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/clientes";
    }
    @PostMapping("/{id}/excluir")
    public ResponseEntity<?> excluirCliente(
        @PathVariable Long id,
        @RequestParam("matriculaAdmin") String matriculaInformada
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String emailLogado = auth.getName();

    Optional<Usuario> usuarioLogadoOpt = usuarioService.buscarPorEmail(emailLogado);
    if (usuarioLogadoOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("erro", "Usuário não autenticado."));
    }

    Usuario usuarioLogado = usuarioLogadoOpt.get();

    // Verifica se o usuário tem nível ADMIN
    if (usuarioLogado.getNivelAcesso() != NivelAcesso.ADMIN) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("erro", "Apenas administradores podem excluir clientes."));
    }

    // Verifica se a matrícula digitada corresponde ao usuário logado
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

@PostMapping("/{id}/editar")
public String atualizarCliente(@PathVariable Long id, @ModelAttribute Cliente clienteAtualizado, Principal principal) {
    Cliente clienteExistente = service.buscarPorId(id)
        .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

    // Atualiza os campos editáveis
    clienteExistente.setNome(clienteAtualizado.getNome());
    clienteExistente.setEmail(clienteAtualizado.getEmail());
    clienteExistente.setTelefone(clienteAtualizado.getTelefone());
    clienteExistente.setCelular(clienteAtualizado.getCelular());
    clienteExistente.setCpfCnpj(clienteAtualizado.getCpfCnpj());
    clienteExistente.setTipoCliente(clienteAtualizado.getTipoCliente());
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
    clienteExistente.setNomeFantasia(clienteAtualizado.getNomeFantasia());
    clienteExistente.setInscricaoMunicipal(clienteAtualizado.getInscricaoMunicipal());
    clienteExistente.setInscricaoEstadual(clienteAtualizado.getInscricaoEstadual());

    // Busca o usuário autenticado pelo email (username)
    Usuario usuarioAutenticado = usuarioService.buscarPorEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));

    // Setar auditoria no cliente
    clienteExistente.setEditadoPor(usuarioAutenticado);
    clienteExistente.setDataUltimaEdicao(java.time.LocalDateTime.now());

    // Salvar o cliente atualizado
    service.salvar(clienteExistente);

    return "redirect:/clientes";
}

}