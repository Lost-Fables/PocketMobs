package net.lordofthecraft.pocketmobs;

import net.lordofthecraft.omniscience.api.OmniApi;
import net.lordofthecraft.pocketmobs.command.GivePokeballCommand;
import net.lordofthecraft.pocketmobs.listener.PocketListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PocketMobs extends JavaPlugin {

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(new PocketListener(this), this);
        getCommand("givepokeball").setExecutor(new GivePokeballCommand());
        if (Bukkit.getPluginManager().isPluginEnabled("Omniscience")) {
            OmniApi.registerEvent("catch", "caught");
            OmniApi.registerEvent("release", "released");
        }
    }
}
