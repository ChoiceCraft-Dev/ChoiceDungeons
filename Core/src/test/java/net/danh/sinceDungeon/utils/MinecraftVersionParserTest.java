package net.danh.sinceDungeon.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MinecraftVersionParserTest {
    @Test
    void parsesPaper26VersionWithoutAReportedPatch() {
        assertArrayEquals(new int[] {26, 3, 0},
                MinecraftVersionParser.parse("26.3-R0.1-SNAPSHOT"));
    }

    @Test
    void parsesPaper26VersionWithAPatchAndBuildSuffix() {
        assertArrayEquals(new int[] {26, 3, 0},
                MinecraftVersionParser.parse("26.3.0+205"));
    }

    @Test
    void keepsTheLegacyThreePartMinecraftVersion() {
        assertArrayEquals(new int[] {1, 21, 11},
                MinecraftVersionParser.parse("1.21.11-R0.1-SNAPSHOT"));
    }

    @Test
    void rejectsAStringWithoutANumericMinecraftPrefix() {
        assertThrows(IllegalArgumentException.class,
                () -> MinecraftVersionParser.parse("unknown"));
    }
}
