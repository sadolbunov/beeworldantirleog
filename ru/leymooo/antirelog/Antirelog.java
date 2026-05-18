package ru.leymooo.antirelog;

import org.bukkit.plugin.java.JavaPlugin;
import ru.leymooo.antirelog.manager.PvPManager;

public abstract class Antirelog extends JavaPlugin {
   private final PvPManager pvpManager = new PvPManager(this);

   public PvPManager getPvpManager() {
      return this.pvpManager;
   }
}
