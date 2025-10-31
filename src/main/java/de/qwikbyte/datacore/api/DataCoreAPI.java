package de.qwikbyte.datacore.api;

import de.qwikbyte.datacore.DataCorePlugin;
import de.qwikbyte.datacore.database.DatabaseManager;
import de.qwikbyte.datacore.orm.RepositoryRegistry;
import de.qwikbyte.datacore.orm.Repository;

public class DataCoreAPI {

    /**
     * Gibt die zentrale RepositoryRegistry zurück.
     * Darüber können andere Plugins Repositories registrieren oder abrufen.
     */
    public static RepositoryRegistry getRegistry() {
        return DataCorePlugin.getInstance().getRepositoryRegistry();
    }

    /**
     * Gibt den zentralen DatabaseManager zurück.
     * Erlaubt Zugriff auf Connection-Pool (z. B. für native Queries).
     */
    public static DatabaseManager getDatabase() {
        return DataCorePlugin.getInstance().getDatabaseManager();
    }

    /**
     * Registriert ein Repository (inkl. Entity-Synchronisierung).
     * Beispiel:
     * PlayerRepository repo = DataCoreAPI.register(PlayerRepository.class, PlayerEntity.class);
     */
    public static <T extends Repository<?>> T register(Class<T> repositoryClass, Class<?> entityClass) {
        return getRegistry().register(repositoryClass, entityClass);
    }

    /**
     * Holt ein bereits registriertes Repository.
     * Beispiel:
     * PlayerRepository repo = DataCoreAPI.getRepository(PlayerRepository.class);
     */
    @SuppressWarnings("unchecked")
    public static <T extends Repository<?>> T getRepository(Class<T> repositoryClass) {
        return getRegistry().get(repositoryClass);
    }
}
