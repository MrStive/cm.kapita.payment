package com.domeni.kapita.payment.service.mapper;

import com.domeni.kapita.generated.payment.dto.CreateDemoDTO;
import com.domeni.kapita.generated.payment.dto.DemoDTO;
import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DemoMapper {

    DemoData map(CreateDemoDTO dto);

    @Mapping(target = "id", expression = "java(java.util.UUID.fromString(demo.getId().getValue()))")
    @Mapping(target = "name", source = "name.value")
    DemoDTO map(Demo demo);
}
