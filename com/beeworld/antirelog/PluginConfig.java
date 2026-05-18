package com.beeworld.antirelog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;

public final class PluginConfig {
   private final BeeWorldAntiRelogPlugin plugin;
   private static final Pattern HEX_COLOR = Pattern.compile("&x(&[0-9a-fA-F]){6}");

   public PluginConfig(BeeWorldAntiRelogPlugin var1) {
      this.plugin = var1;
   }

   public int combatDuration() {
      return Math.max(1, this.getInt("settings.combat-duration-seconds", "pvp-time", 30));
   }

   public int duelsDuration() {
      return Math.max(1, this.plugin.getConfig().getInt("duels.match-start-duration-seconds", this.combatDuration()));
   }

   public boolean tagOnDamage() {
      return this.plugin.getConfig().getBoolean("settings.tag-on-player-damage", true);
   }

   public boolean ignoreCancelledDamage() {
      return this.plugin.getConfig().getBoolean("settings.ignore-cancelled-damage", true);
   }

   public boolean resetTimerOnHit() {
      return this.getBoolean("settings.reset-timer-on-hit", "reset-timer-on-hit", true);
   }

   public boolean removeOnDeath() {
      return this.plugin.getConfig().getBoolean("settings.remove-on-death", true);
   }

   public boolean removeOnKick() {
      return this.plugin.getConfig().getBoolean("settings.remove-on-kick", false);
   }

   public boolean blockCommands() {
      return this.getBoolean("settings.block-commands-in-combat", "disable-commands-in-pvp", true);
   }

   public String commandBypassPermission() {
      return this.plugin.getConfig().getString("settings.blocked-commands-bypass-permission", "beeworldantirelog.admin");
   }

   public String bypassPermission() {
      return this.plugin.getConfig().getString("settings.bypass-permission", "beeworldantirelog.bypass");
   }

   public boolean bossbarEnabled() {
      return this.plugin.getConfig().getBoolean("bossbar.enabled", true);
   }

   public String bossbarTitle() {
      return this.color(this.getString("bossbar.title", "messages.in-pvp-bossbar", "&7До окончания PvP режима &c{time} &7сек."));
   }

   public BarColor bossbarColor() {
      return (BarColor)this.enumValue(BarColor.class, this.plugin.getConfig().getString("bossbar.color", "RED"), BarColor.RED);
   }

   public BarStyle bossbarStyle() {
      return (BarStyle)this.enumValue(BarStyle.class, this.plugin.getConfig().getString("bossbar.style", "SOLID"), BarStyle.SOLID);
   }

   public List<BarFlag> bossbarFlags() {
      ArrayList var1 = new ArrayList();

      for(String var3 : this.plugin.getConfig().getStringList("bossbar.flags")) {
         BarFlag var4 = (BarFlag)this.enumValue(BarFlag.class, var3, (Enum)null);
         if (var4 != null) {
            var1.add(var4);
         }
      }

      return var1;
   }

   public boolean scoreboardEnabled() {
      return this.plugin.getConfig().getBoolean("scoreboard.enabled", true);
   }

   public boolean restorePreviousScoreboard() {
      return this.plugin.getConfig().getBoolean("scoreboard.restore-previous-scoreboard", true);
   }

   public String scoreboardTitle() {
      return this.color(this.plugin.getConfig().getString("scoreboard.title", "&e&lPvP"));
   }

   public List<String> scoreboardLines() {
      return this.color(this.plugin.getConfig().getStringList("scoreboard.lines"));
   }

   public String opponentLineFormat() {
      return this.color(this.plugin.getConfig().getString("scoreboard.opponents.line", this.plugin.getConfig().getString("scoreboard.opponent-line-format", "&c- {player}")));
   }

   public String deadOpponentLineFormat() {
      return this.color(this.plugin.getConfig().getString("scoreboard.dead-opponent-line-format", "&c- {player} &7- &cИгрок умер"));
   }

   public String emptyOpponentsLine() {
      return this.color(this.plugin.getConfig().getString("scoreboard.opponents.empty", this.plugin.getConfig().getString("scoreboard.empty-opponents-line", "&7- none")));
   }

