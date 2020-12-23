package com.cavetale.pvparena;

import com.cavetale.afk.AFKPlugin;
import com.cavetale.sidebar.PlayerSidebarEvent;
import com.cavetale.sidebar.Priority;
import com.destroystokyo.paper.MaterialTags;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class PVPArenaPlugin extends JavaPlugin implements Listener {
    static final int WARM_UP_TICKS = 200;
    static final int SUDDEN_DEATH_TICKS = 20 * 90;
    static final int IDLE_TICKS = 20 * 30;
    World lobbyWorld;
    World world;
    Tag tag;
    Random random = new Random();
    List<Entity> removeEntities = new ArrayList<>();
    String streamerName = "Cavetale";
    BossBar bossBar;
    private Set<UUID> spectators = new HashSet<>();

    static final class Tag {
        State state = State.IDLE;
        int gameTime = 0;
        boolean suddenDeath = false;
        boolean warmUp = false;
        int endTime = 0;
        int idleTime = 0;
        Map<UUID, Integer> scores = new HashMap<>();
        Map<UUID, Integer> lives = new HashMap<>();
        List<String> worlds = new ArrayList<>();
        String worldName = null;
        int worldUsed = 0;
        int totalPlayers = 0;
        SpecialRule specialRule = SpecialRule.NONE;
    }

    enum State {
        IDLE,
        PLAY,
        END;
    }

    enum SpecialRule {
        NONE("Regular combat"),
        HEAL_ON_KILL("Every Kill Heals"),
        GEAR_ON_KILL("Kills drop extra gear"),
        LIVES("You get 3 lives"),
        ARROWS_INSTA_KILL("Arrows are deadly"),
        DOGS_INSTA_KILL("Dogs are deadly"),
        VAMPIRISM("Vampirism"),
        CREEPER_REVENGE("Dying spawns a creeper"),
        SHUFFLE_ON_KILL("Kills shuffle players"),
        ZOMBIECALYPSE("Zombie Apocalypse"),
        EXPLOSIVE_ARROWS("Explosive Arrows");

        public final String displayName;

        SpecialRule(final String dn) {
            this.displayName = dn;
        }
    }

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::tick, 1, 1);
        getCommand("spectator").setExecutor(new SpectatorCommand(this));
        streamerName = getConfig().getString("streamer");
        loadTag();
        if (tag.worldName != null) {
            world = getWorld(tag.worldName);
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
        bossBar.removeAll();
        bossBar = null;
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
        case "resetscore":
            tag.scores.clear();
            sender.sendMessage("score reset");
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
        tag.state = State.IDLE;
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
        List<Player> alive = getAlive();
        if (tag.warmUp) {
            int seconds = (WARM_UP_TICKS - tag.gameTime) / 20;
            bossBar.setTitle(ChatColor.RED + "PvP begins in " + seconds + "s");
            bossBar.setProgress(clampProgress((double) tag.gameTime / (double) WARM_UP_TICKS));
        } else if (tag.suddenDeath) {
            bossBar.setTitle(ChatColor.DARK_RED + "Sudden Death " + alive.size() + "/" + tag.totalPlayers);
            bossBar.setProgress(1.0);
        } else {
            bossBar.setTitle(ChatColor.RED + "Fight " + alive.size() + "/" + tag.totalPlayers);
            bossBar.setProgress(clampProgress((double) tag.gameTime / (double) SUDDEN_DEATH_TICKS));
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
        if (alive.size() == 1) {
            Player winner = alive.get(0);
            getLogger().info("Winner " + winner.getName());
            for (Player target : world.getPlayers()) {
                target.sendTitle(ChatColor.GREEN + winner.getName(),
                                 ChatColor.GREEN + "Wins this round!");
                target.sendMessage(ChatColor.GREEN + winner.getName() + " wins this round!");
                target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.1f, 2.0f);
            }
            //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "titles unlockset " + winner.getName() + " Champion");
            endGame();
            return;
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
        // if (tag.warmUp && tag.gameTime % 10 == 0) {
            // int seconds = (WARM_UP_TICKS - tag.gameTime) / 20;
            // for (Player target : world.getPlayers()) {
            //     target.sendActionBar(ChatColor.RED + "PvP begins in " + seconds);
            // }
        // }
        if (tag.gameTime == SUDDEN_DEATH_TICKS) {
            for (Player target : world.getPlayers()) {
                target.sendMessage(ChatColor.DARK_RED + "Sudden Death!");
                target.sendTitle("", ChatColor.DARK_RED + "Sudden Death!", 0, 20, 0);
                target.playSound(target.getLocation(), Sound.BLOCK_BELL_USE, SoundCategory.MASTER, 0.2f, 0.7f);
            }
            tag.suddenDeath = true;
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
            for (Player player : alives) {
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
                        z.setPersistent(false);
                        z.setRemoveWhenFarAway(true);
                    });
                removeEntities.add(zombie);
            }
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

    void addScore(Player player, int score) {
        Integer sc = tag.scores.get(player.getUniqueId());
        int newScore = sc == null ? score : sc + score;
        tag.scores.put(player.getUniqueId(), newScore);
    }

    int getScore(Player player) {
        Integer sc = tag.scores.get(player.getUniqueId());
        return sc != null ? sc : 0;
    }

    void ensureWorldIsLoaded() {
        if (tag.worlds.isEmpty()) {
            tag.worlds = new ArrayList<>(getConfig().getStringList("worlds"));
            Collections.shuffle(tag.worlds);
        }
        if (tag.worlds.isEmpty()) throw new IllegalStateException("No worlds!");
        if (tag.worldName == null || tag.worldUsed > 3) {
            tag.worldUsed = 0;
            tag.worldName = tag.worlds.remove(tag.worlds.size() - 1);
            getLogger().info("Picking world: " + tag.worldName);
        }
        world = getWorld(tag.worldName);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
    }

    void startGame() {
        ensureWorldIsLoaded();
        List<SpecialRule> rules = Arrays.asList(SpecialRule.values());
        tag.specialRule = rules.get(random.nextInt(rules.size()));
        tag.lives.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(world)) {
                teleport(player, world.getSpawnLocation());
            }
            if (!spectators.contains(player.getUniqueId()) && AFKPlugin.isAfk(player)) {
                spectators.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You were marked as spectator due to inactivity");
            }
            if (spectators.contains(player.getUniqueId())) {
                revivePlayer(player);
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
        List<Player> eligible = getEligible();
        for (Player target : eligible) {
            resetPlayer(target);
            giveGear(target);
            teleport(target, world.getSpawnLocation());
            tag.scores.computeIfAbsent(target.getUniqueId(), u -> 0);
            //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ml add " + target.getName());
            if (tag.specialRule == SpecialRule.LIVES) {
                tag.lives.put(target.getUniqueId(), 3);
            }
            Bukkit.getScheduler().runTaskLater(this, () -> target.setInvisible(true), 1L);
            Bukkit.getScheduler().runTaskLater(this, () -> target.setInvisible(false), 2L);
        }
        tag.state = State.PLAY;
        tag.gameTime = 0;
        tag.suddenDeath = false;
        tag.warmUp = true;
        tag.totalPlayers = eligible.size();
        tag.worldUsed += 1;
    }

    void resetPlayer(Player target) {
        target.getInventory().clear();
        target.setGameMode(GameMode.ADVENTURE);
        revivePlayer(target);
    }

    void revivePlayer(Player target) {
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
        if (tag.state != State.PLAY) return;
        cleanUpGame();
        tag.endTime = 0;
        tag.state = State.END;
        saveTag();
    }

    void cleanUpGame() {
        for (Entity e : removeEntities) e.remove();
        removeEntities.clear();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
        Player player = event.getEntity();
        boolean revive = false;
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
        if (tag.specialRule == SpecialRule.LIVES && !tag.suddenDeath) {
            Integer lives = tag.lives.get(player.getUniqueId());
            if (lives == null || lives <= 1) {
                tag.lives.remove(player.getUniqueId());
                revive = false;
            } else {
                tag.lives.put(player.getUniqueId(), lives - 1);
                revive = true;
            }
        }
        if (revive) {
            event.setCancelled(true);
            revivePlayer(player);
            getServer().getScheduler().runTask(this, () -> {
                    player.setGameMode(GameMode.ADVENTURE);
                    teleport(player, world.getSpawnLocation());
                });
        } else {
            event.setCancelled(true);
            for (Entity entity : removeEntities) {
                if (entity instanceof Tameable) {
                    Tameable tameable = (Tameable) entity;
                    if (player.equals(tameable.getOwner())) {
                        entity.remove();
                    }
                }
            }
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SPECTATOR);
        }
        Player killer = player.getKiller();
        if (killer != null) {
            for (ItemStack item : killer.getInventory()) {
                if (item == null || item.getType() == Material.AIR) continue;
                enchant(item);
            }
            killer.sendMessage(ChatColor.RED + "Your gear was improved");
            if (tag.specialRule == SpecialRule.HEAL_ON_KILL) {
                killer.setHealth(killer.getMaxHealth());
                killer.sendMessage(ChatColor.RED + "You've been healed");
            }
            if (tag.specialRule == SpecialRule.GEAR_ON_KILL) {
                giveGear(killer);
                killer.sendMessage(ChatColor.RED + "You received extra gear");
            }
            addScore(killer, 1);
            getLogger().info(killer.getName() + " killed " + player.getName());
            for (Player target : Bukkit.getOnlinePlayers()) {
                target.sendMessage(ChatColor.DARK_RED + killer.getName() + " killed " + player.getName());
            }
        } else {
            for (Player target : Bukkit.getOnlinePlayers()) {
                target.sendMessage(ChatColor.DARK_RED + player.getName() + " died");
            }
        }
        if (tag.specialRule == SpecialRule.SHUFFLE_ON_KILL) {
            Bukkit.getScheduler().runTask(this, () -> {
                    List<Location> locs = new ArrayList<>();
                    List<Player> alives = getAlive();
                    for (Player alive : alives) {
                        locs.add(alive.getLocation());
                    }
                    Collections.shuffle(locs, random);
                    int i = 0;
                    for (Player alive : alives) {
                        alive.teleport(locs.get(i++), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        target.sendMessage(ChatColor.GREEN + "Shuffle!");
                    }
                });
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
            .filter(p -> !p.getName().equals(streamerName))
            .filter(p -> p.hasPermission("pvparena.player"))
            .filter(p -> !spectators.contains(p.getUniqueId()))
            .collect(Collectors.toList());
    }

    @EventHandler
    public void onPlayerSidebar(PlayerSidebarEvent event) {
        // if (tag.state == State.IDLE) return;
        List<String> ls = new ArrayList<>();
        if (spectators.contains(event.getPlayer().getUniqueId())) {
            ls.add("" + ChatColor.YELLOW + ChatColor.BOLD + "Specating "
                   + ChatColor.YELLOW + "(/spec)");
        }
        if (tag.state == State.PLAY) {
            ls.add(ChatColor.GREEN + "Rule " + ChatColor.RED + tag.specialRule.displayName);
            if (tag.suddenDeath) {
                ls.add("" + ChatColor.DARK_RED + ChatColor.BOLD + "Sudden Death");
            }
        }
        List<Player> list = getEligible().stream()
            .filter(p -> tag.scores.containsKey(p.getUniqueId()))
            .sorted((b, a) -> {
                    int c = Integer.compare(isAlive(a) ? 1 : 0,
                                            isAlive(b) ? 1 : 0);
                    if (c != 0) return c;
                    return Integer.compare(getScore(a), getScore(b));
                })
            .collect(Collectors.toList());
        for (Player player : list) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                ls.add("" + ChatColor.GREEN + getScore(player) + " " + ChatColor.DARK_GRAY + player.getName());
            } else {
                Integer lives = tag.lives.get(player.getUniqueId());
                String lvs = !tag.suddenDeath && lives != null && lives > 0 ? (ChatColor.BLUE + "|" + lives) : "";
                ChatColor nameColor = player.equals(event.getPlayer()) ? ChatColor.GREEN : ChatColor.WHITE;
                ls.add("" + ChatColor.RED + "\u2665"
                       + ((int) Math.ceil(player.getHealth() * 0.5)) + lvs + " " + nameColor + player.getName());
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
        if (tag.state != State.PLAY || tag.gameTime < 200) {
            event.setCancelled(true);
            return;
        }
    }

    ItemStack enchant(ItemStack item) {
        if (item.getType() == Material.TRIDENT) {
            item.addUnsafeEnchantment(Enchantment.LOYALTY, Enchantment.LOYALTY.getMaxLevel());
        }
        do {
            List<Enchantment> enchs = Stream.of(Enchantment.values())
                .filter(e -> e.canEnchantItem(item))
                .collect(Collectors.toList());
            if (enchs.isEmpty()) return item;
            Collections.shuffle(enchs);
            Enchantment enchantment = enchs.get(0);
            item.addUnsafeEnchantment(enchantment, enchantment.getMaxLevel());
        } while (random.nextBoolean());
        return item;
    }

    void giveGear(Player player) {
        giveWeapon(player);
        giveFood(player);
        giveArmor(player);
        giveBonus(player);
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

    void giveWeapon(Player player) {
        switch (random.nextInt(8)) {
        case 0:
        case 1:
            giveItem(player, enchant(new ItemStack(Material.BOW)));
            switch (random.nextInt(4)) {
            case 0: case 1: case 2:
                giveItem(player, enchant(new ItemStack(Material.ARROW, 64))); break;
            case 3:
                giveItem(player, enchant(new ItemStack(Material.SPECTRAL_ARROW, 64))); break;
            default: throw new IllegalStateException();
            }
            break;
        case 2:
            giveItem(player, enchant(new ItemStack(Material.CROSSBOW)));
            switch (random.nextInt(4)) {
            case 0: case 1: case 2:
                giveItem(player, enchant(new ItemStack(Material.ARROW, 64))); break;
            case 3:
                giveItem(player, enchant(new ItemStack(Material.SPECTRAL_ARROW, 64))); break;
            default: throw new IllegalStateException();
            }
            break;
        case 3:
            giveItem(player, enchant(new ItemStack(Material.TRIDENT))); break;
        case 4: giveItem(player, enchant(new ItemStack(Material.DIAMOND_SWORD))); break;
        case 5: giveItem(player, enchant(new ItemStack(Material.NETHERITE_SWORD))); break;
        case 6: giveItem(player, enchant(new ItemStack(Material.DIAMOND_AXE))); break;
        case 7: giveItem(player, enchant(new ItemStack(Material.NETHERITE_AXE))); break;
        default: throw new IllegalStateException();
        }
    }

    void giveFood(Player player) {
        switch (random.nextInt(3)) {
        case 0: giveItem(player, enchant(new ItemStack(Material.BREAD, 64))); break;
        case 1: giveItem(player, enchant(new ItemStack(Material.MELON_SLICE, 64))); break;
        case 2: giveItem(player, enchant(new ItemStack(Material.COOKED_BEEF, 64))); break;
        default: throw new IllegalStateException();
        }
    }

    void giveArmor(Player player) {
        switch (random.nextInt(12)) {
        case 0: giveItem(player, enchant(new ItemStack(Material.IRON_CHESTPLATE))); break;
        case 1: giveItem(player, enchant(new ItemStack(Material.DIAMOND_CHESTPLATE))); break;
        case 2: giveItem(player, enchant(new ItemStack(Material.NETHERITE_CHESTPLATE))); break;
        case 3: giveItem(player, enchant(new ItemStack(Material.IRON_LEGGINGS))); break;
        case 4: giveItem(player, enchant(new ItemStack(Material.DIAMOND_LEGGINGS))); break;
        case 5: giveItem(player, enchant(new ItemStack(Material.NETHERITE_LEGGINGS))); break;
        case 6: giveItem(player, enchant(new ItemStack(Material.IRON_HELMET))); break;
        case 7: giveItem(player, enchant(new ItemStack(Material.DIAMOND_HELMET))); break;
        case 8: giveItem(player, enchant(new ItemStack(Material.NETHERITE_HELMET))); break;
        case 9: giveItem(player, enchant(new ItemStack(Material.IRON_BOOTS))); break;
        case 10: giveItem(player, enchant(new ItemStack(Material.DIAMOND_BOOTS))); break;
        case 11: giveItem(player, enchant(new ItemStack(Material.NETHERITE_BOOTS))); break;
        default: throw new IllegalStateException();
        }
    }

    void giveBonus(Player player) {
        switch (random.nextInt(5)) {
        case 0: giveItem(player, new ItemStack(Material.GOLDEN_APPLE, 2)); break;
        case 1: giveItem(player, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)); break;
        case 2: giveItem(player, enchant(new ItemStack(Material.SHIELD))); break;
        case 3:
            for (int i = 0; i < 1 + random.nextInt(3); i += 1) {
                giveItem(player, potion());
            }
            break;
        case 4: {
            int amount = 1 + random.nextInt(3) + random.nextInt(3);
            for (int i = 0; i < amount; i += 1) {
                Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class, w -> {
                        w.setTamed(true);
                        w.setOwner(player);
                        w.setPersistent(false);
                        w.setRemoveWhenFarAway(true);
                    });
                removeEntities.add(wolf);
            }
            if (amount == 1) {
                player.sendMessage(ChatColor.BLUE + "You got a dog!");
            } else {
                player.sendMessage(ChatColor.BLUE + "You got " + amount + " dogs!");
            }
            break;
        }
        default: throw new IllegalStateException();
        }
    }

    ItemStack potion() {
        ItemStack item;
        switch (random.nextInt(7)) {
        case 0: case 2: case 3:
            item = new ItemStack(Material.POTION); break;
        case 4: case 5:
            item = new ItemStack(Material.SPLASH_POTION); break;
        case 6:
        default:
            item = new ItemStack(Material.LINGERING_POTION); break;
        }
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        List<PotionType> pts = Stream.of(PotionType.values())
            .filter(p -> p.getEffectType() != null)
            .collect(Collectors.toList());
        PotionType pt = pts.get(random.nextInt(pts.size()));
        boolean extended = pt.isExtendable() && random.nextBoolean();
        boolean upgraded = !extended && pt.isUpgradeable() && random.nextBoolean();
        meta.setBasePotionData(new PotionData(pt, extended, upgraded));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (tag.state == State.PLAY) {
            Bukkit.getScheduler().runTask(this, () -> player.setGameMode(GameMode.SPECTATOR));
        } else {
            Bukkit.getScheduler().runTask(this, () -> player.setGameMode(GameMode.ADVENTURE));
        }
        enter(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        exit(event.getPlayer());
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (tag.suddenDeath && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
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
        wc.environment(World.Environment.valueOf(config.getString("world.Environment")));
        wc.generateStructures(config.getBoolean("world.GenerateStructures"));
        wc.generator(config.getString("world.Generator"));
        wc.type(WorldType.valueOf(config.getString("world.WorldType")));
        getServer().createWorld(wc);
        World result = getServer().getWorld("pvparena_" + worldName);
        result.setAutoSave(false);
        return result;
    }

    World getWorld(String worldName) {
        World result = Bukkit.getWorld("pvparena_" + worldName);
        if (result == null) result = loadWorld(worldName);
        return result;
    }

    @EventHandler
    void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        if (tag.state != State.IDLE && world != null) {
            event.setSpawnLocation(spread(world.getSpawnLocation()));
        } else {
            event.setSpawnLocation(spread(lobbyWorld.getSpawnLocation()));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            if (event.getDamager() instanceof AbstractArrow && tag.specialRule == SpecialRule.ARROWS_INSTA_KILL) {
                event.setDamage(2048.0);
            }
            if (event.getDamager() instanceof Wolf && tag.specialRule == SpecialRule.DOGS_INSTA_KILL) {
                event.setDamage(2048.0);
            }
            if (event.getDamager() instanceof Player && tag.specialRule == SpecialRule.VAMPIRISM) {
                Player damager = (Player) event.getDamager();
                double dmg = event.getFinalDamage();
                double health = Math.min(damager.getMaxHealth(), damager.getHealth() + dmg);
                damager.setHealth(health);
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (tag.state != State.PLAY) return;
        if (!event.getEntity().getWorld().equals(world)) return;
        Projectile projectile = event.getEntity();
        removeEntities.add(projectile);
        projectile.setPersistent(false);
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (tag.state != State.PLAY) return;
        if (!event.getPlayer().getWorld().equals(world)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof AbstractArrow && event.getEntity().getWorld().equals(world)) {
            if (tag.state == State.PLAY && tag.specialRule == SpecialRule.EXPLOSIVE_ARROWS) {
                event.getEntity().getWorld().createExplosion(event.getEntity(), event.getEntity().getLocation(), 2.0f, false, false);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    Location spread(Location location) {
        double r = 2.0;
        return location.add(r * random.nextDouble() - r * random.nextDouble(), 0.0, r * random.nextDouble() - r * random.nextDouble());
    }

    void teleport(Player player, Location location) {
        player.teleport(spread(location));
    }

    boolean toggleSpectatorMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (spectators.contains(uuid)) {
            spectators.remove(uuid);
            return false;
        } else {
            spectators.add(uuid);
            revivePlayer(player);
            player.setGameMode(GameMode.SPECTATOR);
            return true;
        }
    }
}
