package de.qwikbyte.datacore.orm;

import de.qwikbyte.datacore.database.DatabaseManager;
import de.qwikbyte.datacore.orm.annotations.*;
import de.qwikbyte.datacore.orm.utils.SQLTypeMapper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * The SchemaSynchronizer class is responsible for ensuring that the database schema
 * is synchronized with the structure of annotated Java entity classes. It provides
 * functionality for creating tables and adding columns to match the fields in the entity classes.
 *
 * This class works with entity classes annotated with {@code @Entity}, and their fields must
 * be annotated with {@code @Column}. It uses database metadata to check existing table and column
 * definitions, and it creates or updates database schemas as necessary.
 *
 * The synchronization process includes:
 * - Checking the presence of an {@code @Entity} annotation to identify the corresponding database table.
 * - Gathering fields annotated with {@code @Column}, determining their properties, and mapping them to SQL types.
 * - Creating new tables if they do not exist.
 * - Adding missing columns to an existing table.
 *
 * Dependencies:
 * - DatabaseManager: A helper class responsible for managing database connections.
 * - Annotations: {@code @Entity}, {@code @Column}, {@code @GeneratedValue}.
 */
public class SchemaSynchronizer {

    private final DatabaseManager db;

    public SchemaSynchronizer(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Synchronizes the database schema for a given entity class by ensuring that its corresponding
     * database table and columns are properly created or updated according to the annotations
     * defined in the class.
     *
     * If the specified entity class is annotated with {@code @Entity}, the method performs the
     * following actions:
     * - Retrieves the table name specified in the {@code @Entity} annotation.
     * - Checks existing columns in the table based on metadata from the database.
     * - Compares the annotated fields in the class with the existing columns.
     * - Creates the table if it does not exist.
     * - Adds missing columns to the table.
     *
     * If the entity class is not annotated with {@code @Entity}, the method exits without performing any actions.
     *
     * The method uses field-level annotations like {@code @Column} to determine the column
     * properties (e.g., name, nullable, primary key). Additionally, it uses {@code @GeneratedValue}
     * to identify fields with generated values and their corresponding generation strategies.
     *
     * Throws {@code SQLException} if any database error occurs during schema synchronization.
     *
     * @param entityClass the class representing the entity for which the database schema
     *                    needs to be synchronized. This class must be annotated with {@code @Entity}.
     *                    Fields annotated with {@code @Column} are treated as database columns.
     * @throws SQLException if there is an error while creating or altering the database schema.
     */
    public void syncEntity(Class<?> entityClass) throws SQLException {
        if (!entityClass.isAnnotationPresent(Entity.class)) return;

        Entity entityAnno = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnno.table();

        Map<String, String> columnsInDb = getExistingColumns(tableName);
        List<String> newColumns = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Column.class)) continue;

            Column c = field.getAnnotation(Column.class);
            String columnName = c.name();

            boolean generated = field.isAnnotationPresent(GeneratedValue.class);
            String generationType = generated
                    ? field.getAnnotation(GeneratedValue.class).strategy().name()
                    : "AUTO";

            String sqlType = SQLTypeMapper.mapJavaTypeToSQL(field, field.getType(), generated, generationType);

            if (!columnsInDb.containsKey(columnName)) {
                String def = columnName + " " + sqlType
                        + (c.id() ? " PRIMARY KEY" : "")
                        + (c.nullable() ? "" : " NOT NULL");
                newColumns.add(def);
            }
        }

        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            if (columnsInDb.isEmpty()) {
                String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        String.join(", ", newColumns) + ")";
                st.executeUpdate(sql);
            } else {
                for (String def : newColumns) {
                    String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + def;
                    st.executeUpdate(sql);
                }
            }
        }
    }

    /**
     * Retrieves a map of existing columns for a specified table in the database.
     * The map contains the column names as keys and their corresponding data types as values.
     *
     * @param tableName the name of the database table whose columns are to be retrieved
     * @return a map where the keys are column names and the values are column data types
     */
    private Map<String, String> getExistingColumns(String tableName) {
        Map<String, String> map = new HashMap<>();
        try (Connection conn = db.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                map.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"));
            }
        } catch (SQLException ignored) { }
        return map;
    }
}
