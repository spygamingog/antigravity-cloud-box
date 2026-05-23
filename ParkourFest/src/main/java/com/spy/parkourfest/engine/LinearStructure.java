package com.spy.parkourfest.engine;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.model.MovingStructureData;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Linear (ping-pong) moving structure.
 * Moves blocks along a single axis (X, Y, or Z) and reverses at the range limits.
 * Creates sweeper arms, sliding platforms, and elevators.
 */
public class LinearStructure extends MovingStructure {

    private double offset = 0.0;
    private int directionMultiplier = 1;

    public LinearStructure(ParkourFest plugin, MovingStructureData data, World world) {
        super(plugin, data, world);
    }

    @Override
    protected void tick() {
        double speed = data.getSpeed();
        double range = data.getRange();

        // Update offset
        offset += speed * directionMultiplier;

        // Ping-pong: reverse direction at range boundaries
        if (offset >= range) {
            offset = range;
            directionMultiplier = -1;
        } else if (offset <= 0) {
            offset = 0;
            directionMultiplier = 1;
        }

        // Move each block
        String axis = data.getAxis();
        for (int i = 0; i < data.getBlocks().size() && i < currentPositions.size(); i++) {
            Location baseLoc = data.getBlocks().get(i).toBukkit(world);
            Location newLoc = baseLoc.clone();

            switch (axis) {
                case "X" -> newLoc.setX(baseLoc.getX() + offset);
                case "Y" -> newLoc.setY(baseLoc.getY() + offset);
                case "Z" -> newLoc.setZ(baseLoc.getZ() + offset);
            }

            currentPositions.get(i).setX(newLoc.getX());
            currentPositions.get(i).setY(newLoc.getY());
            currentPositions.get(i).setZ(newLoc.getZ());

            teleportEntities(i, newLoc);
        }
    }
}
