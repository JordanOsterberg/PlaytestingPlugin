package dev.jcsoftware.playtestingplugin.commands;

import dev.jcsoftware.playtestingplugin.Colorize;
import dev.jcsoftware.playtestingplugin.PlaytestingPlugin;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

@RequiredArgsConstructor
public class PlaytestingCommand implements CommandExecutor {

    private final PlaytestingPlugin plugin;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.hasPermission("playtesting.admin")) {
            return true;
        }

        if (args.length == 0) {
            commandSender.sendMessage("/" + command.getName() + " [open|close|start|stop|reminder|clear]");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            plugin.setPlaytestingEnabled(true);
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&aPlaytesting has been started."));
        } else if (args[0].equalsIgnoreCase("stop")) {
            plugin.setPlaytestingEnabled(false);
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&cPlaytesting has ended."));
        } else if (args[0].equalsIgnoreCase("open")) {
            TextChannel signupChannel = plugin.getOrCreateChannel(plugin.getPlaytestingSignupChannelName());
            if (signupChannel != null && plugin.getDiscordServer() != null) {
                signupChannel.getManager().setTopic("Send your Minecraft IGN to be added to the Playtesting queue! :watermelon:")
                        .complete();

                if (signupChannel.getPermissionOverride(plugin.getDiscordServer().getPublicRole()) != null) {
                    signupChannel.getPermissionOverride(plugin.getDiscordServer().getPublicRole())
                            .getManager()
                            .setAllow(Permission.MESSAGE_READ)
                            .setAllow(Permission.MESSAGE_WRITE)
                            .complete();
                } else {
                    signupChannel.createPermissionOverride(plugin.getDiscordServer().getPublicRole())
                            .setAllow(Permission.MESSAGE_READ)
                            .setAllow(Permission.MESSAGE_WRITE)
                            .complete();
                }
            } else {
                commandSender.sendMessage(Colorize.color(Colorize.prefix() + "&cFailed to create signup channel or server is null &7(" + signupChannel + ", " + plugin.getDiscordServer() + ")"));
            }

            Role playtestingRole = plugin.getOrCreatePlaytestingRole();

            TextChannel chatChannel = plugin.getOrCreateChannel(plugin.getPlaytestingChatChannelName());
            if (chatChannel != null && playtestingRole != null && plugin.getDiscordServer() != null) {
                if (chatChannel.getPermissionOverride(playtestingRole) != null) {
                    chatChannel.getPermissionOverride(playtestingRole)
                            .getManager()
                            .setAllow(Permission.MESSAGE_READ)
                            .setAllow(Permission.MESSAGE_WRITE)
                            .complete();
                } else {
                    chatChannel.createPermissionOverride(playtestingRole)
                            .setAllow(Permission.MESSAGE_READ)
                            .setAllow(Permission.MESSAGE_WRITE)
                            .complete();
                }

                if (chatChannel.getPermissionOverride(plugin.getDiscordServer().getPublicRole()) != null) {
                    chatChannel.getPermissionOverride(plugin.getDiscordServer().getPublicRole())
                            .getManager()
                            .setDeny(Permission.MESSAGE_READ)
                            .complete();
                } else {
                    chatChannel.createPermissionOverride(plugin.getDiscordServer().getPublicRole())
                            .setDeny(Permission.MESSAGE_READ)
                            .complete();
                }
            } else {
                commandSender.sendMessage(Colorize.color(Colorize.prefix() + "&cFailed to create chat channel, playtesting role is null, or server is null &7(" + chatChannel + ", " + playtestingRole + ", " + plugin.getDiscordServer() + ")"));
            }

            commandSender.sendMessage(Colorize.color(Colorize.prefix() + "&aOpened playtesting signup."));
        } else if (args[0].equalsIgnoreCase("close")) {
            TextChannel signupChannel = plugin.getOrCreateChannel(plugin.getPlaytestingSignupChannelName());
            if (signupChannel != null) signupChannel.delete().complete();

            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&cClosed playtesting signup."));
        } else if (args[0].equalsIgnoreCase("reminder")) {
            plugin.sendPlaytestingReminder();
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&fSent playtesting reminder!"));
        } else if (args[0].equalsIgnoreCase("clear")) {
            Optional<TextChannel> optionalChannel = plugin.getDiscordBot().getTextChannels().stream()
                    .filter(channel -> channel.getName().equalsIgnoreCase(plugin.getPlaytestingSignupChannelName())).findAny();
            if (!optionalChannel.isPresent()) {
                commandSender.sendMessage("Can't clear Discord channel- doesn't exist");
            } else {
                TextChannel channel = optionalChannel.get();
                channel.deleteMessages(channel.getHistory().retrievePast(100).complete())
                    .complete();
            }

            plugin.clearIGNs();
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("Cleared &7#" + plugin.getPlaytestingSignupChannelName() + " &fand reset ign list."));
        } else {
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&cInvalid argument " + args[0] + "."));
        }

        return true;
    }
}
