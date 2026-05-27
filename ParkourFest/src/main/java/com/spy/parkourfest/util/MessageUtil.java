package com.spy.parkourfest.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Centralized messaging utilities using the Adventure API.
 * Provides consistent, styled messages throughout the plugin.
 */
public class MessageUtil {

    private static final Component PREFIX = Component.text("[")
            .color(NamedTextColor.DARK_GRAY)
            .append(Component.text("ParkourFest").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
            .append(Component.text("] ").color(NamedTextColor.DARK_GRAY));

    private static final Component WIZARD_PREFIX = Component.text("[")
            .color(NamedTextColor.DARK_GRAY)
            .append(Component.text("Setup Wizard").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
            .append(Component.text("] ").color(NamedTextColor.DARK_GRAY));

    /**
     * Send an info message (yellow text).
     */
    public static void info(Player player, String message) {
        player.sendMessage(PREFIX.append(Component.text(message).color(NamedTextColor.YELLOW)));
    }

    /**
     * Send a success message (green text).
     */
    public static void success(Player player, String message) {
        player.sendMessage(PREFIX.append(Component.text(message).color(NamedTextColor.GREEN)));
    }

    /**
     * Send an error message (red text).
     */
    public static void error(Player player, String message) {
        player.sendMessage(PREFIX.append(Component.text(message).color(NamedTextColor.RED)));
    }

    /**
     * Send a wizard prompt message (light purple text).
     */
    public static void wizard(Player player, String message) {
        player.sendMessage(WIZARD_PREFIX.append(Component.text(message).color(NamedTextColor.WHITE)));
    }

    /**
     * Send a wizard step description.
     */
    public static void wizardStep(Player player, String stepName, String currentValue) {
        player.sendMessage(Component.empty());
        player.sendMessage(WIZARD_PREFIX.append(
                Component.text("Current Step: ").color(NamedTextColor.GRAY)
                        .append(Component.text(stepName).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
        ));
        player.sendMessage(WIZARD_PREFIX.append(
                Component.text("Current Value: ").color(NamedTextColor.GRAY)
                        .append(Component.text(currentValue).color(NamedTextColor.WHITE))
        ));
        player.sendMessage(WIZARD_PREFIX.append(
                Component.text("Type a value in chat, or type ").color(NamedTextColor.GRAY)
                        .append(Component.text("skip").color(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC))
                        .append(Component.text(" to leave unchanged.").color(NamedTextColor.GRAY))
        ));
    }

    /**
     * Send an action bar message.
     */
    public static void actionBar(Player player, String message) {
        player.sendActionBar(Component.text(message).color(NamedTextColor.GREEN));
    }

    /**
     * Send an action bar message with a custom color.
     */
    public static void actionBar(Player player, String message, NamedTextColor color) {
        player.sendActionBar(Component.text(message).color(color));
    }

    /**
     * Send a title and subtitle.
     */
    public static void title(Player player, String titleText, String subtitleText) {
        Title title = Title.title(
                Component.text(titleText).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                Component.text(subtitleText).color(NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(500))
        );
        player.showTitle(title);
    }

    /**
     * Send a countdown title (big number).
     */
    public static void countdownTitle(Player player, int seconds) {
        NamedTextColor color;
        if (seconds <= 1) color = NamedTextColor.RED;
        else if (seconds <= 3) color = NamedTextColor.YELLOW;
        else color = NamedTextColor.GREEN;

        Title title = Title.title(
                Component.text(String.valueOf(seconds)).color(color).decorate(TextDecoration.BOLD),
                Component.text("Get Ready!").color(NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ofMillis(100))
        );
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f + (0.1f * seconds));
    }

    /**
     * Send a "GO!" title when the countdown ends.
     */
    public static void goTitle(Player player) {
        Title title = Title.title(
                Component.text("GO!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                Component.text("Run for the finish line!").color(NamedTextColor.YELLOW),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofMillis(500))
        );
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
    }

    /**
     * Format seconds into a readable time string (MM:SS or HH:MM:SS).
     */
    public static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }
}
