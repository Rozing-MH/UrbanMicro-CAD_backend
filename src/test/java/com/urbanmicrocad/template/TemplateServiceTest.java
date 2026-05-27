package com.urbanmicrocad.template;

import com.urbanmicrocad.template.mapper.TemplateMapper;
import com.urbanmicrocad.template.service.TemplateService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TemplateServiceTest {

    @Test
    void returnsEmptyListForUnknownCategoryWithoutQueryingDatabase() {
        TemplateMapper mapper = mock(TemplateMapper.class);
        TemplateService service = new TemplateService(mapper);

        assertThat(service.list("ROAD_SECTION")).isEqualTo(List.of());
        verifyNoInteractions(mapper);
    }
}
