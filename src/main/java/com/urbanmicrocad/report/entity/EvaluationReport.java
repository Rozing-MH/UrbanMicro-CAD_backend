package com.urbanmicrocad.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.config.JsonNodeTypeHandler;
import com.urbanmicrocad.common.config.UuidTypeHandler;

import java.time.OffsetDateTime;
import java.util.UUID;

@TableName(value = "rpt_evaluation_report", autoResultMap = true)
public class EvaluationReport {
    @TableId(type = IdType.INPUT)
    @TableField(typeHandler = UuidTypeHandler.class)
    private UUID id;
    @TableField(typeHandler = UuidTypeHandler.class)
    private UUID projectId;
    private Long userId;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode laneMetrics;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode intersectionLos;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode heatmapConfig;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode flightLineData;
    private OffsetDateTime generatedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Boolean isDeleted;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public JsonNode getLaneMetrics() {
        return laneMetrics;
    }

    public void setLaneMetrics(JsonNode laneMetrics) {
        this.laneMetrics = laneMetrics;
    }

    public JsonNode getIntersectionLos() {
        return intersectionLos;
    }

    public void setIntersectionLos(JsonNode intersectionLos) {
        this.intersectionLos = intersectionLos;
    }

    public JsonNode getHeatmapConfig() {
        return heatmapConfig;
    }

    public void setHeatmapConfig(JsonNode heatmapConfig) {
        this.heatmapConfig = heatmapConfig;
    }

    public JsonNode getFlightLineData() {
        return flightLineData;
    }

    public void setFlightLineData(JsonNode flightLineData) {
        this.flightLineData = flightLineData;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
