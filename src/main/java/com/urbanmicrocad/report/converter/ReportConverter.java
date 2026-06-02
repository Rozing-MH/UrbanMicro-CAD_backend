package com.urbanmicrocad.report.converter;

import com.urbanmicrocad.report.dto.ReportDetailDTO;
import com.urbanmicrocad.report.dto.ReportSummary;
import com.urbanmicrocad.report.entity.EvaluationReport;
import com.urbanmicrocad.report.service.LosCalculator;
import org.springframework.stereotype.Component;

/**
 * 报表 Entity ↔ DTO 转换器。
 * 对齐设计文档 3.2 后端包图 — Converter 转换层。
 */
@Component
public class ReportConverter {

    public ReportSummary toSummary(EvaluationReport report) {
        double avgDelay = LosCalculator.averageDelay(report.getIntersectionLos());
        return new ReportSummary(
            report.getId(),
            report.getProjectId(),
            report.getGeneratedAt(),
            LosCalculator.losGrade(avgDelay),
            avgDelay
        );
    }

    public ReportDetailDTO toDetail(EvaluationReport report) {
        double avgDelay = LosCalculator.averageDelay(report.getIntersectionLos());
        return new ReportDetailDTO(
            report.getId(),
            report.getProjectId(),
            report.getGeneratedAt(),
            LosCalculator.losGrade(avgDelay),
            avgDelay,
            report.getLaneMetrics(),
            report.getIntersectionLos(),
            report.getHeatmapConfig(),
            report.getFlightLineData()
        );
    }
}
