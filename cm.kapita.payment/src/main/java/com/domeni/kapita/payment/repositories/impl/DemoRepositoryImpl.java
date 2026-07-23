package com.domeni.kapita.payment.repositories.impl;

import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoId;
import com.domeni.kapita.payment.domain.demo.DemoRepository;
import com.domeni.kapita.payment.repositories.DemoSpringRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DemoRepositoryImpl implements DemoRepository {
  private final DemoSpringRepository demoSpringRepository;

  @Override
  public Demo save(Demo demo) {
    return demoSpringRepository.save(demo);
  }

  @Override
  public List<Demo> findAll() {
    return demoSpringRepository.findAll();
  }

  @Override
  public Optional<Demo> findById(DemoId id) {
    return demoSpringRepository.findById(id);
  }
}
