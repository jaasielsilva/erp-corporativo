package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private UsuarioService usuarioService;

    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Cliente salvar(Cliente cliente) {
        return repository.save(cliente);
    }

    public boolean excluir(Long id) {
        Optional<Cliente> cliente = repository.findById(id);
        if (cliente.isPresent()) {
            repository.delete(cliente.get());
            return true;
        }
        return false;
    }

    public long contarTotal() {
        return repository.count();
    }

    public long contarAtivos() {
        return repository.countByStatusIgnoreCase("Ativo");
    }

    public long contarInativos() {
        return repository.countByStatusIgnoreCase("Inativo");
    }

    public long contarPessoasJuridicas() {
        return repository.countByTipoClienteIgnoreCase("PJ");
    }

    public long contarClientesPF() {
        return repository.countByTipoClienteIgnoreCase("PF");
    }

    public long contarPendentes() {
        return repository.countPendentes();
    }

    public long contarInativosMais90Dias() {
        LocalDate dataLimite = LocalDate.now().minusDays(90);
        return repository.countInativosMais90Dias(dataLimite);
    }

    public long contarAtivosUltimos30Dias() {
        LocalDate dataLimite = LocalDate.now().minusDays(30);
        return repository.countAtivosUltimos30Dias(dataLimite);
    }

    public long contarFidelizados() {
        return repository.countFidelizados();
    }

    public List<Cliente> buscarPorNomeOuEmail(String busca) {
        return repository.findByNomeOrEmailContainingIgnoreCase(busca);
    }

    public List<Cliente> buscarAtivos(String busca) {
        if (busca == null || busca.isEmpty()) {
            return repository.findByStatusIgnoreCase("Ativo");
        }
        return repository.findByStatusIgnoreCaseAndNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
            "Ativo", busca, busca);
    }

    public List<Cliente> buscarTodos() {
        return repository.findAll();
    }
    
    public boolean validarPermissaoExcluir(String matriculaAdmin) {
    Optional<Usuario> usuarioOpt = usuarioService.buscarPorMatricula(matriculaAdmin);
    if (usuarioOpt.isEmpty()) {
        return false;
    }
    Usuario usuario = usuarioOpt.get();
    // Exemplo: só admins podem excluir
    return usuario.getNivelAcesso() == NivelAcesso.ADMIN; // ou outro critério
}
    public List<Cliente> listarClientesAtivos() {
    return repository.findByAtivoTrue();
}

public boolean excluirLogicamente(Long clienteId, String matriculaAdmin) {
    Optional<Cliente> clienteOpt = repository.findById(clienteId);
    if (clienteOpt.isEmpty()) {
        return false;
    }
    Cliente cliente = clienteOpt.get();

    Optional<Usuario> usuarioOpt = usuarioService.buscarPorMatricula(matriculaAdmin);
    if (usuarioOpt.isEmpty()) {
        return false;
    }
    Usuario usuario = usuarioOpt.get();

    if (usuario.getNivelAcesso() != NivelAcesso.ADMIN) {
        return false;
    }

    // Exclusão lógica
    cliente.setAtivo(false);                    // marca cliente como inativo
    cliente.setStatus("Inativo");               // atualiza status, opcional mas recomendável
    cliente.setDataExclusao(LocalDateTime.now());
    cliente.setUsuarioExclusao(usuario);

    repository.save(cliente);
    return true;
    }
}