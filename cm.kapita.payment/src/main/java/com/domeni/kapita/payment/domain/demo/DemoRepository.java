package com.domeni.kapita.payment.domain.demo;

import java.util.List;
import java.util.Optional;

public interface DemoRepository {
    Demo save(Demo demo);

    List<Demo> findAll();

    Optional<Demo> findById(DemoId id);
}
