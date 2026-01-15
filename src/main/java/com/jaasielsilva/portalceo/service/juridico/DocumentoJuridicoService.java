package com.jaasielsilva.portalceo.service.juridico;

import com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico;
import com.jaasielsilva.portalceo.repository.juridico.DocumentoJuridicoRepository;
import com.jaasielsilva.portalceo.service.AutentiqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DocumentoJuridicoService {

    private final DocumentoJuridicoRepository documentoJuridicoRepository;
    private final AutentiqueService autentiqueService;

    public DocumentoJuridicoService(DocumentoJuridicoRepository documentoJuridicoRepository,
            AutentiqueService autentiqueService) {
        this.documentoJuridicoRepository = documentoJuridicoRepository;
        this.autentiqueService = autentiqueService;
    }

    @Transactional
    public void sincronizarAssinaturasPendentes() {
        // Encontrar todos os documentos com status pendente que possuem ID da
        // Autentique
        List<DocumentoJuridico> documentos = documentoJuridicoRepository.findAll(); // Idealmente filtrar no banco

        for (DocumentoJuridico d : documentos) {
            if (d.getAutentiqueId() != null && !d.getAutentiqueId().isBlank()
                    && !"ASSINADO".equals(d.getStatusAssinatura())) {
                sincronizarDocumento(d);
            }
        }
    }

    @Transactional
    public void sincronizarDocumento(DocumentoJuridico d) {
        try {
            Map<String, Object> resumo = autentiqueService.obterResumoAssinaturas(d.getAutentiqueId());
            boolean assinado = Boolean.TRUE.equals(resumo.get("todosAssinaram"));
            @SuppressWarnings("unchecked")
            List<String> pendentes = (List<String>) resumo.getOrDefault("pendentes", java.util.Collections.emptyList());

            if (assinado) {
                d.setStatusAssinatura("ASSINADO");
                d.setDetalheStatusAssinatura(null);

                // Tenta baixar e atualizar o conteúdo do arquivo com a versão assinada
                try {
                    byte[] conteudoAssinado = autentiqueService.baixarDocumentoAssinado(d.getAutentiqueId());
                    if (conteudoAssinado != null && conteudoAssinado.length > 0) {
                        d.setConteudo(conteudoAssinado);
                        d.setTamanho((long) conteudoAssinado.length);

                        // Adiciona indicador ao nome do arquivo se não houver
                        if (d.getOriginalFilename() != null && !d.getOriginalFilename().contains("(Assinado)")) {
                            String name = d.getOriginalFilename();
                            String ext = "";
                            int dot = name.lastIndexOf(".");
                            if (dot > 0) {
                                ext = name.substring(dot);
                                name = name.substring(0, dot);
                            }
                            d.setOriginalFilename(name + " (Assinado)" + ext);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Erro ao baixar documento assinado: " + ex.getMessage());
                }
            } else {
                d.setStatusAssinatura("PENDENTE");
                if (pendentes != null && !pendentes.isEmpty()) {
                    String detalhe = "Falta assinatura de: " + String.join(", ", pendentes);
                    d.setDetalheStatusAssinatura(detalhe);
                } else {
                    d.setDetalheStatusAssinatura(null);
                }
            }
            documentoJuridicoRepository.save(d);
        } catch (Exception e) {
            System.err.println("Erro ao sincronizar documento " + d.getId() + ": " + e.getMessage());
        }
    }
}
