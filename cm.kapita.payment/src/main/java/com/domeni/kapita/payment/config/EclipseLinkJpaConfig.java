package com.domeni.kapita.payment.config;

import com.domeni.kapita.jpa.autoconfigure.EnableKapitaJpaRepositories;
import com.domeni.kapita.payment.repositories.DemoSpringRepository;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKapitaJpaRepositories(basePackageClasses = DemoSpringRepository.class)
public class EclipseLinkJpaConfig {}
