package com.spy.parkourfest.game;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.engine.PlatformPhysics;
import com.spy.parkourfest.engine.StructureEngine;
import com.spy.parkourfest.model.StageData;
import com.spy.parkourfest.stage.StageManager;
import com.spy.parkourfest.stage.StageSession;
import com.spy.parkourfest.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages the full game lifecycle for parkour stages.
 * Handles: start → countdown → active play → elimination → end.
 */
public class GameManager {

    private final ParkourFest plugin;
    private final StageManager stageManager;
    private final Map<String, StageSession> activeSessions = new HashMap<>();

    public GameManager(ParkourFest plugin, StageManager stageManager) {
        this.plugin = plugin;
        this.stageManager = stageManager;
    }

    /**
     * Start a game on a stage.
     * @return error message, or null on success
     */
    public String start(String stageName, Player initiator) {
        String key = stageName.toLowerCase();
        StageData data = stageManager.getStage(key);
        if (data == null) return "Stage '" + stageName + "' does not exist.";
        if (!data.isPlayable()) return "Stage is not playable. Ensure start/finish regions are set and stage is enabled.";
        if (activeSessions.containsKey(key)) return "A game is already running on this stage!";

        StageSession session = new StageSession(data);

        // Collect participants: all online players who aren't admins
        Location startSpawn = data.getStartRegion().getSpawnBukkit();
        int count = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("parkourfest.admin")) {
                session.addSpectator(player.getUniqueId());
                continue;
            }
            if (count >= data.getPlayerLimit()) break;
            session.addParticipant(player.getUniqueId());
            count++;
        }

        if (session.getParticipants().isEmpty()) {
            return "No eligible participants found (non-OP players).";
        }

        activeSessions.put(key, session);

        // Teleport all participants to start
        for (UUID uuid : session.getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.teleport(startSpawn);
                player.setGameMode(GameMode.ADVENTURE);
                MessageUtil.info(player, "You've been teleported to the starting line!");
            }
        }

        // Start countdown
        session.setState(GameState.COUNTDOWN);
        startCountdown(key, session);

        MessageUtil.success(initiator, "Game starting on '" + stageName + "' with "
                + session.getParticipants().size() + " participants!");
        return null;
    }

    /**
     * Stop a running game.
     */
    public String stop(String stageName) {
        String key = stageName.toLowerCase();
        StageSession session = activeSessions.get(key);
        if (session == null) return "No game is running on stage '" + stageName + "'.";

        endGame(key, session, true);
        return null;
    }

    /**
     * Stop all running games (called on plugin disable).
     */
    public void stopAll() {
        for (Map.Entry<String, StageSession> entry : new HashMap<>(activeSessions).entrySet()) {
            endGame(entry.getKey(), entry.getValue(), true);
        }
    }

    /**
     * Control structures for a stage.
     */
    public String controlStructures(String stageName, String action) {
        String key = stageName.toLowerCase();
        StageSession session = activeSessions.get(key);
        if (session == null) return "No game running on '" + stageName + "'. Start a game first.";
        if (session.getStructureEngine() == null) return "No structures loaded for this stage.";

        switch (action.toLowerCase()) {
            case "start" -> { session.getStructureEngine().startAll(); return null; }
            case "stop" -> { session.getStructureEngine().stopAll(); return null; }
            case "reset" -> { session.getStructureEngine().resetAll(); return null; }
            default -> { return "Invalid action. Use: start, stop, or reset."; }
        }
    }

    /**
     * Get the session for a stage.
     */
    public StageSession getSession(String stageName) {
        return activeSessions.get(stageName.toLowerCase());
    }

    /**
     * Check if a game is running on a stage.
     */
    public boolean isGameRunning(String stageName) {
        return activeSessions.containsKey(stageName.toLowerCase());
    }

    /**
     * Get all stages with active games.
     */
    public Set<String> getActiveStageNames() {
        return Collections.unmodifiableSet(activeSessions.keySet());
    }

    /**
     * Called when a player crosses the finish line.
     */
    public void onPlayerFinish(String stageName, Player player) {
        StageSession session = activeSessions.get(stageName.toLowerCase());
        if (session == null || session.getState() != GameState.ACTIVE) return;
        if (!session.isParticipant(player.getUniqueId())) return;

        int position = session.addFinisher(player.getUniqueId());

        // Announce
        String suffix = switch (position) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };

        for (UUID uuid : session.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                MessageUtil.info(p, "🏆 " + player.getName() + " finished " + position + suffix + "!");
            }
        }

        MessageUtil.title(player, "🏆 " + position + suffix + " Place!", "Time: "
                + MessageUtil.formatTime(session.getElapsedSeconds()));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        // Put finisher in spectator mode
        player.setGameMode(GameMode.SPECTATOR);

        // Check if completion limit reached
        if (session.isCompletionLimitReached()) {
            endGame(stageName.toLowerCase(), session, false);
        }
    }

    // --- Private Methods ---

    private void startCountdown(String stageKey, StageSession session) {
        int seconds = session.getStageData().getCountdownSeconds();

        // Countdown task
        final int[] remaining = {seconds};
        BukkitTask countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remaining[0] <= 0) {
                // GO!
                session.setState(GameState.ACTIVE);
                session.setStartTime(System.currentTimeMillis());

                for (UUID uuid : session.getParticipants()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) MessageUtil.goTitle(p);
                }

                // Start structures
                StructureEngine engine = new StructureEngine(plugin, session.getStageData());
                engine.loadAndSpawn();
                engine.startAll();
                session.setStructureEngine(engine);

                // Start platform physics
                PlatformPhysics physics = new PlatformPhysics(plugin, session);
                physics.start();
                session.setPlatformPhysics(physics);

                return; // Task will be cancelled by the scheduler check below
            }

            for (UUID uuid : session.getParticipants()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) MessageUtil.countdownTitle(p, remaining[0]);
            }

            remaining[0]--;
        }, 0L, 20L); // Every second

        // Cancel countdown after it's done
        Bukkit.getScheduler().runTaskLater(plugin, countdownTask::cancel, (seconds + 1) * 20L);
    }

    private void endGame(String stageKey, StageSession session, boolean forced) {
        session.setState(GameState.ENDED);
        session.setEndTime(System.currentTimeMillis());

        // Stop physics
        if (session.getPlatformPhysics() != null) {
            session.getPlatformPhysics().stop();
        }

        // Stop and despawn structures
        if (session.getStructureEngine() != null) {
            session.getStructureEngine().stopAll();
            session.getStructureEngine().despawnAll();
        }

        // Announce results
        List<UUID> winners = session.getFinishOrder();
        for (UUID uuid : session.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            p.setGameMode(GameMode.SURVIVAL);

            if (forced) {
                MessageUtil.title(p, "Game Stopped", "The game was ended by an admin.");
            } else if (winners.contains(uuid)) {
                int pos = winners.indexOf(uuid) + 1;
                MessageUtil.title(p, "🏆 You Won!", pos + getOrdinalSuffix(pos) + " place!");
            } else {
                MessageUtil.title(p, "Eliminated!", "Better luck next time!");
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.3f, 0.5f);
            }

            // Teleport back to start
            try {
                Location start = session.getStageData().getStartRegion().getSpawnBukkit();
                p.teleport(start);
            } catch (Exception ignored) {}
        }

        activeSessions.remove(stageKey);
        plugin.getLogger().info("Game ended on stage: " + stageKey
                + " | Winners: " + winners.size() + " | Forced: " + forced);
    }

    private String getOrdinalSuffix(int n) {
        if (n >= 11 && n <= 13) return "th";
        return switch (n % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
