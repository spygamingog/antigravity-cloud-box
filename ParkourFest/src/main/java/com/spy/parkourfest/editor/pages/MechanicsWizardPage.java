package com.spy.parkourfest.editor.pages;

import com.spy.parkourfest.editor.EditorSession;
import com.spy.parkourfest.model.LocationData;
import com.spy.parkourfest.util.ItemBuilder;
import com.spy.parkourfest.util.MessageUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class MechanicsWizardPage extends HotbarPage {

    public MechanicsWizardPage(EditorSession session) { super(session); }

    @Override
    public void setup(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.STICKY_PISTON)
                .name("Linear Mover").lore("Right-click: Configure ping-pong platform").toolId("linear_mover").build());
        player.getInventory().setItem(1, new ItemBuilder(Material.CLOCK)
                .name("Rotator").lore("Right-click: Configure rotating platform").toolId("rotator").build());
        player.getInventory().setItem(2, new ItemBuilder(Material.GLOWSTONE_DUST)
                .name("Structure Wand", NamedTextColor.AQUA)
                .lore("Click blocks to select them").lore("Selected: " + session.getSelectedBlocks().size())
                .toolId("structure_wand").glow().build());
        player.getInventory().setItem(4, new ItemBuilder(Material.REDSTONE_TORCH)
                .name("Test Controls", NamedTextColor.RED)
                .lore("L-click: Start | R-click: Stop").lore("Shift+R: Reset")
                .toolId("test_controls").build());
        player.getInventory().setItem(8, new ItemBuilder(Material.ARROW)
                .name("← Back", NamedTextColor.WHITE).lore("Return to Main Menu").toolId("back").build());
    }

    @Override
    public boolean handleClick(Player player, int slot, Action action, int bx, int by, int bz) {
        switch (slot) {
            case 0 -> { if (isRightClick(action)) {
                if (session.getSelectedBlocks().isEmpty()) { MessageUtil.error(player, "Select blocks first! (Slot 2)"); return true; }
                session.switchToLinearConfig(player); } }
            case 1 -> { if (isRightClick(action)) {
                if (session.getSelectedBlocks().isEmpty()) { MessageUtil.error(player, "Select blocks first! (Slot 2)"); return true; }
                session.switchToRotationConfig(player); } }
            case 2 -> {
                if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                    session.getSelectedBlocks().add(new LocationData(bx, by, bz));
                    int c = session.getSelectedBlocks().size();
                    MessageUtil.actionBar(player, "Block added! Total: " + c);
                    player.getInventory().setItem(2, new ItemBuilder(Material.GLOWSTONE_DUST)
                            .name("Structure Wand", NamedTextColor.AQUA).lore("Selected: " + c)
                            .toolId("structure_wand").glow().build());
                } else if (action == Action.LEFT_CLICK_AIR) {
                    session.clearSelectedBlocks();
                    MessageUtil.info(player, "Selection cleared.");
                    player.getInventory().setItem(2, new ItemBuilder(Material.GLOWSTONE_DUST)
                            .name("Structure Wand", NamedTextColor.AQUA).lore("Selected: 0")
                            .toolId("structure_wand").glow().build());
                }
            }
            case 4 -> MessageUtil.info(player, "Test controls: engine integration in Phase 5")
            ;case 8 -> { if (isRightClick(action)) session.switchToMainMenu(player); }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public String getPageName() { return "Mechanics Wizard"; }

    private boolean isRightClick(Action a) {
        return a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK;
    }
}
