package com.spy.spymotion.engine;

import com.spy.spymotion.model.LocationData;
import com.spy.spymotion.model.MovingStructureData;
import com.spy.spymotion.model.PathPoint;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles linear movement along a path of nodes.
 * Supports multiple concurrent cycle instances, accurate pause delays,
 * and precise segment-based speed controls.
 */
public class LinearStructure extends MovingStructure {

    public LinearStructure(MovingStructureData data) {
        super(data);
    }

    @Override
    public void start() {
        if (running) return;
        running = true;

        despawn();

        // Spawn all concurrent cycle instances with their starting delays
        int count = data.getCycleCount();
        if (count < 1) count = 1;

        for (int c = 0; c < count; c++) {
            double startDelay = c * data.getCycleDelay();
            ActiveInstance inst = new ActiveInstance(this, startDelay);
            inst.spawn(currentOrigin);
            
            // Set initial state to paused at start node
            inst.setCurrentSegmentIndex(0);
            inst.setSegmentProgress(0.0);
            inst.setPauseAtIndex(0);
            // The spawn delay runs FIRST, then the start point delay will run on activation
            inst.setPauseTimerSeconds(0.0); 
            inst.setMovingForward(true);

            instances.add(inst);
        }
    }

    @Override
    protected void tickMovement() {
        List<Location> path = getPathNodes();
        if (path.size() < 2) return;

        int M = path.size();

        for (ActiveInstance inst : instances) {
            if (!inst.isActive()) continue;

            // 1. Handle Spawn Delay
            if (inst.getSpawnDelayRemaining() > 0) {
                inst.setSpawnDelayRemaining(inst.getSpawnDelayRemaining() - 0.05);
                teleportToOrigin(inst, path.get(0));
                
                // If spawn delay just ended, trigger the initial start point delay
                if (inst.getSpawnDelayRemaining() <= 0) {
                    inst.setPauseAtIndex(0);
                    inst.setPauseTimerSeconds(getDelayAt(0));
                }
                continue;
            }

            // 2. Handle Pause / Node Delay
            if (inst.getPauseTimerSeconds() > 0) {
                inst.setPauseTimerSeconds(inst.getPauseTimerSeconds() - 0.05);

                // Pause ended, transition to moving
                if (inst.getPauseTimerSeconds() <= 0) {
                    int currPause = inst.getPauseAtIndex();
                    if (currPause == M - 1) { // Arrived at End Point
                        if (data.isLoop()) {
                            // Restart Loop: instantly teleport to start and start the pause
                            teleportToOrigin(inst, path.get(0));
                            inst.setPauseAtIndex(0);
                            inst.setPauseTimerSeconds(getDelayAt(0));
                            inst.setCurrentSegmentIndex(0);
                            inst.setSegmentProgress(0.0);
                            inst.setMovingForward(true);
                        } else {
                            // Ping-Pong: start moving reverse from M-1 to M-2
                            inst.setMovingForward(false);
                            inst.setCurrentSegmentIndex(M - 2);
                            inst.setSegmentProgress(0.0);
                        }
                    } else if (currPause == 0) { // Arrived at Start Point
                        // Start moving forward from 0 to 1
                        inst.setMovingForward(true);
                        inst.setCurrentSegmentIndex(0);
                        inst.setSegmentProgress(0.0);
                    } else { // Intermediate node pause ended
                        if (inst.isMovingForward()) {
                            inst.setCurrentSegmentIndex(currPause);
                            inst.setSegmentProgress(0.0);
                        } else {
                            inst.setCurrentSegmentIndex(currPause - 1);
                            inst.setSegmentProgress(0.0);
                        }
                    }
                }
                continue;
            }

            // 3. Handle Active Segment Movement
            int idx = inst.getCurrentSegmentIndex();
            if (idx < 0 || idx >= M - 1) continue;

            Location startNode = path.get(idx);
            Location endNode = path.get(idx + 1);

            double speed = inst.isMovingForward() ? getSpeedAt(idx + 1) : getSpeedAt(idx);
            if (speed <= 0.01) speed = 2.0; // Fail-safe speed

            double distance = startNode.distance(endNode);
            if (distance <= 0.001) {
                inst.setSegmentProgress(1.0);
            } else {
                double duration = distance / speed; // in seconds
                double increment = 0.05 / duration; // tick is 0.05s
                inst.setSegmentProgress(inst.getSegmentProgress() + increment);
            }

            // Interpolate position along segment
            double progress = inst.getSegmentProgress();
            if (progress > 1.0) progress = 1.0;

            double x, y, z;
            if (inst.isMovingForward()) {
                x = startNode.getX() + (endNode.getX() - startNode.getX()) * progress;
                y = startNode.getY() + (endNode.getY() - startNode.getY()) * progress;
                z = startNode.getZ() + (endNode.getZ() - startNode.getZ()) * progress;
            } else {
                x = endNode.getX() + (startNode.getX() - endNode.getX()) * progress;
                y = endNode.getY() + (startNode.getY() - endNode.getY()) * progress;
                z = endNode.getZ() + (startNode.getZ() - endNode.getZ()) * progress;
            }

            Location currentLoc = new Location(world, x, y, z);
            teleportToOrigin(inst, currentLoc);

            // Segment complete, trigger node arrival
            if (inst.getSegmentProgress() >= 1.0) {
                int arrivedNode = inst.isMovingForward() ? idx + 1 : idx;
                inst.setPauseAtIndex(arrivedNode);
                inst.setPauseTimerSeconds(getDelayAt(arrivedNode));
                teleportToOrigin(inst, path.get(arrivedNode));
            }
        }
    }

