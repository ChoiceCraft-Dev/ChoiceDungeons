package net.danh.sinceDungeon.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses the numeric Minecraft prefix from Bukkit's version string. */
final class MinecraftVersionParser {
    private static final Pattern PREFIX = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    private MinecraftVersionParser() {
    }

    static int[] parse(String value) {
        Matcher match = PREFIX.matcher(value == null ? "" : value.trim());
        if (!match.find()) throw new IllegalArgumentException("No Minecraft version prefix: " + value);
        return new int[] {
                Integer.parseInt(match.group(1)),
                Integer.parseInt(match.group(2)),
                match.group(3) == null ? 0 : Integer.parseInt(match.group(3))
        };
    }
}
