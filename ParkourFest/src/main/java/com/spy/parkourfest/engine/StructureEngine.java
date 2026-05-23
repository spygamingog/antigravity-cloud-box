package com.spy.parkourfest.engine;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.model.MovingStructureData;
import com.spy.parkourfest.model.StageData;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

/**
 * Orchestrates all moving structures within a single stage.
 * Handles spawning, starting, stopping, and resetting all structures.
 */
public class StructureEngine {

    private final ParkourFest plugin;
    private final StageData stageData;
    private final Map<String, MovingStructure> structures = new LinkedHashMap<>();

    public StructureEngine(ParkourFest plugin, StageData stageData) {
        this.plugin = plugin;
        this.stageData = stageData;
    }

    /**
     * Load and spawn all structures from stage data.
     */
    public void loadAndSpawn() {
        World world = Bukkit.getWorld(stageData.getWorldName());
        if (world == null) {
            plugin.getLogger().warning("Cannot load structures: world '"
                    + stageData.getWorldName() + "' not found!");
            return;
        }

        for (MovingStructureData data : stageData.getMovingStructures()) {
            MovingStructure structure;
            if (data.getType() == MovingStructureData.StructureType.LINEAR) {
                structure = new LinearStructure(plugin, data, world);
            } else {
                structure = new RotationalStructure(plugin, data, world);
            }
            structure.spawn();
            structures.put(data.getId(), structure);
        }

        plugin.getLogger().info("Loaded " + structures.size()
                + " structures for stage: " + stageData.getStageName());
    }

    /**
     * Start all structures.
     */
    public void startAll() {
        structures.values().forEach(MovingStructure::start);
    }

    /**
     * Stop all structures.
     */
    public void stopAll() {
        structures.values().forEach(MovingStructure::stop);
    }

    /**
     * Reset all structures to initial positions.
     */
    public void resetAll() {
        structures.values().forEach(MovingStructure::reset);
    }

    /**
     * Despawn and remove all structures.
     */
    public void despawnAll() {
        structures.values().forEach(MovingStructure::despawn);
        structures.clear();
    }

    /**
     * Get a structure by ID.
     */
    public MovingStructure getStructure(String id) {
        return structures.get(id);
    }

    /**
     * Get all active structures.
     */
    public Collection<MovingStructure> getAllStructures() {
        return Collections.unmodifiableCollection(structures.values());
    }

    /**
     * Check if any structures are currently running.
     */
    public boolean isAnyRunning() {
        return structures.values().stream().anyMatch(MovingStructure::isRunning);
    }
}
