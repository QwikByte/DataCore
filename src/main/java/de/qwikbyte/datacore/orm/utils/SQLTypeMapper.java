package de.qwikbyte.datacore.orm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Utility for automatic mapping and conversion between Java and SQL types.
 * <p>
 * This class is used internally by DataCore's ORM layer to:
 * <ul>
 *     <li>Determine SQL column types during schema generation</li>
 *     <li>Convert Java entity fields into SQL-compatible values for persistence</li>
 *     <li>Reconstruct Java objects from SQL query results</li>
 * </ul>
 *
 * <p>Supports primitive types, wrapper classes, enums, date/time types,
 * JSON structures, collections, and maps. Automatically handles
 * PostgreSQL-specific types such as JSONB and BYTEA.</p>
 *
 * @author Daniel
 * @version 1.0
 * @since DataCore 1.0
 */
public final class SQLTypeMapper {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private SQLTypeMapper() {
        // Utility class → prevent instantiation
    }

    // Cache for dummy field used by Optional<T> conversion
    private static final Field DUMMY_FIELD;

    static {
        try {
            DUMMY_FIELD = DummyFieldHolder.class.getDeclaredField("dummy");
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Maps a Java type to its corresponding SQL type representation based on the provided field
     * information, type, generation status, and generation strategy.
     *
     * @param field           the field representing the property in the Java class
     * @param type            the Java class of the field that needs to be mapped to an SQL data type
     * @param generated       whether the field is a generated value (e.g., @GeneratedValue)
     * @param generationType  the type of generation strategy used (e.g., "UUID", "AUTO")
     * @return SQL data type string
     */
    public static String mapJavaTypeToSQL(Field field, Class<?> type, boolean generated, String generationType) {
        if (generated && "UUID".equalsIgnoreCase(generationType)) {
            return "UUID DEFAULT gen_random_uuid()";
        }

        // Optional<T> → unwrap inner type
        if (Optional.class.isAssignableFrom(type)) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType pt) {
                Type innerType = pt.getActualTypeArguments()[0];
                if (innerType instanceof Class<?> innerClass) {
                    return mapJavaTypeToSQL(field, innerClass, generated, generationType);
                }
            }
            return "TEXT";
        }

        // Basic types
        if (type.equals(UUID.class)) return "UUID";
        if (type.equals(String.class)) return "TEXT";
        if (type.equals(char.class) || type.equals(Character.class)) return "CHAR(1)";
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

        // Time types
        if (type.equals(LocalDate.class)) return "DATE";
        if (type.equals(LocalTime.class)) return "TIME";
        if (type.equals(LocalDateTime.class) || type.equals(Instant.class) || type.equals(java.util.Date.class))
            return "TIMESTAMP";

        // Binary
        if (type.equals(byte[].class)) return "BYTEA";

        // Enum
        if (type.isEnum()) return "TEXT";

        // JSON / Collection / Map
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) return "JSONB";
        if (type.getName().equals("org.json.JSONObject")) return "JSONB";

        try {
            Class<?> jsonNodeClass = Class.forName("com.fasterxml.jackson.databind.JsonNode");
            if (jsonNodeClass.isAssignableFrom(type)) return "JSONB";
        } catch (ClassNotFoundException ignored) {}

        // Default fallback
        return "TEXT";
    }

    /**
     * Converts a given Java object to a value suitable for use in an SQL query.
     * Handles specific object types such as enums, collections, maps, and JSON objects.
     *
     * @param value Java object to convert
     * @return SQL-compatible value (may be null)
     */
    public static Object toSQLValue(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case Enum<?> e -> {
                return e.name();
            }
            case JsonNode node -> {
                return node.toString();
            }
            default -> {
            }
        }

        if (value.getClass().getName().equals("org.json.JSONObject")) {
            return value.toString();
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
     * Converts a value retrieved from a SQL result set into the corresponding Java type
     * defined by the given field. Supports UUID, Enum, Date/Time, JSONB, and Optional.
     *
     * @param rs     SQL result set
     * @param column column name
     * @param field  target field
     * @return Java object representation
     * @throws SQLException when conversion fails
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object fromSQLValue(ResultSet rs, String column, Field field) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) return null;

        // Handle PostgreSQL JSONB PGobject
        if (value instanceof PGobject pg) {
            value = pg.getValue();
        }

        Class<?> type = field.getType();

        // === UUID ===
        if (type.equals(UUID.class)) return UUID.fromString(value.toString());

        // === Date / Time types ===
        if (type.equals(LocalDate.class)) return rs.getDate(column).toLocalDate();
        if (type.equals(LocalTime.class)) return rs.getTime(column).toLocalTime();
        if (type.equals(LocalDateTime.class)) return rs.getTimestamp(column).toLocalDateTime();
        if (type.equals(Instant.class)) return rs.getTimestamp(column).toInstant();
        if (type.equals(java.util.Date.class)) return rs.getTimestamp(column);

        // === Binary ===
        if (type.equals(byte[].class)) return rs.getBytes(column);

        // === Character ===
        if (type.equals(char.class) || type.equals(Character.class)) {
            String str = value.toString();
            return str.isEmpty() ? null : str.charAt(0);
        }

        // === Enum ===
        if (type.isEnum()) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) type;
            return Enum.valueOf(enumClass, value.toString());
        }

        // === JSONB / Collection / Map ===
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            try {
                return jsonMapper.readValue(value.toString(), Object.class);
            } catch (JsonProcessingException e) {
                throw new SQLException("Error parsing JSONB", e);
            }
        }

        // === Jackson JsonNode ===
        try {
            Class<?> jsonNodeClass = Class.forName("com.fasterxml.jackson.databind.JsonNode");
            if (jsonNodeClass.isAssignableFrom(type)) {
                return jsonMapper.readTree(value.toString());
            }
        } catch (ClassNotFoundException | JsonProcessingException ignored) {}

        // === org.json.JSONObject ===
        if (type.getName().equals("org.json.JSONObject")) {
            try {
                return new org.json.JSONObject(value.toString());
            } catch (Exception e) {
                throw new SQLException("Error parsing JSONObject", e);
            }
        }

        // === Optional<T> ===
        if (Optional.class.isAssignableFrom(type)) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType pt) {
                Type innerType = pt.getActualTypeArguments()[0];
                if (innerType instanceof Class<?> innerClass) {
                    Object innerValue = fromSQLValue(rs, column, createFakeField(innerClass));
                    return Optional.ofNullable(innerValue);
                }
            }
            return Optional.ofNullable(value);
        }

        // === Fallback ===
        return value;
    }

    /** Creates a dummy Field for Optional<T> handling. */
    private static Field createFakeField(Class<?> type) {
        return DUMMY_FIELD;
    }

    private static class DummyFieldHolder {
        private static Object dummy;
    }
}
