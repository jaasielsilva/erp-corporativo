package com.jaasielsilva.portalceo.service.whatchat;

import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.service.ProcessoPrevidenciarioService;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.whatchat.ChatConversa;
import com.jaasielsilva.portalceo.model.whatchat.ChatConversaStatus;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import com.jaasielsilva.portalceo.repository.whatchat.ChatConversaRepository;
import com.jaasielsilva.portalceo.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhaTchatProcessoIntegrationService {

    private final WhaTchatConversaService conversaService;
    private final ChatConversaRepository conversaRepository;
    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final ProcessoPrevidenciarioService processoPrevidenciarioService;

    @Transactional
    public ProcessoPrevidenciario criarProcessoPrevidenciario(Long conversaId, Usuario usuarioExecutor) {
        ChatConversa conversa = conversaService.buscarPorId(conversaId);
        if (conversa.getProcessoPrevidenciario() != null) {
            return conversa.getProcessoPrevidenciario();
        }

        Cliente cliente = conversa.getCliente();
        if (cliente == null) {
            cliente = localizarClientePorTelefone(conversa.getWaId());
        }
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setNome(conversa.getNomeContato() != null ? conversa.getNomeContato() : "Cliente WhatsApp");
            cliente.setCelular(conversa.getWaId());
            cliente.setTipoCliente("PF");
            cliente.setStatus("Ativo");
            cliente.setAtivo(true);
            cliente = clienteService.salvar(cliente);
        }

        ProcessoPrevidenciario processo = processoPrevidenciarioService.criar(cliente.getId(), usuarioExecutor.getId(), usuarioExecutor);
        conversa.setCliente(cliente);
        conversa.setProcessoPrevidenciario(processo);
        conversa.setStatus(ChatConversaStatus.EM_ATENDIMENTO);
        conversa.setAtendente(usuarioExecutor);
        conversaRepository.save(conversa);
        return processo;
    }

    private Cliente localizarClientePorTelefone(String waId) {
        String digits = PhoneNormalizer.digitsOnly(waId);
        if (digits == null) {
            return null;
        }
        List<Cliente> clientes = clienteRepository.findAll();
        for (Cliente c : clientes) {
            String cel = PhoneNormalizer.digitsOnly(c.getCelular());
            String tel = PhoneNormalizer.digitsOnly(c.getTelefone());
            if (digits.equals(cel) || digits.equals(tel)) {
                return c;
            }
        }
        return null;
    }
}

