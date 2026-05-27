package com.spy.spymotion.engine;

import com.spy.spymotion.model.LocationData;
import com.spy.spymotion.model.MovingStructureData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single active physical and visual instance of a moving structure.
 * Spawns one ArmorStand per block, with BlockDisplay and Shulker riding as passengers.
 * Client-side interpolates ArmorStand movement for maximum smoothness.
 * Custom JOML Transformations are applied to rotate block display faces in sync with rotation.
 */
public class ActiveInstance {

    private final MovingStructure parent;
    private final World world;
    private final MovingStructureData data;

    // Entities (one per block)
    private final List<ArmorStand> armorStands = new ArrayList<>();
    private final List<BlockDisplay> displayEntities = new ArrayList<>();
    private final List<Shulker> collisionEntities = new ArrayList<>();

    // Tracks exact current and previous world positions for each block
    private final List<Location> currentPositions = new ArrayList<>();
    private final List<Location> previousPositions = new ArrayList<>();

    // State variables
    private boolean active = false;
    private double spawnDelayRemaining = 0.0;

    // Movement state (for Path platform)
    private int currentSegmentIndex = 0;
    private double segmentProgress = 0.0;
    private double pauseTimerSeconds = 0.0;
    private boolean movingForward = true;
    private int pauseAtIndex = 0; // Tracks node index where the platform is currently paused

    // Movement state (for Rotational platform)
    private double rotationAngle = 0.0; // in radians

    public ActiveInstance(MovingStructure parent, double spawnDelayRemaining) {
        this.parent = parent;
        this.world = parent.getWorld();
        this.data = parent.getData();
        this.spawnDelayRemaining = spawnDelayRemaining;
    }

