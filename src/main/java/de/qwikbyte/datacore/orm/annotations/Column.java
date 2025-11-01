package de.qwikbyte.datacore.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies that a field within an entity class corresponds to a database column.
 * This annotation is used to map a Java field to a column in the database table.
 * It can additionally define specific attributes of the column, such as its name,
 * whether it represents a primary key, and its nullable property.
 *
 * Attributes:
 * - name: The name of the database column corresponding to the annotated field. This is mandatory.
 * - id (optional): A boolean flag indicating whether this column is a primary key. Defaults to false.
 * - nullable (optional): A boolean flag indicating whether the column can hold null values. Defaults to true.
 *
 * This annotation must be combined with the {@code Entity} annotation on the corresponding class
 * to ensure proper schema synchronization and table creation within the database.
 *
 * When used in conjunction with {@code GeneratedValue}, it allows specifying generation strategies
 * for fields that are meant to be auto-generated (e.g., primary keys).
 *
 * @see Entity
 * @see GeneratedValue
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name();
    boolean id() default false;
    boolean nullable() default true;
    boolean unique() default false;
}