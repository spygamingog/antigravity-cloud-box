package com.spygamingog.dynamicblocks.model;

/**
 * Represents an intermediate node along a linear structure's movement path.
 * Speed represents the speed (blocks/second) to TRAVEL TO this point from the previous point.
 * Delay represents the PAUSE TIME (seconds) to wait at this point before departing to the next.
 */
public class PathPoint {

    private LocationData location;
    private double speed = 2.0;   // Travel speed to this node (blocks/second)
    private double delay = 0.0;   // Pause delay at this node (seconds)

    public PathPoint() {}

    public PathPoint(LocationData location, double speed, double delay) {
        this.location = location;
        this.speed = speed;
        this.delay = delay;
    }

    public LocationData getLocation() { return location; }
    public void setLocation(LocationData location) { this.location = location; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public double getDelay() { return delay; }
    public void setDelay(double delay) { this.delay = delay; }
}