   public List<String> opponentLayout(int var1) {
      List var2 = this.plugin.getConfig().getStringList("scoreboard.opponents." + var1);
      return (List<String>)(var2.isEmpty() ? new ArrayList() : this.color(var2));
   }

   public int maxOpponents() {
      return Math.max(1, this.plugin.getConfig().getInt("scoreboard.max-opponents", 8));
   }

   public boolean duelsEnabled() {
      return this.plugin.getConfig().getBoolean("duels.enabled", true);
   }

   public boolean duelsTagOnStart() {
      return this.plugin.getConfig().getBoolean("duels.tag-on-match-start", false);
   }

   public boolean duelsRemoveOnEnd() {
      return this.plugin.getConfig().getBoolean("duels.remove-on-match-end", true);
   }

   public boolean duelsTagOnCancelledDamage() {
      return this.plugin.getConfig().getBoolean("duels.tag-on-cancelled-damage", true);
   }

   public boolean duelsDelayAliveRemove() {
      return this.plugin.getConfig().getInt("duels.delay-alive-remove-seconds", 3) > 0;
   }

   public int duelsAliveRemoveDelayTicks() {
      return Math.max(0, this.plugin.getConfig().getInt("duels.delay-alive-remove-seconds", 3)) * 20;
   }

   public boolean quitPunishEnabled() {
      return this.plugin.getConfig().getBoolean("quit-punish.enabled", true);
   }

   public boolean quitBroadcastEnabled() {
      return this.plugin.getConfig().getBoolean("quit-punish.broadcast", true);
   }

   public List<String> quitCommands() {
      return this.plugin.getConfig().getStringList("quit-punish.commands");
   }

   public List<String> allowedCommands() {
      ArrayList var1 = new ArrayList();

      for(String var4 : this.plugin.getConfig().contains("settings.allowed-commands") ? this.plugin.getConfig().getStringList("settings.allowed-commands") : this.plugin.getConfig().getStringList("commands-whitelist")) {
         String var5 = var4.toLowerCase(Locale.ROOT).trim();
         if (!var5.isEmpty()) {
            var1.add(var5.startsWith("/") ? var5 : "/" + var5);
         }
      }

      return var1;
   }

   public boolean isWorldAllowed(String var1) {
      String var2 = this.plugin.getConfig().getString("settings.worlds.mode", "blacklist");
      HashSet var3 = new HashSet();
      List var4 = this.plugin.getConfig().getStringList("settings.worlds.list");
      if (var4.isEmpty()) {
         var4 = this.plugin.getConfig().getStringList("disabledWorlds");
      }

      for(String var6 : var4) {
         var3.add(var6.toLowerCase(Locale.ROOT));
      }

      boolean var7 = var3.contains(var1.toLowerCase(Locale.ROOT));
      return "whitelist".equalsIgnoreCase(var2) ? var7 : !var7;
   }

   public String message(String var1) {
      String var2 = this.color(this.plugin.getConfig().getString("messages.prefix", ""));
      return var2 + this.color(this.getString("messages." + var1, this.legacyMessagePath(var1), this.defaultMessage(var1)));
   }

   public List<String> messageList(String var1) {
      ArrayList var2 = new ArrayList();
      String var3 = this.color(this.plugin.getConfig().getString("messages.prefix", ""));
      List var4 = this.plugin.getConfig().getStringList("messages." + var1);
      if (var4.isEmpty() && "usage".equals(var1)) {
         var4.add("&e/antirelog give <ник> [секунды] [противник] &7- выдать режим");
         var4.add("&e/antirelog take <ник> &7- забрать режим");
         var4.add("&e/antirelog status [ник] &7- проверить режим");
         var4.add("&e/antirelog reload &7- перезагрузить конфиг");
      }

      for(String var6 : var4) {
         var2.add(var3 + this.color(var6));
      }

      return var2;
   }

   public SoundSetting sound(String var1) {
      ConfigurationSection var2 = this.plugin.getConfig().getConfigurationSection("sounds." + var1);
      if (var2 != null && var2.getBoolean("enabled", false)) {
         Sound var3 = (Sound)this.enumValue(Sound.class, var2.getString("name", ""), (Enum)null);
         return var3 == null ? PluginConfig.SoundSetting.disabled() : new SoundSetting(true, var3, (float)var2.getDouble("volume", (double)1.0F), (float)var2.getDouble("pitch", (double)1.0F));
      } else {
         return PluginConfig.SoundSetting.disabled();
      }
   }

