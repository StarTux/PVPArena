package com.cavetale.pvparena;

import com.cavetale.afk.AFKPlugin;
import com.cavetale.pvparena.struct.AreasFile;
import com.cavetale.pvparena.struct.Cuboid;
import com.cavetale.pvparena.struct.Vec3i;
import com.cavetale.sidebar.PlayerSidebarEvent;
import com.cavetale.sidebar.Priority;
import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.Title;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

public final class PVPArenaPlugin extends JavaPlugin implements Listener {
    static final int WARM_UP_TICKS = 200;
    static final int SUDDEN_DEATH_TICKS = 20 * 60 * 3;
    static final int TIMED_SCORE_TICKS = 20 * 60 * 5;
    static final int MOLE_TICKS = 20 * 60 * 5;
    static final int IDLE_TICKS = 20 * 30;
    World lobbyWorld;
    World world;
    AreasFile areasFile;
    Tag tag;
    Random random = new Random();
    List<Entity> removeEntities = new ArrayList<>();
    BossBar bossBar;
    private Set<UUID> spectators = new HashSet<>();

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
        bossBar = Bukkit.createBossBar("PVPArena", BarColor.RED, BarStyle.SOLID);
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
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    void enter(Player player) {
        bossBar.addPlayer(player);
    }

    void exit(Player player) {
        bossBar.removePlayer(player);
    }

    void loadTag() {
        tag = Json.load(new File(getDataFolder(), "save.json"), Tag.class, Tag::new);
    }

