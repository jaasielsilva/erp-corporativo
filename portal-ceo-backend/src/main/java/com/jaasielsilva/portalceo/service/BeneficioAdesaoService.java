package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.repository.PlanoSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gestão de benefícios no processo de adesão de colaboradores.
 * Responsável por listar benefícios disponíveis, calcular custos e processar seleções.
 */
@Service
public class BeneficioAdesaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(BeneficioAdesaoService.class);
    
    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;
    
    /**
     * Obter todos os benefícios disponíveis para adesão
     */
    public List<BeneficioInfo> obterBeneficiosDisponiveis() {
        List<BeneficioInfo> beneficios = new ArrayList<>();
        
        try {
            // Planos de Saúde
            List<PlanoSaude> planosSaude = planoSaudeRepository.findByAtivoTrue();
            if (!planosSaude.isEmpty()) {
                BeneficioInfo planoSaudeInfo = new BeneficioInfo();
                planoSaudeInfo.setTipo("PLANO_SAUDE");
                planoSaudeInfo.setNome("Plano de Saúde");
                planoSaudeInfo.setDescricao("Assistência médica e hospitalar");
                planoSaudeInfo.setObrigatorio(false);
                planoSaudeInfo.setPermiteDependentes(true);
                planoSaudeInfo.setOpcoes(planosSaude.stream()
                    .map(this::criarOpcaoPlanoSaude)
                    .collect(Collectors.toList()));
                beneficios.add(planoSaudeInfo);
            }
            
            // Vale Refeição
            BeneficioInfo valeRefeicao = new BeneficioInfo();
            valeRefeicao.setTipo("VALE_REFEICAO");
            valeRefeicao.setNome("Vale Refeição");
            valeRefeicao.setDescricao("Auxílio alimentação para refeições");
            valeRefeicao.setObrigatorio(false);
            valeRefeicao.setPermiteDependentes(false);
            valeRefeicao.setOpcoes(Arrays.asList(
                criarOpcaoValeRefeicao("VR_200", "R$ 200,00", new BigDecimal("200.00")),
                criarOpcaoValeRefeicao("VR_300", "R$ 300,00", new BigDecimal("300.00")),
                criarOpcaoValeRefeicao("VR_400", "R$ 400,00", new BigDecimal("400.00")),
                criarOpcaoValeRefeicao("VR_500", "R$ 500,00", new BigDecimal("500.00"))
            ));
            beneficios.add(valeRefeicao);
            
            // Vale Transporte
            BeneficioInfo valeTransporte = new BeneficioInfo();
            valeTransporte.setTipo("VALE_TRANSPORTE");
            valeTransporte.setNome("Vale Transporte");
            valeTransporte.setDescricao("Auxílio para deslocamento casa-trabalho");
            valeTransporte.setObrigatorio(false);
            valeTransporte.setPermiteDependentes(false);
            valeTransporte.setOpcoes(Arrays.asList(
                criarOpcaoValeTransporte("VT_150", "R$ 150,00", new BigDecimal("150.00")),
                criarOpcaoValeTransporte("VT_200", "R$ 200,00", new BigDecimal("200.00")),
                criarOpcaoValeTransporte("VT_250", "R$ 250,00", new BigDecimal("250.00")),
                criarOpcaoValeTransporte("VT_300", "R$ 300,00", new BigDecimal("300.00"))
            ));
            beneficios.add(valeTransporte);
            
            logger.info("Carregados {} benefícios disponíveis para adesão", beneficios.size());
            
        } catch (Exception e) {
            logger.error("Erro ao carregar benefícios disponíveis: {}", e.getMessage(), e);
        }
        
        return beneficios;
    }
    
    /**
     * Calcular custo total dos benefícios selecionados
     */
    public CalculoBeneficio calcularCustoBeneficios(Map<String, Object> beneficiosSelecionados) {
        CalculoBeneficio calculo = new CalculoBeneficio();
        BigDecimal custoTotal = BigDecimal.ZERO;
        List<ItemBeneficio> itens = new ArrayList<>();
        
        try {
            // Processar cada benefício selecionado
            for (Map.Entry<String, Object> entry : beneficiosSelecionados.entrySet()) {
                String tipoBeneficio = entry.getKey();
                Object selecao = entry.getValue();
                
                if (selecao == null) continue;
                
                switch (tipoBeneficio) {
                    case "PLANO_SAUDE":
                        ItemBeneficio itemPlano = processarPlanoSaude(selecao);
                        if (itemPlano != null) {
                            itens.add(itemPlano);
                            custoTotal = custoTotal.add(itemPlano.getCustoTotal());
                        }
                        break;
                        
                    case "VALE_REFEICAO":
                        ItemBeneficio itemVR = processarValeRefeicao(selecao);
                        if (itemVR != null) {
                            itens.add(itemVR);
                            custoTotal = custoTotal.add(itemVR.getCustoTotal());
                        }
                        break;
                        
                    case "VALE_TRANSPORTE":
                        ItemBeneficio itemVT = processarValeTransporte(selecao);
                        if (itemVT != null) {
                            itens.add(itemVT);
                            custoTotal = custoTotal.add(itemVT.getCustoTotal());
                        }
                        break;
                }
            }
            
            calculo.setItens(itens);
            calculo.setCustoTotal(custoTotal);
            calculo.setQuantidadeItens(itens.size());
            
            logger.info("Cálculo de benefícios realizado: {} itens, custo total: R$ {}", 
                       itens.size(), custoTotal);
            
        } catch (Exception e) {
            logger.error("Erro ao calcular custo dos benefícios: {}", e.getMessage(), e);
        }
        
        return calculo;
    }
    
    /**
     * Validar seleção de benefícios
     */
    public List<String> validarSelecaoBeneficios(Map<String, Object> beneficiosSelecionados) {
        List<String> erros = new ArrayList<>();
        
        try {
            for (Map.Entry<String, Object> entry : beneficiosSelecionados.entrySet()) {
                String tipoBeneficio = entry.getKey();
                Object selecao = entry.getValue();
                
                if (selecao == null) continue;
                
                switch (tipoBeneficio) {
                    case "PLANO_SAUDE":
                        erros.addAll(validarPlanoSaude(selecao));
                        break;
                    case "VALE_REFEICAO":
                        erros.addAll(validarValeRefeicao(selecao));
                        break;
                    case "VALE_TRANSPORTE":
                        erros.addAll(validarValeTransporte(selecao));
                        break;
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao validar seleção de benefícios: {}", e.getMessage(), e);
            erros.add("Erro interno na validação dos benefícios");
        }
        
        return erros;
    }
    
    // Métodos privados para processamento
    
    private OpcaoBeneficio criarOpcaoPlanoSaude(PlanoSaude plano) {
        OpcaoBeneficio opcao = new OpcaoBeneficio();
        opcao.setId(plano.getId().toString());
        opcao.setNome(plano.getNome());
        opcao.setDescricao(plano.getDescricao());
        opcao.setValor(plano.getValorMensal());
        opcao.setValorFormatado("R$ " + plano.getValorMensal().toString());
        
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("cobertura", plano.getCobertura());
        detalhes.put("rede", plano.getRedeCredenciada());
        detalhes.put("carencia", plano.getCarencia());
        opcao.setDetalhes(detalhes);
        
        return opcao;
    }
    
    private OpcaoBeneficio criarOpcaoValeRefeicao(String id, String nome, BigDecimal valor) {
        OpcaoBeneficio opcao = new OpcaoBeneficio();
        opcao.setId(id);
        opcao.setNome(nome);
        opcao.setDescricao("Vale refeição mensal");
        opcao.setValor(valor);
        opcao.setValorFormatado("R$ " + valor.toString());
        return opcao;
    }
    
    private OpcaoBeneficio criarOpcaoValeTransporte(String id, String nome, BigDecimal valor) {
        OpcaoBeneficio opcao = new OpcaoBeneficio();
        opcao.setId(id);
        opcao.setNome(nome);
        opcao.setDescricao("Vale transporte mensal");
        opcao.setValor(valor);
        opcao.setValorFormatado("R$ " + valor.toString());
        return opcao;
    }
    
    private ItemBeneficio processarPlanoSaude(Object selecao) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosPlano = (Map<String, Object>) selecao;
            
            logger.info("Processando plano de saúde: {}", dadosPlano);
            
            Object planoIdObj = dadosPlano.get("planoId");
            String planoId = planoIdObj != null ? planoIdObj.toString() : null;
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dependentes = (List<Map<String, Object>>) dadosPlano.get("dependentes");
            
            if (planoId == null || planoId.trim().isEmpty()) {
                logger.error("PlanoId não informado ou vazio");
                return null;
            }
            
            // Buscar plano por código primeiro, depois por ID
            Optional<PlanoSaude> planoOpt = Optional.empty();
            
            // 1. Tenta buscar por código (basico, intermediario, premium)
            try {
                planoOpt = planoSaudeRepository.findByCodigo(planoId);
                if (planoOpt.isPresent()) {
                    logger.info("Plano encontrado por código '{}': {}", planoId, planoOpt.get().getNome());
                }
            } catch (Exception e) {
                logger.warn("Erro ao buscar plano por código '{}': {}", planoId, e.getMessage());
            }
            
            // 2. Se não encontrou por código, tenta por ID numérico
            if (!planoOpt.isPresent()) {
                try {
                    Long id = Long.parseLong(planoId);
                    planoOpt = planoSaudeRepository.findById(id);
                    if (planoOpt.isPresent()) {
                        logger.info("Plano encontrado por ID numérico {}: {}", id, planoOpt.get().getNome());
                    }
                } catch (NumberFormatException e) {
                    logger.info("PlanoId '{}' não é um número válido", planoId);
                }
            }
            
            if (!planoOpt.isPresent()) {
                logger.error("Plano com ID/código '{}' não encontrado", planoId);
                return null;
            }
            
            PlanoSaude plano = planoOpt.get();
            ItemBeneficio item = new ItemBeneficio();
            item.setTipo("PLANO_SAUDE");
            item.setNome("Plano de Saúde - " + plano.getNome());
            item.setPlano(plano.getNome());
            
            // Usar valores do plano ou valores padrão se não estiverem definidos
            BigDecimal custoTitular = plano.getValorTitular() != null ? 
                plano.getValorTitular() : plano.getValorMensal();
            
            BigDecimal valorDependente = plano.getValorDependente() != null ? 
                plano.getValorDependente() : custoTitular.multiply(new BigDecimal("0.6"));
            
            BigDecimal custoDependentes = BigDecimal.ZERO;
            int quantidadeDependentes = 0;
            
            if (dependentes != null && !dependentes.isEmpty()) {
                quantidadeDependentes = dependentes.size();
                custoDependentes = valorDependente.multiply(new BigDecimal(quantidadeDependentes));
                logger.info("Calculando {} dependentes: {} x {} = {}", 
                    quantidadeDependentes, valorDependente, quantidadeDependentes, custoDependentes);
            }
            
            item.setCustoTitular(custoTitular);
            item.setCustoDependentes(custoDependentes);
            item.setCustoTotal(custoTitular.add(custoDependentes));
            item.setQuantidadeDependentes(quantidadeDependentes);
            
            logger.info("Item processado: {} - Titular: R$ {}, Dependentes: R$ {}, Total: R$ {}",
                item.getNome(), custoTitular, custoDependentes, item.getCustoTotal());
            
            return item;
            
        } catch (Exception e) {
            logger.error("Erro ao processar plano de saúde: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private ItemBeneficio processarValeRefeicao(Object selecao) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosVR = (Map<String, Object>) selecao;
            
            logger.info("Processando vale refeição: {}", dadosVR);
            
            Object valorObj = dadosVR.get("valor");
            String valorId = valorObj != null ? valorObj.toString() : null;
            
            if (valorId == null || valorId.trim().isEmpty()) {
                logger.error("Valor do vale refeição não informado");
                return null;
            }
            
            BigDecimal valor = extrairValorVale(valorId);
            if (valor == null) {
                logger.error("Não foi possível extrair valor do ID: '{}'", valorId);
                return null;
            }
            
            ItemBeneficio item = new ItemBeneficio();
            item.setTipo("VALE_REFEICAO");
            item.setNome("Vale Refeição");
            item.setPlano("R$ " + valor.toString());
            item.setCustoTitular(valor);
            item.setCustoDependentes(BigDecimal.ZERO);
            item.setCustoTotal(valor);
            item.setQuantidadeDependentes(0);
            
            logger.info("Vale refeição processado: R$ {}", valor);
            
            return item;
            
        } catch (Exception e) {
            logger.error("Erro ao processar vale refeição: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private ItemBeneficio processarValeTransporte(Object selecao) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosVT = (Map<String, Object>) selecao;
            
            logger.info("Processando vale transporte: {}", dadosVT);
            
            Object valorObj = dadosVT.get("valor");
            String valorId = valorObj != null ? valorObj.toString() : null;
            
            if (valorId == null || valorId.trim().isEmpty()) {
                logger.error("Valor do vale transporte não informado");
                return null;
            }
            
            BigDecimal valor = extrairValorVale(valorId);
            if (valor == null) {
                logger.error("Não foi possível extrair valor do ID: '{}'", valorId);
                return null;
            }
            
            ItemBeneficio item = new ItemBeneficio();
            item.setTipo("VALE_TRANSPORTE");
            item.setNome("Vale Transporte");
            item.setPlano("R$ " + valor.toString());
            item.setCustoTitular(valor);
            item.setCustoDependentes(BigDecimal.ZERO);
            item.setCustoTotal(valor);
            item.setQuantidadeDependentes(0);
            
            logger.info("Vale transporte processado: R$ {}", valor);
            
            return item;
            
        } catch (Exception e) {
            logger.error("Erro ao processar vale transporte: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private BigDecimal extrairValorVale(String valorId) {
        if (valorId == null) return null;
        
        logger.info("Extraindo valor do vale com ID: '{}'", valorId);
        
        switch (valorId) {
            // Vale Refeição - formatos antigos
            case "VR_200": case "200": return new BigDecimal("200.00");
            case "VR_300": case "300": return new BigDecimal("300.00");
            case "VR_400": case "400": return new BigDecimal("400.00");
            case "VR_500": case "500": return new BigDecimal("500.00");
            
            // Vale Refeição - novos formatos do frontend
            case "vale-200": return new BigDecimal("200.00");
            case "vale-300": return new BigDecimal("300.00");
            case "vale-400": return new BigDecimal("400.00");
            case "vale-500": return new BigDecimal("500.00");
            
            // Vale Transporte - formatos antigos
            case "VT_150": case "150": return new BigDecimal("150.00");
            case "VT_200": return new BigDecimal("200.00");
            case "VT_250": case "250": return new BigDecimal("250.00");
            case "VT_300": return new BigDecimal("300.00");
            case "VT_350": case "350": return new BigDecimal("350.00");
            
            // Vale Transporte - novos formatos do frontend
            case "transporte-150": return new BigDecimal("150.00");
            case "transporte-200": return new BigDecimal("200.00");
            case "transporte-250": return new BigDecimal("250.00");
            case "transporte-300": return new BigDecimal("300.00");
            case "transporte-350": return new BigDecimal("350.00");
            
            // Planos de códigos textuais
            case "basico": return new BigDecimal("150.00");
            case "intermediario": return new BigDecimal("250.00");
            case "premium": return new BigDecimal("350.00");
            
            default:
                // Tentar extrair valor numérico do ID
                try {
                    // Se for apenas números, usar diretamente
                    if (valorId.matches("^\\d+$")) {
                        return new BigDecimal(valorId + ".00");
                    }
                    
                    // Extrair números do ID (vale-300 -> 300, transporte-150 -> 150)
                    String valorStr = valorId.replaceAll("[^0-9]", "");
                    if (!valorStr.isEmpty()) {
                        return new BigDecimal(valorStr + ".00");
                    }
                } catch (Exception e) {
                    logger.error("Erro ao extrair valor numérico de '{}': {}", valorId, e.getMessage());
                }
                
                logger.warn("Valor de vale não reconhecido: '{}'", valorId);
                return null;
        }
    }
    
    private List<String> validarPlanoSaude(Object selecao) {
        List<String> erros = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosPlano = (Map<String, Object>) selecao;
            
            logger.info("Validando plano de saúde - dados recebidos: {}", dadosPlano);
            
            Object planoIdObj = dadosPlano.get("planoId");
            logger.info("PlanoId objeto: {}, tipo: {}", planoIdObj, planoIdObj != null ? planoIdObj.getClass().getName() : "null");
            
            String planoId = null;
            if (planoIdObj != null) {
                planoId = planoIdObj.toString();
            }
            
            if (planoId == null || planoId.trim().isEmpty()) {
                erros.add("Plano de saúde deve ser selecionado");
                return erros;
            }
            
            logger.info("PlanoId como string: '{}'", planoId);
            
            // Verificar se plano existe - primeiro tenta por código, depois por ID
            boolean planoExiste = false;
            try {
                // Tenta primeiro buscar por código (basico, intermediario, premium)
                Optional<PlanoSaude> planoPorCodigo = planoSaudeRepository.findByCodigo(planoId);
                if (planoPorCodigo.isPresent()) {
                    planoExiste = true;
                    logger.info("Plano encontrado por código '{}': ID {}", planoId, planoPorCodigo.get().getId());
                } else {
                    // Se não encontrou por código, tenta por ID numérico
                    try {
                        Long id = Long.parseLong(planoId);
                        logger.info("Tentando buscar por ID numérico: {}", id);
                        if (planoSaudeRepository.existsById(id)) {
                            planoExiste = true;
                            logger.info("Plano encontrado por ID numérico: {}", id);
                        }
                    } catch (NumberFormatException e) {
                        logger.info("PlanoId '{}' não é um número, tentativa de busca por código falhou", planoId);
                    }
                }
                
                if (!planoExiste) {
                    erros.add("ID do plano de saúde inválido");
                    logger.warn("Plano com código/ID '{}' não existe no banco", planoId);
                }
            } catch (Exception e) {
                erros.add("Erro ao validar plano de saúde");
                logger.error("Erro ao validar plano '{}': {}", planoId, e.getMessage());
            }
            
            // Validar dependentes se existirem
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dependentes = (List<Map<String, Object>>) dadosPlano.get("dependentes");
            if (dependentes != null) {
                for (int i = 0; i < dependentes.size(); i++) {
                    Map<String, Object> dependente = dependentes.get(i);
                    String nome = (String) dependente.get("nome");
                    String parentesco = (String) dependente.get("parentesco");
                    
                    if (nome == null || nome.trim().isEmpty()) {
                        erros.add("Nome do dependente " + (i + 1) + " é obrigatório");
                    }
                    
                    if (parentesco == null || parentesco.trim().isEmpty()) {
                        erros.add("Parentesco do dependente " + (i + 1) + " é obrigatório");
                    }
                }
            }
            
        } catch (Exception e) {
            erros.add("Dados do plano de saúde inválidos");
        }
        
        return erros;
    }
    
    private List<String> validarValeRefeicao(Object selecao) {
        List<String> erros = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosVR = (Map<String, Object>) selecao;
            String valor = (String) dadosVR.get("valor");
            
            if (valor == null || valor.trim().isEmpty()) {
                erros.add("Valor do vale refeição deve ser selecionado");
            }
            
        } catch (Exception e) {
            erros.add("Dados do vale refeição inválidos");
        }
        
        return erros;
    }
    
    private List<String> validarValeTransporte(Object selecao) {
        List<String> erros = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosVT = (Map<String, Object>) selecao;
            String valor = (String) dadosVT.get("valor");
            
            if (valor == null || valor.trim().isEmpty()) {
                erros.add("Valor do vale transporte deve ser selecionado");
            }
            
        } catch (Exception e) {
            erros.add("Dados do vale transporte inválidos");
        }
        
        return erros;
    }
    
    // Classes internas
    
    public static class BeneficioInfo {
        private String tipo;
        private String nome;
        private String descricao;
        private boolean obrigatorio;
        private boolean permiteDependentes;
        private List<OpcaoBeneficio> opcoes;
        
        // Getters e Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public boolean isObrigatorio() { return obrigatorio; }
        public void setObrigatorio(boolean obrigatorio) { this.obrigatorio = obrigatorio; }
        
        public boolean isPermiteDependentes() { return permiteDependentes; }
        public void setPermiteDependentes(boolean permiteDependentes) { this.permiteDependentes = permiteDependentes; }
        
        public List<OpcaoBeneficio> getOpcoes() { return opcoes; }
        public void setOpcoes(List<OpcaoBeneficio> opcoes) { this.opcoes = opcoes; }
    }
    
    public static class OpcaoBeneficio {
        private String id;
        private String nome;
        private String descricao;
        private BigDecimal valor;
        private String valorFormatado;
        private Map<String, Object> detalhes;
        
        // Getters e Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public BigDecimal getValor() { return valor; }
        public void setValor(BigDecimal valor) { this.valor = valor; }
        
        public String getValorFormatado() { return valorFormatado; }
        public void setValorFormatado(String valorFormatado) { this.valorFormatado = valorFormatado; }
        
        public Map<String, Object> getDetalhes() { return detalhes; }
        public void setDetalhes(Map<String, Object> detalhes) { this.detalhes = detalhes; }
    }
    
    public static class CalculoBeneficio {
        private List<ItemBeneficio> itens;
        private BigDecimal custoTotal;
        private int quantidadeItens;
        
        public CalculoBeneficio() {
            this.itens = new ArrayList<>();
            this.custoTotal = BigDecimal.ZERO;
            this.quantidadeItens = 0;
        }
        
        // Getters e Setters
        public List<ItemBeneficio> getItens() { return itens; }
        public void setItens(List<ItemBeneficio> itens) { this.itens = itens; }
        
        public BigDecimal getCustoTotal() { return custoTotal; }
        public void setCustoTotal(BigDecimal custoTotal) { this.custoTotal = custoTotal; }
        
        public int getQuantidadeItens() { return quantidadeItens; }
        public void setQuantidadeItens(int quantidadeItens) { this.quantidadeItens = quantidadeItens; }
    }
    
    public static class ItemBeneficio {
        private String tipo;
        private String nome;
        private String plano;
        private BigDecimal custoTitular;
        private BigDecimal custoDependentes;
        private BigDecimal custoTotal;
        private int quantidadeDependentes;
        
        // Getters e Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public String getPlano() { return plano; }
        public void setPlano(String plano) { this.plano = plano; }
        
        public BigDecimal getCustoTitular() { return custoTitular; }
        public void setCustoTitular(BigDecimal custoTitular) { this.custoTitular = custoTitular; }
        
        public BigDecimal getCustoDependentes() { return custoDependentes; }
        public void setCustoDependentes(BigDecimal custoDependentes) { this.custoDependentes = custoDependentes; }
        
        public BigDecimal getCustoTotal() { return custoTotal; }
        public void setCustoTotal(BigDecimal custoTotal) { this.custoTotal = custoTotal; }
        
        public int getQuantidadeDependentes() { return quantidadeDependentes; }
        public void setQuantidadeDependentes(int quantidadeDependentes) { this.quantidadeDependentes = quantidadeDependentes; }
    }
}