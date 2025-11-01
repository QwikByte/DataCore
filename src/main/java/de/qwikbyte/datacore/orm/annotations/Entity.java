package de.qwikbyte.datacore.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation marks a class as an entity that maps to a specific database table.
 * It is used within the context of ORM (Object-Relational Mapping) to establish
 * a relationship between a Java class and a table in the database.
 * <p>
 * Classes annotated with this annotation represent database entities and must specify
 * the name of the database table to which they correspond.
 * <p>
 * Attributes:
 * - table: Specifies the name of the database table represented by the annotated class.
 * <p>
 * This annotation is intended to work alongside other annotations such as
 * {@code Column} and {@code GeneratedValue} to fully define the structure of the database table.
 * It must be used in a class that encapsulates fields annotated as columns to ensure
 * correct schema mapping and database synchronization.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
    String table();
}
