package com.jaasielsilva.portalceo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // para o @Scheduled funcionar
@EnableAsync       // para métodos @Async continuarem funcionando
public class PortalCeoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalCeoApplication.class, args);

    }
}
