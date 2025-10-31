# 🧠 Repositories

> Repositories provide a declarative, type-safe way to interact with your database — inspired by Spring Data JPA but optimized for Minecraft.

---

## 📖 Table of Contents
- [🏠 Home](index.md)
- [🚀 Getting Started](getting-started.md)
- [🧩 Annotations](annotations.md)
- [🧱 Type Mapping](typemapping.md)
- [🔌 API Reference](api.md)

---

## ⚙️ Overview

A **Repository** defines custom queries for your entities using annotated methods.  
You simply define the method signature and SQL statement — DataCore takes care of parameter mapping, query execution, and entity conversion.

Repositories are automatically registered and managed via the [`DataCoreAPI`](api.md).

---

## 🧩 Basic Example

```java
public interface PlayerRepository extends Repository<PlayerEntity> {

    @Query("SELECT * FROM players WHERE id = :id")
    Optional<PlayerEntity> findById(long id);

    @Query("INSERT INTO players (name, coins) VALUES (:name, :coins)")
    void insert(String name, int coins);

    @Query("UPDATE players SET coins = :coins WHERE id = :id")
    void update(long id, int coins);
}
```

###✅ Features:

- Named parameters using :name
- Automatic type conversion
- SELECT, INSERT, UPDATE, DELETE supported
- Works with any PostgreSQL query

---

## 🔍 `@Query` Annotation

The `@Query` annotation defines the SQL statement to execute.

| Parameter                  | Description                                       |
| -------------------------- | ------------------------------------------------- |
| `value`                    | SQL string (supports named parameters like `:id`) |
| `nativeQuery` *(optional)* | Reserved for future native query handling         |
| `timeout` *(optional)*     | Sets query timeout (in ms, planned feature)       |

Example:
```java
@Query("SELECT * FROM users WHERE name = :username")
Optional<UserEntity> findByName(String username);
```

---

## 🧱 Supported Return Types

| Return Type        | Description                                        |
| ------------------ | -------------------------------------------------- |
| `Entity`           | Single result, throws if multiple found            |
| `Optional<Entity>` | Single or empty result                             |
| `List<Entity>`     | Multiple rows mapped to entity list                |
| `void`             | For non-returning queries (INSERT, UPDATE, DELETE) |
| `int` / `long`     | Number of affected rows                            |

