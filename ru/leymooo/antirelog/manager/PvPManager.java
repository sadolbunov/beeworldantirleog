package ru.leymooo.antirelog.manager;

import com.beeworld.antirelog.BeeWorldAntiRelogPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.leymooo.antirelog.Antirelog;

public final class PvPManager {
   private final Antirelog plugin;

   public PvPManager(Antirelog var1) {
      this.plugin = var1;
   }

   public void stopPvP(Player var1) {
      if (this.plugin instanceof BeeWorldAntiRelogPlugin) {
         BeeWorldAntiRelogPlugin var2 = (BeeWorldAntiRelogPlugin)this.plugin;
         if (var1 != null && !var1.isDead() && var2.settings().duelsDelayAliveRemove()) {
            Bukkit.getScheduler().runTaskLater(var2, () -> var2.combatManager().remove(var1, true), (long)var2.settings().duelsAliveRemoveDelayTicks());
            return;
         }

         var2.combatManager().remove(var1, true);
      }

   }
}
