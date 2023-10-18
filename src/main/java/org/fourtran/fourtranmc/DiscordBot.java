package org.fourtran.fourtranmc;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DiscordBot implements CommandExecutor, Listener {

    private final FourtranMC plugin;

    private final String DISCORD_INVITE = "";
    private final String BOT_TOKEN = "";
    private final Snowflake SERVER_ID = Snowflake.of("1158299345809051671");
    private final Snowflake CHAT_CHANNEL_ID = Snowflake.of("1158299346417238038");
    private final Snowflake CHAT_LINK_WEBHOOK_ID = Snowflake.of("");

    private final DiscordClient client = DiscordClient.create(BOT_TOKEN);
    private final GatewayDiscordClient gateway = client.login().block();

    private final HashMap<Player, Boolean> dscActivityEnabledCache = new HashMap<>();

    private boolean enabled = true;

    public DiscordBot(FourtranMC instance) {
        this.plugin = instance;
    }

    public void init() {
        new Thread(() -> {
            gateway.on(MessageCreateEvent.class).subscribe(evt -> {
                if (
                                evt.getMessage().getChannelId().equals(CHAT_CHANNEL_ID)
                                && evt.getMessage().getAuthor().isPresent()
                                && !evt.getMessage().getAuthor().get().isBot()
                                && enabled
                ) {
                    String authorName = evt.getMessage().getAuthor().get().getUsername();
                    String messageContent = evt.getMessage().getContent();

                    Member member = evt.getMessage().getAuthorAsMember().block();
                    assert member != null;

                    String roleName = "";
                    if (member.getRoles().blockFirst() != null) {
                        roleName = member.getRoles().blockFirst().getName();
                    }

                    plugin.getServer().sendMessage(Component.text("§a" + roleName + " §7<" + authorName + "> " + messageContent));
                }
            });

            gateway.onDisconnect().block();
        }).start();
    }

    public void sendServerMessage(String message) {
        gateway.getWebhookById(CHAT_LINK_WEBHOOK_ID)
                .flatMap(webhook -> webhook.execute()
                        .withAvatarUrl("https://imgur.com/lV2JbN2.png")
                        .withUsername("4tran-minecraft-server")
                        .withContent(message))
                .block();
    }

    public void sendChatLink(String author, String message) {
        HRT.Gender authorGender = plugin.hrt.getGender(author);
        String avatarUrl;

        if (authorGender == HRT.Gender.F) {
            avatarUrl = "https://imgur.com/XGOMHPW.png";
        } else if (authorGender == HRT.Gender.M) {
            avatarUrl = "https://imgur.com/9qGirvj.png";
        } else {
            avatarUrl = "https://imgur.com/45MGOM2.png";
        }

        gateway.getWebhookById(CHAT_LINK_WEBHOOK_ID)
                .flatMap(webhook -> webhook.execute()
                        .withAvatarUrl(avatarUrl)
                        .withUsername(author)
                        .withContent(message))
                .block();
    }

    public boolean getDscActivityEnabled(Player p) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(p);
        boolean value = playerConfig.getBoolean("discordActivityEnabled", true);

        dscActivityEnabledCache.put(p, value);

        return value;
    }

    public void setDscActivityEnabled(Player p, boolean value) {
        FileConfiguration playerConfig = plugin.getPlayerConfig(p);
        playerConfig.set("discordActivityEnabled", value);

        dscActivityEnabledCache.put(p, value);

        plugin.savePlayerConfig(p);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (!dscActivityEnabledCache.getOrDefault(p, true) || !enabled) return;

        plugin.discordBot.sendChatLink(p.getName(), event.getMessage());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!getDscActivityEnabled(p) || !enabled) return;

        sendServerMessage("***" + p.getName() + "*** joined the minecraft server");

        plugin.hrt.sendEndocrinologyReport(p, p.getName());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (!dscActivityEnabledCache.getOrDefault(p, true) || !enabled) return;

        sendServerMessage("***" + p.getName() + "*** quit the minecraft server");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("discord")) {
            sender.sendMessage(Component.text("§b§n" + DISCORD_INVITE).clickEvent(ClickEvent.openUrl(DISCORD_INVITE)));
            return true;
        } else if (command.getName().equals("discordrelay")) {
            if (!sender.hasPermission("4tran.discordrelay")) {
                sender.sendMessage("no... ask a grown up to do this.");
                return false;
            }

            enabled = !enabled;
            sender.sendMessage("discord relay enabled: " + enabled);
            return true;
        }
        if (!(sender instanceof Player)) return false;

        if (command.getName().equals("discordinvisible")) {
            setDscActivityEnabled((Player) sender, false);
            sender.sendMessage("your activity is now invisible from discord. enjoy your privacy!");
        } else if (command.getName().equals("discordvisible")) {
            setDscActivityEnabled((Player) sender, true);
            sender.sendMessage("your activity is now visible from discord.");
        }

        return true;
    }

}
