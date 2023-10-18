package org.fourtran.fourtranmc;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChatPrefixes implements Listener, CommandExecutor {

    private final FourtranMC plugin;

    private final HashMap<Player, String> cachedPrefixes = new HashMap<>();

    public ChatPrefixes(FourtranMC instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerLoginEvent event) {
        Player p = event.getPlayer();

        // Cache the player's prefix if we haven't already
        if (cachedPrefixes.get(p) == null) {
            String prefix = getCustomPrefix(p);
            cachedPrefixes.put(p, prefix);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Retrieve the player's custom prefix from your configuration system based on their UUID.
        String customPrefix = cachedPrefixes.get(event.getPlayer());

        event.setCancelled(true);
        plugin.getServer().sendMessage(
                Component.text(customPrefix + " §f<" + event.getPlayer().getName() + "> " + event.getMessage())
        );
    }

    // Method to get a player's custom prefix.
    public String getCustomPrefix(Player player) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(player);
        // You can set a default prefix here if the player doesn't have one.
        return playerConfig.getString("customPrefix", "");
    }

    // Method to set a player's custom prefix.
    public void setCustomPrefix(Player player, String prefix) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(player);
        playerConfig.set("customPrefix", prefix);
        plugin.savePlayerConfig(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        } else if (args.length < 1 || args[0].isEmpty()) {
            sender.sendMessage("/prefix <prefix> [color]");
            return false;
        } else if (args[0].length() > 16) {
            sender.sendMessage("your prefix is too long. silly.");
            return false;
        } else if (sender.hasPermission("4tran.cantuseprefix") && !sender.hasPermission("4tran.canuseprefix")) {
            sender.sendMessage("you cant change your prefix. there's probably a good reason for that.");
            return false;
        }

        String prefixColor = "f";
        if (args.length >= 2 && !args[1].isEmpty()) {
            switch (args[1]) {
                case "dark_red":
                    prefixColor = "4";
                    break;
                case "red":
                    prefixColor = "c";
                    break;
                case "gold":
                    prefixColor = "6";
                    break;
                case "yellow":
                    prefixColor = "e";
                    break;
                case "dark_green":
                    prefixColor = "2";
                    break;
                case "green":
                    prefixColor = "a";
                    break;
                case "aqua":
                    prefixColor = "b";
                    break;
                case "dark_aqua":
                    prefixColor = "3";
                    break;
                case "dark_blue":
                    prefixColor = "1";
                    break;
                case "blue":
                    prefixColor = "9";
                    break;
                case "pink":
                    prefixColor = "d";
                    break;
                case "purple":
                    prefixColor = "5";
                    break;
                case "grey":
                case "gray":
                    prefixColor = "7";
                    break;
                case "dark_grey":
                case "dark_gray":
                    prefixColor = "8";
                    break;
                default:
                    sender.sendMessage("what color is that??");
                    break;
            }
        }
        prefixColor = "§" + prefixColor;

        String prefix = prefixColor + args[0];
        setCustomPrefix((Player) sender, prefix);
        cachedPrefixes.put((Player) sender, prefix);

        sender.sendMessage("success... yay...");

        return true;
    }
}
