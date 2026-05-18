package com.beeworld.antirelog;

import com.beeworld.antirelog.command.AntiRelogCommand;
import com.beeworld.antirelog.duels.DuelsHook;
import com.beeworld.antirelog.listener.CombatListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import ru.leymooo.antirelog.Antirelog;

public final class BeeWorldAntiRelogPlugin extends Antirelog implements Listener {
   private CombatManager combatManager;
   private PluginConfig pluginConfig;
   private boolean duelsHooked;

   public void onEnable() {
      this.saveDefaultConfig();
      this.reloadLocalConfig();
      this.combatManager = new CombatManager(this, this.pluginConfig);
      this.combatManager.start();
      this.getServer().getPluginManager().registerEvents(new CombatListener(this, this.combatManager, this.pluginConfig), this);
      this.getServer().getPluginManager().registerEvents(this, this);
      this.duelsHooked = DuelsHook.registerIfAvailable(this, this.combatManager, this.pluginConfig);
      AntiRelogCommand var1 = new AntiRelogCommand(this, this.combatManager);
      PluginCommand var2 = this.getCommand("antirelog");
      if (var2 != null) {
         var2.setExecutor(var1);
         var2.setTabCompleter(var1);
      }

   }

   public void onDisable() {
      if (this.combatManager != null) {
         this.combatManager.shutdown();
      }

   }

   public void reloadLocalConfig() {
      this.reloadConfig();
      this.pluginConfig = new PluginConfig(this);
      if (this.combatManager != null) {
         this.combatManager.setConfig(this.pluginConfig);
         this.combatManager.refreshAllViews();
      }

   }

   public PluginConfig settings() {
      return this.pluginConfig;
   }

   public CombatManager combatManager() {
      return this.combatManager;
   }

   public void removeCooldown(Player var1) {
      this.combatManager.remove(var1, true);
   }

   public void clearCooldown(Player var1) {
      this.combatManager.remove(var1, true);
   }

   @EventHandler
   public void onPluginEnable(PluginEnableEvent var1) {
      if (!this.duelsHooked && "BeeWorldDuels".equalsIgnoreCase(var1.getPlugin().getName())) {
         this.duelsHooked = DuelsHook.registerIfAvailable(this, this.combatManager, this.pluginConfig);
      }

   }
}