    /**
     * Spawn ArmorStands, BlockDisplays, and Shulkers for this instance.
     * All entities are properly nested and aligned.
     */
    public void spawn(Location startRefLocation) {
        if (active) return;
        active = true;

        Material material;
        try {
            material = Material.valueOf(data.getBlockMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }

        LocationData startPt = data.getStartPoint();
        if (startPt == null) {
            startPt = data.getBlocks().isEmpty() ? new LocationData(0, 0, 0) : data.getBlocks().get(0);
        }

        for (LocationData blockData : data.getBlocks()) {
            double dx = blockData.getX() - startPt.getX();
            double dy = blockData.getY() - startPt.getY();
            double dz = blockData.getZ() - startPt.getZ();

            Location loc = startRefLocation.clone().add(dx + 0.5, dy, dz + 0.5);
            currentPositions.add(loc.clone());
            previousPositions.add(loc.clone());

            // 1. Spawn invisible Marker ArmorStand
            ArmorStand armorStand = world.spawn(loc, ArmorStand.class, entity -> {
                entity.setInvisible(true);
                entity.setMarker(true);
                entity.setGravity(false);
                entity.setPersistent(false);
                entity.setAI(false);
                entity.setSilent(true);
                entity.setInvulnerable(true);
            });
            armorStands.add(armorStand);

            // 2. Spawn BlockDisplay (Visual)
            final Material displayMat = material;
            BlockDisplay display = world.spawn(loc, BlockDisplay.class, entity -> {
                entity.setBlock(displayMat.createBlockData());
                entity.setPersistent(false);
                entity.setGravity(false);
                
                // Align BlockDisplay corner-origin so visual centers perfectly on the ArmorStand (which is centered)
                Transformation transformation = entity.getTransformation();
                transformation.getTranslation().set(-0.5f, 0.0f, -0.5f);
                entity.setTransformation(transformation);
            });
            displayEntities.add(display);

            // 3. Spawn invisible Shulker (Collision)
            Shulker shulker = world.spawn(loc, Shulker.class, entity -> {
                entity.setAI(false);
                entity.setInvisible(true);
                entity.setSilent(true);
                entity.setInvulnerable(true);
                entity.setPersistent(false);
                entity.setGravity(false);
                entity.setCollidable(true);
            });
            collisionEntities.add(shulker);

            // 4. Ride vehicle
            armorStand.addPassenger(display);
            armorStand.addPassenger(shulker);
        }
    }

    /**
     * Remove all spawned entities for this instance.
     */
    public void despawn() {
        active = false;
        displayEntities.forEach(Entity::remove);
        collisionEntities.forEach(Entity::remove);
        armorStands.forEach(Entity::remove);
        displayEntities.clear();
        collisionEntities.clear();
        armorStands.clear();
        currentPositions.clear();
        previousPositions.clear();
    }

    /**
     * Teleport the ArmorStand vehicle for a block. The passenger block display
     * and shulker will move dynamically with it.
     */
    public void teleportVehicle(int index, Location newLoc, float yaw) {
        if (index < armorStands.size()) {
            ArmorStand armorStand = armorStands.get(index);
            if (armorStand != null && armorStand.isValid()) {
                armorStand.teleport(newLoc);
                armorStand.setRotation(yaw, 0.0f);
            }
        }
    }

    /**
     * Update the visual block display rotation for a specific block using local transformation.
     */
    public void updateBlockDisplayRotation(int index, double angle, String axis) {
        if (index >= displayEntities.size()) return;
        BlockDisplay display = displayEntities.get(index);
        if (display == null || !display.isValid()) return;

        // Compute rotation quaternion
        Quaternionf q = new Quaternionf();
        switch (axis.toUpperCase()) {
            case "X" -> q.rotationX((float) angle);
            case "Z" -> q.rotationZ((float) angle);
            default -> q.rotationY((float) angle);
        }

        // Apply local transformation to rotate around block center while keeping centered
        // t = t_post + q * t_pre where t_post is (0.0, 0.5, 0.0) and t_pre is (-0.5, -0.5, -0.5)
        Vector3f t_pre = new Vector3f(-0.5f, -0.5f, -0.5f);
        Vector3f t_post = new Vector3f(0.0f, 0.5f, 0.0f);
        
        Vector3f rotated = new Vector3f();
        q.transform(t_pre, rotated);
        Vector3f finalTranslation = t_post.add(rotated);

        Transformation trans = display.getTransformation();
        trans.getTranslation().set(finalTranslation.x, finalTranslation.y, finalTranslation.z);
        trans.getLeftRotation().set(q.x, q.y, q.z, q.w);
        display.setTransformation(trans);
    }

    /**
     * Reset the visual block display rotation to default.
     */
    public void resetBlockDisplayRotation(int index) {
        if (index >= displayEntities.size()) return;
        BlockDisplay display = displayEntities.get(index);
        if (display == null || !display.isValid()) return;

        Transformation trans = display.getTransformation();
        trans.getTranslation().set(-0.5f, 0.0f, -0.5f);
        trans.getLeftRotation().set(0f, 0f, 0f, 1f);
        display.setTransformation(trans);
    }

    // --- Getters & Setters ---

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getSpawnDelayRemaining() { return spawnDelayRemaining; }
    public void setSpawnDelayRemaining(double spawnDelayRemaining) { this.spawnDelayRemaining = spawnDelayRemaining; }

    public int getCurrentSegmentIndex() { return currentSegmentIndex; }
    public void setCurrentSegmentIndex(int currentSegmentIndex) { this.currentSegmentIndex = currentSegmentIndex; }

    public double getSegmentProgress() { return segmentProgress; }
    public void setSegmentProgress(double segmentProgress) { this.segmentProgress = segmentProgress; }

    public double getPauseTimerSeconds() { return pauseTimerSeconds; }
    public void setPauseTimerSeconds(double pauseTimerSeconds) { this.pauseTimerSeconds = pauseTimerSeconds; }

    public boolean isMovingForward() { return movingForward; }
    public void setMovingForward(boolean movingForward) { this.movingForward = movingForward; }

    public int getPauseAtIndex() { return pauseAtIndex; }
    public void setPauseAtIndex(int pauseAtIndex) { this.pauseAtIndex = pauseAtIndex; }

    public double getRotationAngle() { return rotationAngle; }
    public void setRotationAngle(double rotationAngle) { this.rotationAngle = rotationAngle; }

    public List<Location> getCurrentPositions() { return currentPositions; }
    public List<Location> getPreviousPositions() { return previousPositions; }
    public List<Shulker> getCollisionEntities() { return collisionEntities; }
    public List<ArmorStand> getArmorStands() { return armorStands; }
    public List<BlockDisplay> getDisplayEntities() { return displayEntities; }
}
