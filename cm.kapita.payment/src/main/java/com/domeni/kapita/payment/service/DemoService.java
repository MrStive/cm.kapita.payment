package com.domeni.kapita.payment.service;

import com.domeni.kapita.generated.payment.dto.CreateDemoDTO;
import com.domeni.kapita.generated.payment.dto.DemoDTO;
import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoData;
import com.domeni.kapita.payment.domain.demo.DemoFactory;
import com.domeni.kapita.payment.domain.demo.DemoFetcher;
import com.domeni.kapita.payment.domain.exception.InvalidDemoPayloadException;
import com.domeni.kapita.payment.service.mapper.DemoMapper;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DemoService {
    private final DemoFactory demoFactory;
    private final DemoFetcher demoFetcher;
    private final DemoMapper demoMapper;

    @Transactional
    public UUID createDemo(CreateDemoDTO data) {
        if (data == null) {
            throw new InvalidDemoPayloadException("create demo payload is required");
        }

        DemoData mappedData = demoMapper.map(data);
        if (mappedData == null || mappedData.name() == null || mappedData.name().isBlank()) {
            throw new InvalidDemoPayloadException("create demo payload is invalid");
        }

        Demo createdDemo = demoFactory.create(mappedData);
        if (createdDemo == null
                || createdDemo.getId() == null
                || createdDemo.getId().getValue() == null
                || createdDemo.getId().getValue().isBlank()) {
            throw new IllegalStateException("created demo has no identifier");
        }

        try {
            return UUID.fromString(createdDemo.getId().getValue());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("created demo id is not a valid UUID", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<DemoDTO> fetchAllDemos() {
        return demoFetcher.loadAllDemos().stream().map(demoMapper::map).toList();
    }

    @Transactional(readOnly = true)
    public DemoDTO getByDemoId(UUID demoId) {
        return demoMapper.map(demoFetcher.getById(demoId));
    }
}
