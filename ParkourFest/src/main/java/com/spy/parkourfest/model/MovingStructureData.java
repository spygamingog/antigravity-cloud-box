package com.spy.parkourfest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration data for a dynamic moving structure (platform/obstacle).
 * Supports LINEAR (ping-pong) and ROTATIONAL (spinner) movement types.
 */
public class MovingStructureData {

    /**
     * Movement type enum.
     */
    public enum StructureType {
        LINEAR,      // Ping-pong movement along an axis
        ROTATIONAL   // Rotation around a center pivot
    }

    /**
     * Movement direction for both linear and rotational.
     */
    public enum Direction {
        POSITIVE,         // +X, +Y, or +Z for linear; Clockwise for rotational
        NEGATIVE,         // -X, -Y, or -Z for linear; Counter-clockwise for rotational
        CLOCKWISE,        // Alias for rotational
        COUNTER_CLOCKWISE // Alias for rotational
    }

    private String id;
    private StructureType type;

    // Block positions that form this structure (relative to origin)
    private List<LocationData> blocks = new ArrayList<>();

    // Material type for the display blocks
    private String blockMaterial = "STONE";

    // --- Linear-specific fields ---
    private String axis = "X";      // X, Y, or Z
    private double range = 5.0;     // Travel distance in blocks

    // --- Rotational-specific fields ---
    private LocationData center;     // Pivot point for rotation

    // --- Shared fields ---
    private double speed = 0.5;      // Blocks/tick for linear, degrees/tick for rotational
    private Direction direction = Direction.POSITIVE;

    // Initial state for reset purposes
    private List<LocationData> initialBlocks = new ArrayList<>();

    public MovingStructureData() {
        // Default constructor for Gson
    }

    public MovingStructureData(String id, StructureType type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Generate a unique ID based on type and a counter.
     */
    public static String generateId(StructureType type, int count) {
        String prefix = type == StructureType.LINEAR ? "linear" : "rotator";
        return prefix + "_" + (count + 1);
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public StructureType getType() { return type; }
    public void setType(StructureType type) { this.type = type; }

    public List<LocationData> getBlocks() { return blocks; }
    public void setBlocks(List<LocationData> blocks) {
        this.blocks = blocks;
        // Save initial state for reset
        this.initialBlocks = new ArrayList<>(blocks);
    }

    public String getBlockMaterial() { return blockMaterial; }
    public void setBlockMaterial(String blockMaterial) { this.blockMaterial = blockMaterial; }

    public String getAxis() { return axis; }
    public void setAxis(String axis) { this.axis = axis; }

    public double getRange() { return range; }
    public void setRange(double range) { this.range = range; }

    public LocationData getCenter() { return center; }
    public void setCenter(LocationData center) { this.center = center; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public List<LocationData> getInitialBlocks() { return initialBlocks; }
    public void setInitialBlocks(List<LocationData> initialBlocks) { this.initialBlocks = initialBlocks; }
}
