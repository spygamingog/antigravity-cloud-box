package com.spygamingog.dynamicblocks.editor.pages;

import com.spygamingog.dynamicblocks.editor.EditorSession;
import com.spygamingog.dynamicblocks.model.LocationData;
import com.spygamingog.dynamicblocks.model.MovingStructureData;
import com.spygamingog.dynamicblocks.model.PathPoint;
import com.spygamingog.dynamicblocks.util.ItemBuilder;
import com.spygamingog.dynamicblocks.util.MessageUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class LinearEditorPage extends HotbarPage {

    public LinearEditorPage(EditorSession session) {
        super(session);
    }

    @Override
    public void setup(Player player) {
        String activeId = session.getActiveStructureId();
        MovingStructureData sd = session.getPlugin().getStructureManager().getStructureData(activeId);
        int blockCount = sd != null ? sd.getBlocks().size() : 0;

        player.getInventory().setItem(0, new ItemBuilder(Material.GLOWSTONE_DUST)
                .name("Structure Wand", NamedTextColor.YELLOW)
                .lore("Click blocks to add/remove from platform.")
                .lore("Structure: " + activeId)
                .lore("Blocks Selected: " + blockCount)
                .toolId("structure_wand").glow().build());

        player.getInventory().setItem(2, new ItemBuilder(Material.GOLD_NUGGET)
                .name("Add Path Point", NamedTextColor.AQUA)
                .lore("Right-click to add an intermediate path node where you are standing.")
                .toolId("add_path_point").build());

        player.getInventory().setItem(3, new ItemBuilder(Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .name("Set End Point", NamedTextColor.RED)
                .lore("Right-click to set end point where you are standing.")
                .toolId("set_end_point").build());

        player.getInventory().setItem(7, new ItemBuilder(Material.BOOK)
                .name("Open Chat Menu", NamedTextColor.GOLD)
                .lore("Right-click to reopen the interactive chat configuration panel.")
                .toolId("open_chat_menu").build());

        player.getInventory().setItem(8, new ItemBuilder(Material.ARROW)
                .name("← Back to Wand Selector", NamedTextColor.WHITE)
                .lore("Right-click to return to general wands.")
                .toolId("back_to_wands").build());
    }

    @Override
    public boolean handleClick(Player player, int slot, Action action, int bx, int by, int bz) {
        String activeId = session.getActiveStructureId();
        if (activeId == null) return false;

        MovingStructureData sd = session.getPlugin().getStructureManager().getStructureData(activeId);
        if (sd == null) return false;

        switch (slot) {
            case 0 -> {
                if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                    boolean added = session.toggleBlock(bx, by, bz);
                    int c = session.getSelectedBlockCount();
                    if (added) {
                        MessageUtil.actionBar(player, "✔ Block added (" + bx + "," + by + "," + bz + ") | Total: " + c);
                    } else {
                        MessageUtil.actionBar(player, "✖ Block removed (" + bx + "," + by + "," + bz + ") | Total: " + c, NamedTextColor.RED);
                    }
                    // Refresh inventory wand to show current blocks selected
                    player.getInventory().setItem(0, new ItemBuilder(Material.GLOWSTONE_DUST)
                            .name("Structure Wand", NamedTextColor.YELLOW)
                            .lore("Click blocks to add/remove from platform.")
                            .lore("Structure: " + activeId)
                            .lore("Blocks Selected: " + c)
                            .toolId("structure_wand").glow().build());
                }
            }
            case 2 -> {
                double x = player.getLocation().getX();
                double y = player.getLocation().getY();
                double z = player.getLocation().getZ();
                LocationData ld = new LocationData(x, y, z);
                PathPoint pt = new PathPoint(ld, 2.0, 0.0);
                sd.getPathPoints().add(pt);
                session.getPlugin().getStructureManager().saveAll();
                session.getPlugin().getStructureManager().spawn(activeId);
                MessageUtil.success(player, "Added Path Point #" + sd.getPathPoints().size() + " at your current coordinates!");
            }
            case 3 -> {
                double x = player.getLocation().getX();
                double y = player.getLocation().getY();
                double z = player.getLocation().getZ();
                LocationData ld = new LocationData(x, y, z);
                sd.setEndPoint(ld);
                session.getPlugin().getStructureManager().saveAll();
                session.getPlugin().getStructureManager().spawn(activeId);
                MessageUtil.success(player, "End Point set to your current coordinates! (" + String.format("%.2f, %.2f, %.2f", x, y, z) + ")");
            }
            case 7 -> {
                player.performCommand("sm structure edit " + activeId);
            }
            case 8 -> {
                session.setActiveStructureId(null);
                session.clearSelectedBlocks();
                session.switchPage(new MechanicsWizardPage(session), player);
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public String getPageName() { return "Linear platform Editor (" + session.getActiveStructureId() + ")"; }
}
