package com.cavetale.pvparena;

import com.cavetale.mytems.Mytems;
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
import static com.cavetale.pvparena.Items.item;
import static com.cavetale.pvparena.Items.leather;
import static com.cavetale.pvparena.Items.potion;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.bukkit.inventory.EquipmentSlot.*;

/**
 * All kits with content and some methods to deliver them.
 */
public enum Kit {
    SWORDSMAN {
        @Override public String getDescription() {
            return "This is the strongest of all the melee fighters.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.NETHERITE_SWORD);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(
                HAND, item(Material.NETHERITE_SWORD, Map.of(Enchantment.SHARPNESS, 4)),
                OFF_HAND, new ItemStack(Material.SHIELD),
                HEAD, item(Material.DIAMOND_HELMET, Map.of(Enchantment.PROTECTION, 4)),
                CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(Enchantment.PROTECTION, 4)),
                LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(Enchantment.PROTECTION, 4)),
                FEET, item(Material.DIAMOND_BOOTS, Map.of(Enchantment.PROTECTION, 4))
            );
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(
                potion(
                    Material.POTION, 1, text("Swordsman Potion", GOLD),
                    List.of(PotionEffectType.STRENGTH, PotionEffectType.HASTE),
                    List.of(ofMinutes(3), ofMinutes(3)),
                    List.of(1, 2)
                )
            );
        }
    },
    AXE_WARRIOR {
        @Override public String getDescription() {
            return "Strong armor and an axe to break down your enemies' shields.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.NETHERITE_AXE);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_AXE, Map.of(Enchantment.SHARPNESS, 5)),
                          HEAD, item(Material.DIAMOND_HELMET, Map.of(Enchantment.PROTECTION, 4)),
                          CHEST, item(Material.DIAMOND_CHESTPLATE, Map.of(Enchantment.PROTECTION, 4, Enchantment.THORNS, 1)),
                          LEGS, item(Material.DIAMOND_LEGGINGS, Map.of(Enchantment.PROTECTION, 4, Enchantment.THORNS, 1)),
                          FEET, item(Material.DIAMOND_BOOTS, Map.of(Enchantment.PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.SPLASH_POTION, PotionType.STRONG_SLOWNESS));
        }
    },
    NINJA {
        @Override public String getDescription() {
            return "Invisibility and jump boost make you a rather deadly thief.";
        }
        @Override public ItemStack getIcon() {
            return Mytems.BLIND_EYE.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.NETHERITE_SWORD, Map.of(Enchantment.SHARPNESS, 5)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(
                potion(
                    Material.POTION, 1, text("Ninja Potion", LIGHT_PURPLE),
                    List.of(PotionEffectType.INVISIBILITY, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST),
                    List.of(ofMinutes(3), ofMinutes(3), ofMinutes(3)),
                    List.of(0, 1, 2)
                )
            );
        }
    },
    ARCHER {
        @Override public String getDescription() {
            return "Take out your enemies from afar with an overpowered bow.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.BOW);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(
                HAND, item(Material.BOW, Map.of(Enchantment.POWER, 6, Enchantment.PUNCH, 5, Enchantment.INFINITY, 1)),
                HEAD, item(Material.CHAINMAIL_HELMET, Map.of(Enchantment.PROTECTION, 3)),
                CHEST, item(Material.CHAINMAIL_CHESTPLATE, Map.of(Enchantment.PROJECTILE_PROTECTION, 5)),
                LEGS, item(Material.CHAINMAIL_LEGGINGS, Map.of(Enchantment.PROJECTILE_PROTECTION, 5)),
                FEET, item(Material.CHAINMAIL_BOOTS, Map.of(Enchantment.PROTECTION, 3))
            );
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(
                new ItemStack(Material.ARROW, 64),
                potion(Material.POTION, PotionType.STRONG_SWIFTNESS)
            );
        }
    },
    CROSSBOWMAN {
        @Override public String getDescription() {
            return "Rain down a hailstorm of arrows with yoru fast charging dual crossbows.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.CROSSBOW);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            ItemStack xbow = item(Material.CROSSBOW, Map.of(Enchantment.QUICK_CHARGE, 4,
                                                            Enchantment.MULTISHOT, 1,
                                                            Enchantment.PIERCING, 1));
            return Map.of(HAND, xbow.clone(),
                          OFF_HAND, xbow.clone(),
                          HEAD, item(Material.IRON_HELMET, Map.of(Enchantment.PROTECTION, 3)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(Enchantment.PROTECTION, 3)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.TIPPED_ARROW, 16, PotionType.HARMING),
                           new ItemStack(Material.ARROW, 64));
        }
    },
    TANK {
        @Override public String getDescription() {
            return "Impenetrable armor turns you into a walking fortress.";
        }
        @Override public ItemStack getIcon() {
            return Mytems.DEFLECTOR_SHIELD.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(
                HAND, item(Material.IRON_SWORD, Map.of()),
                OFF_HAND, Mytems.DEFLECTOR_SHIELD.createItemStack(),
                HEAD, item(Material.NETHERITE_HELMET, Map.of(Enchantment.PROTECTION, 5)),
                CHEST, item(Material.NETHERITE_CHESTPLATE, Map.of(Enchantment.PROTECTION, 5)),
                LEGS, item(Material.NETHERITE_LEGGINGS, Map.of(Enchantment.PROTECTION, 5)),
                FEET, item(Material.NETHERITE_BOOTS, Map.of(Enchantment.PROTECTION, 5))
            );
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(
                new ItemStack(Material.TOTEM_OF_UNDYING),
                potion(Material.POTION, PotionType.STRONG_TURTLE_MASTER)
            );
        }
    },
    POTION_MASTER {
        @Override public String getDescription() {
            return "Throw deadly potions at your enemies.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.LINGERING_POTION);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.BOW, Map.of(Enchantment.FLAME, 1)),
                          HEAD, item(Material.GOLDEN_HELMET, Map.of(Enchantment.PROTECTION, 3)),
                          CHEST, item(Material.GOLDEN_CHESTPLATE, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          LEGS, item(Material.GOLDEN_LEGGINGS, Map.of(Enchantment.PROJECTILE_PROTECTION, 4)),
                          FEET, item(Material.GOLDEN_BOOTS, Map.of(Enchantment.FIRE_PROTECTION, 4)));
        }
        @Override public List<ItemStack> getInventoryItems() {
            return List.of(potion(Material.LINGERING_POTION, 1, text("Lingering Potion of Slow Harming"),
                                  List.of(PotionEffectType.INSTANT_DAMAGE, PotionEffectType.SLOWNESS),
                                  List.of(ofSeconds(1), ofSeconds(10)),
                                  List.of(1, 1)),
                           potion(Material.LINGERING_POTION, 1, text("Lingering Potion of Slow Poison"),
                                  List.of(PotionEffectType.POISON, PotionEffectType.SLOWNESS),
                                  List.of(ofSeconds(10), ofSeconds(10)),
                                  List.of(1, 1)),
                           potion(Material.LINGERING_POTION, 1, text("Lingering Potion of Slow Blindness"),
                                  List.of(PotionEffectType.BLINDNESS, PotionEffectType.SLOWNESS),
                                  List.of(ofSeconds(15), ofSeconds(10)),
                                  List.of(0, 1)));
        }

        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.SPECTRAL_ARROW, 64));
        }
    },
    TRIDENT_THROWER {
        @Override public String getDescription() {
            return "Your loyal trident will instill fear into the minds of your foes.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.TRIDENT);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(HAND, item(Material.TRIDENT, Map.of(Enchantment.LOYALTY, 3,
                                                              Enchantment.CHANNELING, 1,
                                                              Enchantment.IMPALING, 5)),
                          HEAD, item(Material.IRON_HELMET, Map.of(Enchantment.PROTECTION, 4)),
                          CHEST, item(Material.IRON_CHESTPLATE, Map.of(Enchantment.PROTECTION, 4)),
                          LEGS, item(Material.IRON_LEGGINGS, Map.of(Enchantment.PROTECTION, 4)),
                          FEET, item(Material.IRON_BOOTS, Map.of(Enchantment.PROTECTION, 4)));
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of();
        }
    },
    HEALER {
        @Override public String getDescription() {
            return "When you drink a potion, its effects are given to your entire team.";
        }
        @Override public ItemStack getIcon() {
            return Mytems.HEART.createIcon();
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            final Color c = Color.fromRGB(0xFFFFFF);
            return Map.of(
                HEAD, leather(Material.LEATHER_HELMET, c, Map.of(Enchantment.PROTECTION, 5)),
                HAND, item(
                    Material.COPPER_SWORD,
                    Map.of(
                        Enchantment.SHARPNESS, 4,
                        Enchantment.FIRE_ASPECT, 1
                    )
                ),
                CHEST, leather(Material.LEATHER_CHESTPLATE, c, Map.of(Enchantment.PROTECTION, 5)),
                LEGS, leather(Material.LEATHER_LEGGINGS, c, Map.of(Enchantment.PROTECTION, 5)),
                FEET, leather(Material.LEATHER_BOOTS, c, Map.of(Enchantment.PROTECTION, 5))
            );
        }
        @Override public List<ItemStack> getInventoryItems() {
            return List.of(
                potion(Material.LINGERING_POTION, PotionType.STRONG_HEALING),
                potion(Material.POTION, 1, text("Healer Potion", RED),
                       List.of(PotionEffectType.REGENERATION),
                       List.of(ofSeconds(3)),
                       List.of(3)
                ),
                potion(Material.POTION, 1, text("Healer Potion", RED),
                       List.of(PotionEffectType.REGENERATION),
                       List.of(ofSeconds(3)),
                       List.of(3)
                ),
                potion(Material.POTION, 1, text("Healer Potion", RED),
                       List.of(PotionEffectType.REGENERATION),
                       List.of(ofSeconds(3)),
                       List.of(3)
                ),
                potion(Material.POTION, 1, text("Healer Potion", RED),
                       List.of(PotionEffectType.REGENERATION),
                       List.of(ofSeconds(3)),
                       List.of(3)
                ),
                potion(Material.POTION, 1, text("Healer Potion", RED),
                       List.of(PotionEffectType.REGENERATION),
                       List.of(ofSeconds(3)),
                       List.of(3)
                )
            );
        }
    },
    ROCKETEER {
        @Override public String getDescription() {
            return "Soar throug the skies and stomp your enemies into the ground.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.FIREWORK_ROCKET);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(
                HAND, item(Material.CROSSBOW, Map.of(Enchantment.QUICK_CHARGE, 3)),
                HEAD, item(Material.COPPER_HELMET, Map.of(Enchantment.BLAST_PROTECTION, 3)),
                CHEST, new ItemStack(Material.ELYTRA),
                LEGS, item(Material.COPPER_LEGGINGS, Map.of(Enchantment.BLAST_PROTECTION, 3)),
                FEET, Mytems.STOMPERS.createItemStack()
            );
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(potion(Material.POTION, PotionType.SLOW_FALLING),
                           rocket(16),
                           new ItemStack(Material.TNT, 32));
        }
    },
    HAMMERITE {
        @Override public String getDescription() {
            return "Jump around like a bunny and hit your enemies with a mace.";
        }
        @Override public ItemStack getIcon() {
            return new ItemStack(Material.MACE);
        }
        @Override public Map<EquipmentSlot, ItemStack> getEquipmentItems() {
            return Map.of(
                HAND, item(
                    Material.MACE,
                    Map.of(
                        Enchantment.DENSITY, 3,
                        Enchantment.WIND_BURST, 3
                    )
                ),
                HEAD, Mytems.EASTER_HELMET.createItemStack(),
                CHEST, Mytems.EASTER_CHESTPLATE.createItemStack(),
                LEGS, Mytems.EASTER_LEGGINGS.createItemStack(),
                FEET, Mytems.EASTER_BOOTS.createItemStack()
            );
        }
        @Override public List<ItemStack> getRespawnItems() {
            return List.of(new ItemStack(Material.WIND_CHARGE, 8));
        }
    }
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

    public String getDescription() {
        return "";
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
     * Currently unused because we give a new kit on every death.
     * See commit `Give new kit after death`
     */
    public void onRespawn(Player player) {
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().removeItemAnySlot(item);
        }
        for (ItemStack item : getRespawnItems()) {
            player.getInventory().addItem(item);
        }
    }

    private static ItemStack rocket(final int amount) {
        ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET, amount);
        rocket.editMeta(m -> {
                FireworkMeta meta = (FireworkMeta) m;
                meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL)
                               .withColor(Color.RED).build());
                meta.setPower(4);
            });
        return rocket;
    }
}
