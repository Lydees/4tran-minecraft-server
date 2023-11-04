package org.fourtran.fourtranmc;

import com.destroystokyo.paper.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.title.TitlePart;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PointOfInterest implements Listener, CommandExecutor {

    private final FourtranMC plugin;

    public PointOfInterest(FourtranMC instance) {
        this.plugin = instance;

    }

    public enum Locations {

        SPAWN("welcome to spawn. where it all begins.", 130, 92, 0, -179, 0),
        GRAVE("grave of what could've been", 210, 86, -56, -91, -33),
        TEMPLE("an imperfect monument to imperfect people", 186, 82, -252, -180, -23),
        CASTLE("her majesty's residence.", 149, 120, -285, 153, -26),
        ALTY("§dalty§f. dont cry.", 181, 80, -106, -100, -30),
        ILYSM("i §dlove§f u so much.", 16.492, 102, -390.786, -179, 34),
        THERAPY("you need this.", 154, 105, -324, -89, 0),
        BEAR("bear", 206, 205, -243, -84, -21),
        SPLEEF("spleef arena, ig.", 170, -1, -138, 0, 3),
        ARENA("fight to the §cdeath§f.", 138.991, 46, -346.985, 0, -3),
        ARMORY("arm yourself.", 138.991, 102, -303.561, 0, 2),
        CRATER("what happened here?", 598, 69, -320, -88, 24),
        CUTE_HOUSE("§dvi§f and §dsarah§f's house", 299, 120, -341, 0, -3),
        MANSION("livin' the good life", 256, 120, 204, 90, 6),
        PLOT("vi's plot. i wonder what she's working on...", 20, 125, -298, 85, -2),
        TRANNYWOOD("chase the fame.", 183, 85, -218, -45, -13);

        public final String msg;
        public final double x;
        public final double y;
        public final double z;
        public final float pitch;
        public final float yaw;

        Locations(String msg, double x, double y, double z, float pitch, float yaw) {
            this.msg = msg;
            this.x = x;
            this.y = y;
            this.z = z;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public Location getLoc(Player p) {
            return new Location(p.getWorld(), x, y, z, pitch, yaw);
        }

        public void tp(Player p) {
            p.teleport(getLoc(p));
            p.sendMessage(msg);
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player p = event.getPlayer();

        if (!p.hasPlayedBefore()) {
            Locations.SPAWN.tp(p);
            plugin.getServer().sendMessage(Component.text("welcome to 4tran §a" + p.getName()));
        }

        // halloween sequence
        /*
        p.getWorld().strikeLightning(p.getLocation());
        new Thread(() -> {
            p.setPlayerWeather(WeatherType.DOWNFALL);
            p.setPlayerTime(18000, false);
            tempWait(1500);
            p.showElderGuardian(true);
            p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0F, 0.5F);

            p.sendTitle(new Title("§6§lHappy Halloween!", "§c\uD83C\uDF83", 10, 100, 20));

            tempWait(5000);
            p.resetPlayerWeather();
            p.resetPlayerTime();
        }).start();
        */
    }

    private void tempWait(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        if (!p.getWorld().getName().equals("world")) {
            p.sendMessage("only available in the overworld!");
            return false;
        }

        if (args.length < 1 || args[0].isEmpty()) {
            Locations.SPAWN.tp(p);
            return true;
        }

        Locations chosenLocation;
        try {
            chosenLocation = Locations.valueOf(args[0].toUpperCase());
            chosenLocation.tp(p);
            return true;
        } catch (IllegalArgumentException e) {
            if (args[0].equalsIgnoreCase("list")) {
                Component locations = Component.text("");

                for (Locations l : Locations.values()) {
                    locations = locations.append(
                            Component.text(l.name() + "§7, §f")
                                    .hoverEvent(Component.text(l.msg))
                                    .clickEvent(ClickEvent.runCommand("poi " + l.name()))
                    );
                }

                p.sendMessage(locations);
                return true;
            }

            p.sendMessage("cant find point of interest named " + args[0] + ". run '/poi list' for the complete list.");
            return false;
        }
    }
}
