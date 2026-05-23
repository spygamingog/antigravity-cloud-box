package com.spy.parkourfest.engine;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.stage.StageSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.UUID;

/**
 * Handles player-platform physics injection.
 * When a player stands on a moving platform, applies the platform's velocity
 * to the player so they "ride" it smoothly (Create Mod / Fall Guys style).
 */
public class PlatformPhysics {

    private final ParkourFest plugin;
    private final StageSession session;
    private BukkitTask physicsTask;

    private static final double DETECTION_RADIUS = 1.2;
    private static final double VERTICAL_CHECK = 0.35;

    public PlatformPhysics(ParkourFest plugin, StageSession session) {
        this.plugin = plugin;
        this.session = session;
    }

    /**
     * Start the physics tick loop.
     */
    public void start() {
        if (physicsTask != null) return;

        physicsTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 1L);
    }

    /**
     * Stop the physics tick loop.
     */
    public void stop() {
        if (physicsTask != null) {
            physicsTask.cancel();
            physicsTask = null;
        }
    }

    /**
     * Every tick, check each participant for platform contact and apply velocity.
     */
    private void tick() {
        StructureEngine engine = session.getStructureEngine();
        if (engine == null) return;

        Collection<MovingStructure> structures = engine.getAllStructures();
        if (structures.isEmpty()) return;

        for (UUID playerId : session.getParticipants()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) continue;

            Location playerLoc = player.getLocation();
            Location feetLoc = playerLoc.clone().subtract(0, VERTICAL_CHECK, 0);

            // Check each structure for nearby collision entities
            for (MovingStructure structure : structures) {
                if (!structure.isRunning()) continue;

                for (int i = 0; i < structure.getCollisionEntities().size(); i++) {
                    Shulker shulker = structure.getCollisionEntities().get(i);
                    if (!shulker.isValid()) continue;

                    Location shulkerLoc = shulker.getLocation();

                    // Check if player is standing on/near this shulker
                    double dx = feetLoc.getX() - shulkerLoc.getX();
                    double dz = feetLoc.getZ() - shulkerLoc.getZ();
                    double dy = feetLoc.getY() - shulkerLoc.getY();
                    double horizontalDist = dx * dx + dz * dz;

                    if (horizontalDist <= DETECTION_RADIUS * DETECTION_RADIUS
                            && dy >= -0.5 && dy <= 1.5) {
                        // Player is on this platform block!
                        Location velocity = structure.getBlockVelocity(i);
                        if (velocity != null) {
                            Vector vel = player.getVelocity();
                            vel.setX(vel.getX() + velocity.getX());
                            vel.setZ(vel.getZ() + velocity.getZ());
                            // Only apply Y velocity if platform is moving up
                            if (velocity.getY() > 0) {
                                vel.setY(vel.getY() + velocity.getY());
                            }
                            player.setVelocity(vel);
                        }
                        break; // Only apply one platform's velocity per tick
                    }
                }
            }
        }
    }
}
