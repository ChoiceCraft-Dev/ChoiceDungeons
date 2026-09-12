---
layout: page
title: Configuration
---

SinceDungeon loads Core configuration from `config.yml`, plus setting files under `settings/`. Dungeon-specific files
can override many gameplay settings per map.

Core configuration is stored in:

```text
plugins/SinceDungeon/config.yml
```

Premium configuration is stored in:

```text
plugins/SinceDungeon-PremiumAddon/config.yml
```

## Startup

```yaml
startup:
  async-timeout-seconds: 30
```

This controls how long the plugin waits for async startup data loading before failing startup. Increase it if the
database is remote or the server has many dungeon files.

## Cross-Server

```yaml
cross-server:
  enabled: false
  transfer-timeout-seconds: 30
  server-name: "dungeon-node-1"
  bungee-channel: "BungeeCord"
  return-server: "lobby"
```

Cross-server mode is marked experimental in the default config. It uses proxy plugin messaging and Redis Pub/Sub so a
lobby server can request a dungeon run on a dungeon node.

## Redis

```yaml
redis:
  host: "localhost"
  port: 6379
  password: ""
  channel: "SinceDungeon"
```

All servers in the same network must use the same Redis channel.

## Database

```yaml
database:
  type: "sqlite"
  host: "localhost"
  port: 3306
  database: "sincedungeonpremium"
  username: "root"
  password: ""
```

Supported backends:

- `sqlite`: local file storage, recommended for small servers.
- `mysql`: remote database, recommended for large or networked servers.

HikariCP pool settings are available under `database.pool`.

Full pool example:

```yaml
database:
  type: "sqlite"
  host: "localhost"
  port: 3306
  database: "sincedungeonpremium"
  username: "root"
  password: ""
  pool:
    max-size: 10
    min-idle: 2
    max-lifetime: 1800000
    timeout: 5000
```

Database stores:

- Fastest solo clear records
- Fastest party clear records
- Kill records
- Clear count records
- Player lives
- Player cooldowns

## Commands

Core command labels can be changed:

```yaml
commands:
  party: "party"
  party-aliases: [ "p", "pt" ]
  dungeon: "dungeon"
  dungeon-aliases: [ "dg", "inst" ]
  admin: "sincedungeon"
  admin-aliases: [ "sincedungeonpremium", "sd", "sdungeon" ]
```

Premium's command is fixed as `/sdp`, with alias `/sdpremium`.

## Locale and Debug

```yaml
settings:
  locale: "en"
  debug: false
  clear-remaining-mobs-on-action-complete: true
```

Available bundled locales include English, Vietnamese, and Chinese.

Language files are modular under:

```text
plugins/SinceDungeon/languages/<locale>/
```

## Party System

```yaml
party:
  max-members: 4
  allow-friendly-fire: false
  max-join-distance: 50.0
  reward-share-mode: "EQUAL"
  system-name: "System"
  invite-timeout: 60
```

Reward share modes:

- `EQUAL`: all eligible party members receive rewards.
- `LEADER_ONLY`: only the party leader receives reward chests.

## Dungeon Gameplay Defaults

```yaml
dungeon:
  lobby-countdown: 10
  template-folder: "dungeons"
  world-prefix: "SinceDungeon_"
  death-action: "RESPAWN"
  top-awarded-to: "ALL_MEMBERS"
  clear-mob-drops: true
  save-and-restore-stats: false
  out-of-lives-action: "SPECTATE"
```

Important options:

| Option                | Meaning                                                 |
|-----------------------|---------------------------------------------------------|
| `lobby-countdown`     | Delay before an instance starts.                        |
| `template-folder`     | Folder containing template worlds.                      |
| `world-prefix`        | Prefix for generated dungeon instance worlds.           |
| `death-action`        | Default death behavior.                                 |
| `out-of-lives-action` | Behavior when a player has no lives left.               |
| `top-awarded-to`      | Whether all members or leader only receive top entries. |

Gameplay restrictions:

```yaml
dungeon:
  gameplay:
    keep-inventory-on-death: true
    prevent-item-dropping: true
    block-ender-pearls: true
    block-commands: true
    block-teleport-commands: false
    allowed-commands:
      - "/party"
      - "/p"
      - "/dungeon"
      - "/sincedungeon"
      - "/sincedungeonpremium"
    empty-dungeon-timeout: 300
```

### Disconnects and Rejoining

