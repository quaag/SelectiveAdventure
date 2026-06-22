package com.selectiveadventure.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Small helper for turning legacy ampersand color strings into Adventure
 * components and sending them to players / the console.
 */
public final class Msg {

    public static final String PREFIX = "&8[&aSA&8] &7";

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private Msg() {
    }

    public static Component color(String s) {
        return LEGACY.deserialize(s == null ? "" : s);
    }

    /** Sends a prefixed message. */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(color(PREFIX + message));
    }

    /** Sends a raw (un-prefixed) message. */
    public static void raw(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void actionbar(Player player, String message) {
        player.sendActionBar(color(message));
    }
}
