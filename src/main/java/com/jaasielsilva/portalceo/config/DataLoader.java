package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final CargoRepository cargoRepository;

    public DataLoader(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> cargosIniciais = Arrays.asList(
                "Técnico de Suporte",
                "Analista de Sistemas",
                "Desenvolvedor Senior",
                "Gerente de Projetos",
                "Recursos Humanos",
                "Financeiro",
                "Administrativo"
        );

        for (String nomeCargo : cargosIniciais) {
            if (cargoRepository.findByNome(nomeCargo).isEmpty()) {
                Cargo cargo = new Cargo();
                cargo.setNome(nomeCargo);
                cargo.setAtivo(true);
                cargoRepository.save(cargo);
                System.out.println("Cargo criado: " + nomeCargo);
            }
        }
    }
}