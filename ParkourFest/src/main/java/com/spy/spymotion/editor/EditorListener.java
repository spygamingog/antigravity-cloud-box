package com.spy.spymotion.editor;

import com.spy.spymotion.SpyMotion;
import com.spy.spymotion.editor.pages.HotbarPage;
import com.spy.spymotion.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event listener that routes visual hotbar click events to active editor sessions.
 * Intercepts BlockBreak events to block direct blocks breaking while selecting,
 * and safeguards hotbar tool modification.
 */
public class EditorListener implements Listener {

    private final SpyMotion plugin;
    private final Map<UUID, EditorSession> activeSessions = new HashMap<>();

    public EditorListener(SpyMotion plugin) {
        this.plugin = plugin;
    }

    /**
     * Start a new visual editor session for a player.
     */
    public void startSession(Player player) {
        // Exit any existing session
        exitSession(player.getUniqueId());

        EditorSession session = new EditorSession(plugin, player);
        activeSessions.put(player.getUniqueId(), session);
    }

    /**
     * Clean up and exit a player's editor session.
     */
    public void exitSession(UUID playerId) {
        EditorSession session = activeSessions.remove(playerId);
        if (session != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                session.exit(player);
            }
        }
    }

    /**
     * Terminate all ongoing editor sessions (e.g. on plugin disable).
     */
    public void exitAllSessions() {
        for (UUID uuid : new ArrayList<>(activeSessions.keySet())) {
            exitSession(uuid);
        }
    }

    public boolean isInSession(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EditorSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        // Skip off-hand triggers to prevent double activation
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        ItemStack item = event.getItem();
        if (item == null || !ItemBuilder.isSMTool(item)) return;

        event.setCancelled(true);

        HotbarPage currentPage = session.getCurrentPage();
        if (currentPage == null) return;

        int slot = player.getInventory().getHeldItemSlot();
        Action action = event.getAction();

        int bx = -1, by = -1, bz = -1;
        if (event.hasBlock() && event.getClickedBlock() != null) {
            bx = event.getClickedBlock().getX();
            by = event.getClickedBlock().getY();
            bz = event.getClickedBlock().getZ();
        }

        currentPage.handleClick(player, slot, action, bx, by, bz);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (activeSessions.containsKey(player.getUniqueId())) {
            // Cancel block breaks while in editor mode so players can use left-click selection safely!
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!activeSessions.containsKey(player.getUniqueId())) return;

        // Only prevent modification of slots containing SpyMotion tools
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && ItemBuilder.isSMTool(clickedItem)) {
            event.setCancelled(true);
        }
        
        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && ItemBuilder.isSMTool(cursorItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!activeSessions.containsKey(event.getPlayer().getUniqueId())) return;

        // Only prevent dropping SpyMotion tool items
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (ItemBuilder.isSMTool(droppedItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        exitSession(event.getPlayer().getUniqueId());
    }
}
