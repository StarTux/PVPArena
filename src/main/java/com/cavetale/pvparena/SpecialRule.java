package com.cavetale.pvparena;

public enum SpecialRule {
    ARROW_DOUBLE_DAMAGE("Double arrow damage"),
    ARROW_VAMPIRISM("Arrow hits heal"),
    ARROW_NO_DAMAGE("Arrows deal no damage"),
    CREEPER_REVENGE("Dying spawns creeper"),
    //DOGS_INSTA_KILL("Dogs are deadly"),
    HEAL_ON_KILL("Every Kill Heals"),
    NONE("Regular combat"),
    POTION_ON_KILL("Kills give potion effect"),
    SHUFFLE_ON_KILL("Kills shuffle players"),
    VAMPIRISM("Vampirism"),
    ZOMBIECALYPSE("Zombie Apocalypse"),
    ;

    public final String displayName;

    SpecialRule(final String dn) {
        this.displayName = dn;
    }
}
