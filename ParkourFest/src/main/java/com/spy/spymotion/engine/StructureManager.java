package com.spy.spymotion.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.spy.spymotion.SpyMotion;
import com.spy.spymotion.model.MovingStructureData;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Orchestrates and manages all moving structures globally.
 * Persists structures to structures.json, handles startup and shutdown lifecycles,
 * and hosts the 20Hz Bukkit tick scheduler.
 */
public class StructureManager {

    private final SpyMotion plugin;
    private final Map<String, MovingStructureData> structureConfigs = new LinkedHashMap<>();
    private final Map<String, MovingStructure> activeStructures = new HashMap<>();

    private BukkitTask tickTask;
    private final File configFile;
    private final Gson gson;

    public StructureManager(SpyMotion plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "structures.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Start the global 20Hz animation tick scheduler.
     */
    public void startScheduler() {
        if (tickTask != null) return;
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (MovingStructure struct : activeStructures.values()) {
                try {
                    struct.tick();
                } catch (Exception e) {
                    plugin.getLogger().warning("Error ticking structure '" + struct.getData().getId() + "': " + e.getMessage());
                }
            }
        }, 1L, 1L);
    }

    /**
     * Stop the global tick scheduler.
     */
    public void stopScheduler() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Load all structures from disk.
     */
    public void loadAll() {
        structureConfigs.clear();
        if (!configFile.exists()) {
            saveAll(); // Save empty configuration
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<List<MovingStructureData>>() {}.getType();
            List<MovingStructureData> list = gson.fromJson(reader, type);
            if (list != null) {
                for (MovingStructureData data : list) {
                    data.migrateOldFields();
                    structureConfigs.put(data.getId().toLowerCase(), data);
                }
            }
            plugin.getLogger().info("Successfully loaded " + structureConfigs.size() + " structures from structures.json");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load structures.json: " + e.getMessage());
        }
    }

    /**
     * Save all structure configurations to disk.
     */
    public void saveAll() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(new ArrayList<>(structureConfigs.values()), writer);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save structures.json: " + e.getMessage());
        }
    }

    /**
     * Create moving structure instances from config.
     */
    public void spawnAll() {
        despawnAll();
        for (MovingStructureData data : structureConfigs.values()) {
            spawn(data.getId());
        }
    }

    /**
     * Despawn all active structure entities.
     */
    public void despawnAll() {
        for (MovingStructure struct : activeStructures.values()) {
            struct.despawn();
        }
        activeStructures.clear();
    }

    /**
     * Add a new structure.
     */
    public void addStructure(MovingStructureData data) {
        structureConfigs.put(data.getId().toLowerCase(), data);
        saveAll();
    }

    /**
     * Remove a structure by ID.
     */
    public boolean removeStructure(String id) {
        String lowerId = id.toLowerCase();
        if (structureConfigs.containsKey(lowerId)) {
            despawn(lowerId);
            structureConfigs.remove(lowerId);
            saveAll();
            return true;
        }
        return false;
    }

    /**
     * Get structure config data by ID.
     */
    public MovingStructureData getStructureData(String id) {
        return structureConfigs.get(id.toLowerCase());
    }

    /**
     * Get active moving structure runtime instance by ID.
     */
    public MovingStructure getActiveStructure(String id) {
        return activeStructures.get(id.toLowerCase());
    }

    public List<String> getStructureIds() {
        return new ArrayList<>(structureConfigs.keySet());
    }

    // --- Control commands ---

    public void spawn(String id) {
        String lowerId = id.toLowerCase();
        MovingStructureData data = getStructureData(lowerId);
        if (data == null) return;

        despawn(lowerId);

        MovingStructure struct;
        if (data.getType() == MovingStructureData.StructureType.LINEAR) {
            struct = new LinearStructure(data);
        } else {
            struct = new RotationalStructure(data);
        }

        struct.spawn();
        activeStructures.put(lowerId, struct);
    }

    public void despawn(String id) {
        String lowerId = id.toLowerCase();
        MovingStructure struct = activeStructures.remove(lowerId);
        if (struct != null) {
            struct.despawn();
        }
    }

    public void start(String id) {
        MovingStructure struct = activeStructures.get(id.toLowerCase());
        if (struct != null) {
            struct.start();
        }
    }

    public void stop(String id) {
        MovingStructure struct = activeStructures.get(id.toLowerCase());
        if (struct != null) {
            struct.stop();
        }
    }

    public void reset(String id) {
        MovingStructure struct = activeStructures.get(id.toLowerCase());
        if (struct != null) {
            struct.reset();
        }
    }

    public void startAll() {
        for (MovingStructure struct : activeStructures.values()) {
            struct.start();
        }
    }

    public void stopAll() {
        for (MovingStructure struct : activeStructures.values()) {
            struct.stop();
        }
    }

    public void resetAll() {
        for (MovingStructure struct : activeStructures.values()) {
            struct.reset();
        }
    }
}
