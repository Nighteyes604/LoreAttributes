package me.nighteyes604.LoreAttributes;

import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class LoreAttributes extends JavaPlugin {
	
	public static LoreManager loreManager;
	public static FileConfiguration config = null;
	
	public void onEnable() {
		
		//Plugin metrics
		try {
		    MetricsLite metrics = new MetricsLite(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		
		config = getConfig();
		config.options().copyDefaults(true); //If something missing, load default value
		saveConfig();
		
		if(loreManager==null) {
			loreManager = new LoreManager(this);
		}
		
		Bukkit.getServer().getPluginManager().registerEvents(new LoreEvents(), this);
	}
	
	public void onDisable() {
		HandlerList.unregisterAll(this);		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getLabel().equalsIgnoreCase("hp")) {
			if(!(sender instanceof Player)) {				
				return false;
			} else {
				Player p = (Player)sender;
				p.sendMessage("Health: " + p.getHealth() + "/" + p.getMaxHealth());
				return true;
			}
		}
		if(cmd.getLabel().equalsIgnoreCase("lorestats")) {
			if(!(sender instanceof Player)) {
				return false;
			} else {
				LoreAttributes.loreManager.displayLoreStats((Player)sender);
				return true;
			}
		}
		return false;		
	}
}
