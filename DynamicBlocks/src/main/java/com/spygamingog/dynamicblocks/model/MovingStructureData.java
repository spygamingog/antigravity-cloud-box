package com.spygamingog.dynamicblocks.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration data for a dynamic moving structure (platform/obstacle).
 * Supports LINEAR (path-based movement along points) and ROTATIONAL (spinner) movement types.
 * Each structure has a unique ID, optional display name, and full
 * configuration for speed, direction, delays, and block composition.
 */
public class MovingStructureData {

    public enum StructureType {
        LINEAR,
        ROTATIONAL
    }

    public enum Direction {
        POSITIVE,
        NEGATIVE,
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    // Identity
    private String id;
    private String name;  // Optional display name (falls back to id)
    private StructureType type;
    private String worldName = "world";

    // Block positions that form this structure (relative or absolute)
    private List<LocationData> blocks = new ArrayList<>();
    private String blockMaterial = "STONE";

    // --- Path-based (formerly Linear) settings ---
    private LocationData startPoint;
    private List<PathPoint> pathPoints = new ArrayList<>();
    private LocationData endPoint;
    private double endPointDelay = 0.0;     // delay at end point in seconds
    private double endPointSpeed = 2.0;     // travel speed to end point in blocks/second
    private double startPointDelay = 0.0;   // delay at start point in seconds
    private boolean loop = false;           // loop restarts at origin instead of ping-pong
    private int cycleCount = 1;             // number of concurrently active cycle instances
    private double cycleDelay = 2.0;        // seconds of delay before spawning subsequent cycles

    // --- Legacy Linear-specific (for GSON backward compatibility) ---
    private String axis = "X";
    private double range = 5.0;   // Distance in blocks to travel
    private int endpointDelayTicks = 0;
    private int loopDelayTicks = 0;

    // --- Rotational-specific ---
    private LocationData center;  // Pivot point
    private String rotationAxis = "Y"; // Axis to rotate around: X, Y, or Z

    // --- Shared / Legacy Shared ---
    private double speed = 2.0;   // blocks/sec for path, degrees/sec for rotational
    private Direction direction = Direction.POSITIVE;

    // Initial state for reset
    private List<LocationData> initialBlocks = new ArrayList<>();

    public MovingStructureData() {}

    public MovingStructureData(String id, StructureType type) {
        this.id = id;
        this.type = type;
    }

    public static String generateId(StructureType type, int count) {
        String prefix = type == StructureType.LINEAR ? "linear" : "rotator";
        return prefix + "_" + (count + 1);
    }

    /**
     * Get the display name, falling back to ID if no name is set.
     */
    public String getDisplayName() {
        return (name != null && !name.isEmpty()) ? name : id;
    }

    /**
     * Calculate the center position of all blocks in this structure.
     */
    public LocationData calculateCenter() {
        if (blocks.isEmpty()) return new LocationData(0, 0, 0);
        double sx = 0, sy = 0, sz = 0;
        for (LocationData b : blocks) {
            sx += b.getX();
            sy += b.getY();
            sz += b.getZ();
        }
        int n = blocks.size();
        return new LocationData(sx / n, sy / n, sz / n);
    }

    /**
     * Migrate old-format configurations to the new path-based configuration seamlessly.
     */
    public void migrateOldFields() {
        if (type == StructureType.LINEAR) {
            if (startPoint == null) {
                if (!blocks.isEmpty()) {
                    startPoint = blocks.get(0);
                } else {
                    startPoint = new LocationData(0, 0, 0);
                }
            }
            if (endPoint == null) {
                double appliedRange = (direction == Direction.NEGATIVE) ? -range : range;
                double x = startPoint.getX();
                double y = startPoint.getY();
                double z = startPoint.getZ();
                if ("X".equalsIgnoreCase(axis)) x += appliedRange;
                else if ("Y".equalsIgnoreCase(axis)) y += appliedRange;
                else z += appliedRange;
                endPoint = new LocationData(x, y, z);
                
                endPointSpeed = speed * 20.0;
                if (endPointSpeed <= 0.05) endPointSpeed = 2.0;
                
                endPointDelay = endpointDelayTicks / 20.0;
                startPointDelay = loopDelayTicks / 20.0;
                loop = false;
                cycleCount = 1;
                cycleDelay = 2.0;
            }
        } else if (type == StructureType.ROTATIONAL) {
            if (speed < 10.0) {
                speed = speed * 20.0;
                if (speed <= 0.05) speed = 30.0; // Default 30 degrees/second
            }
        }
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StructureType getType() { return type; }
    public void setType(StructureType type) { this.type = type; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public List<LocationData> getBlocks() { return blocks; }
    public void setBlocks(List<LocationData> blocks) {
        this.blocks = blocks;
        this.initialBlocks = new ArrayList<>(blocks);
    }

    public String getBlockMaterial() { return blockMaterial; }
    public void setBlockMaterial(String blockMaterial) { this.blockMaterial = blockMaterial; }

    public LocationData getStartPoint() { return startPoint; }
    public void setStartPoint(LocationData startPoint) { this.startPoint = startPoint; }

    public List<PathPoint> getPathPoints() { return pathPoints; }
    public void setPathPoints(List<PathPoint> pathPoints) { this.pathPoints = pathPoints; }

    public LocationData getEndPoint() { return endPoint; }
    public void setEndPoint(LocationData endPoint) { this.endPoint = endPoint; }

    public double getEndPointDelay() { return endPointDelay; }
    public void setEndPointDelay(double endPointDelay) { this.endPointDelay = endPointDelay; }

    public double getEndPointSpeed() { return endPointSpeed; }
    public void setEndPointSpeed(double endPointSpeed) { this.endPointSpeed = endPointSpeed; }

    public double getStartPointDelay() { return startPointDelay; }
    public void setStartPointDelay(double startPointDelay) { this.startPointDelay = startPointDelay; }

    public boolean isLoop() { return loop; }
    public void setLoop(boolean loop) { this.loop = loop; }

    public int getCycleCount() { return cycleCount; }
    public void setCycleCount(int cycleCount) { this.cycleCount = cycleCount; }

    public double getCycleDelay() { return cycleDelay; }
    public void setCycleDelay(double cycleDelay) { this.cycleDelay = cycleDelay; }

    // Legacy fields getters/setters for completeness
    public String getAxis() { return axis; }
    public void setAxis(String axis) { this.axis = axis; }

    public double getRange() { return range; }
    public void setRange(double range) { this.range = range; }

    public int getEndpointDelayTicks() { return endpointDelayTicks; }
    public void setEndpointDelayTicks(int endpointDelayTicks) { this.endpointDelayTicks = endpointDelayTicks; }

    public int getLoopDelayTicks() { return loopDelayTicks; }
    public void setLoopDelayTicks(int loopDelayTicks) { this.loopDelayTicks = loopDelayTicks; }

    public LocationData getCenter() { return center; }
    public void setCenter(LocationData center) { this.center = center; }

    public String getRotationAxis() { return rotationAxis != null ? rotationAxis : "Y"; }
    public void setRotationAxis(String rotationAxis) { this.rotationAxis = rotationAxis; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public List<LocationData> getInitialBlocks() { return initialBlocks; }
    public void setInitialBlocks(List<LocationData> initialBlocks) { this.initialBlocks = initialBlocks; }
}
