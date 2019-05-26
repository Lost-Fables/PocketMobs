package net.lordofthecraft.pocketmobs.listener;

import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.data.DataWrapper;
import net.lordofthecraft.omniscience.api.entry.OEntry;
import net.lordofthecraft.pocketmobs.PocketMobs;
import net.lordofthecraft.pocketmobs.reflection.EntityReflection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PocketListener implements Listener {

    private static final List<EntityType> hostileEntities;

    static {
        hostileEntities = Arrays.asList(EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.STRAY,
                EntityType.SPIDER,
                EntityType.CAVE_SPIDER,
                EntityType.BLAZE,
                EntityType.GHAST,
                EntityType.STRAY,
                EntityType.CREEPER,
                EntityType.GUARDIAN,
                EntityType.HUSK,
                EntityType.IRON_GOLEM);
    }
    private final PocketMobs core;

    public PocketListener(PocketMobs core) {
        this.core = core;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player pl = e.getPlayer();
        if (e.getHand() == EquipmentSlot.HAND) {
            if (CustomTag.hasCustomTag(e.getItem(), "pokeball")) {
                if (CustomTag.hasCustomTag(e.getItem(), "entity")) {
                    shootNonemptyPokeball(pl, e.getItem());
                    Objects.requireNonNull(pl.getEquipment()).setItemInMainHand(removeOneOrDelete(e.getItem()));
                    e.setCancelled(true);
                } else {
                    shootEmptyPokeball(pl);
                    Objects.requireNonNull(pl.getEquipment()).setItemInMainHand(removeOneOrDelete(e.getItem()));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) e.getEntity().getShooter();
            if (e.getEntity().hasMetadata("pokeball")) {
                if (e.getEntity().hasMetadata("entity")) {

                    String entityType = e.getEntity().getMetadata("entity_type").get(0).asString();
                    EntityType type = EntityType.valueOf(entityType);
                    final Entity entity = e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), type);
                    EntityReflection.loadEntityFromNBT(entity, e.getEntity().getMetadata("entity").get(0).asString());
                    if (entity instanceof Tameable && shooter != null) {
                        Tameable tameable = (Tameable) entity;
                        tameable.setOwner(shooter);
                    }
                    e.getEntity().getWorld().dropItemNaturally(entity.getLocation(), PocketMobs.getEmptyPokeball());
                    logRelease(entity, shooter);

                } else {
                    Entity hitEntity = e.getHitEntity();
                    if (hitEntity != null && hitEntity.isValid() && (hitEntity instanceof Animals || hostileEntities.contains(hitEntity.getType())) && e.getEntity().getShooter() != null && e.getEntity().getShooter() instanceof Player) {
                        if (hitEntity instanceof Tameable) {
                            Tameable tameable = (Tameable) hitEntity;
                            if (tameable.isTamed() && tameable.getOwnerUniqueId() == ((Player) e.getEntity().getShooter()).getUniqueId()) {
                                ItemStack pokeball = PocketMobs.getPokeballForEntity(hitEntity);
                                hitEntity.getWorld().dropItemNaturally(hitEntity.getLocation(), pokeball);
                                hitEntity.remove();
                                if (shooter != null) {
                                    shooter.sendMessage(ChatColor.LIGHT_PURPLE + "All right! " + ChatColor.AQUA + getEntityName(hitEntity) + ChatColor.LIGHT_PURPLE + " was caught!");
                                }
                                logCatch(hitEntity, shooter);
                            } else {
                                e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), PocketMobs.getEmptyPokeball());
                            }
                        } else {
                            ItemStack pokeball = PocketMobs.getPokeballForEntity(hitEntity);
                            hitEntity.getWorld().dropItemNaturally(hitEntity.getLocation(), pokeball);
                            hitEntity.remove();
                            if (shooter != null) {
                                shooter.sendMessage(ChatColor.LIGHT_PURPLE + "All right! " + ChatColor.AQUA + getEntityName(hitEntity) + ChatColor.LIGHT_PURPLE + " was caught!");
                            }
                            logCatch(hitEntity, shooter);
                        }
                    } else {
                        e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), PocketMobs.getEmptyPokeball());
                    }

                }
            }
        }
    }

    private void logCatch(Entity entity, Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("Omniscience")) {
            DataWrapper wrapper = DataWrapper.createNew();
            wrapper.set(DataKeys.TARGET, entity.getType().name());
            wrapper.set(DataKeys.ENTITY_TYPE, entity.getType().name());
            OEntry.create().source(player).customWithLocation("catch", wrapper, entity.getLocation()).save();
        }
    }

    private void logRelease(Entity entity, Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("Omniscience")) {
            DataWrapper wrapper = DataWrapper.createNew();
            wrapper.set(DataKeys.TARGET, entity.getType().name());
            wrapper.set(DataKeys.ENTITY_TYPE, entity.getType().name());
            OEntry.create().source(player).customWithLocation("release", wrapper, entity.getLocation()).save();
        }
    }

    private void shootEmptyPokeball(Player pl) {
        Snowball snowball = pl.launchProjectile(Snowball.class);
        snowball.setMetadata("pokeball", new FixedMetadataValue(core, "!"));
    }

    private void shootNonemptyPokeball(Player pl, ItemStack pokeball) {
        Snowball snowball = pl.launchProjectile(Snowball.class);
        snowball.setMetadata("pokeball", new FixedMetadataValue(core, "!"));
        snowball.setMetadata("entity", new FixedMetadataValue(core, CustomTag.getTagValue(pokeball, "entity")));
        snowball.setMetadata("entity_type", new FixedMetadataValue(core, CustomTag.getTagValue(pokeball, "entity_type")));
    }

    private String getEntityName(Entity entity) {
        return entity.getCustomName() == null ? StringUtils.capitalize(entity.getType().name().toLowerCase()) : entity.getCustomName();
    }

    private ItemStack removeOneOrDelete(ItemStack itemstack) {
        if (itemstack.getAmount() > 1) {
            itemstack.setAmount(itemstack.getAmount() - 1);
            return itemstack;
        }
        return null;
    }
}
