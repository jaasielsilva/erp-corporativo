package com.jaasielsilva.portalceo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.repository.BeneficioRepository;

import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private BeneficioRepository beneficioRepository;

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

        // ===== Aqui vai o código já existente do Spring Security =====
        // criar usuários, permissões, roles, etc.
        // Exemplo:
        // if (usuarioRepository.count() == 0) { criar usuário master }
    }
}
