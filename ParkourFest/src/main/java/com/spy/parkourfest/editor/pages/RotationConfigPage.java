package com.spy.parkourfest.editor.pages;

import com.spy.parkourfest.editor.EditorSession;
import com.spy.parkourfest.model.LocationData;
import com.spy.parkourfest.model.MovingStructureData;
import com.spy.parkourfest.util.ItemBuilder;
import com.spy.parkourfest.util.MessageUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.ArrayList;

/**
 * Page 4: Rotation Configuration Menu (Spinners)
 */
public class RotationConfigPage extends HotbarPage {

    public RotationConfigPage(EditorSession session) { super(session); }

    @Override
    public void setup(Player player) {
        String dirLabel = session.getWorkingDirection() == MovingStructureData.Direction.CLOCKWISE
                ? "Clockwise" : "Counter-CW";
        String centerLabel = session.getWorkingCenter() != null
                ? session.getWorkingCenter().toString() : "Not set";

        player.getInventory().setItem(0, new ItemBuilder(Material.LODESTONE)
                .name("Center Anchor").lore("Right-click block to set pivot")
                .lore("Current: " + centerLabel).toolId("center_anchor").build());
        player.getInventory().setItem(1, new ItemBuilder(Material.SUNFLOWER)
                .name("Clockwise", NamedTextColor.YELLOW).lore("Set rotation to clockwise")
                .toolId("dir_cw").build());
        player.getInventory().setItem(2, new ItemBuilder(Material.BLUE_ORCHID)
                .name("Counter-CW", NamedTextColor.AQUA).lore("Set rotation to counter-clockwise")
                .toolId("dir_ccw").build());
        player.getInventory().setItem(3, new ItemBuilder(Material.SUGAR)
                .name("RPM +").lore("Current: " + String.format("%.1f", session.getWorkingSpeed()) + " deg/tick")
                .toolId("rpm_up").build());
        player.getInventory().setItem(4, new ItemBuilder(Material.SOUL_SAND)
                .name("RPM −").lore("Current: " + String.format("%.1f", session.getWorkingSpeed()) + " deg/tick")
                .toolId("rpm_down").build());
        player.getInventory().setItem(7, new ItemBuilder(Material.GREEN_DYE)
                .name("✔ Confirm", NamedTextColor.GREEN).lore("Create this rotational platform")
                .toolId("confirm").glow().build());
        player.getInventory().setItem(8, new ItemBuilder(Material.ARROW)
                .name("← Back", NamedTextColor.WHITE).lore("Return to Mechanics Wizard")
                .toolId("back").build());
    }

    @Override
    public boolean handleClick(Player player, int slot, Action action, int bx, int by, int bz) {
        switch (slot) {
            case 0 -> {
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    session.setWorkingCenter(new LocationData(bx + 0.5, by, bz + 0.5));
                    MessageUtil.success(player, "✔ Pivot set at (" + bx + ", " + by + ", " + bz + ")");
                    setup(player);
                }
            }
            case 1 -> {
                session.setWorkingDirection(MovingStructureData.Direction.CLOCKWISE);
                MessageUtil.actionBar(player, "Direction: Clockwise");
                setup(player);
            }
            case 2 -> {
                session.setWorkingDirection(MovingStructureData.Direction.COUNTER_CLOCKWISE);
                MessageUtil.actionBar(player, "Direction: Counter-Clockwise");
                setup(player);
            }
            case 3 -> {
                session.setWorkingSpeed(session.getWorkingSpeed() + 0.5);
                MessageUtil.actionBar(player, "RPM: " + String.format("%.1f", session.getWorkingSpeed()) + " deg/tick");
                setup(player);
            }
            case 4 -> {
                session.setWorkingSpeed(Math.max(0.5, session.getWorkingSpeed() - 0.5));
                MessageUtil.actionBar(player, "RPM: " + String.format("%.1f", session.getWorkingSpeed()) + " deg/tick");
                setup(player);
            }
            case 7 -> confirmRotationalStructure(player);
            case 8 -> session.switchToMechanicsWizard(player);
            default -> { return false; }
        }
        return true;
    }

    @Override
    public String getPageName() { return "Rotation Configuration"; }

    private void confirmRotationalStructure(Player player) {
        if (session.getWorkingCenter() == null) {
            MessageUtil.error(player, "Set a center pivot point first! (Slot 0)");
            return;
        }

        int count = session.getStageData().countStructuresByType(MovingStructureData.StructureType.ROTATIONAL);
        String id = MovingStructureData.generateId(MovingStructureData.StructureType.ROTATIONAL, count);

        MovingStructureData data = new MovingStructureData(id, MovingStructureData.StructureType.ROTATIONAL);
        data.setBlocks(new ArrayList<>(session.getSelectedBlocks()));
        data.setCenter(session.getWorkingCenter());
        data.setSpeed(session.getWorkingSpeed());
        data.setDirection(session.getWorkingDirection());

        session.getStageData().getMovingStructures().add(data);
        session.clearSelectedBlocks();
        session.resetWorkingState();

        MessageUtil.success(player, "✔ Rotational platform '" + id + "' created! ("
                + data.getBlocks().size() + " blocks, Speed=" + String.format("%.1f", data.getSpeed())
                + " deg/tick)");

        session.switchToMechanicsWizard(player);
    }
}
