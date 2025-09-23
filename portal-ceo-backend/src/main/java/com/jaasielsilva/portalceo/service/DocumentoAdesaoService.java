package com.jaasielsilva.portalceo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gestão de documentos do processo de adesão de colaboradores.
 * Responsável por upload, validação, armazenamento e limpeza de arquivos.
 */
@Service
public class DocumentoAdesaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentoAdesaoService.class);
    
    @Autowired
    private WorkflowAdesaoService workflowAdesaoService;
    
    @Value("${app.upload.dir:uploads/adesao}")
    private String uploadDir;
    
    @Value("${app.upload.max-size:5242880}") // 5MB
    private long maxFileSize;
    
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
        "application/pdf",
        "image/jpeg",
        "image/jpg", 
        "image/png"
    );
    
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(
        ".pdf", ".jpg", ".jpeg", ".png"
    );
    
    private static final Map<String, String> DOCUMENTOS_OBRIGATORIOS = Map.of(
        "documentoRg", "RG",
        "documentoCpf", "CPF",
        "comprovanteEndereco", "Comprovante de Endereço",
        // Mapeamento alternativo para compatibilidade com frontend
        "rg", "RG",
        "cpf", "CPF",
        "endereco", "Comprovante de Endereço"
    );
    
    private static final Map<String, String> DOCUMENTOS_OPCIONAIS = Map.of(
        "carteiraTrabalho", "Carteira de Trabalho",
        "tituloEleitor", "Título de Eleitor",
        "certificadoReservista", "Certificado de Reservista",
        "comprovanteEscolaridade", "Comprovante de Escolaridade",
        "certidaoNascimentoCasamento", "Certidão de Nascimento/Casamento",
        "outrosDocumentos", "Outros Documentos"
    );
    
    // Mapeamento para normalizar tipos de documento
    private static final Map<String, String> MAPEAMENTO_TIPOS = Map.of(
        "rg", "documentoRg",
        "cpf", "documentoCpf",
        "endereco", "comprovanteEndereco"
    );
    
    /**
     * Processa o upload de um documento
     */
    public DocumentoInfo processarUpload(MultipartFile arquivo, String tipoDocumento, String sessionId) 
            throws IOException, DocumentoException {
        
        // Validações
        validarArquivo(arquivo);
        validarTipoDocumento(tipoDocumento);
        
        // Normalizar tipo de documento para compatibilidade
        String tipoNormalizado = normalizarTipoDocumento(tipoDocumento);
        
        // Criar diretório se não existir
        Path diretorioSessao = criarDiretorioSessao(sessionId);
        
        // Gerar nome único para o arquivo
        String nomeArquivo = gerarNomeArquivo(arquivo.getOriginalFilename(), tipoNormalizado);
        Path caminhoArquivo = diretorioSessao.resolve(nomeArquivo);
        
        try {
            // Salvar arquivo
            Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);
            
            // Criar info do documento
            DocumentoInfo documentoInfo = new DocumentoInfo();
            documentoInfo.setTipo(obterNomeTipoDocumento(tipoDocumento));
            documentoInfo.setTipoInterno(tipoNormalizado);
            documentoInfo.setNomeArquivo(arquivo.getOriginalFilename());
            documentoInfo.setNomeArquivoSalvo(nomeArquivo);
            documentoInfo.setCaminhoArquivo(caminhoArquivo.toString());
            documentoInfo.setTamanho(formatarTamanho(arquivo.getSize()));
            documentoInfo.setTamanhoBytes(arquivo.getSize());
            documentoInfo.setContentType(arquivo.getContentType());
            documentoInfo.setDataUpload(LocalDateTime.now());
            documentoInfo.setObrigatorio(DOCUMENTOS_OBRIGATORIOS.containsKey(tipoDocumento));
            
            logger.info("Documento {} salvo com sucesso para sessão {}: {}", 
                       tipoDocumento, sessionId, nomeArquivo);
            
            return documentoInfo;
            
        } catch (IOException e) {
            logger.error("Erro ao salvar documento {} para sessão {}: {}", 
                        tipoDocumento, sessionId, e.getMessage());
            throw new DocumentoException("Erro ao salvar documento: " + e.getMessage());
        }
    }
    
    /**
     * Lista todos os documentos de uma sessão
     */
    public List<DocumentoInfo> listarDocumentos(String sessionId) {
        Path diretorioSessao = Paths.get(uploadDir, sessionId);
        
        if (!Files.exists(diretorioSessao)) {
            return new ArrayList<>();
        }
        
        try {
            return Files.list(diretorioSessao)
                    .filter(Files::isRegularFile)
                    .map(this::criarDocumentoInfoDoArquivo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                    
        } catch (IOException e) {
            logger.error("Erro ao listar documentos da sessão {}: {}", sessionId, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Verifica se todos os documentos obrigatórios foram enviados
     */
    public boolean verificarDocumentosObrigatorios(String sessionId) {
        List<DocumentoInfo> documentos = listarDocumentos(sessionId);
        Set<String> tiposEnviados = documentos.stream()
                .map(DocumentoInfo::getTipoInterno)
                .collect(Collectors.toSet());
        
        // Verificar apenas os tipos normalizados obrigatórios
        Set<String> tiposObrigatoriosNormalizados = Set.of("documentoRg", "documentoCpf", "comprovanteEndereco");
        
        return tiposObrigatoriosNormalizados.stream()
                .allMatch(tiposEnviados::contains);
    }
    
    /**
     * Remove um documento específico
     */
    public boolean removerDocumento(String sessionId, String tipoDocumento) {
        Path diretorioSessao = Paths.get(uploadDir, sessionId);
        
        try {
            return Files.list(diretorioSessao)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(tipoDocumento + "_"))
                    .findFirst()
                    .map(path -> {
                        try {
                            Files.delete(path);
                            logger.info("Documento {} removido da sessão {}", tipoDocumento, sessionId);
                            return true;
                        } catch (IOException e) {
                            logger.error("Erro ao remover documento {} da sessão {}: {}", 
                                       tipoDocumento, sessionId, e.getMessage());
                            return false;
                        }
                    })
                    .orElse(false);
                    
        } catch (IOException e) {
            logger.error("Erro ao buscar documento para remoção: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Move documentos da sessão temporária para o diretório final do colaborador
     */
    public boolean moverDocumentosParaColaborador(String sessionId, Long colaboradorId) {
        Path diretorioSessao = Paths.get(uploadDir, sessionId);
        Path diretorioColaborador = Paths.get(uploadDir, "colaboradores", colaboradorId.toString());
        
        try {
            // Criar diretório do colaborador
            Files.createDirectories(diretorioColaborador);
            
            // Mover todos os arquivos
            Files.list(diretorioSessao)
                    .filter(Files::isRegularFile)
                    .forEach(arquivo -> {
                        try {
                            Path destino = diretorioColaborador.resolve(arquivo.getFileName());
                            Files.move(arquivo, destino, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            logger.error("Erro ao mover arquivo {}: {}", arquivo, e.getMessage());
                        }
                    });
            
            // Remover diretório da sessão
            limparSessao(sessionId);
            
            logger.info("Documentos movidos da sessão {} para colaborador {}", sessionId, colaboradorId);
            return true;
            
        } catch (IOException e) {
            logger.error("Erro ao mover documentos da sessão {} para colaborador {}: {}", 
                        sessionId, colaboradorId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Limpa todos os arquivos de uma sessão
     */
    public void limparSessao(String sessionId) {
        Path diretorioSessao = Paths.get(uploadDir, sessionId);
        
        if (Files.exists(diretorioSessao)) {
            try {
                Files.walk(diretorioSessao)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                        
                logger.info("Sessão {} limpa com sucesso", sessionId);
                
            } catch (IOException e) {
                logger.error("Erro ao limpar sessão {}: {}", sessionId, e.getMessage());
            }
        }
    }
    
    /**
     * Limpa sessões antigas (mais de 24 horas)
     */
    public void limparSessoesAntigas() {
        Path diretorioUpload = Paths.get(uploadDir);
        
        if (!Files.exists(diretorioUpload)) {
            return;
        }
        
        try {
            Files.list(diretorioUpload)
                    .filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().equals("colaboradores"))
                    .filter(this::isSessaoAntiga)
                    .forEach(path -> {
                        String sessionId = path.getFileName().toString();
                        limparSessao(sessionId);
                        logger.info("Sessão antiga {} removida", sessionId);
                    });
                    
        } catch (IOException e) {
            logger.error("Erro ao limpar sessões antigas: {}", e.getMessage());
        }
    }
    
    // Métodos privados
    
    private void validarArquivo(MultipartFile arquivo) throws DocumentoException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new DocumentoException("Arquivo não pode estar vazio");
        }
        
        if (arquivo.getSize() > maxFileSize) {
            throw new DocumentoException("Arquivo muito grande. Tamanho máximo: " + 
                                       formatarTamanho(maxFileSize));
        }
        
        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new DocumentoException("Tipo de arquivo não permitido. Use PDF, JPG, JPEG ou PNG");
        }
        
        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            throw new DocumentoException("Nome do arquivo inválido");
        }
        
        String extensao = obterExtensao(nomeArquivo).toLowerCase();
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new DocumentoException("Extensão de arquivo não permitida");
        }
    }
    
    private void validarTipoDocumento(String tipoDocumento) throws DocumentoException {
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            throw new DocumentoException("Tipo de documento não informado");
        }
        
        if (!DOCUMENTOS_OBRIGATORIOS.containsKey(tipoDocumento) && 
            !DOCUMENTOS_OPCIONAIS.containsKey(tipoDocumento)) {
            throw new DocumentoException("Tipo de documento inválido: " + tipoDocumento);
        }
    }
    
    private String normalizarTipoDocumento(String tipoDocumento) {
        return MAPEAMENTO_TIPOS.getOrDefault(tipoDocumento, tipoDocumento);
    }
    
    private Path criarDiretorioSessao(String sessionId) throws IOException {
        Path diretorioSessao = Paths.get(uploadDir, sessionId);
        Files.createDirectories(diretorioSessao);
        return diretorioSessao;
    }
    
    private String gerarNomeArquivo(String nomeOriginal, String tipoDocumento) {
        String extensao = obterExtensao(nomeOriginal);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return tipoDocumento + "_" + timestamp + extensao;
    }
    
    private String obterExtensao(String nomeArquivo) {
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        return ultimoPonto > 0 ? nomeArquivo.substring(ultimoPonto) : "";
    }
    
    private String obterNomeTipoDocumento(String tipoInterno) {
        return DOCUMENTOS_OBRIGATORIOS.getOrDefault(tipoInterno, 
               DOCUMENTOS_OPCIONAIS.getOrDefault(tipoInterno, tipoInterno));
    }
    
    private String formatarTamanho(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    private DocumentoInfo criarDocumentoInfoDoArquivo(Path arquivo) {
        try {
            String nomeArquivo = arquivo.getFileName().toString();
            String[] partes = nomeArquivo.split("_", 2);
            
            if (partes.length < 2) {
                return null;
            }
            
            String tipoInterno = partes[0];
            long tamanho = Files.size(arquivo);
            
            DocumentoInfo info = new DocumentoInfo();
            info.setTipo(obterNomeTipoDocumento(tipoInterno));
            info.setTipoInterno(tipoInterno);
            info.setNomeArquivo(nomeArquivo);
            info.setNomeArquivoSalvo(nomeArquivo);
            info.setCaminhoArquivo(arquivo.toString());
            info.setTamanho(formatarTamanho(tamanho));
            info.setTamanhoBytes(tamanho);
            info.setDataUpload(LocalDateTime.now());
            info.setObrigatorio(DOCUMENTOS_OBRIGATORIOS.containsKey(tipoInterno));
            
            return info;
            
        } catch (IOException e) {
            logger.error("Erro ao criar info do documento {}: {}", arquivo, e.getMessage());
            return null;
        }
    }
    
    private boolean isSessaoAntiga(Path diretorio) {
        try {
            return Files.getLastModifiedTime(diretorio).toInstant()
                    .isBefore(LocalDateTime.now().minusHours(24).atZone(
                            java.time.ZoneId.systemDefault()).toInstant());
        } catch (IOException e) {
            return false;
        }
    }
    
    // Classes internas
    
    public static class DocumentoInfo {
        private String tipo;
        private String tipoInterno;
        private String nomeArquivo;
        private String nomeArquivoSalvo;
        private String caminhoArquivo;
        private String tamanho;
        private long tamanhoBytes;
        private String contentType;
        private LocalDateTime dataUpload;
        private boolean obrigatorio;
        
        // Getters e Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getTipoInterno() { return tipoInterno; }
        public void setTipoInterno(String tipoInterno) { this.tipoInterno = tipoInterno; }
        
        public String getNomeArquivo() { return nomeArquivo; }
        public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
        
        public String getNomeArquivoSalvo() { return nomeArquivoSalvo; }
        public void setNomeArquivoSalvo(String nomeArquivoSalvo) { this.nomeArquivoSalvo = nomeArquivoSalvo; }
        
        public String getCaminhoArquivo() { return caminhoArquivo; }
        public void setCaminhoArquivo(String caminhoArquivo) { this.caminhoArquivo = caminhoArquivo; }
        
        public String getTamanho() { return tamanho; }
        public void setTamanho(String tamanho) { this.tamanho = tamanho; }
        
        public long getTamanhoBytes() { return tamanhoBytes; }
        public void setTamanhoBytes(long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public LocalDateTime getDataUpload() { return dataUpload; }
        public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
        
        public boolean isObrigatorio() { return obrigatorio; }
        public void setObrigatorio(boolean obrigatorio) { this.obrigatorio = obrigatorio; }
    }
    
    public static class DocumentoException extends Exception {
        public DocumentoException(String message) {
            super(message);
        }
        
        public DocumentoException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    public void processarDocumentos(String sessionId, MultipartFile documento1,
                                    MultipartFile documento2, MultipartFile documento3,
                                    List<MultipartFile> anexos) {

        Map<String, Object> documentos = new HashMap<>();

        try {
            if (documento1 != null && !documento1.isEmpty()) {
                documentos.put("documento1", documento1.getBytes());
            }
            if (documento2 != null && !documento2.isEmpty()) {
                documentos.put("documento2", documento2.getBytes());
            }
            if (documento3 != null && !documento3.isEmpty()) {
                documentos.put("documento3", documento3.getBytes());
            }
            if (anexos != null && !anexos.isEmpty()) {
                List<byte[]> listaAnexos = new ArrayList<>();
                for (MultipartFile anexo : anexos) {
                    listaAnexos.add(anexo.getBytes());
                }
                documentos.put("anexos", listaAnexos);
            }

            // Salvar documentos no workflow de adesão
            workflowAdesaoService.salvarDocumentos(sessionId, documentos);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar documentos: " + e.getMessage(), e);
        }
    }

    
}