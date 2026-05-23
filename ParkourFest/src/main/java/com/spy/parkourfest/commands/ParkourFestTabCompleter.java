package com.spy.parkourfest.commands;

import com.spy.parkourfest.game.GameManager;
import com.spy.parkourfest.stage.StageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completion for all /pf subcommands.
 */
public class ParkourFestTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "create", "delete", "tools", "enable", "disable",
            "start", "stop", "pvp", "structures", "list", "info"
    );

    private static final List<String> PVP_OPTIONS = Arrays.asList("enable", "disable");
    private static final List<String> STRUCT_OPTIONS = Arrays.asList("start", "stop", "reset");

    private final StageManager stageManager;
    private final GameManager gameManager;

    public ParkourFestTabCompleter(StageManager stageManager, GameManager gameManager) {
        this.stageManager = stageManager;
        this.gameManager = gameManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "pvp" -> { return filterStartsWith(PVP_OPTIONS, args[1]); }
                case "structures" -> { return filterStartsWith(STRUCT_OPTIONS, args[1]); }
                case "create" -> { return List.of("<name>"); }
                case "list" -> { return List.of(); }
                case "stop" -> {
                    return filterStartsWith(new ArrayList<>(gameManager.getActiveStageNames()), args[1]);
                }
                default -> {
                    // Most subcommands take a stage name as arg 2
                    return filterStartsWith(new ArrayList<>(stageManager.getStageNames()), args[1]);
                }
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("pvp") || sub.equals("structures")) {
                return filterStartsWith(new ArrayList<>(stageManager.getStageNames()), args[2]);
            }
        }

        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
