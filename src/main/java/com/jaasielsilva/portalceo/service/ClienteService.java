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

    public long contarNovosPorPeriodo(int dias) {
        LocalDate dataLimite = LocalDate.now().minusDays(dias);
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
    
    // metodo pra validar a permissão do usuario para excluir um cliente ou usuario
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

    public boolean excluirLogicamente(Long id, Usuario usuarioExclusao) {
    Optional<Cliente> clienteOpt = repository.findById(id);

    if (clienteOpt.isEmpty()) {
        return false;
    }

    Cliente cliente = clienteOpt.get();

    cliente.setAtivo(false); // marca como inativo
    cliente.setStatus("INATIVO");
    cliente.setDataExclusao(LocalDateTime.now());
    cliente.setUsuarioExclusao(usuarioExclusao);

    repository.save(cliente);
    return true;
    }


    public List<Cliente> buscarAtivos() {
    return repository.findByStatus("ATIVO");
    }

    public List<Cliente> buscarInativos() {
        return repository.findByStatus("INATIVO");
    }

    public List<Cliente> buscarPendentes() {
        return repository.findByStatus("PENDENTE");
    }

    public List<Cliente> listarAtivosPorTipo(String tipoCliente) {
    return repository.findByAtivoTrueAndTipoCliente(tipoCliente);
    }

    public Cliente findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    
    // Buscar cliente por nome exato
    public Optional<Cliente> buscarPorNomeExato(String nome) {
        return repository.findByNome(nome);
    }
    
    // Calcula performance de qualidade baseada na satisfação e retenção de clientes
    public int calcularPerformanceQualidade() {
        long totalClientes = contarTotal();
        long clientesAtivos = contarAtivos();
        long clientesFidelizados = contarFidelizados();
        
        if (totalClientes == 0) {
            return 80; // Valor padrão
        }
        
        // Calcula percentual de clientes ativos e fidelizados
        double percentualAtivos = ((double) clientesAtivos / totalClientes) * 100;
        double percentualFidelizados = totalClientes > 0 ? ((double) clientesFidelizados / totalClientes) * 100 : 0;
        
        // Média ponderada (60% ativos, 40% fidelizados)
        double performance = (percentualAtivos * 0.6) + (percentualFidelizados * 0.4);
        
        return (int) Math.min(100, Math.max(0, performance));
    }
    }