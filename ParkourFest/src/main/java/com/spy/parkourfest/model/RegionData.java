package com.spy.parkourfest.model;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents an axis-aligned bounding box region (Start or Finish zone).
 * Defined by two corner positions and an optional spawn point within.
 */
public class RegionData {

    private String world;
    private LocationData pos1;
    private LocationData pos2;
    private LocationData spawnLocation;

    public RegionData() {
        // Default constructor for Gson
    }

    public RegionData(String world, LocationData pos1, LocationData pos2) {
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        // Default spawn at center of region
        this.spawnLocation = new LocationData(
                (pos1.getX() + pos2.getX()) / 2.0,
                Math.min(pos1.getY(), pos2.getY()),
                (pos1.getZ() + pos2.getZ()) / 2.0,
                0.0f, 0.0f
        );
    }

    /**
     * Check if a Bukkit Location is inside this region (AABB containment).
     */
    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(world)) return false;

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    /**
     * Get the spawn location as a Bukkit Location.
     */
    public Location getSpawnBukkit() {
        return spawnLocation.toBukkit(world);
    }

    /**
     * Get the center of this region.
     */
    public Location getCenter() {
        World w = org.bukkit.Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w,
                (pos1.getX() + pos2.getX()) / 2.0,
                (pos1.getY() + pos2.getY()) / 2.0,
                (pos1.getZ() + pos2.getZ()) / 2.0
        );
    }

    // --- Getters & Setters ---

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }

    public LocationData getPos1() { return pos1; }
    public void setPos1(LocationData pos1) { this.pos1 = pos1; }

    public LocationData getPos2() { return pos2; }
    public void setPos2(LocationData pos2) { this.pos2 = pos2; }

    public LocationData getSpawnLocation() { return spawnLocation; }
    public void setSpawnLocation(LocationData spawnLocation) { this.spawnLocation = spawnLocation; }

    public boolean isValid() {
        return world != null && pos1 != null && pos2 != null;
    }
}