`dungeon.gameplay.empty-dungeon-timeout` is how many seconds a player who disconnects mid-run keeps their spot.
Dungeon files can override it with `settings.empty-dungeon-timeout`.

- The default is `300` (5 minutes). `0` restores the old behaviour: the player is removed straight away and an empty
  run ends.
- Above `0`, the player's spot, inventory and saved pre-dungeon state are held. Remaining teammates are told how long
  the spot is held. If nobody is left online the run pauses: stage logic stops ticking and the current objective's time
  limit is extended by the paused time when someone returns.
- Logging back in within the timeout puts the player back in their run where they left off.
- If the timeout runs out, `cooldown-on-leave` and `lives-deducted-on-leave` are charged then, and an empty run ends as
  failed. They are not charged to a player who comes back in time.
- The hold only applies once the run has started (not during the lobby countdown or after a clear), and it is kept
  in memory, so a server restart ends held runs.

Any player who logs out inside a dungeon is tagged in their player data. If they log back in after their run has
ended (timeout expired, run finished, server restarted, world deleted or schematic area cleared), they are
teleported back to where they were before the dungeon (or the main world spawn), with fall, void and suffocation
damage blocked for a few seconds so they cannot fall to their death.

### World Flags

`dungeon.world-flags` switches off vanilla behaviour inside the dungeon worlds the plugin creates (Core world copies
and the Premium shared schematic world). `true` keeps the normal behaviour; `false` turns it off. Dungeon files can
override any flag under `settings.world-flags`; in the shared schematic world each run uses its own dungeon's flags.

`weather-cycle` is the one exception, because weather belongs to a whole world rather than to one instance
inside it. In a copied per-run world the dungeon's own override applies; in a shared schematic world, where
several runs are in progress at once, only the global value can apply.
The defaults keep dungeon maps static: everything is off except `fluid-flow`. Set a flag to `true` to bring the vanilla
behaviour back.

```yaml
dungeon:
  world-flags:
    leaf-decay: false
    crop-growth: false
    tree-growth: false
    block-spread: false
    block-fade: false
    block-form: false
    fluid-flow: true
    natural-mob-spawning: false
    weather-cycle: false
```

| Flag                   | Controls                                                                   |
|------------------------|----------------------------------------------------------------------------|
| `leaf-decay`           | Leaves decaying away from logs.                                            |
| `crop-growth`          | Crops, sugar cane, cactus, melons, pumpkins and similar plants growing.   |
| `tree-growth`          | Saplings and mushrooms growing into trees (including bone meal).           |
| `block-spread`         | Grass, mycelium, vines, sculk and similar blocks spreading.                |
| `block-fade`           | Ice and snow melting, coral dying, farmland drying out.                    |
| `block-form`           | Snow and ice forming, copper oxidising, concrete and cobblestone forming.  |
| `fluid-flow`           | Water and lava flowing.                                                    |
| `natural-mob-spawning` | Natural, patrol, trap and village-defence spawns. Dungeon waves still spawn. |
| `weather-cycle`        | Rain and thunder starting on their own. Plugin and command changes still apply. |

Fire spread, explosions and mob griefing are already blocked in dungeon worlds regardless of these flags.

## Lives

```yaml
lives:
  default-max-lives: 3
  default-start-lives: 3
  regen-interval-seconds: 3600
  regen-amount: 1
```

Players can spend lives to join dungeons and lose lives on death depending on dungeon settings. Dungeon files can set
`lives-deducted-per-death` plus outcome costs for leave, fail, and clear; outcome costs are independent from
`cooldown-on-leave`.

## Items

Configurable built-in items:

- `items.key`
- `items.compass`
- `items.cooldown_reset`
- `items.cooldown_reduce`
- `items.life_crystal`

Item options include material, name, lore, glowing, rarity, flags, custom model data, max stack size, sounds, and
particles.

## Menus

Menu settings control the editor, reward GUI, and leaderboard GUI:

```yaml
editor:
  nav-item: "ARROW"
  limits:
    max-locations: 50
    max-mob-amount: 200
    max-radius: 100.0

reward:
  session-expire-seconds: 300

leaderboard:
  fetch-limit: 50
  gui-size: 54
  date-format: "dd/MM/yyyy HH:mm"
```

## Generated Settings Files

Additional Core settings are split into:

```text
settings/actions.yml
settings/effects.yml
settings/gameplay.yml
settings/items.yml
settings/menus.yml
```

These files control GUI items, gameplay defaults, effects, menu layouts, and action defaults used by the editor.
