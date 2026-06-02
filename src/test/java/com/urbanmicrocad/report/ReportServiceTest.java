package com.urbanmicrocad.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.project.service.IProjectService;
import com.urbanmicrocad.report.converter.ReportConverter;
import com.urbanmicrocad.report.dto.ExportReportRequest;
import com.urbanmicrocad.report.mapper.EvaluationReportMapper;
import com.urbanmicrocad.report.service.PdfReportService;
import com.urbanmicrocad.report.service.ReportService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ReportServiceTest {

    @Test
    void rejectsOversizedReportPayloadBeforeProjectLookup() {
        ObjectMapper objectMapper = new ObjectMapper();
        EvaluationReportMapper reportMapper = mock(EvaluationReportMapper.class);
        IProjectService projectService = mock(IProjectService.class);
        PdfReportService pdfReportService = mock(PdfReportService.class);
        ReportService service = new ReportService(reportMapper, projectService, pdfReportService, new ReportConverter());
        ExportReportRequest request = new ExportReportRequest(
            UUID.randomUUID(),
            "CSV",
            objectMapper.createObjectNode().put("payload", "x".repeat(2_000_001)),
            null,
            null,
            null,
            null,
            null
        );

        assertThatThrownBy(() -> service.generate(new CurrentUser(1L, "demo", "USER"), request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("报表数据过大");
        verifyNoInteractions(projectService, reportMapper, pdfReportService);
    }
}
