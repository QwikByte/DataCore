# 🚀 Getting Started

> Learn how to install, configure, and use **DataCore** in your PaperMC plugin project.

---

## 📖 Table of Contents
- [🏠 Home](index.md)
- [🧩 Annotations](annotations.md)
- [🧠 Repositories](repositories.md)
- [🧱 Type Mapping](typemapping.md)
- [🔌 API Reference](api.md)
- [🧾 Changelog](changelog.md)

---

## ⚙️ Requirements

Before using DataCore, ensure your environment meets the following:

| Requirement | Version / Notes |
|--------------|-----------------|
| **Java** | 21 or higher |
| **PaperMC** | 1.21+ |
| **Database** | PostgreSQL (recommended) |
| **Build System** | Maven (tested and supported) |

---

## 🧩 Installation

### 1️⃣ Add DataCore as dependency

In your plugin’s `plugin.yml` file:

```yaml
depend: [DataCore]
```

In your pom.xml:

```xaml
<dependency>
  <groupId>de.qwikbyte</groupId>
  <artifactId>datacore</artifactId>
  <version>1.0</version>
  <scope>provided</scope>
</dependency>
```

### 2️⃣ Configure compiler options

Add this inside the <build> section of your pom.xml:

```xaml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.13.0</version>
  <configuration>
    <source>${java.version}</source>
    <target>${java.version}</target>
    <compilerArgs>
      <arg>-parameters</arg>
    </compilerArgs>
  </configuration>
</plugin>
```
This ensures DataCore can detect parameter names for your repository queries.

---

### 3️⃣ Set up PostgreSQL connection

DataCore automatically manages a HikariCP connection pool.
You can configure database access through [config.yml] (example):

```yaml
database:
  host: localhost
  port: 5432
  name: datacore
  username: postgres
  password: secret
  maxPoolSize: 10
```
---

## 🧠 Create Your First Entity

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
}
```
When DataCore starts, it automatically:

- Creates the players table (if it doesn’t exist)
- Synchronizes missing columns
- Respects primary keys and @GeneratedValue

---

## 💾 Create a Repository

```java
public interface PlayerRepository extends Repository<PlayerEntity> {

    @Query("SELECT * FROM players WHERE id = :id")
    Optional<PlayerEntity> findById(long id);

    @Query("INSERT INTO players (name, coins) VALUES (:name, :coins)")
    void insert(String name, int coins);
}
```
### ⚡ Use It in Your Plugin
```java
PlayerRepository repo =
    DataCoreAPI.register(PlayerRepository.class, PlayerEntity.class);

repo.insert("QwikByte", 100);
Optional<PlayerEntity> player = repo.findById(1);
player.ifPresent(p -> System.out.println(p.getName()));
```

---

## ✅ Next Steps

Continue reading:

- [🧩 Annotations](annotations.md)
- [🧠 Repositories](repositories.md)
- [🧱 Type Mapping](typemapping.md)
