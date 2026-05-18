package com.beeworld.antirelog;

import com.beeworld.antirelog.tab.TabScoreboardHook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class CombatManager {
   private final BeeWorldAntiRelogPlugin plugin;
   private PluginConfig config;
   private final Map<UUID, CombatTag> tags = new HashMap();
   private final TabScoreboardHook tabScoreboardHook = new TabScoreboardHook();
   private BukkitTask task;

   public CombatManager(BeeWorldAntiRelogPlugin var1, PluginConfig var2) {
      this.plugin = var1;
      this.config = var2;
   }

   public void setConfig(PluginConfig var1) {
      this.config = var1;
   }

   public void start() {
      this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
   }

   public void shutdown() {
      if (this.task != null) {
         this.task.cancel();
      }

      for(UUID var2 : new HashSet(this.tags.keySet())) {
         this.remove(var2, false);
      }

   }

   public void tag(Player var1, Player var2, int var3, boolean var4) {
      if (var1 != null && var2 != null && !var1.equals(var2)) {
         this.tagSingle(var1, var2.getUniqueId(), var3, var4);
         this.tagSingle(var2, var1.getUniqueId(), var3, var4);
      }
   }

   public void tag(Player var1, int var2, UUID var3, boolean var4) {
      this.tagSingle(var1, var3, var2, var4);
   }

   public boolean remove(Player var1, boolean var2) {
      return var1 != null && this.remove(var1.getUniqueId(), var2);
   }

   public boolean remove(UUID var1, boolean var2) {
      CombatTag var3 = (CombatTag)this.tags.remove(var1);
      if (var3 == null) {
         return false;
      } else {
         Player var4 = Bukkit.getPlayer(var1);
         if (var3.getBossBar() != null) {
            var3.getBossBar().removeAll();
         }

         if (var4 != null && this.config.scoreboardEnabled() && this.config.restorePreviousScoreboard() && var3.getPreviousScoreboard() != null) {
            var4.setScoreboard(var3.getPreviousScoreboard());
         }

         if (var4 != null) {
            this.tabScoreboardHook.restore(var4);
         }

         if (var2 && var4 != null) {
            var4.sendMessage(this.config.message("left-combat"));
            this.play(var4, "left-combat");
         }

         this.refreshAllViews();
         return true;
      }
   }

   public boolean isTagged(Player var1) {
      return var1 != null && this.tags.containsKey(var1.getUniqueId());
   }

   public int remainingSeconds(Player var1) {
      if (var1 == null) {
         return 0;
      } else {
         CombatTag var2 = (CombatTag)this.tags.get(var1.getUniqueId());
         return var2 == null ? 0 : this.remainingSeconds(var2);
      }
   }

   public List<String> opponentNames(Player var1) {
      if (var1 == null) {
         return Collections.emptyList();
      } else {
         CombatTag var2 = (CombatTag)this.tags.get(var1.getUniqueId());
         return var2 == null ? Collections.emptyList() : this.opponentNames(var2);
      }
   }

   public void markOpponentDead(UUID var1) {
      if (var1 != null) {
         for(CombatTag var3 : this.tags.values()) {
            if (var3.getOpponents().contains(var1)) {
               var3.getDeadOpponents().add(var1);
            }
         }

         this.refreshAllViews();
      }
   }

   public void refreshAllViews() {
      for(UUID var2 : this.tags.keySet()) {
         Player var3 = Bukkit.getPlayer(var2);
         if (var3 != null) {
            this.updateView(var3, (CombatTag)this.tags.get(var2));
         }
      }

   }

   private void tagSingle(Player var1, UUID var2, int var3, boolean var4) {
      if (var1 != null) {
         int var5 = Math.max(1, var3);
         long var6 = System.currentTimeMillis() + (long)var5 * 1000L;
         CombatTag var8 = (CombatTag)this.tags.get(var1.getUniqueId());
         boolean var9 = var8 == null;
         if (var9) {
            var8 = new CombatTag(var6, var5);
            var8.setPreviousScoreboard(var1.getScoreboard());
            this.tags.put(var1.getUniqueId(), var8);
         } else if (this.config.resetTimerOnHit() || var4) {
            var8.setEndAtMillis(var6);
            var8.setDurationSeconds(var5);
         }

         if (var2 != null) {
            var8.getOpponents().add(var2);
         }

         this.updateView(var1, var8);
         if (var9 || var4) {
            var1.sendMessage(this.apply(this.config.message("entered-combat"), var1, var8));
            this.play(var1, "entered-combat");
         }

      }
   }

   private void tick() {
      long var1 = System.currentTimeMillis();

      for(UUID var4 : new HashSet(this.tags.keySet())) {
         CombatTag var5 = (CombatTag)this.tags.get(var4);
         if (var5 != null) {
            Player var6 = Bukkit.getPlayer(var4);
            if (var6 != null && var5.getEndAtMillis() > var1) {
               this.updateView(var6, var5);
            } else {
               this.remove(var4, var6 != null);
            }
         }
      }

   }

   private void updateView(Player var1, CombatTag var2) {
      if (this.config.bossbarEnabled()) {
         BossBar var3 = var2.getBossBar();
         if (var3 == null) {
            var3 = Bukkit.createBossBar(this.config.bossbarTitle(), this.config.bossbarColor(), this.config.bossbarStyle(), (BarFlag[])this.config.bossbarFlags().toArray(new BarFlag[0]));
            var3.addPlayer(var1);
            var2.setBossBar(var3);
         }

         var3.setTitle(this.apply(this.config.bossbarTitle(), var1, var2));
         var3.setColor(this.config.bossbarColor());
         var3.setStyle(this.config.bossbarStyle());
         var3.setProgress(this.discreteProgress(var2));
      } else if (var2.getBossBar() != null) {
         var2.getBossBar().removeAll();
         var2.setBossBar((BossBar)null);
      }

      if (this.config.scoreboardEnabled()) {
         List var4 = this.buildScoreboardLines(var1, var2);
         if (!this.tabScoreboardHook.show(var1, this.config.scoreboardTitle(), var4)) {
            var1.setScoreboard(this.createScoreboard(var1, var4));
         }
      } else {
         this.tabScoreboardHook.restore(var1);
      }

   }

   private Scoreboard createScoreboard(Player var1, List<String> var2) {
      Scoreboard var3 = Bukkit.getScoreboardManager().getNewScoreboard();
      Objective var4 = var3.registerNewObjective("bwar", "dummy", this.trimVisible(this.config.scoreboardTitle(), 128));
      var4.setDisplaySlot(DisplaySlot.SIDEBAR);
      int var5 = var2.size();
      int var6 = 0;

      for(String var8 : var2) {
         String var9 = ChatColor.values()[var6 % ChatColor.values().length].toString() + ChatColor.values()[var6 / ChatColor.values().length % ChatColor.values().length];
         Team var10 = var3.registerNewTeam("l" + var6);
         String[] var11 = this.splitLine(var8);
         var10.setPrefix(var11[0]);
         var10.setSuffix(var11[1]);
         var10.addEntry(var9);
         var4.getScore(var9).setScore(var5--);
         ++var6;
      }

      return var3;
   }

   private List<String> buildScoreboardLines(Player var1, CombatTag var2) {
      ArrayList var3 = new ArrayList();

      for(String var5 : this.config.scoreboardLines()) {
         String var6 = ChatColor.stripColor(var5);
         if (!"{opponent_lines}".equals(var6) && !"%opponents".equals(var6 == null ? "" : var6.trim())) {
            var3.add(this.apply(var5, var1, var2));
         } else {
            var3.addAll(this.opponentLines(var2));
         }
      }

      return var3;
   }

   private List<String> opponentLines(CombatTag var1) {
      ArrayList var2 = new ArrayList();
      List var3 = this.sortedOpponents(var1);
      if (var3.isEmpty()) {
         var2.add(this.config.emptyOpponentsLine());
         return var2;
      } else {
         int var4 = Math.min(Math.min(var3.size(), this.config.maxOpponents()), 8);
         ArrayList var5 = new ArrayList();

         for(int var6 = 0; var6 < var4; ++var6) {
            UUID var7 = (UUID)var3.get(var6);
            Player var8 = Bukkit.getPlayer(var7);
            String var9 = var1.getDeadOpponents().contains(var7) ? this.config.deadOpponentLineFormat() : this.config.opponentLineFormat();
            var5.add(var9.replace("{player}", this.playerName(var7)).replace("%player", this.playerName(var7)).replace("%health", String.valueOf(this.health(var8))).replace("%maxhealth", String.valueOf(this.maxHealth(var8))));
         }

         List var11 = this.config.opponentLayout(var4);
         if (var11.isEmpty()) {
            return var5;
         } else {
            for(String var13 : var11) {
               String var14 = var13;

               for(int var10 = 0; var10 < var5.size(); ++var10) {
                  var14 = var14.replace("%" + (var10 + 1), (CharSequence)var5.get(var10));
               }

               var2.add(var14);
            }

            return var2;
         }
      }
   }

   private double discreteProgress(CombatTag var1) {
      int var2 = Math.max(1, var1.getDurationSeconds());
      int var3 = this.remainingSeconds(var1);
      return Math.max((double)0.0F, Math.min((double)1.0F, (double)var3 / (double)var2));
   }

   private int remainingSeconds(CombatTag var1) {
      return (int)Math.ceil((double)Math.max(0L, var1.getEndAtMillis() - System.currentTimeMillis()) / (double)1000.0F);
   }

   private String apply(String var1, Player var2, CombatTag var3) {
      String var4 = String.join(", ", this.opponentNames(var3));
      if (var4.isEmpty()) {
         var4 = "-";
      }

      return var1.replace("{player}", var2.getName()).replace("{time}", String.valueOf(this.remainingSeconds(var3))).replace("{opponents}", var4).replace("%time%", String.valueOf(this.remainingSeconds(var3))).replace("%time!", String.valueOf(this.remainingSeconds(var3))).replace("%time", String.valueOf(this.remainingSeconds(var3))).replace("%opponents%", var4).replace("%opponents", var4);
   }

   private List<String> opponentNames(CombatTag var1) {
      ArrayList var2 = new ArrayList();

      for(UUID var4 : var1.getOpponents()) {
         var2.add(this.playerName(var4));
      }

      Collections.sort(var2, String.CASE_INSENSITIVE_ORDER);
      return var2;
   }

   private List<UUID> sortedOpponents(CombatTag var1) {
      ArrayList var2 = new ArrayList(var1.getOpponents());
      Collections.sort(var2, (var1x, var2x) -> this.playerName(var1x).compareToIgnoreCase(this.playerName(var2x)));
      return var2;
   }

   private String playerName(UUID var1) {
      Player var2 = Bukkit.getPlayer(var1);
      if (var2 != null) {
         return var2.getName();
      } else {
         OfflinePlayer var3 = Bukkit.getOfflinePlayer(var1);
         return var3.getName() == null ? var1.toString().substring(0, 8) : var3.getName();
      }
   }

   private void play(Player var1, String var2) {
      PluginConfig.SoundSetting var3 = this.config.sound(var2);
      if (var3.isEnabled()) {
         var1.playSound(var1.getLocation(), var3.getSound(), var3.getVolume(), var3.getPitch());
      }

   }

   private String trim(String var1, int var2) {
      return var1.length() <= var2 ? var1 : var1.substring(0, var2);
   }

   private String unique(String var1, Set<String> var2) {
      String var3 = var1;

      for(int var4 = 0; var2.contains(var3); ++var4) {
         var3 = this.trim(var1, Math.max(0, 38 - var4)) + ChatColor.values()[var4 % ChatColor.values().length];
      }

      var2.add(var3);
      return var3;
   }

   private int health(Player var1) {
      return var1 == null ? 0 : (int)Math.round(Math.max((double)0.0F, var1.getHealth()));
   }

   private int maxHealth(Player var1) {
      if (var1 == null) {
         return 20;
      } else {
         AttributeInstance var2 = var1.getAttribute(Attribute.GENERIC_MAX_HEALTH);
         return var2 == null ? 20 : (int)Math.round(var2.getValue());
      }
   }

   private String[] splitLine(String var1) {
      if (var1.length() <= 64) {
         return new String[]{var1, ""};
      } else {
         int var2 = this.safeSplitIndex(var1, 64);
         String var3 = var1.substring(0, var2);
         String var4 = ChatColor.getLastColors(var3) + var1.substring(var2);
         if (var4.length() > 64) {
            var4 = var4.substring(0, this.safeSplitIndex(var4, 64));
         }

         return new String[]{var3, var4};
      }
   }

   private int safeSplitIndex(String var1, int var2) {
      int var3 = Math.min(var2, var1.length());
      if (var3 > 0 && var1.charAt(var3 - 1) == 167) {
         --var3;
      }

      return var3;
   }

   private String trimVisible(String var1, int var2) {
      return var1.length() <= var2 ? var1 : var1.substring(0, this.safeSplitIndex(var1, var2));
   }

   public String formatStatus(Player var1, String var2) {
      CombatTag var3 = (CombatTag)this.tags.get(var1.getUniqueId());
      return var3 == null ? var2.replace("{player}", var1.getName()).replace("{time}", "0").replace("{opponents}", "-") : this.apply(var2, var1, var3);
   }
}
