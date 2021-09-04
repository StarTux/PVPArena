package com.cavetale.pvparena;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WinRule {
    LAST_SURVIVOR(5, "Last Survivor"),
    TIMED_SCORE(10, "Most Kills"),
    MOLE(1, "Kill the Mole");

    public final int weight;
    public final String displayName;

    public String getDescription() {
        switch (this) {
        case LAST_SURVIVOR:
            return "The last player alive wins. Sudden death kicks in after a few minutes.";
        case TIMED_SCORE:
            return "When the timer ends, the player with the highest score wins.";
        case MOLE:
            return "One secret player is the mole. The mole deals double damage."
                + " You can only score by killing the mole or killing as the mole."
                + " The mole killer becomes the new mole.";
        default: return "";
        }
    }
}
