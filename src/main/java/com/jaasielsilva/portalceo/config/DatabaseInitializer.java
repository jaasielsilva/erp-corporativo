package com.jaasielsilva.portalceo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.repository.BeneficioRepository;
import com.jaasielsilva.portalceo.repository.PlanoSaudeRepository;
import com.jaasielsilva.portalceo.service.FormaPagamentoService;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private BeneficioRepository beneficioRepository;
    
    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;
    
    @Autowired
    private FormaPagamentoService formaPagamentoService;

    // Aqui você já deve ter injeções de UserRepository, RoleRepository etc.
    // @Autowired
    // private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        // DESABILITADO - Planos de Saúde são criados no SecurityConfig.java
        // Manter somente os benefícios genéricos se necessário
        
        // ===== Populando Benefícios =====
        List<String> planos = List.of(
            "Plano de Saúde",
            "Vale Transporte",
            "Vale Refeição"
        );

        for (String nome : planos) {
            if (!beneficioRepository.existsByNome(nome)) {
                Beneficio b = new Beneficio();
                b.setNome(nome);
                beneficioRepository.save(b);
            }
        }
        
        System.out.println("DatabaseInitializer: Benefícios genéricos criados. Planos de Saúde são gerenciados pelo SecurityConfig.");
        
        // ===== Inicializando Formas de Pagamento =====
        formaPagamentoService.inicializarFormasPadrao();
        System.out.println("DatabaseInitializer: Formas de pagamento inicializadas.");
    }
}