   public String color(String var1) {
      if (var1 == null) {
         return "";
      } else {
         Matcher var2 = HEX_COLOR.matcher(var1);
         StringBuffer var3 = new StringBuffer();

         while(var2.find()) {
            var2.appendReplacement(var3, Matcher.quoteReplacement(var2.group().replace('&', '§')));
         }

         var2.appendTail(var3);
         return ChatColor.translateAlternateColorCodes('&', var3.toString());
      }
   }

   public List<String> color(List<String> var1) {
      ArrayList var2 = new ArrayList();

      for(String var4 : var1) {
         var2.add(this.color(var4));
      }

      return var2;
   }

   private <T extends Enum<T>> T enumValue(Class<T> var1, String var2, T var3) {
      if (var2 == null) {
         return (T)var3;
      } else {
         try {
            return (T)Enum.valueOf(var1, var2.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var5) {
            return (T)var3;
         }
      }
   }

   private int getInt(String var1, String var2, int var3) {
      return this.plugin.getConfig().contains(var1) ? this.plugin.getConfig().getInt(var1, var3) : this.plugin.getConfig().getInt(var2, var3);
   }

   private boolean getBoolean(String var1, String var2, boolean var3) {
      return this.plugin.getConfig().contains(var1) ? this.plugin.getConfig().getBoolean(var1, var3) : this.plugin.getConfig().getBoolean(var2, var3);
   }

   private String getString(String var1, String var2, String var3) {
      if (this.plugin.getConfig().contains(var1)) {
         return this.plugin.getConfig().getString(var1, var3);
      } else {
         return var2 != null && this.plugin.getConfig().contains(var2) ? this.plugin.getConfig().getString(var2, var3) : var3;
      }
   }

   private String legacyMessagePath(String var1) {
      switch (var1) {
         case "entered-combat":
            return "messages.pvp-started";
         case "left-combat":
            return "messages.pvp-stopped";
         case "command-blocked":
            return "messages.commands-disabled";
         case "quit-broadcast":
            return "messages.pvp-leaved";
         default:
            return null;
      }
   }

   private String defaultMessage(String var1) {
      switch (var1) {
         case "entered-combat":
            return "&6АнтиРелог &7» &fВы вошли в режим PvP. За выход с сервера - &cсмерть";
         case "left-combat":
            return "&6АнтиРелог &7» &fPvP режим &aзакончился&f. Можно спокойно выходить";
         case "combat-given":
            return "&aИгроку &f{player}&a выдан PvP режим на &f{time}&a секунд.";
         case "combat-taken":
            return "&aУ игрока &f{player}&a забран PvP режим.";
         case "target-not-found":
            return "&cИгрок не найден.";
         case "not-in-combat":
            return "&cИгрок не находится в PvP режиме.";
         case "no-permission":
            return "&cНедостаточно прав.";
         case "reloaded":
            return "&aКонфиг перезагружен.";
         case "status-active":
            return "&f{player}&7 в PvP режиме. Осталось: &c{time}s&7. Противники: &c{opponents}&7.";
         case "status-inactive":
            return "&f{player}&7 не находится в PvP режиме.";
         case "command-blocked":
            return "&6АнтиРелог &7» &7Вы не можете использовать команды в режиме PvP.";
         case "quit-broadcast":
            return "";
         default:
            return "";
      }
   }

   public static final class SoundSetting {
      private final boolean enabled;
      private final Sound sound;
      private final float volume;
      private final float pitch;

      private SoundSetting(boolean var1, Sound var2, float var3, float var4) {
         this.enabled = var1;
         this.sound = var2;
         this.volume = var3;
         this.pitch = var4;
      }

      public static SoundSetting disabled() {
         return new SoundSetting(false, (Sound)null, 1.0F, 1.0F);
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      public Sound getSound() {
         return this.sound;
      }

      public float getVolume() {
         return this.volume;
      }

      public float getPitch() {
         return this.pitch;
      }
   }
}
