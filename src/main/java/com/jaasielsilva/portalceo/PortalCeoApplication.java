package com.jaasielsilva.portalceo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PortalCeoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalCeoApplication.class, args);

    }
}
  