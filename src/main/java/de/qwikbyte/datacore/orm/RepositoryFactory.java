package de.qwikbyte.datacore.orm;

import de.qwikbyte.datacore.database.DatabaseManager;
import de.qwikbyte.datacore.orm.annotations.Column;
import de.qwikbyte.datacore.orm.annotations.Entity;
import de.qwikbyte.datacore.orm.annotations.Query;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

/**
 * A factory class responsible for creating repository implementations dynamically at runtime.
 * This class enables the creation of repository interfaces for handling database operations
 * in a type-safe and modular way by utilizing Java's dynamic proxy mechanism.
 * The factory simplifies the process of mapping database tables to Java entities.
 */
public class RepositoryFactory {

    private final DatabaseManager db;

    public RepositoryFactory(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Creates a dynamic proxy instance for the specified repository interface type and associates it with the
     * corresponding entity class. This method dynamically generates an implementation of the repository interface
     * to handle database operations for the given entity type.
     *
     * @param <T>        the type of the repository interface
     * @param repoClass  the class object representing the repository interface
     * @param entityClass the class object representing the entity type associated with the repository
     * @return an instance of the repository proxy implementing the specified repository interface
     */
    @SuppressWarnings("unchecked")
    public <T> T createRepository(Class<T> repoClass, Class<?> entityClass) {
        return (T) Proxy.newProxyInstance(
                repoClass.getClassLoader(),
                new Class[]{repoClass},
                new QueryHandler(db, entityClass)
        );
    }

    /**
     * A record-based implementation of an InvocationHandler that dynamically handles
     * database query execution for methods annotated with {@code @Query}.
     *
     * This handler is responsible for parsing and executing SQL queries associated
     * with methods of a repository interface. It supports named parameter binding,
     * automatic result mapping, and transaction management.
     *
     * Key responsibilities include:
     * - Parsing SQL queries with named parameters (e.g., {@code :paramName}) into executable
     *   SQL with placeholders (e.g., {@code ?}) and mapping parameters to method arguments.
     * - Executing SQL queries, either as SELECT for retrieving data or as UPDATE/INSERT/DELETE
     *   for modifying data.
     * - Mapping query results into entities or other return types based on method signatures.
     *
     * Components:
     * - {@code db}: A {@code DatabaseManager} instance for managing database connections.
     * - {@code entityClass}: The class representing the database entity associated with the repository.
     * - Query parsing: Extracts and replaces named parameters in SQL with placeholders.
     * - Result mapping: Maps rows from a {@code ResultSet} to objects of the specified entity or return type.
     *
     * Usage considerations:
     * - When invoking a method, the proxy checks for the presence of the {@code @Query} annotation.
     *   If absent, an {@code UnsupportedOperationException} is thrown.
     * - Query execution adapts to the method's return type, supporting common types such as
     *   {@code List}, {@code Optional}, or single entity objects.
     * - The entity mapping relies on field annotations like {@code @Column} to resolve
     *   database column names. If no annotation is present, field names are used as column names by default.
     *
     * Error handling:
     * - Throws exceptions for missing {@code @Query} annotations, invalid parameters, or SQL execution issues.
     * - Ensures resources like {@code Connection} and {@code PreparedStatement} are properly managed.
     */
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

        /**
         * Maps the rows of a given {@link ResultSet} into objects of a specified entity class.
         * The method uses reflection to instantiate and populate fields of the entity class based on
         * database column mappings (using {@code @Entity} and {@code @Column} annotations).
         * Supports returning results as a {@link List}, a single object, or an {@link Optional}.
         *
         * @param rs the {@link ResultSet} containing the query results to be mapped
         * @param returnType the class type for the desired return (e.g., {@link List}, {@link Optional}, or the entity class itself)
         * @param entityClass the class of the entity to map each row of the {@link ResultSet} to, which must be annotated with {@code @Entity}
         * @return an instance of either a {@link List}, an {@link Optional}, or a single object of {@code entityClass}, depending on {@code returnType};
         *         returns {@code null} if no results are found and {@code returnType} is not a {@link List} or {@link Optional}
         * @throws Exception if errors occur during reflection, entity instantiation, or accessing the {@link ResultSet}
         */
        private Object mapResultSet(ResultSet rs, Class<?> returnType, Class<?> entityClass) throws Exception {
            List<Object> results = new ArrayList<>();

            /*String table = entityClass.isAnnotationPresent(Entity.class)
                    ? entityClass.getAnnotation(Entity.class).table()
                    : entityClass.getSimpleName();*/

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
                return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
            if (results.isEmpty()) return null;
            return results.getFirst();
        }
    }
}
