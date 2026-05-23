package com.spy.parkourfest.editor.pages;

import com.spy.parkourfest.editor.ChatWizard;
import com.spy.parkourfest.editor.EditorSession;
import com.spy.parkourfest.model.CheckpointData;
import com.spy.parkourfest.model.LocationData;
import com.spy.parkourfest.model.RegionData;
import com.spy.parkourfest.util.ItemBuilder;
import com.spy.parkourfest.util.MessageUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

/**
 * Page 1: Main Setup Menu
 * 
 * Slot 0: Region Wand (Wooden Axe) — Left=Pos1, Right=Pos2
 * Slot 1: Region Definer (Blaze Powder) — Assign selection as Start/Finish
 * Slot 2: Checkpoint Tool (Pressure Plate) — Left=Add, Right=Remove
 * Slot 3: Dynamic Mechanisms (Repeater) — Opens Mechanics Wizard (Page 2)
 * Slot 4: Chat Tweaks Panel (Book) — Opens interactive chat wizard
 * Slot 8: Save & Exit (Barrier)
 */
public class MainMenuPage extends HotbarPage {

    // Chat state for region definer
    private boolean awaitingRegionType = false;

    public MainMenuPage(EditorSession session) {
        super(session);
    }

    @Override
    public void setup(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.WOODEN_AXE)
                .name("Region Wand")
                .lore("Left-click block: Set Pos1")
                .lore("Right-click block: Set Pos2")
                .toolId("region_wand")
                .build());

        player.getInventory().setItem(1, new ItemBuilder(Material.BLAZE_POWDER)
                .name("Region Definer")
                .lore("Right-click: Assign selection")
                .lore("as START or FINISH region")
                .toolId("region_definer")
                .build());

        player.getInventory().setItem(2, new ItemBuilder(Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .name("Checkpoint Tool")
                .lore("Left-click block: Add checkpoint")
                .lore("Right-click: Remove nearest")
                .toolId("checkpoint_tool")
                .build());

        player.getInventory().setItem(3, new ItemBuilder(Material.REPEATER)
                .name("Dynamic Mechanisms")
                .lore("Right-click: Open Mechanics Wizard")
                .lore("Create moving platforms & obstacles")
                .toolId("mechanics_wizard")
                .glow()
                .build());

        player.getInventory().setItem(4, new ItemBuilder(Material.BOOK)
                .name("Chat Tweaks Panel")
                .lore("Right-click: Configure limits,")
                .lore("countdown, and PVP via chat")
                .toolId("chat_tweaks")
                .build());

        player.getInventory().setItem(8, new ItemBuilder(Material.BARRIER)
                .name("Save & Exit", NamedTextColor.RED)
                .lore("Right-click: Save and exit editor")
                .toolId("save_exit")
                .build());
    }

    @Override
    public boolean handleClick(Player player, int slot, Action action,
                                int clickedBlockX, int clickedBlockY, int clickedBlockZ) {
        switch (slot) {
            case 0 -> handleRegionWand(player, action, clickedBlockX, clickedBlockY, clickedBlockZ);
            case 1 -> handleRegionDefiner(player, action);
            case 2 -> handleCheckpointTool(player, action, clickedBlockX, clickedBlockY, clickedBlockZ);
            case 3 -> {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    session.switchToMechanicsWizard(player);
                }
            }
            case 4 -> {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    startChatWizard(player);
                }
            }
            case 8 -> {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    session.saveAndExit(player);
                }
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public String getPageName() {
        return "Main Setup Menu";
    }

    private void handleRegionWand(Player player, Action action,
                                    int blockX, int blockY, int blockZ) {
        if (action == Action.LEFT_CLICK_BLOCK) {
            LocationData pos1 = LocationData.fromBlock(blockX, blockY, blockZ);
            session.setPos1Selection(pos1);
            MessageUtil.actionBar(player, "Pos1 set to (" + blockX + ", " + blockY + ", " + blockZ + ")");
            MessageUtil.success(player, "Position 1 set at (" + blockX + ", " + blockY + ", " + blockZ + ")");
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            LocationData pos2 = LocationData.fromBlock(blockX, blockY, blockZ);
            session.setPos2Selection(pos2);
            MessageUtil.actionBar(player, "Pos2 set to (" + blockX + ", " + blockY + ", " + blockZ + ")");
            MessageUtil.success(player, "Position 2 set at (" + blockX + ", " + blockY + ", " + blockZ + ")");
        }
    }

    private void handleRegionDefiner(Player player, Action action) {
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        if (session.getPos1Selection() == null || session.getPos2Selection() == null) {
            MessageUtil.error(player, "You must set Pos1 and Pos2 with the Region Wand first!");
            return;
        }

        // Enter awaiting state — prompt the player to type START or FINISH in chat
        awaitingRegionType = true;
        MessageUtil.info(player, "Type 'START' or 'FINISH' in chat to assign this region.");
        MessageUtil.info(player, "Type 'cancel' to abort.");
    }

    /**
     * Handle chat input when awaiting region type assignment.
     */
    public boolean handleChatInput(Player player, String message) {
        if (!awaitingRegionType) return false;

        awaitingRegionType = false;
        String input = message.trim().toUpperCase();

        if (input.equals("CANCEL")) {
            MessageUtil.info(player, "Region assignment cancelled.");
            return true;
        }

        String worldName = player.getWorld().getName();

        if (input.equals("START")) {
            RegionData region = new RegionData(worldName, session.getPos1Selection(), session.getPos2Selection());
            session.getStageData().setStartRegion(region);
            session.getStageData().setWorldName(worldName);
            MessageUtil.success(player, "✔ Start Region defined!");
            return true;
        } else if (input.equals("FINISH")) {
            RegionData region = new RegionData(worldName, session.getPos1Selection(), session.getPos2Selection());
            session.getStageData().setFinishRegion(region);
            MessageUtil.success(player, "✔ Finish Region defined!");
            return true;
        }

        MessageUtil.error(player, "Invalid input. Type 'START', 'FINISH', or 'cancel'.");
        awaitingRegionType = true; // Keep waiting
        return true;
    }

    public boolean isAwaitingInput() {
        return awaitingRegionType;
    }

    private void handleCheckpointTool(Player player, Action action,
                                       int blockX, int blockY, int blockZ) {
        if (action == Action.LEFT_CLICK_BLOCK) {
            // Add checkpoint at clicked block
            int nextId = session.getStageData().getNextCheckpointId();
            LocationData loc = LocationData.fromBlock(blockX, blockY + 1, blockZ); // On top of block
            CheckpointData checkpoint = new CheckpointData(nextId, loc);
            session.getStageData().getCheckpoints().add(checkpoint);
            MessageUtil.success(player, "✔ Checkpoint #" + nextId + " placed at ("
                    + blockX + ", " + (blockY + 1) + ", " + blockZ + ")");
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            // Remove nearest checkpoint
            LocationData playerLoc = LocationData.from(player.getLocation());
            CheckpointData nearest = session.getStageData().findNearestCheckpoint(playerLoc);
            if (nearest != null) {
                session.getStageData().removeCheckpoint(nearest.getId());
                MessageUtil.success(player, "✔ Removed Checkpoint #" + nearest.getId());
            } else {
                MessageUtil.error(player, "No checkpoints to remove.");
            }
        }
    }

    private void startChatWizard(Player player) {
        if (session.getActiveChatWizard() != null) {
            MessageUtil.error(player, "A chat wizard is already active! Complete or cancel it first.");
            return;
        }

        ChatWizard wizard = new ChatWizard(session, player);
        session.setActiveChatWizard(wizard);
        wizard.start();
    }
}
