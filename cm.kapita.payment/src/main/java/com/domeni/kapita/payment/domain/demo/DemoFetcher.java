package com.domeni.kapita.payment.domain.demo;

import java.util.List;
import java.util.UUID;

public interface DemoFetcher {
  List<Demo> loadAllDemos();

  Demo getById(UUID id);
}
