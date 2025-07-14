package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.service.ClienteService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

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
        List<Cliente> clientes = service.buscarAtivos(busca);

        model.addAttribute("clientes", clientes);
        model.addAttribute("totalClientes", service.contarTotal());
        model.addAttribute("ativos", service.contarAtivos());
        model.addAttribute("inativos", service.contarInativos());
        model.addAttribute("pessoasJuridicas", service.contarPessoasJuridicas());
        model.addAttribute("ativosUltimos30", service.contarAtivosUltimos30Dias());
        model.addAttribute("clientesPF", service.contarClientesPF());
        model.addAttribute("clientesPJ", service.contarPessoasJuridicas()); // Usa contarPessoasJuridicas pois contaPJ e redundante
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
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/cadastro";
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
        service.salvar(cliente);
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
        return "clientes/form";
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
}
