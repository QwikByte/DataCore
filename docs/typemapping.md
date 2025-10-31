# 🧱 Java ↔ PostgreSQL Type Mapping

DataCore automatically converts between **Java types** and **PostgreSQL column types**  
during entity creation, schema synchronization, and query execution.

This ensures that entities and repositories work seamlessly without manually writing SQL type definitions.

---

## 🧩 Overview

The following table lists all supported Java types and how they are mapped to PostgreSQL columns.  
This mapping is handled internally by the [`SQLTypeMapper`](../src/main/java/de/qwikbyte/datacore/orm/utils/SQLTypeMapper.java) utility class.

| Java Type                       | PostgreSQL Type                  | Description                                            |
| ------------------------------- | -------------------------------- | ------------------------------------------------------ |
| `byte`, `Byte`                  | `SMALLINT`                       | 8-bit integer                                          |
| `short`, `Short`                | `SMALLINT`                       | 16-bit integer                                         |
| `int`, `Integer`                | `INT` / `SERIAL`                 | 32-bit integer (auto-increment when using `@GeneratedValue`) |
| `long`, `Long`                  | `BIGINT` / `BIGSERIAL`           | 64-bit integer (auto-increment when using `@GeneratedValue`) |
| `float`, `Float`                | `REAL`                           | Single-precision floating-point number                 |
| `double`, `Double`              | `DOUBLE PRECISION`               | Double-precision floating-point number                 |
| `BigDecimal`                    | `NUMERIC(18,4)`                  | Fixed precision, ideal for monetary or precise values  |
| `boolean`, `Boolean`            | `BOOLEAN`                        | True / False                                           |
| `char`, `Character`             | `CHAR(1)`                        | Single character                                       |
| `String`                        | `TEXT`                           | UTF-8 string of variable length                        |
| `UUID`                          | `UUID`                           | Universally unique identifier                          |
| `LocalDate`                     | `DATE`                           | Calendar date (yyyy-MM-dd)                             |
| `LocalTime`                     | `TIME`                           | Time of day (HH:mm:ss)                                 |
| `LocalDateTime`                 | `TIMESTAMP`                      | Date and time                                          |
| `Instant`                       | `TIMESTAMP`                      | UTC timestamp                                          |
| `Date (java.util)`              | `TIMESTAMP`                      | Legacy Java date                                       |
| `byte[]`                        | `BYTEA`                          | Binary data (e.g., files, images)                      |
| `Enum`                          | `TEXT`                           | Enum constant name stored as string                    |
| `Collection<?>` (`List`, `Set`) | `JSONB`                          | Automatically serialized to JSON                       |
| `Map<?, ?>`                     | `JSONB`                          | Key-value JSON mapping                                 |
| `JsonNode` (Jackson)            | `JSONB`                          | Native JSON object support                             |
| `JSONObject` (org.json)         | `JSONB`                          | Alternative JSON implementation                        |
| `Optional<T>`                   | Unwrapped `T`                    | Automatically resolves underlying type                 |
| `Object` (any other type)       | `TEXT`                           | Fallback (uses `toString()`)                           |
| `@GeneratedValue(UUID)`         | `UUID DEFAULT gen_random_uuid()` | Automatically generated UUID value                     |

---

## 💡 Notes

- JSON-related types (`List`, `Map`, `JsonNode`, `JSONObject`) are automatically stored as **PostgreSQL JSONB**.  
- Enum fields store the **enum name** as a `TEXT` value.  
- `@GeneratedValue` supports both `AUTO` and `UUID` generation strategies.  
- Binary fields (`byte[]`) map directly to PostgreSQL `BYTEA`.  
- Date/time conversions use Java’s `java.time` API (`LocalDate`, `LocalTime`, `Instant`, etc.).

---

## 🧠 Example

```java
@Entity(table = "items")
public class ItemEntity {

    @Column(id = true, name = "id")
    @GeneratedValue(strategy = GeneratedValue.GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "tags")
    private List<String> tags;

    @Column(name = "metadata")
    private Map<String, Object> metadata;
}
```
This will produce the following PostgreSQL table:
```sql
CREATE TABLE items (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  name TEXT,
  price NUMERIC(18,4),
  tags JSONB,
  metadata JSONB
);
```
