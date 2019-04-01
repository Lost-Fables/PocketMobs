package net.lordofthecraft.pocketmobs;

import co.lotc.core.bukkit.util.ItemUtil;
import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.omniscience.api.OmniApi;
import net.lordofthecraft.pocketmobs.command.GivePokeballCommand;
import net.lordofthecraft.pocketmobs.listener.PocketListener;
import net.lordofthecraft.pocketmobs.reflection.EntityReflection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class PocketMobs extends JavaPlugin {

    public static ItemStack getPokeballForEntity(Entity entity) {
        ItemStack skull = ItemUtil.getSkullFromTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQzZDRiN2FjMjRhMWQ2NTBkZGY3M2JkMTQwZjQ5ZmMxMmQyNzM2ZmMxNGE4ZGMyNWMwZjNmMjlkODVmOGYifX19");
        ItemMeta meta = skull.getItemMeta();
        if (entity.getCustomName() != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + entity.getCustomName() + ChatColor.GRAY + " [" + entity.getType().name() + "]");
        } else {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + entity.getType().name());
        }
        skull.setItemMeta(meta);
        CustomTag tag = new CustomTag();
        tag.put("entity", EntityReflection.getEntityAsBytes(entity));
        tag.put("unplaceable", "unplaceable");
        tag.put("entity_type", entity.getType().name());
        tag.put("pokeball", "!");
        skull = tag.apply(skull);
        return skull;
    }

    public static ItemStack getEmptyPokeball() {
        ItemStack skull = ItemUtil.getSkullFromTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQzZDRiN2FjMjRhMWQ2NTBkZGY3M2JkMTQwZjQ5ZmMxMmQyNzM2ZmMxNGE4ZGMyNWMwZjNmMjlkODVmOGYifX19");
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Empty");
        skull.setItemMeta(meta);
        CustomTag tag = new CustomTag();
        tag.put("unplaceable", "unplaceable");
        tag.put("pokeball", "!");
        skull = tag.apply(skull);
        return skull;
    }

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
