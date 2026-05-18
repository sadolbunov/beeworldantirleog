package com.beeworld.antirelog.command;

import com.beeworld.antirelog.BeeWorldAntiRelogPlugin;
import com.beeworld.antirelog.CombatManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class AntiRelogCommand implements CommandExecutor, TabCompleter {
   private final BeeWorldAntiRelogPlugin plugin;
   private final CombatManager combatManager;

   public AntiRelogCommand(BeeWorldAntiRelogPlugin var1, CombatManager var2) {
      this.plugin = var1;
      this.combatManager = var2;
   }

   public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
      if (!var1.hasPermission("beeworldantirelog.admin")) {
         var1.sendMessage(this.plugin.settings().message("no-permission"));
         return true;
      } else if (var4.length == 0) {
         this.usage(var1);
         return true;
      } else {
         String var5 = var4[0].toLowerCase(Locale.ROOT);
         if (var5.equals("reload")) {
            this.plugin.reloadLocalConfig();
            var1.sendMessage(this.plugin.settings().message("reloaded"));
            return true;
         } else if (!var5.equals("give") && !var5.equals("add") && !var5.equals("выдать")) {
            if (!var5.equals("take") && !var5.equals("remove") && !var5.equals("забрать")) {
               if (!var5.equals("status") && !var5.equals("check")) {
                  this.usage(var1);
                  return true;
               } else {
                  this.status(var1, var4);
                  return true;
               }
            } else {
               this.take(var1, var4);
               return true;
            }
         } else {
            this.give(var1, var4);
            return true;
         }
      }
   }

   private void give(CommandSender var1, String[] var2) {
      if (var2.length < 2) {
         this.usage(var1);
      } else {
         Player var3 = Bukkit.getPlayer(var2[1]);
         if (var3 == null) {
            var1.sendMessage(this.plugin.settings().message("target-not-found"));
         } else {
            int var4 = var2.length >= 3 ? this.parseInt(var2[2], this.plugin.settings().combatDuration()) : this.plugin.settings().combatDuration();
            Player var5 = var2.length >= 4 ? Bukkit.getPlayer(var2[3]) : null;
            this.combatManager.tag(var3, var4, var5 == null ? null : var5.getUniqueId(), true);
            var1.sendMessage(this.plugin.settings().message("combat-given").replace("{player}", var3.getName()).replace("{time}", String.valueOf(var4)));
         }
      }
   }

   private void take(CommandSender var1, String[] var2) {
      if (var2.length < 2) {
         this.usage(var1);
      } else {
         Player var3 = Bukkit.getPlayer(var2[1]);
         if (var3 == null) {
            var1.sendMessage(this.plugin.settings().message("target-not-found"));
         } else if (!this.combatManager.remove(var3, true)) {
            var1.sendMessage(this.plugin.settings().message("not-in-combat"));
         } else {
            var1.sendMessage(this.plugin.settings().message("combat-taken").replace("{player}", var3.getName()));
         }
      }
   }

   private void status(CommandSender var1, String[] var2) {
      Player var3;
      if (var2.length >= 2) {
         var3 = Bukkit.getPlayer(var2[1]);
      } else {
         var3 = var1 instanceof Player ? (Player)var1 : null;
      }

      if (var3 == null) {
         var1.sendMessage(this.plugin.settings().message("target-not-found"));
      } else {
         if (this.combatManager.isTagged(var3)) {
            var1.sendMessage(this.combatManager.formatStatus(var3, this.plugin.settings().message("status-active")));
         } else {
            var1.sendMessage(this.plugin.settings().message("status-inactive").replace("{player}", var3.getName()));
         }

      }
   }

   private void usage(CommandSender var1) {
      for(String var3 : this.plugin.settings().messageList("usage")) {
         var1.sendMessage(var3);
      }

   }

   private int parseInt(String var1, int var2) {
      try {
         return Math.max(1, Integer.parseInt(var1));
      } catch (NumberFormatException var4) {
         return var2;
      }
   }

   public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
      if (!var1.hasPermission("beeworldantirelog.admin")) {
         return Collections.emptyList();
      } else if (var4.length == 1) {
         return this.filter(Arrays.asList("give", "take", "status", "reload"), var4[0]);
      } else if ((var4.length == 2 || var4.length == 4) && !var4[0].equalsIgnoreCase("reload")) {
         ArrayList var5 = new ArrayList();

         for(Player var7 : Bukkit.getOnlinePlayers()) {
            var5.add(var7.getName());
         }

         return this.filter(var5, var4[var4.length - 1]);
      } else {
         return var4.length == 3 && var4[0].equalsIgnoreCase("give") ? this.filter(Arrays.asList("10", "30", "60", "300"), var4[2]) : Collections.emptyList();
      }
   }

   private List<String> filter(List<String> var1, String var2) {
      String var3 = var2.toLowerCase(Locale.ROOT);
      ArrayList var4 = new ArrayList();

      for(String var6 : var1) {
         if (var6.toLowerCase(Locale.ROOT).startsWith(var3)) {
            var4.add(var6);
         }
      }

      return var4;
   }
}
