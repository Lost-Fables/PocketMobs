package net.lordofthecraft.pocketmobs;

import co.lotc.core.bukkit.util.ItemUtil;
import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.pocketmobs.reflection.EntityReflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class OrbHandler {
    /*public static ItemStack OrbGenerator(Entity entity, int tier) {
        ItemStack skull = ItemUtil.getSkullFromTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg3OWVkMmIzOWZhMDQ2MmM3NDI5MmY1Y2EzZDE4ODQyMDEyOGI0YTYzYWM3NWRiOGM5N2EwOTRkMWFjNjNmNCJ9fX0=");
        ItemMeta meta = skull.getItemMeta();
        if (entity.getCustomName() != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + entity.getCustomName() + ChatColor.GRAY + " [" + entity.getType().name() + "]");
        } else {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Soul Crystal" + ChatColor.GRAY + entity.getType().name());
        }
        skull.setItemMeta(meta);
        CustomTag tag = new CustomTag();
        tag.put("entity", EntityReflection.getEntityAsBytes(entity));
        tag.put("unplaceable", "unplaceable");
        tag.put("entity_type", entity.getType().name());
        tag.put("pokeball", "!");

        switch (tier){
            case 1: tag.put("tier","1");
                break;
            case 2: tag.put("tier","1");
                break;
            case 3: tag.put("tier","1");
                break;
            case 4: tag.put("tier","1");
                break;
            default: tag.put("tier","1");
                break;
        }
        skull = tag.apply(skull);
        return skull;
    }

    public static ItemStack OrbGenerator(int tier) {
        ItemStack skull = ItemUtil.getSkullFromTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg3OWVkMmIzOWZhMDQ2MmM3NDI5MmY1Y2EzZDE4ODQyMDEyOGI0YTYzYWM3NWRiOGM5N2EwOTRkMWFjNjNmNCJ9fX0=");
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Empty");
        skull.setItemMeta(meta);
        CustomTag tag = new CustomTag();
        tag.put("unplaceable", "unplaceable");
        tag.put("pokeball", "!");
        skull = tag.apply(skull);
        return skull;
    }*/

    public static ItemStack OrbGenerator(int tier, Entity entity){
        String[] textures = new String[]{
            /*Tier 0*/ "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDBkNzJkN2VjZmYzMmYzOWU3MDA0Yjc1OGE1M2IxNzljZmZjMTcwMzUwNGY4MzdmNTUwOTA1NzJiODgxMzkwYiJ9fX0=",
            /*Tier 1*/ "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGYwYzQ4MDQzYjIxNTk5NDhlODFjYzU3NWUzZDY5MmFkYjFkNzNiMjViZDAxZDlmNzhiYTg5MmE5NDQ0ZTQyMCJ9fX0=",
            /*Tier 2*/ "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWFiMDllZTE0ZTNhNWRmNTk5Y2ZhNGQxNmM1Y2U4ZDA1NGJlZjg0Njk1MTMyMGY3MjFhYTRhNzE4MWY4ZGI1ZSJ9fX0=",
            /*Tier 3*/ "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGY2MGU2MTgyY2FiMjE1ZDU5ZWEzMmUxYTQ4YTJhNmE5NzNkNzYyMDkzNjc3NGQ1OGIzZmRlOWVmYTcxODEyMyJ9fX0=",
            /*Tier 4*/ "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ3ZWQ0NDliODVkZTEyNTUxZTAxZmVmYjE4MTk4NDA5MTVlMjE1YWQxMzRlMTVlY2QyNzBmMGZkODBiZDMyNCJ9fX0="
        };

        String[] names = new String[]{
            /*Tier 0*/ ChatColor.WHITE + "Petty Soul Orb",
            /*Tier 1*/ ChatColor.GREEN +"Lesser Soul Orb",
            /*Tier 2*/ ChatColor.BLUE +"Common Soul Orb",
            /*Tier 3*/ ChatColor.DARK_PURPLE +"Greater Soul Orb",
            /*Tier 4*/ ChatColor.GOLD +"Grand Soul Orb"
        };

        CustomTag tag = new CustomTag();
        ItemStack skull = ItemUtil.getSkullFromTexture(textures[tier]);
        ItemMeta meta = skull.getItemMeta();
        ArrayList<String> Lore = new ArrayList<String>();

        meta.setDisplayName(names[tier]);
        skull.setItemMeta(meta);

        tag.put("soulorb", Integer.toString(tier));
        tag.put("unplaceable", "unplaceable");

        if (entity != null){
            tag.put("entity", EntityReflection.getEntityAsBytes(entity));
            tag.put("entity_type", entity.getType().name());
        }
        return skull;
    }
}
