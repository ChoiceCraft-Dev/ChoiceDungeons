package net.danh.sinceDungeon.resources;

import net.danh.sinceDungeon.guis.editor.EditorSession;
import net.danh.sinceDungeon.testsupport.Resources;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A setting added to the Editor GUI without language entries renders as its raw enum name
 * with placeholder lore, in every locale. The GUI's fallbacks hide it from the developer
 * who added it, so it is only ever noticed by a server owner.
 */
class EditorSettingLabelsTest {

    @Test
    void everyEditorSettingHasANameAndLoreInEveryLocale() {
        List<String> problems = new ArrayList<>();

        for (String locale : Resources.LOCALES) {
            YamlConfiguration editor = Resources.language(locale, "editor.yml");

            for (EditorSession.SettingOption option : EditorSession.SettingOption.values()) {
                String name = "editor.items." + option.getLangKey();
                String lore = name + "_lore";

                if (editor.getString(name, "").isEmpty()) {
                    problems.add(locale + " is missing " + name);
                }
                if (editor.getStringList(lore).isEmpty()) {
                    problems.add(locale + " is missing " + lore);
                }
            }
        }

        assertTrue(problems.isEmpty(), "editor settings without labels:\n  " + String.join("\n  ", problems));
    }

    @Test
    void everyTypedInputSettingHasAnInputPrompt() {
        List<String> problems = new ArrayList<>();

        for (String locale : Resources.LOCALES) {
            YamlConfiguration editor = Resources.language(locale, "editor.yml");

            for (EditorSession.SettingOption option : EditorSession.SettingOption.values()) {
                // Only the types that ask the player to type a value use a prompt.
                if (!List.of("INT", "STRING", "LOCATION").contains(option.getDataType())) {
                    continue;
                }
                // EditorMenuListener builds the key as "edit_" + enum name, lowercased; the
                // prompts are grouped under editor.input, so match on the trailing segment.
                String suffix = ".edit_" + option.name().toLowerCase(Locale.ROOT);
                boolean present = editor.getKeys(true).stream().anyMatch(key -> key.endsWith(suffix));

                if (!present) {
                    problems.add(locale + " has no input prompt ending in " + suffix
                            + " for setting " + option.name());
                }
            }
        }

        assertTrue(problems.isEmpty(), "editor settings without input prompts:\n  " + String.join("\n  ", problems));
    }
}
