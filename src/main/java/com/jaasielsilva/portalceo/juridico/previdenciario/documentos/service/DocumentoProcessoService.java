package com.jaasielsilva.portalceo.juridico.previdenciario.documentos.service;

import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.entity.DocumentoProcesso;
import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.entity.TipoDocumentoProcesso;
import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.repository.DocumentoProcessoRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.historico.service.HistoricoProcessoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.repository.ProcessoPrevidenciarioRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.service.WorkflowService;
import com.jaasielsilva.portalceo.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DocumentoProcessoService {

    private final DocumentoProcessoRepository documentoRepository;
    private final ProcessoPrevidenciarioRepository processoRepository;
    private final WorkflowService workflowService;
    private final HistoricoProcessoService historicoProcessoService;

    @Value("${app.upload.path:uploads}")
    private String uploadBasePath;

    @Transactional(readOnly = true)
    public List<DocumentoProcesso> listarPorProcesso(Long processoId) {
        return documentoRepository.findByProcessoPrevidenciario_IdOrderByDataUploadDesc(processoId);
    }

    @Transactional
    public DocumentoProcesso anexarDocumento(Long processoId,
            MultipartFile arquivo,
            TipoDocumentoProcesso tipoDocumento,
            Usuario usuarioExecutor) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo é obrigatório");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("Tipo de documento é obrigatório");
        }

        ProcessoPrevidenciario processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        EtapaWorkflowCodigo etapaAtual = processo.getEtapaAtual();
        if (!workflowService.permiteAnexo(etapaAtual)) {
            throw new IllegalStateException("A etapa atual não permite anexos");
        }

        Long clienteId = processo.getCliente() != null ? processo.getCliente().getId() : null;
        if (clienteId == null) {
            throw new IllegalStateException("Processo sem cliente associado");
        }
        Path dir = Paths.get(uploadBasePath, "processos", "cliente_" + clienteId);
        Files.createDirectories(dir);

        String original = arquivo.getOriginalFilename() == null ? "arquivo" : arquivo.getOriginalFilename();
        String sanitized = original.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
        String nomeUnico = UUID.randomUUID() + "_" + sanitized;
        Path destino = dir.resolve(nomeUnico);
        Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        DocumentoProcesso doc = new DocumentoProcesso();
        doc.setProcessoPrevidenciario(processo);
        doc.setTipoDocumento(tipoDocumento);
        doc.setCaminhoArquivo(destino.toString());
        doc.setNomeOriginal(original);
        doc.setNomeArquivo(nomeUnico);
        doc.setContentType(arquivo.getContentType());
        doc.setTamanhoBytes(arquivo.getSize());
        doc.setEnviadoPor(usuarioExecutor);
        doc.setDataUpload(LocalDateTime.now());

        DocumentoProcesso salvo = documentoRepository.save(doc);
        historicoProcessoService.registrar(processo, "UPLOAD_DOCUMENTO", usuarioExecutor,
                tipoDocumento + " na etapa " + etapaAtual);
        return salvo;
    }

    @Transactional
    public DocumentoProcesso anexarDocumentoBytes(Long processoId,
            byte[] bytes,
            String originalFilename,
            TipoDocumentoProcesso tipoDocumento,
            Usuario usuarioExecutor) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Arquivo é obrigatório");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("Tipo de documento é obrigatório");
        }
        if (usuarioExecutor == null) {
            throw new IllegalArgumentException("Usuário executor é obrigatório");
        }

        ProcessoPrevidenciario processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        EtapaWorkflowCodigo etapaAtual = processo.getEtapaAtual();
        if (!workflowService.permiteAnexo(etapaAtual)) {
            throw new IllegalStateException("A etapa atual não permite anexos");
        }

        Long clienteId = processo.getCliente() != null ? processo.getCliente().getId() : null;
        if (clienteId == null) {
            throw new IllegalStateException("Processo sem cliente associado");
        }
        Path dir = Paths.get(uploadBasePath, "processos", "cliente_" + clienteId);
        Files.createDirectories(dir);

        String original = (originalFilename == null || originalFilename.isBlank()) ? "arquivo" : originalFilename;
        String sanitized = original.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
        String nomeUnico = UUID.randomUUID() + "_" + sanitized;
        Path destino = dir.resolve(nomeUnico);
        Files.write(destino, bytes);

        DocumentoProcesso doc = new DocumentoProcesso();
        doc.setProcessoPrevidenciario(processo);
        doc.setTipoDocumento(tipoDocumento);
        doc.setCaminhoArquivo(destino.toString());
        doc.setNomeOriginal(original);
        doc.setNomeArquivo(nomeUnico);
        doc.setContentType("application/octet-stream");
        doc.setTamanhoBytes((long) bytes.length);
        doc.setEnviadoPor(usuarioExecutor);
        doc.setDataUpload(LocalDateTime.now());

        DocumentoProcesso salvo = documentoRepository.save(doc);
        historicoProcessoService.registrar(processo, "UPLOAD_DOCUMENTO", usuarioExecutor,
                tipoDocumento + " na etapa " + etapaAtual);
        return salvo;
    }

    @Transactional(readOnly = true)
    public byte[] gerarZipDocumentos(Long processoId) throws IOException {
        List<DocumentoProcesso> documentos = listarPorProcesso(processoId);
        if (documentos == null || documentos.isEmpty()) {
            throw new IllegalStateException("Nenhum documento anexado para este processo");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (DocumentoProcesso doc : documentos) {
                if (doc == null || doc.getCaminhoArquivo() == null || doc.getCaminhoArquivo().isBlank()) {
                    continue;
                }

                Path arquivoPath = Paths.get(doc.getCaminhoArquivo());
                if (!Files.exists(arquivoPath) || Files.isDirectory(arquivoPath)) {
                    continue;
                }

                String fileName = arquivoPath.getFileName() != null ? arquivoPath.getFileName().toString() : "arquivo";
                String entryName = (doc.getTipoDocumento() != null ? doc.getTipoDocumento().name() : "DOCUMENTO")
                        + "/" + fileName;
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(Files.readAllBytes(arquivoPath));
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        }
    }
}
