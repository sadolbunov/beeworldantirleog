package com.beeworld.antirelog.tab;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class TabScoreboardHook {
   private final Set<UUID> disabled = new HashSet();
   private final Map<UUID, Object> scoreboards = new HashMap();

   public boolean show(Player var1, String var2, List<String> var3) {
      try {
         Object var4 = this.tabPlayer(var1);
         Object var5 = this.scoreboardManager();
         if (var4 != null && var5 != null) {
            Object var6 = this.scoreboards.get(var1.getUniqueId());
            if (var6 == null) {
               var6 = this.invokeResult(var5, "createScoreboard", "bwar-" + var1.getUniqueId().toString().substring(0, 8), var2, var3);
               this.scoreboards.put(var1.getUniqueId(), var6);
            } else {
               this.updateScoreboard(var6, var2, var3);
            }

            this.invoke(var5, "showScoreboard", var4, var6);
            this.disabled.add(var1.getUniqueId());
            return true;
         } else {
            return false;
         }
      } catch (RuntimeException | LinkageError | ReflectiveOperationException var7) {
         return false;
      }
   }

   public boolean disable(Player var1) {
      if (var1 != null && !this.disabled.contains(var1.getUniqueId())) {
         if (this.toggle(var1, false)) {
            this.disabled.add(var1.getUniqueId());
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void restore(Player var1) {
      if (var1 != null && this.disabled.remove(var1.getUniqueId())) {
         Object var3 = this.scoreboards.remove(var1.getUniqueId());
         if (!this.reset(var1)) {
            this.toggle(var1, true);
         }

         this.unregister(var3);
      } else {
         Object var2 = this.scoreboards.remove(var1 == null ? null : var1.getUniqueId());
         this.unregister(var2);
         this.reset(var1);
      }
   }

   private boolean toggle(Player var1, boolean var2) {
      try {
         Object var3 = this.tabPlayer(var1);
         Object var4 = this.scoreboardManager();
         if (var3 != null && var4 != null) {
            this.invoke(var4, "toggleScoreboard", var3, var2);
            return true;
         } else {
            return false;
         }
      } catch (RuntimeException | LinkageError | ReflectiveOperationException var5) {
         return false;
      }
   }

   private boolean reset(Player var1) {
      try {
         Object var2 = this.tabPlayer(var1);
         Object var3 = this.scoreboardManager();
         if (var2 != null && var3 != null) {
            this.invoke(var3, "resetScoreboard", var2);
            return true;
         } else {
            return false;
         }
      } catch (RuntimeException | LinkageError | ReflectiveOperationException var4) {
         return false;
      }
   }

   private Object tabPlayer(Player var1) throws ReflectiveOperationException {
      Object var2 = this.tabApi();
      if (var2 == null) {
         return null;
      } else {
         try {
            return var2.getClass().getMethod("getPlayer", UUID.class).invoke(var2, var1.getUniqueId());
         } catch (NoSuchMethodException var4) {
            return var2.getClass().getMethod("getPlayer", String.class).invoke(var2, var1.getName());
         }
      }
   }

   private Object scoreboardManager() throws ReflectiveOperationException {
      Object var1 = this.tabApi();
      return var1 == null ? null : var1.getClass().getMethod("getScoreboardManager").invoke(var1);
   }

   private Object tabApi() throws ReflectiveOperationException {
      Plugin var1 = Bukkit.getPluginManager().getPlugin("TAB");
      if (var1 != null && var1.isEnabled()) {
         Class var2 = Class.forName("me.neznamy.tab.api.TabAPI");
         return var2.getMethod("getInstance").invoke((Object)null);
      } else {
         return null;
      }
   }

   private void updateScoreboard(Object var1, String var2, List<String> var3) throws ReflectiveOperationException {
      this.invoke(var1, "setTitle", var2);
      List var4 = (List)this.invokeResult(var1, "getLines");
      int var5 = Math.min(var4.size(), var3.size());

      for(int var6 = 0; var6 < var5; ++var6) {
         this.invoke(var4.get(var6), "setText", var3.get(var6));
      }

      for(int var7 = var4.size() - 1; var7 >= var3.size(); --var7) {
         this.invoke(var1, "removeLine", var7);
      }

      for(int var8 = var4.size(); var8 < var3.size(); ++var8) {
         this.invoke(var1, "addLine", var3.get(var8));
      }

   }

   private void unregister(Object var1) {
      if (var1 != null) {
         this.invokeOptional(var1, "unregister");
      }
   }

   private void invoke(Object var1, String var2, Object... var3) throws ReflectiveOperationException {
      this.invokeResult(var1, var2, var3);
   }

   private Object invokeResult(Object var1, String var2, Object... var3) throws ReflectiveOperationException {
      for(Method var7 : var1.getClass().getMethods()) {
         if (var7.getName().equals(var2) && var7.getParameterTypes().length == var3.length) {
            return var7.invoke(var1, var3);
         }
      }

      throw new NoSuchMethodException(var2);
   }

   private void invokeOptional(Object var1, String var2, Object... var3) {
      try {
         this.invoke(var1, var2, var3);
      } catch (ReflectiveOperationException var5) {
      }

   }
}
