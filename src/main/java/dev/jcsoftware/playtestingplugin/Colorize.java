package dev.jcsoftware.playtestingplugin;

import org.bukkit.ChatColor;

public class Colorize {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String prefix() {
        return color("&f&lPlaytesting &r&7> &r&f");
    }

}
