package de.qwikbyte.datacore.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used to associate a database query with a specific method.
 * This annotation plays a crucial role in simplifying custom query execution
 * by specifying the query directly on methods in a repository or handler class.
 *
 * Attributes:
 * - value: Represents the actual SQL query string that will be executed when
 *          the annotated method is invoked. Named parameters are supported
 *          by using the syntax `:parameterName` within the query string.
 *          These parameters will be bound to method arguments at runtime.
 *
 * This annotation is typically used within the context of a repository
 * or query handler. It enables the mapping of SQL queries to methods,
 * which can be invoked programmatically while providing strong typing and
 * adherence to the method's return type.
 *
 * Note: A method annotated with {@code @Query} must specify a valid
 * SQL query in the `value()` attribute. If the annotation is missing,
 * or the query is invalid, runtime exceptions may be thrown during the
 * execution of the method.
 *
 * Usage consideration:
 * - Methods annotated with this annotation will require proper parsing and
 *   parameter binding to replace any named placeholders (e.g., `:param`).
 * - The query must be constructed in such a way that it aligns with the
 *   database schema associated with the corresponding entity, if applicable.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
    String value();
}
