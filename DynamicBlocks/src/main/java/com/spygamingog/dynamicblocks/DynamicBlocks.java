package com.spygamingog.dynamicblocks;

import com.spygamingog.dynamicblocks.commands.DynamicBlocksCommand;
import com.spygamingog.dynamicblocks.commands.DynamicBlocksTabCompleter;
import com.spygamingog.dynamicblocks.editor.EditorListener;
import com.spygamingog.dynamicblocks.engine.StructureManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DynamicBlocks - Standalone Block and Structure Movement Plugin
 * 
 * Features dynamic, butter-smooth linear and orbital rotating structures
 * using per-block ArmorStand passenger interpolation. Completely decoupled from
 * game/stage orchestration.
 * 
 * @author SpyGamingOG
 */
public class DynamicBlocks extends JavaPlugin {

    private static DynamicBlocks instance;

    private StructureManager structureManager;
    private EditorListener editorListener;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize Managers
        structureManager = new StructureManager(this);
        structureManager.loadAll();
        structureManager.spawnAll(); // Spawns in stopped/reset state initially

        // Register Listeners
        editorListener = new EditorListener(this);
        getServer().getPluginManager().registerEvents(editorListener, this);

        // Register Commands
        DynamicBlocksCommand commandExecutor = new DynamicBlocksCommand(this, structureManager, editorListener);
        DynamicBlocksTabCompleter tabCompleter = new DynamicBlocksTabCompleter(structureManager);

        getCommand("db").setExecutor(commandExecutor);
        getCommand("db").setTabCompleter(tabCompleter);

        // Start scheduler tick task (20Hz)
        structureManager.startScheduler();

        getLogger().info("DynamicBlocks v3 has been enabled! Move blocks smoothly in any direction or rotation.");
    }

    @Override
    public void onDisable() {
        // Stop global structure scheduler
        if (structureManager != null) {
            structureManager.stopScheduler();
            structureManager.despawnAll();
            structureManager.saveAll();
        }
        if (editorListener != null) {
            editorListener.exitAllSessions();
        }

        getLogger().info("DynamicBlocks has been disabled.");
        instance = null;
    }

    public static DynamicBlocks getInstance() { return instance; }
    public StructureManager getStructureManager() { return structureManager; }
    public EditorListener getEditorListener() { return editorListener; }
}
