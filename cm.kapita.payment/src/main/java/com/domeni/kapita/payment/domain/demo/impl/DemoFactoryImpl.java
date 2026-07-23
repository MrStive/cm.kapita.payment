package com.domeni.kapita.payment.domain.demo.impl;

import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoData;
import com.domeni.kapita.payment.domain.demo.DemoFactory;
import com.domeni.kapita.payment.domain.demo.DemoName;
import com.domeni.kapita.payment.domain.demo.DemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoFactoryImpl implements DemoFactory {
  private final DemoRepository demoRepository;

  @Override
  public Demo create(DemoData data) {
    Demo demo = Demo.builder().name(new DemoName(data.name())).build();
    return demoRepository.save(demo);
  }
}
