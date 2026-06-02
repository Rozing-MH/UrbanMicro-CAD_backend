package com.urbanmicrocad.report.service;

import com.urbanmicrocad.common.response.PageResponse;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.report.dto.ExportReportRequest;
import com.urbanmicrocad.report.dto.ReportDetailDTO;
import com.urbanmicrocad.report.dto.ReportSummary;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * 报表服务接口。
 * 对齐设计文档 3.2 后端包图 — Service 接口化。
 */
public interface IReportService {

    /**
     * 生成并保存报表摘要。
     */
    ReportSummary generate(CurrentUser user, ExportReportRequest request);

    /**
     * 工程报表列表（分页）。
     */
    PageResponse<ReportSummary> list(CurrentUser user, UUID projectId, int page, int size);

    /**
     * 导出报表（CSV 或 PDF）。
     */
    ResponseEntity<byte[]> export(CurrentUser user, ExportReportRequest request);

    /**
     * 下载已有报表 CSV。
     */
    ResponseEntity<byte[]> download(CurrentUser user, UUID reportId);

    /**
     * 下载已有报表 PDF。
     */
    ResponseEntity<byte[]> downloadPdf(CurrentUser user, UUID reportId);

    /**
     * 报表详情（含完整指标数据）。
     *
     * @throws com.urbanmicrocad.common.exception.ApiException 报表不存在时抛 NOT_FOUND
     */
    ReportDetailDTO detail(CurrentUser user, UUID reportId);
}
