package com.cavetale.pvparena;

import com.cavetale.sidebar.PlayerSidebarEvent;
import com.cavetale.sidebar.Priority;
import java.util.*;
import java.util.stream.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

public final class PVPArenaPlugin extends JavaPlugin implements Listener {
    boolean playing = false;
    int gameTime = 0;
    Map<UUID, Integer> scores = new HashMap<>();
    Random random = new Random();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::tick, 1, 1);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String alias,
                             final String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
        case "start":
            startGame((Player) sender);
            sender.sendMessage("started");
            return true;
        default:
            return false;
        }
    }

    void tick() {
        if (!playing) return;
        gameTime += 1;
        List<Player> alive = getAlive();
        if (alive.isEmpty()) {
            getLogger().info("The game is a draw!");
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendTitle(ChatColor.RED + "Draw",
                                 ChatColor.RED + "Nobody survives");
                target.sendMessage(ChatColor.RED + "Draw! Nobody survives.");
            }
            playing = false;
            return;
        }
        if (alive.size() == 1) {
            Player winner = alive.get(0);
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendTitle(ChatColor.GREEN + winner.getName(),
                                 ChatColor.GREEN + "Wins this round!");
                target.sendMessage(ChatColor.GREEN + winner.getName() + " wins this round!");
                target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.MASTER, 0.2f, 2.0f);
            }
            playing = false;
            return;
        }
        if (gameTime < 200 && gameTime % 20 == 0) {
            int seconds = (200 - gameTime) / 20;
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendActionBar(ChatColor.RED + "PvP begins in " + seconds);
            }
        }
    }

    void addScore(Player player, int score) {
        Integer sc = scores.get(player.getUniqueId());
        int newScore = sc == null ? score : sc + score;
        scores.put(player.getUniqueId(), newScore);
    }

    int getScore(Player player) {
        Integer sc = scores.get(player.getUniqueId());
        return sc != null ? sc : 0;
    }

    void startGame(Player player) {
        scores.clear();
        World world = player.getWorld();
        for (Player target : getServer().getOnlinePlayers()) {
            target.setHealth(20.0);
            target.setFoodLevel(20);
            target.teleport(world.getSpawnLocation());
            target.setGameMode(GameMode.SURVIVAL);
            getServer().dispatchCommand(getServer().getConsoleSender(), "ml add " + target.getName());
            target.getInventory().clear();
            giveWeapons(target);
        }
        playing = true;
        gameTime = 0;
        getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:effect clear @a");
    }        

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
        Player player = event.getEntity();
        player.getInventory().clear();
        getServer().getScheduler().runTask(this, () -> {
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setGameMode(GameMode.SPECTATOR);
            });
        Player killer = player.getKiller();
        if (killer != null) addScore(killer, 1);
    }

    public List<Player> getAlive() {
        return getServer().getOnlinePlayers().stream()
            .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
            .filter(Player::isValid)
            .collect(Collectors.toList());
    }

    @EventHandler
    public void onPlayerSidebar(PlayerSidebarEvent event) {
        List<String> ls = new ArrayList<>();
        if (playing) ls.add(ChatColor.GREEN + "Alive " + ChatColor.WHITE + getAlive().size());
        List<Player> scores = getServer().getOnlinePlayers().stream()
            .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
            .sorted((b, a) -> Integer.compare(getScore(a), getScore(b)))
            .collect(Collectors.toList());
        for (Player player : scores) {
            ls.add("" + ChatColor.YELLOW + getScore(player) + " " + player.getName());
        }
        if (ls.isEmpty()) return;
        event.addLines(this, Priority.DEFAULT, ls);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!playing || gameTime < 200) {
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
        switch (random.nextInt(3)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_SWORD))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_SWORD))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_SWORD))); break;
        }
        switch (random.nextInt(4)) {
        case 0: case 1:
            player.getInventory().addItem(enchant(new ItemStack(Material.BOW))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.CROSSBOW))); break;
        case 3: player.getInventory().addItem(enchant(new ItemStack(Material.TRIDENT))); break;
        }
        switch (random.nextInt(4)) {
        case 0: case 1: case 2:
            player.getInventory().addItem(enchant(new ItemStack(Material.ARROW, 64))); break;
        case 3: player.getInventory().addItem(enchant(new ItemStack(Material.SPECTRAL_ARROW, 64))); break;
        }
        switch (random.nextInt(3)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.BREAD, 64))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.MELON_SLICE, 64))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.COOKED_BEEF, 64))); break;
        }
        switch (random.nextInt(5)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_CHESTPLATE))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_CHESTPLATE))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_CHESTPLATE))); break;
        }
        switch (random.nextInt(5)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_LEGGINGS))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_LEGGINGS))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_LEGGINGS))); break;
        }
        switch (random.nextInt(5)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_HELMET))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_HELMET))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_HELMET))); break;
        }
        switch (random.nextInt(5)) {
        case 0: player.getInventory().addItem(enchant(new ItemStack(Material.IRON_BOOTS))); break;
        case 1: player.getInventory().addItem(enchant(new ItemStack(Material.DIAMOND_BOOTS))); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.NETHERITE_BOOTS))); break;
        }
        switch (random.nextInt(10)) {
        case 0: player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2)); break;
        case 1: player.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)); break;
        case 2: player.getInventory().addItem(enchant(new ItemStack(Material.SHIELD))); break;
        }
        for (int i = 0; i < 1 + random.nextInt(3); i += 1) {
            player.getInventory().addItem(potion());
        }
        if (random.nextInt(10) == 0) {
            player.getWorld().spawn(player.getLocation(), Wolf.class, w -> {
                    w.setTamed(true);
                    w.setOwner(player);
                });
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
        if (playing) event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }
}
