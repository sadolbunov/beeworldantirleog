package com.beeworld.antirelog.duels;

import com.beeworld.antirelog.BeeWorldAntiRelogPlugin;
import com.beeworld.antirelog.CombatManager;
import com.beeworld.antirelog.PluginConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

public final class DuelsHook implements Listener {
   private static final Set<UUID> ACTIVE_DUEL_PLAYERS = new HashSet();
   private final BeeWorldAntiRelogPlugin plugin;
   private final CombatManager combatManager;
   private final PluginConfig config;

   private DuelsHook(BeeWorldAntiRelogPlugin var1, CombatManager var2, PluginConfig var3) {
      this.plugin = var1;
      this.combatManager = var2;
      this.config = var3;
   }

   public static boolean registerIfAvailable(BeeWorldAntiRelogPlugin var0, CombatManager var1, PluginConfig var2) {
      if (!var2.duelsEnabled()) {
         return false;
      } else {
         Plugin var3 = Bukkit.getPluginManager().getPlugin("BeeWorldDuels");
         if (var3 == null) {
            var0.getLogger().info("BeeWorldDuels not found; duels hook will stay disabled.");
            return false;
         } else {
            try {
               ClassLoader var4 = var3.getClass().getClassLoader();
               Class var5 = Class.forName("com.beeworld.duels.api.event.match.MatchStartEvent", false, var4).asSubclass(Event.class);
               Class var6 = Class.forName("com.beeworld.duels.api.event.match.MatchEndEvent", false, var4).asSubclass(Event.class);
               DuelsHook var7 = new DuelsHook(var0, var1, var2);
               Bukkit.getPluginManager().registerEvent(var5, var7, EventPriority.MONITOR, var7.startExecutor(), var0, true);
               Bukkit.getPluginManager().registerEvent(var6, var7, EventPriority.MONITOR, var7.endExecutor(), var0, true);
               var0.getLogger().info("Hooked into BeeWorldDuels.");
               return true;
            } catch (LinkageError | ReflectiveOperationException var8) {
               var0.getLogger().warning("BeeWorldDuels hook disabled: " + ((Throwable)var8).getMessage());
               return false;
            }
         }
      }
   }

