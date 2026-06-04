package com.domeni.kapita.payment.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.domeni.kapita.generated.payment.dto.CreateDemoDTO;
import com.domeni.kapita.generated.payment.dto.DemoDTO;
import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoData;
import com.domeni.kapita.payment.domain.demo.DemoId;
import com.domeni.kapita.payment.domain.demo.DemoName;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class DemoMapperTest {

    private final DemoMapper demoMapper = Mappers.getMapper(DemoMapper.class);

    @Test
    void mapCreateDemoDtoShouldReturnDemoDataWithNameTest() {
        // Given
        CreateDemoDTO input = new CreateDemoDTO().name("demo-name");

        // When
        DemoData result = demoMapper.map(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("demo-name");
    }

    @Test
    void mapCreateDemoDtoWhenInputIsNullShouldReturnNullTest() {
        // When
        DemoData result = demoMapper.map((CreateDemoDTO) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void mapDemoShouldReturnDemoDtoWithIdAndNameTest() {
        // Given
        UUID expectedId = UUID.randomUUID();
        Demo input = new Demo();
        input.setId(new DemoId(expectedId.toString()));
        input.setName(new DemoName("demo-name"));

        // When
        DemoDTO result = demoMapper.map(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getName()).isEqualTo("demo-name");
    }

    @Test
    void mapDemoWhenDemoNameIsNullShouldReturnDtoWithNullNameTest() {
        // Given
        UUID expectedId = UUID.randomUUID();
        Demo input = new Demo();
        input.setId(new DemoId(expectedId.toString()));
        input.setName(null);

        // When
        DemoDTO result = demoMapper.map(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getName()).isNull();
    }
}
