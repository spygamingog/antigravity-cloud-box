package com.spy.parkourfest.stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.model.StageData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages loading, saving, creating, and deleting stage configurations.
 * Each stage is stored as its own JSON file in plugins/ParkourFest/stages/.
 */
public class StageManager {

    private final ParkourFest plugin;
    private final File stagesDir;
    private final Gson gson;
    private final Map<String, StageData> stages = new LinkedHashMap<>();

    public StageManager(ParkourFest plugin) {
        this.plugin = plugin;
        this.stagesDir = new File(plugin.getDataFolder(), "stages");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        if (!stagesDir.exists()) {
            stagesDir.mkdirs();
        }
    }

    /**
     * Load all stage JSON files from the stages directory.
     */
    public void loadAll() {
        stages.clear();
        File[] files = stagesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No stages found in " + stagesDir.getPath());
            return;
        }

        for (File file : files) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                StageData data = gson.fromJson(reader, StageData.class);
                if (data != null && data.getStageName() != null) {
                    stages.put(data.getStageName().toLowerCase(), data);
                    plugin.getLogger().info("Loaded stage: " + data.getStageName());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load stage from " + file.getName(), e);
            }
        }

        plugin.getLogger().info("Loaded " + stages.size() + " stage(s) total.");
    }

    /**
     * Save a single stage to its JSON file.
     */
    public void save(String name) {
        String key = name.toLowerCase();
        StageData data = stages.get(key);
        if (data == null) {
            plugin.getLogger().warning("Cannot save stage '" + name + "' — not found in memory.");
            return;
        }

        File file = new File(stagesDir, key + ".json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save stage: " + name, e);
        }
    }

    /**
     * Save all stages to disk.
     */
    public void saveAll() {
        for (String name : stages.keySet()) {
            save(name);
        }
    }

    /**
     * Create a new empty stage.
     * @return the created StageData, or null if it already exists
     */
    public StageData createStage(String name) {
        String key = name.toLowerCase();
        if (stages.containsKey(key)) {
            return null; // Already exists
        }

        StageData data = new StageData(name);
        stages.put(key, data);
        save(key);
        plugin.getLogger().info("Created new stage: " + name);
        return data;
    }

    /**
     * Delete a stage and its JSON file.
     * @return true if deleted, false if not found
     */
    public boolean deleteStage(String name) {
        String key = name.toLowerCase();
        StageData removed = stages.remove(key);
        if (removed == null) return false;

        File file = new File(stagesDir, key + ".json");
        if (file.exists()) {
            file.delete();
        }

        plugin.getLogger().info("Deleted stage: " + name);
        return true;
    }

    /**
     * Get a stage by name (case-insensitive).
     */
    public StageData getStage(String name) {
        return stages.get(name.toLowerCase());
    }

    /**
     * Check if a stage exists.
     */
    public boolean stageExists(String name) {
        return stages.containsKey(name.toLowerCase());
    }

    /**
     * Get all stage names.
     */
    public Set<String> getStageNames() {
        return Collections.unmodifiableSet(stages.keySet());
    }

    /**
     * Get all loaded stages.
     */
    public Collection<StageData> getAllStages() {
        return Collections.unmodifiableCollection(stages.values());
    }
}
