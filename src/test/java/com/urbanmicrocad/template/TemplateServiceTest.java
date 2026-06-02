package com.urbanmicrocad.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.converter.TemplateConverter;
import com.urbanmicrocad.template.mapper.TemplateMapper;
import com.urbanmicrocad.template.service.TemplateService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TemplateServiceTest {

    @Test
    void returnsBuiltInRoadSectionTemplatesWithoutQueryingDatabase() {
        TemplateMapper mapper = mock(TemplateMapper.class);
        TemplateService service = new TemplateService(mapper, new TemplateConverter());

        List<TemplateDTO> templates = service.list("ROAD_SECTION", null);

        assertThat(templates).hasSizeGreaterThanOrEqualTo(3);
        assertThat(templates)
            .allSatisfy(template -> {
                assertThat(template.category()).isEqualTo("ROAD_SECTION");
                assertThat(template.profile()).isNotNull();
                assertThat(template.profile().hasNonNull("lanes")).isTrue();
                assertThat(template.profile().hasNonNull("median")).isTrue();
                assertThat(template.profile().hasNonNull("sidewalk")).isTrue();
                assertThat(template.profile().hasNonNull("totalWidth")).isTrue();
                assertProfileWidthIsConsistent(template.profile());
            });
        verifyNoInteractions(mapper);
    }

    @Test
    void returnsCrossSectionByProfileId() {
        TemplateMapper mapper = mock(TemplateMapper.class);
        TemplateService service = new TemplateService(mapper, new TemplateConverter());

        assertThat(service.getCrossSection("default-2lane").get("id").asText()).isEqualTo("default-2lane");
        verifyNoInteractions(mapper);
    }

    @Test
    void throwsNotFoundForUnknownCrossSectionId() {
        TemplateMapper mapper = mock(TemplateMapper.class);
        TemplateService service = new TemplateService(mapper, new TemplateConverter());

        assertThatThrownBy(() -> service.getCrossSection("missing-profile"))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("断面模板不存在");
        verifyNoInteractions(mapper);
    }

    @Test
    void returnsEmptyListForUnknownCategoryWithoutQueryingDatabase() {
        TemplateMapper mapper = mock(TemplateMapper.class);
        TemplateService service = new TemplateService(mapper, new TemplateConverter());

        assertThat(service.list("UNKNOWN_CATEGORY", null)).isEqualTo(List.of());
        verifyNoInteractions(mapper);
    }

    private static void assertProfileWidthIsConsistent(JsonNode profile) {
        double laneWidth = 0;
        for (JsonNode lane : profile.get("lanes")) {
            laneWidth += lane.get("width").asDouble();
        }
        double medianWidth = profile.get("median").get("width").asDouble();
        double sidewalkWidth =
            profile.get("sidewalk").get("leftWidth").asDouble()
                + profile.get("sidewalk").get("rightWidth").asDouble();

        assertThat(profile.get("totalWidth").asDouble()).isCloseTo(laneWidth + medianWidth + sidewalkWidth, within(0.0001));
    }
}
