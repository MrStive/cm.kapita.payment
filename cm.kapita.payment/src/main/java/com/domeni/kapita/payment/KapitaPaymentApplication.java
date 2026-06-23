package com.domeni.kapita.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KapitaPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KapitaPaymentApplication.class, args);
    }
}
