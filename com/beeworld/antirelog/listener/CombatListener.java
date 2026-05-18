package com.beeworld.antirelog.listener;

import com.beeworld.antirelog.BeeWorldAntiRelogPlugin;
import com.beeworld.antirelog.CombatManager;
import com.beeworld.antirelog.PluginConfig;
import com.beeworld.antirelog.duels.DuelsHook;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class CombatListener implements Listener {
   private final BeeWorldAntiRelogPlugin plugin;
   private final CombatManager combatManager;
   private final PluginConfig config;

   public CombatListener(BeeWorldAntiRelogPlugin var1, CombatManager var2, PluginConfig var3) {
      this.plugin = var1;
      this.combatManager = var2;
      this.config = var3;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onDamage(EntityDamageByEntityEvent var1) {
      if (this.config.tagOnDamage()) {
         if (var1.getEntity() instanceof Player) {
            Player var2 = (Player)var1.getEntity();
            Player var3 = this.attacker(var1.getDamager());
            if (var3 != null && !var3.equals(var2)) {
               if (var3.getWorld() == var2.getWorld()) {
                  if (var2.getGameMode() != GameMode.CREATIVE) {
                     if (!var3.hasMetadata("NPC") && !var2.hasMetadata("NPC")) {
                        if (!var3.isDead() && !var2.isDead()) {
                           if (this.config.isWorldAllowed(var2.getWorld().getName()) && this.config.isWorldAllowed(var3.getWorld().getName())) {
                              this.combatManager.tag(var3, var2, this.config.combatDuration(), false);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDeath(PlayerDeathEvent var1) {
      Player var2 = var1.getEntity().getKiller();
      this.combatManager.markOpponentDead(var1.getEntity().getUniqueId());
      if (this.config.removeOnDeath()) {
         this.combatManager.remove(var1.getEntity(), false);
      }

      if (var2 != null && DuelsHook.isInMatch(var1.getEntity()) && DuelsHook.isInMatch(var2)) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.combatManager.remove(var2, true), (long)this.config.duelsAliveRemoveDelayTicks());
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent var1) {
      Player var2 = var1.getPlayer();
      if (this.combatManager.isTagged(var2)) {
         if (this.config.quitPunishEnabled()) {
            this.punishQuit(var2);
         }

         this.combatManager.remove(var2, false);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onKick(PlayerKickEvent var1) {
      if (this.config.removeOnKick()) {
         this.combatManager.remove(var1.getPlayer(), false);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onCommand(PlayerCommandPreprocessEvent var1) {
      if (this.config.blockCommands() && this.combatManager.isTagged(var1.getPlayer())) {
         String var2 = var1.getMessage().split(" ")[0].toLowerCase(Locale.ROOT);

         for(String var4 : this.config.allowedCommands()) {
            if (var2.equals(var4) || var2.startsWith(var4 + " ")) {
               return;
            }
         }

         var1.setCancelled(true);
         var1.getPlayer().sendMessage(this.config.message("command-blocked"));
      }
   }

   private Player attacker(Entity var1) {
      if (var1 instanceof Player) {
         return (Player)var1;
      } else {
         if (var1 instanceof Projectile) {
            ProjectileSource var2 = ((Projectile)var1).getShooter();
            if (var2 instanceof Player) {
               return (Player)var2;
            }
         }

         return null;
      }
   }

   private void punishQuit(Player var1) {
      if (this.config.quitBroadcastEnabled()) {
         Bukkit.broadcastMessage(this.config.message("quit-broadcast").replace("{player}", var1.getName()));
      }

      for(String var3 : this.config.quitCommands()) {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), var3.replace("{player}", var1.getName()).replace("{uuid}", var1.getUniqueId().toString()));
      }

   }
}
