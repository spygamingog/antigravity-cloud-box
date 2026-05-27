package com.spygamingog.dynamicblocks.engine;

import com.spygamingog.dynamicblocks.model.LocationData;
import com.spygamingog.dynamicblocks.model.MovingStructureData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base abstract class representing runtime logic for a dynamic moving structure.
 * Manages active instances (especially useful for multi-cycle linear platforms).
 */
public abstract class MovingStructure {

    protected final MovingStructureData data;
    protected final World world;

    protected final List<ActiveInstance> instances = new ArrayList<>();
    protected boolean running = false;

    // Center pivot or home reference location
    protected Location currentOrigin;

    public MovingStructure(MovingStructureData data) {
        this.data = data;
        this.world = Bukkit.getWorld(data.getWorldName());
        
        LocationData home = data.getStartPoint();
        if (home == null && data.getType() == MovingStructureData.StructureType.ROTATIONAL) {
            home = data.getCenter();
        }
        if (home == null && !data.getBlocks().isEmpty()) {
            home = data.getBlocks().get(0);
        }
        if (home != null) {
            this.currentOrigin = home.toBukkit(world);
        } else {
            this.currentOrigin = new Location(world, 0, 100, 0);
        }
    }

    /**
     * Start animation loop.
     */
    public void start() {
        if (running) return;
        running = true;

        if (instances.isEmpty() && data.getType() == MovingStructureData.StructureType.LINEAR) {
            // First instance starts immediately
            ActiveInstance first = new ActiveInstance(this, 0.0);
            first.spawn(currentOrigin);
            instances.add(first);
        } else if (instances.isEmpty() && data.getType() == MovingStructureData.StructureType.ROTATIONAL) {
            ActiveInstance rotator = new ActiveInstance(this, 0.0);
            rotator.spawn(currentOrigin);
            instances.add(rotator);
        }
    }

    /**
     * Stop animation loop.
     */
    public void stop() {
        running = false;
    }

    /**
     * Reset the structure back to its home origin/angle.
     */
    public void reset() {
        stop();
        
        LocationData home = data.getStartPoint();
        if (home == null && data.getType() == MovingStructureData.StructureType.ROTATIONAL) {
            home = data.getCenter();
        }
        if (home == null && !data.getBlocks().isEmpty()) {
            home = data.getBlocks().get(0);
        }
        if (home != null) {
            this.currentOrigin = home.toBukkit(world);
        }
        
        boolean wasSpawned = !instances.isEmpty();
        despawn();
        
        if (wasSpawned) {
            spawn();
        }
    }

    /**
     * Spawn the home instance. Does not start the movement.
     */
    public void spawn() {
        if (!instances.isEmpty()) return;
        ActiveInstance homeInst = new ActiveInstance(this, 0.0);
        homeInst.spawn(currentOrigin);
        instances.add(homeInst);
    }

    /**
     * Despawn all active/inactive instances.
     */
    public void despawn() {
        for (ActiveInstance inst : instances) {
            inst.despawn();
        }
        instances.clear();
    }

    /**
     * Main animation tick (called every server tick).
     */
    public void tick() {
        if (!running) return;

        // Save previous origin positions for deltas before modifying them
        savePreviousOrigins();

        // Specific sub-class logic (movement interpolation)
        tickMovement();
    }

    protected abstract void tickMovement();

    /**
     * Saves the current locations of all blocks to the previous locations tracking lists.
     * Essential for physics/delta calculations.
     */
    protected void savePreviousOrigins() {
        for (ActiveInstance inst : instances) {
            if (!inst.isActive()) continue;
            List<Location> prev = inst.getPreviousPositions();
            List<Location> curr = inst.getCurrentPositions();
            for (int i = 0; i < curr.size(); i++) {
                prev.set(i, curr.get(i).clone());
            }
        }
    }

    // --- Getters & Setters ---

    public MovingStructureData getData() { return data; }
    public World getWorld() { return world; }
    public boolean isRunning() { return running; }
    public List<ActiveInstance> getInstances() { return instances; }
    public Location getCurrentOrigin() { return currentOrigin; }
}
