package com.urbanmicrocad.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.report.dto.ExportReportRequest;
import com.urbanmicrocad.report.dto.ReportSummary;
import com.urbanmicrocad.report.service.PdfReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class PdfReportServiceTest {

    private PdfReportService pdfReportService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        pdfReportService = new PdfReportService();
    }

    @Test
    @DisplayName("generatePdf with minimal data produces valid PDF")
    void generatePdf_minimalData_producesValidPdf() {
        ExportReportRequest request = new ExportReportRequest(
            UUID.randomUUID(), "PDF", null, null, null, null, null, null
        );
        ReportSummary summary = new ReportSummary(
            UUID.randomUUID(), request.projectId(), OffsetDateTime.now(), "C", 0
        );

        byte[] pdf = pdfReportService.generatePdf(request, summary);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("generatePdf with intersection LOS data produces valid PDF")
    void generatePdf_withIntersectionLos_producesValidPdf() {
        ObjectNode losData = objectMapper.createObjectNode();
        ObjectNode node1 = losData.putObject("node-1");
        node1.put("los", "B");
        node1.put("delay", 12.5);

        ExportReportRequest request = new ExportReportRequest(
            UUID.randomUUID(), "PDF", null, null, losData, null, null, null
        );
        ReportSummary summary = new ReportSummary(
            UUID.randomUUID(), request.projectId(), OffsetDateTime.now(), "B", 12.5
        );

        byte[] pdf = pdfReportService.generatePdf(request, summary);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("generatePdf with lane metrics data produces valid PDF")
    void generatePdf_withLaneMetrics_producesValidPdf() {
        ObjectNode metricsData = objectMapper.createObjectNode();
        ObjectNode lane1 = metricsData.putObject("lane-1");
        lane1.put("flow", 850.0);
        lane1.put("speed", 45.2);
        lane1.put("density", 18.8);
        lane1.put("queueLength", 120.0);

        ExportReportRequest request = new ExportReportRequest(
            UUID.randomUUID(), "PDF", null, metricsData, null, null, null, null
        );
        ReportSummary summary = new ReportSummary(
            UUID.randomUUID(), request.projectId(), OffsetDateTime.now(), "C", 22.0
        );

        byte[] pdf = pdfReportService.generatePdf(request, summary);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("generatePdf with array-format metrics produces valid PDF")
    void generatePdf_withArrayMetrics_producesValidPdf() {
        ArrayNode losArray = objectMapper.createArrayNode();
        ObjectNode item1 = losArray.addObject();
        item1.put("id", "intersection-A");
        item1.put("los", "A");
        item1.put("delay", 5.2);
        ObjectNode item2 = losArray.addObject();
        item2.put("id", "intersection-B");
        item2.put("los", "D");
        item2.put("delay", 35.8);

        ExportReportRequest request = new ExportReportRequest(
            UUID.randomUUID(), "PDF", null, null, losArray, null, null, null
        );
        ReportSummary summary = new ReportSummary(
            UUID.randomUUID(), request.projectId(), OffsetDateTime.now(), "C", 20.5
        );

        byte[] pdf = pdfReportService.generatePdf(request, summary);

        assertThat(pdf).isNotEmpty();
    }

    @Test
    @DisplayName("generatePdf with full data does not throw")
    void generatePdf_fullData_noException() {
        ObjectNode losData = objectMapper.createObjectNode();
        ObjectNode node1 = losData.putObject("node-1");
        node1.put("los", "C");
        node1.put("delay", 22.0);

        ObjectNode metricsData = objectMapper.createObjectNode();
        ObjectNode lane1 = metricsData.putObject("lane-1");
        lane1.put("flow", 600.0);
        lane1.put("speed", 38.0);
        lane1.put("density", 15.8);
        lane1.put("queueLength", 80.0);

        ExportReportRequest request = new ExportReportRequest(
            UUID.randomUUID(), "PDF", null, metricsData, losData,
            objectMapper.createObjectNode(), objectMapper.createArrayNode(), null
        );
        ReportSummary summary = new ReportSummary(
            UUID.randomUUID(), request.projectId(), OffsetDateTime.now(), "C", 22.0
        );

        assertThatNoException().isThrownBy(() -> pdfReportService.generatePdf(request, summary));
    }

    @Test
    @DisplayName("generatePdf with empty JSON objects produces valid PDF")
    void generatePdf_emptyObjects_producesValidPdf() {
        ExportReportRequest request = new ExportReportRequest(
            UUID.randomUUID(), "PDF", null,
            objectMapper.createObjectNode(),
            objectMapper.createObjectNode(),
            objectMapper.createObjectNode(),
            objectMapper.createArrayNode(),
            objectMapper.createObjectNode()
        );
        ReportSummary summary = new ReportSummary(
            UUID.randomUUID(), request.projectId(), OffsetDateTime.now(), "A", 0
        );

        byte[] pdf = pdfReportService.generatePdf(request, summary);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }
}
