package dev.jcsoftware.playtestingplugin.listener;

import dev.jcsoftware.playtestingplugin.Colorize;
import dev.jcsoftware.playtestingplugin.PlaytestingPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@RequiredArgsConstructor
public class PlayerConnectionListener implements Listener {

    private final PlaytestingPlugin plugin;

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getUniqueId());
        if (offlinePlayer.isOp()) return;

        if (!plugin.isPlaytestingEnabled()) {
            if (plugin.isWhitelisted(event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Colorize.color("&cPlaytesting hasn't started yet! The Playtesting Bot will notify you when playtesting begins."));
            } else {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Colorize.color("&cPlaytesting hasn't started yet! Join the whitelist by going to the community Discord (https://discord.gg/kpsk9tW) and adding your IGN to #playtesting-igns"));
            }
        }
    }

}
