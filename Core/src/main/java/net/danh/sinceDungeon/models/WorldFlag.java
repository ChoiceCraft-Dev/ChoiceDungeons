package net.danh.sinceDungeon.models;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Vanilla world behaviours that can be switched off inside dungeon worlds.
 * A flag set to true keeps the vanilla behaviour, false cancels it.
 * Enforced through events so it also works per-run inside a shared schematic world.
 */
public enum WorldFlag {
    LEAF_DECAY("leaf-decay", false),
    CROP_GROWTH("crop-growth", false),
    TREE_GROWTH("tree-growth", false),
    BLOCK_SPREAD("block-spread", false),
    BLOCK_FADE("block-fade", false),
    BLOCK_FORM("block-form", false),
    // Kept on by default: maps often rely on flowing water or lava
    FLUID_FLOW("fluid-flow", true),
    NATURAL_MOB_SPAWNING("natural-mob-spawning", false),
    WEATHER_CYCLE("weather-cycle", false);

    private final String key;
    private final boolean defaultValue;

    WorldFlag(String key, boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    /**
     * Reads only the flags explicitly set in the section, so unset flags fall back to the global value.
     */
    public static Map<WorldFlag, Boolean> fromSection(ConfigurationSection section) {
        if (section == null) return Collections.emptyMap();
        Map<WorldFlag, Boolean> flags = new EnumMap<>(WorldFlag.class);
        for (WorldFlag flag : values()) {
            if (section.isBoolean(flag.key)) {
                flags.put(flag, section.getBoolean(flag.key));
            }
        }
        return Collections.unmodifiableMap(flags);
    }
}
