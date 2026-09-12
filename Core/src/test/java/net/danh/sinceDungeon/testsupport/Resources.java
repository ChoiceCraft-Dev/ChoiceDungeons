package net.danh.sinceDungeon.testsupport;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Loads the plugin's own shipped resources off the test classpath.
 * These are the files a server actually receives, so the tests assert against
 * the real defaults rather than a copy that can drift.
 */
public final class Resources {

    public static final List<String> LOCALES = List.of("en", "vi", "zh");

    public static final List<String> LANGUAGE_FILES = List.of(
            "admin.yml", "cooldown.yml", "cross_server.yml", "editor.yml", "error.yml",
            "game.yml", "general.yml", "lives.yml", "party.yml", "reward.yml", "top.yml");

    private Resources() {
    }

    public static YamlConfiguration load(String path) {
        try (InputStream in = Resources.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing shipped resource: " + path);
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("could not read " + path, e);
        }
    }

    public static YamlConfiguration language(String locale, String file) {
        return load("/languages/" + locale + "/" + file);
    }

    public static YamlConfiguration gameplay() {
        return load("/settings/gameplay.yml");
    }
}
