package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    /**
     * Retorna todos os clientes cadastrados.
     */
    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    /**
     * Busca um cliente pelo seu ID.
     * @param id identificador do cliente
     * @return Optional com o cliente, ou vazio se não encontrado
     */
    public Optional<Cliente> buscarPorId(Long id) {
        return repository.findById(id);
    }

    /**
     * Salva ou atualiza um cliente no banco de dados.
     * @param cliente objeto cliente a ser salvo
     * @return cliente salvo
     */
    public Cliente salvar(Cliente cliente) {
        return repository.save(cliente);
    }

    /**
     * Exclui um cliente pelo ID.
     * @param id identificador do cliente a excluir
     */
    public void excluir(Long id) {
        repository.deleteById(id);
    }

    /**
     * Conta o total de clientes cadastrados.
     * @return quantidade total de clientes
     */
    public long contarTotal() {
        return repository.count();
    }

    /**
     * Conta quantos clientes possuem status "Ativo".
     * @return quantidade de clientes ativos
     */
    public long contarAtivos() {
        return repository.countByStatusIgnoreCase("Ativo");
    }

    /**
     * Conta quantos clientes possuem status "Inativo".
     * @return quantidade de clientes inativos
     */
    public long contarInativos() {
        return repository.countByStatusIgnoreCase("Inativo");
    }

    /**
     * Conta quantos clientes são Pessoas Jurídicas ("PJ").
     * @return quantidade de clientes PJ
     */
    public long contarPessoasJuridicas() {
        return repository.countByTipoClienteIgnoreCase("PJ");
    }

    /**
     * Conta quantos clientes são Pessoas Físicas ("PF").
     * @return quantidade de clientes PF
     */
    public long contarClientesPF() {
        return repository.countByTipoClienteIgnoreCase("PF");
    }

    /**
     * Conta quantos clientes estão com status "Pendente".
     * @return quantidade de clientes pendentes
     */
    public long contarPendentes() {
        return repository.countPendentes();
    }

    /**
     * Conta clientes inativos há mais de 90 dias.
     * @return quantidade de clientes inativos há mais de 90 dias
     */
    public long contarInativosMais90Dias() {
        LocalDate dataLimite = LocalDate.now().minusDays(90);
        return repository.countInativosMais90Dias(dataLimite);
    }

    /**
     * Conta clientes ativos cadastrados nos últimos 30 dias.
     * @return quantidade de clientes ativos nos últimos 30 dias
     */
    public long contarAtivosUltimos30Dias() {
        LocalDate dataLimite = LocalDate.now().minusDays(30);
        return repository.countAtivosUltimos30Dias(dataLimite);
    }

    /**
     * Conta clientes com status "Fidelizado".
     * @return quantidade de clientes fidelizados
     */
    public long contarFidelizados() {
        return repository.countFidelizados();
    }

    /**
     * Busca clientes pelo nome ou email contendo o termo informado (case insensitive).
     * @param busca termo para busca (nome ou email)
     * @return lista de clientes que correspondem ao termo
     */
    public List<Cliente> buscarPorNomeOuEmail(String busca) {
        return repository.findByNomeOrEmailContainingIgnoreCase(busca);
    }

    /**
     * Busca clientes ativos pelo nome ou email contendo o termo informado.
     * Se o termo for vazio, retorna todos os clientes ativos.
     * @param busca termo para busca (nome ou email)
     * @return lista de clientes ativos que correspondem ao termo
     */
    public List<Cliente> buscarAtivos(String busca) {
        if (busca == null || busca.isEmpty()) {
            return repository.findByStatusIgnoreCase("Ativo");
        }
        return repository.findByStatusIgnoreCaseAndNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
            "Ativo", busca, busca);
    }

    // Buscar todos os clientes, sem filtro de status
    public List<Cliente> buscarTodos() {
    return repository.findAll();
}

}
