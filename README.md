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

| Java Type                       | PostgreSQL Type                  | Beschreibung                                           |
| ------------------------------- | -------------------------------- | ------------------------------------------------------ |
| `byte`, `Byte`                  | `SMALLINT`                       | 8-Bit Ganzzahl                                         |
| `short`, `Short`                | `SMALLINT`                       | 16-Bit Ganzzahl                                        |
| `int`, `Integer`                | `INT` / `SERIAL`                 | 32-Bit Ganzzahl (Auto-Increment bei `@GeneratedValue`) |
| `long`, `Long`                  | `BIGINT` / `BIGSERIAL`           | 64-Bit Ganzzahl (Auto-Increment bei `@GeneratedValue`) |
| `float`, `Float`                | `REAL`                           | Einfach-präzise Fließkommazahl                         |
| `double`, `Double`              | `DOUBLE PRECISION`               | Doppel-präzise Fließkommazahl                          |
| `BigDecimal`                    | `NUMERIC(18,4)`                  | Feste Präzision, ideal für Geld-/Werte                 |
| `boolean`, `Boolean`            | `BOOLEAN`                        | Wahr / Falsch                                          |
| `char`, `Character`             | `CHAR(1)`                        | Einzelnes Zeichen                                      |
| `String`                        | `TEXT`                           | UTF-8 String beliebiger Länge                          |
| `UUID`                          | `UUID`                           | Universell eindeutige ID                               |
| `LocalDate`                     | `DATE`                           | Datum (yyyy-MM-dd)                                     |
| `LocalTime`                     | `TIME`                           | Uhrzeit (HH:mm:ss)                                     |
| `LocalDateTime`                 | `TIMESTAMP`                      | Datum + Zeit                                           |
| `Instant`                       | `TIMESTAMP`                      | UTC-Zeitpunkt                                          |
| `Date (java.util)`              | `TIMESTAMP`                      | Java-Legacy-Datum                                      |
| `byte[]`                        | `BYTEA`                          | Binärdaten (z. B. Bilder, Dateien)                     |
| `Enum`                          | `TEXT`                           | Enum-Name als String gespeichert                       |
| `Collection<?>` (`List`, `Set`) | `JSONB`                          | Automatisch zu JSON serialisiert                       |
| `Map<?, ?>`                     | `JSONB`                          | Key-Value-JSON Mapping                                 |
| `JsonNode` (Jackson)            | `JSONB`                          | Native JSON Objekte                                    |
| `JSONObject` (org.json)         | `JSONB`                          | Alternative JSON-Implementierung                       |
| `Optional<T>`                   | Entpackt T                       | Automatische Typweitergabe                             |
| `Object` (sonstiges)            | `TEXT`                           | Fallback (`toString()`)                                |
| `@GeneratedValue(UUID)`         | `UUID DEFAULT gen_random_uuid()` | Automatisch generierte UUID                            |
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

