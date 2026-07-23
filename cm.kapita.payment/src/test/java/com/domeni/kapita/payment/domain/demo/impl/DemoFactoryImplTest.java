package com.domeni.kapita.payment.domain.demo.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoData;
import com.domeni.kapita.payment.domain.demo.DemoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoFactoryImplTest {

  @Mock private DemoRepository demoRepository;

  @InjectMocks private DemoFactoryImpl demoFactory;

  @Test
  void createShouldBuildAndPersistDemoFromDemoDataTest() {
    // Given
    DemoData input = new DemoData("demo-name");
    Demo persistedDemo = new Demo();
    given(demoRepository.save(any(Demo.class))).willReturn(persistedDemo);

    // When
    Demo result = demoFactory.create(input);

    // Then
    assertThat(result).isSameAs(persistedDemo);

    ArgumentCaptor<Demo> demoCaptor = ArgumentCaptor.forClass(Demo.class);
    then(demoRepository).should().save(demoCaptor.capture());

    Demo demoToSave = demoCaptor.getValue();
    assertThat(demoToSave.getId()).isNotNull();
    assertThat(demoToSave.getId().getValue()).isNotBlank();
    assertThat(demoToSave.getName()).isNotNull();
    assertThat(demoToSave.getName().getValue()).isEqualTo("demo-name");
  }

  @Test
  void createWhenDemoDataIsNullShouldThrowNullPointerExceptionTest() {
    // When / Then
    assertThatThrownBy(() -> demoFactory.create(null)).isInstanceOf(NullPointerException.class);

    verifyNoInteractions(demoRepository);
  }
}
