package de.qwikbyte.datacore.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the value for a field in an entity class should be generated dynamically.
 * This annotation is typically used for fields that represent primary keys or other
 * unique identifiers in the database.
 *
 * The {@code strategy} attribute specifies the generation strategy for the field.
 * Available strategies include:
 * - AUTO: The default strategy, where the database or ORM provider determines the generation method.
 * - UUID: Specifies that the field value should be generated as a universally unique identifier (UUID).
 *
 * This annotation is intended to be used in conjunction with the {@code Column} annotation
 * and the {@code Entity} annotation to define how a field's value should be generated
 * during database insert operations.
 *
 * Attributes:
 * - strategy: The strategy for value generation. Defaults to {@code GenerationType.AUTO}.
 *
 * @see Entity
 * @see Column
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedValue {
    GenerationType strategy() default GenerationType.AUTO;

    enum GenerationType {
        AUTO,
        UUID,
    }
}
