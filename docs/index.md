---
layout: home
title: SinceDungeon Wiki
---

SinceDungeon is a Paper 26.2 dungeon plugin for instanced, configurable dungeon runs. It provides template-world
instancing, staged objectives, built-in party support, lives and cooldown systems, reward chests, leaderboards,
PlaceholderAPI support, an in-game editor, and an extensible Java API.

## Plugin Packages

- **SinceDungeon Core**: dungeon engine, commands, editor, actions, rewards, top boards, lives, cooldowns, database,
  Redis/cross-server support, and developer API.
- **SinceDungeon Premium Addon**: advanced dungeon actions, roulette rewards, hologram drops, hologram leaderboards,
  Mythic+ affixes, Discord webhooks, native TextDisplay holograms, and extra MythicMobs item integration.

## Read In This Order

1. [Getting Started](getting-started.md): install the plugin and create a first dungeon.
2. [Configuration](configuration.md): configure global Core settings.
3. [Dungeon Files](dungeon-files.md): understand the YAML file structure.
4. [Actions](actions.md): build stages and objectives.
5. [Rewards](rewards.md): configure completion loot.
6. [Editor](editor.md): use the in-game editor for faster setup.
7. [Example Dungeon](example-dungeon.md): copy a compact complete YAML example.
8. [Schematic Setup](folia-schematic.md): use Premium schematic instancing, including Folia.
9. [Troubleshooting](troubleshooting.md): debug common setup mistakes.

The left navigation contains every available documentation page.

## Requirements

- Paper API: **26.2** (paper-api 26.2.build.62-beta)
- Java: **25**
- Optional integrations:
    - PlaceholderAPI
    - MythicMobs
    - MythicLib
    - MMOItems
    - WorldEdit or FastAsyncWorldEdit, Premium schematic instancing
    - Redis and proxy messaging for experimental cross-server matchmaking

## Core Concepts

| Concept        | What it does                                                                                 |
|----------------|----------------------------------------------------------------------------------------------|
| Template world | A source world folder copied into a temporary dungeon instance.                              |
| Dungeon file   | A YAML file in `plugins/SinceDungeon/dungeons/<id>.yml`.                                     |
| Stage          | A numbered step in the dungeon. Each stage can contain one or more actions.                  |
| Action         | An objective such as reaching a location, clearing mobs, opening a door, or fighting a boss. |
| Rewards        | Completion grants reward chest sessions based on clear time tiers.                           |
| Party          | Built-in party system, with an API override for custom party plugins.                        |
| Lives          | Player life pool used for entry requirements, death penalties, and revive items.             |
| Cooldowns      | Per-dungeon cooldowns after completion or early exit, depending on config.                   |
| Top boards     | Database-backed leaderboard categories for time, party time, kills, and clears.              |

Premium `SCHEMATIC` shared-world mode works on Paper and Folia. On Folia, Core template-world copy mode is blocked
before a run starts because Folia cannot create Bukkit worlds at runtime; use schematic mode with a preloaded shared
world.

## Common Tasks

| Task                                                 | Page                                  |
|------------------------------------------------------|---------------------------------------|
| Install Core and Premium                             | [Getting Started](getting-started.md) |
| Set command aliases, database, worlds, and lives     | [Configuration](configuration.md)     |
| Create a dungeon YAML file                           | [Dungeon Files](dungeon-files.md)     |
| Add combat, doors, chests, bosses, and traps         | [Actions](actions.md)                 |
| Copy a complete dungeon YAML example                 | [Example Dungeon](example-dungeon.md) |
| Create a schematic dungeon correctly                 | [Schematic Setup](folia-schematic.md) |
| Configure roulette, affixes, webhooks, and holograms | [Premium Addon](premium.md)           |
| Use API events or custom providers                   | [Developer API](developer-api.md)     |
