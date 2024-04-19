package org.fourtran.fourtranmc;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class HRT implements Listener, CommandExecutor {

    private final FourtranMC plugin;

    public static final Component componentE = Component.text("§dE");
    public static final Component componentT = Component.text("§3T");

    private final HashMap<UUID, Integer> queuedE = new HashMap<>();
    private final HashMap<UUID, Integer> queuedT = new HashMap<>();

    private final HashMap<String, Gender> cachedGenders = new HashMap<>();

    public HRT(FourtranMC instance) {
        this.plugin = instance;
    }

    public enum Gender { M, F, A;}

    public enum EndoResult {

        //ANDROGYNOUS(0, 50),
        POON_HON(51, 200),
        POON_TWINKHON(201, 500),
        PASSOID(501, 1000),
        GIGAPASSOID(1001, 5000),
        ULTRAPASSOID(5001, 10000),
        GOD_GODDESS(10001, 99999);

        public final int min, max;

        EndoResult(int min, int max) {
            this.min = min;
            this.max = max;
        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHRTConsumation(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        Player p = event.getPlayer();

        if (item.getItemMeta().getDisplayName().equals("§dE")) {
            item.setAmount(item.getAmount()-1);

            PotionEffect pe = new PotionEffect(
                    PotionEffectType.JUMP,
                    200,
                    2
            );

            p.addPotionEffect(pe);
            queuedE.put(p.getUniqueId(), queuedE.getOrDefault(p.getUniqueId(), 0) + 1);
            p.sendMessage("§d+1 E");
        } else if (item.getItemMeta().getDisplayName().equals("§3T")) {
            item.setAmount(item.getAmount()-1);

            PotionEffect pe = new PotionEffect(
                    PotionEffectType.HEALTH_BOOST,
                    200,
                    2
            );

            p.addPotionEffect(pe);
            queuedT.put(p.getUniqueId(), queuedT.getOrDefault(p.getUniqueId(), 0) + 1);
            p.sendMessage("§3+1 T");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        UUID player = Bukkit.getOfflinePlayer(event.getPlayer().getName()).getUniqueId();
        updateHRTprofile(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID player = Bukkit.getOfflinePlayer(event.getPlayer().getName()).getUniqueId();
        updateHRTprofile(player);
        sendEndocrinologyReport(event.getPlayer(), event.getPlayer().getName());
    }

    public void setEstrogen(UUID player, int amount) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(player);
        playerConfig.set("estrogen", amount);
        plugin.savePlayerConfig(player);
    }

    public int getEstrogen(UUID player) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(player);
        return playerConfig.getInt("estrogen", 0);
    }

    public void setTestosterone(UUID player, int amount) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(player);
        playerConfig.set("testosterone", amount);
        plugin.savePlayerConfig(player);
    }

    public int getTestosterone(UUID player) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(player);
        return playerConfig.getInt("testosterone", 0);
    }

    public int[] updateHRTprofile(UUID player) {
        int E = getEstrogen(player) + queuedE.getOrDefault(player, 0);
        int T = getTestosterone(player) + queuedT.getOrDefault(player, 0);

        setEstrogen(player, E);
        setTestosterone(player, T);

        queuedE.put(player, 0);
        queuedT.put(player, 0);

        int[] r = new int[2];
        r[0] = E;
        r[1] = T;
        return r;
    }

    public String sendEndocrinologyReport(Player receiver, String target) {
        UUID targetUUID = Bukkit.getOfflinePlayer(target).getUniqueId();

        int[] hrtProfile = updateHRTprofile(targetUUID);
        int E = hrtProfile[0];
        int T = hrtProfile[1];

        // calculate result
        int result;
        Gender gender;
        if (T > E) {
            result = T-E;
            gender = Gender.M;
        } else if (T < E) {
            result = E-T;
            gender = Gender.F;
        } else {
            result = 0;
            gender = Gender.A;
        }
        cachedGenders.put(target, gender);

        String finalResult = "§2ANDROGYNOUS";
        for (EndoResult r : EndoResult.values()) {
            if (r.min <= result && result <= r.max) {
                if (gender == Gender.M) {
                    finalResult = "§3" + r.name().split("_")[0];
                } else {
                    try {
                        finalResult = "§d" + r.name().split("_")[1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        finalResult = "§d" + r.name();
                    }
                }
                break;
            }
        }

        receiver.sendMessage(
                Component.text("§7Endocrinologist's report for §f" + target + "§7:").appendNewline().appendNewline()
                        .append(Component.text("§dEstrogen§7: .................. §f" + E)).appendNewline()
                        .append(Component.text("§3Testosterone§7: ....... §f" + T)).appendNewline().appendNewline()
                        .append(Component.text("§7Result: " + finalResult))
        );

        if (target.equalsIgnoreCase(receiver.getName())) {
            receiver.playerListName(Component.text(finalResult + "§f " + receiver.getName()));
        }

        return finalResult;
    }

    public Gender getGender(String player) {
        return cachedGenders.getOrDefault(player, Gender.A);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        String target;
        if (args.length < 1 || args[0].isEmpty()) {
            target = p.getName();
        } else {
            target = args[0];
        }

        sendEndocrinologyReport(p, target);

        return false;
    }

}
