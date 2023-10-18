package org.fourtran.fourtranmc;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public final class FourtranMC extends JavaPlugin {

    public final ChatPrefixes    chatPrefixes = new ChatPrefixes   (this);
    public final FunnyMobs       funnyMobs    = new FunnyMobs      (this);
    public final RedditRelay     redditRelay  = new RedditRelay    (this);
    public final PointOfInterest poi          = new PointOfInterest(this);
    public final HRT             hrt          = new HRT            (this);
    public final DiscordBot      discordBot   = new DiscordBot     (this);

    // This map will store player-specific configurations.
    private final java.util.Map<UUID, FileConfiguration> playerConfigs = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(chatPrefixes, this);
        getServer().getPluginManager().registerEvents(funnyMobs, this);
        getServer().getPluginManager().registerEvents(poi, this);
        getServer().getPluginManager().registerEvents(hrt, this);
        getServer().getPluginManager().registerEvents(discordBot, this);

        PluginCommand cmdPrefix = getCommand("prefix");
        cmdPrefix.setExecutor(chatPrefixes);

        PluginCommand cmdPoi = getCommand("pointofinterest");
        cmdPoi.getAliases().add("poi");
        cmdPoi.getAliases().add("warp");
        cmdPoi.getAliases().add("takemeto");
        cmdPoi.getAliases().add("spawn");
        cmdPoi.setExecutor(poi);

        PluginCommand cmdEndo = getCommand("endo");
        cmdEndo.getAliases().add("endocrinology");
        cmdEndo.setExecutor(hrt);

        getCommand("redditrelay").setExecutor(redditRelay);

        getCommand("discord").setExecutor(discordBot);
        getCommand("discordinvisible").setExecutor(discordBot);
        getCommand("discordvisible").setExecutor(discordBot);
        getCommand("discordrelay").setExecutor(discordBot);

        discordBot.init();
        redditRelay.startListeningForNewPosts();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Load or create a player-specific configuration file.
    public FileConfiguration getPlayerConfig(UUID playerUuid) {
        File playerFile = new File(getDataFolder(), playerUuid + ".yml");

        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                // ignore
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        playerConfigs.put(playerUuid, config);
        return config;
    }

    public FileConfiguration getPlayerConfig(Player player) {
        return getPlayerConfig(player.getUniqueId());
    }

    // Save a player-specific configuration file.
    public void savePlayerConfig(UUID playerUuid) {
        FileConfiguration playerConfig = playerConfigs.get(playerUuid);
        try {
            playerConfig.save(new File(getDataFolder(), playerUuid + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerConfig(Player player) {
        savePlayerConfig(player.getUniqueId());
    }

}
