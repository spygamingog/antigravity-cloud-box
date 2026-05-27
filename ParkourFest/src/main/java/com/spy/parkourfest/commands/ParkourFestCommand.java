package com.spy.parkourfest.commands;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.editor.EditorListener;
import com.spy.parkourfest.game.GameManager;
import com.spy.parkourfest.model.StageData;
import com.spy.parkourfest.stage.StageManager;
import com.spy.parkourfest.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Main /pf command router.
 * Routes all subcommands to their respective handlers.
 */
public class ParkourFestCommand implements CommandExecutor {

    private final ParkourFest plugin;
    private final StageManager stageManager;
    private final GameManager gameManager;
    private final EditorListener editorListener;

    public ParkourFestCommand(ParkourFest plugin, StageManager stageManager,
                               GameManager gameManager, EditorListener editorListener) {
        this.plugin = plugin;
        this.stageManager = stageManager;
        this.gameManager = gameManager;
        this.editorListener = editorListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("pf.admin")) {
            MessageUtil.error(player, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "tools" -> handleTools(player, args);
            case "enable" -> handleToggle(player, args, true);
            case "disable" -> handleToggle(player, args, false);
            case "start" -> handleStart(player, args);
            case "stop" -> handleStop(player, args);
            case "pvp" -> handlePvp(player, args);
            case "structures" -> handleStructures(player, args);
            case "list" -> handleList(player);
            case "info" -> handleInfo(player, args);
            default -> {
                MessageUtil.error(player, "Unknown subcommand: " + sub);
                showHelp(player);
            }
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) { MessageUtil.error(player, "Usage: /pf create <name>"); return; }
        String name = args[1];
        StageData data = stageManager.createStage(name);
        if (data == null) {
            MessageUtil.error(player, "Stage '" + name + "' already exists!");
        } else {
            MessageUtil.success(player, "Stage '" + name + "' created! Use /pf tools " + name + " to configure.");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) { MessageUtil.error(player, "Usage: /pf delete <name>"); return; }
        String name = args[1];
        if (gameManager.isGameRunning(name)) {
            MessageUtil.error(player, "Cannot delete a stage with an active game! Stop it first.");
            return;
        }
        if (stageManager.deleteStage(name)) {
            MessageUtil.success(player, "Stage '" + name + "' deleted.");
        } else {
            MessageUtil.error(player, "Stage '" + name + "' not found.");
        }
    }

    private void handleTools(Player player, String[] args) {
        if (args.length < 2) { MessageUtil.error(player, "Usage: /pf tools <name>"); return; }
        String name = args[1];
        StageData data = stageManager.getStage(name);
        if (data == null) { MessageUtil.error(player, "Stage '" + name + "' not found."); return; }
        editorListener.startSession(player, data);
    }

    private void handleToggle(Player player, String[] args, boolean enable) {
        if (args.length < 2) { MessageUtil.error(player, "Usage: /pf " + (enable ? "enable" : "disable") + " <name>"); return; }
        String name = args[1];
        StageData data = stageManager.getStage(name);
        if (data == null) { MessageUtil.error(player, "Stage '" + name + "' not found."); return; }
        data.setEnabled(enable);
        stageManager.save(name);
        MessageUtil.success(player, "Stage '" + name + "' " + (enable ? "enabled" : "disabled") + ".");
    }

    private void handleStart(Player player, String[] args) {
        if (args.length < 2) { MessageUtil.error(player, "Usage: /pf start <name>"); return; }
        String error = gameManager.start(args[1], player);
        if (error != null) MessageUtil.error(player, error);
    }

    private void handleStop(Player player, String[] args) {
        if (args.length < 2) { MessageUtil.error(player, "Usage: /pf stop <name>"); return; }
        String error = gameManager.stop(args[1]);
        if (error != null) MessageUtil.error(player, error);
        else MessageUtil.success(player, "Game stopped on '" + args[1] + "'.");
    }

    private void handlePvp(Player player, String[] args) {
        if (args.length < 3) { MessageUtil.error(player, "Usage: /pf pvp <enable/disable> <name>"); return; }
        String toggle = args[1].toLowerCase();
        String name = args[2];
        StageData data = stageManager.getStage(name);
        if (data == null) { MessageUtil.error(player, "Stage '" + name + "' not found."); return; }

        boolean pvp = toggle.equals("enable") || toggle.equals("on");
        data.setPvpEnabled(pvp);
        stageManager.save(name);
        MessageUtil.success(player, "PVP " + (pvp ? "enabled" : "disabled") + " for '" + name + "'.");
    }

    private void handleStructures(Player player, String[] args) {
        if (args.length < 3) { MessageUtil.error(player, "Usage: /pf structures <start/stop/reset> <name>"); return; }
        String error = gameManager.controlStructures(args[2], args[1]);
        if (error != null) MessageUtil.error(player, error);
        else MessageUtil.success(player, "Structures " + args[1] + " on '" + args[2] + "'.");
    }

    private void handleList(Player player) {
        var names = stageManager.getStageNames();
        if (names.isEmpty()) {
            MessageUtil.info(player, "No stages created yet. Use /pf create <name>");
            return;
        }
        MessageUtil.info(player, "=== ParkourFest Stages (" + names.size() + ") ===");
        for (String name : names) {
            StageData data = stageManager.getStage(name);
            String status = data.isEnabled() ? "§a✔ Enabled" : "§c✖ Disabled";
            boolean running = gameManager.isGameRunning(name);
            String runStatus = running ? " §6[RUNNING]" : "";
            player.sendMessage(Component.text(" • " + name + " ")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text(status))
                    .append(Component.text(runStatus)));
        }
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) { MessageUtil.error(player, "Usage: /pf info <name>"); return; }
        StageData data = stageManager.getStage(args[1]);
        if (data == null) { MessageUtil.error(player, "Stage '" + args[1] + "' not found."); return; }

        player.sendMessage(Component.text("═══ Stage: " + data.getStageName() + " ═══")
                .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text(" Enabled: " + data.isEnabled()).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" PVP: " + data.isPvpEnabled()).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" Player Limit: " + data.getPlayerLimit()).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" Completion Limit: " + data.getCompletionLimit()).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" Countdown: " + data.getCountdownSeconds() + "s").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" World: " + data.getWorldName()).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" Start Region: " + (data.getStartRegion() != null ? "✔ Set" : "✖ Not set")).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" Finish Region: " + (data.getFinishRegion() != null ? "✔ Set" : "✖ Not set")).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" Checkpoints: " + data.getCheckpoints().size()).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" Moving Structures: " + data.getMovingStructures().size()).color(NamedTextColor.YELLOW));
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.text("═══ ParkourFest Commands ═══").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        String[][] cmds = {
                {"/pf create <name>", "Create a new stage"},
                {"/pf delete <name>", "Delete a stage"},
                {"/pf tools <name>", "Open editor tools"},
                {"/pf enable <name>", "Enable a stage"},
                {"/pf disable <name>", "Disable a stage"},
                {"/pf start <name>", "Start a game"},
                {"/pf stop <name>", "Stop a game"},
                {"/pf pvp <on/off> <name>", "Toggle PVP"},
                {"/pf structures <start/stop/reset> <name>", "Control structures"},
                {"/pf list", "List all stages"},
                {"/pf info <name>", "Show stage details"}
        };
        for (String[] cmd : cmds) {
            player.sendMessage(Component.text(" " + cmd[0]).color(NamedTextColor.AQUA)
                    .append(Component.text(" — " + cmd[1]).color(NamedTextColor.GRAY)));
        }
    }
}
