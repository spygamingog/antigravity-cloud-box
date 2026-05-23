# ParkourFest Plugin — Walkthrough

## Overview

**ParkourFest** is a fully-featured Minecraft Paper 1.21 parkour mini-game plugin featuring dynamic moving platforms inspired by Create Mod and Fall Guys. The plugin was built as a single Maven project targeting Java 21 and Paper API `1.21.4-R0.1-SNAPSHOT`.

**Built JAR:** `/workspaces/antigravity-cloud-box/ParkourFest/target/ParkourFest-1.0-SNAPSHOT.jar`

---

## Architecture

```
27 Java files across 7 packages:
├── model/      — 5 files (POJOs: LocationData, RegionData, CheckpointData, MovingStructureData, StageData)
├── stage/      — 2 files (StageManager, StageSession)
├── engine/     — 5 files (MovingStructure, LinearStructure, RotationalStructure, StructureEngine, PlatformPhysics)
├── editor/     — 3 files (EditorSession, EditorListener, ChatWizard)
├── editor/pages/ — 5 files (HotbarPage, MainMenuPage, MechanicsWizardPage, LinearConfigPage, RotationConfigPage)
├── game/       — 3 files (GameState, GameManager, GameListener)
├── commands/   — 2 files (ParkourFestCommand, ParkourFestTabCompleter)
├── util/       — 3 files (MessageUtil, RegionUtil, ItemBuilder)
└── ParkourFest.java — Main plugin class
```

---

## Key Systems Implemented

### 1. Stage Persistence (JSON)
- Each stage is saved as its own JSON file in `plugins/ParkourFest/stages/<name>.json`
- Uses Gson with pretty-printing for human-readable configs
- Full CRUD operations via StageManager

### 2. Nested Hotbar Editor (4 Pages)
| Page | Name | Purpose |
|------|------|---------|
| 1 | Main Setup Menu | Region Wand, Region Definer, Checkpoint Tool, Dynamic Mechanisms, Chat Tweaks, Save & Exit |
| 2 | Mechanics Wizard | Linear Mover, Rotator, Structure Wand (block selection), Test Controls |
| 3 | Linear Config | Axis selector (X/Y/Z), Speed ±, Range ±, Confirm |
| 4 | Rotation Config | Center Anchor, CW/CCW direction, RPM ±, Confirm |

### 3. Moving Platform Engine
- **BlockDisplay entities** for visual rendering (smooth client-side interpolation)
- **Invisible Shulker entities** for collision/solidity
- **LinearStructure**: Ping-pong movement along X, Y, or Z axis
- **RotationalStructure**: Trigonometric rotation around a pivot point
- **PlatformPhysics**: Velocity injection — when a player stands on a moving platform, the platform's velocity is applied to the player

### 4. Game Loop
1. `/pf start <stage>` → Validates stage, collects participants, teleports to start
2. Countdown phase: Players frozen, titles count down with sound effects
3. Active phase: Structures start moving, physics engine activates
4. Checkpoint tracking: `PlayerMoveEvent` detects proximity to checkpoints
5. Finish detection: First N players to enter the finish region win
6. Elimination: Remaining players are eliminated when completion limit is reached
7. Void fall / lava: Instant respawn at last checkpoint with teleport sound

### 5. Interactive Chat Wizard
- Triggered from hotbar Page 1, Slot 4 (Book)
- Steps through: Player Limit → Countdown Seconds → Completion Limit → PVP Toggle
- Each step has a 20-second timeout
- Supports "skip" to keep current values

### 6. Command System
All commands use `/pf` with full tab completion:

| Command | Description |
|---------|-------------|
| `/pf create <name>` | Create a new stage |
| `/pf delete <name>` | Delete a stage |
| `/pf tools <name>` | Open editor tools (nested hotbar) |
| `/pf enable <name>` | Enable a stage |
| `/pf disable <name>` | Disable a stage |
| `/pf start <name>` | Start a game |
| `/pf stop <name>` | Stop a game |
| `/pf pvp <on/off> <name>` | Toggle PVP |
| `/pf structures <start/stop/reset> <name>` | Control moving structures |
| `/pf list` | List all stages |
| `/pf info <name>` | Show stage details |

---

## Verification

- ✅ **Compilation**: `mvn clean package` — **BUILD SUCCESS**
- ✅ **27 source files** compile cleanly with no errors
- ✅ **Plugin descriptor** (`plugin.yml`) is valid

> [!NOTE]
> Full functional testing requires deploying `ParkourFest-1.0-SNAPSHOT.jar` to a Paper 1.21.4 server.

---

## Deployment Instructions

1. Copy `target/ParkourFest-1.0-SNAPSHOT.jar` to your server's `plugins/` folder
2. Start or restart the Paper server
3. Run `/pf create my_stage` to create your first stage
4. Use `/pf tools my_stage` to open the editor and configure regions, checkpoints, and moving structures
5. Run `/pf enable my_stage` then `/pf start my_stage` to begin a game!
