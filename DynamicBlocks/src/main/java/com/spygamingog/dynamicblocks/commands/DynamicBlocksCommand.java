package com.spygamingog.dynamicblocks.commands;

import com.spygamingog.dynamicblocks.DynamicBlocks;
import com.spygamingog.dynamicblocks.editor.EditorListener;
import com.spygamingog.dynamicblocks.editor.StructureEditMenu;
import com.spygamingog.dynamicblocks.engine.StructureManager;
import com.spygamingog.dynamicblocks.model.LocationData;
import com.spygamingog.dynamicblocks.model.MovingStructureData;
import com.spygamingog.dynamicblocks.model.PathPoint;
import com.spygamingog.dynamicblocks.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Main command executor for /db and /dynamicblocks commands.
 * Provides all per-structure configuration and execution command logic,
 * as well as visual tools editor trigger and configuration loading.
 */
public class DynamicBlocksCommand implements CommandExecutor {

    private final DynamicBlocks plugin;
    private final StructureManager structureManager;
    private final EditorListener editorListener;

    public DynamicBlocksCommand(DynamicBlocks plugin, StructureManager structureManager, EditorListener editorListener) {
        this.plugin = plugin;
        this.structureManager = structureManager;
        this.editorListener = editorListener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("dynamicblocks.admin")) {
            MessageUtil.error(sender, "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> showHelp(sender);
            case "reload" -> {
                structureManager.despawnAll();
                structureManager.loadAll();
                structureManager.spawnAll();
                MessageUtil.success(sender, "DynamicBlocks configuration reloaded successfully!");
            }
            case "tools" -> {
                if (!(sender instanceof Player p)) {
                    MessageUtil.error(sender, "This command can only be used by players.");
                    return true;
                }
                editorListener.startSession(p);
                MessageUtil.success(p, "Standalone visual structure editor tools activated!");
            }
            case "structures" -> handleStructuresBulk(sender, args);
            case "structure" -> handleStructure(sender, args);
            default -> {
                MessageUtil.error(sender, "Unknown subcommand: " + sub);
                showHelp(sender);
            }
        }

        return true;
    }

