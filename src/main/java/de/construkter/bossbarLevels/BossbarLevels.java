package de.construkter.bossbarLevels;

import de.construkter.bossbarLevels.commands.TextCommands;
import org.bukkit.plugin.java.JavaPlugin;

public final class BossbarLevels extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new TextCommands(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
