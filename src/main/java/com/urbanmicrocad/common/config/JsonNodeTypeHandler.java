package com.urbanmicrocad.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
        String dbProductName = ps.getConnection().getMetaData().getDatabaseProductName();
        if ("PostgreSQL".equalsIgnoreCase(dbProductName)) {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(parameter.toString());
            ps.setObject(i, jsonObject);
        } else {
            ps.setString(i, parameter.toString());
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    public static ObjectNode emptyObject() {
        return OBJECT_MAPPER.createObjectNode();
    }

    private JsonNode parse(String value) throws SQLException {
        if (value == null || value.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (JsonProcessingException ex) {
            throw new SQLException("Invalid JSONB value", ex);
        }
    }
}
