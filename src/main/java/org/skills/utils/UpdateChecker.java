/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.skills.utils;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.skills.main.locale.SkillsLang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A simple async update checker that uses <a href="https://www.spigotmc.org/api/general.php">SpigotMC</a> official General API.
 * This API can also accept API keys, but this program does not use any.
 * <p>
 * Before using this class, you'll have to invoke the {@link #checkForUpdates()} method
 * at least once. The method can be invoked multiple times if you want to recheck for updates.
 * <p>
 * The automatic downloader uses <a href="https://spiget.org">SpiGet</a> API.
 * You can only download the plugin if it's a free plugin. Premium resources are not supported
 * and will not be supported as it needs the user's information to access the account, which is
 * something that you shouldn't really bother doing.<br>
 * Also it's not possible to update the plugin automatically using the downloaded JAR file.
 * The owner must manually change it unless you make another plugin that handles your updates,
 * which is not recommended.
 * <p>
 * No schedulers or async tasks need to be performed for any of the methods.
 * They're already handled by {@link CompletableFuture}.
 * <p>
 * <b>Example:</b><br>
 * The simplest and most common way of doing this in your <b>main class</b> is:
 * <p><blockquote><pre>
 *      UpdateChecker updater = new UpdateChecker(this, $RESOURCE_ID);
 *      updater.checkForUpdates()
 *         .thenRunAsync(updater::sendUpdates) // notify console
 *         .thenRunAsync(updater::downloadUpdate); // Only for free plugins - not really recommended.
 * </pre></blockquote><p>
 * To get your <b>$RESOURCE_ID</b> you need to go to your plugin's Spigot page and
 * the link should look like this <i>https://www.spigotmc.org/resources/PLUGIN-NAME.<b>$RESOURCE_ID</b>/</i>
 *
 * @author Crypto Morin
 * @version 2.0.0
 */
public class UpdateChecker implements Listener {
    public final String currentVersion;
    private final String REQUEST_URL;
    private final String prefix;
    private final JavaPlugin plugin;
    private final int $RESOURCE_ID;
    private final File download;
    public int lastHttpResponseCode;
    public String latestVersion;
    public boolean canUpdate;

