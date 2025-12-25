package com.jaasielsilva.portalceo.service.whatchat;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.whatchat.ChatConversa;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagem;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemDirecao;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import com.jaasielsilva.portalceo.repository.whatchat.ChatConversaRepository;
import com.jaasielsilva.portalceo.repository.whatchat.ChatMensagemRepository;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhaTchatAutomationService {

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final ChatConversaRepository conversaRepository;
    private final ChatMensagemRepository mensagemRepository;
    private final WhaTchatMensagemService mensagemService;
    private final UsuarioService usuarioService;
    private final WhaTchatProcessoIntegrationService processoIntegrationService;

    private static final String TEMPLATE_BOAS_VINDAS = """
            {nome}, bem-vindo(a) ao atendimento {brand}.

            Para agilizar, responda com:
            1) Nome completo
            2) CPF
            3) Qual assunto/processo deseja tratar
            """;

    private static final String TEMPLATE_PROCESSO_CRIADO = """
            Perfeito. Já iniciei o seu processo no {brand}.
            ID interno: {processoId}

            Se tiver documentos (RG, CPF, CNIS, cartas do INSS), pode enviar por aqui.
            """;

    @Value("${erp.whatchat.auto-link-client:true}")
    private boolean autoLinkClient;

    @Value("${erp.whatchat.auto-create-client:false}")
    private boolean autoCreateClient;

    @Value("${erp.whatchat.auto-reply.enabled:false}")
    private boolean autoReplyEnabled;

    @Value("${erp.whatchat.auto-reply.once:true}")
    private boolean autoReplyOnce;

    @Value("${erp.whatchat.brand-name:Portal CEO}")
    private String brandName;

    @Value("${erp.whatchat.default-country-code:55}")
    private String defaultCountryCode;

    @Value("${erp.whatchat.auto-create-process.enabled:false}")
    private boolean autoCreateProcessEnabled;

    @Value("${erp.whatchat.system-user-email:}")
    private String systemUserEmail;

    @Transactional
    public void onMensagemRecebida(ChatConversa conversa, ChatMensagem mensagem, String texto) {
        if (conversa == null || mensagem == null) {
            return;
        }

        if (autoLinkClient) {
            vincularClienteSePossivel(conversa);
        }

        if (autoReplyEnabled) {
            enviarAutoRespostaSeAplicavel(conversa);
        }

        if (autoCreateProcessEnabled) {
            criarProcessoSeAplicavel(conversa, texto);
        }
    }

    private void vincularClienteSePossivel(ChatConversa conversa) {
        if (conversa.getCliente() != null) {
            return;
        }
        Cliente cliente = localizarClientePorTelefone(conversa.getWaId());
        if (cliente == null && autoCreateClient) {
            Cliente novo = new Cliente();
            novo.setNome(conversa.getNomeContato() != null && !conversa.getNomeContato().isBlank()
                    ? conversa.getNomeContato().trim()
                    : "Cliente WhatsApp");
            novo.setCelular(PhoneNormalizer.toE164(conversa.getWaId(), defaultCountryCode));
            novo.setTipoCliente("PF");
            novo.setStatus("Ativo");
            novo.setAtivo(true);
            cliente = clienteService.salvar(novo);
        }
        if (cliente != null) {
            conversa.setCliente(cliente);
            conversaRepository.save(conversa);
        }
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

    private void enviarAutoRespostaSeAplicavel(ChatConversa conversa) {
        if (conversa.getAtendente() != null) {
            return;
        }
        if (autoReplyOnce
                && mensagemRepository.existsByConversa_IdAndDirecao(conversa.getId(), ChatMensagemDirecao.ENVIADA)) {
            return;
        }
        mensagemService.enviarTextoSistema(conversa.getId(), renderTemplate(TEMPLATE_BOAS_VINDAS, conversa, null));
    }

    private void criarProcessoSeAplicavel(ChatConversa conversa, String texto) {
        if (conversa.getProcessoPrevidenciario() != null) {
            return;
        }
        if (systemUserEmail == null || systemUserEmail.isBlank()) {
            return;
        }
        if (texto == null) {
            return;
        }
        String t = texto.trim().toLowerCase();
        if (!(t.startsWith("/processo") || t.startsWith("/novo-processo") || t.contains("novo processo")
                || t.contains("processo previdenci"))) {
            return;
        }
        Usuario usuario = usuarioService.buscarPorEmail(systemUserEmail).orElse(null);
        if (usuario == null) {
            return;
        }
        var processo = processoIntegrationService.criarProcessoPrevidenciario(conversa.getId(), usuario);
        mensagemService.enviarTextoSistema(conversa.getId(),
                renderTemplate(TEMPLATE_PROCESSO_CRIADO, conversa, processo.getId()));
    }

    private String renderTemplate(String template, ChatConversa conversa, Long processoId) {
        String nome = conversa != null && conversa.getNomeContato() != null && !conversa.getNomeContato().isBlank()
                ? conversa.getNomeContato().trim()
                : "Olá";
        String brand = brandName != null ? brandName : "";
        String pid = processoId != null ? String.valueOf(processoId) : "";
        return template
                .replace("{nome}", nome)
                .replace("{brand}", brand)
                .replace("{processoId}", pid);
    }
}
