package de.qwikbyte.datacore.orm;

import de.qwikbyte.datacore.database.DatabaseManager;
import de.qwikbyte.datacore.orm.annotations.Column;
import de.qwikbyte.datacore.orm.annotations.Entity;
import de.qwikbyte.datacore.orm.annotations.Query;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

public class RepositoryFactory {

    private final DatabaseManager db;

    public RepositoryFactory(DatabaseManager db) {
        this.db = db;
    }

    @SuppressWarnings("unchecked")
    public <T> T createRepository(Class<T> repoClass, Class<?> entityClass) {
        return (T) Proxy.newProxyInstance(
                repoClass.getClassLoader(),
                new Class[]{repoClass},
                new QueryHandler(db, entityClass)
        );
    }

    private record QueryHandler(DatabaseManager db, Class<?> entityClass) implements InvocationHandler {
            private static final Pattern PARAM_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+)");

        @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (!method.isAnnotationPresent(Query.class)) {
                    throw new UnsupportedOperationException("Missing @Query on method: " + method.getName());
                }

                String rawQuery = method.getAnnotation(Query.class).value();
                Parameter[] parameters = method.getParameters();

                // Parse Query: find all :param occurrences
                Matcher matcher = PARAM_PATTERN.matcher(rawQuery);
                List<String> paramNames = new ArrayList<>();
                StringBuilder parsedSql = new StringBuilder();

                while (matcher.find()) {
                    String name = matcher.group(1);
                    paramNames.add(name);
                    matcher.appendReplacement(parsedSql, "?");
                }
                matcher.appendTail(parsedSql);

                // Build argument map
                Map<String, Object> argMap = new HashMap<>();
                if (args != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        argMap.put(parameters[i].getName(), args[i]);
                    }
                }

                String sql = parsedSql.toString();
                boolean isSelect = sql.trim().toUpperCase().startsWith("SELECT");

                try (Connection conn = db.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    // Assign parameters by name order
                    for (int i = 0; i < paramNames.size(); i++) {
                        String name = paramNames.get(i);
                        stmt.setObject(i + 1, argMap.get(name));
                    }

                    if (isSelect) {
                        ResultSet rs = stmt.executeQuery();
                        return mapResultSet(rs, method.getReturnType(), entityClass);
                    } else {
                        stmt.executeUpdate();
                        return null;
                    }
                }
            }

        private Object mapResultSet(ResultSet rs, Class<?> returnType, Class<?> entityClass) throws Exception {
            List<Object> results = new ArrayList<>();

            String table = entityClass.isAnnotationPresent(Entity.class)
                    ? entityClass.getAnnotation(Entity.class).table()
                    : entityClass.getSimpleName();

            while (rs.next()) {
                Object entity = entityClass.getDeclaredConstructor().newInstance();

                for (Field f : entityClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    String columnName;

                    if (f.isAnnotationPresent(Column.class)) {
                        columnName = f.getAnnotation(Column.class).name();
                    } else {
                        columnName = f.getName(); // Fallback
                    }

                    try {
                        Object value = rs.getObject(columnName);
                        if (value != null)
                            f.set(entity, value);
                    } catch (SQLException e) {
                        // Spalte existiert evtl. nicht – kein Problem
                    }
                }
                results.add(entity);
            }

            // Rückgabe je nach Typ
            if (returnType.equals(List.class)) return results;
            if (returnType.equals(Optional.class))
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            if (results.isEmpty()) return null;
            return results.get(0);
        }
    }
}
