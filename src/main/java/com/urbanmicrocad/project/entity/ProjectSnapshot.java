package com.urbanmicrocad.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.config.JsonNodeTypeHandler;
import com.urbanmicrocad.common.config.UuidTypeHandler;

import java.time.OffsetDateTime;
import java.util.UUID;

@TableName(value = "prj_snapshot", autoResultMap = true)
public class ProjectSnapshot {
    @TableId(type = IdType.INPUT)
    private UUID id;
    @TableField(typeHandler = UuidTypeHandler.class)
    private UUID projectId;
    private Integer version;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode snapshotData;
    private String description;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public JsonNode getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(JsonNode snapshotData) {
        this.snapshotData = snapshotData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
