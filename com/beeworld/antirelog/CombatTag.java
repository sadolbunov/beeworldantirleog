package com.beeworld.antirelog;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.boss.BossBar;
import org.bukkit.scoreboard.Scoreboard;

final class CombatTag {
   private long endAtMillis;
   private int durationSeconds;
   private final Set<UUID> opponents = new HashSet();
   private final Set<UUID> deadOpponents = new HashSet();
   private BossBar bossBar;
   private Scoreboard previousScoreboard;

   CombatTag(long var1, int var3) {
      this.endAtMillis = var1;
      this.durationSeconds = var3;
   }

   long getEndAtMillis() {
      return this.endAtMillis;
   }

   void setEndAtMillis(long var1) {
      this.endAtMillis = var1;
   }

   int getDurationSeconds() {
      return this.durationSeconds;
   }

   void setDurationSeconds(int var1) {
      this.durationSeconds = var1;
   }

   Set<UUID> getOpponents() {
      return this.opponents;
   }

   Set<UUID> getDeadOpponents() {
      return this.deadOpponents;
   }

   BossBar getBossBar() {
      return this.bossBar;
   }

   void setBossBar(BossBar var1) {
      this.bossBar = var1;
   }

   Scoreboard getPreviousScoreboard() {
      return this.previousScoreboard;
   }

   void setPreviousScoreboard(Scoreboard var1) {
      this.previousScoreboard = var1;
   }
}
