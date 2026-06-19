package com.domeni.kapita.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class KapitaPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KapitaPaymentApplication.class, args);
    }
}
