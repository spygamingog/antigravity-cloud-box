package com.spy.spymotion;

import com.spy.spymotion.commands.SpyMotionCommand;
import com.spy.spymotion.commands.SpyMotionTabCompleter;
import com.spy.spymotion.editor.EditorListener;
import com.spy.spymotion.engine.StructureManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SpyMotion - Standalone Block and Structure Movement Plugin
 * 
 * Features dynamic, butter-smooth linear and orbital rotating structures
 * using per-block ArmorStand passenger interpolation. Completely decoupled from
 * game/stage orchestration.
 * 
 * @author SpyGamingOG
 */
public class SpyMotion extends JavaPlugin {

    private static SpyMotion instance;

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
        SpyMotionCommand commandExecutor = new SpyMotionCommand(this, structureManager, editorListener);
        SpyMotionTabCompleter tabCompleter = new SpyMotionTabCompleter(structureManager);

        getCommand("sm").setExecutor(commandExecutor);
        getCommand("sm").setTabCompleter(tabCompleter);

        // Start scheduler tick task (20Hz)
        structureManager.startScheduler();

        getLogger().info("SpyMotion v3 has been enabled! Move blocks smoothly in any direction or rotation.");
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

        getLogger().info("SpyMotion has been disabled.");
        instance = null;
    }

    public static SpyMotion getInstance() { return instance; }
    public StructureManager getStructureManager() { return structureManager; }
    public EditorListener getEditorListener() { return editorListener; }
}
