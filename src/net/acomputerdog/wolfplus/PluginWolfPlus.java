package net.acomputerdog.wolfplus;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginWolfPlus extends JavaPlugin implements Listener {

    private Player lastWolfBreeder;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        lastWolfBreeder = null;
    }

    /*
    1. If a player attacks their own wolf, cancel the event unless the wolf is sitting.
       This stops players from hitting their own wolves in combat
    2. If a wolf takes damage from an entity explosion, cancel the event.
       Because creepers exploding your entire wolf pack SUCKS
    3. Wolves cannot take damage from fire
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityHurt(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.WOLF) {
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
    }

    /*
    If a wolf is right-clicked, save that player as the last breeder
      Not always accurate, but the timing would have to be extremely close for this to be an issue.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (entity.getType() == EntityType.WOLF) {
            lastWolfBreeder = e.getPlayer();
        }
    }

    /*
    If a wolf spawns and someone has recently bred a wolf, make them its owner
      So that players can breed other players' wolves and the puppies will be theirs
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.WOLF) {
            if (lastWolfBreeder != null) {
                Wolf wolf = (Wolf)e.getEntity();
                wolf.setOwner(lastWolfBreeder);
                lastWolfBreeder = null;
            }
        }
    }
}
