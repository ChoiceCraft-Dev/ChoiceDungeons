package net.danh.sinceDungeon.systems.state;

import net.danh.sinceDungeon.SinceDungeon;
import net.danh.sinceDungeon.utils.ColorUtils;
import net.danh.sinceDungeon.utils.SchedulerCompat;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Holds a player's pre-dungeon state on disk while they are offline.
 * <p>
 * Entering a {@code save-and-restore-stats} dungeon empties the player's inventory, armour and XP;
 * the only copy lives in the running game. If that game ends while the player is away — their rejoin
 * hold expires, the run is cleaned up, or the server restarts — the in-memory copy goes with it and
 * the items are gone for good. Parking the snapshot here means the next login can hand it back.
 * <p>
 * One small YAML file per player, written synchronously so a shutdown cannot outrun it.
 */
public final class PendingRestoreStore {

    private static final String FOLDER = "pending-restores";
    private static final int FORMAT = 1;

    private PendingRestoreStore() {
    }

    /**
     * @param restoreStats whether the run actually took the player's inventory; when false only
     *                     confiscated items are worth returning.
     */
    public record Snapshot(boolean restoreStats, GameMode gameMode, double health, int foodLevel,
                           int level, float exp, ItemStack[] contents, ItemStack[] armor,
                           ItemStack[] extra, List<ItemStack> confiscated) {
    }

    private static File folder(SinceDungeon plugin) {
        return new File(plugin.getDataFolder(), FOLDER);
    }

    private static File fileFor(SinceDungeon plugin, UUID uuid) {
        return new File(folder(plugin), uuid + ".yml");
    }

    /**
     * Writes the snapshot, replacing any earlier one for this player. A second dungeon cannot
     * overwrite a pending restore with an empty one: callers only reach here holding real state.
     */
    public static void save(SinceDungeon plugin, UUID uuid, Snapshot snapshot) {
        if (plugin == null || uuid == null || snapshot == null) return;

        File dir = folder(plugin);
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().severe("Could not create " + dir.getPath() + "; a player's saved items are at risk");
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.set("format", FORMAT);
        config.set("saved-at", System.currentTimeMillis());
        config.set("restore-stats", snapshot.restoreStats());

        if (snapshot.restoreStats()) {
            config.set("gamemode", snapshot.gameMode() != null ? snapshot.gameMode().name() : null);
            config.set("health", snapshot.health());
            config.set("food", snapshot.foodLevel());
            config.set("level", snapshot.level());
            config.set("exp", snapshot.exp());
            config.set("contents", snapshot.contents() != null ? Arrays.asList(snapshot.contents()) : null);
            config.set("armor", snapshot.armor() != null ? Arrays.asList(snapshot.armor()) : null);
            config.set("extra", snapshot.extra() != null ? Arrays.asList(snapshot.extra()) : null);
        }
        if (snapshot.confiscated() != null && !snapshot.confiscated().isEmpty()) {
            config.set("confiscated", snapshot.confiscated());
        }

        try {
            config.save(fileFor(plugin, uuid));
            plugin.getLogger().info("Parked pre-dungeon items for " + uuid + " until their next login");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to park pre-dungeon items for " + uuid, e);
        }
    }

    public static boolean has(SinceDungeon plugin, UUID uuid) {
        return fileFor(plugin, uuid).isFile();
    }

    /**
     * Gives a joining player their parked state back and deletes the file. Does nothing when there
     * is none, so it is safe to call on every join.
     */
    @SuppressWarnings("unchecked")
    public static void applyIfPresent(SinceDungeon plugin, Player player) {
        if (plugin == null || player == null) return;

        File file = fileFor(plugin, player.getUniqueId());
        if (!file.isFile()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        boolean restoreStats = config.getBoolean("restore-stats", false);

        List<ItemStack> contents = (List<ItemStack>) (List<?>) config.getList("contents", new ArrayList<>());
        List<ItemStack> armor = (List<ItemStack>) (List<?>) config.getList("armor", new ArrayList<>());
        List<ItemStack> extra = (List<ItemStack>) (List<?>) config.getList("extra", new ArrayList<>());
        List<ItemStack> confiscated = (List<ItemStack>) (List<?>) config.getList("confiscated", new ArrayList<>());
        String gameMode = config.getString("gamemode");
        double health = config.getDouble("health", -1);
        int food = config.getInt("food", -1);
        int level = config.getInt("level", -1);
        double exp = config.getDouble("exp", -1);

        SchedulerCompat.runAtEntity(plugin, player, () -> {
            if (!player.isOnline()) return;

            if (restoreStats) {
                if (gameMode != null) {
                    try {
                        player.setGameMode(GameMode.valueOf(gameMode));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (health >= 0) {
                    AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
                    double max = attr != null ? attr.getValue() : 20.0;
                    player.setHealth(Math.max(1.0, Math.min(health, max)));
                }
                if (food >= 0) player.setFoodLevel(food);
                if (level >= 0) player.setLevel(level);
                if (exp >= 0) player.setExp((float) exp);

                if (!contents.isEmpty()) player.getInventory().setContents(contents.toArray(new ItemStack[0]));
                if (!armor.isEmpty()) player.getInventory().setArmorContents(armor.toArray(new ItemStack[0]));
                if (!extra.isEmpty()) player.getInventory().setExtraContents(extra.toArray(new ItemStack[0]));
            }

            for (ItemStack item : confiscated) {
                if (item == null) continue;
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
                for (ItemStack overflow : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), overflow);
                }
            }

            player.updateInventory();

            String msg = plugin.getLanguageManager().getString("game.items_returned",
                    "&aYour items from your last Dungeon run have been returned.");
            if (msg != null && !msg.isEmpty()) {
                player.sendMessage(ColorUtils.parseWithPrefix(msg));
            }

            // Only after the items are actually in the inventory, so a failure mid-way keeps the file.
            if (!file.delete()) {
                plugin.getLogger().warning("Could not delete " + file.getPath()
                        + "; the player may receive these items again on their next login");
            }
        });
    }
}
