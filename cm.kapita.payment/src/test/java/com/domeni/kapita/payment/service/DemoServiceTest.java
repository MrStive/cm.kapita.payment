package com.domeni.kapita.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoData;
import com.domeni.kapita.payment.domain.demo.DemoFactory;
import com.domeni.kapita.payment.domain.demo.DemoFetcher;
import com.domeni.kapita.payment.domain.demo.DemoId;
import com.domeni.kapita.payment.domain.exception.InvalidDemoPayloadException;
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

  @InjectMocks private DemoService demoService;

  @Test
  void createDemoShouldReturnCreatedDemoIdTest() {
    // Given
    DemoData input = new DemoData("demo");
    UUID expectedId = UUID.randomUUID();
    Demo createdDemo = new Demo();
    createdDemo.setId(new DemoId(expectedId.toString()));

    given(demoFactory.create(input)).willReturn(createdDemo);

    // When
    UUID result = demoService.createDemo(input);

    // Then
    assertThat(result).isEqualTo(expectedId);
    then(demoFactory).should().create(input);
    verifyNoInteractions(demoFetcher);
  }

  @Test
  void createDemoWhenInputIsNullShouldThrowInvalidDemoPayloadExceptionTest() {
    // Act / Assert
    assertThatThrownBy(() -> demoService.createDemo(null))
        .isInstanceOf(InvalidDemoPayloadException.class)
        .hasMessage("create demo payload is invalid");

    verifyNoInteractions(demoFactory);
    verifyNoInteractions(demoFetcher);
  }

  @Test
  void createDemoWhenInputHasBlankNameShouldThrowInvalidDemoPayloadExceptionTest() {
    // Given
    DemoData input = new DemoData(" ");

    // Then / When
    assertThatThrownBy(() -> demoService.createDemo(input))
        .isInstanceOf(InvalidDemoPayloadException.class)
        .hasMessage("create demo payload is invalid");

    verifyNoInteractions(demoFactory);
    verifyNoInteractions(demoFetcher);
  }

  @Test
  void fetchAllDemosShouldReturnDemosTest() {
    // Given
    Demo firstDemo = new Demo();
    firstDemo.setId(new DemoId(UUID.randomUUID().toString()));
    Demo secondDemo = new Demo();
    secondDemo.setId(new DemoId(UUID.randomUUID().toString()));

    given(demoFetcher.loadAllDemos()).willReturn(List.of(firstDemo, secondDemo));

    // When
    List<Demo> result = demoService.fetchAllDemos();

    // Then
    assertThat(result).containsExactly(firstDemo, secondDemo);
    then(demoFetcher).should().loadAllDemos();
    verifyNoInteractions(demoFactory);
  }

  @Test
  void getByDemoIdShouldReturnDemoTest() {
    // Given
    UUID demoId = UUID.randomUUID();
    Demo demo = new Demo();
    demo.setId(new DemoId(demoId.toString()));

    given(demoFetcher.getById(demoId)).willReturn(demo);

    // When
    Demo result = demoService.getByDemoId(demoId);

    // Then
    assertThat(result).isEqualTo(demo);
    then(demoFetcher).should().getById(demoId);
    verifyNoInteractions(demoFactory);
  }
}
