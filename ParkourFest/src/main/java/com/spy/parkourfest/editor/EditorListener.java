package com.spy.parkourfest.editor;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.editor.pages.MainMenuPage;
import com.spy.parkourfest.model.StageData;
import com.spy.parkourfest.util.ItemBuilder;
import com.spy.parkourfest.util.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central listener for all editor interactions.
 * Manages editor sessions and delegates clicks to the active hotbar page.
 */
public class EditorListener implements Listener {

    private final ParkourFest plugin;
    private final Map<UUID, EditorSession> activeSessions = new ConcurrentHashMap<>();

    public EditorListener(ParkourFest plugin) {
        this.plugin = plugin;
    }

    /**
     * Start an editor session for a player.
     */
    public void startSession(Player player, StageData stageData) {
        UUID uuid = player.getUniqueId();

        // Exit any existing session
        if (activeSessions.containsKey(uuid)) {
            activeSessions.get(uuid).exit(player);
        }

        EditorSession session = new EditorSession(plugin, player, stageData);
        activeSessions.put(uuid, session);
        MessageUtil.success(player, "Entered editor mode for stage: " + stageData.getStageName());
        MessageUtil.info(player, "Use the hotbar tools to configure the stage.");
    }

    /**
     * Exit all active sessions (called on plugin disable).
     */
    public void exitAllSessions() {
        for (Map.Entry<UUID, EditorSession> entry : activeSessions.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                entry.getValue().exit(player);
            }
        }
        activeSessions.clear();
    }

    /**
     * Check if a player is in editor mode.
     */
    public boolean isInEditorMode(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }

    /**
     * Get the editor session for a player.
     */
    public EditorSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    /**
     * Remove a session (called when player exits editor).
     */
    public void removeSession(UUID uuid) {
        activeSessions.remove(uuid);
    }

    // --- Event Handlers ---

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EditorSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        ItemStack item = event.getItem();
        if (item == null || !ItemBuilder.isPFTool(item)) return;

        // Cancel the default interaction
        event.setCancelled(true);

        Action action = event.getAction();
        int slot = player.getInventory().getHeldItemSlot();

        // Get clicked block coordinates
        int bx = -1, by = -1, bz = -1;
        if (event.getClickedBlock() != null) {
            bx = event.getClickedBlock().getX();
            by = event.getClickedBlock().getY();
            bz = event.getClickedBlock().getZ();
        }

        // Delegate to current page
        session.getCurrentPage().handleClick(player, slot, action, bx, by, bz);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!activeSessions.containsKey(player.getUniqueId())) return;

        // Prevent any inventory modification while in editor mode
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!activeSessions.containsKey(event.getPlayer().getUniqueId())) return;

        // Prevent dropping editor tools
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        EditorSession session = activeSessions.remove(uuid);
        if (session != null) {
            // Save stage before they leave
            plugin.getStageManager().save(session.getStageName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        EditorSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Check if chat wizard is active
        ChatWizard wizard = session.getActiveChatWizard();
        if (wizard != null && !wizard.isCancelled()) {
            event.setCancelled(true);
            // Run on main thread since wizard may modify game state
            plugin.getServer().getScheduler().runTask(plugin, () -> wizard.handleInput(player, message));
            return;
        }

        // Check if main menu page is awaiting region type input
        if (session.getCurrentPage() instanceof MainMenuPage mainMenu) {
            if (mainMenu.isAwaitingInput()) {
                event.setCancelled(true);
                plugin.getServer().getScheduler().runTask(plugin, () -> mainMenu.handleChatInput(player, message));
            }
        }
    }
}
