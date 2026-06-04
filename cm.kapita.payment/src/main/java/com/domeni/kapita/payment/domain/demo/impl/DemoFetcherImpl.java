package com.domeni.kapita.payment.domain.demo.impl;

import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoFetcher;
import com.domeni.kapita.payment.domain.demo.DemoId;
import com.domeni.kapita.payment.domain.demo.DemoRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoFetcherImpl implements DemoFetcher {
    private final DemoRepository demoRepository;

    @Override
    public List<Demo> loadAllDemos() {
        return demoRepository.findAll();
    }

    @Override
    public Demo getById(UUID id) {
        return demoRepository
                .findById(new DemoId(id.toString()))
                .orElseThrow(() -> new RuntimeException("Demo not found: " + id));
    }
}
