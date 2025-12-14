package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.ConfiguracaoValeTransporteDTO;
import com.jaasielsilva.portalceo.model.RhValeTransporteConfig;
import com.jaasielsilva.portalceo.repository.RhValeTransporteConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class RhValeTransporteConfigService {

    private final RhValeTransporteConfigRepository repository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RhValeTransporteConfigService(RhValeTransporteConfigRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable("vtConfig")
    public ConfiguracaoValeTransporteDTO obterConfiguracaoAtual() {
        RhValeTransporteConfig cfg = repository.findFirstByOrderByDataAtualizacaoDesc();
        if (cfg == null) {
            return new ConfiguracaoValeTransporteDTO();
        }
        ConfiguracaoValeTransporteDTO dto = new ConfiguracaoValeTransporteDTO();
        dto.setPercentualDesconto(cfg.getPercentualDesconto());
        dto.setValorPassagem(cfg.getValorPassagem());
        dto.setDiasUteisPadrao(cfg.getDiasUteisMes());
        dto.setPercentualMaximoDesconto(BigDecimal.valueOf(6.0));
        return dto;
    }

    @CacheEvict(value = "vtConfig", allEntries = true)
    public java.util.Map<String,Object> salvarConfiguracao(ConfiguracaoValeTransporteDTO dto, String usuario) {
        try {
            java.util.List<java.util.Map<String,Object>> result = jdbcTemplate.query(
                    "CALL sp_config_vale_transporte(?, ?, ?, ?)",
                    ps -> {
                        ps.setBigDecimal(1, dto.getPercentualDesconto());
                        ps.setBigDecimal(2, dto.getValorPassagem());
                        ps.setInt(3, dto.getDiasUteisPadrao());
                        ps.setString(4, usuario);
                    },
                    (ResultSet rs, int rowNum) -> {
                        java.util.Map<String,Object> map = new java.util.HashMap<>();
                        map.put("sucesso", rs.getBoolean("sucesso"));
                        map.put("mensagem", rs.getString("mensagem"));
                        return map;
                    }
            );
            return result.isEmpty() ? java.util.Map.of("sucesso", false, "mensagem", "Sem resposta da procedure") : result.get(0);
        } catch (Exception e) {
            RhValeTransporteConfig cfg = repository.findFirstByOrderByDataAtualizacaoDesc();
            if (cfg == null) {
                cfg = new RhValeTransporteConfig();
                cfg.setVersao(1);
            } else {
                cfg.setVersao(Integer.valueOf(cfg.getVersao() == null ? 1 : cfg.getVersao() + 1));
            }
            cfg.setPercentualDesconto(dto.getPercentualDesconto());
            cfg.setValorPassagem(dto.getValorPassagem());
            cfg.setDiasUteisMes(dto.getDiasUteisPadrao());
            cfg.setUsuarioAtualizacao(usuario != null ? usuario : "sistema");
            cfg.setDataAtualizacao(LocalDateTime.now());
            repository.save(cfg);
            return java.util.Map.of("sucesso", true, "mensagem", "Configuração atualizada (fallback)");
        }
    }
}
