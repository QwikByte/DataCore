package de.qwikbyte.datacore.api;

import de.qwikbyte.datacore.DataCorePlugin;
import de.qwikbyte.datacore.database.DatabaseManager;
import de.qwikbyte.datacore.orm.RepositoryRegistry;
import de.qwikbyte.datacore.orm.Repository;

/**
 * DataCoreAPI is a utility class that provides static methods for accessing and managing
 * the repository system and database in the application. It serves as an entry point to
 * interact with the core data layer of the application.
 *
 * The class wraps functionality provided by the DataCorePlugin instance, allowing access
 * to the RepositoryRegistry and DatabaseManager. It also facilitates repository
 * registration and retrieval for managing data entities.
 */
public class DataCoreAPI {

    /**
     * Retrieves the central {@link RepositoryRegistry} instance, which provides access
     * to registered repository instances and facilitates repository management operations.
     *
     * @return the {@link RepositoryRegistry} instance associated with the application
     */
    public static RepositoryRegistry getRegistry() {
        return DataCorePlugin.getInstance().getRepositoryRegistry();
    }


    /**
     * Retrieves the {@code DatabaseManager} instance associated with the application.
     * The {@code DatabaseManager} provides methods for managing database connections
     * and interactions with the underlying database.
     *
     * @return the {@code DatabaseManager} instance used for managing database operations
     */
    public static DatabaseManager getDatabase() {
        return DataCorePlugin.getInstance().getDatabaseManager();
    }


    /**
     * Registers a repository class with its corresponding entity class in the repository registry.
     *
     * @param <T> the type of the repository, extending {@code Repository}.
     * @param repositoryClass the class of the repository to register.
     * @param entityClass the class of the entity associated with the repository.
     * @return the registered repository instance of type {@code T}.
     */
    public static <T extends Repository<?>> T register(Class<T> repositoryClass, Class<?> entityClass) {
        return getRegistry().register(repositoryClass, entityClass);
    }

    /**
     * Retrieves an instance of a repository class from the repository registry.
     * The repository must already be registered in the repository registry.
     *
     * @param <T> the type of the repository, extending {@code Repository}.
     * @param repositoryClass the class of the repository to retrieve.
     * @return the repository instance of type {@code T}, or {@code null} if not registered.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Repository<?>> T getRepository(Class<T> repositoryClass) {
        return getRegistry().get(repositoryClass);
    }
}
