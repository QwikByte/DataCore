# 🧠 DataCore — Centralized ORM Framework for PaperMC

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://www.oracle.com/de/java/)
[![PaperMC](https://img.shields.io/badge/API-Paper%201.21-blue?logo=minecraft)](https://papermc.io)
[![Build](https://img.shields.io/badge/Build-Maven-green?logo=apachemaven)](https://maven.apache.org)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](https://github.com/QwikByte/DataCore?tab=MIT-1-ov-file)

> A lightweight ORM and centralized SQL abstraction for Minecraft (PaperMC) plugins — Inspired by [Spring Data JPA](https://spring.io/projects/spring-data-jpa), built for performance and simplicity.

---

## ⚙️ Overview

DataCore provides:
- 🧩 **Annotation-based ORM** (`@Entity`, `@Column`, `@GeneratedValue`)
- 🔄 **Schema synchronization** between Entities and PostgreSQL
- 🧠 **Repository API** similar to Spring Data JPA
- 💾 **HikariCP**-based connection pooling
- 🌐 **Cross-plugin access** via `DataCoreAPI`
- 🧰 Full **JSONB**, **Enum**, and **Date/Time** support
- 🧱 Designed for **Paper 1.21+ / Java 21**

---

## 🚀 Installation

Note: I only tested with maven-projects and the following way worked for me:

Add `depend: [DataCore]` to your plugin.yml.

Then add the DataCore plugin as dependency in your project.

Finally, in the `<build>` section in your `pom.xml` add the following `<compilerArgs>` lines:

```xaml
<configuration>
  <source>${java.version}</source>
  <target>${java.version}</target>
  <compilerArgs>
    <arg>-parameters</arg>
  </compilerArgs>
</configuration>
```

---

## 🧩 Usage Example

### Entity

```java
@Entity(table = "players")
public class PlayerEntity {

    @Column(id = true, name = "id")
    @GeneratedValue(strategy = GeneratedValue.GenerationType.AUTO)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "coins")
    private int coins;

    // Getters/Setters
}
```
### Repository
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
### Register Repository
```java
PlayerRepository repo = DataCoreAPI.register(PlayerRepository.class, PlayerEntity.class);
repo.insert("QwikByte", 100);
```
---

## 🧠 Supported Java Types

| Java Type                       | PostgreSQL Type                  | Description                                                  |
| ------------------------------- | -------------------------------- | ------------------------------------------------------------ |
| `byte`, `Byte`                  | `SMALLINT`                       | 8-bit integer                                                |
| `short`, `Short`                | `SMALLINT`                       | 16-bit integer                                               |
| `int`, `Integer`                | `INT` / `SERIAL`                 | 32-bit integer (auto-increment when using `@GeneratedValue`) |
| `long`, `Long`                  | `BIGINT` / `BIGSERIAL`           | 64-bit integer (auto-increment when using `@GeneratedValue`) |
| `float`, `Float`                | `REAL`                           | Single-precision floating-point number                       |
| `double`, `Double`              | `DOUBLE PRECISION`               | Double-precision floating-point number                       |
| `BigDecimal`                    | `NUMERIC(18,4)`                  | Fixed precision, ideal for monetary or precise values        |
| `boolean`, `Boolean`            | `BOOLEAN`                        | True / False                                                 |
| `char`, `Character`             | `CHAR(1)`                        | Single character                                             |
| `String`                        | `TEXT`                           | UTF-8 string of variable length                              |
| `UUID`                          | `UUID`                           | Universally unique identifier                                |
| `LocalDate`                     | `DATE`                           | Calendar date (yyyy-MM-dd)                                   |
| `LocalTime`                     | `TIME`                           | Time of day (HH:mm:ss)                                       |
| `LocalDateTime`                 | `TIMESTAMP`                      | Date and time                                                |
| `Instant`                       | `TIMESTAMP`                      | UTC timestamp                                                |
| `Date (java.util)`              | `TIMESTAMP`                      | Legacy Java date                                             |
| `byte[]`                        | `BYTEA`                          | Binary data (e.g., files, images)                            |
| `Enum`                          | `TEXT`                           | Enum constant name stored as string                          |
| `Collection<?>` (`List`, `Set`) | `JSONB`                          | Automatically serialized to JSON                             |
| `Map<?, ?>`                     | `JSONB`                          | Key-value JSON mapping                                       |
| `JsonNode` (Jackson)            | `JSONB`                          | Native JSON object support                                   |
| `JSONObject` (org.json)         | `JSONB`                          | Alternative JSON implementation                              |
| `Optional<T>`                   | Unwrapped `T`                    | Automatically resolves underlying type                       |
| `Object` (any other type)       | `TEXT`                           | Fallback (uses `toString()`)                                 |
| `@GeneratedValue(UUID)`         | `UUID DEFAULT gen_random_uuid()` | Automatically generated UUID value                           |

---
## 🔌 API Access

```java
import de.qwikbyte.datacore.api.DataCoreAPI;

PlayerRepository repo = DataCoreAPI.register(PlayerRepository.class, PlayerEntity.class);
DatabaseManager db = DataCoreAPI.getDatabase();

```

| Method                         | Description                               |
| ------------------------------ | ----------------------------------------- |
| `getDatabase()`                | Returns the shared DatabaseManager        |
| `getRegistry()`                | Returns the RepositoryRegistry            |
| `register(Class<T>, Class<?>)` | Registers and syncs a Repository          |
| `getRepository(Class<T>)`      | Retrieves an existing Repository instance |

