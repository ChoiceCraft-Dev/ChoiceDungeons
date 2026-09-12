package net.danh.sinceDungeon.resources;

import net.danh.sinceDungeon.models.WorldFlag;
import net.danh.sinceDungeon.testsupport.Resources;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shipped templates are what a new server copies and what owners edit by hand, so a
 * broken one is somebody's first experience of the plugin. DungeonLoader reads them with
 * no schema, and an unreadable key is simply ignored.
 */
class ShippedDungeonTemplatesTest {

    private static final List<String> TEMPLATES = List.of("example_dungeon.yml", "schematic_test_arena.yml");

    @Test
    void everyShippedTemplateParsesAndNamesAWorld() {
        for (String template : TEMPLATES) {
            YamlConfiguration config = Resources.load("/dungeons/" + template);

            assertFalse(config.getKeys(true).isEmpty(), template + " is empty or failed to parse");
            assertFalse(config.getString("template-world", "").isEmpty(),
                    template + " has no template-world, so the instance provider has nothing to load");
            assertNotNull(config.getConfigurationSection("settings"), template + " has no settings block");
            assertNotNull(config.getConfigurationSection("stages"), template + " has no stages block");
        }
    }

    @Test
    void templateWorldFlagOverridesUseKeysTheCodeReads() {
        Set<String> known = new HashSet<>();
        for (WorldFlag flag : WorldFlag.values()) {
            known.add(flag.getKey());
        }

        for (String template : TEMPLATES) {
            ConfigurationSection flags = Resources.load("/dungeons/" + template)
                    .getConfigurationSection("settings.world-flags");
            if (flags == null) {
                continue;
            }
            for (String key : flags.getKeys(false)) {
                assertTrue(known.contains(key),
                        template + " overrides an unknown world flag '" + key + "', which is silently ignored");
                assertTrue(flags.isBoolean(key),
                        template + " sets world flag '" + key + "' to a non-boolean, which is silently ignored");
            }
        }
    }

    @Test
    void templateTimeoutsAreNotNegative() {
        for (String template : TEMPLATES) {
            YamlConfiguration config = Resources.load("/dungeons/" + template);
            if (!config.contains("settings.empty-dungeon-timeout")) {
                continue;
            }
            assertTrue(config.getInt("settings.empty-dungeon-timeout") >= 0,
                    template + " has a negative empty-dungeon-timeout");
        }
    }
}
