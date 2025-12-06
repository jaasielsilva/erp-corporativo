package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev")
public class DevCnpjTestDataConfig {

    @Bean
    public CommandLineRunner seedClientes(ClienteRepository clienteRepository) {
        return args -> {
            if (clienteRepository.countByTipoClienteIgnoreCase("PJ") > 0) {
                return;
            }
            List<String> amostra = List.of(
                    "00000000000000",
                    "11111111111111",
                    "22222222222222",
                    "123",
                    "12345678901234",
                    "12.345.678/0001-95",
                    "27.865.757/0001-02",
                    "04.252.011/0001-10",
                    "40.688.134/0001-61",
                    "33.000.167/0001-01",
                    "07.575.651/0001-56",
                    "19.131.243/0001-97",
                    "27865757000102",
                    "04252011000110",
                    "40688134000161",
                    "33000167000101",
                    "07575651000156",
                    "19131243000197"
            );
            int i = 0;
            for (String cnpj : amostra) {
                Cliente c = new Cliente();
                c.setNome("Cliente PJ " + (++i));
                c.setEmail("cliente" + i + "@teste.com");
                c.setTelefone("(11) 90000-000" + (i % 10));
                c.setCpfCnpj(cnpj);
                c.setTipoCliente("PJ");
                c.setStatus("Ativo");
                c.setAtivo(true);
                clienteRepository.save(c);
            }
        };
    }
}