    public UpdateChecker(@Nonnull JavaPlugin plugin, int $RESOURCE_ID) {
        Objects.requireNonNull(plugin, "Update checker plugin cannot be null");

        this.plugin = plugin;
        this.prefix = SkillsLang.PREFIX.parse();
        this.$RESOURCE_ID = $RESOURCE_ID;
        this.download = new File(plugin.getDataFolder(), plugin.getName() + ".jar");
        this.currentVersion = plugin.getDescription().getVersion();
        REQUEST_URL = "https://api.spigotmc.org/legacy/update.php?resource=" + $RESOURCE_ID;

        if (plugin.getConfig().getBoolean("check-updates")) Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Compares two program versions.<br>
     * This also supports <b>BETA</b> and <b>SNAPSHOT</b> suffixes.
     * Note that these versions are ignored as a new update. If you want to
     * change this, you have to refer to the <b>prereleases</b> region inside the method.<br>
     * It also supports multiple version schemes such as <i>1.0.0</i> or <i>1.0.2.0.5.7b</i>
     * <p><br>
     * <b>Examples</b>
     * <p>
     * <pre>
     *     2.0.0 > 1.0.0, 2.2.0 > 2.0.0, 2.2.2 > 2.2.0
     *     1.0.0.0.0 > 1.0.0 (Not really, but still...)
     *     1.0.0 = 1.0.0-beta = 1.0.0-SNAPSHOT = 1.0.0b = 1.0.0a
     * </pre>
     *
     * @param oldVer the old version.
     * @param newVer the new version.
     *
     * @return true if the newVer is higher than oldVer, otherwise false.
     * @since 1.0.0
     */
    public static boolean isVersionHigher(@Nullable String oldVer, @Nullable String newVer) {
        if (Strings.isNullOrEmpty(oldVer) || Strings.isNullOrEmpty(newVer)) return false;
        if (oldVer.equals(newVer)) return false;

        oldVer = StringUtils.remove(StringUtils.remove(StringUtils.remove(StringUtils.remove(
                StringUtils.deleteWhitespace(oldVer.toLowerCase(Locale.ENGLISH)), 'v'), 'b'), '-'), "snapshot");
        newVer = StringUtils.remove(StringUtils.remove(StringUtils.remove(StringUtils.remove(
                StringUtils.deleteWhitespace(newVer.toLowerCase(Locale.ENGLISH)), 'v'), 'b'), '-'), "snapshot");

        boolean isOldPrerelease = false;
        boolean isNewPrerelease = false;

        if (oldVer.contains("beta")) {
            isOldPrerelease = true;
            oldVer = StringUtils.remove(oldVer, "beta");
        }
        if (newVer.contains("beta")) {
            isNewPrerelease = true;
            newVer = StringUtils.remove(newVer, "beta");
        }

        String[] oldV = StringUtils.split(oldVer, '.');
        String[] newV = StringUtils.split(newVer, '.');

        int max = Math.max(oldV.length, newV.length);
        boolean hasBeta = oldV.length != newV.length;
        for (int i = 0; i < max; i++) {
            if (hasBeta && i + 1 == max) return max == newV.length;

            int older = Integer.parseInt(oldV[i]);
            int newer = Integer.parseInt(newV[i]);

            if (newer == older) continue;
            if (newer > older) {
                if (isNewPrerelease && isOldPrerelease) return true;
                return !isNewPrerelease || isOldPrerelease;
            }
            return false;
        }
        return false;
    }

    /**
     * Gets the last HTTP response code received from {@link #getVersion()}
     * A list of response codes with names can be found in {@link HttpURLConnection}
     * Any other codes other than {@link HttpURLConnection#HTTP_OK} is not normal.
     *
     * @return the last HTTP response code received while getting the version.
     * @since 1.0.0
     */
    public int getLastHttpResponseCode() {
        return lastHttpResponseCode;
    }

    /**
     * <b>Initialization</b> - Can be called multiple times.<br>
     * A simple of waiting and preparing for the next action is to use {@link CompletableFuture#thenRunAsync(Runnable)}
     *
     * @return the new version string.
     * @see #sendUpdates()
     * @since 1.0.0
     */
    public CompletableFuture<String> checkForUpdates() {
        return getVersion().thenApply(result -> {
            this.latestVersion = result;
            this.canUpdate = isVersionHigher(currentVersion, result);
            return result;
        });
    }

    /**
     * You need to run {@link #checkForUpdates()} first to use this.
     *
     * @return true if there's a new version available and you should update, otherwise false.
     * @see #getLatestVersion()
     * @since 1.0.0
     */
    public boolean canUpdate() {
        return canUpdate;
    }

    /**
     * You need to run {@link #checkForUpdates()} first to use this.
     *
     * @return the latest version that was obtained from {@link #getVersion()}
     * @see #canUpdate()
     * @since 1.0.0
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Sends plugin updates to console.
     *
     * @since 1.0.0
     */
    public void sendUpdates() {
        if (!plugin.getConfig().getBoolean("check-updates")) return;

        // Use a task so it can run when the server finished loading everything.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (canUpdate)
                sendMessages(updateText());
            else if (latestVersion != null)
                sendMessages("&2No updates found &7- &8(&6v" + currentVersion + "&8)");
        });
    }

    /**
     * Notify admins about updates when they join the server.
     *
     * @since 1.0.0
     */
    @EventHandler
    public void onJoinNotify(PlayerJoinEvent event) {
        if (!canUpdate) return;
        Player player = event.getPlayer();
        if (player.hasPermission(plugin.getName().toLowerCase() + ".updates")) sendMessages(player, updateText());
    }

    /**
     * Sends a message to console.
     *
     * @see #sendMessages(CommandSender, String)
     * @since 1.0.0
     */
    private void sendMessages(@Nonnull String msg) {
        sendMessages(Bukkit.getConsoleSender(), msg);
    }

    /**
     * A simple built-in feature to send fancy messages.
     *
     * @param receiver the receiver which is either a player or the console. Null also means console.
     * @param msg      the message that is going to be sent to the receiver.
     *
     * @since 1.0.0
     */
    private void sendMessages(@Nonnull CommandSender receiver, @Nonnull String msg) {
        String lastColor = "";
        for (String string : StringUtils.splitPreserveAllTokens(msg, '\n')) {
            string = lastColor + ChatColor.translateAlternateColorCodes('&', prefix + string);
            receiver.sendMessage(string);
            lastColor = ChatColor.getLastColors(string);
        }
    }

    /**
     * @return the update text used when an update is available.
     * @since 1.0.0
     */
    public String updateText() {
        return "&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-" +
                "\n  &2There is an update available!" +
                "\n  &2Current Version&8: &6v" + currentVersion +
                "\n  &2Latest Version&8: &6v" + latestVersion + '\n' +
                "&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
    }

    /**
     * Gets the version of the Spigot plugin resource asynchronous.
     *
     * @return the plugin's version or null if no response or empty version string.
     * @see #checkForUpdates()
     * @since 1.0.0
     */
    public CompletableFuture<String> getVersion() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(REQUEST_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(2000); // Two seconds seems fair and should not be changed. Min is 1sec, Max is 2secs.
                con.setReadTimeout(2000); // Freezes offline servers if not set.
                con.setDoOutput(true);

                sendMessages("&2Checking for updates...");
                lastHttpResponseCode = con.getResponseCode();
                try (InputStream response = con.getResponseCode() == HttpURLConnection.HTTP_OK ? con.getInputStream() : con.getErrorStream()) {
                    String version;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8))) {
                        version = reader.readLine();
                    }
                    return version.isEmpty() ? null : version;
                }
            } catch (IOException ex) {
                sendMessages("&cFailed to check for updates&8: &e" + ex.getMessage() + " (internet connection problems?)");
                if (plugin.getConfig().getBoolean("debug")) ex.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Downloads any available update to plugin's data folder using <a href="https://spiget.org">SpiGet</a> API asynchronous.<br>
     * <b>Only works with free plugins.</b> Don't waste your time making one for premium ones.
     * Uses HTTP request.
     *
     * @since 1.0.0
     */
    @Nonnull
    public CompletableFuture<Void> downloadUpdate() {
        return CompletableFuture.runAsync(() -> {
            try {
                // http://aqua.api.spiget.org/v2/resources/ID/download/
                // We need to send a HTTP request not HTTPS. It's just Minecraft...
                // We don't need to cache anything here. This request is meant to be sent once.
                URL link = new URL("http://api.spiget.org/v2/resources/" + $RESOURCE_ID + "/download/");
                ReadableByteChannel readChan = Channels.newChannel(link.openStream());
                FileOutputStream output = new FileOutputStream(download);
                FileChannel writeChan = output.getChannel();
                writeChan.transferFrom(readChan, 0L, Integer.MAX_VALUE);

                writeChan.close();
                output.close();
                readChan.close();
                sendMessages("&2Successfully downloaded the plugin. Check the plugin folder.");
            } catch (IOException ex) {
                sendMessages("&cFailed to download the plugin&8:");
                ex.printStackTrace();
            }
        });
    }
}