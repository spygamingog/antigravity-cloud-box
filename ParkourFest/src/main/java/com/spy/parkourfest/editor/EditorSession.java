package com.spy.parkourfest.editor;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.editor.pages.*;
import com.spy.parkourfest.model.LocationData;
import com.spy.parkourfest.model.MovingStructureData;
import com.spy.parkourfest.model.StageData;
import com.spy.parkourfest.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an active editor session for a player configuring a stage.
 * Holds all working state including page navigation, block selections,
 * and structure configuration parameters.
 */
public class EditorSession {

    private final ParkourFest plugin;
    private final UUID playerId;
    private final String stageName;
    private final StageData stageData;

    // Saved original inventory
    private ItemStack[] savedInventory;
    private ItemStack[] savedArmor;

    // Current hotbar page
    private HotbarPage currentPage;

    // Pre-built page instances
    private final MainMenuPage mainMenuPage;
    private final MechanicsWizardPage mechanicsWizardPage;
    private final LinearConfigPage linearConfigPage;
    private final RotationConfigPage rotationConfigPage;

    // --- Region selection state ---
    private LocationData pos1Selection;
    private LocationData pos2Selection;

    // --- Structure wand selection state ---
    private List<LocationData> selectedBlocks = new ArrayList<>();

    // --- Working structure configuration (used during creation) ---
    private String workingAxis = "X";
    private double workingSpeed = 0.5;
    private double workingRange = 5.0;
    private MovingStructureData.Direction workingDirection = MovingStructureData.Direction.POSITIVE;
    private LocationData workingCenter;

    // --- Chat wizard state ---
    private ChatWizard activeChatWizard;

    public EditorSession(ParkourFest plugin, Player player, StageData stageData) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
        this.stageName = stageData.getStageName();
        this.stageData = stageData;

        // Save original inventory
        this.savedInventory = player.getInventory().getContents().clone();
        this.savedArmor = player.getInventory().getArmorContents().clone();

        // Create all pages
        this.mainMenuPage = new MainMenuPage(this);
        this.mechanicsWizardPage = new MechanicsWizardPage(this);
        this.linearConfigPage = new LinearConfigPage(this);
        this.rotationConfigPage = new RotationConfigPage(this);

        // Start on main menu
        switchPage(mainMenuPage, player);
    }

    /**
     * Switch to a different hotbar page.
     */
    public void switchPage(HotbarPage page, Player player) {
        this.currentPage = page;
        player.getInventory().clear();
        page.setup(player);
        MessageUtil.actionBar(player, "✦ " + page.getPageName() + " ✦");
    }

    /**
     * Switch to main menu.
     */
    public void switchToMainMenu(Player player) {
        switchPage(mainMenuPage, player);
    }

    /**
     * Switch to mechanics wizard.
     */
    public void switchToMechanicsWizard(Player player) {
        switchPage(mechanicsWizardPage, player);
    }

    /**
     * Switch to linear config.
     */
    public void switchToLinearConfig(Player player) {
        resetWorkingState();
        switchPage(linearConfigPage, player);
    }

    /**
     * Switch to rotation config.
     */
    public void switchToRotationConfig(Player player) {
        resetWorkingState();
        switchPage(rotationConfigPage, player);
    }

    /**
     * Save stage data and exit editor mode.
     */
    public void saveAndExit(Player player) {
        // Save stage
        plugin.getStageManager().save(stageName);

        // Restore inventory
        exit(player);

        MessageUtil.success(player, "Stage '" + stageName + "' saved successfully!");
    }

    /**
     * Exit editor mode without explicit save (still auto-saves).
     */
    public void exit(Player player) {
        // Cancel any active chat wizard
        if (activeChatWizard != null) {
            activeChatWizard.cancel();
            activeChatWizard = null;
        }

        // Restore inventory
        player.getInventory().setContents(savedInventory);
        player.getInventory().setArmorContents(savedArmor);
    }

    /**
     * Reset working state for a new structure configuration.
     */
    public void resetWorkingState() {
        workingAxis = "X";
        workingSpeed = 0.5;
        workingRange = 5.0;
        workingDirection = MovingStructureData.Direction.POSITIVE;
        workingCenter = null;
    }

    // --- Getters & Setters ---

    public ParkourFest getPlugin() { return plugin; }
    public UUID getPlayerId() { return playerId; }
    public String getStageName() { return stageName; }
    public StageData getStageData() { return stageData; }
    public HotbarPage getCurrentPage() { return currentPage; }

    public LocationData getPos1Selection() { return pos1Selection; }
    public void setPos1Selection(LocationData pos1Selection) { this.pos1Selection = pos1Selection; }

    public LocationData getPos2Selection() { return pos2Selection; }
    public void setPos2Selection(LocationData pos2Selection) { this.pos2Selection = pos2Selection; }

    public List<LocationData> getSelectedBlocks() { return selectedBlocks; }
    public void clearSelectedBlocks() { selectedBlocks.clear(); }

    public String getWorkingAxis() { return workingAxis; }
    public void setWorkingAxis(String workingAxis) { this.workingAxis = workingAxis; }

    public double getWorkingSpeed() { return workingSpeed; }
    public void setWorkingSpeed(double workingSpeed) { this.workingSpeed = workingSpeed; }

    public double getWorkingRange() { return workingRange; }
    public void setWorkingRange(double workingRange) { this.workingRange = workingRange; }

    public MovingStructureData.Direction getWorkingDirection() { return workingDirection; }
    public void setWorkingDirection(MovingStructureData.Direction workingDirection) { this.workingDirection = workingDirection; }

    public LocationData getWorkingCenter() { return workingCenter; }
    public void setWorkingCenter(LocationData workingCenter) { this.workingCenter = workingCenter; }

    public ChatWizard getActiveChatWizard() { return activeChatWizard; }
    public void setActiveChatWizard(ChatWizard activeChatWizard) { this.activeChatWizard = activeChatWizard; }

    public MainMenuPage getMainMenuPage() { return mainMenuPage; }
    public MechanicsWizardPage getMechanicsWizardPage() { return mechanicsWizardPage; }
    public LinearConfigPage getLinearConfigPage() { return linearConfigPage; }
    public RotationConfigPage getRotationConfigPage() { return rotationConfigPage; }
}
