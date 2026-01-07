package com.jaasielsilva.portalceo.juridico.previdenciario.documentos.controller;

import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.entity.TipoDocumentoProcesso;
import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.service.DocumentoProcessoService;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/juridico/previdenciario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('MENU_JURIDICO_PREVIDENCIARIO', 'ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL','ROLE_JURIDICO','ROLE_ESTAGIARIO_JURIDICO')")
public class DocumentoProcessoController {

    private final DocumentoProcessoService documentoProcessoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/{id}/anexar-documento")
    public String anexarDocumento(@PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipoDocumento") TipoDocumentoProcesso tipoDocumento,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario executor = usuarioLogado(authentication);
            documentoProcessoService.anexarDocumento(id, arquivo, tipoDocumento, executor);
            redirectAttributes.addFlashAttribute("sucesso", "Documento anexado com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/juridico/previdenciario/" + id;
    }

    @GetMapping("/{id}/documentos.zip")
    public ResponseEntity<Resource> baixarDocumentosZip(@PathVariable Long id) throws IOException {
        byte[] zipBytes = documentoProcessoService.gerarZipDocumentos(id);
        ByteArrayResource resource = new ByteArrayResource(zipBytes);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"processo-" + id + "-documentos.zip\"")
                .contentLength(zipBytes.length)
                .body(resource);
    }

    private Usuario usuarioLogado(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        String username = authentication.getName();
        return usuarioRepository.findByEmail(username)
                .or(() -> usuarioRepository.findByMatricula(username))
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }
}
