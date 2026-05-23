package com.spy.parkourfest;

import com.spy.parkourfest.commands.ParkourFestCommand;
import com.spy.parkourfest.commands.ParkourFestTabCompleter;
import com.spy.parkourfest.editor.EditorListener;
import com.spy.parkourfest.game.GameListener;
import com.spy.parkourfest.game.GameManager;
import com.spy.parkourfest.stage.StageManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ParkourFest - Ultimate Parkour Mini-Game Plugin
 * 
 * Features dynamic moving platforms inspired by Create Mod and Fall Guys,
 * managed through a nested context-aware hotbar UI system.
 * 
 * @author SpyGamingOG
 */
public class ParkourFest extends JavaPlugin {

    private static ParkourFest instance;

    private StageManager stageManager;
    private GameManager gameManager;
    private EditorListener editorListener;
    private GameListener gameListener;

    @Override
    public void onEnable() {
        instance = this;

        // Create plugin data directories
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize managers
        stageManager = new StageManager(this);
        stageManager.loadAll();

        gameManager = new GameManager(this, stageManager);

        // Register listeners
        editorListener = new EditorListener(this);
        gameListener = new GameListener(this, gameManager);

        getServer().getPluginManager().registerEvents(editorListener, this);
        getServer().getPluginManager().registerEvents(gameListener, this);

        // Register commands
        ParkourFestCommand commandExecutor = new ParkourFestCommand(this, stageManager, gameManager, editorListener);
        ParkourFestTabCompleter tabCompleter = new ParkourFestTabCompleter(stageManager, gameManager);

        getCommand("pf").setExecutor(commandExecutor);
        getCommand("pf").setTabCompleter(tabCompleter);

        getLogger().info("ParkourFest has been enabled! Dynamic parkour awaits.");
    }

    @Override
    public void onDisable() {
        // Stop all active games
        if (gameManager != null) {
            gameManager.stopAll();
        }

        // Exit all editor sessions
        if (editorListener != null) {
            editorListener.exitAllSessions();
        }

        // Save all stages
        if (stageManager != null) {
            stageManager.saveAll();
        }

        getLogger().info("ParkourFest has been disabled. See you next run!");
        instance = null;
    }

    public static ParkourFest getInstance() {
        return instance;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public EditorListener getEditorListener() {
        return editorListener;
    }
}
