package com.spy.parkourfest.util;

import com.spy.parkourfest.model.LocationData;
import org.bukkit.Location;

/**
 * Utilities for region/AABB geometry checks.
 */
public class RegionUtil {

    /**
     * Check if a Bukkit Location is inside an AABB defined by two corner points.
     */
    public static boolean isInside(Location loc, LocationData pos1, LocationData pos2) {
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
     * Get the center point between two locations.
     */
    public static LocationData center(LocationData pos1, LocationData pos2) {
        return new LocationData(
                (pos1.getX() + pos2.getX()) / 2.0,
                (pos1.getY() + pos2.getY()) / 2.0,
                (pos1.getZ() + pos2.getZ()) / 2.0
        );
    }

    /**
     * Check if a location is within a given radius of a point (horizontal only).
     */
    public static boolean isNearHorizontal(Location loc, LocationData point, double radius) {
        double dx = loc.getX() - point.getX();
        double dz = loc.getZ() - point.getZ();
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    /**
     * Check if a location is within a checkpoint detection radius (3D).
     */
    public static boolean isNearCheckpoint(Location loc, LocationData checkpoint, double radius) {
        double dx = loc.getX() - checkpoint.getX();
        double dy = loc.getY() - checkpoint.getY();
        double dz = loc.getZ() - checkpoint.getZ();
        return (dx * dx + dy * dy + dz * dz) <= (radius * radius);
    }
}
