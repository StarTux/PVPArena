package com.cavetale.pvparena;

import com.cavetale.afk.AFKPlugin;
import com.cavetale.core.event.hud.PlayerHudEvent;
import com.cavetale.core.event.hud.PlayerHudPriority;
import com.cavetale.core.event.minigame.MinigameFlag;
import com.cavetale.core.event.minigame.MinigameMatchCompleteEvent;
import com.cavetale.core.event.minigame.MinigameMatchType;
import com.cavetale.core.event.player.PlayerTeamQuery;
import com.cavetale.fam.trophy.Highscore;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.trophy.TrophyCategory;
import com.cavetale.pvparena.struct.AreasFile;
import com.cavetale.pvparena.struct.Cuboid;
import com.cavetale.pvparena.struct.Vec3i;
import com.cavetale.server.ServerPlugin;
import com.destroystokyo.paper.MaterialTags;
import com.winthier.title.TitlePlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;
import static net.kyori.adventure.text.format.TextDecoration.*;
import static net.kyori.adventure.title.Title.Times.times;

public final class PVPArenaPlugin extends JavaPlugin implements Listener {
    public static final String PERM_PLAYER = "pvparena.pvparena";
    public static final String PERM_ADMIN = "pvparena.admin";
    public static final String PERM_STREAMER = "group.streamer";
    protected static final int WARM_UP_TICKS = 20 * 30;
    protected static final int SUDDEN_DEATH_TICKS = 20 * 60 * 3;
    protected static final int TIMED_SCORE_TICKS = 20 * 60 * 5;
    protected static final int MOLE_TICKS = 20 * 60 * 5;
    protected static final int IDLE_TICKS = 20 * 30;
    protected World lobbyWorld;
    protected World world;
    protected AreasFile areasFile;
    protected Tag tag;
    protected Random random = new Random();
    protected List<Entity> removeEntities = new ArrayList<>();
    protected BossBar bossBar;
    private Set<UUID> spectators = new HashSet<>();
    protected List<Highscore> highscore = List.of();
    protected List<Component> highscoreLines = List.of();
    protected static final String HEART = "\u2764";
    public static final Component TITLE = join(noSeparators(),
                                               Mytems.LETTER_P,
                                               Mytems.LETTER_V,
                                               Mytems.LETTER_P,
                                               text("A", color(0xff2200), BOLD),
                                               text("r", color(0xff562a), BOLD),
                                               text("e", color(0xff8955), BOLD),
                                               text("n", color(0xffbd7f), BOLD),
                                               text("a", color(0xfff0a9), BOLD));
    private List<UUID> winners = List.of();

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::tick, 1, 1);
        getCommand("spectator").setExecutor(new SpectatorCommand(this));
        loadTag();
        if (tag.worldName != null) {
            world = getWorld(tag.worldName);
            areasFile = loadAreasFile();
        }
        lobbyWorld = Bukkit.getWorlds().get(0);
        bossBar = BossBar.bossBar(text("PVPArena", RED), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        new PVPAdminCommand(this).enable();
        for (Player player : Bukkit.getOnlinePlayers()) {
            enter(player);
        }
    }

    @Override
    public void onDisable() {
        saveTag();
        for (Player player : Bukkit.getOnlinePlayers()) {
            exit(player);
        }
        cleanUpGame();
        ServerPlugin.getInstance().setServerSidebarLines(null);
    }

    protected void enter(Player player) {
        if (tag.state == ArenaState.PLAY && tag.useSquads) {
            Gladiator gladiator = getGladiator(player);
            if (gladiator != null) {
                TitlePlugin.getInstance().setColor(player, tag.squads.get(gladiator.squad).getTextColor());
            }
        }
    }

    protected void exit(Player player) {
    }

    protected void loadTag() {
        tag = Json.load(new File(getDataFolder(), "save.json"), Tag.class, Tag::new);
        computeHighscore();
    }

    protected void saveTag() {
        Json.save(new File(getDataFolder(), "save.json"), tag);
    }

    private void log(String msg) {
        getLogger().info("[" + tag.worldName + "] [" + tag.state + "] " + msg);
    }

    private void logSevere(String msg) {
        getLogger().severe("[" + tag.worldName + "] [" + tag.state + "] " + msg);
    }

    private void tick() {
        if (tag.state != ArenaState.IDLE && getEligible().isEmpty()) {
            ServerPlugin.getInstance().setServerSidebarLines(null);
            log("Eligible is empty");
            setIdle();
            return;
        }
        switch (tag.state) {
        case IDLE: tickIdle(); break;
        case END: tickEnd(); break;
        case PLAY:
            tickPlay();
            tag.gameTime += 1;
            break;
        default: break;
        }
    }

    protected void setIdle() {
        if (tag.state == ArenaState.IDLE) return;
        log("State IDLE");
        tag.state = ArenaState.IDLE;
        tag.idleTime = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.getWorld().equals(lobbyWorld)) {
                teleport(target, lobbyWorld.getSpawnLocation());
            }
            resetPlayer(target);
        }
    }

    private static float clampProgress(float in) {
        return Math.max(0.0f, Math.min(1.0f, in));
    }

    private String players(int count) {
        return count == 1 ? "1 player" : count + " players";
    }

    private String teams(int count) {
        return count == 1 ? "1 team" : count + " teams";
    }

    private void tickIdle() {
        if (tag.event) {
            tag.idleTime = 0;
            bossBar.name(text("Preparing for Event...", RED));
            bossBar.progress(0.0f);
            ServerPlugin.getInstance().setServerSidebarLines(List.of(new Component[] {
                        text("/pvparena", YELLOW),
                        text("Preparing Event", GRAY),
                    }));
            return;
        }
        int eligible = getEligible().size();
        bossBar.name(textOfChildren(text("Waiting for Players: ", GRAY),
                                    text(eligible, GREEN)));
        bossBar.progress(clampProgress((float) tag.idleTime / (float) IDLE_TICKS));
        if (eligible >= 1) {
            ServerPlugin.getInstance().setServerSidebarLines(List.of(new Component[] {
                        text("/pvparena", YELLOW),
                        text(players(eligible) + " waiting", GRAY),
                    }));
        } else {
            ServerPlugin.getInstance().setServerSidebarLines(null);
        }
        if (eligible < 2) {
            tag.idleTime = 0;
            return;
        }
        if (tag.idleTime > IDLE_TICKS) {
            startGame();
        }
        tag.idleTime += 1;
    }

    private void tickPlay() {
        List<Gladiator> aliveGladiators = new ArrayList<>();
        List<Squad> aliveSquads = new ArrayList<>();
        if (tag.useSquads) {
            for (Squad it : tag.squads) {
                it.alive = 0;
            }
        }
        List<Gladiator> gladiators = new ArrayList<>(tag.gladiators.values());
        Collections.shuffle(gladiators);
        for (Gladiator it : gladiators) {
            Player player = it.getPlayer();
            if (player == null) {
                tag.gladiators.remove(it.uuid);
                continue;
            }
            tickPlayer(it, player);
            if (!it.gameOver) {
                aliveGladiators.add(it);
                if (tag.useSquads) {
                    Squad squad = tag.squads.get(it.squad);
                    squad.alive += 1;
                    if (squad.alive == 1) aliveSquads.add(squad);
                }
            }
        }
        int aliveCount = tag.useSquads ? aliveSquads.size() : aliveGladiators.size();
        if (tag.warmUp) {
            int seconds = (WARM_UP_TICKS - tag.gameTime) / 20;
            bossBar.name(text("PvP begins in " + seconds + "s", RED));
            bossBar.progress(clampProgress((float) tag.gameTime / (float) WARM_UP_TICKS));
        } else if (tag.suddenDeath) {
            bossBar.name(text("Sudden Death " + aliveGladiators.size() + "/" + tag.totalPlayers, DARK_RED));
            bossBar.progress(1.0f);
        } else {
            bossBar.name(text("Fight " + aliveGladiators.size() + "/" + tag.totalPlayers, RED));
            final float progress;
            switch (tag.winRule) {
            case TIMED_SCORE:
                progress = clampProgress((float) tag.gameTime / (float) TIMED_SCORE_TICKS);
                break;
            case MOLE:
                progress = clampProgress((float) tag.gameTime / (float) MOLE_TICKS);
                break;
            case LAST_SURVIVOR:
            default:
                progress = clampProgress((float) tag.gameTime / (float) SUDDEN_DEATH_TICKS);
                break;
            }
            bossBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));
        }
        ServerPlugin.getInstance().setServerSidebarLines(List.of(new Component[] {
                    text("/pvparena", YELLOW),
                    text((tag.useSquads ? teams(aliveCount) : players(aliveCount)) + " fighting", GRAY),
                }));
        if (aliveCount == 0) {
            log("Game Draw");
            for (Player target : world.getPlayers()) {
                target.showTitle(Title.title(text("Draw", RED),
                                             text("Nobody survives", RED)));
                target.sendMessage(text("Draw! Nobody survives", RED));
            }
            endGame();
            return;
        }
        if (tag.winRule == WinRule.LAST_SURVIVOR) {
            if (aliveCount == 1 && !tag.debug) {
                if (tag.useSquads) {
                    squadWinsTheGame(aliveSquads.get(0));
                } else {
                    playerWinsTheGame(aliveGladiators.get(0));
                }
                return;
            }
            if (tag.gameTime == SUDDEN_DEATH_TICKS) {
                for (Player target : world.getPlayers()) {
                    target.sendMessage(text("Sudden Death!", DARK_RED));
                    target.showTitle(Title.title(empty(),
                                                 text("Sudden Death!", DARK_RED),
                                                 times(Duration.ZERO,
                                                       Duration.ofSeconds(1),
                                                       Duration.ZERO)));
                    target.playSound(target.getLocation(), Sound.BLOCK_BELL_USE, SoundCategory.MASTER, 0.2f, 0.7f);
                }
                tag.suddenDeath = true;
            }
        }
        boolean timesUp = (tag.winRule == WinRule.TIMED_SCORE && tag.gameTime > TIMED_SCORE_TICKS)
            || (tag.winRule == WinRule.MOLE && tag.gameTime > MOLE_TICKS);
        if (timesUp) {
            if (tag.useSquads) {
                Collections.sort(aliveSquads, (b, a) -> Integer.compare(a.score, b.score));
                int maxScore = aliveSquads.get(0).score;
                List<Squad> winnerSquads = new ArrayList<>();
                for (Squad squad : aliveSquads) {
                    if (squad.score < maxScore) break;
                    winnerSquads.add(squad);
                }
                if (winnerSquads.size() == 1) {
                    squadWinsTheGame(winnerSquads.get(0));
                } else {
                    squadsDraw(winnerSquads);
                }
            } else {
                Collections.sort(aliveGladiators, (b, a) -> Integer.compare(a.score, b.score));
                int maxScore = aliveGladiators.get(0).score;
                List<Gladiator> winnerPlayers = new ArrayList<>();
                for (Gladiator gladiator : aliveGladiators) {
                    if (gladiator.score < maxScore) break;
                    winnerPlayers.add(gladiator);
                }
                if (winnerPlayers.size() == 1) {
                    playerWinsTheGame(winnerPlayers.get(0));
                } else {
                    playersDraw(winnerPlayers);
                }
            }
            return;
        }
        if (tag.winRule == WinRule.MOLE) {
            if (tag.moleUuid == null || tag.gladiators.get(tag.moleUuid) == null) {
                tag.moleUuid = aliveGladiators.get(random.nextInt(aliveGladiators.size())).uuid;
            } else {
                Player mole = Bukkit.getPlayer(tag.moleUuid);
                if (mole != null) {
                    mole.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1, true, false, true));
                }
            }
        }
        if (tag.gameTime == WARM_UP_TICKS) {
            log("Warmup End");
            tag.warmUp = false;
            world.setPVP(true);
            world.setGameRule(GameRule.FALL_DAMAGE, true);
            for (Player target : world.getPlayers()) {
                target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.2f, 1.2f);
                target.showTitle(Title.title(text("Fight!", DARK_RED),
                                             text(tag.specialRule.displayName, RED),
                                             times(Duration.ZERO,
                                                   Duration.ofSeconds(1),
                                                   Duration.ZERO)));
                target.sendMessage(text("Fight! " + tag.specialRule.displayName, DARK_RED));
                target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 0.1f, 1.2f);
            }
        }
        if (tag.suddenDeath && tag.gameTime % 40 == 0) {
            for (Player target : world.getPlayers()) {
                if (target.getGameMode() != GameMode.SPECTATOR) {
                    target.damage(1.0);
                }
            }
        }
        if (!tag.warmUp && tag.gameTime % 20 == 0 && tag.specialRule == SpecialRule.ZOMBIECALYPSE) {
            List<Player> alives = getAlive();
            int zombieCount = 0;
            for (Entity entity : removeEntities) {
                if (entity instanceof Zombie) zombieCount += 1;
            }
            for (Player player : alives) {
                if (zombieCount >= 30) break;
                Location loc = player.getLocation();
                Vector vec = new Vector(random.nextDouble(), 0.0, random.nextDouble());
                vec = vec.normalize().multiply(10.0);
                loc = loc.add(vec);
                Block block = loc.getBlock();
                for (int i = 0; i < 4; i += 1) {
                    if (!block.isEmpty()) break;
                    block = block.getRelative(0, -1, 0);
                }
                for (int i = 0; i < 4; i += 1) {
                    if (block.isEmpty()) break;
                    block = block.getRelative(0, 1, 0);
                }
                if (!block.isEmpty() || !block.getRelative(0, 1, 0).isEmpty()) continue;
                if (!block.getRelative(0, -1, 0).isSolid()) continue;
                loc = block.getLocation().add(0.5, 0.0, 0.5);
                boolean tooClose = false;
                for (Player nearby : alives) {
                    if (loc.distanceSquared(nearby.getLocation()) < 64.0) {
                        tooClose = true;
                        break;
                    }
                }
                if (tooClose) continue;
                Zombie zombie = loc.getWorld().spawn(loc, Zombie.class, z -> {
                        z.setHealth(1.0);
                        z.setPersistent(false);
                        z.setRemoveWhenFarAway(true);
                        z.setShouldBurnInDay(false);
                    });
                removeEntities.add(zombie);
                zombieCount += 1;
            }
        }
        if (!tag.warmUp && (tag.gameTime % 200) == 0) {
            Location location = findSpawnLocation();
            ItemStack item;
            switch (random.nextInt(3)) {
            case 0: item = spawnWeapon(); break;
            case 1: item = spawnArmor(); break;
            case 2: item = spawnArrows(); break;
            default: item = null;
            }
            if (item != null) {
                Item entity = world.dropItem(location, item);
                if (entity != null) {
                    log(item.getType() + " dropped at " + location.getBlockX()
                        + "," + location.getBlockY()
                        + "," + location.getBlockZ());
                    entity.setPersistent(false);
                    removeEntities.add(entity);
                }
            }
        }
        removeEntities.removeIf(e -> !e.isValid());
    }

    private void playerWinsTheGame(final Gladiator winnerPlayer) {
        log("Winner " + winnerPlayer.getName());
        for (Player target : world.getPlayers()) {
            target.showTitle(Title.title(text(winnerPlayer.getName(), GREEN),
                                         text("Wins this round!", GREEN)));
            target.sendMessage(text(winnerPlayer.getName() + " wins this round!", GREEN));
            target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.1f, 2.0f);
        }
        if (tag.event) {
            rewardEventWinner(winnerPlayer);
        }
        this.winners = List.of(winnerPlayer.uuid);
        endGame();
    }

    private void playersDraw(List<Gladiator> drawers) {
        String[] names = new String[drawers.size()];
        for (int i = 0; i < names.length; i += 1) {
            names[i] = drawers.get(i).getName();
        }
        log("Draw: " + String.join(" ", names));
        for (Player target : world.getPlayers()) {
            target.showTitle(Title.title(text("Draw", RED),
                                         text(String.join(", ", names), RED)));
            target.sendMessage(text("The game is a draw between " + String.join(", ", names), GREEN));
            target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.MASTER, 0.1f, 2.0f);
        }
        if (tag.event) {
            for (Gladiator drawer : drawers) {
                rewardEventWinner(drawer);
            }
        }
        this.winners = List.of();
        endGame();
    }

    private void squadWinsTheGame(Squad winner) {
        List<Gladiator> gladiators = new ArrayList<>();
        this.winners = new ArrayList<>();
        for (Gladiator gladiator : tag.gladiators.values()) {
            if (gladiator.squad != winner.index) continue;
            gladiators.add(gladiator);
            this.winners.add(gladiator.uuid);
        }
        log("Team " + winner.name + " Victory: " + gladiators.stream().map(g -> g.name).collect(Collectors.joining(" ")));
        Title title = Title.title(text(winner.name, winner.getTextColor()),
                                  text("Wins this round!", winner.getTextColor()));
        Component message = join(JoinConfiguration.noSeparators(),
                                 text(winner.name + " wins this round: ", winner.getTextColor()),
                                 join(JoinConfiguration.separator(text(", ", GRAY)),
                                      gladiators.stream()
                                      .map(g -> text(g.name, WHITE))
                                      .collect(Collectors.toList())));
        for (Player target : world.getPlayers()) {
            target.sendMessage("");
            target.showTitle(title);
            target.sendMessage(message);
            target.sendMessage("");
            target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.1f, 2.0f);
        }
        if (tag.event) {
            for (Gladiator gladiator : gladiators) {
                rewardEventWinner(gladiator);
            }
        }
        endGame();
    }

    protected void squadsDraw(final List<Squad> winnerSquads) {
        List<Gladiator> gladiators = new ArrayList<>();
        for (Squad winner : winnerSquads) {
            for (Gladiator gladiator : tag.gladiators.values()) {
                if (gladiator.squad != winner.index) continue;
                gladiators.add(gladiator);
            }
        }
        log("Teams "
            + winnerSquads.stream().map(s -> s.name).collect(Collectors.joining(" "))
            + " Victory: "
            + gladiators.stream().map(g -> g.name).collect(Collectors.joining(" ")));
        Title title = Title.title(text("Draw!", GRAY),
                                  empty());
        Component message = join(JoinConfiguration.noSeparators(),
                                 join(JoinConfiguration.separator(text(", ", GRAY)),
                                      winnerSquads.stream()
                                      .map(sq -> text(sq.name, sq.getTextColor()))
                                      .collect(Collectors.toList())),
                                 text(" draw this round: ", GRAY),
                                 join(JoinConfiguration.separator(text(", ", GRAY)),
                                      gladiators.stream()
                                      .map(g -> text(g.name, WHITE))
                                      .collect(Collectors.toList())));
        for (Player target : world.getPlayers()) {
            target.sendMessage("");
            target.showTitle(title);
            target.sendMessage(message);
            target.sendMessage("");
            target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.1f, 2.0f);
        }
        this.winners = List.of();
        endGame();
    }

    protected void rewardEventWinner(Gladiator gladiator) {
        List<String> titles = List.of("Champion", "Slayer", "IronSword", "GoldenSword", "DiamondSword", "NetheriteSword");
        String cmd = "titles unlockset " + gladiator.name + " " + String.join(" ", titles);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    void tickPlayer(Gladiator gladiator, Player player) {
        if (!gladiator.gameOver && gladiator.dead) {
            long now = System.currentTimeMillis();
            if (now > gladiator.respawnCooldown) {
                gladiator.dead = false;
                gladiator.invulnerable = now + 1000L;
                respawn(player);
            } else {
                int seconds = (int) ((gladiator.respawnCooldown - now) / 1000L);
                if (gladiator.respawnCooldownDisplay > seconds) {
                    gladiator.respawnCooldownDisplay = seconds;
                    player.showTitle(Title.title(text((seconds + 1), DARK_RED, BOLD),
                                                 text("Get Ready!", DARK_RED),
                                                 times(Duration.ofMillis(0),
                                                       Duration.ofMillis(500),
                                                       Duration.ofMillis(500))));
                }
            }
        } else {
            gladiator.health = player.getHealth();
        }
    }

    private void tickEnd() {
        ServerPlugin.getInstance().setServerSidebarLines(List.of(new Component[] {
                    text("/pvparena", YELLOW),
                    text("Game Over"),
                }));
        tag.endTime += 1;
        if (tag.endTime > (30 * 20)) {
            if (getEligible().size() < 2) {
                setIdle();
            } else {
                startGame();
            }
        }
    }

    @Nullable Gladiator getGladiator(Player player) {
        return tag.gladiators.get(player.getUniqueId());
    }

    public Squad getSquad(Player player) {
        if (!tag.useSquads) return null;
        Gladiator gladiator = getGladiator(player);
        if (gladiator == null) return null;
        return tag.squads.get(gladiator.squad);
    }

    public int getScore(Player player) {
        Gladiator gladiator = getGladiator(player);
        return gladiator != null ? gladiator.score : 0;
    }

    private void ensureWorldIsLoaded() {
        if (tag.worldName != null && tag.worldUsed < 1) return;
        tag.worldUsed = 0;
        if (tag.worlds.isEmpty()) {
            tag.worlds = new ArrayList<>(getConfig().getStringList("worlds"));
            Collections.shuffle(tag.worlds);
        }
        if (tag.worlds.isEmpty()) throw new IllegalStateException("No worlds!");
        tag.worldName = tag.worlds.remove(tag.worlds.size() - 1);
        log("Pick World: " + tag.worldName);
        world = getWorld(tag.worldName);
        areasFile = loadAreasFile();
    }

    protected void startGame() {
        ensureWorldIsLoaded();
        List<WinRule> wins = new ArrayList<>();
        int total = 0;
        for (WinRule win : WinRule.values()) {
            for (int i = 0; i < win.weight; i += 1) {
                wins.add(win);
            }
        }
        tag.winRule = wins.get(random.nextInt(wins.size()));
        List<SpecialRule> rules = new ArrayList<>(Arrays.asList(SpecialRule.values()));
        rules.remove(SpecialRule.NONE);
        tag.specialRule = rules.get(random.nextInt(rules.size()));
        final WorldBorder originalWorldBorder = world.getWorldBorder();
        final WorldBorder fakeWorldBorder = Bukkit.createWorldBorder();
        fakeWorldBorder.setCenter(originalWorldBorder.getCenter());
        fakeWorldBorder.setSize(fakeWorldBorder.getMaxSize());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(world)) {
                teleport(player, world.getSpawnLocation());
                player.setWorldBorder(fakeWorldBorder);
            }
            if (!spectators.contains(player.getUniqueId()) && AFKPlugin.isAfk(player)) {
                spectators.add(player.getUniqueId());
                player.sendMessage(text("You were marked as spectator due to inactivity", YELLOW));
            }
            if (spectators.contains(player.getUniqueId())) {
                preparePlayer(player);
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
        tag.gladiators.clear();
        tag.limitedLives = tag.winRule == WinRule.LAST_SURVIVOR;
        List<Player> eligible = getEligible();
        Collections.shuffle(eligible);
        tag.moleUuid = tag.winRule == WinRule.MOLE ? eligible.get(random.nextInt(eligible.size())).getUniqueId() : null;
        tag.useSquads = random.nextInt(eligible.size()) > 0;
        if (tag.useSquads) {
            List<Integer> spawnIndexes = new ArrayList<>();
            for (int i = 0; i < areasFile.getAreas().getSpawn().size(); i += 1) {
                spawnIndexes.add(i);
            }
            if (spawnIndexes.isEmpty()) spawnIndexes = Arrays.asList(0);
            Collections.shuffle(spawnIndexes);
            List<NamedTextColor> squadColors = Arrays.asList(new NamedTextColor[] {
                    RED,
                    BLUE,
                    GOLD,
                    GREEN,
                    AQUA,
                    LIGHT_PURPLE,
                    YELLOW,
                });
            //Collections.shuffle(squadColors);
            int squadCount;
            squadCount = 2; //eligible.size() < 12 ? 2 : 3;
            squadCount = Math.min(squadCount, squadColors.size());
            if (areasFile.getAreas().getSpawn().size() < squadCount) {
                logSevere("Fewer spawn areas than squads: " + areasFile.getAreas().getSpawn().size() + "/" + squadCount);
                squadCount = Math.min(squadCount, areasFile.getAreas().getSpawn().size());
            }
            tag.squads = new ArrayList<>();
            for (int i = 0; i < squadCount; i += 1) {
                Squad squad = new Squad();
                NamedTextColor color = squadColors.get(i);
                squad.setTextColor(color);
                String name = NamedTextColor.NAMES.key(color);
                if (name.startsWith("light_")) name = name.substring(6);
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                squad.name = name;
                squad.index = i;
                squad.spawn = spawnIndexes.get(i % spawnIndexes.size());
                tag.squads.add(squad);
            }
        } else {
            tag.squads = null;
        }
        for (Player target : eligible) {
            resetPlayer(target);
            giveGear(target);
            Bukkit.getScheduler().runTaskLater(this, () -> target.setInvisible(true), 1L);
            Bukkit.getScheduler().runTaskLater(this, () -> target.setInvisible(false), 2L);
            Gladiator gladiator = new Gladiator(target);
            gladiator.lives = tag.limitedLives ? 3 : 0;
            tag.gladiators.put(target.getUniqueId(), gladiator);
            if (tag.useSquads) {
                Squad theSquad = tag.squads.get(0);
                for (Squad it : tag.squads) {
                    if (it.memberCount < theSquad.memberCount) {
                        theSquad = it;
                    }
                }
                gladiator.squad = theSquad.index;
                theSquad.memberCount += 1;
                TitlePlugin.getInstance().setColor(target, theSquad.getTextColor());
                log(target.getName() + " in team " + gladiator.squad);
            }
            if (tag.event) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ml add " + target.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "titles unlockset " + target.getName() + " Gladiator");
            }
            target.teleport(findSpawnLocation(target), TeleportCause.PLUGIN);
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.sendMessage("");
            target.sendMessage(join(noSeparators(),
                                    text(tag.winRule.displayName, RED, BOLD),
                                    text(" " + tag.winRule.getDescription(), WHITE)));
            target.sendMessage(join(noSeparators(),
                                    text("Special: ", GRAY),
                                    text(tag.specialRule.displayName, WHITE)));
            if (tag.winRule == WinRule.MOLE && Objects.equals(target.getUniqueId(), tag.moleUuid)) {
                target.showTitle(Title.title(empty(),
                                             text("You are the mole!", RED)));
                target.sendMessage(text("You are the mole!", RED));
            }
            target.sendMessage("");
        }
        log("State PLAY");
        tag.state = ArenaState.PLAY;
        log("Warmup Start");
        world.setPVP(false);
        world.setGameRule(GameRule.FALL_DAMAGE, false);
        tag.gameTime = 0;
        tag.suddenDeath = false;
        tag.warmUp = true;
        tag.totalPlayers = eligible.size();
        tag.worldUsed += 1;
    }

    protected void resetPlayer(Player target) {
        target.getInventory().clear();
        target.setGameMode(GameMode.ADVENTURE);
        preparePlayer(target);
    }

    protected void preparePlayer(Player target) {
        target.setHealth(20.0);
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setArrowsInBody(0);
        target.setInvisible(false);
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }
    }

    protected void endGame() {
        if (tag.state != ArenaState.PLAY) return;
        cleanUpGame();
        tag.endTime = 0;
        log("State END");
        tag.state = ArenaState.END;
        saveTag();
        do {
            MinigameMatchCompleteEvent event = new MinigameMatchCompleteEvent(MinigameMatchType.PVP_ARENA);
            if (tag.event) event.addFlags(MinigameFlag.EVENT);
            for (Gladiator gladiator : tag.gladiators.values()) {
                event.addPlayerUuid(gladiator.uuid);
            }
            event.addWinnerUuids(winners);
            event.callEvent();
        } while (false);
    }

    protected void cleanUpGame() {
        for (Entity e : removeEntities) e.remove();
        for (Player p : Bukkit.getOnlinePlayers()) {
            TitlePlugin.getInstance().setColor(p, null);
        }
        removeEntities.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerDeath(PlayerDeathEvent event) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.sendActionBar(event.deathMessage());
        }
        event.deathMessage(null);
        event.getDrops().clear();
        Player player = event.getEntity();
        Gladiator gladiator = getGladiator(player);
        if (gladiator == null) return;
        if (tag.specialRule == SpecialRule.CREEPER_REVENGE) {
            Creeper creeper = player.getWorld().spawn(player.getLocation(), Creeper.class, c -> {
                    c.setPersistent(false);
                    c.setRemoveWhenFarAway(true);
                    c.setPowered(true);
                    c.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(10.0);
                    c.setHealth(10.0);
                });
            removeEntities.add(creeper);
        }
        if (tag.limitedLives) {
            if (gladiator.lives <= 1) {
                gladiator.lives = 0;
                gladiator.gameOver = true;
                player.showTitle(Title.title(text("Game Over", DARK_RED),
                                             text("Wait for the next round", DARK_RED)));
            } else {
                gladiator.lives -= 1;
                if (gladiator.lives == 1) {
                    player.showTitle(Title.title(text("One Life", BLUE),
                                                 text("Last Chance", BLUE)));
                } else {
                    player.showTitle(Title.title(text(gladiator.lives + " Lives", BLUE),
                                                 text("Respawn soon!", BLUE)));
                }
            }
        }
        event.setCancelled(true);
        for (Entity entity : removeEntities) {
            if (entity instanceof Tameable) {
                Tameable tameable = (Tameable) entity;
                if (player.equals(tameable.getOwner())) {
                    entity.remove();
                }
            }
        }
        preparePlayer(player);
        player.setGameMode(GameMode.SPECTATOR);
        if (player.getLastDamageCause() instanceof EntityDamageEvent) {
            EntityDamageEvent lastDamage = (EntityDamageEvent) player.getLastDamageCause();
            switch (lastDamage.getCause()) {
            case VOID:
                player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
            default: break;
            }
        }
        gladiator.dead = true;
        gladiator.deaths += 1;
        gladiator.respawnCooldown = System.currentTimeMillis() + 5000;
        gladiator.respawnCooldownDisplay = 3;
        Player killer = player.getKiller();
        Gladiator gladiator2 = killer != null && !killer.equals(player)
            ? getGladiator(killer)
            : null;
        if (gladiator2 != null) {
            if (tag.specialRule == SpecialRule.ENCHANT_ON_KILL) {
                for (ItemStack item : killer.getInventory()) {
                    if (item == null || item.getType() == Material.AIR) continue;
                    enchant(item);
                }
                killer.sendMessage(text("Your gear was improved", GOLD));
            }
            if (tag.specialRule == SpecialRule.HEAL_ON_KILL) {
                killer.setHealth(killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                killer.sendMessage(text("You've been healed", GOLD));
            }
            if (tag.specialRule == SpecialRule.GEAR_ON_KILL) {
                ItemStack item;
                switch (random.nextInt(3)) {
                case 0: item = spawnWeapon(); break;
                case 1: item = spawnArmor(); break;
                case 2: item = spawnArrows(); break;
                default: item = null;
                }
                if (item != null) giveItem(player, item);
                killer.sendMessage(text("You received extra gear", GOLD));
            }
            if (tag.specialRule == SpecialRule.POTION_ON_KILL) {
                List<PotionEffectType> pts = Arrays.asList(PotionEffectType.INCREASE_DAMAGE,
                                                           PotionEffectType.ABSORPTION,
                                                           PotionEffectType.HEALTH_BOOST,
                                                           PotionEffectType.INVISIBILITY,
                                                           PotionEffectType.DAMAGE_RESISTANCE);
                PotionEffectType potion = pts.get(random.nextInt(pts.size()));
                killer.addPotionEffect(new PotionEffect(potion, 20 * 30, 1, true, false, true));
                killer.sendMessage(text("You received a potion effect!", GOLD));
            }
            gladiator2.kills += 1;
            if (tag.event) {
                tag.addScore(gladiator2.uuid, 1);
                computeHighscore();
            }
            if (tag.winRule == WinRule.MOLE) {
                if (Objects.equals(tag.moleUuid, gladiator.uuid)) {
                    // Mole got killed
                    gladiator2.score += 1;
                    if (tag.useSquads) {
                        tag.squads.get(gladiator2.squad).score += 1;
                    }
                    if (!killer.equals(player)) {
                        tag.moleUuid = gladiator2.uuid;
                        killer.setHealth(killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        killer.showTitle(Title.title(empty(),
                                                     text("You are the mole!", RED)));
                        killer.sendMessage(text("You are the mole!", RED));
                    } else {
                        tag.moleUuid = null;
                    }
                    player.sendMessage(text("You are no longer the mole", RED));
                } else if (Objects.equals(tag.moleUuid, gladiator2.uuid)) {
                    // Killer is mole: mole killed someone
                    gladiator2.score += 1;
                    if (tag.useSquads) {
                        tag.squads.get(gladiator2.squad).score += 1;
                    }
                }
            } else {
                gladiator2.score += 1;
                if (tag.useSquads) {
                    tag.squads.get(gladiator2.squad).score += 1;
                }
            }
            log(killer.getName() + " killed " + player.getName());
        } else {
            if (tag.winRule == WinRule.MOLE && Objects.equals(gladiator.uuid, tag.moleUuid)) {
                player.sendMessage(text("You are no longer the mole", RED));
                tag.moleUuid = null;
            }
        }
        if (tag.specialRule == SpecialRule.SHUFFLE_ON_KILL && System.currentTimeMillis() > tag.shuffleCooldown) {
            tag.shuffleCooldown = System.currentTimeMillis() + 10000L;
            Bukkit.getScheduler().runTask(this, () -> {
                    List<Location> locs = new ArrayList<>();
                    List<Player> alives = getAlive();
                    for (Player alive : alives) {
                        locs.add(alive.getLocation());
                    }
                    Collections.shuffle(locs, random);
                    int i = 0;
                    for (Player alive : alives) {
                        alive.teleport(locs.get(i++), TeleportCause.PLUGIN);
                    }
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        target.sendMessage(text("Shuffle!", GREEN));
                    }
                });
        }
    }

    protected List<Vec3i> findSpawnVectors() {
        if (areasFile.getAreas().getSpawn().isEmpty()) {
            return Arrays.asList(Vec3i.of(world.getSpawnLocation()));
        }
        Set<Vec3i> result = new HashSet<>();
        for (Cuboid cuboid : areasFile.getAreas().getSpawn()) {
            result.addAll(cuboid.enumerate());
        }
        return new ArrayList<>(result);
    }

    protected Vec3i findSpawnVector() {
        List<Vec3i> blocks = findSpawnVectors();
        if (blocks.size() == 1) return blocks.get(0);
        List<Vec3i> players = new ArrayList<>();
        for (Gladiator gladiator : tag.gladiators.values()) {
            if (gladiator.dead || gladiator.gameOver) continue;
            Player player = gladiator.getPlayer();
            if (player == null) continue;
            players.add(Vec3i.of(player.getLocation()));
        }
        if (players.isEmpty()) {
            return blocks.get(random.nextInt(blocks.size()));
        }
        Vec3i result = null;
        int maxDist = 0;
        // Goal: Find the spawn location farthest away from any other player.
        for (Vec3i block : blocks) {
            int minDist = Integer.MAX_VALUE;
            // Find distance to closest player
            for (Vec3i player : players) {
                int dist = player.distanceSquared(block);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
            // Update result if farther than best result
            if (result == null || minDist > maxDist) {
                maxDist = minDist;
                result = block;
            }
        }
        return result;
    }

    protected Location findSpawnLocation() {
        Vec3i vector = findSpawnVector();
        Location location = world.getBlockAt(vector.x, vector.y, vector.z).getLocation();
        location = location.add(0.5, 0.1, 0.5);
        location.setYaw((float) (random.nextDouble() * 360.0));
        return location;
    }

    protected Location findSpawnLocation(Player player) {
        if (!tag.useSquads) return findSpawnLocation();
        Gladiator gladiator = getGladiator(player);
        if (gladiator == null) return findSpawnLocation();
        if (areasFile.getAreas().getSpawn().isEmpty()) return findSpawnLocation();
        Squad squad = tag.squads.get(gladiator.squad);
        Cuboid cuboid = areasFile.getAreas().getSpawn().get(squad.spawn);
        List<Vec3i> vecs = cuboid.enumerate();
        Vec3i vector = vecs.get(random.nextInt(vecs.size()));
        Location location = world.getBlockAt(vector.x, vector.y, vector.z).getLocation();
        location = location.add(0.5, 0.1, 0.5);
        location.setYaw((float) (random.nextDouble() * 360.0));
        return location;
    }

    protected void respawn(Player player) {
        Location spawnLocation = findSpawnLocation(player);
        preparePlayer(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(spawnLocation, TeleportCause.PLUGIN);
        if (tag.specialRule == SpecialRule.DEATH_BUFF) {
            for (ItemStack item : player.getInventory()) {
                if (item == null || item.getType() == Material.AIR) continue;
                enchant(item);
            }
            giveGear(player);
            player.sendMessage(text("You received extra gear!", GOLD));
        }
    }

    public List<Player> getAlive() {
        return world.getPlayers().stream()
            .filter(p -> p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)
            .filter(Player::isValid)
            .collect(Collectors.toList());
    }

    public List<Player> getEligible() {
        return Bukkit.getOnlinePlayers()
            .stream()
            .filter(p -> !p.isPermissionSet(PERM_STREAMER) || !p.hasPermission(PERM_STREAMER))
            .filter(p -> p.hasPermission(PERM_PLAYER))
            .filter(p -> !spectators.contains(p.getUniqueId()))
            .collect(Collectors.toList());
    }

    @EventHandler
    private void onPlayerHud(PlayerHudEvent event) {
        event.bossbar(PlayerHudPriority.HIGH, bossBar);
        List<Component> ls = new ArrayList<>();
        ls.add(TITLE);
        Player player = event.getPlayer();
        Gladiator playerGladiator = getGladiator(player);
        Squad playerSquad = playerGladiator != null && tag.useSquads
            ? tag.squads.get(playerGladiator.squad) : null;
        if (spectators.contains(player.getUniqueId())) {
            ls.add(text("Specating (/spec)", YELLOW));
        }
        if (tag.state == ArenaState.PLAY) {
            if (playerSquad != null) {
                ls.add(text("Team " + playerSquad.name, playerSquad.getTextColor(), TextDecoration.BOLD));
            }
            ls.add(textOfChildren(text("Win ", GRAY),
                                  text(tag.winRule.displayName, RED)));
            ls.add(textOfChildren(text("Special ", GRAY),
                                  text(tag.specialRule.displayName, RED)));
            if (tag.limitedLives && playerGladiator != null) {
                ls.add(join(JoinConfiguration.noSeparators(),
                            text("Lives ", GRAY),
                            text("" + playerGladiator.lives, RED)));
            }
            if (tag.suddenDeath) {
                ls.add(text("Sudden Death", DARK_RED, TextDecoration.BOLD));
            }
        }
        if (tag.state != ArenaState.IDLE) {
            if (tag.useSquads) {
                List<Squad> squads = new ArrayList<>(tag.squads);
                if (tag.limitedLives) {
                    Collections.sort(squads, (b, a) -> Integer.compare(a.alive, b.alive));
                    for (Squad squad : squads) {
                        ls.add(join(JoinConfiguration.noSeparators(),
                                    text(HEART + squad.alive, RED),
                                    space(),
                                    text(squad.name, squad.getTextColor())));
                    }
                } else {
                    Collections.sort(squads, (b, a) -> Integer.compare(a.score, b.score));
                    for (Squad squad : squads) {
                        ls.add(join(JoinConfiguration.noSeparators(),
                                    text("" + squad.score, WHITE),
                                    space(),
                                    text(HEART + squad.alive, RED),
                                    space(),
                                    text(squad.name, squad.getTextColor())));
                    }
                }
            } else {
                List<Gladiator> gladiators = new ArrayList<>(tag.gladiators.values());
                Collections.sort(gladiators, (b, a) -> {
                        int c = Integer.compare(!a.gameOver ? 1 : 0,
                                                !b.gameOver ? 1 : 0);
                        if (c != 0) return c;
                        return tag.winRule == WinRule.LAST_SURVIVOR
                            ? Integer.compare(a.lives, b.lives)
                            : Integer.compare(a.score, b.score);
                    });
                for (Gladiator gladiator : gladiators) {
                    if (gladiator.gameOver) {
                        ls.add(join(JoinConfiguration.noSeparators(),
                                    text("" + gladiator.score, GREEN),
                                    space(),
                                    text(gladiator.name, DARK_GRAY)));
                    } else {
                        int hearts = (int) Math.ceil(gladiator.health * 0.5);
                        TextColor nameColor;
                        if (gladiator.is(player)) {
                            nameColor = GREEN;
                        } else {
                            nameColor = WHITE;
                        }
                        if (tag.limitedLives) {
                            ls.add(join(JoinConfiguration.noSeparators(),
                                        text(HEART + hearts, RED),
                                        (gladiator.lives > 0 ? text("|" + gladiator.lives, BLUE) : empty()),
                                        space(),
                                        text(gladiator.name, nameColor)));
                        } else {
                            ls.add(join(JoinConfiguration.noSeparators(),
                                        text("" + gladiator.score, WHITE),
                                        space(),
                                        text(HEART + hearts, RED),
                                        space(),
                                        text(gladiator.name, nameColor)));
                        }
                    }
                }
            }
        } else {
            ls.add(text("Preparing Game...", RED));
        }
        if (tag.event) {
            ls.addAll(highscoreLines);
        }
        if (ls.isEmpty()) return;
        event.sidebar(PlayerHudPriority.HIGHEST, ls);
    }

    protected boolean isAlive(Player p) {
        return !p.isDead() && (p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL);
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event) {
        if (tag.state != ArenaState.PLAY || tag.gameTime < WARM_UP_TICKS) {
            event.setCancelled(true);
            return;
        }
    }

    protected static final List<Enchantment> FORBIDDEN_ENCHANTMENTS = List.of(new Enchantment[] {
            Enchantment.DURABILITY,
            Enchantment.DAMAGE_ARTHROPODS,
            Enchantment.DAMAGE_UNDEAD,
            Enchantment.DEPTH_STRIDER,
            Enchantment.DIG_SPEED,
            Enchantment.FROST_WALKER,
            Enchantment.LOOT_BONUS_BLOCKS,
            Enchantment.LOOT_BONUS_MOBS,
            Enchantment.LUCK,
            Enchantment.LURE,
            Enchantment.MENDING,
            Enchantment.OXYGEN,
            Enchantment.PROTECTION_FALL,
            Enchantment.PROTECTION_FIRE,
            Enchantment.SILK_TOUCH,
            Enchantment.SOUL_SPEED,
            Enchantment.WATER_WORKER,
        });

    protected ItemStack enchant(ItemStack item) {
        if (item.getType() == Material.TRIDENT) {
            item.addUnsafeEnchantment(Enchantment.LOYALTY, Enchantment.LOYALTY.getMaxLevel());
            item.addUnsafeEnchantment(Enchantment.IMPALING, Enchantment.IMPALING.getMaxLevel());
        } else if (item.getType() == Material.CROSSBOW) {
            item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, Enchantment.QUICK_CHARGE.getMaxLevel());
            item.addUnsafeEnchantment(Enchantment.MULTISHOT, Enchantment.MULTISHOT.getMaxLevel());
            item.addUnsafeEnchantment(Enchantment.PIERCING, Enchantment.PIERCING.getMaxLevel());
        } else if (item.getType() == Material.BOW) {
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, Enchantment.ARROW_INFINITE.getMaxLevel());
        }
        for (int i = 0; i < 10; i += 1) {
            List<Enchantment> list = new ArrayList<>();
            list.addAll(List.of(Enchantment.values()));
            list.removeAll(FORBIDDEN_ENCHANTMENTS);
            list.removeIf(enchantment -> !enchantment.canEnchantItem(item) || enchantment.isCursed());
            if (list.isEmpty()) break;
            Collections.shuffle(list);
            Enchantment enchantment = list.get(0);
            item.addUnsafeEnchantment(enchantment, enchantment.getMaxLevel());
        }
        return item;
    }

    protected void giveGear(Player player) {
        ItemStack weapon = spawnWeapon();
        giveItem(player, weapon);
        giveItem(player, spawnHelmet());
        giveItem(player, spawnChestplate());
        giveItem(player, spawnLeggings());
        giveItem(player, spawnBoots());
        giveBuffItem(player);
        giveDebuffItem(player);
        if (weapon.getType() == Material.BOW || weapon.getType() == Material.CROSSBOW) {
            giveItem(player, spawnArrows());
            giveItem(player, new ItemStack(Material.ARROW, 64));
            int dogAmount = random.nextInt(8) - random.nextInt(8);
            if (dogAmount > 0) {
                for (int i = 0; i < dogAmount; i += 1) {
                    Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class, w -> {
                            w.setTamed(true);
                            w.setOwner(player);
                            w.setPersistent(false);
                            w.setRemoveWhenFarAway(true);
                            final double health = 40.0;
                            w.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                            w.setHealth(health);
                            w.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
                            w.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5);
                        });
                    removeEntities.add(wolf);
                }
                if (dogAmount == 1) {
                    player.sendMessage(text("You got a dog!", BLUE));
                } else {
                    player.sendMessage(text("You got " + dogAmount + " dogs!", BLUE));
                }
            }
        } else {
            giveItem(player, enchant(new ItemStack(Material.SHIELD)));
        }
    }

    protected boolean setEquipment(Player player, EquipmentSlot slot, ItemStack item) {
        ItemStack old = player.getEquipment().getItem(slot);
        if (old != null && old.getType() != Material.AIR) return false;
        player.getEquipment().setItem(slot, item);
        return true;
    }

    protected boolean addInventory(Player player, ItemStack item) {
        player.getInventory().addItem(item);
        return true;
    }

    protected boolean giveItem(Player player, ItemStack item) {
        Material material = item.getType();
        if (MaterialTags.HELMETS.isTagged(material)) {
            return setEquipment(player, EquipmentSlot.HEAD, item) || addInventory(player, item);
        } else if (MaterialTags.CHESTPLATES.isTagged(material)) {
            return setEquipment(player, EquipmentSlot.CHEST, item) || addInventory(player, item);
        } else if (MaterialTags.LEGGINGS.isTagged(material)) {
            return setEquipment(player, EquipmentSlot.LEGS, item) || addInventory(player, item);
        } else if (MaterialTags.BOOTS.isTagged(material)) {
            return setEquipment(player, EquipmentSlot.FEET, item) || addInventory(player, item);
        } else if (MaterialTags.SWORDS.isTagged(material) || MaterialTags.BOWS.isTagged(material)
                   || material == Material.TRIDENT || material == Material.CROSSBOW) {
            return setEquipment(player, EquipmentSlot.HAND, item) || addInventory(player, item);
        } else if (material == Material.SHIELD) {
            return setEquipment(player, EquipmentSlot.OFF_HAND, item) || addInventory(player, item);
        } else {
            addInventory(player, item);
            return true;
        }
    }

    protected ItemStack spawnArrows() {
        switch (random.nextInt(8)) {
        case 0: case 1: case 2:
            return enchant(new ItemStack(Material.ARROW, 64));
        case 3:
            return enchant(new ItemStack(Material.SPECTRAL_ARROW, 64));
        case 4:
            return Items.potionItem(new ItemStack(Material.TIPPED_ARROW, 32), PotionType.POISON);
        case 5:
            return Items.potionItem(new ItemStack(Material.TIPPED_ARROW, 32), PotionType.INSTANT_DAMAGE);
        case 6:
            return Items.potionItem(new ItemStack(Material.TIPPED_ARROW, 64), PotionType.SLOWNESS);
        case 7:
            return Items.potionItem(new ItemStack(Material.TIPPED_ARROW, 32), PotionEffectType.WITHER, 200);
        default: throw new IllegalStateException();
        }
    }

    protected ItemStack spawnWeapon() {
        switch (random.nextInt(4)) {
        case 0: return enchant(new ItemStack(Material.BOW));
        case 1: return enchant(new ItemStack(Material.NETHERITE_SWORD));
        case 2: return enchant(new ItemStack(Material.NETHERITE_AXE));
        case 3: return enchant(new ItemStack(Material.CROSSBOW));
        default: throw new IllegalStateException();
        }
    }

    protected ItemStack spawnArmor() {
        switch (random.nextInt(9)) {
        case 0: return enchant(new ItemStack(Material.IRON_CHESTPLATE));
        case 1: return enchant(new ItemStack(Material.DIAMOND_CHESTPLATE));
        case 2: return enchant(new ItemStack(Material.NETHERITE_CHESTPLATE));
        case 3: return enchant(new ItemStack(Material.IRON_LEGGINGS));
        case 4: return enchant(new ItemStack(Material.DIAMOND_LEGGINGS));
        case 5: return enchant(new ItemStack(Material.NETHERITE_LEGGINGS));
        case 6: return enchant(new ItemStack(Material.IRON_BOOTS));
        case 7: return enchant(new ItemStack(Material.DIAMOND_BOOTS));
        case 8: return enchant(new ItemStack(Material.NETHERITE_BOOTS));
        default: throw new IllegalStateException();
        }
    }

    protected ItemStack spawnHelmet() {
        switch (random.nextInt(3)) {
        case 0: return enchant(new ItemStack(Material.IRON_HELMET));
        case 1: return enchant(new ItemStack(Material.DIAMOND_HELMET));
        case 2: return enchant(new ItemStack(Material.NETHERITE_HELMET));
        default: throw new IllegalStateException();
        }
    }

    protected ItemStack spawnChestplate() {
        switch (random.nextInt(3)) {
        case 0: return enchant(new ItemStack(Material.IRON_CHESTPLATE));
        case 1: return enchant(new ItemStack(Material.DIAMOND_CHESTPLATE));
        case 2: return enchant(new ItemStack(Material.NETHERITE_CHESTPLATE));
        default: throw new IllegalStateException();
        }
    }

    protected ItemStack spawnLeggings() {
        switch (random.nextInt(3)) {
        case 0: return enchant(new ItemStack(Material.IRON_LEGGINGS));
        case 1: return enchant(new ItemStack(Material.DIAMOND_LEGGINGS));
        case 2: return enchant(new ItemStack(Material.NETHERITE_LEGGINGS));
        default: throw new IllegalStateException();
        }
    }

    protected ItemStack spawnBoots() {
        switch (random.nextInt(3)) {
        case 0: return enchant(new ItemStack(Material.IRON_BOOTS));
        case 1: return enchant(new ItemStack(Material.DIAMOND_BOOTS));
        case 2: return enchant(new ItemStack(Material.NETHERITE_BOOTS));
        default: throw new IllegalStateException();
        }
    }

    protected void giveBuffItem(Player player) {
        switch (random.nextInt(3)) {
        case 0: case 1: {
            int appleAmount = 1;
            for (int i = 0; i < 6; i += 1) appleAmount += random.nextInt(2);
            giveItem(player, new ItemStack(Material.GOLDEN_APPLE, appleAmount));
            break;
        }
        case 2: {
            int potionAmount = 1 + random.nextInt(3);
            for (int i = 0; i < potionAmount; i += 1) {
                giveItem(player, potion(true));
            }
            break;
        }
        default: throw new IllegalStateException();
        }
    }

    protected void giveDebuffItem(Player player) {
        int potionAmount = 1 + random.nextInt(2);
        for (int i = 0; i < potionAmount; i += 1) {
            giveItem(player, potion(false));
        }
    }

    protected ItemStack potion(boolean buff) {
        final ItemStack item;
        final PotionType pt;
        if (buff) {
            item = new ItemStack(Material.POTION);
            PotionType[] pts = {
                PotionType.STRENGTH,
                PotionType.INSTANT_HEAL,
                PotionType.REGEN,
                PotionType.INVISIBILITY
            };
            pt = pts[random.nextInt(pts.length)];
        } else {
            if (random.nextBoolean()) {
                item = new ItemStack(Material.SPLASH_POTION);
            } else {
                item = new ItemStack(Material.LINGERING_POTION);
            }
            PotionType[] pts = {
                PotionType.INSTANT_DAMAGE,
                PotionType.POISON
            };
            pt = pts[random.nextInt(pts.length)];
        }
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        final boolean extended;
        final boolean upgraded;
        if (pt.isExtendable() && pt.isUpgradeable()) {
            extended = random.nextBoolean();
            upgraded = !extended;
        } else {
            extended = pt.isExtendable();
            upgraded = pt.isUpgradeable();
        }
        try {
            meta.setBasePotionData(new PotionData(pt, extended, upgraded));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            meta.setBasePotionData(new PotionData(pt, false, false));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (tag.state == ArenaState.PLAY) {
            Bukkit.getScheduler().runTask(this, () -> player.setGameMode(GameMode.SPECTATOR));
        } else {
            Bukkit.getScheduler().runTask(this, () -> player.setGameMode(GameMode.ADVENTURE));
        }
        enter(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        exit(player);
        if (tag.state == ArenaState.PLAY) {
            tag.gladiators.remove(player.getUniqueId());
        }
    }

    @EventHandler
    private void onEntityRegainHealth(EntityRegainHealthEvent event) {
        switch (event.getRegainReason()) {
        case SATIATED:
        case REGEN:
            event.setCancelled(true);
        default:
            break;
        }
    }

    @EventHandler
    private void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().isOp()) return;
        event.setCancelled(true);
    }

    private void copyFileStructure(File source, File target) {
        log("copyFileStructure " + source + " => " + target);
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));
            if (!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists()) {
                        if (!target.mkdirs()) {
                            throw new IOException("Couldn't create world directory!");
                        }
                    }
                    String[] files = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyWorld(String worldName) {
        log("copyWorld " + worldName);
        File source = new File(new File(getDataFolder(), "maps"), worldName);
        File dest = new File(Bukkit.getWorldContainer(), "pvparena_" + worldName);
        copyFileStructure(source, dest);
    }

    private World loadWorld(String worldName) {
        log("loadWorld " + worldName);
        File folder = new File(Bukkit.getWorldContainer(), "pvparena_" + worldName);
        if (!folder.isDirectory()) {
            copyWorld(worldName);
        }
        File configFile = new File(folder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        WorldCreator wc = new WorldCreator("pvparena_" + worldName);
        wc.environment(World.Environment.valueOf(config.getString("world.Environment", "NORMAL")));
        wc.generateStructures(config.getBoolean("world.GenerateStructures"));
        wc.generator(config.getString("world.Generator"));
        wc.type(WorldType.valueOf(config.getString("world.WorldType", "NORMAL")));
        getServer().createWorld(wc);
        World result = getServer().getWorld("pvparena_" + worldName);
        result.setAutoSave(false);
        return result;
    }

    protected World getWorld(String worldName) {
        World result = Bukkit.getWorld("pvparena_" + worldName);
        if (result == null) result = loadWorld(worldName);
        result.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        result.setGameRule(GameRule.MOB_GRIEFING, false);
        result.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        result.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        result.setGameRule(GameRule.DO_FIRE_TICK, false);
        result.setGameRule(GameRule.FIRE_DAMAGE, true);
        result.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
        result.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
        result.setDifficulty(Difficulty.EASY);
        return result;
    }

    @EventHandler
    private void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        if (tag.state != ArenaState.IDLE && world != null) {
            event.setSpawnLocation(spread(world.getSpawnLocation()));
        } else {
            event.setSpawnLocation(spread(lobbyWorld.getSpawnLocation()));
        }
    }

    private static Player getPlayerDamager(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        return null;
    }

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Hanging) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof ArmorStand) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player damaged) {
            Gladiator gladiator = getGladiator(damaged);
            if (gladiator == null) {
                event.setCancelled(true);
                return;
            }
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                Gladiator gladiator2 = getGladiator(damager);
                if (gladiator2 == null) {
                    event.setCancelled(true);
                    return;
                }
                if (tag.useSquads && gladiator.squad == gladiator2.squad) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (gladiator.invulnerable > System.currentTimeMillis()) {
                event.setCancelled(true);
                return;
            }
            if (event.getDamager() instanceof AbstractArrow && tag.specialRule == SpecialRule.ARROWS_DOUBLE_DAMAGE) {
                event.setDamage(event.getFinalDamage() * 2.0);
            }
            if (event.getDamager() instanceof Wolf wolf) {
                if (tag.useSquads) {
                    if (wolf.isTamed() && wolf.getOwner() instanceof Player owner) {
                        if (getSquad(owner) == getSquad(damaged)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                if (tag.specialRule == SpecialRule.DOGS_INSTA_KILL) {
                    event.setDamage(2048.0);
                }
            }
            if (event.getDamager() instanceof Player && tag.specialRule == SpecialRule.VAMPIRISM) {
                Player damager = (Player) event.getDamager();
                double dmg = event.getFinalDamage();
                double maxHealth = damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double health = Math.min(maxHealth, damager.getHealth() + dmg * 0.5);
                damager.setHealth(health);
            }
            if (event.getDamager() instanceof AbstractArrow && tag.specialRule == SpecialRule.ARROW_VAMPIRISM) {
                AbstractArrow arrow = (AbstractArrow) event.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    Player damager = (Player) arrow.getShooter();
                    double dmg = event.getFinalDamage();
                    double maxHealth = damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double health = Math.min(maxHealth, damager.getHealth() + dmg * 0.66);
                    damager.setHealth(health);
                }
            }
            Player damager = getPlayerDamager(event.getDamager());
            if (damager != null) {
                Gladiator gladiator2 = getGladiator(damager);
                if (gladiator2 == null) {
                    event.setCancelled(true);
                    return;
                }
                if (tag.useSquads && gladiator.squad == gladiator2.squad) {
                    event.setCancelled(true);
                    return;
                }
                if (damager.equals(damaged)) return;
                for (Entity entity : removeEntities) {
                    if (entity instanceof Tameable) {
                        Tameable tameable = (Tameable) entity;
                        if (tameable.isTamed() && Objects.equals(tameable.getOwnerUniqueId(), damaged.getUniqueId())) {
                            tameable.setTarget(damager);
                        }
                    }
                }
                if (tag.winRule == WinRule.MOLE) {
                    if (Objects.equals(damager.getUniqueId(), tag.moleUuid)) {
                        event.setDamage(event.getFinalDamage() * 2.0);
                    } else if (Objects.equals(damaged.getUniqueId(), tag.moleUuid)) {
                        event.setDamage(event.getFinalDamage() * 0.5);
                    } else {
                        event.setDamage(event.getFinalDamage() * 0.25);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (tag.state != ArenaState.PLAY || tag.gameTime < WARM_UP_TICKS) {
            event.setCancelled(true);
            return;
        }
        if (!event.getEntity().getWorld().equals(world)) return;
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            if (player.getGameMode() == GameMode.SPECTATOR) {
                projectile.remove();
                event.setCancelled(true);
                return;
            }
        }
        removeEntities.add(projectile);
        projectile.setPersistent(false);
    }

    @EventHandler
    private void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (tag.state != ArenaState.PLAY) return;
        if (!event.getPlayer().getWorld().equals(world)) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    private void onCreatureSpawn(CreatureSpawnEvent event) {
        switch (event.getSpawnReason()) {
        case CUSTOM: return;
        default:
            event.setCancelled(true);
            break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
        Block block = event.getBlock();
        Leaves leaves = (Leaves) block.getBlockData();
        leaves.setPersistent(true);
        block.setBlockData(leaves, false);
    }

    protected Location spread(Location location) {
        double r = 2.0;
        return location.add(r * random.nextDouble() - r * random.nextDouble(), 0.0, r * random.nextDouble() - r * random.nextDouble());
    }

    protected void teleport(Player player, Location location) {
        player.teleport(spread(location), TeleportCause.PLUGIN);
    }

    protected boolean toggleSpectatorMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (spectators.contains(uuid)) {
            spectators.remove(uuid);
            return false;
        } else {
            spectators.add(uuid);
            preparePlayer(player);
            player.setGameMode(GameMode.SPECTATOR);
            tag.gladiators.remove(uuid);
            return true;
        }
    }

    protected AreasFile loadAreasFile() {
        File folder = new File(world.getWorldFolder(), "areas");
        File file = new File(folder, "pvparena.json");
        return Json.load(file, AreasFile.class, AreasFile::new);
    }

    @EventHandler
    private void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onHangingBreak(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        if (e instanceof Hanging) {
            event.setCancelled(true);
        } else if (e instanceof ArmorStand) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity e = event.getRightClicked();
        if (e instanceof Hanging) {
            event.setCancelled(true);
        } else if (e instanceof ArmorStand) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < event.getEntity().getFoodLevel()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerTeam(PlayerTeamQuery query) {
        if (!tag.useSquads || tag.squads == null) return;
        if (tag.state == ArenaState.IDLE) return;
        List<PlayerTeamQuery.Team> teams = new ArrayList<>();
        for (Squad squad : tag.squads) {
            PlayerTeamQuery.Team team = new PlayerTeamQuery
                .Team("pvparena:" + squad.name.toLowerCase(),
                      text(squad.name, squad.getTextColor()));
            teams.add(team);
        }
        for (Gladiator gladiator : tag.gladiators.values()) {
            if (gladiator.squad < 0 || gladiator.squad >= teams.size()) continue;
            Squad squad = tag.squads.get(gladiator.squad);
            Player player = gladiator.getPlayer();
            if (player == null) continue;
            query.setTeam(player, teams.get(gladiator.squad));
        }
    }

    @EventHandler
    private void onEntityTarget(EntityTargetEvent event) {
        if (tag.state != ArenaState.PLAY || tag.gameTime < WARM_UP_TICKS) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Wolf wolf) {
            if (tag.useSquads && wolf.isTamed() && wolf.getOwner() instanceof Player owner && event.getTarget() instanceof Player target) {
                if (getSquad(owner) == getSquad(target)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    protected void computeHighscore() {
        highscore = Highscore.of(tag.scores);
        highscoreLines = Highscore.sidebar(highscore, TrophyCategory.SWORD);
    }

    protected List<String> getWorldList() {
        return new ArrayList<>(getConfig().getStringList("worlds"));
    }
}
