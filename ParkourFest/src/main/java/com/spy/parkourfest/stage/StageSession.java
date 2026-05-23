package com.spy.parkourfest.stage;

import com.spy.parkourfest.engine.PlatformPhysics;
import com.spy.parkourfest.engine.StructureEngine;
import com.spy.parkourfest.game.GameState;
import com.spy.parkourfest.model.StageData;

import java.util.*;

/**
 * Runtime state for an active game session on a stage.
 * Tracks participants, spectators, checkpoints, winners, and timing.
 */
public class StageSession {

    private final StageData stageData;
    private GameState state = GameState.IDLE;

    // Players actively running the parkour (non-OP)
    private final Set<UUID> participants = new LinkedHashSet<>();
    // OPs/admins watching (not counted for elimination)
    private final Set<UUID> spectators = new LinkedHashSet<>();
    // Last checkpoint ID each player reached
    private final Map<UUID, Integer> playerCheckpoints = new HashMap<>();
    // Ordered list of players who crossed the finish line
    private final List<UUID> finishOrder = new ArrayList<>();

    // Engine managing moving structures
    private StructureEngine structureEngine;
    // Physics engine for platform riding
    private PlatformPhysics platformPhysics;

    // Timing
    private long startTime;
    private long endTime;

    public StageSession(StageData stageData) {
        this.stageData = stageData;
    }

    /**
     * Add a participant to the game.
     */
    public void addParticipant(UUID playerId) {
        participants.add(playerId);
    }

    /**
     * Add a spectator (OP/admin).
     */
    public void addSpectator(UUID playerId) {
        spectators.add(playerId);
    }

    /**
     * Register a player reaching a checkpoint.
     */
    public void setPlayerCheckpoint(UUID playerId, int checkpointId) {
        playerCheckpoints.put(playerId, checkpointId);
    }

    /**
     * Get the last checkpoint a player reached.
     * @return checkpoint ID, or -1 if no checkpoint reached
     */
    public int getPlayerCheckpoint(UUID playerId) {
        return playerCheckpoints.getOrDefault(playerId, -1);
    }

    /**
     * Record a player crossing the finish line.
     * @return their finish position (1st, 2nd, 3rd...)
     */
    public int addFinisher(UUID playerId) {
        if (!finishOrder.contains(playerId)) {
            finishOrder.add(playerId);
        }
        return finishOrder.indexOf(playerId) + 1;
    }

    /**
     * Check if the completion limit has been reached.
     */
    public boolean isCompletionLimitReached() {
        return finishOrder.size() >= stageData.getCompletionLimit();
    }

    /**
     * Check if a player is a participant.
     */
    public boolean isParticipant(UUID playerId) {
        return participants.contains(playerId);
    }

    /**
     * Get elapsed time in seconds since game start.
     */
    public long getElapsedSeconds() {
        long end = (endTime > 0) ? endTime : System.currentTimeMillis();
        return (end - startTime) / 1000;
    }

    // --- Getters & Setters ---

    public StageData getStageData() { return stageData; }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public Set<UUID> getParticipants() { return participants; }
    public Set<UUID> getSpectators() { return spectators; }
    public List<UUID> getFinishOrder() { return finishOrder; }

    public StructureEngine getStructureEngine() { return structureEngine; }
    public void setStructureEngine(StructureEngine structureEngine) { this.structureEngine = structureEngine; }

    public PlatformPhysics getPlatformPhysics() { return platformPhysics; }
    public void setPlatformPhysics(PlatformPhysics platformPhysics) { this.platformPhysics = platformPhysics; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
}