    /**
     * Instantly teleports all blocks in an instance to the given reference origin.
     */
    private void teleportToOrigin(ActiveInstance inst, Location refLoc) {
        LocationData startPt = data.getStartPoint();
        if (startPt == null) {
            startPt = data.getBlocks().isEmpty() ? new LocationData(0, 0, 0) : data.getBlocks().get(0);
        }

        for (int i = 0; i < data.getBlocks().size(); i++) {
            LocationData blockData = data.getBlocks().get(i);
            double dx = blockData.getX() - startPt.getX();
            double dy = blockData.getY() - startPt.getY();
            double dz = blockData.getZ() - startPt.getZ();

            Location target = refLoc.clone().add(dx + 0.5, dy, dz + 0.5);
            inst.getCurrentPositions().set(i, target.clone());
            inst.teleportVehicle(i, target, 0.0f);
        }
    }

    /**
     * Construct list of all path nodes.
     */
    private List<Location> getPathNodes() {
        List<Location> path = new ArrayList<>();
        if (data.getStartPoint() != null) {
            path.add(data.getStartPoint().toBukkit(world));
        }
        for (PathPoint pt : data.getPathPoints()) {
            if (pt.getLocation() != null) {
                path.add(pt.getLocation().toBukkit(world));
            }
        }
        if (data.getEndPoint() != null) {
            path.add(data.getEndPoint().toBukkit(world));
        }
        return path;
    }

    /**
     * Get delay in seconds at a specific node index.
     */
    private double getDelayAt(int index) {
        int M = getPathNodes().size();
        if (index == 0) return data.getStartPointDelay();
        if (index == M - 1) return data.getEndPointDelay();
        if (index > 0 && index - 1 < data.getPathPoints().size()) {
            return data.getPathPoints().get(index - 1).getDelay();
        }
        return 0.0;
    }

    /**
     * Get speed in blocks/second to travel to a specific node index.
     */
    private double getSpeedAt(int index) {
        int M = getPathNodes().size();
        if (index == M - 1) return data.getEndPointSpeed();
        if (index > 0 && index - 1 < data.getPathPoints().size()) {
            return data.getPathPoints().get(index - 1).getSpeed();
        }
        // Fallback speed for start point travel
        if (index == 0) {
            return data.getPathPoints().isEmpty() ? data.getEndPointSpeed() : data.getPathPoints().get(0).getSpeed();
        }
        return 2.0;
    }
}
