package net.danh.sinceDungeon.models;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The legacy constructor is the fork's API promise to third-party integrations compiled
 * against the pre-1.7.0 record. It must keep working AND must not quietly enable the
 * rejoin hold for callers that have never heard of it.
 */
class DungeonTemplateSettingsTest {

    private static DungeonTemplate.Settings legacySettings() {
        return new DungeonTemplate.Settings(
                true, true, true, 10,
                true, false, "RESPAWN", true,
                1, 1, 0, 0, 0,
                false, 4, 600, false,
                List.of(), List.of(), List.of(), "NONE",
                true, "NONE", List.of());
    }

    @Test
    void legacyConstructorDisablesTheRejoinHold() {
        assertEquals(0, legacySettings().emptyDungeonTimeout(),
                "a caller that predates the timeout must get the old behaviour, not a 5 minute hold");
    }

    @Test
    void legacyConstructorSetsNoWorldFlagOverrides() {
        assertTrue(legacySettings().worldFlags().isEmpty(),
                "no overrides means every flag falls through to the global value");
    }

    @Test
    void legacyConstructorPreservesEveryOtherSetting() {
        DungeonTemplate.Settings settings = legacySettings();

        assertEquals(10, settings.kickDelayAfterFinish());
        assertEquals("RESPAWN", settings.deathAction());
        assertEquals(4, settings.maxPlayers());
        assertEquals(600, settings.cooldownSeconds());
        assertEquals("NONE", settings.requiredItem());
        assertTrue(settings.keepInventoryOnDeath());
    }

    @Test
    void canonicalConstructorKeepsTheValuesItIsGiven() {
        DungeonTemplate.Settings settings = new DungeonTemplate.Settings(
                true, true, true, 10,
                true, false, "RESPAWN", true,
                1, 1, 0, 0, 0,
                false, 4, 600, false,
                List.of(), List.of(), List.of(), "NONE",
                true, "NONE", List.of(), 300,
                Map.of(WorldFlag.LEAF_DECAY, false));

        assertEquals(300, settings.emptyDungeonTimeout());
        assertEquals(Boolean.FALSE, settings.worldFlags().get(WorldFlag.LEAF_DECAY));
        assertEquals(1, settings.worldFlags().size());
    }
}
