package com.domeni.kapita.payment.repositories;

import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoSpringRepository extends JpaRepository<Demo, DemoId> {}
