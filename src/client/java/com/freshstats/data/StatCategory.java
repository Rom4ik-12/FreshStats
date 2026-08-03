package com.freshstats.data;

import net.minecraft.text.Text;

public enum StatCategory {
    TRAVEL("travel", 0xFF00D2FF),
    COMBAT("combat", 0xFFFF453A),
    TRADING("trading", 0xFFFFD700),
    AGRICULTURE("agriculture", 0xFF30D158),
    BUILDING("building", 0xFFBF5AF2),
    MINING("mining", 0xFFFF9F0A);

    private final String id;
    private final int color;

    StatCategory(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public Text getText() {
        return Text.translatable("freshstats.category." + id);
    }

    public String getDisplayName() {
        return getText().getString();
    }

    public Text getDescriptionText() {
        return Text.translatable("freshstats.category." + id + ".desc");
    }

    public Text getMainUnitText() {
        return Text.translatable("freshstats.category." + id + ".unit");
    }
}