   public static boolean isInMatch(Player var0) {
      if (var0 == null) {
         return false;
      } else if (ACTIVE_DUEL_PLAYERS.contains(var0.getUniqueId())) {
         return true;
      } else {
         Plugin var1 = Bukkit.getPluginManager().getPlugin("BeeWorldDuels");
         if (var1 != null && var1.isEnabled()) {
            try {
               ClassLoader var2 = var1.getClass().getClassLoader();
               Class var3 = Class.forName("com.beeworld.duels.api.DuelsAPI", false, var2);
               Method var4 = var3.getMethod("isInMatch", Player.class);
               Object var5 = var4.invoke((Object)null, var0);
               return var5 instanceof Boolean && (Boolean)var5;
            } catch (LinkageError | ReflectiveOperationException var6) {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private EventExecutor startExecutor() {
      return new EventExecutor() {
         public void execute(Listener var1, Event var2) throws EventException {
            if (DuelsHook.this.config.duelsEnabled()) {
               try {
                  List var3 = DuelsHook.this.playersFromMatch(var2);
                  if (var3.isEmpty()) {
                     var3.addAll(DuelsHook.this.playersFromEvent(var2));
                  }

                  DuelsHook.this.rememberPlayers(var3);
                  if (!DuelsHook.this.config.duelsTagOnStart()) {
                     return;
                  }

                  for(Player var5 : var3) {
                     for(Player var7 : var3) {
                        if (!var5.equals(var7)) {
                           DuelsHook.this.combatManager.tag(var5, DuelsHook.this.config.duelsDuration(), var7.getUniqueId(), false);
                        }
                     }
                  }
               } catch (ReflectiveOperationException var8) {
                  DuelsHook.this.plugin.getLogger().warning("Could not handle BeeWorldDuels match start: " + var8.getMessage());
               }

            }
         }
      };
   }

   private EventExecutor endExecutor() {
      return new EventExecutor() {
         public void execute(Listener var1, Event var2) throws EventException {
            if (DuelsHook.this.config.duelsEnabled() && DuelsHook.this.config.duelsRemoveOnEnd()) {
               try {
                  List var3 = DuelsHook.this.startingPlayersFromMatch(var2);
                  DuelsHook.this.forgetPlayers(var3);

                  for(Player var5 : var3) {
                     DuelsHook.this.removeDuelPlayer(var5, true);
                  }

                  DuelsHook.this.removeDuelUuid(DuelsHook.this.call(var2, "getWinner"), true);
                  DuelsHook.this.removeDuelUuid(DuelsHook.this.call(var2, "getLoser"), true);
                  Bukkit.getScheduler().runTaskLater(DuelsHook.this.plugin, () -> {
                     try {
                        for(Player var3 : DuelsHook.this.startingPlayersFromMatch(var2)) {
                           DuelsHook.this.removeDuelPlayer(var3, false);
                        }

                        DuelsHook.this.removeDuelUuid(DuelsHook.this.call(var2, "getWinner"), false);
                        DuelsHook.this.removeDuelUuid(DuelsHook.this.call(var2, "getLoser"), false);
                     } catch (ReflectiveOperationException var4) {
                     }

                  }, 1L);
               } catch (ReflectiveOperationException var6) {
                  DuelsHook.this.plugin.getLogger().warning("Could not handle BeeWorldDuels match end: " + var6.getMessage());
               }

            }
         }
      };
   }

   private List<Player> playersFromEvent(Event var1) throws ReflectiveOperationException {
      ArrayList var2 = new ArrayList();
      Object var3 = this.call(var1, "getPlayers");
      if (var3 instanceof Player[]) {
         for(Player var7 : (Player[])var3) {
            var2.add(var7);
         }
      }

      return var2;
   }

   private List<Player> playersFromMatch(Event var1) throws ReflectiveOperationException {
      Object var2 = this.call(var1, "getMatch");
      Object var3 = this.call(var2, "getPlayers");
      return this.playerCollection(var3);
   }

   private List<Player> startingPlayersFromMatch(Event var1) throws ReflectiveOperationException {
      Object var2 = this.call(var1, "getMatch");
      Object var3 = this.call(var2, "getStartingPlayers");
      return this.playerCollection(var3);
   }

   private List<Player> playerCollection(Object var1) {
      ArrayList var2 = new ArrayList();
      if (var1 instanceof Collection) {
         for(Object var4 : (Collection)var1) {
            if (var4 instanceof Player) {
               var2.add((Player)var4);
            }
         }
      }

      return var2;
   }

   private Object call(Object var1, String var2) throws ReflectiveOperationException {
      Method var3 = var1.getClass().getMethod(var2);
      var3.setAccessible(true);
      return var3.invoke(var1);
   }

   private void rememberPlayers(List<Player> var1) {
      for(Player var3 : var1) {
         if (var3 != null) {
            ACTIVE_DUEL_PLAYERS.add(var3.getUniqueId());
         }
      }

   }

   private void forgetPlayers(List<Player> var1) {
      for(Player var3 : var1) {
         if (var3 != null) {
            ACTIVE_DUEL_PLAYERS.remove(var3.getUniqueId());
         }
      }

   }

   private void removeDuelUuid(Object var1, boolean var2) {
      if (var1 instanceof UUID) {
         Player var3 = Bukkit.getPlayer((UUID)var1);
         if (var3 != null) {
            this.removeDuelPlayer(var3, var2);
         } else {
            this.combatManager.remove((UUID)var1, var2);
         }
      }

   }

   private void removeDuelPlayer(Player var1, boolean var2) {
      if (var1 != null && !var1.isDead() && this.config.duelsDelayAliveRemove()) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.combatManager.remove(var1, var2), (long)this.config.duelsAliveRemoveDelayTicks());
      } else {
         this.combatManager.remove(var1, var2);
      }
   }
}
