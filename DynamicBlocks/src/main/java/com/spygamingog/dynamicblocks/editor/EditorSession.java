package com.spygamingog.dynamicblocks.editor;

import com.spygamingog.dynamicblocks.DynamicBlocks;
import com.spygamingog.dynamicblocks.editor.pages.HotbarPage;
import com.spygamingog.dynamicblocks.editor.pages.MechanicsWizardPage;
import com.spygamingog.dynamicblocks.model.LocationData;
import com.spygamingog.dynamicblocks.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Represents an active editor session for a player configuring structures.
 * Holds all working state including block selections and inventory snapshots.
 */
public class EditorSession {

    private final DynamicBlocks plugin;
    private final UUID playerId;

    private final ItemStack[] savedInventory;
    private final ItemStack[] savedArmor;

    private HotbarPage currentPage;
    private final MechanicsWizardPage mechanicsWizardPage;

    private final LinkedHashSet<String> selectedBlockKeys = new LinkedHashSet<>();
    private final List<LocationData> selectedBlockList = new ArrayList<>();

    private String activeStructureId = null;

    public String getActiveStructureId() {
        return activeStructureId;
    }

    public void setActiveStructureId(String activeStructureId) {
        this.activeStructureId = activeStructureId;
        if (activeStructureId != null) {
            com.spygamingog.dynamicblocks.model.MovingStructureData sd = plugin.getStructureManager().getStructureData(activeStructureId);
            if (sd != null) {
                selectedBlockKeys.clear();
                selectedBlockList.clear();
                for (LocationData ld : sd.getBlocks()) {
                    selectedBlockKeys.add((int) ld.getX() + "," + (int) ld.getY() + "," + (int) ld.getZ());
                    selectedBlockList.add(ld);
                }
            }
        }
    }

    public EditorSession(DynamicBlocks plugin, Player player) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();

        this.savedInventory = player.getInventory().getContents().clone();
        this.savedArmor = player.getInventory().getArmorContents().clone();

        this.mechanicsWizardPage = new MechanicsWizardPage(this);
        switchPage(mechanicsWizardPage, player);
    }

    public void switchPage(HotbarPage page, Player player) {
        this.currentPage = page;
        player.getInventory().clear();
        page.setup(player);
        MessageUtil.actionBar(player, "✦ " + page.getPageName() + " ✦");
    }

    /**
     * Toggle a block in the selection. Returns true if block was added, false if removed.
     */
    public boolean toggleBlock(int x, int y, int z) {
        String key = x + "," + y + "," + z;
        boolean added;
        if (selectedBlockKeys.contains(key)) {
            selectedBlockKeys.remove(key);
            selectedBlockList.removeIf(l -> (int) l.getX() == x && (int) l.getY() == y && (int) l.getZ() == z);
            added = false; // Removed
        } else {
            selectedBlockKeys.add(key);
            selectedBlockList.add(new LocationData(x, y, z));
            added = true; // Added
        }

        // If we are editing an active structure, sync directly to the structure!
        if (activeStructureId != null) {
            com.spygamingog.dynamicblocks.model.MovingStructureData sd = plugin.getStructureManager().getStructureData(activeStructureId);
            if (sd != null) {
                sd.setBlocks(new ArrayList<>(selectedBlockList));
                plugin.getStructureManager().saveAll();
                plugin.getStructureManager().spawn(activeStructureId);
            }
        }
        return added;
    }

    /**
     * Check if a block is currently selected.
     */
    public boolean isBlockSelected(int x, int y, int z) {
        return selectedBlockKeys.contains(x + "," + y + "," + z);
    }

    /**
     * Get selected blocks as a list (for structure creation).
     */
    public List<LocationData> getSelectedBlocks() {
        return new ArrayList<>(selectedBlockList);
    }

    /**
     * Get count of selected blocks.
     */
    public int getSelectedBlockCount() {
        return selectedBlockList.size();
    }

    /**
     * Clear all selected blocks.
     */
    public void clearSelectedBlocks() {
        selectedBlockKeys.clear();
        selectedBlockList.clear();
    }

    public void saveAndExit(Player player) {
        plugin.getStructureManager().saveAll();
        MessageUtil.success(player, "Structures saved successfully!");
        exit(player);
    }

    public void exit(Player player) {
        player.getInventory().setContents(savedInventory);
        player.getInventory().setArmorContents(savedArmor);
    }

    public DynamicBlocks getPlugin() { return plugin; }
    public UUID getPlayerId() { return playerId; }
    public HotbarPage getCurrentPage() { return currentPage; }
}
