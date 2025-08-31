package com.jaasielsilva.portalceo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.repository.BeneficioRepository;
import com.jaasielsilva.portalceo.repository.PlanoSaudeRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private BeneficioRepository beneficioRepository;
    
    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;

    // Aqui você já deve ter injeções de UserRepository, RoleRepository etc.
    // @Autowired
    // private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {

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
        
        // ===== Populando Planos de Saúde =====
        if (planoSaudeRepository.count() == 0) {
            // Plano Básico
            PlanoSaude basico = new PlanoSaude();
            basico.setNome("Plano Básico");
            basico.setOperadora("Unimed");
            basico.setCodigo("basico");
            basico.setDescricao("Consultas médicas e exames básicos");
            basico.setTipo(PlanoSaude.TipoPlano.BASICO);
            basico.setValorTitular(new BigDecimal("89.90"));
            basico.setValorDependente(new BigDecimal("60.00"));
            basico.setAtivo(true);
            planoSaudeRepository.save(basico);
            
            // Plano Intermediário
            PlanoSaude intermediario = new PlanoSaude();
            intermediario.setNome("Plano Intermediário");
            intermediario.setOperadora("Unimed");
            intermediario.setCodigo("intermediario");
            intermediario.setDescricao("Consultas, exames e internações");
            intermediario.setTipo(PlanoSaude.TipoPlano.INTERMEDIARIO);
            intermediario.setValorTitular(new BigDecimal("149.90"));
            intermediario.setValorDependente(new BigDecimal("100.00"));
            intermediario.setAtivo(true);
            planoSaudeRepository.save(intermediario);
            
            // Plano Premium
            PlanoSaude premium = new PlanoSaude();
            premium.setNome("Plano Premium");
            premium.setOperadora("Unimed");
            premium.setCodigo("premium");
            premium.setDescricao("Cobertura completa com rede ampliada");
            premium.setTipo(PlanoSaude.TipoPlano.PREMIUM);
            premium.setValorTitular(new BigDecimal("249.90"));
            premium.setValorDependente(new BigDecimal("150.00"));
            premium.setAtivo(true);
            planoSaudeRepository.save(premium);
            
            System.out.println("Planos de saúde inicializados com sucesso!");
        } else {
            System.out.println("Planos de saúde já existem. Pulando inicialização.");
        }

        // ===== Aqui vai o código já existente do Spring Security =====
        // criar usuários, permissões, roles, etc.
        // Exemplo:
        // if (usuarioRepository.count() == 0) { criar usuário master }
    }
}
