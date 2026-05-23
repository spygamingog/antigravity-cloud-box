package com.spy.parkourfest.game;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.model.CheckpointData;
import com.spy.parkourfest.model.StageData;
import com.spy.parkourfest.stage.StageSession;
import com.spy.parkourfest.util.MessageUtil;
import com.spy.parkourfest.util.RegionUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

/**
 * Listens for gameplay events during active parkour games.
 * Handles: movement freeze, void falls, checkpoints, finish detection, PVP.
 */
public class GameListener implements Listener {

    private final ParkourFest plugin;
    private final GameManager gameManager;

    private static final double CHECKPOINT_RADIUS = 2.5;

    public GameListener(ParkourFest plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Skip OPs/admins
        if (player.hasPermission("parkourfest.admin")) return;

        UUID uuid = player.getUniqueId();

        // Check all active game sessions
        for (String stageName : gameManager.getActiveStageNames()) {
            StageSession session = gameManager.getSession(stageName);
            if (session == null || !session.isParticipant(uuid)) continue;

            StageData data = session.getStageData();

            switch (session.getState()) {
                case COUNTDOWN -> handleCountdownFreeze(event, player);
                case ACTIVE -> {
                    handleVoidFall(event, player, session, data);
                    handleCheckpoints(player, session, data);
                    handleFinishDetection(player, session, data, stageName);
                }
            }
            return; // Player can only be in one game at a time
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.hasPermission("parkourfest.admin")) return;

        UUID uuid = player.getUniqueId();

        for (String stageName : gameManager.getActiveStageNames()) {
            StageSession session = gameManager.getSession(stageName);
            if (session == null || !session.isParticipant(uuid)) continue;

            // Handle lava/fire damage — teleport back to checkpoint
            if (event.getCause() == EntityDamageEvent.DamageCause.LAVA
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
                respawnAtCheckpoint(player, session, session.getStageData());
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        for (String stageName : gameManager.getActiveStageNames()) {
            StageSession session = gameManager.getSession(stageName);
            if (session == null) continue;

            boolean victimInGame = session.isParticipant(victim.getUniqueId());
            boolean attackerInGame = session.isParticipant(attacker.getUniqueId());

            if (victimInGame || attackerInGame) {
                if (!session.getStageData().isPvpEnabled()) {
                    event.setCancelled(true);
                }
                return;
            }
        }
    }

    // --- Private Handlers ---

    private void handleCountdownFreeze(PlayerMoveEvent event, Player player) {
        // Allow head rotation but freeze position
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(),
                    to.getYaw(), to.getPitch()));
        }
    }

    private void handleVoidFall(PlayerMoveEvent event, Player player,
                                 StageSession session, StageData data) {
        Location to = event.getTo();
        if (to == null) return;

        // Check if player fell into the void (below world min height)
        double minY = player.getWorld().getMinHeight();
        if (to.getY() < minY + 5) {
            respawnAtCheckpoint(player, session, data);
        }
    }

    private void handleCheckpoints(Player player, StageSession session, StageData data) {
        Location loc = player.getLocation();

        for (CheckpointData cp : data.getCheckpoints()) {
            if (RegionUtil.isNearCheckpoint(loc, cp.getLocation(), CHECKPOINT_RADIUS)) {
                int currentCp = session.getPlayerCheckpoint(player.getUniqueId());
                if (currentCp < cp.getId()) {
                    session.setPlayerCheckpoint(player.getUniqueId(), cp.getId());
                    MessageUtil.actionBar(player, "✔ Checkpoint #" + cp.getId() + " reached!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                }
            }
        }
    }

    private void handleFinishDetection(Player player, StageSession session,
                                        StageData data, String stageName) {
        if (data.getFinishRegion() == null) return;
        if (session.getFinishOrder().contains(player.getUniqueId())) return;

        if (data.getFinishRegion().contains(player.getLocation())) {
            gameManager.onPlayerFinish(stageName, player);
        }
    }

    private void respawnAtCheckpoint(Player player, StageSession session, StageData data) {
        int cpId = session.getPlayerCheckpoint(player.getUniqueId());
        Location respawnLoc;

        if (cpId > 0) {
            CheckpointData cp = data.getCheckpointById(cpId);
            if (cp != null) {
                respawnLoc = cp.getLocation().toBukkit(data.getWorldName());
            } else {
                respawnLoc = data.getStartRegion().getSpawnBukkit();
            }
        } else {
            respawnLoc = data.getStartRegion().getSpawnBukkit();
        }

        // Cancel velocity and teleport
        player.setFallDistance(0);
        player.setVelocity(player.getVelocity().zero());
        player.teleport(respawnLoc);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        MessageUtil.actionBar(player, "☠ Respawned at checkpoint!");
    }
}
