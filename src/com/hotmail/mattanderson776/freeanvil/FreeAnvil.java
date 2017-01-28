/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hotmail.mattanderson776.freeanvil;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Manderson
 */
public class FreeAnvil extends JavaPlugin implements Listener {
    
    private HashMap<UUID, Integer> level;
    
    @Override
    public void onEnable() {
        level = new HashMap<>();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onPrepareAnvil (PrepareAnvilEvent e) {
        e.getInventory().setRepairCost(1);
    }
    
    @EventHandler
    public void onPlayerLevelChange (PlayerLevelChangeEvent e) {
        if(level.containsKey(e.getPlayer().getUniqueId())) {
            e.getPlayer().setLevel(e.getOldLevel());
        }
    }
    
    @EventHandler
    public void onInventoryOpen (InventoryOpenEvent e) {
        if(e.getInventory().getType().equals(InventoryType.ANVIL) && e.getPlayer() instanceof Player){
            Player p = Bukkit.getServer().getPlayer(e.getPlayer().getUniqueId());
            int playerLevel = p.getLevel();
            p.setLevel(1);
            new AddExperienceTask(p, playerLevel).runTaskLater(this, 1);
        }
    }
    
    @EventHandler
    public void onInventoryClose (InventoryCloseEvent e) {
        if(e.getInventory().getType().equals(InventoryType.ANVIL) && e.getPlayer() instanceof Player) {
            Player p = Bukkit.getServer().getPlayer(e.getPlayer().getUniqueId());
            p.setLevel(level.get(p.getUniqueId()));
            level.remove(p.getUniqueId());
        }
        
    }
    
    private class AddExperienceTask extends BukkitRunnable {

        private final Player player;
        private final int playerLevel;

        public AddExperienceTask(Player player, int playerLevel) {
            this.playerLevel = playerLevel;
            this.player = player;
        }

        @Override
        public void run() {
            // What you want to schedule goes here
            level.put(player.getUniqueId(), playerLevel);
            this.cancel();
        }
    }
    
}


