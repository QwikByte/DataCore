# 🧠 DataCore Documentation

> Centralized ORM and SQL abstraction framework for PaperMC — inspired by [Spring Data JPA](https://spring.io/projects/spring-data-jpa)  
> Built for simplicity, performance, and plugin interoperability.

---

## 📖 Table of Contents
- [🚀 Getting Started](getting-started.md)
- [🧩 Annotations](annotations.md)
- [🧠 Repositories](repositories.md)
- [🧱 Type Mapping](typemapping.md)
- [🔌 API Reference](api.md)
- [🧾 Changelog](changelog.md)

---

## ⚙️ What is DataCore?

**DataCore** provides a lightweight, annotation-based ORM layer for [PaperMC](https://papermc.io) plugins.  
It allows developers to define entities, repositories, and queries similar to Spring Data JPA —  
but optimized for Minecraft servers with minimal overhead.

**Core Features**
- 🧩 Annotation-based ORM (`@Entity`, `@Column`, `@GeneratedValue`)
- 🔄 Automatic schema synchronization with PostgreSQL
- 🧠 Repository-based data access layer (`@Query`)
- 💾 Connection pooling with **HikariCP**
- 🌐 Shared `DataCoreAPI` for cross-plugin database access
- 🧰 Built-in JSONB, Enum, and Date/Time support
- 🧱 Designed for **Java 21 + Paper 1.21+**

---

## 🚀 Quick Start

### 1️⃣ Add DataCore dependency

In your plugin’s `plugin.yml`:
```yaml
depend: [DataCore]
```

Then add the DataCore plugin as dependency in your project:

```xaml
<dependency>
  <groupId>de.qwikbyte</groupId>
  <artifactId>datacore</artifactId>
  <version>1.0</version>
  <scope>provided</scope>
</dependency>
```

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

### 2️⃣ Define an Entity

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

### 3️⃣ Create a Repository

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

### 4️⃣ Register your Repository

```java
PlayerRepository repo = DataCoreAPI.register(PlayerRepository.class, PlayerEntity.class);
repo.insert("QwikByte", 100);
```
---

## 🧠 Supported Types

DataCore automatically maps Java types to PostgreSQL column types.
For the full list, see [TypeMapping](https://qwikbyte.github.io/DataCore/typemapping.md).

| Java Type     | PostgreSQL Type | Example            |
| ------------- | --------------- | ------------------ |
| `int`         | `INT`           | Player coins or XP |
| `long`        | `BIGINT`        | Timestamps or IDs  |
| `String`      | `TEXT`          | Player names       |
| `boolean`     | `BOOLEAN`       | Status flags       |
| `UUID`        | `UUID`          | Entity identifiers |
| `List`, `Map` | `JSONB`         | Custom metadata    |

---

## 🔌 API Reference

For API details, visit the [JavaDoc Reference](https://qwikbyte.github.io/DataCore/javadocs).

| Method                        | Description                              |
| ----------------------------- | ---------------------------------------- |
| `DataCoreAPI.getDatabase()`   | Access the global database manager       |
| `DataCoreAPI.register()`      | Register and synchronize repositories    |
| `DataCoreAPI.getRepository()` | Retrieve an existing repository instance |
