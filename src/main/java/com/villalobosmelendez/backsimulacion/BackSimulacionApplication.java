package com.villalobosmelendez.backsimulacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackSimulacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackSimulacionApplication.class, args);
    }

}
