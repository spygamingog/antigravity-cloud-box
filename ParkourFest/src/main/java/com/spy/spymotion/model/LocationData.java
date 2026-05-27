package com.spy.spymotion.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Serializable location wrapper that can be persisted to JSON.
 * Stores relative coordinates or absolute coordinates without hard world references.
 */
public class LocationData {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public LocationData() {
        // Default constructor for Gson
    }

    public LocationData(double x, double y, double z) {
        this(x, y, z, 0.0f, 0.0f);
    }

    public LocationData(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Create a LocationData from a Bukkit Location.
     */
    public static LocationData from(Location loc) {
        return new LocationData(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Create a LocationData from block coordinates (center of block bottom).
     */
    public static LocationData fromBlock(int x, int y, int z) {
        return new LocationData(x + 0.5, y, z + 0.5, 0.0f, 0.0f);
    }

    /**
     * Convert to a Bukkit Location in the given world.
     */
    public Location toBukkit(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Get the distance squared to another LocationData.
     */
    public double distanceSquared(LocationData other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    // --- Getters & Setters ---

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f, %.1f | yaw=%.1f, pitch=%.1f)", x, y, z, yaw, pitch);
    }
}
