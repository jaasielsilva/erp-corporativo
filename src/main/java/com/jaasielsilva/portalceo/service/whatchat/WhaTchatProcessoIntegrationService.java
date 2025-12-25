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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${erp.whatchat.default-country-code:55}")
    private String defaultCountryCode;

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
            cliente.setCelular(PhoneNormalizer.toE164(conversa.getWaId(), defaultCountryCode));
            cliente.setTipoCliente("PF");
            cliente.setStatus("Ativo");
            cliente.setAtivo(true);
            cliente = clienteService.salvar(cliente);
        }

        ProcessoPrevidenciario processo = processoPrevidenciarioService.criar(cliente.getId(), usuarioExecutor.getId(),
                usuarioExecutor);
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
            if (PhoneNormalizer.samePhone(digits, c.getCelular())
                    || PhoneNormalizer.samePhone(digits, c.getTelefone())) {
                return c;
            }
        }
        return null;
    }
}
