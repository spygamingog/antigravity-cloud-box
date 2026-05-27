package com.spy.spymotion.editor;

import com.spy.spymotion.model.LocationData;
import com.spy.spymotion.model.MovingStructureData;
import com.spy.spymotion.model.PathPoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

/**
 * Sends a clickable chat-based configuration panel for a moving structure.
 * Features granular path configurations, speeds and delays in seconds, looping,
 * and concurrent cycle controls.
 */
public class StructureEditMenu {

    /**
     * Send the structure edit panel to a sender.
     */
    public static void send(CommandSender sender, MovingStructureData data) {
        String id = data.getId();
        String base = "/sm structure ";

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("═══ Editing: " + data.getDisplayName() + " (" + data.getType() + ") ═══")
                .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));

        // Name
        sender.sendMessage(row("Name", data.getDisplayName(),
                clickable("[RENAME]", NamedTextColor.AQUA, "/sm structure rename " + id + " ",
                        "Click to type a new name", true)));

        // Material
        sender.sendMessage(row("Material", data.getBlockMaterial(),
                clickable("[CHANGE]", NamedTextColor.AQUA, "/sm structure setmaterial " + id + " ",
                        "Click to type a material", true)));

        // --- Type-specific fields ---
        if (data.getType() == MovingStructureData.StructureType.LINEAR) {
            sender.sendMessage(Component.text(" ─── Path & Cycle Settings ───").color(NamedTextColor.DARK_GRAY));

            // Start Point
            String startStr = data.getStartPoint() != null ? String.format("(%.0f, %.0f, %.0f)", data.getStartPoint().getX(), data.getStartPoint().getY(), data.getStartPoint().getZ()) : "Not set";
            sender.sendMessage(row("Start Node", startStr,
                    btn("SET HERE", NamedTextColor.YELLOW, base + "setstartpoint " + id, "Set start point to your current position")));

            // Start Delay
            double startDelay = data.getStartPointDelay();
            sender.sendMessage(row("Start Delay", String.format("%.1fs", startDelay),
                    btn("▲", NamedTextColor.GREEN, base + "setstartdelay " + id + " " + (startDelay + 0.5), "+0.5s"),
                    btn("▼", NamedTextColor.RED, base + "setstartdelay " + id + " " + Math.max(0, startDelay - 0.5), "-0.5s")));

            // Intermediate Path Points
            sender.sendMessage(Component.text(" Intermediate Nodes:").color(NamedTextColor.GRAY));
            if (data.getPathPoints().isEmpty()) {
                sender.sendMessage(Component.text("   (No intermediate nodes added yet)").color(NamedTextColor.DARK_GRAY));
            } else {
                for (int i = 0; i < data.getPathPoints().size(); i++) {
                    PathPoint pt = data.getPathPoints().get(i);
                    String ptStr = String.format("(%.0f, %.0f, %.0f)", pt.getLocation().getX(), pt.getLocation().getY(), pt.getLocation().getZ());
                    sender.sendMessage(Component.text("   #" + (i + 1) + ": ").color(NamedTextColor.YELLOW)
                            .append(Component.text(ptStr).color(NamedTextColor.WHITE))
                            .append(Component.text("  Spd: " + String.format("%.1f b/s", pt.getSpeed())).color(NamedTextColor.GRAY))
                            .append(Component.text("  "))
                            .append(clickable("[Spd]", NamedTextColor.AQUA, base + "setpointspeed " + id + " " + i + " ", "Set travel speed to this node (blocks/second)", true))
                            .append(Component.text("  Del: " + String.format("%.1fs", pt.getDelay())).color(NamedTextColor.GRAY))
                            .append(Component.text("  "))
                            .append(clickable("[Del]", NamedTextColor.AQUA, base + "setpointdelay " + id + " " + i + " ", "Set delay at this node (seconds)", true))
                            .append(Component.text("  "))
                            .append(btn("[✖]", NamedTextColor.RED, base + "removepoint " + id + " " + i, "Remove this node")));
                }
            }
            // Add Node helper
            sender.sendMessage(Component.text("   ")
                    .append(btn("+ ADD NODE AT MY POS", NamedTextColor.GOLD, base + "addpoint " + id, "Add your current position as an intermediate node")));

            // End Point
            String endStr = data.getEndPoint() != null ? String.format("(%.0f, %.0f, %.0f)", data.getEndPoint().getX(), data.getEndPoint().getY(), data.getEndPoint().getZ()) : "Not set";
            sender.sendMessage(row("End Node", endStr,
                    btn("SET HERE", NamedTextColor.YELLOW, base + "setendpoint " + id, "Set end point to your current position")));

            // End Speed
            double endSpd = data.getEndPointSpeed();
            sender.sendMessage(row("End Speed", String.format("%.1f b/s", endSpd),
                    btn("▲", NamedTextColor.GREEN, base + "setendspeed " + id + " " + (endSpd + 0.5), "+0.5 b/s"),
                    btn("▼", NamedTextColor.RED, base + "setendspeed " + id + " " + Math.max(0.1, endSpd - 0.5), "-0.5 b/s")));

            // End Delay
            double endDelay = data.getEndPointDelay();
            sender.sendMessage(row("End Delay", String.format("%.1fs", endDelay),
                    btn("▲", NamedTextColor.GREEN, base + "setenddelay " + id + " " + (endDelay + 0.5), "+0.5s"),
                    btn("▼", NamedTextColor.RED, base + "setenddelay " + id + " " + Math.max(0, endDelay - 0.5), "-0.5s")));

            // Loop Toggle
            sender.sendMessage(row("Looping", data.isLoop() ? "ENABLED (Restart)" : "DISABLED (Ping-Pong)",
                    btn("TOGGLE", NamedTextColor.YELLOW, base + "setloop " + id + " " + (!data.isLoop()), "Cycle looping mode")));

            // Cycle Count
            int cycles = data.getCycleCount();
            sender.sendMessage(row("Cycle Count", String.valueOf(cycles),
                    btn("▲", NamedTextColor.GREEN, base + "setcyclecount " + id + " " + (cycles + 1), "+1 Cycle"),
                    btn("▼", NamedTextColor.RED, base + "setcyclecount " + id + " " + Math.max(1, cycles - 1), "-1 Cycle")));

            // Cycle Delay
            double cycleDelay = data.getCycleDelay();
            sender.sendMessage(row("Cycle Delay", String.format("%.1fs", cycleDelay),
                    btn("▲", NamedTextColor.GREEN, base + "setcycledelay " + id + " " + (cycleDelay + 0.5), "+0.5s"),
                    btn("▼", NamedTextColor.RED, base + "setcycledelay " + id + " " + Math.max(0, cycleDelay - 0.5), "-0.5s")));

        } else {
            sender.sendMessage(Component.text(" ─── Rotation Settings ───").color(NamedTextColor.DARK_GRAY));

            // Speed in deg/sec
            double speed = data.getSpeed();
            sender.sendMessage(row("Rotation Speed", String.format("%.1f deg/s", speed),
                    btn("▲", NamedTextColor.GREEN, base + "setspeed " + id + " " + (speed + 5.0), "+5.0 deg/s"),
                    btn("▼", NamedTextColor.RED, base + "setspeed " + id + " " + Math.max(0.1, speed - 5.0), "-5.0 deg/s")));

            // Direction
            String dirLabel = data.getDirection().name();
            String nextDir = data.getDirection() == MovingStructureData.Direction.CLOCKWISE ? "COUNTER_CLOCKWISE" : "CLOCKWISE";
            sender.sendMessage(row("Direction", dirLabel,
                    btn("TOGGLE", NamedTextColor.YELLOW, base + "setdirection " + id + " " + nextDir, "Cycle direction")));

            // Center pivot
            String centerStr = data.getCenter() != null
                    ? String.format("(%.1f, %.1f, %.1f)", data.getCenter().getX(), data.getCenter().getY(), data.getCenter().getZ())
                    : "Not set";
            sender.sendMessage(row("Center Pivot", centerStr,
                    btn("SET TO MY POS", NamedTextColor.AQUA, base + "setcenter " + id, "Set pivot to your current position")));

            // Rotation Axis
            String rotAxis = data.getRotationAxis();
            String nextAxis = rotAxis.equals("Y") ? "X" : rotAxis.equals("X") ? "Z" : "Y";
            sender.sendMessage(row("Rotation Axis", rotAxis + "-axis",
                    btn("CYCLE", NamedTextColor.YELLOW, base + "setrotationaxis " + id + " " + nextAxis, "Cycle through X/Y/Z rotation axes")));
        }

        // Blocks info
        sender.sendMessage(row("Block Count", data.getBlocks().size() + " block(s)", Component.empty()));

        // Controls
        sender.sendMessage(Component.text(" ───────────────────").color(NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.text("  ")
                .append(btn("START", NamedTextColor.GREEN, base + "start " + id, "Start this structure"))
                .append(Component.text("  "))
                .append(btn("STOP", NamedTextColor.RED, base + "stop " + id, "Stop this structure"))
                .append(Component.text("  "))
                .append(btn("RESET", NamedTextColor.YELLOW, base + "reset " + id, "Reset this structure"))
                .append(Component.text("  "))
                .append(btn("SPAWN", NamedTextColor.AQUA, base + "spawn " + id, "Spawn this structure"))
                .append(Component.text("  "))
                .append(btn("DELETE", NamedTextColor.DARK_RED, base + "remove " + id, "Delete this structure"))
                .append(Component.text("  "))
                .append(btn("BACK", NamedTextColor.GRAY, "/sm structure list", "Return to structure list"))
        );
        sender.sendMessage(Component.empty());
    }

    // --- Helpers ---

    private static Component row(String label, String val, Component btnComponent) {
        return Component.text(" ").append(Component.text(label + ": ").color(NamedTextColor.GRAY))
                .append(Component.text(val).color(NamedTextColor.WHITE))
                .append(Component.text("   "))
                .append(btnComponent);
    }

    private static Component row(String label, String val, Component b1, Component b2) {
        return Component.text(" ").append(Component.text(label + ": ").color(NamedTextColor.GRAY))
                .append(Component.text(val).color(NamedTextColor.WHITE))
                .append(Component.text("   "))
                .append(b1).append(Component.text(" ")).append(b2);
    }

    private static Component btn(String text, NamedTextColor color, String cmd, String hoverText) {
        return clickable("[" + text + "]", color, cmd, hoverText, false);
    }

    private static Component clickable(String text, NamedTextColor color, String cmd, String hover, boolean suggest) {
        return Component.text(text).color(color).decorate(TextDecoration.BOLD)
                .clickEvent(suggest ? ClickEvent.suggestCommand(cmd) : ClickEvent.runCommand(cmd))
                .hoverEvent(HoverEvent.showText(Component.text(hover).color(NamedTextColor.YELLOW)));
    }
}
