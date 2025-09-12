package com.cavetale.pvparena;

import com.cavetale.core.struct.Cuboid;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Squad {
    protected String color;
    protected String name;
    protected int score;
    protected int index;
    protected int memberCount;
    protected int alive;
    protected Cuboid spawn;

    public NamedTextColor getTextColor() {
        return NamedTextColor.NAMES.value(color);
    }

    public void setTextColor(NamedTextColor c) {
        this.color = NamedTextColor.NAMES.key(c);
    }
}
