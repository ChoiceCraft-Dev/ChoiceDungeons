package net.danh.sinceDungeon.resources;

import net.danh.sinceDungeon.models.WorldFlag;
import net.danh.sinceDungeon.testsupport.Resources;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConfigUtils copies missing default keys into a server's existing file, so a key that is
 * absent here never reaches servers that upgrade — the feature silently uses its code
 * fallback instead, and the option is undiscoverable.
 */
class GameplayDefaultsTest {

    /** Mirrors DungeonLoader's fallback; the shipped default and the code default must agree. */
    private static final int EXPECTED_EMPTY_DUNGEON_TIMEOUT = 300;

    @Test
    void everyWorldFlagShipsADefault() {
        ConfigurationSection flags = Resources.gameplay().getConfigurationSection("dungeon.world-flags");
        assertNotNull(flags, "dungeon.world-flags section is missing from settings/gameplay.yml");

        for (WorldFlag flag : WorldFlag.values()) {
            assertTrue(flags.isBoolean(flag.getKey()),
                    "no shipped default for world flag " + flag.getKey() + " — upgrading servers never receive it");
        }
    }

    @Test
    void shippedFlagDefaultsMatchTheCodeDefaults() {
        ConfigurationSection flags = Resources.gameplay().getConfigurationSection("dungeon.world-flags");
        assertNotNull(flags);

        for (WorldFlag flag : WorldFlag.values()) {
            assertEquals(flag.getDefaultValue(), flags.getBoolean(flag.getKey()),
                    flag.getKey() + " disagrees between gameplay.yml and WorldFlag, so behaviour depends on"
                            + " whether the key reached the server");
        }
    }

    @Test
    void worldFlagsSectionHasNoKeysTheCodeIgnores() {
        ConfigurationSection flags = Resources.gameplay().getConfigurationSection("dungeon.world-flags");
        assertNotNull(flags);

        Set<String> known = new HashSet<>();
        for (WorldFlag flag : WorldFlag.values()) {
            known.add(flag.getKey());
        }

        for (String key : flags.getKeys(false)) {
            assertTrue(known.contains(key), "gameplay.yml documents a world flag the code does not read: " + key);
        }
    }

    @Test
    void emptyDungeonTimeoutShipsAndMatchesTheLoaderFallback() {
        YamlConfiguration gameplay = Resources.gameplay();

        assertTrue(gameplay.isInt("dungeon.gameplay.empty-dungeon-timeout"),
                "dungeon.gameplay.empty-dungeon-timeout is missing from settings/gameplay.yml");
        assertEquals(EXPECTED_EMPTY_DUNGEON_TIMEOUT, gameplay.getInt("dungeon.gameplay.empty-dungeon-timeout"),
                "shipped timeout differs from DungeonLoader's fallback, so servers behave differently"
                        + " depending on whether the key was written to their config");
    }

    @Test
    void gameplayFileKeepsTheRootsConfigManagerRoutesToIt() {
        YamlConfiguration gameplay = Resources.gameplay();

        for (String root : new String[]{"dungeon", "party", "lives"}) {
            assertNotNull(gameplay.getConfigurationSection(root),
                    "ConfigManager routes '" + root + ".*' to settings/gameplay.yml, but the section is gone");
        }
    }
}
