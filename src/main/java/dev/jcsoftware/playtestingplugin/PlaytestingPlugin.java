package dev.jcsoftware.playtestingplugin;

import dev.jcsoftware.playtestingplugin.commands.PlaytestingCommand;
import dev.jcsoftware.playtestingplugin.listener.DiscordEventListener;
import dev.jcsoftware.playtestingplugin.listener.PlayerConnectionListener;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaytestingPlugin extends JavaPlugin {

    @Getter
    private JDA discordBot;
    private ConfigurationFile ignFile;

    @Getter @Setter
    private Guild discordServer;

    private List<String> igns = new ArrayList<>();

    @SneakyThrows
    @Override
    public void onEnable() {
        super.onEnable();

        saveDefaultConfig();

        String discordToken = getConfig().getString("DISCORD_TOKEN");
        if (discordToken == null) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe("Please provide a DISCORD_TOKEN in the config.yml file.");
            return;
        }

        getCommand("playtesting").setExecutor(new PlaytestingCommand(this));

        this.discordBot = JDABuilder.createDefault(discordToken)
                .build();
        this.discordBot.addEventListener(new DiscordEventListener(this));

        this.ignFile = new ConfigurationFile(this, "igns");
        igns = this.ignFile.getConfiguration().getStringList("igns");

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    }

    @Override
    public void onDisable() {
        discordBot.shutdown();
    }

    public boolean isPlaytestingEnabled() {
        return getConfig().getBoolean("whitelist-igns");
    }

    public void setPlaytestingEnabled(boolean newValue) {
        if (isPlaytestingEnabled() == newValue) return;

        getConfig().set("whitelist-igns", newValue);
        saveConfig();

        if (!newValue) {
            igns.forEach(ign -> {
                Player player = Bukkit.getPlayer(ign);
                if (player != null && !player.isOp()) player.kickPlayer(Colorize.color("&aPlaytesting is now over. Thank you for playtesting!"));
            });

            clearIGNs();
        } else {
            sendPlaytestingReminder();
        }

        updateBotStatus();
    }

    public void updateBotStatus() {
        if (isPlaytestingEnabled()) {
            discordBot.getPresence().setActivity(Activity.of(Activity.ActivityType.WATCHING, "Playtesting is active!"));
        } else {
            discordBot.getPresence().setActivity(Activity.of(Activity.ActivityType.WATCHING, "Playtesting is not active."));
        }
    }

    public void sendPlaytestingReminder() {
        TextChannel chatChannel = getOrCreateChannel(getPlaytestingChatChannelName());
        if (chatChannel == null) return;
        chatChannel.sendMessage("Hello @here! Let's get started... our server IP is " + getServer().getIp() + ":" + getServer().getPort() + ". Please connect within the next **3 minutes** to be in the playtesting session. **Only the IGN you submitted will be able to connect to the server.**")
            .complete();
    }

    public void registerIGN(String ign) {
        if (igns.contains(ign)) return;

        igns.add(ign);
        ignFile.getConfiguration().set("igns", igns);
        ignFile.save();
    }

    public boolean isWhitelisted(String name) {
        return igns.contains(name);
    }

    public void clearIGNs() {
        igns.clear();
        ignFile.getConfiguration().set("igns", new ArrayList<String>());
        ignFile.save();
    }

    public TextChannel getOrCreateChannel(String name) {
        List<TextChannel> matches = discordBot.getTextChannelsByName(name, true);
        TextChannel channel;

        if (matches.size() == 0) {
            List<Category> categories = discordBot.getCategoriesByName(getPlaytestingCategoryName(), true);
            Category category;

            if (categories.size() == 0) {
                if (discordServer == null) {
                    return null;
                }

                category = discordServer.createCategory(getPlaytestingCategoryName()).complete();
            } else {
                category = categories.get(0);
            }

            channel = category.createTextChannel(name).complete();
        } else {
            channel = matches.get(0);
        }

        return channel;
    }

    public Role getOrCreatePlaytestingRole() {
        return getOrCreateRole(getPlaytestingRoleName(), "#0fd9b7");
    }

    public Role getOrCreateRole(String name, String hexColor) {
        if (discordServer == null) return null;
        List<Role> matches = discordServer.getRolesByName(name, true);

        return matches.size() <= 0 ? discordServer.createRole()
                    .setName(name)
                    .setColor(Color.decode(hexColor))
                    .complete() :
                matches.get(0);
    }

    public String getPlaytestingSignupChannelName() {
        return getConfigOptionOrDefault("playtesting-signup-channel-name", "playtesting-igns");
    }

    public String getPlaytestingChatChannelName() {
        return getConfigOptionOrDefault("playtesting-chat-channel-name", "playtesting-chat");
    }

    public String getPlaytestingCategoryName() {
        return getConfigOptionOrDefault("playtesting-category-name", "Playtesting");
    }

    private String getConfigOptionOrDefault(String key, String defaultValue) {
        String name = getConfig().getString(key);
        if (name == null) {
            return defaultValue;
        } else {
            return name;
        }
    }

    public String getDiscordServerName() {
        return getConfigOptionOrDefault("discord-server-name", "");
    }

    public String getPlaytestingRoleName() {
        return getConfigOptionOrDefault("playtesting-role-name", "Playtester");
    }

    public boolean isRegistered(String ign) {
        return igns.contains(ign);
    }

}