    private void showHelp(CommandSender s) {
        s.sendMessage(Component.empty());
        s.sendMessage(Component.text("═════════ DynamicBlocks Help ═════════").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        s.sendMessage(Component.text(" /db tools").color(NamedTextColor.YELLOW).append(Component.text(" - Open standalone visual structure tools").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text(" /db reload").color(NamedTextColor.YELLOW).append(Component.text(" - Reload structure configurations").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text(" /db structures <spawn|despawn|start|stop|reset>").color(NamedTextColor.YELLOW).append(Component.text(" - Bulk structure controls").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text(" /db structure add <LINEAR|ROTATIONAL> [name] [material]").color(NamedTextColor.YELLOW).append(Component.text(" - Create structure").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text(" /db structure remove <id>").color(NamedTextColor.YELLOW).append(Component.text(" - Remove structure").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text(" /db structure edit <id>").color(NamedTextColor.YELLOW).append(Component.text(" - Open configuration menu").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text(" /db structure list").color(NamedTextColor.YELLOW).append(Component.text(" - List all structures").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text(" /db structure <start|stop|reset|spawn|despawn> <id>").color(NamedTextColor.YELLOW).append(Component.text(" - Per-structure controls").color(NamedTextColor.GRAY)));
        s.sendMessage(Component.text("══════════════════════════════════").color(NamedTextColor.GOLD));
    }

    // ========== /db structures <action> ==========
    private void handleStructuresBulk(CommandSender s, String[] a) {
        if (a.length < 2) {
            MessageUtil.error(s, "Usage: /db structures <spawn|despawn|start|stop|reset>");
            return;
        }
        String act = a[1].toLowerCase();
        switch (act) {
            case "spawn" -> { structureManager.spawnAll(); MessageUtil.success(s, "Spawned all structures."); }
            case "despawn" -> { structureManager.despawnAll(); MessageUtil.success(s, "Despawned all structures."); }
            case "start" -> { structureManager.startAll(); MessageUtil.success(s, "Started all structures."); }
            case "stop" -> { structureManager.stopAll(); MessageUtil.success(s, "Stopped all structures."); }
            case "reset" -> { structureManager.resetAll(); MessageUtil.success(s, "Reset all structures."); }
            default -> MessageUtil.error(s, "Unknown bulk action: " + act);
        }
    }

    // ========== /db structure <action> [id] [args...] ==========
    private void handleStructure(CommandSender s, String[] a) {
        if (a.length < 2) { showStructureHelp(s); return; }
        String act = a[1].toLowerCase();
        switch (act) {
            case "add" -> structureAdd(s, a);
            case "remove" -> structureRemove(s, a);
            case "list" -> structureList(s);
            case "info", "edit" -> structureInfo(s, a);
            case "tp" -> structureTp(s, a);
            case "rename" -> structureRename(s, a);
            case "start" -> structureControl(s, a, "start");
            case "stop" -> structureControl(s, a, "stop");
            case "reset" -> structureControl(s, a, "reset");
            case "spawn" -> structureControl(s, a, "spawn");
            case "despawn" -> structureControl(s, a, "despawn");
            case "setmaterial" -> structureSetField(s, a, "material");
            case "setspeed" -> structureSetField(s, a, "speed");
            case "setrange" -> structureSetField(s, a, "range");
            case "setaxis" -> structureSetField(s, a, "axis");
            case "setdelay" -> structureSetField(s, a, "delay");
            case "setloopdelay" -> structureSetField(s, a, "loopdelay");
            case "setdirection" -> structureSetField(s, a, "direction");
            case "setrotationaxis" -> structureSetField(s, a, "rotationaxis");
            case "setcenter" -> structureSetCenter(s, a);
            case "setstartpoint" -> structureSetStartPoint(s, a);
            case "setendpoint" -> structureSetEndPoint(s, a);
            case "setendspeed" -> structureSetField(s, a, "endspeed");
            case "setenddelay" -> structureSetField(s, a, "enddelay");
            case "setstartdelay" -> structureSetField(s, a, "startdelay");
            case "setloop" -> structureSetField(s, a, "loop");
            case "setcyclecount" -> structureSetField(s, a, "cyclecount");
            case "setcycledelay" -> structureSetField(s, a, "cycledelay");
            case "addpoint" -> structureAddPoint(s, a);
            case "removepoint" -> structureRemovePoint(s, a);
            case "setpointspeed" -> structureSetPointSpeed(s, a);
            case "setpointdelay" -> structureSetPointDelay(s, a);
            default -> { MessageUtil.error(s, "Unknown structure action: " + act); showStructureHelp(s); }
        }
    }

    private void showStructureHelp(CommandSender s) {
        MessageUtil.info(s, "Usage: /db structure <add|remove|list|edit|start|stop|reset|setspeed|setrotationaxis|setendpoint|...>");
    }

    private void structureAdd(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { MessageUtil.error(s, "Player only."); return; }
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure add <LINEAR|ROTATIONAL> [name] [material]"); return; }
        MovingStructureData.StructureType type;
        try { type = MovingStructureData.StructureType.valueOf(a[2].toUpperCase()); }
        catch (Exception e) { MessageUtil.error(s, "Invalid type! Must be LINEAR or ROTATIONAL."); return; }

        String name = a.length >= 4 ? a[3] : null;
        String mat = a.length >= 5 ? a[4].toUpperCase() : "STONE";

        try { Material.valueOf(mat); }
        catch (Exception e) { MessageUtil.error(s, "Invalid material name."); return; }

        int count = structureManager.getStructureIds().size();
        String id = MovingStructureData.generateId(type, count);

        MovingStructureData sd = new MovingStructureData(id, type);
        sd.setName(name);
        sd.setBlockMaterial(mat);
        sd.setWorldName(p.getWorld().getName());

        // Fill with a single solid block right under the player as initial block if no selection exists
        LocationData pLoc = LocationData.fromBlock(p.getLocation().getBlockX(), p.getLocation().getBlockY() - 1, p.getLocation().getBlockZ());
        sd.setBlocks(Arrays.asList(pLoc));

        if (type == MovingStructureData.StructureType.LINEAR) {
            sd.setStartPoint(pLoc);
            sd.setEndPoint(LocationData.fromBlock(p.getLocation().getBlockX() + 5, p.getLocation().getBlockY() - 1, p.getLocation().getBlockZ()));
        } else {
            sd.setCenter(pLoc);
        }

        sd.migrateOldFields();
        structureManager.addStructure(sd);
        structureManager.spawn(id);
        
        MessageUtil.success(s, "Created " + type + " structure '" + sd.getDisplayName() + "'! Opening edit menu.");
        StructureEditMenu.send(s, sd);
    }

    private void structureControl(CommandSender s, String[] a, String action) {
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure " + action + " <id>"); return; }
        String id = a[2].toLowerCase();
        MovingStructureData ms = structureManager.getStructureData(id);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }
        switch (action) {
            case "start" -> { structureManager.start(id); MessageUtil.success(s, "Structure '" + ms.getDisplayName() + "' started."); }
            case "stop" -> { structureManager.stop(id); MessageUtil.success(s, "Structure '" + ms.getDisplayName() + "' stopped."); }
            case "reset" -> { structureManager.reset(id); MessageUtil.success(s, "Structure '" + ms.getDisplayName() + "' reset."); }
            case "spawn" -> { structureManager.spawn(id); MessageUtil.success(s, "Structure '" + ms.getDisplayName() + "' spawned."); }
            case "despawn" -> { structureManager.despawn(id); MessageUtil.success(s, "Structure '" + ms.getDisplayName() + "' despawned."); }
        }
    }

    private void structureRemove(CommandSender s, String[] a) {
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure remove <id>"); return; }
        if (structureManager.removeStructure(a[2])) {
            MessageUtil.success(s, "Structure '" + a[2] + "' removed successfully.");
        } else {
            MessageUtil.error(s, "Structure not found.");
        }
    }

    private void structureList(CommandSender s) {
        s.sendMessage(Component.empty());
        s.sendMessage(Component.text("═════ DynamicBlocks Structures ═════").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        if (structureManager.getStructureIds().isEmpty()) {
            s.sendMessage(Component.text(" No structures created yet. Use /db tools!").color(NamedTextColor.GRAY));
        } else {
            for (String id : structureManager.getStructureIds()) {
                MovingStructureData ms = structureManager.getStructureData(id);
                Component line = Component.text(" • ").color(NamedTextColor.DARK_GRAY)
                        .append(Component.text(ms.getDisplayName()).color(NamedTextColor.YELLOW))
                        .append(Component.text(" (" + ms.getType() + ") ").color(NamedTextColor.GRAY))
                        .append(Component.text("[EDIT]").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD)
                                .clickEvent(ClickEvent.runCommand("/db structure edit " + id))
                                .hoverEvent(HoverEvent.showText(Component.text("Click to configure " + ms.getDisplayName()))))
                        .append(Component.text("  "))
                        .append(Component.text("[TP]").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                                .clickEvent(ClickEvent.runCommand("/db structure tp " + id))
                                .hoverEvent(HoverEvent.showText(Component.text("Teleport to center"))));
                s.sendMessage(line);
            }
        }
        s.sendMessage(Component.text("═══════════════════════════════").color(NamedTextColor.GOLD));
    }

    private void structureInfo(CommandSender s, String[] a) {
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure info <id>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }
        StructureEditMenu.send(s, ms);
    }

    private void structureTp(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { MessageUtil.error(s, "Player only."); return; }
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure tp <id>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }

        LocationData center = ms.getCenter() != null ? ms.getCenter() : ms.calculateCenter();
        p.teleport(center.toBukkit(p.getWorld()).add(0, 1.5, 0));
        MessageUtil.success(s, "Teleported to '" + ms.getDisplayName() + "' center.");
    }

    private void structureRename(CommandSender s, String[] a) {
        if (a.length < 4) { MessageUtil.error(s, "Usage: /db structure rename <id> <new name>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }

        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < a.length; i++) {
            sb.append(a[i]).append(" ");
        }
        String name = sb.toString().trim();
        ms.setName(name);
        structureManager.saveAll();
        MessageUtil.success(s, "Renamed to '" + name + "'.");
        StructureEditMenu.send(s, ms);
    }

    private void structureSetField(CommandSender s, String[] a, String field) {
        if (a.length < 4) { MessageUtil.error(s, "Usage: /db structure set" + field + " <id> <value>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }
        try {
            switch (field) {
                case "material" -> {
                    Material mat = Material.valueOf(a[3].toUpperCase());
                    if (!mat.isBlock() || !mat.isSolid()) throw new IllegalArgumentException("Material must be a solid block!");
                    ms.setBlockMaterial(mat.name());
                }
                case "speed" -> {
                    double val = Double.parseDouble(a[3]);
                    ms.setSpeed(val);
                    ms.setEndPointSpeed(val);
                }
                case "range" -> ms.setRange(Double.parseDouble(a[3]));
                case "axis" -> { if (!a[3].matches("[XYZxyz]")) throw new Exception(); ms.setAxis(a[3].toUpperCase()); }
                case "delay" -> ms.setEndpointDelayTicks(Integer.parseInt(a[3]));
                case "loopdelay" -> ms.setLoopDelayTicks(Integer.parseInt(a[3]));
                case "direction" -> ms.setDirection(MovingStructureData.Direction.valueOf(a[3].toUpperCase()));
                case "endspeed" -> ms.setEndPointSpeed(Double.parseDouble(a[3]));
                case "enddelay" -> ms.setEndPointDelay(Double.parseDouble(a[3]));
                case "startdelay" -> ms.setStartPointDelay(Double.parseDouble(a[3]));
                case "loop" -> ms.setLoop(Boolean.parseBoolean(a[3]));
                case "cyclecount" -> ms.setCycleCount(Integer.parseInt(a[3]));
                case "cycledelay" -> ms.setCycleDelay(Double.parseDouble(a[3]));
                case "rotationaxis" -> { if (!a[3].matches("[XYZxyz]")) throw new Exception(); ms.setRotationAxis(a[3].toUpperCase()); }
            }
            structureManager.saveAll();
            // Re-spawn the structure to apply settings dynamically if it's spawned!
            if (structureManager.getActiveStructure(ms.getId()) != null) {
                structureManager.spawn(ms.getId());
            }
            MessageUtil.success(s, "Field '" + field + "' updated successfully.");
        } catch (Exception e) {
            MessageUtil.error(s, "Invalid value: " + a[3] + " (" + e.getMessage() + ")");
        }
        StructureEditMenu.send(s, ms);
    }

    private void structureSetCenter(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { MessageUtil.error(s, "Player only."); return; }
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure setcenter <id>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }

        ms.setCenter(LocationData.from(p.getLocation()));
        structureManager.saveAll();
        if (structureManager.getActiveStructure(ms.getId()) != null) {
            structureManager.spawn(ms.getId());
        }
        MessageUtil.success(s, "Pivot center set to your current coordinates!");
        StructureEditMenu.send(s, ms);
    }

    private void structureSetStartPoint(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { MessageUtil.error(s, "Player only."); return; }
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure setstartpoint <id>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }

        ms.setStartPoint(LocationData.from(p.getLocation()));
        structureManager.saveAll();
        if (structureManager.getActiveStructure(ms.getId()) != null) {
            structureManager.spawn(ms.getId());
        }
        MessageUtil.success(s, "Start Node set to your current coordinates!");
        StructureEditMenu.send(s, ms);
    }

    private void structureSetEndPoint(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { MessageUtil.error(s, "Player only."); return; }
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure setendpoint <id>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }

        ms.setEndPoint(LocationData.from(p.getLocation()));
        structureManager.saveAll();
        if (structureManager.getActiveStructure(ms.getId()) != null) {
            structureManager.spawn(ms.getId());
        }
        MessageUtil.success(s, "End Node set to your current coordinates!");
        StructureEditMenu.send(s, ms);
    }

    private void structureAddPoint(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { MessageUtil.error(s, "Player only."); return; }
        if (a.length < 3) { MessageUtil.error(s, "Usage: /db structure addpoint <id>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }

        LocationData pt = LocationData.from(p.getLocation());
        ms.getPathPoints().add(new PathPoint(pt, ms.getEndPointSpeed(), 0.0));
        structureManager.saveAll();
        if (structureManager.getActiveStructure(ms.getId()) != null) {
            structureManager.spawn(ms.getId());
        }
        MessageUtil.success(s, "Added path node #" + ms.getPathPoints().size() + " at your coordinates.");
        StructureEditMenu.send(s, ms);
    }

    private void structureRemovePoint(CommandSender s, String[] a) {
        if (a.length < 4) { MessageUtil.error(s, "Usage: /db structure removepoint <id> <index>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }
        try {
            int idx = Integer.parseInt(a[3]);
            if (idx >= 0 && idx < ms.getPathPoints().size()) {
                ms.getPathPoints().remove(idx);
                structureManager.saveAll();
                if (structureManager.getActiveStructure(ms.getId()) != null) {
                    structureManager.spawn(ms.getId());
                }
                MessageUtil.success(s, "Removed path node.");
            } else {
                MessageUtil.error(s, "Index out of range.");
            }
        } catch (Exception e) {
            MessageUtil.error(s, "Invalid index.");
        }
        StructureEditMenu.send(s, ms);
    }

    private void structureSetPointSpeed(CommandSender s, String[] a) {
        if (a.length < 5) { MessageUtil.error(s, "Usage: /db structure setpointspeed <id> <index> <speed>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }
        try {
            int idx = Integer.parseInt(a[3]);
            double speed = Double.parseDouble(a[4]);
            if (idx >= 0 && idx < ms.getPathPoints().size()) {
                ms.getPathPoints().get(idx).setSpeed(speed);
                structureManager.saveAll();
                MessageUtil.success(s, "Updated travel speed to path node #" + (idx + 1) + ".");
            } else {
                MessageUtil.error(s, "Index out of range.");
            }
        } catch (Exception e) {
            MessageUtil.error(s, "Invalid parameters.");
        }
        StructureEditMenu.send(s, ms);
    }

    private void structureSetPointDelay(CommandSender s, String[] a) {
        if (a.length < 5) { MessageUtil.error(s, "Usage: /db structure setpointdelay <id> <index> <delay seconds>"); return; }
        MovingStructureData ms = structureManager.getStructureData(a[2]);
        if (ms == null) { MessageUtil.error(s, "Structure not found."); return; }
        try {
            int idx = Integer.parseInt(a[3]);
            double delay = Double.parseDouble(a[4]);
            if (idx >= 0 && idx < ms.getPathPoints().size()) {
                ms.getPathPoints().get(idx).setDelay(delay);
                structureManager.saveAll();
                MessageUtil.success(s, "Updated wait delay at path node #" + (idx + 1) + ".");
            } else {
                MessageUtil.error(s, "Index out of range.");
            }
        } catch (Exception e) {
            MessageUtil.error(s, "Invalid parameters.");
        }
        StructureEditMenu.send(s, ms);
    }
}
