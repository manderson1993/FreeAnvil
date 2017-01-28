/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hotmail.mattanderson776.freeanvil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Manderson
 */
public class FreeAnvil extends JavaPlugin implements Listener {
    
    // A hashmap to store the player's ID and their level before opening an anvil.
    private HashMap<UUID, Integer> level;
    private Logger logger;
    private boolean saved;
    
    // When the plugin is enabled, initialize the hashmap and register events
    @Override
    public void onEnable() {
        logger = getLogger();
        level = new HashMap<>();
        loadHashMap();
        saved = true;
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        logger.info("Plugin Enabled!");
    }
    
    @Override
    public void onDisable() {
        if(!saved)
            saveHashMap();
    }
    
    // When an anvil item is updated, set the repair cost to 1
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        e.getInventory().setRepairCost(1);
    }
    
    // When spending levels, set the player's level to the level before spending.
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent e) {
        if(level.containsKey(e.getPlayer().getUniqueId()))
            e.getPlayer().setLevel(e.getOldLevel());
    }
    
    // When a player opens an anvil, add their level to the hashmap and set their level to 1
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if(e.getInventory().getType().equals(InventoryType.ANVIL) && e.getPlayer() instanceof Player){
            Player p = Bukkit.getServer().getPlayer(e.getPlayer().getUniqueId());
            int playerLevel = p.getLevel();
            p.setLevel(1);
            // Task is run 1 tick later so it isn't changed back by the PlayerLevelChangeEvent listener.
            new AddExperienceTask(p, playerLevel).runTaskLater(this, 1);
        }
    }
    
    // When closing an inventory, revert experience and remove player from hashmap.
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if(e.getPlayer() instanceof Player) {
            restorePlayerLevel(Bukkit.getServer().getPlayer(e.getPlayer().getUniqueId()));
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        restorePlayerLevel(e.getPlayer());
    }
    
    // When a player quits, revert experience and remove player from hashmap
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        restorePlayerLevel(e.getPlayer());
    }
    
    // When the world saves, save the level hashmap to a file (to protect against crashes)
    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
        if(!saved)
            saveHashMap();
    }
    
    // Saves hashmap to file
    private void saveHashMap() {
        try {
            File file = new File(getDataFolder(), "Levels.dat");
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for(UUID p : level.keySet()) {
                    bw.write(p + "," + level.get(p));
                    bw.newLine();
                }
                bw.flush();
            }
            logger.info("Successfully saved levels to file!");
        }
        catch (Exception ex) {
            logger.warning("Error saving levels to file!");
            logger.warning(ex.getMessage());
        }
        saved = true;
    }
    
    // Loads hashmap data from file.
    private void loadHashMap() {
        try {
            File file = new File(getDataFolder(), "Levels.dat");
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                String fileLine;
                while((fileLine = br.readLine()) != null) {
                    String[] args = fileLine.split("[,]", 2);
                    if(args.length != 2)
                        continue;
                    UUID playerID = UUID.fromString(args[0]);
                    Integer playerLevel = Integer.parseInt(args[1]);
                    level.put(playerID, playerLevel);
                }
            }
        }
        catch (Exception ex) {
            logger.warning("Error loading levels from file!");
            logger.warning(ex.getMessage());
        }
    }
    
    private void restorePlayerLevel(Player p) {
        if(level.containsKey(p.getUniqueId())) {
            p.setLevel(level.get(p.getUniqueId()));
            level.remove(p.getUniqueId());
        }
        saved = false;
    }
    
    // A Runnable task that adds a player and their level to the hashmap
    private class AddExperienceTask extends BukkitRunnable {

        private final Player player;
        private final int playerLevel;

        public AddExperienceTask(Player player, int playerLevel) {
            this.playerLevel = playerLevel;
            this.player = player;
        }

        @Override
        public void run() {
            level.put(player.getUniqueId(), playerLevel);
            saved = false;
            this.cancel();
        }
    }
    
    
    
}


