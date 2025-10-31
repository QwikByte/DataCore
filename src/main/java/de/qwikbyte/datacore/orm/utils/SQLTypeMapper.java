package de.qwikbyte.datacore.orm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class SQLTypeMapper {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static String mapJavaTypeToSQL(Field field, Class<?> type, boolean generated, String generationType) {
        if (generated && "UUID".equals(generationType)) {
            return "UUID DEFAULT gen_random_uuid()";
        }

        if (type.equals(UUID.class)) return "UUID";
        if (type.equals(String.class)) return "TEXT";
        if (type.equals(int.class) || type.equals(Integer.class))
            return (generated ? "SERIAL" : "INT");
        if (type.equals(long.class) || type.equals(Long.class))
            return (generated ? "BIGSERIAL" : "BIGINT");
        if (type.equals(double.class) || type.equals(Double.class)) return "DOUBLE PRECISION";
        if (type.equals(float.class) || type.equals(Float.class)) return "REAL";
        if (type.equals(boolean.class) || type.equals(Boolean.class)) return "BOOLEAN";
        if (type.equals(short.class) || type.equals(Short.class)) return "SMALLINT";
        if (type.equals(byte.class) || type.equals(Byte.class)) return "SMALLINT";
        if (type.equals(BigDecimal.class)) return "NUMERIC(18,4)";
        if (type.equals(LocalDate.class)) return "DATE";
        if (type.equals(LocalDateTime.class) || type.equals(Instant.class)) return "TIMESTAMP";
        if (type.equals(byte[].class)) return "BYTEA";
        if (type.isEnum()) return "TEXT";
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) return "JSONB";
        if (type.getName().equals("org.json.JSONObject") ||
                type.getName().equals("com.fasterxml.jackson.databind.JsonNode")) return "JSONB";

        return "TEXT"; // fallback
    }

    public static Object toSQLValue(Object value) {
        if (value == null) return null;

        if (value instanceof Enum<?> e) {
            return e.name();
        }

        if (value instanceof Collection<?> || value instanceof Map<?, ?>) {
            try {
                return jsonMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing JSON value", e);
            }
        }

        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object fromSQLValue(ResultSet rs, String column, Field field) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) return null;

        Class<?> type = field.getType();

        if (type.equals(UUID.class)) return UUID.fromString(value.toString());
        if (type.equals(LocalDate.class)) return rs.getDate(column).toLocalDate();
        if (type.equals(LocalDateTime.class)) return rs.getTimestamp(column).toLocalDateTime();
        if (type.equals(Instant.class)) return rs.getTimestamp(column).toInstant();
        if (type.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumClass = (Class<? extends Enum>) type;
            return Enum.valueOf(enumClass, value.toString());
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            try {
                return jsonMapper.readValue(value.toString(), Object.class);
            } catch (JsonProcessingException e) {
                throw new SQLException("Error parsing JSONB", e);
            }
        }

        return value;
    }
}
