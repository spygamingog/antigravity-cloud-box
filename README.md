# 🎡 SpyMotion — Standalone Moving Blocks & Structures Plugin

**SpyMotion** is a standalone, production-grade Minecraft Paper 1.21 plugin that allows you to easily select blocks in the world and animate them as moving, path-following, or rotating structures.

Built using per-block invisible Marker ArmorStands as vehicles, SpyMotion hosts visual `BlockDisplay` passengers and physical `Shulker` collision passengers. This setup leverages Minecraft's client-side passenger interpolation for butter-smooth rendering. All blocks maintain full axis-aligned solid collision boxes, allowing players complete free will to walk, jump, and interact naturally on moving platforms without clipping or sliding.

---

## ✨ Features

### 1. Butter-Smooth passenger Interpolation
* **Dual-Passenger Vehicle System**: Spawns one invisible Marker ArmorStand vehicle per block.
* **Seamless client-side Rendering**: Visual `BlockDisplay` and physical `Shulker` ride the ArmorStand. Teleporting the vehicles causes Minecraft to interpolate passenger movement client-side, eliminating jerky movement.
* **Grid-Locked Alignment**: All vehicles are centered on the X and Z axes at block centers `(X + 0.5, Y, Z + 0.5)` with direct Y integer block height. Visual displays are translated by `(-0.5, 0.0, -0.5)` relative to the vehicle, locking both visuals and solid `1.0 × 1.0 × 1.0` AABB collision boxes perfectly to integer block coordinates.

### 2. Cohesive 3D Multi-Axis Rotation
* **Arbitrary Axis Spin**: Orbit blocks around a custom pivot center across the **Y-axis** (horizontal spinner), **X-axis** (pitch spinner), or **Z-axis** (roll spinner).
* **Display Face Rotation**: Rotates the visual faces of displays in local space using custom JOML Quaternion matrices, matching the orbit angle. The platform spins as a single connected rigid body.
* **Automatic Y-Axis Yaw**: For Y-axis spinners, the block displays inherit the vehicle ArmorStand's native yaw, completely avoiding visual desync or double-rotation.

### 3. Smart Linear Pathing & Reversal Loop
* **Multi-Node Paths**: Define a chain of nodes (intermediate Path Points, and End Point) for platforms to navigate relative to its start center block reference.
* **Segment-Specific Speeds**: Travels to each node using its own unique speed setting.
* **Node-Specific Delay Pauses**: Stays at each node for a dedicated delay duration.
* **Drift & Jitter Prevention**: Locks coordinate alignment precisely during delays, completely eliminating visual jittering or desync.
* **Reversal loop Modes**: Supports both standard **Ping-Pong** (runs back and forth in reverse order) and **Restart Loop** (teleports instantly back to start node upon reaching the end point).

### 4. Interactive visual Sub-Editor Tools
* **Dynamic Block Selection Wand** (*Glowstone Dust*): Visual left/right-click blocks to add/remove them. While editing an existing structure, changes update the active spawned entities **live in the world in real-time**!
* **Automated Block Cleanup**: Once a structure is created, the original physical blocks placed by the user are automatically removed (replaced with AIR) so they don't overlap with the spawned moving visual/collision entities.
* **Contextual Hotbars**: Transition into dedicated, visual page editors:
  * **Linear Platform Editor**: Includes *Add Path Point* and *Set End Point* tools that set travel nodes relative to the platform's reference pivot center block.
  * **Rotator Editor**: Includes a *Set Rotation Center* compass tool to easily set the pivot center.
* **Instant Edit Menus**: Creating a structure instantly launches a clickable, interactive chat panel for granular adjustments.

### 5. Lifecycle Management
* **Zero entity Leakage**: All spawned ArmorStands, displays, and Shulkers are non-persistent. They cleanly despawn on server restarts, stops, or reloads.
* **Startup Initialization**: Restarts load configurations from `plugins/SpyMotion/structures.json` and spawn all platforms halted at their home/reset locations.

---

## 📋 Command Reference

All commands require the `spymotion.admin` permission. Most commands are runnable from the console.

### Bulk Structure Controls
| Command | Console | Description |
|---------|---------|-------------|
| `/sm structures spawn` | ✅ | Spawn all structures in the world |
| `/sm structures despawn` | ✅ | Despawn all structures |
| `/sm structures start` | ✅ | Start movement of all structures |
| `/sm structures stop` | ✅ | Stop movement of all structures |
| `/sm structures reset` | ✅ | Reset all structures to home positions |

