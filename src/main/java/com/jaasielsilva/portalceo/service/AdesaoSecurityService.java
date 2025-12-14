package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.AdesaoColaboradorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Serviço responsável por validações de segurança no processo de adesão de colaboradores.
 * Implementa controles de rate limiting, validação de dados, sanitização e prevenção de ataques.
 */
@Service
public class AdesaoSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdesaoSecurityService.class);
    
    // Rate limiting por IP
    private final Map<String, List<LocalDateTime>> requestsByIp = new ConcurrentHashMap<>();
    
    // Controle de tentativas de upload por sessão
    private final Map<String, Integer> uploadAttempts = new ConcurrentHashMap<>();
    
    // Sessões bloqueadas temporariamente
    private final Map<String, LocalDateTime> blockedSessions = new ConcurrentHashMap<>();
    
    // Padrões de validação
    private static final Pattern CPF_PATTERN = Pattern.compile("^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]{2,100}$");
    
    // Limites de segurança
    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final int MAX_UPLOAD_ATTEMPTS_PER_SESSION = 10;
    private static final int SESSION_TIMEOUT_HOURS = 2;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    // Tipos de arquivo permitidos
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif"
    );
    
    // Extensões de arquivo permitidas
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".pdf", ".jpg", ".jpeg", ".png", ".gif"
    );
    
    /**
     * Verifica rate limiting por IP
     */
    public boolean checkRateLimit(String clientIp) {
        if (clientIp == null || clientIp.trim().isEmpty()) {
            logger.warn("IP do cliente não fornecido para verificação de rate limit");
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
        
        // Limpar requests antigos
        requestsByIp.computeIfPresent(clientIp, (ip, requests) -> {
            requests.removeIf(time -> time.isBefore(oneMinuteAgo));
            return requests.isEmpty() ? null : requests;
        });
        
        // Verificar limite
        List<LocalDateTime> requests = requestsByIp.computeIfAbsent(clientIp, k -> new ArrayList<>());
        
        if (requests.size() >= MAX_REQUESTS_PER_MINUTE) {
            logger.warn("Rate limit excedido para IP: {} - {} requests no último minuto", clientIp, requests.size());
            return false;
        }
        
        // Adicionar request atual
        requests.add(now);
        return true;
    }
    
    /**
     * Valida dados pessoais do colaborador
     */
    public ValidationResult validateDadosPessoais(AdesaoColaboradorDTO dados) {
        ValidationResult result = new ValidationResult();
        
        // Validar nome
        if (dados.getNome() == null || dados.getNome().trim().isEmpty()) {
            result.addError("nome", "Nome é obrigatório");
        } else if (!NAME_PATTERN.matcher(dados.getNome().trim()).matches()) {
            result.addError("nome", "Nome deve conter apenas letras e espaços (2-100 caracteres)");
        }
        
        // Validar CPF (aceita com ou sem pontuação)
        if (dados.getCpf() == null || dados.getCpf().trim().isEmpty()) {
            result.addError("cpf", "CPF é obrigatório");
        } else {
            String rawCpf = dados.getCpf().trim();
            String digits = rawCpf.replaceAll("[^0-9]", "");
            if (!CPF_PATTERN.matcher(rawCpf).matches() && digits.length() != 11) {
                result.addError("cpf", "CPF deve conter 11 dígitos (com ou sem pontuação)");
            } else if (digits.length() != 11) {
                result.addError("cpf", "CPF deve conter 11 dígitos (com ou sem pontuação)");
            } else if (digits.matches("(\\d)\\1{10}")) {
                result.addError("cpf", "CPF inválido: todos os dígitos iguais");
            } else if (!isValidCPF(digits)) {
                result.addError("cpf", "CPF inválido: dígitos verificadores não conferem");
            }
        }
        
        // Validar email
        if (dados.getEmail() == null || dados.getEmail().trim().isEmpty()) {
            result.addError("email", "Email é obrigatório");
        } else if (!EMAIL_PATTERN.matcher(dados.getEmail().trim()).matches()) {
            result.addError("email", "Email deve ter um formato válido");
        }
        
        // Validar telefone
        if (dados.getTelefone() == null || dados.getTelefone().trim().isEmpty()) {
            result.addError("telefone", "Telefone é obrigatório");
        } else if (!PHONE_PATTERN.matcher(dados.getTelefone().trim()).matches()) {
            result.addError("telefone", "Telefone deve estar no formato (00) 00000-0000");
        }
        
        // Validar cargo
        if (dados.getCargoId() == null) {
            result.addError("cargoId", "Cargo é obrigatório");
        }
        
        // Validar departamento
        if (dados.getDepartamentoId() == null) {
            result.addError("departamentoId", "Departamento é obrigatório");
        }
        
        // Validar salário
        if (dados.getSalario() == null || dados.getSalario().doubleValue() <= 0) {
            result.addError("salario", "Salário deve ser maior que zero");
        }
        
        // Sanitizar dados
        if (result.isValid()) {
            dados.setNome(sanitizeString(dados.getNome()));
            dados.setEmail(sanitizeString(dados.getEmail()).toLowerCase());
            // Padroniza apresentação do CPF para o formato 000.000.000-00
            String digits = dados.getCpf() != null ? dados.getCpf().replaceAll("[^0-9]", "") : null;
            dados.setCpf(digits != null && digits.length() == 11 ?
                    digits.substring(0,3) + "." + digits.substring(3,6) + "." + digits.substring(6,9) + "-" + digits.substring(9)
                    : sanitizeString(dados.getCpf()));
            dados.setTelefone(sanitizeString(dados.getTelefone()));
        }
        
        return result;
    }
    
    /**
     * Valida arquivo de upload
     */
    public ValidationResult validateFileUpload(MultipartFile file, String sessionId) {
        ValidationResult result = new ValidationResult();
        
        // Verificar tentativas de upload por sessão
        int attempts = uploadAttempts.getOrDefault(sessionId, 0);
        if (attempts >= MAX_UPLOAD_ATTEMPTS_PER_SESSION) {
            result.addError("file", "Limite de tentativas de upload excedido para esta sessão");
            return result;
        }
        
        // Incrementar contador de tentativas
        uploadAttempts.put(sessionId, attempts + 1);
        
        // Validar se arquivo foi enviado
        if (file == null || file.isEmpty()) {
            result.addError("file", "Arquivo é obrigatório");
            return result;
        }
        
        // Validar tamanho do arquivo
        if (file.getSize() > MAX_FILE_SIZE) {
            result.addError("file", "Arquivo muito grande. Tamanho máximo: 10MB");
            return result;
        }
        
        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)) {
            result.addError("file", "Tipo de arquivo não permitido. Permitidos: PDF, JPG, PNG, GIF");
            return result;
        }
        
        // Validar extensão do arquivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            result.addError("file", "Nome do arquivo é obrigatório");
            return result;
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            result.addError("file", "Extensão de arquivo não permitida");
            return result;
        }
        
        // Validar nome do arquivo (prevenir path traversal)
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            result.addError("file", "Nome de arquivo inválido");
            return result;
        }
        
        return result;
    }
    
    /**
     * Verifica se sessão está bloqueada
     */
    public boolean isSessionBlocked(String sessionId) {
        LocalDateTime blockedUntil = blockedSessions.get(sessionId);
        if (blockedUntil != null) {
            if (LocalDateTime.now().isBefore(blockedUntil)) {
                return true;
            } else {
                // Remover bloqueio expirado
                blockedSessions.remove(sessionId);
            }
        }
        return false;
    }
    
    /**
     * Bloqueia sessão temporariamente
     */
    public void blockSession(String sessionId, int minutes) {
        LocalDateTime blockedUntil = LocalDateTime.now().plus(minutes, ChronoUnit.MINUTES);
        blockedSessions.put(sessionId, blockedUntil);
        logger.warn("Sessão {} bloqueada até {}", sessionId, blockedUntil);
    }
    
    /**
     * Limpa dados de segurança antigos
     */
    public void cleanupOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minus(SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS);
        
        // Limpar requests antigos
        requestsByIp.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(time -> time.isBefore(cutoff));
            return entry.getValue().isEmpty();
        });
        
        // Limpar bloqueios expirados
        blockedSessions.entrySet().removeIf(entry -> 
            LocalDateTime.now().isAfter(entry.getValue()));
        
        logger.debug("Limpeza de dados de segurança concluída");
    }
    
    /**
     * Sanitiza string removendo caracteres perigosos
     */
    private String sanitizeString(String input) {
        if (input == null) return null;
        
        return input.trim()
            .replaceAll("[<>\"'&]", "") // Remove caracteres HTML perigosos
            .replaceAll("\\s+", " "); // Normaliza espaços
    }
    
    /**
     * Valida CPF usando algoritmo oficial
     */
    private boolean isValidCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) return false;
        
        // Verificar se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) return false;
        
        try {
            // Calcular primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;
            
            // Calcular segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;
            
            // Verificar dígitos
            return Character.getNumericValue(cpf.charAt(9)) == firstDigit &&
                   Character.getNumericValue(cpf.charAt(10)) == secondDigit;
                   
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extrai extensão do arquivo
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    /**
     * Classe para resultado de validação
     */
    public static class ValidationResult {
        private final Map<String, List<String>> errors = new HashMap<>();
        
        public void addError(String field, String message) {
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public Map<String, List<String>> getErrors() {
            return errors;
        }
        
        public List<String> getFieldErrors(String field) {
            return errors.getOrDefault(field, new ArrayList<>());
        }
        
        public String getFirstError() {
            return errors.values().stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(null);
        }
    }
}
