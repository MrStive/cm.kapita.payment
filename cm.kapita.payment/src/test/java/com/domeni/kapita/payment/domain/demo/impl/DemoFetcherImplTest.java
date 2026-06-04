package com.domeni.kapita.payment.domain.demo.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.domeni.kapita.payment.domain.demo.Demo;
import com.domeni.kapita.payment.domain.demo.DemoId;
import com.domeni.kapita.payment.domain.demo.DemoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoFetcherImplTest {

    @Mock private DemoRepository demoRepository;

    @InjectMocks private DemoFetcherImpl demoFetcher;

    @Test
    void loadAllDemosShouldReturnAllDemosFromRepositoryTest() {
        // Given
        List<Demo> expectedDemos = List.of(new Demo(), new Demo());
        given(demoRepository.findAll()).willReturn(expectedDemos);

        // When
        List<Demo> result = demoFetcher.loadAllDemos();

        // Then
        assertThat(result).isSameAs(expectedDemos);
        then(demoRepository).should().findAll();
    }

    @Test
    void getByIdShouldReturnDemoFromRepositoryWhenFoundTest() {
        // Given
        UUID id = UUID.randomUUID();
        DemoId demoId = new DemoId(id.toString());
        Demo expectedDemo = new Demo();
        given(demoRepository.findById(demoId)).willReturn(Optional.of(expectedDemo));

        // When
        Demo result = demoFetcher.getById(id);

        // Then
        assertThat(result).isSameAs(expectedDemo);
        then(demoRepository).should().findById(demoId);
    }

    @Test
    void getByIdShouldThrowExceptionWhenNotFoundTest() {
        // Given
        UUID id = UUID.randomUUID();
        DemoId demoId = new DemoId(id.toString());
        given(demoRepository.findById(demoId)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> demoFetcher.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Demo not found: " + id);

        then(demoRepository).should().findById(demoId);
    }
}
