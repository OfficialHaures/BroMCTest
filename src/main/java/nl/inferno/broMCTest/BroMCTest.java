package nl.inferno.broMCTest;

import nl.inferno.broMCTest.Listeners.AutoCompressor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public final class BroMCTest extends JavaPlugin {

    private AutoCompressor autoCompressor;

    @Override
    public void onEnable() {
        if (!setupWorldGuard()) {
            getLogger().severe("WorldGuard not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        autoCompressor = new AutoCompressor(this);
        getServer().getPluginManager().registerEvents(autoCompressor, this);
    }

    private boolean setupWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        return plugin != null && plugin instanceof WorldGuardPlugin;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
}
