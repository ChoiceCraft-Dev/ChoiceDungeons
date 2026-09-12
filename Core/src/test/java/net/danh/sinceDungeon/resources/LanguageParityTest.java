package net.danh.sinceDungeon.resources;

import net.danh.sinceDungeon.testsupport.Resources;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LanguageManager falls back to an inline English default when a key is missing, so a
 * translation gap shows up as English text in a Vietnamese or Chinese server rather than
 * as an error. Nothing else catches it.
 */
class LanguageParityTest {

    private static Set<String> leafKeys(YamlConfiguration config) {
        Set<String> keys = new TreeSet<>();
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    @Test
    void everyLanguageFileParsesAndIsNotEmpty() {
        for (String locale : Resources.LOCALES) {
            for (String file : Resources.LANGUAGE_FILES) {
                YamlConfiguration config = Resources.language(locale, file);
                assertFalse(config.getKeys(true).isEmpty(), locale + "/" + file + " is empty or failed to parse");
            }
        }
    }

    @Test
    void translationsCoverEveryEnglishKey() {
        List<String> problems = new ArrayList<>();

        for (String file : Resources.LANGUAGE_FILES) {
            Set<String> english = leafKeys(Resources.language("en", file));

            for (String locale : Resources.LOCALES) {
                if (locale.equals("en")) {
                    continue;
                }
                Set<String> translated = leafKeys(Resources.language(locale, file));

                Set<String> missing = new TreeSet<>(english);
                missing.removeAll(translated);
                for (String key : missing) {
                    problems.add(locale + "/" + file + " is missing " + key);
                }

                Set<String> extra = new TreeSet<>(translated);
                extra.removeAll(english);
                for (String key : extra) {
                    problems.add(locale + "/" + file + " has " + key + ", which en does not");
                }
            }
        }

        assertTrue(problems.isEmpty(), "language files have drifted apart:\n  " + String.join("\n  ", problems));
    }
}
