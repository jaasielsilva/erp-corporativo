package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.repository.BeneficioRepository;
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
    private BeneficioRepository beneficioRepository;
    
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
            
            String planoId = (String) dadosPlano.get("planoId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dependentes = (List<Map<String, Object>>) dadosPlano.get("dependentes");
            
            Optional<PlanoSaude> planoOpt = planoSaudeRepository.findById(Long.parseLong(planoId));
            if (!planoOpt.isPresent()) {
                return null;
            }
            
            PlanoSaude plano = planoOpt.get();
            ItemBeneficio item = new ItemBeneficio();
            item.setTipo("PLANO_SAUDE");
            item.setNome("Plano de Saúde - " + plano.getNome());
            
            BigDecimal custoTitular = plano.getValorTitular();
            BigDecimal custoDependentes = BigDecimal.ZERO;
            
            if (dependentes != null && !dependentes.isEmpty()) {
                custoDependentes = plano.getValorDependente()
                    .multiply(new BigDecimal(dependentes.size()));
            }
            
            item.setCustoTitular(custoTitular);
            item.setCustoDependentes(custoDependentes);
            item.setCustoTotal(custoTitular.add(custoDependentes));
            item.setQuantidadeDependentes(dependentes != null ? dependentes.size() : 0);
            
            return item;
            
        } catch (Exception e) {
            logger.error("Erro ao processar plano de saúde: {}", e.getMessage());
            return null;
        }
    }
    
    private ItemBeneficio processarValeRefeicao(Object selecao) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosVR = (Map<String, Object>) selecao;
            String valorId = (String) dadosVR.get("valor");
            
            BigDecimal valor = extrairValorVale(valorId);
            if (valor == null) {
                return null;
            }
            
            ItemBeneficio item = new ItemBeneficio();
            item.setTipo("VALE_REFEICAO");
            item.setNome("Vale Refeição");
            item.setCustoTitular(valor);
            item.setCustoDependentes(BigDecimal.ZERO);
            item.setCustoTotal(valor);
            item.setQuantidadeDependentes(0);
            
            return item;
            
        } catch (Exception e) {
            logger.error("Erro ao processar vale refeição: {}", e.getMessage());
            return null;
        }
    }
    
    private ItemBeneficio processarValeTransporte(Object selecao) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosVT = (Map<String, Object>) selecao;
            String valorId = (String) dadosVT.get("valor");
            
            BigDecimal valor = extrairValorVale(valorId);
            if (valor == null) {
                return null;
            }
            
            ItemBeneficio item = new ItemBeneficio();
            item.setTipo("VALE_TRANSPORTE");
            item.setNome("Vale Transporte");
            item.setCustoTitular(valor);
            item.setCustoDependentes(BigDecimal.ZERO);
            item.setCustoTotal(valor);
            item.setQuantidadeDependentes(0);
            
            return item;
            
        } catch (Exception e) {
            logger.error("Erro ao processar vale transporte: {}", e.getMessage());
            return null;
        }
    }
    
    private BigDecimal extrairValorVale(String valorId) {
        if (valorId == null) return null;
        
        switch (valorId) {
            case "VR_200": case "VT_150": return new BigDecimal("150.00");
            case "VR_300": case "VT_200": return new BigDecimal("200.00");
            case "VR_400": case "VT_250": return new BigDecimal("250.00");
            case "VR_500": case "VT_300": return new BigDecimal("300.00");
            default:
                // Tentar extrair valor do ID
                try {
                    String valorStr = valorId.replaceAll("[^0-9]", "");
                    return new BigDecimal(valorStr + ".00");
                } catch (Exception e) {
                    return null;
                }
        }
    }
    
    private List<String> validarPlanoSaude(Object selecao) {
        List<String> erros = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosPlano = (Map<String, Object>) selecao;
            
            String planoId = (String) dadosPlano.get("planoId");
            if (planoId == null || planoId.trim().isEmpty()) {
                erros.add("Plano de saúde deve ser selecionado");
                return erros;
            }
            
            // Verificar se plano existe
            try {
                Long id = Long.parseLong(planoId);
                if (!planoSaudeRepository.existsById(id)) {
                    erros.add("Plano de saúde selecionado não existe");
                }
            } catch (NumberFormatException e) {
                erros.add("ID do plano de saúde inválido");
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
        private BigDecimal custoTitular;
        private BigDecimal custoDependentes;
        private BigDecimal custoTotal;
        private int quantidadeDependentes;
        
        // Getters e Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
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