package com.urbanmicrocad.report.controller;

import com.urbanmicrocad.common.response.ApiResponse;
import com.urbanmicrocad.common.response.PageResponse;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.report.dto.ExportReportRequest;
import com.urbanmicrocad.report.dto.ReportDetailDTO;
import com.urbanmicrocad.report.dto.ReportSummary;
import com.urbanmicrocad.report.service.IReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final IReportService reportService;

    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    public ApiResponse<ReportSummary> generate(
        @AuthenticationPrincipal CurrentUser user,
        @Valid @RequestBody ExportReportRequest request
    ) {
        return ApiResponse.ok(reportService.generate(user, request));
    }

    @GetMapping
    public ApiResponse<PageResponse<ReportSummary>> list(
        @AuthenticationPrincipal CurrentUser user,
        @RequestParam UUID projectId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(reportService.list(user, projectId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportDetailDTO> detail(
        @AuthenticationPrincipal CurrentUser user,
        @PathVariable UUID id
    ) {
        return ApiResponse.ok(reportService.detail(user, id));
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(
        @AuthenticationPrincipal CurrentUser user,
        @Valid @RequestBody ExportReportRequest request
    ) {
        return reportService.export(user, request);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
        return reportService.download(user, id);
    }

    @GetMapping("/{id}/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
        return reportService.downloadPdf(user, id);
    }
}
