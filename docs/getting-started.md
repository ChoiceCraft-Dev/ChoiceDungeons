---
layout: page
title: Getting Started
---

## Installation

Requirements:

- Paper server compatible with the plugin build target.
- Java 25 or newer (the servers run Java 25; the plugin is compiled for it).
- PlaceholderAPI is optional but recommended for conditions and placeholders.
- MythicMobs is optional for Mythic actions.
- MMOItems is optional for MMOItems rewards/items.
- SinceDungeon Premium requires SinceDungeon Core.
- Premium schematic mode requires WorldEdit or FastAsyncWorldEdit.
- Premium hologram leaderboards use native TextDisplay entities and do not require DecentHolograms.

Core installation:

1. Stop the server.
2. Place `SinceDungeon.jar` in the server `plugins/` folder.
3. Start the server once to generate plugin files.
4. Stop the server before editing large YAML files.
5. Edit `plugins/SinceDungeon/config.yml`.
6. Create dungeon files inside `plugins/SinceDungeon/dungeons/`.
7. Start the server and run `/dungeon join <dungeon_id>`.

For Premium features, also place `SinceDungeon-PremiumAddon.jar` in `plugins/`. Premium requires Core to be installed
and enabled.

Premium installation:

1. Install SinceDungeon Core first.
2. Place `SinceDungeon-PremiumAddon.jar` in `plugins/`.
3. Start the server once.
4. Edit `plugins/SinceDungeon-PremiumAddon/config.yml`.
5. If using schematic mode, place files in `plugins/SinceDungeon-PremiumAddon/schematics/`.

Premium schematic shared-world mode works on both Paper and Folia. On Folia, Core's default full-world cloning relies on
runtime world creation, which Folia does not support; use schematic mode with a preloaded shared world instead. Core
template-world dungeons are validated during load, editor save, and join so unsupported Folia setups are blocked before
runtime.

## Generated Files

Core generates:

```text
plugins/SinceDungeon/
  config.yml
  data.db
  dungeons/
  languages/
  settings/
```

Premium generates:

```text
plugins/SinceDungeon-PremiumAddon/
  config.yml
  messages.yml
  schematics/
```

## Build From Source

The project uses Gradle (run on Java 21) and compiles with a Java 25 toolchain against Paper 26.2.

```bash
./gradlew build
```

Artifacts are generated in:

```text
build/libs/
```

Expected artifact names:

- `SinceDungeon-<version>.jar`
- `SinceDungeon-PremiumAddon-<version>.jar`

## First Dungeon Checklist

- Create or copy a world folder for the dungeon template.
- Set `template-world` in the dungeon file to that folder name.
- Keep `public: true` if regular players should see and join it. Use `public: false` for private test dungeons; admins
  can still join them.
- Add at least one stage with at least one action.
- Add reward tiers and reward pool entries if completion should grant loot.
- Test with an operator account first.

Minimal dungeon:

```yaml
template-world: "ForgottenCrypt_Template"
public: true

settings:
  max-players: 4
  required-lives-to-join: 1
  lives-deducted-per-death: 1
  lives-deducted-on-leave: 0
  lives-deducted-on-fail: 0
  lives-deducted-on-clear: 0
  cooldown-seconds: 1800

rewards:
  solo-tiers:
    600: 1
  party-tiers:
    500: 1
  pool:
    diamond:
      type: "ITEM"
      value: "DIAMOND:1-3"
      chance: 100.0
      name: "<aqua>Diamonds"

stages:
  1:
    chance: 100.0
    actions:
      start:
        type: "REACH_LOCATION"
        target: "0,64,0"
        radius: 3.0
```

## Premium Schematic First Setup

Premium schematic mode uses the dungeon `template-world` value as the schematic file base name.

Example:

```yaml
template-world: "ForgottenCrypt_Template"
```

The Premium plugin looks for:

```text
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schem
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schematic
```

Enable schematic mode in Premium config:

```yaml
instancing:
  mode: "SCHEMATIC"
```

All schematic runs are pasted into isolated regions inside one shared void world. Paper can create that world at
runtime; on Folia, that shared world must already be loaded before SinceDungeon Premium enables.

For the complete per-dungeon workflow, see [Schematic and Folia Setup](folia-schematic.md).

## File Locations

| File or folder                                 | Purpose                                |
|------------------------------------------------|----------------------------------------|
| `plugins/SinceDungeon/config.yml`              | Main merged Core configuration.        |
| `plugins/SinceDungeon/dungeons/`               | Dungeon template YAML files.           |
| `plugins/SinceDungeon/languages/`              | Language files.                        |
| `plugins/SinceDungeon-PremiumAddon/config.yml` | Premium configuration.                 |
| Template world folder                          | Source world copied for each instance. |

## Reloading

Use:

```text
/sincedungeon reload
```

or the configured admin command and aliases. Premium also provides:

```text
/sdp reload
```

Server restart is still recommended after changing dependencies, template world folders, database backend, or
cross-server settings.
