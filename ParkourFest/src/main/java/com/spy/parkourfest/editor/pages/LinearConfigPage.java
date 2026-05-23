package com.spy.parkourfest.editor.pages;

import com.spy.parkourfest.editor.EditorSession;
import com.spy.parkourfest.model.MovingStructureData;
import com.spy.parkourfest.util.ItemBuilder;
import com.spy.parkourfest.util.MessageUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.ArrayList;

/**
 * Page 3: Linear Configuration Menu (Ping-Pong / Sweepers)
 */
public class LinearConfigPage extends HotbarPage {

    public LinearConfigPage(EditorSession session) { super(session); }

    @Override
    public void setup(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS)
                .name("Axis: " + session.getWorkingAxis())
                .lore("Click to cycle: X → Y → Z")
                .toolId("axis_selector").build());
        player.getInventory().setItem(1, new ItemBuilder(Material.SUGAR)
                .name("Speed +").lore("Current: " + String.format("%.1f", session.getWorkingSpeed()) + " b/tick")
                .toolId("speed_up").build());
        player.getInventory().setItem(2, new ItemBuilder(Material.SOUL_SAND)
                .name("Speed −").lore("Current: " + String.format("%.1f", session.getWorkingSpeed()) + " b/tick")
                .toolId("speed_down").build());
        player.getInventory().setItem(3, new ItemBuilder(Material.IRON_BARS)
                .name("Range +").lore("Current: " + String.format("%.1f", session.getWorkingRange()) + " blocks")
                .toolId("range_up").build());
        player.getInventory().setItem(4, new ItemBuilder(Material.CHAIN)
                .name("Range −").lore("Current: " + String.format("%.1f", session.getWorkingRange()) + " blocks")
                .toolId("range_down").build());
        player.getInventory().setItem(7, new ItemBuilder(Material.GREEN_DYE)
                .name("✔ Confirm", NamedTextColor.GREEN).lore("Create this linear platform")
                .toolId("confirm").glow().build());
        player.getInventory().setItem(8, new ItemBuilder(Material.ARROW)
                .name("← Back", NamedTextColor.WHITE).lore("Return to Mechanics Wizard")
                .toolId("back").build());
    }

    @Override
    public boolean handleClick(Player player, int slot, Action action, int bx, int by, int bz) {
        switch (slot) {
            case 0 -> {
                String axis = session.getWorkingAxis();
                axis = switch (axis) { case "X" -> "Y"; case "Y" -> "Z"; default -> "X"; };
                session.setWorkingAxis(axis);
                MessageUtil.actionBar(player, "Axis: " + axis);
                setup(player); // Refresh display
            }
            case 1 -> {
                session.setWorkingSpeed(session.getWorkingSpeed() + 0.1);
                MessageUtil.actionBar(player, "Speed: " + String.format("%.1f", session.getWorkingSpeed()) + " b/tick");
                setup(player);
            }
            case 2 -> {
                session.setWorkingSpeed(Math.max(0.1, session.getWorkingSpeed() - 0.1));
                MessageUtil.actionBar(player, "Speed: " + String.format("%.1f", session.getWorkingSpeed()) + " b/tick");
                setup(player);
            }
            case 3 -> {
                session.setWorkingRange(session.getWorkingRange() + 1.0);
                MessageUtil.actionBar(player, "Range: " + String.format("%.1f", session.getWorkingRange()) + " blocks");
                setup(player);
            }
            case 4 -> {
                session.setWorkingRange(Math.max(1.0, session.getWorkingRange() - 1.0));
                MessageUtil.actionBar(player, "Range: " + String.format("%.1f", session.getWorkingRange()) + " blocks");
                setup(player);
            }
            case 7 -> confirmLinearStructure(player);
            case 8 -> session.switchToMechanicsWizard(player);
            default -> { return false; }
        }
        return true;
    }

    @Override
    public String getPageName() { return "Linear Configuration"; }

    private void confirmLinearStructure(Player player) {
        int count = session.getStageData().countStructuresByType(MovingStructureData.StructureType.LINEAR);
        String id = MovingStructureData.generateId(MovingStructureData.StructureType.LINEAR, count);

        MovingStructureData data = new MovingStructureData(id, MovingStructureData.StructureType.LINEAR);
        data.setBlocks(new ArrayList<>(session.getSelectedBlocks()));
        data.setAxis(session.getWorkingAxis());
        data.setSpeed(session.getWorkingSpeed());
        data.setRange(session.getWorkingRange());
        data.setDirection(session.getWorkingDirection());

        session.getStageData().getMovingStructures().add(data);
        session.clearSelectedBlocks();
        session.resetWorkingState();

        MessageUtil.success(player, "✔ Linear platform '" + id + "' created! ("
                + data.getBlocks().size() + " blocks, Axis=" + data.getAxis()
                + ", Speed=" + String.format("%.1f", data.getSpeed())
                + ", Range=" + String.format("%.1f", data.getRange()) + ")");

        session.switchToMechanicsWizard(player);
    }
}
