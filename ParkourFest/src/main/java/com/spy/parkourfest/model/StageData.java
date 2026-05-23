package com.spy.parkourfest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete configuration data for a parkour stage.
 * Each stage is serialized to its own JSON file.
 */
public class StageData {

    private String stageName;
    private boolean enabled = false;
    private boolean pvpEnabled = false;
    private int playerLimit = 20;
    private int completionLimit = 3;
    private int countdownSeconds = 10;

    // The world this stage exists in
    private String worldName = "world";

    // Start and finish zones
    private RegionData startRegion;
    private RegionData finishRegion;

    // Ordered list of checkpoints
    private List<CheckpointData> checkpoints = new ArrayList<>();

    // Dynamic moving structures (platforms, spinners, sweepers)
    private List<MovingStructureData> movingStructures = new ArrayList<>();

    public StageData() {
        // Default constructor for Gson
    }

    public StageData(String stageName) {
        this.stageName = stageName;
    }

    /**
     * Get the next available checkpoint ID.
     */
    public int getNextCheckpointId() {
        return checkpoints.stream()
                .mapToInt(CheckpointData::getId)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Find a checkpoint by its ID.
     */
    public CheckpointData getCheckpointById(int id) {
        return checkpoints.stream()
                .filter(cp -> cp.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Remove a checkpoint by its ID.
     */
    public boolean removeCheckpoint(int id) {
        return checkpoints.removeIf(cp -> cp.getId() == id);
    }

    /**
     * Find the closest checkpoint to a location.
     */
    public CheckpointData findNearestCheckpoint(LocationData loc) {
        CheckpointData nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (CheckpointData cp : checkpoints) {
            double dist = cp.getLocation().distanceSquared(loc);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = cp;
            }
        }
        return nearest;
    }

    /**
     * Count moving structures by type.
     */
    public int countStructuresByType(MovingStructureData.StructureType type) {
        return (int) movingStructures.stream()
                .filter(s -> s.getType() == type)
                .count();
    }

    /**
     * Check if this stage has the minimum required configuration to run a game.
     */
    public boolean isPlayable() {
        return enabled
                && startRegion != null && startRegion.isValid()
                && finishRegion != null && finishRegion.isValid();
    }

    // --- Getters & Setters ---

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }

    public int getPlayerLimit() { return playerLimit; }
    public void setPlayerLimit(int playerLimit) { this.playerLimit = playerLimit; }

    public int getCompletionLimit() { return completionLimit; }
    public void setCompletionLimit(int completionLimit) { this.completionLimit = completionLimit; }

    public int getCountdownSeconds() { return countdownSeconds; }
    public void setCountdownSeconds(int countdownSeconds) { this.countdownSeconds = countdownSeconds; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public RegionData getStartRegion() { return startRegion; }
    public void setStartRegion(RegionData startRegion) { this.startRegion = startRegion; }

    public RegionData getFinishRegion() { return finishRegion; }
    public void setFinishRegion(RegionData finishRegion) { this.finishRegion = finishRegion; }

    public List<CheckpointData> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(List<CheckpointData> checkpoints) { this.checkpoints = checkpoints; }

    public List<MovingStructureData> getMovingStructures() { return movingStructures; }
    public void setMovingStructures(List<MovingStructureData> movingStructures) { this.movingStructures = movingStructures; }
}
