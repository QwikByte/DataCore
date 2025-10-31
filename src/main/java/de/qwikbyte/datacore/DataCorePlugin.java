package de.qwikbyte.datacore;

import de.qwikbyte.datacore.database.DatabaseManager;
import de.qwikbyte.datacore.orm.RepositoryRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public final class DataCorePlugin extends JavaPlugin {


    private static DataCorePlugin instance;

    private DatabaseManager databaseManager;
    private RepositoryRegistry repositoryRegistry;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getLogger().info("Starting DataCore...");

        this.databaseManager = new DatabaseManager(this);
        this.repositoryRegistry = new RepositoryRegistry(databaseManager);

        getLogger().info("DataCore initialized successfully.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("DataCore shut down.");
    }

    public static DataCorePlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RepositoryRegistry getRepositoryRegistry() {
        return repositoryRegistry;
    }

}
