package com.jaasielsilva.portalceo.service.whatchat;

public record WhatsAppMediaDownloadResult(
        byte[] bytes,
        String mimeType,
        String fileName,
        String sha256
) {}

