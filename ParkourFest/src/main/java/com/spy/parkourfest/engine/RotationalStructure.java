package com.spy.parkourfest.engine;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.model.LocationData;
import com.spy.parkourfest.model.MovingStructureData;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Rotational moving structure.
 * Rotates blocks around a center pivot point on the horizontal plane (Y-axis rotation).
 * Creates spinning obstacles, windmill arms, and rotating platforms.
 */
public class RotationalStructure extends MovingStructure {

    private double angle = 0.0; // Current angle in radians

    public RotationalStructure(ParkourFest plugin, MovingStructureData data, World world) {
        super(plugin, data, world);
    }

    @Override
    protected void tick() {
        LocationData center = data.getCenter();
        if (center == null) return;

        double speedDegrees = data.getSpeed();
        double speedRadians = Math.toRadians(speedDegrees);

        // Direction multiplier
        int dirMul = (data.getDirection() == MovingStructureData.Direction.COUNTER_CLOCKWISE
                || data.getDirection() == MovingStructureData.Direction.NEGATIVE) ? -1 : 1;

        angle += speedRadians * dirMul;

        // Keep angle in [0, 2π) range
        if (angle >= Math.PI * 2) angle -= Math.PI * 2;
        if (angle < 0) angle += Math.PI * 2;

        double cx = center.getX();
        double cz = center.getZ();

        for (int i = 0; i < data.getBlocks().size() && i < currentPositions.size(); i++) {
            LocationData blockData = data.getBlocks().get(i);

            // Calculate relative position from center
            double relX = blockData.getX() - cx;
            double relZ = blockData.getZ() - cz;

            // Apply rotation using trigonometry
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double newX = cx + (relX * cos - relZ * sin);
            double newZ = cz + (relX * sin + relZ * cos);

            Location newLoc = new Location(world, newX, blockData.getY(), newZ);

            currentPositions.get(i).setX(newLoc.getX());
            currentPositions.get(i).setY(newLoc.getY());
            currentPositions.get(i).setZ(newLoc.getZ());

            teleportEntities(i, newLoc);
        }
    }
}