    void saveTag() {
        Json.save(new File(getDataFolder(), "save.json"), tag);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
        case "start":
            cleanUpGame();
            startGame();
            sender.sendMessage("started");
            return true;
        case "stop":
            cleanUpGame();
            setIdle();
            sender.sendMessage("stopped");
            return true;
        case "save":
            saveTag();
            sender.sendMessage("tag saved");
            return true;
        case "load":
            loadTag();
            sender.sendMessage("tag loaded");
            return true;
        case "rule":
            tag.specialRule = SpecialRule.valueOf(args[1].toUpperCase());
            sender.sendMessage("SpecialRule = " + tag.specialRule);
            return true;
        case "nextworld":
            tag.worlds = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
            tag.worldUsed = 999;
            sender.sendMessage("Worlds coming up: " + tag.worlds);
            return true;
        case "skip":
            tag.worldUsed = 999;
            sender.sendMessage("New world next round!");
            return true;
        case "areas":
            sender.sendMessage("AreasFile: " + Json.serialize(areasFile));
            return true;
        case "event":
            tag.event = !tag.event;
            saveTag();
            sender.sendMessage("Event Mode: " + tag.event);
            return true;
        default:
            return false;
        }
    }

    void tick() {
        if (getEligible().isEmpty()) {
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

    void setIdle() {
        tag.state = ArenaState.IDLE;
        tag.idleTime = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.getWorld().equals(lobbyWorld)) {
                teleport(target, lobbyWorld.getSpawnLocation());
            }
            resetPlayer(target);
        }
    }

    double clampProgress(double in) {
        return Math.max(0.0, Math.min(1.0, in));
    }

    void tickIdle() {
        int eligible = getEligible().size();
        bossBar.setTitle(ChatColor.RED + "Waiting for players... " + ChatColor.WHITE + eligible);
        bossBar.setProgress(clampProgress((double) tag.idleTime / (double) IDLE_TICKS));
        if (eligible < 2) {
            tag.idleTime = 0;
            return;
        }
        if (tag.idleTime > IDLE_TICKS) {
            startGame();
        }
        tag.idleTime += 1;
    }

    void tickPlay() {
        List<Gladiator> alive = new ArrayList<>();
        for (Gladiator it : new ArrayList<>(tag.gladiators.values())) {
            Player player = it.getPlayer();
            if (player == null) {
                tag.gladiators.remove(it.uuid);
                continue;
            }
            tickPlayer(it, player);
            if (!it.gameOver) alive.add(it);
        }
        if (tag.warmUp) {
            int seconds = (WARM_UP_TICKS - tag.gameTime) / 20;
            bossBar.setTitle(ChatColor.RED + "PvP begins in " + seconds + "s");
            bossBar.setProgress(clampProgress((double) tag.gameTime / (double) WARM_UP_TICKS));
        } else if (tag.suddenDeath) {
            bossBar.setTitle(ChatColor.DARK_RED + "Sudden Death " + alive.size() + "/" + tag.totalPlayers);
            bossBar.setProgress(1.0);
        } else {
            bossBar.setTitle(ChatColor.RED + "Fight " + alive.size() + "/" + tag.totalPlayers);
            final double progress;
            switch (tag.winRule) {
            case TIMED_SCORE:
                progress = clampProgress((double) tag.gameTime / (double) TIMED_SCORE_TICKS);
                break;
            case MOLE:
                progress = clampProgress((double) tag.gameTime / (double) MOLE_TICKS);
                break;
            case LAST_SURVIVOR:
            default:
                progress = clampProgress((double) tag.gameTime / (double) SUDDEN_DEATH_TICKS);
                break;
            }
            bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        }
        if (alive.isEmpty()) {
            getLogger().info("The game is a draw!");
            for (Player target : world.getPlayers()) {
                target.sendTitle(ChatColor.RED + "Draw",
                                 ChatColor.RED + "Nobody survives");
                target.sendMessage(ChatColor.RED + "Draw! Nobody survives.");
            }
            endGame();
            return;
        }
        if (tag.winRule == WinRule.LAST_SURVIVOR) {
            if (alive.size() == 1) {
                playerWinsTheGame(alive.get(0));
                return;
            }
            if (tag.gameTime == SUDDEN_DEATH_TICKS) {
                for (Player target : world.getPlayers()) {
                    target.sendMessage(ChatColor.DARK_RED + "Sudden Death!");
                    target.sendTitle("", ChatColor.DARK_RED + "Sudden Death!", 0, 20, 0);
                    target.playSound(target.getLocation(), Sound.BLOCK_BELL_USE, SoundCategory.MASTER, 0.2f, 0.7f);
                }
                tag.suddenDeath = true;
            }
        }
        boolean timesUp = (tag.winRule == WinRule.TIMED_SCORE && tag.gameTime > TIMED_SCORE_TICKS)
            || (tag.winRule == WinRule.MOLE && tag.gameTime > MOLE_TICKS);
        if (timesUp) {
            Collections.sort(alive, (b, a) -> Integer.compare(a.score, b.score));
            int maxScore = alive.get(0).score;
            List<Gladiator> winners = new ArrayList<>();
            for (Gladiator gladiator : alive) {
                if (gladiator.score < maxScore) break;
                winners.add(gladiator);
            }
            if (winners.size() == 1) {
                playerWinsTheGame(winners.get(0));
            } else {
                playersDraw(winners);
            }
            return;
        }
        if (tag.winRule == WinRule.MOLE) {
            if (tag.moleUuid == null || tag.gladiators.get(tag.moleUuid) == null) {
                tag.moleUuid = alive.get(random.nextInt(alive.size())).uuid;
            } else {
                Player mole = Bukkit.getPlayer(tag.moleUuid);
                if (mole != null) {
                    mole.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1, true, false, true), true);
                }
            }
        }
        if (tag.gameTime == WARM_UP_TICKS) {
            for (Player target : world.getPlayers()) {
                target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.2f, 1.2f);
                target.sendTitle(ChatColor.DARK_RED + "Fight!", ChatColor.RED + tag.specialRule.displayName, 0, 20, 0);
                target.sendMessage(ChatColor.DARK_RED + "Fight! " + tag.specialRule.displayName);
                target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 0.1f, 1.2f);
            }
            tag.warmUp = false;
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
                    getLogger().info(item.getType() + " dropped at " + location.getBlockX()
                                     + "," + location.getBlockY()
                                     + "," + location.getBlockZ());
                    entity.setPersistent(false);
                    removeEntities.add(entity);
                }
            }
        }
        removeEntities.removeIf(e -> !e.isValid());
    }

    void playerWinsTheGame(Gladiator winner) {
        getLogger().info("Winner " + winner.getName());
        for (Player target : world.getPlayers()) {
            target.sendTitle(ChatColor.GREEN + winner.getName(),
                             ChatColor.GREEN + "Wins this round!");
            target.sendMessage(ChatColor.GREEN + winner.getName() + " wins this round!");
            target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.1f, 2.0f);
        }
        if (tag.event) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "titles unlockset " + winner.getName() + " Champion");
        endGame();
    }

    void playersDraw(List<Gladiator> drawers) {
        String[] names = new String[drawers.size()];
        for (int i = 0; i < names.length; i += 1) {
            names[i] = drawers.get(i).getName();
        }
        getLogger().info("Draw: " + String.join(" ", names));
        for (Player target : world.getPlayers()) {
            target.sendTitle(ChatColor.RED + "Draw",
                             ChatColor.RED + String.join(", ", names));
            target.sendMessage(ChatColor.RED + "The game is a draw between " + String.join(", ", names));
            target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.MASTER, 0.1f, 2.0f);
        }
        if (tag.event) {
            for (Gladiator drawer : drawers) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "titles unlockset " + drawer.getName() + " Champion");
            }
        }
        endGame();
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
                    player.sendTitle(new Title("" + ChatColor.DARK_RED + ChatColor.BOLD + (seconds + 1),
                                               "" + ChatColor.DARK_RED + "Get Ready!",
                                               0, 10, 10));
                }
            }
        } else {
            gladiator.health = player.getHealth();
        }
    }

    void tickEnd() {
        tag.endTime += 1;
        if (tag.endTime > 200) {
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

    int getScore(Player player) {
        Gladiator gladiator = getGladiator(player);
        return gladiator != null ? gladiator.score : 0;
    }

    void ensureWorldIsLoaded() {
        if (tag.worldName != null && tag.worldUsed < 3) return;
        tag.worldUsed = 0;
        if (tag.worlds.isEmpty()) {
            tag.worlds = new ArrayList<>(getConfig().getStringList("worlds"));
            Collections.shuffle(tag.worlds);
        }
        if (tag.worlds.isEmpty()) throw new IllegalStateException("No worlds!");
        tag.worldName = tag.worlds.remove(tag.worlds.size() - 1);
        getLogger().info("Picking world: " + tag.worldName);
        world = getWorld(tag.worldName);
        areasFile = loadAreasFile();
    }

    void startGame() {
        ensureWorldIsLoaded();
        List<WinRule> wins = new ArrayList<>(Arrays.asList(WinRule.values()));
        tag.winRule = wins.get(random.nextInt(wins.size()));
        List<SpecialRule> rules = new ArrayList<>(Arrays.asList(SpecialRule.values()));
        rules.remove(SpecialRule.NONE);
        tag.specialRule = rules.get(random.nextInt(rules.size()));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(world)) {
                teleport(player, world.getSpawnLocation());
            }
            if (!spectators.contains(player.getUniqueId()) && AFKPlugin.isAfk(player)) {
                spectators.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You were marked as spectator due to inactivity");
            }
            if (spectators.contains(player.getUniqueId())) {
                preparePlayer(player);
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
        tag.gladiators.clear();
        tag.limitedLives = tag.winRule == WinRule.LAST_SURVIVOR;
        List<Player> eligible = getEligible();
        tag.moleUuid = tag.winRule == WinRule.MOLE ? eligible.get(random.nextInt(eligible.size())).getUniqueId() : null;
        for (Player target : eligible) {
            resetPlayer(target);
            giveGear(target);
            target.teleport(findSpawnLocation(), TeleportCause.PLUGIN);
            Bukkit.getScheduler().runTaskLater(this, () -> target.setInvisible(true), 1L);
            Bukkit.getScheduler().runTaskLater(this, () -> target.setInvisible(false), 2L);
            Gladiator gladiator = new Gladiator(target);
            gladiator.lives = tag.limitedLives ? 3 : 0;
            tag.gladiators.put(target.getUniqueId(), gladiator);
            if (tag.event) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ml add " + target.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "titles unlockset " + target.getName() + " Gladiator");
            }
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.sendMessage("");
            target.sendMessage("" + ChatColor.RED + ChatColor.BOLD + tag.winRule.displayName
                               + ChatColor.WHITE + " " + tag.winRule.getDescription());
            target.sendMessage(ChatColor.GRAY + "Special: " + ChatColor.WHITE + tag.specialRule.displayName);
            if (tag.winRule == WinRule.MOLE && Objects.equals(target.getUniqueId(), tag.moleUuid)) {
                target.sendTitle("", ChatColor.RED + "You are the mole!");
                target.sendMessage(ChatColor.RED + "You are the mole!");
            }
            target.sendMessage("");
        }
        tag.state = ArenaState.PLAY;
        tag.gameTime = 0;
        tag.suddenDeath = false;
        tag.warmUp = true;
        tag.totalPlayers = eligible.size();
        tag.worldUsed += 1;
    }

    void resetPlayer(Player target) {
        target.getInventory().clear();
        target.setGameMode(GameMode.ADVENTURE);
        preparePlayer(target);
    }

    void preparePlayer(Player target) {
        target.setHealth(20.0);
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setArrowsInBody(0);
        target.setInvisible(false);
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }
    }

    void endGame() {
        if (tag.state != ArenaState.PLAY) return;
        cleanUpGame();
        tag.endTime = 0;
        tag.state = ArenaState.END;
        saveTag();
    }

    void cleanUpGame() {
        for (Entity e : removeEntities) e.remove();
        removeEntities.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
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
                    c.setMaxHealth(100.0);
                    c.setHealth(100.0);
                });
            removeEntities.add(creeper);
        }
        if (tag.limitedLives) {
            if (gladiator.lives <= 1) {
                gladiator.lives = 0;
                gladiator.gameOver = true;
                player.sendTitle(new Title("" + ChatColor.DARK_RED + "Game Over",
                                           "" + ChatColor.DARK_RED + "Wait for the next round"));
            } else {
                gladiator.lives -= 1;
                if (gladiator.lives == 1) {
                    player.sendTitle(new Title("" + ChatColor.BLUE + "One Life",
                                               "" + ChatColor.BLUE + "Last Chance"));
                } else {
                    player.sendTitle(new Title("" + ChatColor.BLUE + gladiator.lives + " Lives",
                                               "" + ChatColor.BLUE + "Respawn soon!"));
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
        // Fireworks.spawnFirework(player.getLocation()).detonate();
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
                killer.sendMessage(ChatColor.GOLD + "Your gear was improved");
            }
            if (tag.specialRule == SpecialRule.HEAL_ON_KILL) {
                killer.setHealth(killer.getMaxHealth());
                killer.sendMessage(ChatColor.GOLD + "You've been healed");
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
                killer.sendMessage(ChatColor.GOLD + "You received extra gear");
            }
            if (tag.specialRule == SpecialRule.POTION_ON_KILL) {
                List<PotionEffectType> pts = Arrays.asList(PotionEffectType.INCREASE_DAMAGE,
                                                           PotionEffectType.ABSORPTION,
                                                           PotionEffectType.HEALTH_BOOST,
                                                           PotionEffectType.INVISIBILITY,
                                                           PotionEffectType.DAMAGE_RESISTANCE);
                PotionEffectType potion = pts.get(random.nextInt(pts.size()));
                killer.addPotionEffect(new PotionEffect(potion, 20 * 30, 1, true, false, true));
                killer.sendMessage(ChatColor.GOLD + "You received a potion effect!");
            }
            gladiator2.kills += 1;
            if (tag.winRule == WinRule.MOLE) {
                if (Objects.equals(tag.moleUuid, gladiator.uuid)) {
                    // Mole got killed
                    gladiator2.score += 1;
                    if (!killer.equals(player)) {
                        tag.moleUuid = gladiator2.uuid;
                        killer.setHealth(killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        killer.sendTitle("", ChatColor.RED + "You are the mole!");
                        killer.sendMessage(ChatColor.RED + "You are the mole!");
                    } else {
                        tag.moleUuid = null;
                    }
                    player.sendMessage(ChatColor.RED + "You are no longer the mole");
                } else if (Objects.equals(tag.moleUuid, gladiator2.uuid)) {
                    // Killer is mole: mole killed someone
                    gladiator2.score += 1;
                }
            } else {
                gladiator2.score += 1;
            }
            getLogger().info(killer.getName() + " killed " + player.getName());
        } else {
            if (tag.winRule == WinRule.MOLE && Objects.equals(gladiator.uuid, tag.moleUuid)) {
                player.sendMessage(ChatColor.RED + "You are no longer the mole");
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
                        target.sendMessage(ChatColor.GREEN + "Shuffle!");
                    }
                });
        }
    }

    List<Vec3i> findSpawnVectors() {
        if (areasFile.getAreas().getSpawn().isEmpty()) {
            return Arrays.asList(Vec3i.of(world.getSpawnLocation()));
        }
        Set<Vec3i> result = new HashSet<>();
        for (Cuboid cuboid : areasFile.getAreas().getSpawn()) {
            result.addAll(cuboid.enumerate());
        }
        return new ArrayList<>(result);
    }

    Vec3i findSpawnVector() {
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

    Location findSpawnLocation() {
        Vec3i vector = findSpawnVector();
        Location location = world.getBlockAt(vector.x, vector.y, vector.z).getLocation();
        location = location.add(0.5, 0.1, 0.5);
        location.setYaw((float) (random.nextDouble() * 360.0));
        return location;
    }

    void respawn(Player player) {
        Location spawnLocation = findSpawnLocation();
        preparePlayer(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(spawnLocation, TeleportCause.PLUGIN);
        if (tag.specialRule == SpecialRule.DEATH_BUFF) {
            for (ItemStack item : player.getInventory()) {
                if (item == null || item.getType() == Material.AIR) continue;
                enchant(item);
            }
            giveGear(player);
            player.sendMessage(ChatColor.GOLD + "You received extra gear!");
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
            .filter(p -> !p.isPermissionSet("group.streamer") || !p.hasPermission("group.streamer"))
            .filter(p -> p.hasPermission("pvparena.player"))
            .filter(p -> !spectators.contains(p.getUniqueId()))
            .collect(Collectors.toList());
    }

    @EventHandler
    public void onPlayerSidebar(PlayerSidebarEvent event) {
        List<String> ls = new ArrayList<>();
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) {
            ls.add("" + ChatColor.YELLOW + ChatColor.BOLD + "Specating "
                   + ChatColor.YELLOW + "(/spec)");
        }
        if (tag.state == ArenaState.PLAY) {
            ls.add(ChatColor.GRAY + "Win " + ChatColor.RED + tag.winRule.displayName);
            ls.add(ChatColor.GRAY + "Special " + ChatColor.RED + tag.specialRule.displayName);
            if (tag.suddenDeath) {
                ls.add("" + ChatColor.DARK_RED + ChatColor.BOLD + "Sudden Death");
            }
        }
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
                ls.add("" + ChatColor.GREEN + gladiator.score + " " + ChatColor.DARK_GRAY + gladiator.name);
            } else {
                int hearts = (int) Math.ceil(gladiator.health * 0.5);
                ChatColor nameColor;
                if (gladiator.is(player)) {
                    nameColor = ChatColor.GREEN;
                } else {
                    nameColor = ChatColor.WHITE;
                }
                if (tag.limitedLives) {
                    String lvs = gladiator.lives > 0 ? (ChatColor.BLUE + "|" + gladiator.lives) : "";
                    ls.add("" + ChatColor.RED + "\u2764" + hearts + lvs + " " + nameColor + gladiator.name);
                } else {
                    ls.add("" + ChatColor.WHITE + gladiator.score
                           + " " + ChatColor.RED + "\u2764" + hearts
                           + " " + nameColor + gladiator.name);
                }
            }
        }
        if (ls.isEmpty()) return;
        event.addLines(this, Priority.DEFAULT, ls);
    }

    boolean isAlive(Player p) {
        return !p.isDead() && (p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (tag.state != ArenaState.PLAY || tag.gameTime < 200) {
            event.setCancelled(true);
            return;
        }
    }

    ItemStack enchant(ItemStack item) {
        if (item.getType() == Material.TRIDENT) {
            item.addUnsafeEnchantment(Enchantment.LOYALTY, Enchantment.LOYALTY.getMaxLevel());
            item.addUnsafeEnchantment(Enchantment.IMPALING, Enchantment.IMPALING.getMaxLevel());
        } else if (item.getType() == Material.CROSSBOW) {
            item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 3);
        }
        do {
            List<Enchantment> list = new ArrayList<>();
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.equals(Enchantment.DURABILITY)) continue;
                if (enchantment.equals(Enchantment.DAMAGE_ARTHROPODS)) continue;
                if (enchantment.equals(Enchantment.DAMAGE_UNDEAD)) continue;
                if (enchantment.equals(Enchantment.DEPTH_STRIDER)) continue;
                if (enchantment.equals(Enchantment.DIG_SPEED)) continue;
                if (enchantment.equals(Enchantment.FROST_WALKER)) continue;
                if (enchantment.equals(Enchantment.LOOT_BONUS_BLOCKS)) continue;
                if (enchantment.equals(Enchantment.LOOT_BONUS_MOBS)) continue;
                if (enchantment.equals(Enchantment.LUCK)) continue;
                if (enchantment.equals(Enchantment.LURE)) continue;
                if (enchantment.equals(Enchantment.MENDING)) continue;
                if (enchantment.equals(Enchantment.OXYGEN)) continue;
                if (enchantment.equals(Enchantment.PROTECTION_FALL)) continue;
                if (enchantment.equals(Enchantment.PROTECTION_FIRE)) continue;
                if (enchantment.equals(Enchantment.SILK_TOUCH)) continue;
                if (enchantment.equals(Enchantment.SOUL_SPEED)) continue;
                if (enchantment.equals(Enchantment.WATER_WORKER)) continue;
                if (!enchantment.canEnchantItem(item)) continue;
                if (enchantment.isCursed()) continue;
                list.add(enchantment);
            }
            if (list.isEmpty()) return item;
            Collections.shuffle(list);
            Enchantment enchantment = list.get(0);
            item.addUnsafeEnchantment(enchantment, enchantment.getMaxLevel());
        } while (random.nextBoolean());
        return item;
    }

    void giveGear(Player player) {
        ItemStack weapon = spawnWeapon();
        giveItem(player, weapon);
        giveItem(player, spawnArmor());
        giveBuffItem(player);
        giveDebuffItem(player);
        if (weapon.getType() == Material.BOW || weapon.getType() == Material.CROSSBOW) {
            giveItem(player, spawnArrows());
            int dogAmount = random.nextInt(8) - random.nextInt(8);
            if (dogAmount > 0) {
                for (int i = 0; i < dogAmount; i += 1) {
                    Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class, w -> {
                            w.setTamed(true);
                            w.setOwner(player);
                            w.setPersistent(false);
                            w.setRemoveWhenFarAway(true);
                        });
                    removeEntities.add(wolf);
                }
                if (dogAmount == 1) {
                    player.sendMessage(ChatColor.BLUE + "You got a dog!");
                } else {
                    player.sendMessage(ChatColor.BLUE + "You got " + dogAmount + " dogs!");
                }
            }
        } else {
            giveItem(player, enchant(new ItemStack(Material.SHIELD)));
        }
    }

    boolean setEquipment(Player player, EquipmentSlot slot, ItemStack item) {
        ItemStack old = player.getEquipment().getItem(slot);
        if (old != null && old.getAmount() > 0) return false;
        player.getEquipment().setItem(slot, item);
        return true;
    }

    boolean addInventory(Player player, ItemStack item) {
        // for (int i = 9; i < 36; i += 1) {
        //     ItemStack old = player.getInventory().getItem(i);
        //     if (old != null && old.getAmount() > 0) continue;
        //     player.getInventory().setItem(i, item);
        //     return true;
        // }
        // return false;
        player.getInventory().addItem(item);
        return true;
    }

    boolean giveItem(Player player, ItemStack item) {
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

    ItemStack spawnArrows() {
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

    ItemStack spawnWeapon() {
        switch (random.nextInt(10)) {
        case 0:
        case 1:
        case 2:
            return enchant(new ItemStack(Material.BOW));
        case 3:
        case 4:
            return enchant(new ItemStack(Material.CROSSBOW));
        case 5:
        case 6:
            return enchant(new ItemStack(Material.DIAMOND_SWORD));
        case 7:
            return enchant(new ItemStack(Material.NETHERITE_SWORD));
        case 8:
            return enchant(new ItemStack(Material.DIAMOND_AXE));
        case 9:
            return enchant(new ItemStack(Material.NETHERITE_AXE));
        default: throw new IllegalStateException();
        }
    }

    ItemStack spawnArmor() {
        switch (random.nextInt(12)) {
        case 0: return enchant(new ItemStack(Material.IRON_CHESTPLATE));
        case 1: return enchant(new ItemStack(Material.DIAMOND_CHESTPLATE));
        case 2: return enchant(new ItemStack(Material.NETHERITE_CHESTPLATE));
        case 3: return enchant(new ItemStack(Material.IRON_LEGGINGS));
        case 4: return enchant(new ItemStack(Material.DIAMOND_LEGGINGS));
        case 5: return enchant(new ItemStack(Material.NETHERITE_LEGGINGS));
        case 6: return enchant(new ItemStack(Material.IRON_HELMET));
        case 7: return enchant(new ItemStack(Material.DIAMOND_HELMET));
        case 8: return enchant(new ItemStack(Material.NETHERITE_HELMET));
        case 9: return enchant(new ItemStack(Material.IRON_BOOTS));
        case 10: return enchant(new ItemStack(Material.DIAMOND_BOOTS));
        case 11: return enchant(new ItemStack(Material.NETHERITE_BOOTS));
        default: throw new IllegalStateException();
        }
    }

    void giveBuffItem(Player player) {
        switch (random.nextInt(2)) {
        case 0: {
            int appleAmount = 1 + random.nextInt(3);
            giveItem(player, new ItemStack(Material.GOLDEN_APPLE, appleAmount));
            break;
        }
        case 1: {
            int potionAmount = 1 + random.nextInt(3);
            for (int i = 0; i < potionAmount; i += 1) {
                giveItem(player, potion(true));
            }
            break;
        }
        default: throw new IllegalStateException();
        }
    }

    void giveDebuffItem(Player player) {
        int potionAmount = 1 + random.nextInt(2);
        for (int i = 0; i < potionAmount; i += 1) {
            giveItem(player, potion(false));
        }
    }

    ItemStack potion(boolean buff) {
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (tag.state == ArenaState.PLAY) {
            Bukkit.getScheduler().runTask(this, () -> player.setGameMode(GameMode.SPECTATOR));
        } else {
            Bukkit.getScheduler().runTask(this, () -> player.setGameMode(GameMode.ADVENTURE));
        }
        enter(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        exit(player);
        if (tag.state == ArenaState.PLAY) {
            tag.gladiators.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        switch (event.getRegainReason()) {
        case SATIATED:
        case REGEN:
            event.setCancelled(true);
        default:
            break;
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().isOp()) return;
        event.setCancelled(true);
    }

    private void copyFileStructure(File source, File target) {
        getLogger().info("copyFileStructure " + source + " => " + target);
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
        getLogger().info("copyWorld " + worldName);
        File source = new File(new File(getDataFolder(), "maps"), worldName);
        File dest = new File(Bukkit.getWorldContainer(), "pvparena_" + worldName);
        copyFileStructure(source, dest);
    }

    private World loadWorld(String worldName) {
        getLogger().info("loadWorld " + worldName);
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

    World getWorld(String worldName) {
        World result = Bukkit.getWorld("pvparena_" + worldName);
        if (result == null) result = loadWorld(worldName);
        result.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        result.setGameRule(GameRule.MOB_GRIEFING, false);
        result.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        result.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        result.setGameRule(GameRule.DO_FIRE_TICK, false);
        result.setGameRule(GameRule.DO_FIRE_TICK, false);
        result.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
        result.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
        result.setDifficulty(Difficulty.EASY);
        return result;
    }

    @EventHandler
    void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
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
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            Gladiator gladiator = getGladiator(damaged);
            if (gladiator != null && gladiator.invulnerable > System.currentTimeMillis()) {
                event.setCancelled(true);
                return;
            }
            if (event.getDamager() instanceof AbstractArrow && tag.specialRule == SpecialRule.ARROWS_DOUBLE_DAMAGE) {
                event.setDamage(event.getFinalDamage() * 2.0);
            }
            if (event.getDamager() instanceof Wolf && tag.specialRule == SpecialRule.DOGS_INSTA_KILL) {
                event.setDamage(2048.0);
            }
            if (event.getDamager() instanceof Player && tag.specialRule == SpecialRule.VAMPIRISM) {
                Player damager = (Player) event.getDamager();
                double dmg = event.getFinalDamage();
                double health = Math.min(damager.getMaxHealth(), damager.getHealth() + dmg * 0.5);
                damager.setHealth(health);
            }
            if (event.getDamager() instanceof AbstractArrow && tag.specialRule == SpecialRule.ARROW_VAMPIRISM) {
                AbstractArrow arrow = (AbstractArrow) event.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    Player damager = (Player) arrow.getShooter();
                    double dmg = event.getFinalDamage();
                    double health = Math.min(damager.getMaxHealth(), damager.getHealth() + dmg * 0.66);
                    damager.setHealth(health);
                }
            }
            Player damager = getPlayerDamager(event.getDamager());
            if (damager != null) {
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
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (tag.state != ArenaState.PLAY) return;
        if (!event.getEntity().getWorld().equals(world)) return;
        Projectile projectile = event.getEntity();
        removeEntities.add(projectile);
        projectile.setPersistent(false);
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (tag.state != ArenaState.PLAY) return;
        if (!event.getPlayer().getWorld().equals(world)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        switch (event.getSpawnReason()) {
        case CUSTOM: return;
        default:
            event.setCancelled(true);
            break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
        Block block = event.getBlock();
        Leaves leaves = (Leaves) block.getBlockData();
        leaves.setPersistent(true);
        block.setBlockData(leaves, false);
    }

    Location spread(Location location) {
        double r = 2.0;
        return location.add(r * random.nextDouble() - r * random.nextDouble(), 0.0, r * random.nextDouble() - r * random.nextDouble());
    }

    void teleport(Player player, Location location) {
        player.teleport(spread(location), TeleportCause.PLUGIN);
    }

    boolean toggleSpectatorMode(Player player) {
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

    AreasFile loadAreasFile() {
        File folder = new File(world.getWorldFolder(), "areas");
        File file = new File(folder, "pvparena.json");
        return Json.load(file, AreasFile.class, AreasFile::new);
    }

    @EventHandler
    void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onHangingBreak(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        if (e instanceof Hanging) {
            event.setCancelled(true);
        } else if (e instanceof ArmorStand) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity e = event.getRightClicked();
        if (e instanceof Hanging) {
            event.setCancelled(true);
        } else if (e instanceof ArmorStand) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < event.getEntity().getFoodLevel()) {
            event.setCancelled(true);
        }
    }
}
