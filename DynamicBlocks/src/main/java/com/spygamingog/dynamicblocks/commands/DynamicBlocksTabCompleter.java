package com.spygamingog.dynamicblocks.commands;

import com.spygamingog.dynamicblocks.engine.StructureManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tab completer providing rich context-aware suggestions for all DynamicBlocks commands.
 */
public class DynamicBlocksTabCompleter implements TabCompleter {

    private final StructureManager structureManager;

    private static final List<String> SUBS = Arrays.asList("tools", "structures", "structure", "reload", "help");
    private static final List<String> BULK_ACTIONS = Arrays.asList("spawn", "despawn", "start", "stop", "reset");
    private static final List<String> STRUCT_ACTIONS = Arrays.asList(
            "add", "remove", "list", "info", "edit", "tp", "rename",
            "start", "stop", "reset", "spawn", "despawn",
            "setmaterial", "setspeed", "setrange", "setaxis", "setdelay", "setloopdelay", "setdirection",
            "setcenter", "setstartpoint", "setendpoint", "setendspeed", "setenddelay", "setstartdelay",
            "setloop", "setcyclecount", "setcycledelay", "setrotationaxis",
            "addpoint", "removepoint", "setpointspeed", "setpointdelay"
    );
    private static final List<String> TYPES = Arrays.asList("LINEAR", "ROTATIONAL");
    private static final List<String> AXES = Arrays.asList("X", "Y", "Z");
    private static final List<String> DIRS = Arrays.asList("POSITIVE", "NEGATIVE", "CLOCKWISE", "COUNTER_CLOCKWISE");
    private static final List<String> SPEEDS = Arrays.asList("1.0", "2.0", "3.0", "5.0", "10.0", "30.0", "45.0", "90.0");
    private static final List<String> DELAYS = Arrays.asList("0.0", "0.5", "1.0", "2.0", "3.0", "5.0");

    private static final List<String> MATS;
    static {
        List<String> list = new ArrayList<>();
        for (org.bukkit.Material m : org.bukkit.Material.values()) {
            if (m.isBlock() && m.isSolid()) {
                list.add(m.name().toLowerCase());
            }
        }
        MATS = list;
    }

    public DynamicBlocksTabCompleter(StructureManager structureManager) {
        this.structureManager = structureManager;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("dynamicblocks.admin")) return List.of();

        if (args.length == 1) {
            return f(SUBS, args[0]);
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            if (sub.equals("structures")) {
                return f(BULK_ACTIONS, args[1]);
            }
            if (sub.equals("structure")) {
                return f(STRUCT_ACTIONS, args[1]);
            }
        }

        if (args.length == 3 && sub.equals("structure")) {
            String act = args[1].toLowerCase();
            if (act.equals("add")) {
                return f(TYPES, args[2]);
            }
            // Suggest structure IDs for other actions
            return f(structureManager.getStructureIds(), args[2]);
        }

        if (args.length == 4 && sub.equals("structure")) {
            String act = args[1].toLowerCase();
            return switch (act) {
                case "add" -> List.of("<name>");
                case "setmaterial" -> f(MATS, args[3]);
                case "setspeed", "setendspeed" -> f(SPEEDS, args[3]);
                case "setrange" -> f(Arrays.asList("1", "3", "5", "10", "15", "20"), args[3]);
                case "setaxis", "setrotationaxis" -> f(AXES, args[3]);
                case "setdelay", "setloopdelay", "setenddelay", "setstartdelay" -> f(DELAYS, args[3]);
                case "setdirection" -> f(DIRS, args[3]);
                case "setloop" -> f(Arrays.asList("true", "false"), args[3]);
                case "setcyclecount" -> f(Arrays.asList("1", "2", "3", "4", "5"), args[3]);
                case "setcycledelay" -> f(DELAYS, args[3]);
                default -> List.of();
            };
        }

        if (args.length == 5 && sub.equals("structure") && args[1].equalsIgnoreCase("add")) {
            return f(MATS, args[4]);
        }

        return List.of();
    }

    private List<String> f(List<String> src, String query) {
        String q = query.toLowerCase();
        List<String> res = new ArrayList<>();
        for (String s : src) {
            if (s.toLowerCase().startsWith(q)) {
                res.add(s);
            }
        }
        return res;
    }
}
