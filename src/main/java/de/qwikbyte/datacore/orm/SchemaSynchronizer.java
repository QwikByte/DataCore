package de.qwikbyte.datacore.orm;

import de.qwikbyte.datacore.database.DatabaseManager;
import de.qwikbyte.datacore.orm.annotations.*;
import de.qwikbyte.datacore.orm.utils.SQLTypeMapper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class SchemaSynchronizer {

    private final DatabaseManager db;

    public SchemaSynchronizer(DatabaseManager db) {
        this.db = db;
    }

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
