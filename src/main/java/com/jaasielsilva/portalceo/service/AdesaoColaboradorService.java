package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.AdesaoColaboradorDTO;
import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.repository.*;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AdesaoColaboradorService {

    private static final Logger logger = LoggerFactory.getLogger(AdesaoColaboradorService.class);

    // Cache temporário para dados de adesão em andamento
    private final Map<String, AdesaoColaboradorDTO> adesaoTemporaria = new ConcurrentHashMap<>();

    // Controle de etapas concluídas por sessão
    private final Map<String, Set<String>> etapasConcluidas = new ConcurrentHashMap<>();

    @Value("${app.upload.dir:uploads/colaboradores}")
    private String uploadDir;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private BeneficioService beneficioService;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DocumentoAdesaoService documentoService;

    @Autowired
    private WorkflowAdesaoService workflowService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Verifica se CPF já existe no sistema
     */
    public boolean existeCpf(String cpf) {
        String cpfFmt = formatCpf(cpf);
        return colaboradorRepository.existsByCpf(cpfFmt != null ? cpfFmt : cpf);
    }

    /**
     * Verifica se email já existe no sistema
     */
    public boolean existeEmail(String email) {
        return colaboradorRepository.existsByEmail(email);
    }

    /**
     * Salva dados temporários da adesão e retorna ID da sessão
     */
    public String salvarDadosTemporarios(AdesaoColaboradorDTO dadosAdesao) {
        String sessionId = UUID.randomUUID().toString();

        dadosAdesao.setSessionId(sessionId);
        dadosAdesao.setEtapaAtual("DADOS_PESSOAIS");
        dadosAdesao.setStatusProcesso("EM_ANDAMENTO");
        dadosAdesao.setDataInicioProcesso(java.time.LocalDate.now());

        adesaoTemporaria.put(sessionId, dadosAdesao);

        logger.info("Dados temporários salvos para sessão: {} - Colaborador: {}",
                sessionId, dadosAdesao.getNome());

        return sessionId;
    }

    /**
     * Marcar etapa como concluída
     */
    public void marcarEtapaConcluida(String sessionId, String etapa) {
        etapasConcluidas.computeIfAbsent(sessionId, k -> new HashSet<>()).add(etapa);

        // Atualizar etapa atual no DTO
        AdesaoColaboradorDTO adesao = adesaoTemporaria.get(sessionId);
        if (adesao != null) {
            switch (etapa) {
                case "dados-pessoais":
                    adesao.setEtapaAtual("DOCUMENTOS");
                    break;
                case "documentos":
                    adesao.setEtapaAtual("BENEFICIOS");
                    break;
                case "beneficios":
                    adesao.setEtapaAtual("revisao");
                    break;
            }
        }

        logger.info("Etapa {} marcada como concluída para sessão {}", etapa, sessionId);
    }

    /**
     * Verificar se etapa foi concluída
     */
    public boolean isEtapaConcluida(String sessionId, String etapa) {
        Set<String> etapas = etapasConcluidas.get(sessionId);
        return etapas != null && etapas.contains(etapa);
    }

    /**
     * Processa upload de documentos
     */
    public Map<String, String> processarDocumentos(
            String sessionId,
            MultipartFile documentoRg,
            MultipartFile documentoCpf,
            MultipartFile comprovanteEndereco,
            List<MultipartFile> outrosDocumentos) throws IOException {

        AdesaoColaboradorDTO dadosAdesao = adesaoTemporaria.get(sessionId);
        if (dadosAdesao == null) {
            throw new IllegalArgumentException("Sessão não encontrada: " + sessionId);
        }

        Map<String, String> documentosUpload = new HashMap<>();

        // Criar diretório específico para este colaborador
        String colaboradorDir = uploadDir + "/" + sessionId;
        Path dirPath = Paths.get(colaboradorDir);
        Files.createDirectories(dirPath);

        // Processar documentos obrigatórios
        documentosUpload.put("RG", salvarArquivo(documentoRg, colaboradorDir, "rg"));
        documentosUpload.put("CPF", salvarArquivo(documentoCpf, colaboradorDir, "cpf"));
        documentosUpload.put("Comprovante de Endereço",
                salvarArquivo(comprovanteEndereco, colaboradorDir, "comprovante_endereco"));

        // Processar documentos opcionais
        if (outrosDocumentos != null && !outrosDocumentos.isEmpty()) {
            int contador = 1;
            for (MultipartFile arquivo : outrosDocumentos) {
                if (!arquivo.isEmpty()) {
                    String nomeArquivo = "documento_adicional_" + contador;
                    documentosUpload.put("Documento Adicional " + contador,
                            salvarArquivo(arquivo, colaboradorDir, nomeArquivo));
                    contador++;
                }
            }
        }

        // Atualizar dados temporários
        dadosAdesao.setDocumentosUpload(documentosUpload);
        dadosAdesao.setEtapaAtual("DOCUMENTOS");
        adesaoTemporaria.put(sessionId, dadosAdesao);

        logger.info("Documentos processados para sessão: {} - Total: {}",
                sessionId, documentosUpload.size());

        return documentosUpload;
    }

    /**
     * Salva arquivo no sistema de arquivos
     */
    private String salvarArquivo(MultipartFile arquivo, String diretorio, String nomeBase) throws IOException {
        String extensao = getExtensaoArquivo(arquivo.getOriginalFilename());
        String nomeArquivo = nomeBase + "_" + System.currentTimeMillis() + extensao;
        Path caminhoArquivo = Paths.get(diretorio, nomeArquivo);

        Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

        return caminhoArquivo.toString();
    }

    /**
     * Extrai extensão do arquivo
     */
    private String getExtensaoArquivo(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
    }

    /**
     * Processa seleção de benefícios
     */
    public void processarBeneficios(String sessionId, Map<String, Object> beneficiosSelecionados) {
        AdesaoColaboradorDTO dadosAdesao = adesaoTemporaria.get(sessionId);
        if (dadosAdesao == null) {
            throw new IllegalArgumentException("Sessão não encontrada: " + sessionId);
        }

        dadosAdesao.setBeneficiosSelecionados(beneficiosSelecionados);

        // Processar benefícios específicos
        processarBeneficiosEspecificos(dadosAdesao, beneficiosSelecionados);

        // Atualizar etapa atual para revisao após processar benefícios
        dadosAdesao.setEtapaAtual("revisao");
        adesaoTemporaria.put(sessionId, dadosAdesao);

        logger.info("Benefícios processados para sessão: {} - Total selecionados: {}",
                sessionId, beneficiosSelecionados.size());
    }

    /**
     * Processa benefícios específicos (plano de saúde, vale refeição, etc.)
     */
    private void processarBeneficiosEspecificos(AdesaoColaboradorDTO dadosAdesao, Map<String, Object> beneficios) {
        // Plano de Saúde
        if (beneficios.containsKey("planoSaude")) {
            Map<String, Object> planoSaude = (Map<String, Object>) beneficios.get("planoSaude");
            dadosAdesao.setPlanoSaudeOpcional(true);
            dadosAdesao.setPlanoSaudeTipo((String) planoSaude.get("tipo"));
            dadosAdesao.setPlanoSaudeDependentes((Integer) planoSaude.get("dependentes"));
        }

        // Vale Refeição
        if (beneficios.containsKey("valeRefeicao")) {
            Map<String, Object> valeRefeicao = (Map<String, Object>) beneficios.get("valeRefeicao");
            dadosAdesao.setValeRefeicaoOpcional(true);
            dadosAdesao.setValeRefeicaoValor(
                    java.math.BigDecimal.valueOf((Double) valeRefeicao.get("valor")));
        }

        // Vale Transporte
        if (beneficios.containsKey("valeTransporte")) {
            Map<String, Object> valeTransporte = (Map<String, Object>) beneficios.get("valeTransporte");
            dadosAdesao.setValeTransporteOpcional(true);
            dadosAdesao.setValeTransporteValor(
                    java.math.BigDecimal.valueOf((Double) valeTransporte.get("valor")));
        }

        // Vale Alimentação
        if (beneficios.containsKey("valeAlimentacao")) {
            Map<String, Object> valeAlimentacao = (Map<String, Object>) beneficios.get("valeAlimentacao");
            dadosAdesao.setValeAlimentacaoOpcional(true);
            dadosAdesao.setValeAlimentacaoValor(
                    java.math.BigDecimal.valueOf((Double) valeAlimentacao.get("valor")));
        }
    }

    /**
     * Obtém benefícios selecionados da sessão
     */
    public Map<String, Object> obterBeneficiosSessao(String sessionId) {
        AdesaoColaboradorDTO dadosAdesao = adesaoTemporaria.get(sessionId);
        if (dadosAdesao == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> beneficios = dadosAdesao.getBeneficiosSelecionados();
        return beneficios != null ? beneficios : new HashMap<>();
    }

    /**
     * Verifica se a sessão existe
     */
    public boolean existeSessao(String sessionId) {
        return adesaoTemporaria.containsKey(sessionId);
    }

    /**
     * Obtém dados completos da adesão
     */
    public AdesaoColaboradorDTO obterDadosCompletos(String sessionId) {
        AdesaoColaboradorDTO dadosAdesao = adesaoTemporaria.get(sessionId);
        if (dadosAdesao == null) {
            throw new IllegalArgumentException("Sessão não encontrada: " + sessionId);
        }

        // Consultar a etapa atual do workflow ao invés de sempre definir como 'revisao'
        try {
            ProcessoAdesao processo = workflowService.buscarProcessoPorSessionId(sessionId);
            if (processo != null) {
                dadosAdesao.setEtapaAtual(processo.getEtapaAtual());
            }
        } catch (Exception e) {
            logger.warn("Erro ao consultar etapa atual do workflow para sessão {}: {}", sessionId, e.getMessage());
            // Manter a etapa atual que já estava no DTO se houver erro
        }

        adesaoTemporaria.put(sessionId, dadosAdesao);

        return dadosAdesao;
    }
    
    /**
     * Atualiza dados temporários da adesão
     */
    public void atualizarDadosTemporarios(String sessionId, AdesaoColaboradorDTO dadosAtualizados) {
        if (sessionId == null || dadosAtualizados == null) {
            throw new IllegalArgumentException("SessionId e dados não podem ser nulos");
        }
        dadosAtualizados.setSessionId(sessionId);
        if (dadosAtualizados.getEtapaAtual() == null || dadosAtualizados.getEtapaAtual().isBlank()) {
            dadosAtualizados.setEtapaAtual("DADOS_PESSOAIS");
        }
        if (dadosAtualizados.getStatusProcesso() == null || dadosAtualizados.getStatusProcesso().isBlank()) {
            dadosAtualizados.setStatusProcesso("EM_ANDAMENTO");
        }
        adesaoTemporaria.put(sessionId, dadosAtualizados);
        logger.info("Dados temporários atualizados para sessionId: {} - Etapa: {}", sessionId, dadosAtualizados.getEtapaAtual());
    }

    /**
     * Finaliza o processo de adesão criando o colaborador definitivamente
     */
    public Colaborador finalizarAdesao(String sessionId) {
        AdesaoColaboradorDTO dadosAdesao = adesaoTemporaria.get(sessionId);
        if (dadosAdesao == null) {
            throw new IllegalArgumentException("Sessão não encontrada: " + sessionId);
        }

        if (!dadosAdesao.isProntoParaFinalizacao()) {
            throw new IllegalStateException("Processo não está pronto para finalização");
        }

        try {
            // Criar colaborador
            Colaborador colaborador = criarColaboradorFromDTO(dadosAdesao);
            colaborador = colaboradorRepository.save(colaborador);

            // Removido: criação e vínculo automático de usuário ao finalizar adesão
            // Usuário deve ser criado posteriormente por fluxo específico
            
            // Recarregar o colaborador com o usuário vinculado
            colaborador = colaboradorRepository.findById(colaborador.getId())
                    .orElseThrow(() -> new RuntimeException("Erro ao recarregar colaborador"));

            logger.info("Usuário criado automaticamente para colaborador: {} - Matrícula: {}", 
                    colaborador.getNome(), 
                    colaborador.getUsuario() != null ? colaborador.getUsuario().getMatricula() : "N/A");

            // Mover documentos para o diretório do colaborador
            documentoService.moverDocumentosParaColaborador(sessionId, colaborador.getId());

            // Processar benefícios
            if (dadosAdesao.getBeneficiosSelecionados() != null &&
                    !dadosAdesao.getBeneficiosSelecionados().isEmpty()) {
                processarBeneficiosColaborador(colaborador, dadosAdesao);
            }

            // Criar histórico
            criarHistoricoAdesao(colaborador, dadosAdesao);

            // Enviar notificações
            enviarNotificacoesAdesao(colaborador, dadosAdesao);

            // Limpar dados temporários
            dadosAdesao.setEtapaAtual("FINALIZADO");
            dadosAdesao.setStatusProcesso("FINALIZADO");
            dadosAdesao.setDataFinalizacaoProcesso(java.time.LocalDate.now());

            // Limpar controle de etapas
            etapasConcluidas.remove(sessionId);

            // Manter por mais um tempo para consulta, depois remover
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    adesaoTemporaria.remove(sessionId);
                }
            }, 24 * 60 * 60 * 1000); // 24 horas

            logger.info("Adesão finalizada com sucesso - Colaborador: {} (ID: {})",
                    colaborador.getNome(), colaborador.getId());

            return colaborador;

        } catch (Exception e) {
            logger.error("Erro ao finalizar adesão para sessão: {}", sessionId, e);
            throw new RuntimeException("Erro ao finalizar adesão: " + e.getMessage(), e);
        }
    }

    /**
     * Cria colaborador a partir do DTO
     */
    private Colaborador criarColaboradorFromDTO(AdesaoColaboradorDTO dto) {
        Colaborador colaborador = new Colaborador();

        // Dados pessoais
        colaborador.setNome(dto.getNome());
        colaborador.setCpf(formatCpf(dto.getCpf()));
        colaborador.setEmail(dto.getEmail());
        colaborador.setTelefone(dto.getTelefone());
        colaborador.setSexo(Colaborador.Sexo.valueOf(dto.getSexo()));
        colaborador.setDataNascimento(dto.getDataNascimento());
        colaborador.setEstadoCivil(Colaborador.EstadoCivil.valueOf(dto.getEstadoCivil()));
        colaborador.setDependentes(dto.getDependentes());
        colaborador.setRg(dto.getRg());

        // Dados profissionais
        colaborador.setDataAdmissao(dto.getDataAdmissao());
        colaborador.setSalario(dto.getSalario());
        colaborador.setTipoContrato(dto.getTipoContrato());
        colaborador.setCargaHoraria(dto.getCargaHoraria());

        // Relacionamentos
        if (dto.getCargoId() != null) {
            Cargo cargo = cargoRepository.findById(dto.getCargoId())
                    .orElseThrow(() -> new RuntimeException("Cargo não encontrado: " + dto.getCargoId()));
            colaborador.setCargo(cargo);
        }

        if (dto.getDepartamentoId() != null) {
            Departamento departamento = departamentoRepository.findById(dto.getDepartamentoId())
                    .orElseThrow(() -> new RuntimeException("Departamento não encontrado: " + dto.getDepartamentoId()));
            colaborador.setDepartamento(departamento);
        }

        if (dto.getSupervisorId() != null) {
            Colaborador supervisor = colaboradorRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("Supervisor não encontrado: " + dto.getSupervisorId()));
            colaborador.setSupervisor(supervisor);
        }

        // Endereço
        colaborador.setCep(dto.getCep());
        colaborador.setLogradouro(dto.getLogradouro());
        colaborador.setNumero(dto.getNumero());
        colaborador.setComplemento(dto.getComplemento());
        colaborador.setBairro(dto.getBairro());
        colaborador.setCidade(dto.getCidade());
        colaborador.setEstado(dto.getEstado());
        colaborador.setPais(dto.getPais());

        // Observações
        colaborador.setObservacoes(dto.getObservacoes());

        // Status inicial: aguardando aprovação do RH
        colaborador.setStatus(Colaborador.StatusColaborador.INATIVO);
        colaborador.setAtivo(false);

        return colaborador;
    }

    private String formatCpf(String cpf) {
        if (cpf == null) return null;
        String trimmed = cpf.trim();
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.length() != 11) return trimmed;
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9, 11);
    }

    /**
     * Processa benefícios do colaborador
     */
    private void processarBeneficiosColaborador(Colaborador colaborador, AdesaoColaboradorDTO dto) {
        List<ColaboradorBeneficio> lista = new ArrayList<>();
        List<Beneficio> todos = beneficioService.listarTodos();

        Optional<Beneficio> planoSaudeBeneficio = todos.stream()
                .filter(b -> b.getNome() != null && b.getNome().equalsIgnoreCase("Plano de Saúde"))
                .findFirst()
                .or(() -> todos.stream().filter(b -> b.getNome() != null && b.getNome().equalsIgnoreCase("Plano Saude")).findFirst())
                .or(() -> todos.stream().filter(b -> b.getNome() != null && b.getNome().toLowerCase().contains("plano")).findFirst());

        Optional<Beneficio> valeRefeicaoBeneficio = todos.stream()
                .filter(b -> b.getNome() != null && b.getNome().equalsIgnoreCase("Vale Refeição"))
                .findFirst()
                .or(() -> todos.stream().filter(b -> b.getNome() != null && b.getNome().toLowerCase().contains("refei")).findFirst());

        Optional<Beneficio> valeTransporteBeneficio = todos.stream()
                .filter(b -> b.getNome() != null && b.getNome().equalsIgnoreCase("Vale Transporte"))
                .findFirst()
                .or(() -> todos.stream().filter(b -> b.getNome() != null && b.getNome().toLowerCase().contains("transp")).findFirst());

        Optional<Beneficio> valeAlimentacaoBeneficio = todos.stream()
                .filter(b -> b.getNome() != null && b.getNome().equalsIgnoreCase("Vale Alimentação"))
                .findFirst()
                .or(() -> todos.stream().filter(b -> b.getNome() != null && b.getNome().toLowerCase().contains("aliment")).findFirst());

        if (planoSaudeBeneficio.isPresent()) {
            BigDecimal valorPlano = BigDecimal.ZERO;
            Map<String, Object> selecionados = dto.getBeneficiosSelecionados();
            Map<String, Object> planoMap = null;
            if (selecionados != null) {
                Object psUpper = selecionados.get("PLANO_SAUDE");
                Object psLower = selecionados.get("planoSaude");
                if (psUpper instanceof Map) planoMap = (Map<String, Object>) psUpper;
                else if (psLower instanceof Map) planoMap = (Map<String, Object>) psLower;
            }
            String planoId = null;
            Integer dependentes = dto.getPlanoSaudeDependentes() != null ? dto.getPlanoSaudeDependentes() : 0;
            if (planoMap != null) {
                Object planoIdObj = planoMap.get("planoId");
                planoId = planoIdObj != null ? planoIdObj.toString() : null;
                Object depObj = planoMap.get("dependentes");
                if (depObj instanceof Number) {
                    dependentes = ((Number) depObj).intValue();
                } else if (depObj instanceof String) {
                    try {
                        dependentes = Integer.parseInt((String) depObj);
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (dto.getPlanoSaudeId() != null) {
                planoId = dto.getPlanoSaudeId().toString();
            }
            if (planoId != null) {
                Optional<PlanoSaude> planoOpt = Optional.empty();
                Optional<PlanoSaude> porCodigo = planoSaudeRepository.findByCodigo(planoId);
                if (porCodigo.isPresent()) {
                    planoOpt = porCodigo;
                } else {
                    try {
                        Long idLong = Long.parseLong(planoId);
                        planoOpt = planoSaudeRepository.findById(idLong);
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (planoOpt.isPresent()) {
                    PlanoSaude plano = planoOpt.get();
                    BigDecimal titular = plano.calcularValorColaboradorTitular();
                    BigDecimal dep = plano.calcularValorColaboradorDependente();
                    BigDecimal dependentesTotal = dep.multiply(BigDecimal.valueOf(dependentes));
                    valorPlano = titular.add(dependentesTotal);
                }
            }
            if (valorPlano.compareTo(BigDecimal.ZERO) > 0) {
                Beneficio b = planoSaudeBeneficio.get();
                ColaboradorBeneficio cb = new ColaboradorBeneficio();
                cb.setColaborador(colaborador);
                cb.setBeneficio(b);
                cb.setValor(valorPlano);
                lista.add(cb);
            }
        }

        {
            BigDecimal valorVR = null;
            Map<String, Object> selecionados = dto.getBeneficiosSelecionados();
            Map<String, Object> vrMap = null;
            if (selecionados != null) {
                Object vrUpper = selecionados.get("VALE_REFEICAO");
                Object vrLower = selecionados.get("valeRefeicao");
                if (vrUpper instanceof Map) vrMap = (Map<String, Object>) vrUpper;
                else if (vrLower instanceof Map) vrMap = (Map<String, Object>) vrLower;
            }
            if (vrMap != null) {
                Object val = vrMap.get("valor");
                if (val instanceof Number) {
                    valorVR = BigDecimal.valueOf(((Number) val).doubleValue());
                } else if (val instanceof String) {
                    String s = (String) val;
                    String nums = s.replaceAll("[^0-9]", "");
                    if (!nums.isEmpty()) {
                        valorVR = new BigDecimal(nums + ".00");
                    }
                }
            } else if (dto.getValeRefeicaoValor() != null) {
                valorVR = dto.getValeRefeicaoValor();
            }
            if (valorVR != null && valeRefeicaoBeneficio.isPresent()) {
                Beneficio b = valeRefeicaoBeneficio.get();
                ColaboradorBeneficio cb = new ColaboradorBeneficio();
                cb.setColaborador(colaborador);
                cb.setBeneficio(b);
                cb.setValor(valorVR);
                lista.add(cb);
            }
        }

        {
            BigDecimal valorVT = null;
            Map<String, Object> selecionados = dto.getBeneficiosSelecionados();
            Map<String, Object> vtMap = null;
            if (selecionados != null) {
                Object vtUpper = selecionados.get("VALE_TRANSPORTE");
                Object vtLower = selecionados.get("valeTransporte");
                if (vtUpper instanceof Map) vtMap = (Map<String, Object>) vtUpper;
                else if (vtLower instanceof Map) vtMap = (Map<String, Object>) vtLower;
            }
            if (vtMap != null) {
                Object val = vtMap.get("valor");
                if (val instanceof Number) {
                    valorVT = BigDecimal.valueOf(((Number) val).doubleValue());
                } else if (val instanceof String) {
                    String s = (String) val;
                    String nums = s.replaceAll("[^0-9]", "");
                    if (!nums.isEmpty()) {
                        valorVT = new BigDecimal(nums + ".00");
                    }
                }
            } else if (dto.getValeTransporteValor() != null) {
                valorVT = dto.getValeTransporteValor();
            }
            if (valorVT != null && valeTransporteBeneficio.isPresent()) {
                Beneficio b = valeTransporteBeneficio.get();
                ColaboradorBeneficio cb = new ColaboradorBeneficio();
                cb.setColaborador(colaborador);
                cb.setBeneficio(b);
                cb.setValor(valorVT);
                lista.add(cb);
            }
        }

        {
            BigDecimal valorVA = null;
            Map<String, Object> selecionados = dto.getBeneficiosSelecionados();
            Map<String, Object> vaMap = null;
            if (selecionados != null) {
                Object vaUpper = selecionados.get("VALE_ALIMENTACAO");
                Object vaLower = selecionados.get("valeAlimentacao");
                if (vaUpper instanceof Map) vaMap = (Map<String, Object>) vaUpper;
                else if (vaLower instanceof Map) vaMap = (Map<String, Object>) vaLower;
            }
            if (vaMap != null) {
                Object val = vaMap.get("valor");
                if (val instanceof Number) {
                    valorVA = BigDecimal.valueOf(((Number) val).doubleValue());
                } else if (val instanceof String) {
                    String s = (String) val;
                    String nums = s.replaceAll("[^0-9]", "");
                    if (!nums.isEmpty()) {
                        valorVA = new BigDecimal(nums + ".00");
                    }
                }
            } else if (dto.getValeAlimentacaoValor() != null) {
                valorVA = dto.getValeAlimentacaoValor();
            }
            if (valorVA != null && valeAlimentacaoBeneficio.isPresent()) {
                Beneficio b = valeAlimentacaoBeneficio.get();
                ColaboradorBeneficio cb = new ColaboradorBeneficio();
                cb.setColaborador(colaborador);
                cb.setBeneficio(b);
                cb.setValor(valorVA);
                lista.add(cb);
            }
        }

        if (!lista.isEmpty()) {
            colaborador.setBeneficios(lista);
            beneficioService.salvarBeneficiosDoColaborador(colaborador);
        }
    }

    /**
     * Cria histórico da adesão
     */
    private void criarHistoricoAdesao(Colaborador colaborador, AdesaoColaboradorDTO dto) {
        // Implementar criação de histórico
        logger.info("Criando histórico de adesão para colaborador: {}", colaborador.getId());
    }

    /**
     * Envia notificações sobre a adesão
     */
    private void enviarNotificacoesAdesao(Colaborador colaborador, AdesaoColaboradorDTO dto) {
        try {
            // Notificar RH
            notificationService.notificarNovoColaborador(colaborador);

            // Enviar email de boas-vindas
            emailService.enviarEmailBoasVindas(colaborador);

            logger.info("Notificações enviadas para novo colaborador: {}", colaborador.getId());

        } catch (Exception e) {
            logger.warn("Erro ao enviar notificações para colaborador: {}", colaborador.getId(), e);
        }
    }

    /**
     * Cancela processo de adesão
     */
    public void cancelarAdesao(String sessionId) {
        AdesaoColaboradorDTO dadosAdesao = adesaoTemporaria.remove(sessionId);
        etapasConcluidas.remove(sessionId);

        if (dadosAdesao != null) {
            // Limpar arquivos uploadados
            limparArquivosTemporarios(sessionId);

            logger.info("Processo de adesão cancelado para sessão: {}", sessionId);
        }
    }

    

    /**
     * Remove arquivos temporários
     */
    private void limparArquivosTemporarios(String sessionId) {
        try {
            Path dirPath = Paths.get(uploadDir, sessionId);
            if (Files.exists(dirPath)) {
                Files.walk(dirPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                logger.warn("Não foi possível deletar arquivo: {}", file.getPath());
                            }
                        });
            }
        } catch (IOException e) {
            logger.error("Erro ao limpar arquivos temporários para sessão: {}", sessionId, e);
        }
    }

    /**
     * Lista adesões em andamento
     */
    public List<AdesaoColaboradorDTO> listarAdesoesEmAndamento() {
        return adesaoTemporaria.values().stream()
                .filter(adesao -> "EM_ANDAMENTO".equals(adesao.getStatusProcesso()))
                .sorted((a, b) -> b.getDataInicioProcesso().compareTo(a.getDataInicioProcesso()))
                .toList();
    }

    /**
     * Limpa adesões antigas automaticamente (executar periodicamente)
     */
    public void limparAdesoesAntigas() {
        java.time.LocalDate dataLimite = java.time.LocalDate.now().minusDays(7);

        List<String> sessionsParaRemover = adesaoTemporaria.entrySet().stream()
                .filter(entry -> entry.getValue().getDataInicioProcesso().isBefore(dataLimite))
                .map(Map.Entry::getKey)
                .toList();

        sessionsParaRemover.forEach(sessionId -> {
            cancelarAdesao(sessionId);
            logger.info("Adesão antiga removida automaticamente: {}", sessionId);
        });
    }
}
