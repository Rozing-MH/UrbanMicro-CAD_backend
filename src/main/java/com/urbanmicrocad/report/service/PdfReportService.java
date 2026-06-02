package com.urbanmicrocad.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.urbanmicrocad.report.dto.ExportReportRequest;
import com.urbanmicrocad.report.dto.ReportSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * PDF 报表生成服务。
 * <p>
 * 对齐设计文档 FR6.5：导出 PDF 格式图文评估简报，含热力图截图、LOS 评级表、车道指标表。
 */
@Service
public class PdfReportService {

    private static final Logger log = LoggerFactory.getLogger(PdfReportService.class);
    private static final String FONT_PATH = "fonts/SimHei.ttf";
    private static final String FALLBACK_FONT = "Helvetica";

    private final Font titleFont;
    private final Font headingFont;
    private final Font bodyFont;
    private final Font tableHeaderFont;
    private final Font tableCellFont;

    public PdfReportService() {
        BaseFont bf = loadChineseBaseFont();
        this.titleFont = new Font(bf, 22, Font.BOLD);
        this.headingFont = new Font(bf, 14, Font.BOLD);
        this.bodyFont = new Font(bf, 10, Font.NORMAL);
        this.tableHeaderFont = new Font(bf, 10, Font.BOLD);
        this.tableCellFont = new Font(bf, 9, Font.NORMAL);
    }

