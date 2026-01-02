package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.suporte.ChamadoAnexo;
import com.jaasielsilva.portalceo.repository.ChamadoAnexoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ChamadoAnexoService {

    private static final Logger logger = LoggerFactory.getLogger(ChamadoAnexoService.class);

    @Autowired
    private ChamadoAnexoRepository anexoRepository;

    @Value("${app.upload.path:uploads}")
    private String uploadBasePath;

    @Value("${app.upload.chamados.max-size:10485760}")
    private long maxFileSize;

    /**
     * Salva um anexo para um chamado
     */
    public ChamadoAnexo salvarAnexo(MultipartFile arquivo, Chamado chamado, Usuario usuario) throws IOException {
        // Validações
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }

        if (arquivo.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Arquivo excede o tamanho máximo permitido");
        }

        // Validar tipo de arquivo
        String contentType = arquivo.getContentType();
        if (!isContentTypePermitido(contentType)) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido: " + contentType);
        }

        // Criar diretório específico para o chamado
        Path diretorioChamado = criarDiretorioChamado(chamado.getId());

        // Gerar nome único para o arquivo
        String nomeOriginal = arquivo.getOriginalFilename();
        String extensao = getExtensao(nomeOriginal);
        String nomeUnico = gerarNomeUnico() + "." + extensao;

        // Salvar arquivo no sistema de arquivos
        Path caminhoArquivo = diretorioChamado.resolve(nomeUnico);
        Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

        // Criar entidade ChamadoAnexo
        ChamadoAnexo anexo = new ChamadoAnexo();
        anexo.setChamado(chamado);
        anexo.setUsuario(usuario);
        anexo.setNomeArquivo(nomeUnico);
        anexo.setNomeOriginal(nomeOriginal);
        anexo.setCaminhoArquivo(caminhoArquivo.toString());
        anexo.setTipoMime(contentType);
        anexo.setTamanhoBytes(arquivo.getSize());
        anexo.setDataUpload(LocalDateTime.now());
        anexo.setAtivo(true);

        // Salvar no banco de dados
        ChamadoAnexo anexoSalvo = anexoRepository.save(anexo);

        logger.info("Anexo salvo: {} para chamado {} no diretório {}", nomeOriginal, chamado.getNumero(), diretorioChamado);
        return anexoSalvo;
    }

    /**
     * Lista anexos ativos de um chamado
     */
    public List<ChamadoAnexo> listarAnexosPorChamado(Long chamadoId) {
        return anexoRepository.findByChamadoIdAndAtivoTrue(chamadoId);
    }

    /**
     * Busca anexo por ID
     */
    public ChamadoAnexo buscarPorId(Long id) {
        return anexoRepository.findById(id).orElse(null);
    }

    /**
     * Remove anexo (marca como inativo)
     */
    public void removerAnexo(Long id) {
        ChamadoAnexo anexo = anexoRepository.findById(id).orElse(null);
        if (anexo != null) {
            anexo.setAtivo(false);
            anexoRepository.save(anexo);
            logger.info("Anexo {} marcado como inativo", anexo.getNomeOriginal());
        }
    }

    /**
     * Remove anexo fisicamente
     */
    public void removerAnexoFisicamente(Long id) throws IOException {
        ChamadoAnexo anexo = anexoRepository.findById(id).orElse(null);
        if (anexo != null) {
            // Remover arquivo do sistema de arquivos
            Path caminhoArquivo = Paths.get(anexo.getCaminhoArquivo());
            if (Files.exists(caminhoArquivo)) {
                Files.delete(caminhoArquivo);
            }

            // Remover do banco de dados
            anexoRepository.delete(anexo);
            logger.info("Anexo {} removido fisicamente", anexo.getNomeOriginal());
        }
    }

    /**
     * Conta anexos ativos de um chamado
     */
    public Long contarAnexosPorChamado(Long chamadoId) {
        return anexoRepository.countByChamadoIdAndAtivoTrue(chamadoId);
    }

    /**
     * Verifica se o tipo de conteúdo é permitido
     */
    private boolean isContentTypePermitido(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("application/vnd.ms-excel") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
               contentType.startsWith("image/");
    }

    /**
     * Extrai a extensão do arquivo
     */
    private String getExtensao(String nomeArquivo) {
        if (nomeArquivo != null && nomeArquivo.contains(".")) {
            return nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Gera nome único para o arquivo
     */
    private String gerarNomeUnico() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "anexo_" + timestamp + "_" + uuid;
    }

    /**
     * Cria diretório específico para o chamado
     */
    private Path criarDiretorioChamado(Long chamadoId) throws IOException {
        Path diretorioChamado = Paths.get(uploadBasePath, "chamados", chamadoId.toString());
        Files.createDirectories(diretorioChamado);
        logger.debug("Diretório criado/verificado: {}", diretorioChamado);
        return diretorioChamado;
    }
}
