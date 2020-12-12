package com.cavetale.pvparena;

import com.cavetale.sidebar.PlayerSidebarEvent;
import com.cavetale.sidebar.Priority;
import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class PVPArenaPlugin extends JavaPlugin implements Listener {
    World world;
    Tag tag;
    Random random = new Random();
    List<Entity> removeEntities = new ArrayList<>();
    final String SPECTATOR = "Cavetale";

    static final class Tag {
        State state = State.IDLE;
        int gameTime = 0;
        boolean suddenDeath = false;
        int endTime = 0;
        Map<UUID, Integer> scores = new HashMap<>();
        Map<UUID, Integer> lives = new HashMap<>();
        List<String> worlds = new ArrayList<>();
        String worldName = null;
        int worldUsed = 0;
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
        DOGS_INSTA_KILL("Dogs are deadly");

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
        loadTag();
        if (tag.worldName != null) {
            world = getWorld(tag.worldName);
        }
    }

    @Override
    public void onDisable() {
        saveTag();
        stopGame();
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
            startGame();
            sender.sendMessage("started");
            return true;
        case "stop":
            stopGame();
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
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        if (tag.state == State.IDLE) return;
        if (tag.state == State.END) {
            tag.endTime += 1;
            if (tag.endTime > 200) startGame();
            return;
        }
        if (tag.state == State.PLAY) tickPlay();
    }

    void tickPlay() {
        tag.gameTime += 1;
        List<Player> alive = getAlive();
        if (alive.isEmpty()) {
            getLogger().info("The game is a draw!");
            for (Player target : Bukkit.getOnlinePlayers()) {
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
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendTitle(ChatColor.GREEN + winner.getName(),
                                 ChatColor.GREEN + "Wins this round!");
                target.sendMessage(ChatColor.GREEN + winner.getName() + " wins this round!");
                target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.1f, 2.0f);
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "titles unlockset " + winner.getName() + " Champion");
            endGame();
            return;
        }
        if (tag.gameTime == 200) {
            for (Player target : getServer().getOnlinePlayers()) {
                target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.2f, 1.2f);
                target.sendTitle(ChatColor.DARK_RED + "Fight!", ChatColor.RED + tag.specialRule.displayName, 0, 20, 0);
                target.sendMessage(ChatColor.DARK_RED + "Fight! " + tag.specialRule.displayName);
                target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 0.1f, 1.2f);
            }
        }
        if (tag.gameTime < 200 && tag.gameTime % 20 == 0) {
            int seconds = (200 - tag.gameTime) / 20;
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendActionBar(ChatColor.RED + "PvP begins in " + seconds);
            }
        }
        if (tag.gameTime == 20 * 90) {
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendMessage(ChatColor.DARK_RED + "Sudden Death!");
                target.sendTitle("", ChatColor.DARK_RED + "Sudden Death!", 0, 20, 0);
                target.playSound(target.getLocation(), Sound.BLOCK_BELL_USE, SoundCategory.MASTER, 0.2f, 0.7f);
            }
            tag.suddenDeath = true;
        }
        if (tag.suddenDeath && tag.gameTime % 40 == 0) {
            for (Player target : getServer().getOnlinePlayers()) {
                if (target.getGameMode() != GameMode.SPECTATOR) {
                    target.damage(1.0);
                }
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
        for (Player target : getServer().getOnlinePlayers()) {
            if (target.getName().equals(SPECTATOR)) continue;
            resetPlayer(target);
            giveWeapons(target);
            target.teleport(world.getSpawnLocation());
            tag.scores.computeIfAbsent(target.getUniqueId(), u -> 0);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ml add " + target.getName());
            if (tag.specialRule == SpecialRule.LIVES) {
                tag.lives.put(target.getUniqueId(), 3);
            }
        }
        tag.state = State.PLAY;
        tag.gameTime = 0;
        tag.suddenDeath = false;
    }

    void resetPlayer(Player target) {
        target.getInventory().clear();
        target.setHealth(20.0);
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setGameMode(GameMode.ADVENTURE);
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }
    }

    void endGame() {
        if (tag.state != State.PLAY) return;
        tag.state = State.END;
        tag.endTime = 0;
        tag.worldUsed += 1;
        for (Entity e : removeEntities) e.remove();
        removeEntities.clear();
        saveTag();
    }

    void stopGame() {
        if (tag.state == State.IDLE) return;
        tag.state = State.IDLE;
        for (Entity e : removeEntities) e.remove();
        removeEntities.clear();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
        Player player = event.getEntity();
        boolean revive = false;
        if (tag.specialRule == SpecialRule.LIVES) {
            Integer lives = tag.lives.get(player.getUniqueId());
            if (lives == null || lives <= 0) {
                revive = false;
            } else {
                tag.lives.put(player.getUniqueId(), lives - 1);
                revive = true;
            }
        }
        if (revive) {
            player.sendMessage(ChatColor.RED + "Lives left: " + tag.lives.get(player.getUniqueId()));
            event.setCancelled(true);
            resetPlayer(player);
            getServer().getScheduler().runTask(this, () -> {
                    resetPlayer(player);
                    giveWeapons(player);
                    player.setGameMode(GameMode.ADVENTURE);
                    player.teleport(world.getSpawnLocation());
                });
        } else {
            getServer().getScheduler().runTask(this, () -> {
                    player.setHealth(20.0);
                    player.setFoodLevel(20);
                    player.setGameMode(GameMode.SPECTATOR);
                });
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
                giveWeapons(killer);
                killer.sendMessage(ChatColor.RED + "You received extra gear");
            }
            addScore(killer, 1);
            getLogger().info(killer.getName() + " killed " + player.getName());
        }
    }

    public List<Player> getAlive() {
        return getServer().getOnlinePlayers().stream()
            .filter(p -> p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)
            .filter(Player::isValid)
            .collect(Collectors.toList());
    }

    @EventHandler
    public void onPlayerSidebar(PlayerSidebarEvent event) {
        if (tag.state == State.IDLE) return;
        List<String> ls = new ArrayList<>();
        if (tag.state == State.PLAY) {
            int minutes = (tag.gameTime / 20) / 60;
            int seconds = (tag.gameTime / 20) % 60;
            ls.add(ChatColor.GREEN + "Time " + ChatColor.WHITE + String.format("%02d:%02d", minutes, seconds));
            ls.add(ChatColor.GREEN + "Rule " + ChatColor.RED + tag.specialRule.displayName);
            if (tag.suddenDeath) {
                ls.add("" + ChatColor.DARK_RED + ChatColor.BOLD + "Sudden Death");
            }
        }
        List<Player> list = getServer().getOnlinePlayers().stream()
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
                ls.add("" + ChatColor.GREEN + getScore(player) + " " + ChatColor.WHITE + player.getName());
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

    void giveWeapons(Player player) {
        // Weapon
        switch (random.nextInt(8)) {
        case 0:
        case 1:
            player.getInventory().addItem(enchant(new ItemStack(Material.BOW)));
            switch (random.nextInt(4)) {
            case 0: case 1: case 2:
                player.getInventory().addItem(enchant(new ItemStack(Material.ARROW, 64))); break;
            case 3:
                player.getInventory().addItem(enchant(new ItemStack(Material.SPECTRAL_ARROW, 64))); break;
            }
            break;
        case 2:
            player.getInventory().addItem(enchant(new ItemStack(Material.CROSSBOW)));
            switch (random.nextInt(4)) {
            case 0: case 1: case 2:
                player.getInventory().addItem(enchant(new ItemStack(Material.ARROW, 64))); break;
            case 3:
                player.getInventory().addItem(enchant(new ItemStack(Material.SPECTRAL_ARROW, 64))); break;
            }
            break;
        case 3:
            player.getInventory().addItem(enchant(new ItemStack(Material.TRIDENT))); break;
        case 4: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_SWORD))); break;
        case 5: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_SWORD))); break;
        case 6: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_AXE))); break;
        case 7: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_AXE))); break;
        default: break;
        }
        // Food
        switch (random.nextInt(3)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.BREAD, 64))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.MELON_SLICE, 64))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.COOKED_BEEF, 64))); break;
        }
        // Armor
        switch (random.nextInt(12)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_CHESTPLATE))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_CHESTPLATE))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_CHESTPLATE))); break;
        case 3: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_LEGGINGS))); break;
        case 4: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_LEGGINGS))); break;
        case 5: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_LEGGINGS))); break;
        case 6: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_HELMET))); break;
        case 7: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_HELMET))); break;
        case 8: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_HELMET))); break;
        case 9: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_BOOTS))); break;
        case 10: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_BOOTS))); break;
        case 11: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_BOOTS))); break;
        default: break;
        }
        // Bonus
        switch (random.nextInt(5)) {
        case 0: player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2)); break;
        case 1: player.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.SHIELD))); break;
        case 3:
            for (int i = 0; i < 1 + random.nextInt(3); i += 1) {
                player.getInventory().addItem(potion());
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
            if(!ignore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists()) {
                        if(!target.mkdirs())
                            throw new IOException("Couldn't create world directory!");
                    }
                    String[] files = source.list();
                    for(String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);

                    byte[] buffer = new byte[1024];
                    int length;

                    while((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();
                }
            }
        }
        catch (IOException e) {
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
        World world = getServer().getWorld("pvparena_" + worldName);
        world.setAutoSave(false);
        return world;
    }

    World getWorld(String worldName) {
        World result = Bukkit.getWorld("pvparena_" + worldName);
        if (result == null) result = loadWorld(worldName);
        return result;
    }

    @EventHandler
    void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        if (world != null) event.setSpawnLocation(world.getSpawnLocation());
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            if (event.getDamager() instanceof Arrow && tag.specialRule == SpecialRule.ARROWS_INSTA_KILL) {
                event.setDamage(2048.0);
            }
            if (event.getDamager() instanceof Wolf && tag.specialRule == SpecialRule.DOGS_INSTA_KILL) {
                event.setDamage(2048.0);
            }
        }
    }
}
