package org.fourtran.fourtranmc;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

public class SurvivalGameplay implements CommandExecutor, Listener {

    private final FourtranMC plugin;

    private final HashMap<Player, Boolean> survivalGameplayEnabledCache = new HashMap<>();
    private final HashMap<Player, GameMode> lastGamemodeCache = new HashMap<>();

    public final WorldGuardListener.Factory wgListenerFactory;

    // inner class to listen for region boundary cross events
    private static class WorldGuardListener extends Handler {

        public static FourtranMC plugin;

        public static final HashMap<Player, Boolean> playersInsideSurvivalArea = new HashMap<>();

        // factory needed in order to register this listener with WG's API
        private static class Factory extends Handler.Factory<WorldGuardListener> {
            @Override
            public WorldGuardListener create(Session session) {
                return new WorldGuardListener(session);
            }
        }

        public WorldGuardListener(Session session) {
            super(session);
        }

        @Override
        public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
                                       Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
            Player p = plugin.getServer().getPlayer(player.getName());
            
            // player entered survival area
            if (entered.stream().anyMatch(rgn -> rgn.getId().equals("survivalmode"))) {
                playersInsideSurvivalArea.put(p, true);

                // if the player is in survival gameplay, do nothing
                if (plugin.survivalGameplay.getSurvivalGameplayEnabled(p)) return true;

                // if the player is not in survival gameplay, put them in spectator so they cant interact with the survival area
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage("you entered the survival gameplay area and you've been put in spectator mode in order to not interfere. run '/survival' if you wish to participate.");
            }
            
            // player exited survival area
            if (exited.stream().anyMatch(rgn -> rgn.getId().equals("survivalmode"))) {
                playersInsideSurvivalArea.put(p, false);

                // if the player is in survival gameplay they are not allowed to leave the area. cancel the event and teleport them to the position they came from
                if (plugin.survivalGameplay.getSurvivalGameplayEnabled(p)) {
                    p.sendMessage("§cyou cannot exit the survival area while in survival gameplay. run '/survival' to exit");
                    p.teleport(new org.bukkit.Location(p.getWorld(), from.getX(), from.getY(), from.getZ()));
                    return false;
                }

                // otherwise the other players are free to leave
            }
            
            return true;
        }

    }

    public SurvivalGameplay(FourtranMC instance) {
        this.plugin = instance;
        WorldGuardListener.plugin = plugin;
        wgListenerFactory = new WorldGuardListener.Factory();
    }

    public boolean getSurvivalGameplayEnabled(Player p) {
        // query the cache first
        if (WorldGuardListener.playersInsideSurvivalArea.getOrDefault(p, false)) {
            return survivalGameplayEnabledCache.get(p);
        }

        // if the cache doesn't have a value cached, query the databse and update the cache
        FileConfiguration playerConfig = plugin.getPlayerConfig(p);
        boolean value = playerConfig.getBoolean("survivalGameplayEnabled", false);

        // update cache
        survivalGameplayEnabledCache.put(p, value);

        return value;
    }

    public void setSurvivalGameplayEnabled(Player p, boolean value) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(p);
        playerConfig.set("survivalGameplayEnabled", value);

        survivalGameplayEnabledCache.put(p, value);

        plugin.savePlayerConfig(p);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onGameModeChange(PlayerGameModeChangeEvent evt) {
        Player p = evt.getPlayer();
        // if the survival gameplay mode isnt enabled
        if (!getSurvivalGameplayEnabled(p)) {
            // ... and the player is inside the survival area, prevent them from changing gamemode
            if (WorldGuardListener.playersInsideSurvivalArea.getOrDefault(p, false) &&
                            evt.getNewGameMode() != GameMode.SPECTATOR
            ) {
                p.sendMessage("you cannot change your gamemode while in the survival gameplay area");
                evt.setCancelled(true);
                return;
            }
            return;
        }

        // if the survival gameplay mode is enabled the player isn't allowed to change gamemode, other than to survival mode itself
        if (evt.getNewGameMode() == GameMode.SURVIVAL) return;
        p.sendMessage("you cannot change your gamemode while in survival gameplay. exit survival gameplay with '/survival'");
        evt.setCancelled(true);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        // check if the player has permission to play survival
        if (!sender.hasPermission("4tran.survivalGameplay")) {
            sender.sendMessage("you are not an approved player! ask an admin to approve you.");
            return false;
        }

        // if the survival gameplay mode isn't enabled, enable it
        if (!getSurvivalGameplayEnabled(p)) {
            // prompt the player to empty their inventory, we don't want cheated-in items to circulate in survival
            if (!p.getInventory().isEmpty()) {
                p.sendMessage("§cplease empty your inventory before entering survival gameplay");
                return false;
            }

            setSurvivalGameplayEnabled(p, true); // update the database and cache
            lastGamemodeCache.put(p, p.getGameMode()); // update the last gamemode cache, so we can put the player back in their original gamemode when they quit survival gameplay
            p.setGameMode(GameMode.SURVIVAL); // set the new gamemode, survival of
            PointOfInterest.Locations.SURVIVAL.tp(p); // teleport the player to the survival area

            p.sendMessage("you have entered survival gameplay. you will not be able to change your gamemode until you disable it with '/survival'");
        } else {
            // if the player is already in survival gameplay mode...

            // prompt them to empty their inventory before exiting the mode, we don't want them to lose any of their items
            if (!p.getInventory().isEmpty()) {
                p.sendMessage("§cplease empty your inventory before exiting survival gameplay");
                return false;
            }

            setSurvivalGameplayEnabled(p, false); // update the cache and database
            p.setGameMode(lastGamemodeCache.getOrDefault(p, GameMode.ADVENTURE)); // put the player back in their original gamemode, or adventure by default
            PointOfInterest.Locations.SPAWN.tp(p); // teleport the player to spawn

            p.sendMessage("you have exited survival gameplay. you can change your gamemode at will.");
        }

        return true;
    }
}
