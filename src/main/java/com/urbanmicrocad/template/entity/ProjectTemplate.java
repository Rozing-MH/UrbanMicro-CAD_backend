package com.urbanmicrocad.template.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.config.JsonNodeTypeHandler;

import java.time.OffsetDateTime;
import java.util.UUID;

@TableName(value = "prj_template", autoResultMap = true)
public class ProjectTemplate {
    @TableId(type = IdType.INPUT)
    private UUID id;
    private String name;
    private String category;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode snapshotData;
    private String thumbnailUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    private Boolean isDeleted;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public JsonNode getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(JsonNode snapshotData) {
        this.snapshotData = snapshotData;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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
