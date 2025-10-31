package de.qwikbyte.datacore.orm;

import de.qwikbyte.datacore.database.DatabaseManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The RepositoryRegistry class is responsible for managing the lifecycle and retrieval
 * of repository instances. It acts as a centralized registry for repositories, providing
 * both registration and lookup functionality. This class ensures schema synchronization
 * for registered entities and repository creation via a factory.
 */
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

    /**
     * Registers a repository for a specific entity class, ensuring that the entity schema is synchronized
     * in the underlying database. Creates an instance of the repository and stores it for later retrieval.
     *
     * @param <T> the type of the repository, which must extend {@link Repository}
     * @param repoClass the class of the repository interface to register
     * @param entityClass the class of the entity associated with the repository
     * @return an instance of the registered repository
     */
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

    /**
     * Retrieves a repository instance associated with the specified repository class.
     *
     * @param <T> the type of the repository to retrieve
     * @param repoClass the class of the repository interface to look up
     * @return an instance of the requested repository, or {@code null} if no repository
     *         has been registered for the specified class
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> repoClass) {
        return (T) repositories.get(repoClass);
    }
}
