package net.danh.sinceDungeon.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression cover for the incident that broke dungeons twice.
 * <p>
 * {@code DefaultInstanceProvider} loads an instance with {@code new WorldCreator(instanceId)}, and Bukkit
 * resolves that name against {@link org.bukkit.Bukkit#getWorldContainer()}. The copy therefore has exactly
 * one correct destination — the container — and the vanilla {@code <level>/dimensions/<namespace>/} layout
 * is never it, however real that folder is on a given server.
 * <p>
 * 1.7.0 copied into {@code <level>/dimensions/minecraft/}: the template bytes landed in a directory nothing
 * reads, Bukkit found an empty folder under the container and generated raw terrain, and players were
 * teleported to the dungeon's coordinates underground and in lava. 1.7.1 tried to fix it by using that
 * layout only when the dimensions folder exists — which on the affected server it did (its level directory
 * is {@code skyworld}), so the bug survived and each run also leaked a full copy of the template.
 */
class WorldUtilsTargetFolderTest {

    @Test
    void theInstanceIsAlwaysWrittenBesideTheWorldContainer(@TempDir Path tmp) {
        File container = tmp.resolve("server").toFile();

        assertEquals(new File(container, "SinceDungeon_abc"),
                WorldUtils.resolveInstanceFolder(container, "SinceDungeon_abc"),
                "WorldCreator resolves the instance name against the world container, so the copy must go there");
    }

    @Test
    void anExistingDimensionsLayoutDoesNotChangeTheDestination(@TempDir Path tmp) {
        File container = tmp.resolve("server").toFile();
        File dimensions = new File(new File(container, "skyworld"), "dimensions/minecraft");
        if (!dimensions.mkdirs()) {
            throw new IllegalStateException("could not create " + dimensions);
        }

        File target = WorldUtils.resolveInstanceFolder(container, "SinceDungeon_abc");

        // Only the returned path is asserted. This function is pure path arithmetic and never touches the
        // filesystem, so checking that the dimensions folder stayed empty would pass no matter what it
        // returned — that would be false confidence, not coverage. Whether bytes actually land there is a
        // property of copyWorld, which needs a running server to exercise.
        assertEquals(new File(container, "SinceDungeon_abc"), target,
                "the production server has a real skyworld/dimensions folder; routing the copy there is what"
                        + " left Bukkit generating a fresh world under the container");
    }

    @Test
    void theInstanceNameIsUsedVerbatim(@TempDir Path tmp) {
        File container = tmp.resolve("server").toFile();

        assertEquals(new File(container, "SinceDungeon_Player_deadbeef"),
                WorldUtils.resolveInstanceFolder(container, "SinceDungeon_Player_deadbeef"),
                "the folder name is the world name Bukkit will look up; it must not be rewritten");
    }
}
