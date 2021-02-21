package com.cavetale.pvparena;

public enum SpecialRule {
    NONE("Regular combat"),
    HEAL_ON_KILL("Every Kill Heals"),
    GEAR_ON_KILL("Kills drop extra gear"),
    POTION_ON_KILL("Kills give potion effect"),
    ENCHANT_ON_KILL("Kills enchant your gear"),
    ARROWS_INSTA_KILL("Arrows are deadly"),
    DOGS_INSTA_KILL("Dogs are deadly"),
    VAMPIRISM("Vampirism"),
    ARROW_VAMPIRISM("Arrow Hits Heal"),
    CREEPER_REVENGE("Dying spawns a creeper"),
    SHUFFLE_ON_KILL("Kills shuffle players"),
    ZOMBIECALYPSE("Zombie Apocalypse"),
    EXPLOSIVE_ARROWS("Explosive Arrows"),
    DEATH_BUFF("Dying makes you Stronger");

    public final String displayName;

    SpecialRule(final String dn) {
        this.displayName = dn;
    }
}
