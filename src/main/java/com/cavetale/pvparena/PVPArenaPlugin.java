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
    List<Entity> removeEntities = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::tick, 1, 1);
    }

    @Override
    public void onDisable() {
        stopGame();
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
        case "stop":
            stopGame();
            sender.sendMessage("stopped");
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
            stopGame();
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
            stopGame();
            return;
        }
        if (gameTime == 200) {
            for (Player target : getServer().getOnlinePlayers()) {
                target.playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.2f, 1.2f);
                target.sendTitle("", ChatColor.DARK_RED + "Fight!", 0, 20, 0);
                target.sendMessage(ChatColor.DARK_RED + "Fight!");
            }
        }
        if (gameTime < 200 && gameTime % 20 == 0) {
            int seconds = (200 - gameTime) / 20;
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendActionBar(ChatColor.RED + "PvP begins in " + seconds);
            }
        }
        if (gameTime == 20 * 90) {
            for (Player target : getServer().getOnlinePlayers()) {
                target.sendMessage(ChatColor.DARK_RED + "Sudden Death!");
                target.sendTitle("", ChatColor.DARK_RED + "Sudden Death!", 0, 20, 0);
            }
        }
        if (gameTime > 20 * 90 && gameTime % 40 == 0) {
            for (Player target : getServer().getOnlinePlayers()) {
                if (target.getGameMode() != GameMode.SPECTATOR) {
                    target.damage(1.0);
                }
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
            if (target.getName().equals("Cavetale")) continue;
            target.setHealth(20.0);
            target.setFoodLevel(20);
            target.teleport(world.getSpawnLocation());
            target.setGameMode(GameMode.SURVIVAL);
            target.getInventory().clear();
            giveWeapons(target);
            for (PotionEffect effect : target.getActivePotionEffects()) {
                target.removePotionEffect(effect.getType());
            }
            scores.put(target.getUniqueId(), 0);
        }
        playing = true;
        gameTime = 0;
    }

    void stopGame() {
        if (!playing) return;
        playing = false;
        for (Entity e : removeEntities) e.remove();
        removeEntities.clear();
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
        if (killer != null) {
            addScore(killer, 1);
            for (ItemStack item : killer.getInventory()) {
                if (item == null || item.getType() == Material.AIR) continue;
                enchant(item);
            }
            getLogger().info(killer.getName() + " killed " + player.getName());
        }
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
        if (playing) {
            int minutes = (gameTime / 20) / 60;
            int seconds = (gameTime / 20) % 60;
            ls.add(ChatColor.GREEN + "Time " + ChatColor.WHITE + String.format("%02d:%02d", minutes, seconds));
            ls.add(ChatColor.GREEN + "Alive " + ChatColor.WHITE + getAlive().size());
        }
        List<Player> list = getServer().getOnlinePlayers().stream()
            .filter(p -> scores.containsKey(p.getUniqueId()))
            .sorted((b, a) -> Integer.compare(getScore(a), getScore(b)))
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
        if (playing) event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }
}
