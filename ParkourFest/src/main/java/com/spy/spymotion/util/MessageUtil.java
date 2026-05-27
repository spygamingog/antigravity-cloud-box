package com.spy.spymotion.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Message utility for consistent color schemes.
 */
public class MessageUtil {

    private static final String PREFIX = "§6§lSpyMotion §8» §f";

    public static void success(CommandSender sender, String msg) {
        sender.sendMessage(Component.text(PREFIX).append(Component.text(msg).color(NamedTextColor.GREEN)));
    }

    public static void error(CommandSender sender, String msg) {
        sender.sendMessage(Component.text(PREFIX).append(Component.text(msg).color(NamedTextColor.RED)));
    }

    public static void info(CommandSender sender, String msg) {
        sender.sendMessage(Component.text(PREFIX).append(Component.text(msg).color(NamedTextColor.GRAY)));
    }

    public static void actionBar(Player player, String msg) {
        player.sendActionBar(Component.text(msg).color(NamedTextColor.GOLD));
    }

    public static void actionBar(Player player, String msg, NamedTextColor color) {
        player.sendActionBar(Component.text(msg).color(color));
    }
}
