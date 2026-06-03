package com.urbanmicrocad.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.config.JsonNodeTypeHandler;

import java.time.OffsetDateTime;
import java.util.UUID;

@TableName(value = "prj_project", autoResultMap = true)
public class Project {
    @TableId(type = IdType.INPUT)
    private UUID id;
    private Long userId;
    private String name;
    private String description;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode topologyData;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode ruleData;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Boolean isDeleted;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JsonNode getTopologyData() {
        return topologyData;
    }

    public void setTopologyData(JsonNode topologyData) {
        this.topologyData = topologyData;
    }

    public JsonNode getRuleData() {
        return ruleData;
    }

    public void setRuleData(JsonNode ruleData) {
        this.ruleData = ruleData;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
