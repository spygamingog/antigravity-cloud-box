package com.spygamingog.dynamicblocks.engine;

import com.spygamingog.dynamicblocks.model.LocationData;
import com.spygamingog.dynamicblocks.model.MovingStructureData;
import org.bukkit.Location;

/**
 * Handles rotational orbit and block visual face alignment rotation.
 * Rotates blocks in 3D around an arbitrary pivot axis (X, Y, or Z) and automatically
 * rotates the block visual faces so the entire platform rotates cohesively.
 */
public class RotationalStructure extends MovingStructure {

    public RotationalStructure(MovingStructureData data) {
        super(data);
    }

    @Override
    protected void tickMovement() {
        Location pivot = getCenterLocation();
        if (pivot == null) return;

        double speedDegPerSec = data.getSpeed();
        double speedRadPerTick = Math.toRadians(speedDegPerSec) * 0.05; // 20 ticks per second

        // Direction modifier
        if (data.getDirection() == MovingStructureData.Direction.COUNTER_CLOCKWISE ||
            data.getDirection() == MovingStructureData.Direction.NEGATIVE) {
            speedRadPerTick = -speedRadPerTick;
        }

        String axis = data.getRotationAxis();

        for (ActiveInstance inst : instances) {
            if (!inst.isActive()) continue;

            // Increment rotation angle
            double newAngle = inst.getRotationAngle() + speedRadPerTick;
            // Normalize angle between 0 and 2PI
            newAngle = newAngle % (2 * Math.PI);
            inst.setRotationAngle(newAngle);

            double cos = Math.cos(newAngle);
            double sin = Math.sin(newAngle);

            for (int i = 0; i < data.getBlocks().size(); i++) {
                LocationData bData = data.getBlocks().get(i);

                double newX = bData.getX() + 0.5;
                double newY = bData.getY();
                double newZ = bData.getZ() + 0.5;

                // Perform 2D rotation matrix multiplication depending on active rotation axis
                switch (axis.toUpperCase()) {
                    case "X" -> {
                        // Rotation around X-axis: modifies Y and Z (Y-Z plane)
                        double dy = bData.getY() - pivot.getY();
                        double dz = bData.getZ() - pivot.getZ();
                        newY = pivot.getY() + (dy * cos - dz * sin);
                        newZ = (pivot.getZ() + 0.5) + (dy * sin + dz * cos);
                    }
                    case "Z" -> {
                        // Rotation around Z-axis: modifies X and Y (X-Y plane)
                        double dx = bData.getX() - pivot.getX();
                        double dy = bData.getY() - pivot.getY();
                        newX = (pivot.getX() + 0.5) + (dx * cos - dy * sin);
                        newY = pivot.getY() + (dx * sin + dy * cos);
                    }
                    default -> {
                        // Rotation around Y-axis: modifies X and Z (X-Z plane) (Default)
                        double dx = bData.getX() - pivot.getX();
                        double dz = bData.getZ() - pivot.getZ();
                        newX = (pivot.getX() + 0.5) + (dx * cos - dz * sin);
                        newZ = (pivot.getZ() + 0.5) + (dx * sin + dz * cos);
                    }
                }

                Location target = new Location(world, newX, newY, newZ);
                inst.getCurrentPositions().set(i, target.clone());

                // Calculate yaw for ArmorStand (only relevant for Y-axis rotation to align collision)
                float yaw = 0.0f;
                if ("Y".equalsIgnoreCase(axis)) {
                    yaw = (float) Math.toDegrees(-newAngle);
                }

                // Teleport the vehicle ArmorStand
                inst.teleportVehicle(i, target, yaw);

                // Update the BlockDisplay's passenger visual transformation to rotate its faces!
                if (!"Y".equalsIgnoreCase(axis)) {
                    inst.updateBlockDisplayRotation(i, newAngle, axis);
                } else {
                    inst.resetBlockDisplayRotation(i);
                }
            }
        }
    }

    /**
     * Get pivot center location in Bukkit representation.
     */
    private Location getCenterLocation() {
        if (data.getCenter() != null) {
            return data.getCenter().toBukkit(world);
        }
        if (!data.getBlocks().isEmpty()) {
            return data.getBlocks().get(0).toBukkit(world);
        }
        return currentOrigin;
    }
}
