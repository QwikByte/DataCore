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

/**
 * SQLTypeMapper is a utility class responsible for mapping Java types to their
 * corresponding SQL types and handling conversions between Java objects and SQL values.
 * It facilitates automatic type mapping and conversion to simplify integration with SQL databases.
 *
 * The class provides methods for:
 * - Determining the appropriate SQL type for a given Java field based on its type and metadata.
 * - Converting Java objects to their SQL-compatible representations.
 * - Converting SQL result set values back to their corresponding Java representations.
 */
public class SQLTypeMapper {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Maps a Java type to its corresponding SQL type representation based on the provided field
     * information, type, generation status, and generation strategy.
     *
     * @param field the field representing the property in the Java class
     * @param type the Java class of the field that needs to be mapped to an SQL data type
     * @param generated a boolean indicating whether the field is a generated value
     * @param generationType the type of generation strategy used (e.g., "UUID", "AUTO", etc.)
     * @return a string representation of the SQL data type for the given Java type
     */
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

    /**
     * Converts a given Java object to a value suitable for use in an SQL query.
     * Handles specific object types such as enums, collections, and maps by converting
     * them to appropriate representations (e.g., name for enums, JSON strings for collections/maps).
     *
     * @param value the Java object to be converted for SQL integration; can be null, an enum, a collection,
     *              a map, or any other object.
     * @return an SQL-compatible value derived from the provided object. If the object is null,
     *         returns null. If it is an enum, returns its name. If it is a collection or map,
     *         returns its JSON string representation. For other objects, returns the object itself.
     */
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

    /**
     * Converts a value retrieved from a SQL query result set into a corresponding Java object
     * based on the expected type represented by the provided field.
     * Handles specific data types such as UUID, LocalDate, LocalDateTime, Instant, enums,
     * collections, and maps. If the value is null, the method returns null.
     *
     * @param rs the ResultSet containing the SQL data to be converted
     * @param column the name of the column from which the value is to be retrieved
     * @param field the Field object representing the target Java property type
     * @return the Java object converted from the SQL value, or null if the SQL value is null
     * @throws SQLException if an error occurs while accessing the result set or processing the data
     */
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