    /**
     * 生成 PDF 报表字节数组。
     *
     * @param request 导出请求（含指标数据）
     * @param summary 报表摘要
     * @return PDF 字节数组
     */
    public byte[] generatePdf(ExportReportRequest request, ReportSummary summary) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitlePage(document, request, summary);
            addEvaluationSummary(document, summary);
            addIntersectionLosTable(document, request.intersectionLos());
            addLaneMetricsTable(document, request.laneMetrics());
            addHeatmapImage(document, request.snapshot());

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF 生成失败: reportId={}", summary.id(), e);
            throw new IllegalStateException("PDF 生成失败", e);
        }
    }

    private void addTitlePage(Document document, ExportReportRequest request, ReportSummary summary) throws Exception {
        Paragraph title = new Paragraph("城市微观交通评估报告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(30);
        document.add(title);

        document.add(spacedParagraph("报告 ID: " + summary.id(), bodyFont, 10));
        document.add(spacedParagraph("工程 ID: " + summary.projectId(), bodyFont, 10));
        document.add(spacedParagraph("生成时间: " + formatDateTime(summary.createdAt()), bodyFont, 10));
        if (request.format() != null) {
            document.add(spacedParagraph("导出格式: " + request.format(), bodyFont, 10));
        }
        document.add(Chunk.NEWLINE);
    }

    private void addEvaluationSummary(Document document, ReportSummary summary) throws Exception {
        document.add(new Paragraph("评估概要", headingFont));
        document.add(Chunk.NEWLINE);

        Table summaryTable = new Table(2);
        summaryTable.setWidth(60);
        summaryTable.setBorderWidth(1);
        summaryTable.setPadding(5);

        summaryTable.addCell(headerCell("指标"));
        summaryTable.addCell(headerCell("数值"));

        summaryTable.addCell(cell("网络 LOS"));
        summaryTable.addCell(cell(summary.networkLOS()));

        summaryTable.addCell(cell("平均延误 (s/veh)"));
        summaryTable.addCell(cell(String.valueOf(summary.averageDelay())));

        document.add(summaryTable);
        document.add(Chunk.NEWLINE);
    }

    private void addIntersectionLosTable(Document document, JsonNode intersectionLos) throws Exception {
        if (intersectionLos == null || !intersectionLos.isObject() && !intersectionLos.isArray()) {
            return;
        }
        if (intersectionLos.size() == 0) {
            return;
        }

        document.add(new Paragraph("交叉口 LOS 评级", headingFont));
        document.add(Chunk.NEWLINE);

        Table losTable = new Table(3);
        losTable.setWidth(80);
        losTable.setBorderWidth(1);
        losTable.setPadding(5);

        losTable.addCell(headerCell("交叉口 ID"));
        losTable.addCell(headerCell("LOS 等级"));
        losTable.addCell(headerCell("平均延误 (s/veh)"));

        if (intersectionLos.isObject()) {
            var fields = intersectionLos.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                String nodeId = entry.getKey();
                JsonNode data = entry.getValue();
                losTable.addCell(cell(nodeId));
                losTable.addCell(cell(textField(data, "los")));
                losTable.addCell(cell(textField(data, "delay")));
            }
        } else if (intersectionLos.isArray()) {
            for (JsonNode item : intersectionLos) {
                losTable.addCell(cell(textField(item, "id")));
                losTable.addCell(cell(textField(item, "los")));
                losTable.addCell(cell(textField(item, "delay")));
            }
        }

        document.add(losTable);
        document.add(Chunk.NEWLINE);
    }

    private void addLaneMetricsTable(Document document, JsonNode laneMetrics) throws Exception {
        if (laneMetrics == null || !laneMetrics.isObject() && !laneMetrics.isArray()) {
            return;
        }
        if (laneMetrics.size() == 0) {
            return;
        }

        document.add(new Paragraph("车道级指标", headingFont));
        document.add(Chunk.NEWLINE);

        Table metricsTable = new Table(5);
        metricsTable.setWidth(90);
        metricsTable.setBorderWidth(1);
        metricsTable.setPadding(5);

        metricsTable.addCell(headerCell("车道 ID"));
        metricsTable.addCell(headerCell("流量 (pcu/h)"));
        metricsTable.addCell(headerCell("速度 (km/h)"));
        metricsTable.addCell(headerCell("密度 (veh/km)"));
        metricsTable.addCell(headerCell("排队长度 (m)"));

        if (laneMetrics.isObject()) {
            var fields = laneMetrics.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                JsonNode data = entry.getValue();
                metricsTable.addCell(cell(entry.getKey()));
                metricsTable.addCell(cell(textField(data, "flow")));
                metricsTable.addCell(cell(textField(data, "speed")));
                metricsTable.addCell(cell(textField(data, "density")));
                metricsTable.addCell(cell(textField(data, "queueLength")));
            }
        } else if (laneMetrics.isArray()) {
            for (JsonNode item : laneMetrics) {
                metricsTable.addCell(cell(textField(item, "id")));
                metricsTable.addCell(cell(textField(item, "flow")));
                metricsTable.addCell(cell(textField(item, "speed")));
                metricsTable.addCell(cell(textField(item, "density")));
                metricsTable.addCell(cell(textField(item, "queueLength")));
            }
        }

        document.add(metricsTable);
        document.add(Chunk.NEWLINE);
    }

    private void addHeatmapImage(Document document, JsonNode snapshot) throws Exception {
        if (snapshot == null) {
            return;
        }
        String base64Image = findBase64Image(snapshot);
        if (base64Image == null) {
            return;
        }

        document.add(new Paragraph("热力图截图", headingFont));
        document.add(Chunk.NEWLINE);

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            Image image = Image.getInstance(imageBytes);
            float maxWidth = document.getPageSize().getWidth() - 100;
            float maxHeight = 300;
            image.scaleToFit(maxWidth, maxHeight);
            image.setAlignment(Element.ALIGN_CENTER);
            document.add(image);
            document.add(Chunk.NEWLINE);
        } catch (Exception e) {
            log.warn("热力图图片嵌入失败，跳过: {}", e.getMessage());
            document.add(new Paragraph("（热力图截图无法嵌入）", bodyFont));
        }
    }

    private String findBase64Image(JsonNode node) {
        if (node == null) {
            return null;
        }
        // Search common field names for base64 image data
        for (String field : new String[]{"heatmapImage", "screenshot", "image", "heatmapBase64"}) {
            JsonNode imageNode = node.get(field);
            if (imageNode != null && imageNode.isValueNode() && !imageNode.asText().isBlank()) {
                return imageNode.asText();
            }
        }
        // Recursively search one level deep
        var fields = node.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            if (entry.getValue().isObject()) {
                for (String field : new String[]{"heatmapImage", "screenshot", "image", "heatmapBase64"}) {
                    JsonNode imageNode = entry.getValue().get(field);
                    if (imageNode != null && imageNode.isValueNode() && !imageNode.asText().isBlank()) {
                        return imageNode.asText();
                    }
                }
            }
        }
        return null;
    }

    // --- Helper methods ---

    private Paragraph spacedParagraph(String text, Font font, float spacingAfter) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingAfter(spacingAfter);
        return p;
    }

    private Cell headerCell(String text) {
        Cell cell = new Cell(new Phrase(text, tableHeaderFont));
        cell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        return cell;
    }

    private Cell cell(String text) {
        return new Cell(new Phrase(text != null ? text : "-", tableCellFont));
    }

    private String textField(JsonNode node, String field) {
        if (node == null || !node.has(field)) {
            return "-";
        }
        JsonNode value = node.get(field);
        if (value.isNumber()) {
            return String.valueOf(value.doubleValue());
        }
        return value.asText("-");
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private BaseFont loadChineseBaseFont() {
        // 1. Try loading bundled font from classpath (development)
        ClassPathResource fontResource = new ClassPathResource(FONT_PATH);
        if (fontResource.exists()) {
            try (InputStream is = fontResource.getInputStream()) {
                byte[] fontBytes = is.readAllBytes();
                return BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, true, fontBytes, null);
            } catch (IOException e) {
                log.warn("类路径中文字体加载失败: {}", e.getMessage());
            }
        }

        // 2. Try system font paths (Docker /app/fonts/ volume or OS font directories)
        String[] systemFontPaths = {
            "/app/fonts/SimHei.ttf",
            "/app/fonts/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/truetype/noto-cjk/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
        };
        for (String path : systemFontPaths) {
            try {
                return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception e) {
                // Font not found at this path, try next
            }
        }

        // 3. Fallback to Helvetica (Chinese characters will render as blank)
        log.warn("未找到中文字体，PDF 中文字符将无法正常显示。"
            + "请在 /app/fonts/ 目录放置 SimHei.ttf 或使用包含 Noto CJK 字体的 Docker 镜像。");
        try {
            return BaseFont.createFont(FALLBACK_FONT, BaseFont.WINANSI, BaseFont.EMBEDDED);
        } catch (Exception e) {
            throw new IllegalStateException("无法加载基础字体", e);
        }
    }
}
