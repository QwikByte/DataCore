package de.qwikbyte.datacore.orm;

import de.qwikbyte.datacore.database.DatabaseManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RepositoryRegistry {

    private final DatabaseManager db;
    private final RepositoryFactory factory;
    private final SchemaSynchronizer schemaSync;
    private final Map<Class<?>, Object> repositories = new HashMap<>();

    public RepositoryRegistry(DatabaseManager db) {
        this.db = db;
        this.factory = new RepositoryFactory(db);
        this.schemaSync = new SchemaSynchronizer(db);
    }

    public <T extends Repository<?>> T register(Class<T> repoClass, Class<?> entityClass) {
        try {
            schemaSync.syncEntity(entityClass);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        T repo = factory.createRepository(repoClass, entityClass);
        repositories.put(repoClass, repo);
        return repo;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> repoClass) {
        return (T) repositories.get(repoClass);
    }
}
