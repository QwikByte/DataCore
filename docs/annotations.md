# 🧩 Annotations

> Learn how DataCore maps your Java classes and fields to database tables using lightweight annotations.

---

## 📖 Table of Contents
- [🏠 Home](index.md)
- [🚀 Getting Started](getting-started.md)
- [🧠 Repositories](repositories.md)
- [🧱 Type Mapping](typemapping.md)
- [🔌 API Reference](api.md)

---

## 🧱 `@Entity`

Marks a class as a database entity managed by DataCore.  
Each entity corresponds to one table in your PostgreSQL database.

```java
@Entity(table = "players")
public class PlayerEntity {
    ...
}
```
| Attribute    | Type      | Description                                                   |
| ------------ | --------- | ------------------------------------------------------------- |
| `table`      | `String`  | Name of the SQL table (required).                             |
| `schema`     | `String`  | Optional schema name.                                         |
| `autoUpdate` | `boolean` | If `true`, columns are automatically synchronized on startup. |

---

## 🧱 `@Column`

Defines a field as a column in the database table.

```java
@Column(name = "name")
private String name;
```
| Attribute  | Type      | Description                                     |
| ---------- | --------- | ----------------------------------------------- |
| `name`     | `String`  | Column name (defaults to field name).           |
| `id`       | `boolean` | Marks this column as primary key.               |
| `nullable` | `boolean` | Whether the column can contain NULL values.     |
| `unique`   | `boolean` | Whether this column must contain unique values. |

---

## ⚙️ `@GeneratedValue`

Used for auto-generated fields (like IDs or UUIDs).
You can control how the value is generated.
```java
@GeneratedValue(strategy = GeneratedValue.GenerationType.AUTO)
private long id;
```
| Strategy | SQL Result                              |
| -------- | --------------------------------------- |
| `AUTO`   | Uses PostgreSQL `SERIAL` or `BIGSERIAL` |
| `UUID`   | Uses `UUID DEFAULT gen_random_uuid()`   |

💡 When using UUID, ensure the PostgreSQL extension pgcrypto is enabled:

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

---

## 🧠 Combined Example

```java
@Entity(table = "players", autoUpdate = true)
public class PlayerEntity {

    @Column(id = true, name = "id")
    @GeneratedValue(strategy = GeneratedValue.GenerationType.AUTO)
    private long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "coins")
    private int coins;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

```
When DataCore starts:

- The players table is created if it doesn’t exist.
- All missing columns are automatically added.
- Auto-generated fields (like id) use the correct PostgreSQL strategy.

---

## 🔍 Future Extensions (Planned)

| Annotation   | Purpose                                             |
| ------------ | --------------------------------------------------- |
| `@OneToMany` | Define one-to-many relationships (planned feature). |
| `@ManyToOne` | Define foreign key relationships (planned feature). |
| `@Transient` | Exclude field from persistence (planned feature).   |

---

## 🧠 Tips

- Always define at least one field with @Column(id = true).
- Use @GeneratedValue(UUID) for safe cross-server identifiers.
- If autoUpdate is true, DataCore automatically applies schema updates when plugins load.

---

## ✅ Next Steps

Continue reading:

- [🧠 Repositories](repositories.md)
- [🧱 Type Mapping](typemapping.md)
- [🔌 API Reference](apireference.md)