### Individual Structure Controls
| Command | Console | Description |
|---------|---------|-------------|
| `/sm tools` | ❌ | Open the visual hotbar editor wands (player only) |
| `/sm reload` | ✅ | Reload configurations and respawn structures |
| `/sm structure list` | ✅ | List all structures with clickable Edit/TP links |
| `/sm structure edit <id>` | ✅ | Open the interactive chat edit menu |
| `/sm structure info <id>` | ✅ | Alias for structure edit |
| `/sm structure start <id>` | ✅ | Start this structure |
| `/sm structure stop <id>` | ✅ | Stop this structure |
| `/sm structure reset <id>` | ✅ | Reset this structure to its home position |
| `/sm structure spawn <id>` | ✅ | Spawn entities for this structure |
| `/sm structure despawn <id>` | ✅ | Despawn entities for this structure |
| `/sm structure tp <id>` | ❌ | Teleport player to structure center (player only) |
| `/sm structure remove <id>` | ✅ | Delete structure configuration and despawn |
| `/sm structure rename <id> <name>` | ✅ | Rename structure display name |
| `/sm structure setmaterial <id> <mat>` | ✅ | Set solid block material |
| `/sm structure setspeed <id> <speed>` | ✅ | Set speed (deg/s or blocks/s) |
| `/sm structure setrotationaxis <id> <X\|Y\|Z>` | ✅ | Set rotator rotation axis |
| `/sm structure setcenter <id>` | ❌ | Set rotator pivot to player location |
| `/sm structure setstartpoint <id>` | ❌ | Set linear reference start point to player location |
| `/sm structure setendpoint <id>` | ❌ | Set linear end point to player location |
| `/sm structure setendspeed <id> <speed>` | ✅ | Set travel speed to end point |
| `/sm structure setenddelay <id> <secs>` | ✅ | Set wait delay at end point |
| `/sm structure setstartdelay <id> <secs>` | ✅ | Set wait delay at start point |
| `/sm structure setloop <id> <true\|false>` | ✅ | Toggle linear looping (true = restart, false = ping-pong) |
| `/sm structure setcyclecount <id> <count>` | ✅ | Set concurrent active cycle count |
| `/sm structure setcycledelay <id> <secs>` | ✅ | Set delay between spawns of cycle instances |
| `/sm structure addpoint <id>` | ❌ | Add an intermediate path node at player location |
| `/sm structure removepoint <id> <index>` | ✅ | Remove intermediate path node |
| `/sm structure setpointspeed <id> <index> <speed>` | ✅ | Set travel speed to specific intermediate node |
| `/sm structure setpointdelay <id> <index> <delay>` | ✅ | Set wait delay at specific intermediate node |

---

## 🛠️ Step-by-Step Creation & Configuration Guide

### 1. Creating a Path-based Linear Elevator
1. Build a `3×3` platform of solid blocks in the world.
2. Run `/sm tools` to load the creator hotbar wands.
3. Select the **Structure Wand** (slot 1) and click all 9 blocks to select them.
4. Scroll to slot 3 (**Create Linear Platform**) and click it. 
5. The original blocks are cleared, the elevator instantly spawns, your hotbar switches to the **Linear Editor Page**, and the chat config panel opens. (The first block in your selection automatically serves as the reference center block/pivot).
6. Fly to where you want the elevator to end, and right-click slot 3 (**Set End Point**). The center block of the platform will align perfectly with your position at the end.
7. Fly to any intermediate stop and right-click slot 2 (**Add Path Point**) to create a middle landing!
8. In the chat menu, click the **Start Delay** and **End Delay** buttons to set a `3.0s` wait.
9. Click **[START]** in chat. Your elevator will glide smoothly up and down, pausing at each stop!

### 2. Creating a Rotating Spinner
1. Build a spinner arm of blocks (e.g. 5 blocks in a straight line).
2. Enter `/sm tools` and click all blocks using the wand.
3. Stand at the exact center pivot of the arm.
4. Scroll to slot 4 (**Create Rotator**) and click it.
5. The original blocks are cleared, the rotator spawns, your hotbar switches to the **Rotator Editor Page**, and the chat config panel opens.
6. Right-click slot 3 (**Set Rotation Center**) to set the pivot point where you are standing.
7. In the chat menu, set rotation speed to `45.0 deg/s`.
8. Click **[START]** in chat. The spinner will spin smoothly around your pivot!
9. *Optional*: In chat, under **Rotation Axis**, click **Cycle** to change the axis to `X-axis` or `Z-axis` to rotate vertically! Watch the visual blocks rotate on their faces alongside the orbit.

---

## 🧪 Compilation & Installation

Compile the plugin using Maven:
```bash
mvn clean package
```
* **Output JAR**: `ParkourFest/target/SpyMotion-1.0-SNAPSHOT.jar`
* **Installation**: Place the compiled JAR into your server's `plugins/` directory and start/restart the server.
