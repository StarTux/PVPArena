package com.cavetale.pvparena;

import com.cavetale.mytems.Mytems;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static com.cavetale.pvparena.Items.*;
import static org.bukkit.inventory.EquipmentSlot.*;

/**
 * All kits with content and some methods to deliver them.
 */
public enum Kit {
    SWORDSMAN {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.NETHERITE_SWORD);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_SWORD, Map.of(SHARPNESS, 4)),
                          HEAD, item(Material.DIAMOND_HELMET, Map.of(PROTECTION, 4)),
                          CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(PROTECTION, 4)),
                          LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(PROTECTION, 4)),
                          FEET, item(Material.DIAMOND_BOOTS, Map.of(PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.STRENGTH, PotionPotency.EXTENDED));
        }
    },
    AXE_WARRIOR {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.NETHERITE_AXE);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_AXE, Map.of(SHARPNESS, 5)),
                          OFF_HAND, item(Material.SHIELD, Map.of()),
                          HEAD, item(Material.DIAMOND_HELMET, Map.of(PROTECTION, 4, Enchantment.THORNS, 1)),
                          CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(PROTECTION, 4, Enchantment.THORNS, 1)),
                          LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(PROTECTION, 4, Enchantment.THORNS, 1)),
                          FEET, item(Material.DIAMOND_BOOTS, Map.of(PROTECTION, 4, Enchantment.THORNS, 1)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.SPLASH_POTION, PotionType.SLOWNESS, PotionPotency.UPGRADED));
        }
    },
    NINJA {
        @Override public ItemStack getIcon() {
            return Mytems.BLIND_EYE.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_SWORD, Map.of(SHARPNESS, 5)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.INVISIBILITY, PotionPotency.EXTENDED),
                           potion(Material.POTION, PotionType.STRENGTH, PotionPotency.UPGRADED),
                           potion(Material.POTION, PotionType.NIGHT_VISION, PotionPotency.EXTENDED));
        }
    },
    ARCHER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.BOW);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.BOW, Map.of(POWER, 5, PUNCH, 5, INFINITY, 1)),
                          HEAD, item(Material.CHAINMAIL_HELMET, Map.of(PROTECTION, 3)),
                          CHEST, item(Material.CHAINMAIL_CHESTPLATE, Map.of(PROJECTILE_PROTECTION, 5)),
                          LEGS, item(Material.CHAINMAIL_LEGGINGS, Map.of(PROJECTILE_PROTECTION, 5)),
                          FEET, item(Material.CHAINMAIL_BOOTS, Map.of(PROTECTION, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.ARROW, 64),
                           potion(Material.POTION, PotionType.SPEED, PotionPotency.UPGRADED));
        }
    },
    CROSSBOWMAN {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.CROSSBOW);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            ItemStack xbow = item(Material.CROSSBOW, Map.of(Enchantment.QUICK_CHARGE, 4,
                                                            Enchantment.MULTISHOT, 1,
                                                            Enchantment.PIERCING, 1));
            return Map.of(HAND, xbow.clone(),
                          OFF_HAND, xbow.clone(),
                          HEAD, item(Material.IRON_HELMET, Map.of(PROTECTION, 3)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(PROTECTION, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            ItemStack damageArrow = potion(Material.TIPPED_ARROW, PotionType.INSTANT_DAMAGE, PotionPotency.UPGRADED);
            damageArrow.setAmount(64);
            return List.of(damageArrow);
        }
    },
    TANK {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.SHIELD);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.IRON_SWORD, Map.of()),
                          OFF_HAND, Mytems.VENGEANCE_SHIELD.createItemStack(),
                          HEAD, item(Material.NETHERITE_HELMET, Map.of(PROTECTION, 5)),
                          CHEST, item(Material.NETHERITE_CHESTPLATE, Map.of(PROTECTION, 5)),
                          LEGS, item(Material.NETHERITE_LEGGINGS, Map.of(PROTECTION, 5)),
                          FEET, item(Material.NETHERITE_BOOTS, Map.of(PROTECTION, 5)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.TURTLE_MASTER, PotionPotency.UPGRADED),
                           potion(Material.POTION, PotionType.FIRE_RESISTANCE, PotionPotency.UPGRADED));
        }
    },
    POTION_MASTER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.LINGERING_POTION);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.BOW, Map.of(FLAME, 1)),
                          HEAD, item(Material.GOLDEN_HELMET, Map.of(PROTECTION, 3)),
                          CHEST, item(Material.GOLDEN_CHESTPLATE, Map.of(PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.GOLDEN_LEGGINGS, Map.of(PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.GOLDEN_BOOTS, Map.of(FIRE_PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.LINGERING_POTION, PotionType.INSTANT_DAMAGE, PotionPotency.UPGRADED),
                           potion(Material.LINGERING_POTION, PotionEffectType.HARM, Duration.ofSeconds(1), 2),
                           potion(Material.LINGERING_POTION, PotionEffectType.POISON, Duration.ofSeconds(60), 1),
                           potion(Material.LINGERING_POTION, PotionEffectType.GLOWING, Duration.ofSeconds(30), 0),
                           potion(Material.LINGERING_POTION, PotionEffectType.BLINDNESS, Duration.ofSeconds(10), 0),
                           new ItemStack(Material.SPECTRAL_ARROW, 64));
        }
    },
    TRIDENT_THROWER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.TRIDENT);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.TRIDENT, Map.of(Enchantment.LOYALTY, 3,
                                                              Enchantment.CHANNELING, 1,
                                                              Enchantment.IMPALING, 5)),
                          HEAD, item(Material.IRON_HELMET, Map.of(PROTECTION, 4)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of();
        }
    },
    HEALER {
        @Override public ItemStack getIcon() {
            return Mytems.HEART.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            final Color c = Color.fromRGB(0xFFFFFF);
            return Map.of(HEAD, leather(Material.LEATHER_HELMET, c, Map.of(PROTECTION, 3)),
                          CHEST, leather(Material.LEATHER_CHESTPLATE, c, Map.of(PROJECTILE_PROTECTION, 4)),
                          LEGS, leather(Material.LEATHER_LEGGINGS, c, Map.of(PROJECTILE_PROTECTION, 4)),
                          FEET, leather(Material.LEATHER_BOOTS, c, Map.of(FIRE_PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, PotionPotency.UPGRADED),
                           potion(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, PotionPotency.UPGRADED),
                           potion(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, PotionPotency.UPGRADED),
                           potion(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, PotionPotency.UPGRADED),
                           potion(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, PotionPotency.UPGRADED),
                           potion(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, PotionPotency.UPGRADED),
                           potion(Material.SPLASH_POTION, PotionType.REGEN, PotionPotency.EXTENDED),
                           potion(Material.SPLASH_POTION, PotionType.REGEN, PotionPotency.EXTENDED),
                           potion(Material.SPLASH_POTION, PotionType.REGEN, PotionPotency.EXTENDED));
        }
    },
    ROCKETEER {
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.FIREWORK_ROCKET);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET, 64);
            rocket.editMeta(m -> {
                    FireworkMeta meta = (FireworkMeta) m;
                    meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL)
                                   .withColor(Color.RED).build());
                });
            return Map.of(HAND, new ItemStack(Material.CROSSBOW),
                          OFF_HAND, rocket,
                          CHEST, new ItemStack(Material.ELYTRA));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.SLOW_FALLING, PotionPotency.UPGRADED),
                           new ItemStack(Material.FIREWORK_ROCKET, 64),
                           new ItemStack(Material.FIREWORK_ROCKET, 64));
        }
    },
    ;

    public abstract ItemStack getIcon();

    public abstract Map<EquipmentSlot, ItemStack> getEquipmentItems();

    public List<ItemStack> getInventoryItems() {
        return List.of();
    }

    public List<ItemStack> getRespawnItems() {
        return List.of();
    }

    public List<ItemStack> getAllItems() {
        List<ItemStack> result = new ArrayList<>();
        result.addAll(getEquipmentItems().values());
        result.addAll(getInventoryItems());
        result.addAll(getRespawnItems());
        return result;
    }

    public String getDisplayName() {
        return toCamelCase(" ", this);
    }

    /**
     * Call this when the player should get the kit.
     */
    public void apply(Player player) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : getEquipmentItems().entrySet()) {
            player.getEquipment().setItem(entry.getKey(), entry.getValue());
        }
        for (ItemStack item : getInventoryItems()) {
            player.getInventory().addItem(item);
        }
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().addItem(item);
        }
    }

    /**
     * Call this when the kit owner respawns.
     */
    public void onRespawn(Player player) {
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().removeItemAnySlot(item);
        }
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().addItem(item);
        }
    }
}
