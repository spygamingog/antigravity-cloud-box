package com.spy.parkourfest.engine;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.model.LocationData;
import com.spy.parkourfest.model.MovingStructureData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for moving structures (platforms/obstacles).
 * Manages BlockDisplay entities (visuals) and Shulker entities (collision).
 */
public abstract class MovingStructure {

    protected final ParkourFest plugin;
    protected final MovingStructureData data;
    protected final World world;

    // Display entities (visual blocks)
    protected final List<BlockDisplay> displayEntities = new ArrayList<>();
    // Collision entities (invisible shulkers for solidity)
    protected final List<Shulker> collisionEntities = new ArrayList<>();
    // Current world positions of each block
    protected final List<Location> currentPositions = new ArrayList<>();
    // Previous tick positions (for velocity calculation)
    protected final List<Location> previousPositions = new ArrayList<>();

    protected boolean running = false;
    protected BukkitTask animationTask;

    public MovingStructure(ParkourFest plugin, MovingStructureData data, World world) {
        this.plugin = plugin;
        this.data = data;
        this.world = world;
    }

    /**
     * Spawn all display and collision entities for this structure.
     */
    public void spawn() {
        Material material;
        try {
            material = Material.valueOf(data.getBlockMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }

        for (LocationData blockData : data.getBlocks()) {
            Location loc = blockData.toBukkit(world);
            currentPositions.add(loc.clone());
            previousPositions.add(loc.clone());

            // Spawn BlockDisplay (visual)
            final Material displayMat = material;
            BlockDisplay display = world.spawn(loc, BlockDisplay.class, entity -> {
                entity.setBlock(displayMat.createBlockData());
                entity.setPersistent(false);
                entity.setGravity(false);
            });
            displayEntities.add(display);

            // Spawn invisible Shulker (collision)
            Shulker shulker = world.spawn(loc, Shulker.class, entity -> {
                entity.setAI(false);
                entity.setInvisible(true);
                entity.setSilent(true);
                entity.setInvulnerable(true);
                entity.setPersistent(false);
                entity.setGravity(false);
                entity.setCollidable(true);
            });
            collisionEntities.add(shulker);
        }
    }

    /**
     * Remove all entities from the world.
     */
    public void despawn() {
        stop();
        displayEntities.forEach(Entity::remove);
        collisionEntities.forEach(Entity::remove);
        displayEntities.clear();
        collisionEntities.clear();
        currentPositions.clear();
        previousPositions.clear();
    }

    /**
     * Start the animation loop.
     */
    public void start() {
        if (running) return;
        running = true;

        animationTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Save previous positions for velocity calculation
            for (int i = 0; i < currentPositions.size(); i++) {
                previousPositions.get(i).setX(currentPositions.get(i).getX());
                previousPositions.get(i).setY(currentPositions.get(i).getY());
                previousPositions.get(i).setZ(currentPositions.get(i).getZ());
            }
            tick();
        }, 0L, 1L); // Every tick (20Hz)
    }

    /**
     * Stop the animation loop.
     */
    public void stop() {
        running = false;
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
    }

    /**
     * Stop and return to initial positions.
     */
    public void reset() {
        stop();
        for (int i = 0; i < data.getBlocks().size() && i < displayEntities.size(); i++) {
            Location initialLoc = data.getBlocks().get(i).toBukkit(world);
            currentPositions.get(i).setX(initialLoc.getX());
            currentPositions.get(i).setY(initialLoc.getY());
            currentPositions.get(i).setZ(initialLoc.getZ());
            teleportEntities(i, initialLoc);
        }
    }

    /**
     * Teleport both display and collision entities for a block index.
     */
    protected void teleportEntities(int index, Location newLoc) {
        if (index < displayEntities.size()) {
            BlockDisplay display = displayEntities.get(index);
            if (display.isValid()) {
                display.teleport(newLoc);
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(1);
            }
        }
        if (index < collisionEntities.size()) {
            Shulker shulker = collisionEntities.get(index);
            if (shulker.isValid()) {
                shulker.teleport(newLoc);
            }
        }
    }

    /**
     * Called every tick when running. Subclasses implement movement logic.
     */
    protected abstract void tick();

    /**
     * Get the velocity vector of a specific block in this structure (current - previous).
     */
    public Location getBlockVelocity(int index) {
        if (index >= currentPositions.size()) return null;
        Location cur = currentPositions.get(index);
        Location prev = previousPositions.get(index);
        return new Location(world,
                cur.getX() - prev.getX(),
                cur.getY() - prev.getY(),
                cur.getZ() - prev.getZ());
    }

    // --- Getters ---

    public MovingStructureData getData() { return data; }
    public String getId() { return data.getId(); }
    public boolean isRunning() { return running; }
    public List<Location> getCurrentPositions() { return currentPositions; }
    public List<Shulker> getCollisionEntities() { return collisionEntities; }
}
