package com.cavetale.pvparena;

import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitTask;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Store all available worlds and manage votes.
 */
@RequiredArgsConstructor
public final class PVPArenaMaps {
    private final PVPArenaPlugin plugin;
    protected final Map<String, PVPArenaMap> maps = new HashMap<>();
    @Getter private boolean voteActive;
    protected BukkitTask task;
    protected Map<UUID, String> votes = new HashMap<>();
    protected final int maxTicks = 20 * 60;
    protected int ticksLeft;
    private final Random random = new Random();

    public void startVote() {
        voteActive = true;
        votes.clear();
        ticksLeft = maxTicks;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 1L);
        for (Player player : Bukkit.getOnlinePlayers()) {
            remindToVote(player);
        }
    }

    public void stopVote() {
        voteActive = false;
        task.cancel();
        task = null;
    }

    private void tick() {
        final int ticks = ticksLeft--;
        if (ticks <= 0) {
            finishVote();
        }
    }

    public float voteProgress() {
        return 1.0f - (float) ticksLeft / (float) maxTicks;
    }

    public void vote(UUID uuid, PVPArenaMap map) {
        votes.put(uuid, map.getPath());
    }

    public void finishVote() {
        stopVote();
        final Map<String, Integer> stats = new HashMap<>();
        final List<PVPArenaMap> randomMaps = new ArrayList<>();
        for (String it : votes.values()) {
            stats.compute(it, (s, i) -> i != null ? i + 1 : 1);
            PVPArenaMap pvpArenaMap = maps.get(it);
            if (pvpArenaMap != null) randomMaps.add(pvpArenaMap);
        }
        plugin.getLogger().info("Votes: " + stats);
        if (randomMaps.isEmpty()) randomMaps.addAll(maps.values());
        PVPArenaMap pvpArenaMap = randomMaps.get(random.nextInt(randomMaps.size()));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.sendMessage(textOfChildren(text("Map ", GRAY), text(pvpArenaMap.getDisplayName(), GREEN)));
            player.sendMessage(textOfChildren(text("By ", GRAY), text(pvpArenaMap.getDescription(), GREEN)));
            player.sendMessage("");
        }
        plugin.startGame(pvpArenaMap.getPath());
    }

    protected void load(List<String> worldNames) {
        final File creativeFile = new File("/home/cavetale/creative/plugins/Creative/worlds.yml");
        final ConfigurationSection creativeConfig = creativeFile.exists()
            ? YamlConfiguration.loadConfiguration(creativeFile)
            : new YamlConfiguration();
        maps.clear();
        for (Map<?, ?> map : creativeConfig.getMapList("worlds")) {
            ConfigurationSection worldConfig = creativeConfig.createSection("_tmp", map);
            String path = worldConfig.getString("path");
            if (!worldNames.contains(path)) continue;
            PVPArenaMap pvpArenaMap = new PVPArenaMap();
            pvpArenaMap.setPath(path);
            pvpArenaMap.setDisplayName(worldConfig.getString("name"));
            String uuidString = worldConfig.getString("owner.uuid");
            if (uuidString != null) {
                UUID uuid = UUID.fromString(uuidString);
                pvpArenaMap.setDescription(PlayerCache.nameForUuid(uuid));
            }
            maps.put(path, pvpArenaMap);
        }
        plugin.getLogger().info(maps.size() + " worlds loaded");
    }

    public void remindToVote(Player player) {
        if (!voteActive) return;
        if (!player.hasPermission("pvparena.pvparena")) return;
        player.sendMessage(textOfChildren(newline(),
                                          Mytems.ARROW_RIGHT,
                                          (text(" Click here to vote on the next map", GREEN)
                                           .hoverEvent(showText(text("Map Selection", GRAY)))
                                           .clickEvent(runCommand("/pvparena vote"))),
                                          newline()));
    }

    public void openVoteBook(Player player) {
        List<PVPArenaMap> pvpArenaMaps = new ArrayList<>();
        pvpArenaMaps.addAll(maps.values());
        Collections.sort(pvpArenaMaps, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getDisplayName(), b.getDisplayName()));
        List<Component> lines = new ArrayList<>();
        for (PVPArenaMap pvpArenaMap : pvpArenaMaps) {
            List<Component> tooltip = new ArrayList<>();
            String raw = pvpArenaMap.getDisplayName();
            if (raw.length() > 16) raw = raw.substring(0, 16);
            Component displayName = text(raw, BLUE);
            tooltip.add(displayName);
            tooltip.addAll(Text.wrapLore(pvpArenaMap.getDescription(), c -> c.color(GRAY)));
            lines.add(displayName
                      .hoverEvent(showText(join(separator(newline()), tooltip)))
                      .clickEvent(runCommand("/pvparena vote " + pvpArenaMap.getPath())));
        }
        bookLines(player, lines);
    }

    private static List<Component> toPages(List<Component> lines) {
        final int lineCount = lines.size();
        final int linesPerPage = 10;
        List<Component> pages = new ArrayList<>((lineCount - 1) / linesPerPage + 1);
        for (int i = 0; i < lineCount; i += linesPerPage) {
            List<Component> page = new ArrayList<>(14);
            page.add(textOfChildren(PVPArenaPlugin.TITLE, text(" Worlds")));
            page.add(empty());
            page.addAll(lines.subList(i, Math.min(lines.size(), i + linesPerPage)));
            pages.add(join(separator(newline()), page));
        }
        return pages;
    }

    private static void bookLines(Player player, List<Component> lines) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.editMeta(m -> {
                if (m instanceof BookMeta meta) {
                    meta.author(text("Cavetale"));
                    meta.title(text("Title"));
                    meta.pages(toPages(lines));
                }
            });
        player.closeInventory();
        player.openBook(book);
    }

    public List<String> getWorldNames() {
        List<String> result = new ArrayList<>();
        result.addAll(maps.keySet());
        return result;
    }
}
