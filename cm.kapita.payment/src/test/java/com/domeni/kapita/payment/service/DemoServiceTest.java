package com.domeni.kapita.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.domeni.kapita.generated.payment.dto.CreateDemoDTO;
import com.domeni.kapita.generated.payment.dto.DemoDTO;
import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoData;
import com.domeni.kapita.payment.domain.demo.DemoFactory;
import com.domeni.kapita.payment.domain.demo.DemoFetcher;
import com.domeni.kapita.payment.domain.demo.DemoId;
import com.domeni.kapita.payment.domain.exception.InvalidDemoPayloadException;
import com.domeni.kapita.payment.service.mapper.DemoMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock private DemoFactory demoFactory;

    @Mock private DemoFetcher demoFetcher;

    @Mock private DemoMapper demoMapper;

    @InjectMocks private DemoService demoService;

    @Test
    void createDemoShouldReturnCreatedDemoIdTest() {
        // Given
        CreateDemoDTO input = new CreateDemoDTO().name("demo");
        DemoData mappedData = new DemoData("demo");
        UUID expectedId = UUID.randomUUID();
        Demo createdDemo = new Demo();
        createdDemo.setId(new DemoId(expectedId.toString()));

        given(demoMapper.map(input)).willReturn(mappedData);
        given(demoFactory.create(mappedData)).willReturn(createdDemo);

        // When
        UUID result = demoService.createDemo(input);

        // Then
        assertThat(result).isEqualTo(expectedId);
        then(demoMapper).should().map(input);
        then(demoFactory).should().create(mappedData);
        verifyNoInteractions(demoFetcher);
    }

    @Test
    void createDemoWhenInputIsNullShouldThrowInvalidDemoPayloadExceptionTest() {
        // Act / Assert
        assertThatThrownBy(() -> demoService.createDemo(null))
                .isInstanceOf(InvalidDemoPayloadException.class)
                .hasMessage("create demo payload is required");

        verifyNoInteractions(demoMapper);
        verifyNoInteractions(demoFactory);
        verifyNoInteractions(demoFetcher);
    }

    @Test
    void createDemoWhenMapperReturnsNullShouldThrowInvalidDemoPayloadExceptionTest() {
        // Given
        CreateDemoDTO input = new CreateDemoDTO().name("demo");
        given(demoMapper.map(input)).willReturn(null);

        // Then / When
        assertThatThrownBy(() -> demoService.createDemo(input))
                .isInstanceOf(InvalidDemoPayloadException.class)
                .hasMessage("create demo payload is invalid");

        then(demoMapper).should().map(input);
        verifyNoInteractions(demoFactory);
        verifyNoInteractions(demoFetcher);
    }

    @Test
    void createDemoWhenMapperReturnsBlankNameShouldThrowInvalidDemoPayloadExceptionTest() {
        // Given
        CreateDemoDTO input = new CreateDemoDTO().name(" ");
        given(demoMapper.map(input)).willReturn(new DemoData(" "));

        // Then / When
        assertThatThrownBy(() -> demoService.createDemo(input))
                .isInstanceOf(InvalidDemoPayloadException.class)
                .hasMessage("create demo payload is invalid");

        then(demoMapper).should().map(input);
        verifyNoInteractions(demoFactory);
        verifyNoInteractions(demoFetcher);
    }

    @Test
    void fetchAllDemosShouldReturnMappedDemoDtosTest() {
        // Given
        Demo firstDemo = new Demo();
        firstDemo.setId(new DemoId(UUID.randomUUID().toString()));
        Demo secondDemo = new Demo();
        secondDemo.setId(new DemoId(UUID.randomUUID().toString()));

        DemoDTO firstDto = new DemoDTO().id(UUID.randomUUID()).name("first");
        DemoDTO secondDto = new DemoDTO().id(UUID.randomUUID()).name("second");

        given(demoFetcher.loadAllDemos()).willReturn(List.of(firstDemo, secondDemo));
        given(demoMapper.map(firstDemo)).willReturn(firstDto);
        given(demoMapper.map(secondDemo)).willReturn(secondDto);

        // When
        List<DemoDTO> result = demoService.fetchAllDemos();

        // Then
        assertThat(result).containsExactly(firstDto, secondDto);
        then(demoFetcher).should().loadAllDemos();
        then(demoMapper).should().map(firstDemo);
        then(demoMapper).should().map(secondDemo);
        verifyNoInteractions(demoFactory);
    }

    @Test
    void getByDemoIdShouldReturnMappedDemoDtoTest() {
        // Given
        UUID demoId = UUID.randomUUID();
        Demo demo = new Demo();
        demo.setId(new DemoId(demoId.toString()));
        DemoDTO expectedDto = new DemoDTO().id(demoId).name("demo");

        given(demoFetcher.getById(demoId)).willReturn(demo);
        given(demoMapper.map(demo)).willReturn(expectedDto);

        // When
        DemoDTO result = demoService.getByDemoId(demoId);

        // Then
        assertThat(result).isEqualTo(expectedDto);
        then(demoFetcher).should().getById(demoId);
        then(demoMapper).should().map(demo);
        verifyNoInteractions(demoFactory);
    }
}
