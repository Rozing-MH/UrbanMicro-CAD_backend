package com.urbanmicrocad.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.config.JsonNodeTypeHandler;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.project.service.ProjectService;
import com.urbanmicrocad.report.dto.ExportReportRequest;
import com.urbanmicrocad.report.dto.ReportSummary;
import com.urbanmicrocad.report.entity.EvaluationReport;
import com.urbanmicrocad.report.mapper.EvaluationReportMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {
    private static final int MAX_LIST_SIZE = 100;
    private static final int MAX_REPORT_JSON_CHARS = 2_000_000;

    private final EvaluationReportMapper reportMapper;
    private final ProjectService projectService;

    public ReportService(EvaluationReportMapper reportMapper, ProjectService projectService) {
        this.reportMapper = reportMapper;
        this.projectService = projectService;
    }

    @Transactional
    public ReportSummary generate(CurrentUser user, ExportReportRequest request) {
        validatePayloadSize(request);
        return createAndSave(user, request);
    }

    public List<ReportSummary> list(CurrentUser user, UUID projectId) {
        projectService.requireProject(user, projectId);
        return reportMapper.selectPage(new Page<>(1, MAX_LIST_SIZE), new LambdaQueryWrapper<EvaluationReport>()
                .eq(EvaluationReport::getUserId, user.id())
                .eq(EvaluationReport::getProjectId, projectId)
                .eq(EvaluationReport::getIsDeleted, false)
                .orderByDesc(EvaluationReport::getGeneratedAt))
            .getRecords()
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public ResponseEntity<byte[]> export(CurrentUser user, ExportReportRequest request) {
        validatePayloadSize(request);
        if ("PDF".equalsIgnoreCase(request.format())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "PDF 报表导出将在后续版本实现，请先使用 CSV。 ");
        }
        ReportSummary summary = createAndSave(user, request);
        byte[] csv = csvFor(summary, request).getBytes(StandardCharsets.UTF_8);
        return csvResponse(csv, "urbanmicro-report-" + summary.id() + ".csv");
    }

    public ResponseEntity<byte[]> download(CurrentUser user, UUID reportId) {
        EvaluationReport report = reportMapper.selectOne(new LambdaQueryWrapper<EvaluationReport>()
            .eq(EvaluationReport::getId, reportId)
            .eq(EvaluationReport::getUserId, user.id())
            .eq(EvaluationReport::getIsDeleted, false));
        if (report == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "报表不存在");
        }
        ReportSummary summary = toSummary(report);
        byte[] csv = csvFor(summary, null).getBytes(StandardCharsets.UTF_8);
        return csvResponse(csv, "urbanmicro-report-" + report.getId() + ".csv");
    }

    private ReportSummary createAndSave(CurrentUser user, ExportReportRequest request) {
        projectService.requireProject(user, request.projectId());
        EvaluationReport report = createReport(user, request);
        reportMapper.insert(report);
        return toSummary(report);
    }

    private EvaluationReport createReport(CurrentUser user, ExportReportRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        EvaluationReport report = new EvaluationReport();
        report.setId(UUID.randomUUID());
        report.setProjectId(request.projectId());
        report.setUserId(user.id());
        report.setLaneMetrics(firstNonNull(request.laneMetrics(), request.metrics()));
        report.setIntersectionLos(firstNonNull(request.intersectionLos(), JsonNodeTypeHandler.emptyObject()));
        report.setHeatmapConfig(firstNonNull(request.heatmapConfig(), JsonNodeTypeHandler.emptyObject()));
        report.setFlightLineData(firstNonNull(request.flightLineData(), JsonNodeTypeHandler.emptyObject()));
        report.setGeneratedAt(now);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        report.setIsDeleted(false);
        return report;
    }

    private ReportSummary toSummary(EvaluationReport report) {
        return new ReportSummary(
            report.getId(),
            report.getProjectId(),
            report.getGeneratedAt(),
            "C",
            0
        );
    }

    private ResponseEntity<byte[]> csvResponse(byte[] csv, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    private String csvFor(ReportSummary summary, ExportReportRequest request) {
        String metrics = request == null || request.metrics() == null ? "" : escape(request.metrics().toString());
        return "reportId,projectId,createdAt,networkLOS,averageDelay,metrics\n"
            + escape(summary.id().toString()) + ","
            + escape(summary.projectId().toString()) + ","
            + escape(summary.createdAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)) + ","
            + escape(summary.networkLOS()) + ","
            + summary.averageDelay() + ","
            + metrics
            + "\n";
    }

    private String escape(String value) {
        if (value == null) return "";
        String safeValue = neutralizeFormula(value);
        return '"' + safeValue.replace("\"", "\"\"") + '"';
    }

    private String neutralizeFormula(String value) {
        if (value.isEmpty()) return value;
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }

    private void validatePayloadSize(ExportReportRequest request) {
        int totalChars = jsonChars(request.metrics())
            + jsonChars(request.laneMetrics())
            + jsonChars(request.intersectionLos())
            + jsonChars(request.heatmapConfig())
            + jsonChars(request.flightLineData())
            + jsonChars(request.snapshot());
        if (totalChars > MAX_REPORT_JSON_CHARS) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "报表数据过大");
        }
    }

    private int jsonChars(JsonNode node) {
        return node == null ? 0 : node.toString().length();
    }

    private JsonNode firstNonNull(JsonNode primary, JsonNode fallback) {
        return primary == null ? fallback : primary;
    }
}
