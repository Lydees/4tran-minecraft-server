package org.fourtran.fourtranmc;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RedditRelay implements CommandExecutor  {

    private final FourtranMC plugin;
    public final RedditClient reddit;

    private boolean enabled = true;

    private final HashMap<String, String> lastPostsIds = new HashMap<>();

    public RedditRelay(FourtranMC instance) {
        this.plugin = instance;

        // Assuming we have a 'script' reddit app
        Credentials oauthCreds = Credentials.script(
                "Lydees",
                "",
                "",
                ""
        );

        // Create a unique User-Agent for our bot
        UserAgent userAgent = new UserAgent("4tran-minecraft-server", "org.fourtran.fourtranmc", "0", "4tranMC");

        // Authenticate our client
        this.reddit = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds);
        this.reddit.setLogHttp(false);

        lastPostsIds.put("4tran4", "-1");
        lastPostsIds.put("4trancirclejerk", "-1");
        lastPostsIds.put("4tran", "-1");
        lastPostsIds.put("ttttrans", "-1");
    }

    public void startListeningForNewPosts() {
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, (scheduledTask) -> {
            if (enabled) {
                checkForNewSubmission("4tran4");
                checkForNewSubmission("4tran");
                checkForNewSubmission("4trancirclejerk");
                checkForNewSubmission("ttttrans");
            }
        }, 30, 60, TimeUnit.SECONDS);
    }

    private void checkForNewSubmission(String subredditName) {
        SubredditReference subreddit = reddit.subreddit(subredditName);

        DefaultPaginator<Submission> findNewestPost = subreddit.posts().sorting(SubredditSort.NEW).limit(1).build();
        Submission newestPost = findNewestPost.next().get(0);

        String postId = newestPost.getId();
        if (lastPostsIds.get(subredditName).equals("-1") || postId.equals(lastPostsIds.get(subredditName))) {
            lastPostsIds.put(subredditName, postId);
            return;
        }
        lastPostsIds.put(subredditName, postId);

        plugin.getServer().sendMessage(
                Component.text(
                        "new post on §d" + subredditName + "§f: §7\"" + newestPost.getTitle() + "\" §f(§n§b" + newestPost.getAuthor() + "§f)"
                )
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("4tran.redditrelay")) {
            sender.sendMessage("no... ask a grown up to do this.");
            return false;
        }

        enabled = !enabled;
        sender.sendMessage("reddit relay enabled: " + enabled);

        return true;
    }
}
