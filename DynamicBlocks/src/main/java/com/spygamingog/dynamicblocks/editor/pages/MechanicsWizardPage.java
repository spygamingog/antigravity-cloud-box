package com.spygamingog.dynamicblocks.editor.pages;

import com.spygamingog.dynamicblocks.editor.EditorSession;
import com.spygamingog.dynamicblocks.model.MovingStructureData;
import com.spygamingog.dynamicblocks.model.LocationData;
import com.spygamingog.dynamicblocks.util.ItemBuilder;
import com.spygamingog.dynamicblocks.util.MessageUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class MechanicsWizardPage extends HotbarPage {

    public MechanicsWizardPage(EditorSession session) { super(session); }

    @Override
    public void setup(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.GLOWSTONE_DUST)
                .name("Structure Wand", NamedTextColor.AQUA)
                .lore("Click blocks to select/deselect them.")
                .lore("Selected: " + session.getSelectedBlocks().size())
                .toolId("structure_wand").glow().build());

        player.getInventory().setItem(2, new ItemBuilder(Material.STICKY_PISTON)
                .name("Create Linear Platform", NamedTextColor.GREEN)
                .lore("Click to create a linear path-based structure.")
                .toolId("create_linear").build());

        player.getInventory().setItem(3, new ItemBuilder(Material.CLOCK)
                .name("Create Rotator", NamedTextColor.GOLD)
                .lore("Click to create a rotating spinner structure.")
                .toolId("create_rotational").build());

        player.getInventory().setItem(6, new ItemBuilder(Material.BARRIER)
                .name("Clear Selection", NamedTextColor.RED)
                .lore("Click to clear current block selection.")
                .toolId("clear_selection").build());

        player.getInventory().setItem(8, new ItemBuilder(Material.ARROW)
                .name("← Exit Editor", NamedTextColor.WHITE)
                .lore("Return your inventory and exit editor mode.")
                .toolId("exit_editor").build());
    }

    @Override
    public boolean handleClick(Player player, int slot, Action action, int bx, int by, int bz) {
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
                    // Refresh wand item in slot 0 to show count
                    player.getInventory().setItem(0, new ItemBuilder(Material.GLOWSTONE_DUST)
                            .name("Structure Wand", NamedTextColor.AQUA)
                            .lore("Click blocks to select/deselect them.")
                            .lore("Selected: " + c)
                            .toolId("structure_wand").glow().build());
                }
            }
            case 2 -> {
                if (session.getSelectedBlocks().isEmpty()) {
                    MessageUtil.error(player, "Select some blocks first using the Structure Wand in slot 1!");
                    return true;
                }
                // Generate next available ID
                int count = session.getPlugin().getStructureManager().getStructureIds().size();
                String id = MovingStructureData.generateId(MovingStructureData.StructureType.LINEAR, count);
                
                MovingStructureData sd = new MovingStructureData(id, MovingStructureData.StructureType.LINEAR);
                sd.setBlocks(session.getSelectedBlocks());
                sd.setWorldName(player.getWorld().getName());
                sd.setStartPoint(session.getSelectedBlocks().get(0));
                sd.setEndPoint(session.getSelectedBlocks().get(0));
                sd.migrateOldFields();

                // Clear original physical blocks in the world
                for (LocationData blockData : session.getSelectedBlocks()) {
                    new Location(player.getWorld(), blockData.getX(), blockData.getY(), blockData.getZ())
                            .getBlock().setType(Material.AIR);
                }

                session.getPlugin().getStructureManager().addStructure(sd);
                session.getPlugin().getStructureManager().spawn(id);

                MessageUtil.success(player, "Created independent LINEAR structure '" + id + "'! Config tools are now in your hotbar.");
                
                // Keep player in editor session and switch page!
                session.setActiveStructureId(id);
                session.switchPage(new LinearEditorPage(session), player);

                // Open chat config menu instantly!
                player.performCommand("sm structure edit " + id);
            }
            case 3 -> {
                if (session.getSelectedBlocks().isEmpty()) {
                    MessageUtil.error(player, "Select some blocks first using the Structure Wand in slot 1!");
                    return true;
                }
                // Generate next available ID
                int count = session.getPlugin().getStructureManager().getStructureIds().size();
                String id = MovingStructureData.generateId(MovingStructureData.StructureType.ROTATIONAL, count);
                
                MovingStructureData sd = new MovingStructureData(id, MovingStructureData.StructureType.ROTATIONAL);
                sd.setBlocks(session.getSelectedBlocks());
                sd.setWorldName(player.getWorld().getName());
                sd.setCenter(session.getSelectedBlocks().get(0));
                sd.migrateOldFields();

                // Clear original physical blocks in the world
                for (LocationData blockData : session.getSelectedBlocks()) {
                    new Location(player.getWorld(), blockData.getX(), blockData.getY(), blockData.getZ())
                            .getBlock().setType(Material.AIR);
                }

                session.getPlugin().getStructureManager().addStructure(sd);
                session.getPlugin().getStructureManager().spawn(id);

                MessageUtil.success(player, "Created independent ROTATIONAL structure '" + id + "'! Config tools are now in your hotbar.");

                // Keep player in editor session and switch page!
                session.setActiveStructureId(id);
                session.switchPage(new RotationalEditorPage(session), player);

                // Open chat config menu instantly!
                player.performCommand("sm structure edit " + id);
            }
            case 6 -> {
                session.clearSelectedBlocks();
                MessageUtil.info(player, "Selection cleared.");
                player.getInventory().setItem(0, new ItemBuilder(Material.GLOWSTONE_DUST)
                        .name("Structure Wand", NamedTextColor.AQUA)
                        .lore("Click blocks to select/deselect them.")
                        .lore("Selected: 0")
                        .toolId("structure_wand").glow().build());
            }
            case 8 -> {
                session.saveAndExit(player);
                session.getPlugin().getEditorListener().exitSession(player.getUniqueId());
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public String getPageName() { return "DynamicBlocks Structure Wand"; }
}
