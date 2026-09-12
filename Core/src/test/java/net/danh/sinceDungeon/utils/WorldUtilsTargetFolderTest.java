package net.danh.sinceDungeon.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression cover for the 1.7.0 incident: instances were written to {@code world/dimensions/minecraft/}
 * on a server that keeps worlds beside the container, so the copied template landed where nothing reads
 * it. Bukkit then loaded the instance name from the container, found an empty directory, and generated a
 * brand new world — players were teleported to the dungeon's coordinates in raw terrain, underground and
 * in lava, instead of into the map.
 * <p>
 * The rule is simply: use the dimension layout only when this server has one.
 */
class WorldUtilsTargetFolderTest {

    @Test
    void classicLayoutPutsTheInstanceBesideTheContainer(@TempDir Path tmp) {
        File container = tmp.resolve("server").toFile();
        File dimensions = new File(new File(container, "world"), "dimensions"); // never created

        File target = WorldUtils.resolveInstanceFolder(container, dimensions, "SinceDungeon_abc");

        assertEquals(new File(container, "SinceDungeon_abc"), target,
                "a server with no dimensions directory must receive the instance in its world container,"
                        + " which is where WorldCreator will look for it");
    }

    @Test
    void dimensionLayoutPutsTheInstanceUnderTheMinecraftNamespace(@TempDir Path tmp) throws Exception {
        File container = tmp.resolve("server").toFile();
        File dimensions = new File(new File(container, "world"), "dimensions");
        if (!dimensions.mkdirs()) {
            throw new IllegalStateException("could not create " + dimensions);
        }

        File target = WorldUtils.resolveInstanceFolder(container, dimensions, "SinceDungeon_abc");

        assertEquals(new File(new File(dimensions, "minecraft"), "SinceDungeon_abc"), target);
    }

    @Test
    void aMissingDimensionsPathIsTreatedAsClassicLayout(@TempDir Path tmp) {
        File container = tmp.resolve("server").toFile();

        assertEquals(new File(container, "SinceDungeon_abc"),
                WorldUtils.resolveInstanceFolder(container, null, "SinceDungeon_abc"));
    }

    @Test
    void aFileWhereDimensionsShouldBeIsNotADimensionLayout(@TempDir Path tmp) throws Exception {
        File container = tmp.resolve("server").toFile();
        if (!container.mkdirs()) {
            throw new IllegalStateException("could not create " + container);
        }
        File dimensions = new File(container, "dimensions");
        if (!dimensions.createNewFile()) {
            throw new IllegalStateException("could not create " + dimensions);
        }

        assertEquals(new File(container, "SinceDungeon_abc"),
                WorldUtils.resolveInstanceFolder(container, dimensions, "SinceDungeon_abc"),
                "isDirectory(), not exists(): a stray file must not be mistaken for the dimension layout");
    }
}
