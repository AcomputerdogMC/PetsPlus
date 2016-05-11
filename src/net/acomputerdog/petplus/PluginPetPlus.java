package net.acomputerdog.petplus;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginPetPlus extends JavaPlugin implements Listener {

    private Player lastWolfBreeder;
    private Player lastCatBreeder;
    private boolean isLoaded = false;

    @Override
    public void onEnable() {
        if (!isLoaded) {
            getServer().getPluginManager().registerEvents(this, this);
            isLoaded = true;
        }
    }

    @Override
    public void onDisable() {
        lastWolfBreeder = null;
        lastCatBreeder = null;
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityHurt(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.WOLF) {
            onWolfHurt(e);
        } else if (isTamedHorse(e.getEntity())) {
            onHorseHurt(e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (entity.getType() == EntityType.WOLF) {
            onRightClickWolf(e);
        } else if (isCat(entity)) {
            onRightClickCat(e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.WOLF) {
            onWolfSpawn(e);
        } else if (isCat(e.getEntity())) {
            onCatSpawn(e);
        }
    }

    private boolean isCat(Entity e) {
        return e.getType() == EntityType.OCELOT && ((Ocelot)e).getCatType() != Ocelot.Type.WILD_OCELOT;
    }

    private boolean isTamedHorse(Entity e) {
        return e.getType() == EntityType.HORSE && ((Horse)e).isTamed();
    }

    /*
      If a pet is right-clicked, save that player as the last breeder.
      Not always accurate, but the timing would have to be extremely close for this to be an issue.
     */
    private void onRightClickWolf(PlayerInteractEntityEvent e) {
        lastWolfBreeder = e.getPlayer();
    }

    private void onRightClickCat(PlayerInteractEntityEvent e) {
        lastCatBreeder = e.getPlayer();
    }

    /*
    If a pet spawns and someone has recently bred a pet of that type, make them its owner
      So that players can breed other players' wolves/cats and the babies will be theirs
     */
    private void onWolfSpawn(EntitySpawnEvent e) {
        if (lastWolfBreeder != null) {
            Wolf wolf = (Wolf)e.getEntity();
            wolf.setOwner(lastWolfBreeder);
            lastWolfBreeder = null;
        }
    }

    private void onCatSpawn(EntitySpawnEvent e) {
        if (lastCatBreeder != null) {
            Ocelot cat = (Ocelot)e.getEntity();
            cat.setOwner(lastCatBreeder);
            lastCatBreeder = null;
        }
    }

    /*
    1. If a player attacks their own wolf, cancel the event unless the wolf is sitting.
       This stops players from hitting their own wolves in combat
    2. If a wolf takes damage from an entity explosion, cancel the event.
       Because creepers exploding your entire wolf pack SUCKS
    3. Wolves cannot take damage from fire
     */
    private void onWolfHurt(EntityDamageEvent e) {
        Wolf wolf = (Wolf)e.getEntity();
        if (e instanceof EntityDamageByEntityEvent) { //wolf hurt by entity
            EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent)e;
            Entity attacker = e2.getDamager();
            if (attacker.getType() == EntityType.PLAYER) {
                Player player = (Player) attacker;
                if (wolf.getOwner().equals(player) && !wolf.isSitting()) {
                    e2.setCancelled(true);
                }
            } else if (attacker.getType() == EntityType.CREEPER) {
                e2.setCancelled(true);
            }
            //wolf hurt by fire
        } else if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            e.setCancelled(true);
        }
    }

    /*
    1. Ridden horses cannot take damage from arrows shot by their owner
    2. Ridden horses don't take damage from fire
     */
    private void onHorseHurt(EntityDamageEvent e) {
        Horse horse = (Horse)e.getEntity();
        Entity passenger = horse.getPassenger();
        if (passenger instanceof Player) {
            if (e instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent) e;
                if (e2.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow)e2.getDamager();
                    if (arrow.getShooter() == passenger) {
                        e.setCancelled(true);
                    }
                }
            } else if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                e.setCancelled(true);
            }
        }
    }
}
