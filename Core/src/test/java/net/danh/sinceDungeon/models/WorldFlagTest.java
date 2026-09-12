package net.danh.sinceDungeon.models;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The parsing contract matters because an unset flag must fall through to the global
 * value: a per-dungeon section that silently reported "false" for flags it never
 * mentioned would switch off vanilla behaviour nobody asked to switch off.
 */
class WorldFlagTest {

    private static YamlConfiguration sectionWith(String... keyValuePairs) {
        YamlConfiguration cfg = new YamlConfiguration();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            cfg.set("world-flags." + keyValuePairs[i], Boolean.parseBoolean(keyValuePairs[i + 1]));
        }
        return cfg;
    }

    @Test
    void absentSectionYieldsNoOverrides() {
        assertTrue(WorldFlag.fromSection(null).isEmpty(), "a dungeon with no world-flags block overrides nothing");
    }

    @Test
    void onlyExplicitlySetFlagsAreOverridden() {
        YamlConfiguration cfg = sectionWith("leaf-decay", "false", "fluid-flow", "true");

        Map<WorldFlag, Boolean> flags = WorldFlag.fromSection(cfg.getConfigurationSection("world-flags"));

        assertEquals(2, flags.size());
        assertEquals(Boolean.FALSE, flags.get(WorldFlag.LEAF_DECAY));
        assertEquals(Boolean.TRUE, flags.get(WorldFlag.FLUID_FLOW));
        assertNull(flags.get(WorldFlag.CROP_GROWTH), "an unset flag must fall through to the global value");
    }

    @Test
    void nonBooleanValuesAreIgnoredRatherThanCoerced() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("world-flags.leaf-decay", "false");
        cfg.set("world-flags.crop-growth", 0);

        Map<WorldFlag, Boolean> flags = WorldFlag.fromSection(cfg.getConfigurationSection("world-flags"));

        assertTrue(flags.isEmpty(), "a quoted or numeric value is a config mistake, not an override");
    }

    @Test
    void unknownKeysAreIgnored() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("world-flags.not-a-flag", true);

        assertTrue(WorldFlag.fromSection(cfg.getConfigurationSection("world-flags")).isEmpty());
    }

    @Test
    void resultCannotBeMutatedByCallers() {
        YamlConfiguration cfg = sectionWith("leaf-decay", "false");
        Map<WorldFlag, Boolean> flags = WorldFlag.fromSection(cfg.getConfigurationSection("world-flags"));

        assertThrows(UnsupportedOperationException.class, () -> flags.put(WorldFlag.BLOCK_FADE, true));
    }

    @Test
    void defaultsKeepDungeonMapsStaticExceptFluidFlow() {
        for (WorldFlag flag : WorldFlag.values()) {
            boolean expected = flag == WorldFlag.FLUID_FLOW;
            assertEquals(expected, flag.getDefaultValue(),
                    flag + " default changed — maps rely on water and lava still flowing, and on nothing else moving");
        }
    }

    @Test
    void configKeysAreUniqueAndKebabCase() {
        Set<String> seen = new HashSet<>();
        for (WorldFlag flag : WorldFlag.values()) {
            String key = flag.getKey();
            assertTrue(key.matches("[a-z]+(-[a-z]+)*"), "not a kebab-case config key: " + key);
            assertTrue(seen.add(key), "duplicate world flag key: " + key);
        }
    }
}
