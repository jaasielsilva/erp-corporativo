package com.jaasielsilva.portalceo.service.whatchat;

import org.springframework.web.multipart.MultipartFile;

public interface WhatsAppProviderClient {
    String enviarTexto(String waId, String texto);
    String enviarImagem(String waId, MultipartFile arquivo);
    String enviarDocumento(String waId, MultipartFile arquivo);
    WhatsAppMediaDownloadResult baixarMidia(String mediaId);
}

