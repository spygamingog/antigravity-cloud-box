package com.spy.parkourfest.editor;

import com.spy.parkourfest.ParkourFest;
import com.spy.parkourfest.model.StageData;
import com.spy.parkourfest.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Interactive chat-based setup wizard for configuring stage parameters.
 * Steps through: Player Limit → Countdown → PVP toggle.
 * Each step has a 20-second timeout.
 */
public class ChatWizard {

    public enum WizardStep {
        PLAYER_LIMIT,
        COUNTDOWN_SECONDS,
        COMPLETION_LIMIT,
        PVP_TOGGLE,
        DONE
    }

    private final EditorSession session;
    private final UUID playerId;
    private WizardStep currentStep = WizardStep.PLAYER_LIMIT;
    private BukkitTask timeoutTask;
    private boolean cancelled = false;

    public ChatWizard(EditorSession session, Player player) {
        this.session = session;
        this.playerId = player.getUniqueId();
    }

    public void start() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        MessageUtil.wizard(player, "═══ Stage Configuration Wizard ═══");
        MessageUtil.wizard(player, "Answer each prompt in chat. Type 'skip' to keep current values.");
        promptCurrentStep(player);
    }

    /**
     * Handle a chat message from the wizard player.
     * @return true if the message was consumed by the wizard
     */
    public boolean handleInput(Player player, String message) {
        if (cancelled || currentStep == WizardStep.DONE) return false;

        String input = message.trim().toLowerCase();
        cancelTimeout();

        if (input.equals("cancel")) {
            cancel();
            MessageUtil.info(player, "Setup wizard cancelled.");
            return true;
        }

        StageData data = session.getStageData();
        boolean skip = input.equals("skip");

        switch (currentStep) {
            case PLAYER_LIMIT -> {
                if (!skip) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val < 2 || val > 100) {
                            MessageUtil.error(player, "Must be between 2 and 100.");
                            startTimeout(player);
                            return true;
                        }
                        data.setPlayerLimit(val);
                        MessageUtil.success(player, "Player limit set to " + val);
                    } catch (NumberFormatException e) {
                        MessageUtil.error(player, "Invalid number. Try again or type 'skip'.");
                        startTimeout(player);
                        return true;
                    }
                }
                currentStep = WizardStep.COUNTDOWN_SECONDS;
            }
            case COUNTDOWN_SECONDS -> {
                if (!skip) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val < 3 || val > 60) {
                            MessageUtil.error(player, "Must be between 3 and 60.");
                            startTimeout(player);
                            return true;
                        }
                        data.setCountdownSeconds(val);
                        MessageUtil.success(player, "Countdown set to " + val + " seconds");
                    } catch (NumberFormatException e) {
                        MessageUtil.error(player, "Invalid number. Try again or type 'skip'.");
                        startTimeout(player);
                        return true;
                    }
                }
                currentStep = WizardStep.COMPLETION_LIMIT;
            }
            case COMPLETION_LIMIT -> {
                if (!skip) {
                    try {
                        int val = Integer.parseInt(input);
                        if (val < 1 || val > 50) {
                            MessageUtil.error(player, "Must be between 1 and 50.");
                            startTimeout(player);
                            return true;
                        }
                        data.setCompletionLimit(val);
                        MessageUtil.success(player, "Completion limit set to " + val);
                    } catch (NumberFormatException e) {
                        MessageUtil.error(player, "Invalid number.");
                        startTimeout(player);
                        return true;
                    }
                }
                currentStep = WizardStep.PVP_TOGGLE;
            }
            case PVP_TOGGLE -> {
                if (!skip) {
                    if (input.equals("enable") || input.equals("true") || input.equals("on")) {
                        data.setPvpEnabled(true);
                        MessageUtil.success(player, "PVP enabled!");
                    } else if (input.equals("disable") || input.equals("false") || input.equals("off")) {
                        data.setPvpEnabled(false);
                        MessageUtil.success(player, "PVP disabled!");
                    } else {
                        MessageUtil.error(player, "Type 'enable' or 'disable'.");
                        startTimeout(player);
                        return true;
                    }
                }
                currentStep = WizardStep.DONE;
                finish(player);
                return true;
            }
            default -> { return false; }
        }

        promptCurrentStep(player);
        return true;
    }

    private void promptCurrentStep(Player player) {
        StageData data = session.getStageData();
        switch (currentStep) {
            case PLAYER_LIMIT ->
                    MessageUtil.wizardStep(player, "Player Limit", String.valueOf(data.getPlayerLimit()));
            case COUNTDOWN_SECONDS ->
                    MessageUtil.wizardStep(player, "Countdown Seconds", String.valueOf(data.getCountdownSeconds()));
            case COMPLETION_LIMIT ->
                    MessageUtil.wizardStep(player, "Completion Limit (winners needed)", String.valueOf(data.getCompletionLimit()));
            case PVP_TOGGLE ->
                    MessageUtil.wizardStep(player, "PVP Enabled", data.isPvpEnabled() ? "enabled" : "disabled");
        }
        startTimeout(player);
    }

    private void startTimeout(Player player) {
        cancelTimeout();
        timeoutTask = Bukkit.getScheduler().runTaskLater(session.getPlugin(), () -> {
            if (!cancelled && currentStep != WizardStep.DONE) {
                MessageUtil.error(player, "Setup wizard timed out (20s). Wizard cancelled.");
                cancel();
            }
        }, 20 * 20L); // 20 seconds
    }

    private void cancelTimeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
    }

    private void finish(Player player) {
        cancelTimeout();
        cancelled = true;
        session.setActiveChatWizard(null);
        MessageUtil.wizard(player, "═══ Configuration Complete! ═══");
        MessageUtil.success(player, "All values updated. Use Save & Exit (Slot 8) to persist.");
    }

    public void cancel() {
        cancelTimeout();
        cancelled = true;
        session.setActiveChatWizard(null);
    }

    public boolean isCancelled() { return cancelled; }
    public WizardStep getCurrentStep() { return currentStep; }
}
