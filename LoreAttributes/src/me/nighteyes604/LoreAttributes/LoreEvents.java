package me.nighteyes604.LoreAttributes;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class LoreEvents implements Listener {
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void applyOnInventoryClose(InventoryCloseEvent event) {
		LoreAttributes.loreManager.applyHpBonus(event.getPlayer());
		
		if(event.getPlayer() instanceof Player) {
			LoreAttributes.loreManager.handleArmorRestriction((Player)event.getPlayer());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void applyOnPlayerLogin(PlayerJoinEvent event) {
		LoreAttributes.loreManager.applyHpBonus(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void applyOnPlayerRespawn(PlayerRespawnEvent event) {
		LoreAttributes.loreManager.applyHpBonus(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void applyOnEntityTarget(EntityTargetEvent event) {
		if(event.getEntity() instanceof LivingEntity) {
			LivingEntity e = (LivingEntity)event.getEntity();
			
			LoreAttributes.loreManager.applyHpBonus(e);
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void modifyEntityDamage(EntityDamageByEntityEvent event) {
		if(event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) {
			return;
		}
		
		if(LoreAttributes.loreManager.dodgedAttack((LivingEntity)event.getEntity())) {
			event.setDamage(0);
			event.setCancelled(true);
			return;			
		}
		
		if(event.getDamager() instanceof LivingEntity) {
			LivingEntity damager = (LivingEntity)event.getDamager();
			
			if(damager instanceof Player) {
				if(LoreAttributes.loreManager.canAttack(((Player)damager).getName())) {
					LoreAttributes.loreManager.addAttackCooldown(((Player)damager).getName());
				} else {
					if(!LoreAttributes.config.getBoolean("lore.attack-speed.display-message")) {
						event.setCancelled(true);
						return;
					} else {
						((Player) damager).sendMessage(LoreAttributes.config.getString("lore.attack-speed.message"));
						event.setCancelled(true);
						return;
					}
				}
			}
			
			if(LoreAttributes.loreManager.useRangeOfDamage(damager)) {
				event.setDamage(Math.max(0, LoreAttributes.loreManager.getDamageBonus(damager) - LoreAttributes.loreManager.getArmorBonus((LivingEntity)event.getEntity())));
			} else {
				event.setDamage(Math.max(0, event.getDamage() + LoreAttributes.loreManager.getDamageBonus(damager) - LoreAttributes.loreManager.getArmorBonus((LivingEntity)event.getEntity())));
			}
			
			damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + Math.min(LoreAttributes.loreManager.getLifeSteal(damager),event.getDamage())));			
		} else if(event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if(arrow.getShooter() != null && arrow.getShooter() instanceof LivingEntity) {
				LivingEntity damager = (LivingEntity)arrow.getShooter();				
				
				if(damager instanceof Player) {
					if(LoreAttributes.loreManager.canAttack(((Player)damager).getName())) {
						LoreAttributes.loreManager.addAttackCooldown(((Player)damager).getName());
					} else {
						if(!LoreAttributes.config.getBoolean("lore.attack-speed.display-message")) {
							event.setCancelled(true);
							return;
						} else {
							((Player) damager).sendMessage(LoreAttributes.config.getString("lore.attack-speed.message"));
							event.setCancelled(true);
							return;
						}
					}
				}
				
				if(LoreAttributes.loreManager.useRangeOfDamage(damager)) {
					event.setDamage(Math.max(0, LoreAttributes.loreManager.getDamageBonus(damager) - LoreAttributes.loreManager.getArmorBonus((LivingEntity)event.getEntity())));
				} else {
					event.setDamage(Math.max(0, event.getDamage() + LoreAttributes.loreManager.getDamageBonus(damager)) - LoreAttributes.loreManager.getArmorBonus((LivingEntity)event.getEntity()));
				}
				
				damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + Math.min(LoreAttributes.loreManager.getLifeSteal(damager),event.getDamage())));
			}
		}
	}
	
	/*@EventHandler(priority=EventPriority.MONITOR)
	public void debugDamage(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
			((Player)event.getDamager()).sendMessage(ChatColor.GRAY + "[Lore Debug] Dealt: " + event.getDamage() + " damage. Enemy HP: " + (((LivingEntity)event.getEntity()).getHealth()-event.getDamage()) + "/" + ((LivingEntity)event.getEntity()).getMaxHealth());
		} else if(event.getDamager() instanceof Arrow) {
			if(((Arrow)event.getDamager()).getShooter() instanceof Player) {
				((Player)((Arrow)event.getDamager()).getShooter()).sendMessage(ChatColor.GRAY + "[Lore Debug] Dealt: " + event.getDamage() + " damage. Enemy HP: " + (((LivingEntity)event.getEntity()).getHealth()-event.getDamage()) + "/" + ((LivingEntity)event.getEntity()).getMaxHealth());
			}
		}
	}*/
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void applyHealthRegen(EntityRegainHealthEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(event.getEntity() instanceof Player) {
			if(event.getRegainReason() == RegainReason.SATIATED) {
				event.setAmount(event.getAmount()+LoreAttributes.loreManager.getRegenBonus((LivingEntity)event.getEntity()));
				
				if(event.getAmount()<=0) {
					event.setCancelled(true);
				}
			}
		}		
	}
	
	//Type related / class restriction items
	@EventHandler(priority=EventPriority.HIGHEST)
	public void checkBowRestriction(EntityShootBowEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}
		if(!LoreAttributes.loreManager.canUse((Player)event.getEntity(), event.getBow())) {
			event.setCancelled(true);
		}		
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void checkCraftRestriction(CraftItemEvent event) {
		if(!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		for(ItemStack item : event.getInventory().getContents()) {
			if(!LoreAttributes.loreManager.canUse((Player)event.getWhoClicked(), item)) {
				event.setCancelled(true);
				return;
			}
		}		
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void checkWeaponRestriction(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player)) {
			return;
		}		
		if(!LoreAttributes.loreManager.canUse((Player)event.getDamager(), ((Player)event.getDamager()).getItemInHand())) {
			event.setCancelled(true);
			return;
		}	
	}
	
	
	

}
