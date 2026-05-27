package com.spy.parkourfest.model;

/**
 * Represents a checkpoint in a parkour stage.
 * Players touching a checkpoint register it as their respawn point.
 */
public class CheckpointData {

    private int id;
    private LocationData location;

    public CheckpointData() {
        // Default constructor for Gson
    }

    public CheckpointData(int id, LocationData location) {
        this.id = id;
        this.location = location;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocationData getLocation() { return location; }
    public void setLocation(LocationData location) { this.location = location; }

    @Override
    public String toString() {
        return "Checkpoint #" + id + " at " + location;
    }
}
