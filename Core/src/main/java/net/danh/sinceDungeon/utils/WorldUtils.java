package net.danh.sinceDungeon.utils;

import net.danh.sinceDungeon.SinceDungeon;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.logging.Level;

/**
 * Advanced operations targeting system I/O, World duplication, and recursive cleanup logic.
 * Incorporates Retry-Delete loops to bypass OS-level file locks.
 */
public class WorldUtils {

    private static final ArrayList<String> IGNORE_FILES = new ArrayList<>(Arrays.asList(
            "uid.dat", "session.lock", "playerdata", "stats", "advancements", "poi", "entities", "datapacks", "metadata.dat"
    ));

    /**
     * Forcefully duplicates the entirety of a valid directory folder matching typical Bukkit formats.
     *
     * @param source Target folder to duplicate from.
     * @param target End destination for contents.
     * @return Boolean matching completion state.
     */
    public static boolean copyWorld(File source, File target) {
        if (!source.exists()) return false;
        try {
            Files.walkFileTree(source.toPath(), EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (IGNORE_FILES.contains(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    Path targetDir = target.toPath().resolve(source.toPath().relativize(dir));
                    try {
                        Files.createDirectories(targetDir);
                    } catch (FileAlreadyExistsException e) {
                        if (!Files.isDirectory(targetDir)) throw e;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (IGNORE_FILES.contains(file.getFileName().toString())) {
                        return FileVisitResult.CONTINUE;
                    }
                    Files.copy(file, target.toPath().resolve(source.toPath().relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            SinceDungeon.getPlugin().getLogger().log(Level.WARNING, "Failed to copy world " + source.getName() + " to " + target.getName(), e);
            return false;
        }
    }

    /**
     * Recursively executes forceful deletions on an entire system directory safely bypassing rigid system locks.
     * Utilizes a minor retry loop to ensure OS file handles are completely released.
     *
     * @param path The origin path node to sever.
     * @return Returns true upon a fully successful execution.
     */
    public static boolean deleteWorld(File path) {
        if (!path.exists()) return true;

        int retries = 3;
        while (retries > 0 && path.exists()) {
            try {
                Files.walkFileTree(path.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            Files.delete(file);
                        } catch (IOException ignored) {
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        try {
                            Files.delete(dir);
                        } catch (IOException ignored) {
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                if (!path.exists()) return true;
            } catch (IOException ignored) {
            }

            retries--;
            try {
                Thread.sleep(500); // Backoff for OS file lock release
            } catch (InterruptedException ignored) {
            }
        }
        return !path.exists();
    }

    /**
     * Resolves the world template folder. Checks the root container first, then searches within world/dimensions.
     */
    public static File getTemplateFolder(String templateName) {
        File container = org.bukkit.Bukkit.getWorldContainer();
        File defaultSource = new File(container, templateName);
        if (defaultSource.exists() && defaultSource.isDirectory()) {
            return defaultSource;
        }

        File dimensionsFolder = new File(container, "world/dimensions");
        if (dimensionsFolder.exists() && dimensionsFolder.isDirectory()) {
            File[] namespaces = dimensionsFolder.listFiles(File::isDirectory);
            if (namespaces != null) {
                for (File namespace : namespaces) {
                    File potentialWorld = new File(namespace, templateName);
                    if (potentialWorld.exists() && potentialWorld.isDirectory()) {
                        return potentialWorld;
                    }
                }
            }
        }
        return defaultSource;
    }

    /**
     * Resolves the correct target folder for a new world.
     * Paper 1.20+ with Vanilla world layout places new worlds in world/dimensions/minecraft/
     */
    public static File getTargetFolder(String instanceId) {
        File container = org.bukkit.Bukkit.getWorldContainer();
        File dimensions;

        try {
            // Paper 1.20.6+ (v26.1+) API to get the correct level directory
            java.lang.reflect.Method getLevelDirectory = org.bukkit.Server.class.getMethod("getLevelDirectory");
            java.nio.file.Path levelDir = (java.nio.file.Path) getLevelDirectory.invoke(org.bukkit.Bukkit.getServer());
            dimensions = new File(levelDir.toFile(), "dimensions");
        } catch (Exception e) {
            // Fallback to Spigot / Older versions
            dimensions = new File(org.bukkit.Bukkit.getWorlds().get(0).getWorldFolder(), "dimensions");
        }

        return resolveInstanceFolder(container, dimensions, instanceId);
    }

    /**
     * Decides where a copied instance world belongs, given where this server keeps its worlds.
     * <p>
     * The dimension layout is used ONLY when the server actually has one. The Paper API that reports it
     * exists on every modern build, so trusting the API alone wrote instances into
     * {@code world/dimensions/minecraft/} on servers that keep worlds beside the container — and since
     * {@code WorldCreator} then loads the instance by name FROM the container and finds nothing there, the
     * server generated a fresh world instead. Players were teleported to the dungeon's configured
     * coordinates in raw generated terrain: underground, inside blocks, sometimes in lava.
     * <p>
     * Package-private and file-only so the decision can be tested without a running server.
     */
    static File resolveInstanceFolder(File container, File dimensions, String instanceId) {
        if (dimensions != null && dimensions.isDirectory()) {
            return new File(new File(dimensions, "minecraft"), instanceId);
        }
        return new File(container, instanceId);
    }

    /**
     * Ensures that the target world folder has a level.dat file.
     * Sub-dimensions in Vanilla do not have their own level.dat. If missing, we copy it from the main world
     * so Bukkit recognizes it as a valid world instead of generating a completely new one.
     */
    public static void ensureLevelDat(File targetWorldFolder) {
        File levelDat = new File(targetWorldFolder, "level.dat");
        if (!levelDat.exists()) {
            File mainLevelDat = new File(org.bukkit.Bukkit.getWorlds().get(0).getWorldFolder(), "level.dat");
            if (mainLevelDat.exists()) {
                try {
                    Files.copy(mainLevelDat.toPath(), levelDat.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    SinceDungeon.getPlugin().getLogger().warning("Could not copy level.dat to instance: " + e.getMessage());
                }
            }
        }
    }
}
