# Changelog

Releases follow [Semantic Versioning](https://semver.org/) and are cut by
`.github/workflows/auto-release.yml` as `choicedungeons/v<version>` whenever `version=` in
`gradle.properties` changes on `master`.

## [Unreleased]

## [1.7.2] — 2026-09-12

### Fixed

- **Dungeon instances are copied to the world container again, so they load instead of being generated.**
  `DefaultInstanceProvider` loads an instance with `new WorldCreator(instanceId)`, which Bukkit resolves
  against `Bukkit.getWorldContainer()` — the vanilla `<level>/dimensions/<namespace>/` layout is never
  consulted for it. 1.7.0 wrote the copy into `<level>/dimensions/minecraft/`, so the template bytes landed
  where nothing reads them, Bukkit found an empty folder under the container and generated raw terrain, and
  players were teleported to the dungeon's coordinates underground and in lava. 1.7.1's attempt — use that
  layout only when the dimensions folder exists — did not help: the affected server's level directory
  (`skyworld`) does have one, so every run still generated a world *and* leaked a full copy of the template
  (675 MB of orphans had accumulated). The destination is now unconditionally the world container, matching
  the behaviour that worked before 1.7.0. Regression test: `WorldUtilsTargetFolderTest`.
- **Upgrading from 1.7.0 or 1.7.1 leaves orphaned instance copies behind, and this release does not delete
  them.** `cleanUpStuckWorlds()` only scans the world container, so copies written by those versions under
  `<level>/dimensions/minecraft/SinceDungeon_*` are invisible to it. They are inert — nothing loads them —
  and safe to delete by hand once you are on 1.7.2. Expect roughly one template-sized copy (a few MB) per
  dungeon run made while 1.7.0/1.7.1 was installed. Delete only entries whose name starts with
  `SinceDungeon_`: that folder also holds real dimensions.
- `getTemplateFolder()` no longer hardcodes `world/dimensions` when searching for a template stored under
  the dimension layout. The level directory is whatever `level-name` says, so on a server named anything
  other than `world` that search silently never ran.

## [1.7.1] — 2026-09-12

### Fixed

- **Dungeon instances were generated instead of copied on servers that keep worlds beside the world
  container.** `WorldUtils.getTargetFolder()` trusted Paper's `getLevelDirectory()` API, which exists on every
  modern build, and wrote the copied template into `world/dimensions/minecraft/`. `WorldCreator` then loaded
  the instance by name from the container, found nothing, and generated fresh terrain — players were
  teleported to the dungeon's configured coordinates underground, inside blocks and in lava. The dimension
  layout is now used only when the server actually has one. Regression test:
  `WorldUtilsTargetFolderTest`.
- A held player whose team cleared the dungeon was charged `cooldown-on-leave` and `lives-deducted-on-leave`
  for a run that succeeded; the disconnect path has always skipped penalties on a cleared run.
- **An expired rejoin hold could delete a player's inventory permanently.** Entering a
  `save-and-restore-stats` dungeon empties inventory, armour and XP, and the only copy lived in the running
  game — so a player who did not return in time, or a restart, lost it. The snapshot is now parked on disk
  (`pending-restores/<uuid>.yml`) and handed back on their next login. This also closes the same leak on the
  ordinary quit path, where the restore was queued after the player had already left.
- A run holding a spot for a disconnected player is no longer ended when the last remaining player leaves by
  a non-quit path (walking out, running out of lives, or leaving the party); it pauses, as a quit would.
- The Editor GUI showed the hardcoded default for `empty-dungeon-timeout` instead of the configured global
  value, and writing from that screen saved the wrong number as a per-dungeon override.
- `weather-cycle` now resolves per world rather than at world spawn, and the docs no longer claim a
  per-dungeon override applies to it inside Premium's shared schematic world, where several runs share one
  sky.

### Added

- Added a JUnit 5 test suite for Core (27 tests), run by `./gradlew build` and therefore by CI.
  It covers world flag parsing, the `DungeonTemplate.Settings` compatibility constructor, and the
  shipped resources: config defaults matching their code defaults, en/vi/zh language parity, Editor
  setting labels/lore/prompts in every locale, and the bundled dungeon templates.

### Fixed

- Added the missing Vietnamese and Chinese translations for `admin.log.startup_slow`; those servers
  logged the English text.
- Added the missing Editor input prompts for `lives-deducted-on-leave`, `-on-fail` and `-on-clear` in
  all three languages. They fell back to a generic "enter a number" prompt while every comparable
  setting had tailored guidance.

## [1.7.0] — 2026-09-12

- Added proactive Folia validation for Core template-world/world-copy dungeons during template load, join, and editor
  save, with guidance that Premium `SCHEMATIC` shared-world mode works on Paper and Folia.
- Documented outcome-based life costs (`lives-deducted-on-leave`, `lives-deducted-on-fail`,
  `lives-deducted-on-clear`) and clarified their interaction with per-death costs and cooldown-on-leave.
- Updated Premium hologram docs for native TextDisplay leaderboard holograms and added default line spacing/view range
  config keys.

### Fixed

- Players who log back in after their dungeon has ended are now always rescued to their pre-dungeon location (or the
  main world spawn) instead of spawning in a void world, including Premium's shared schematic world and after a
  restart, with fall/void/suffocation damage blocked briefly. Ghost cleanup no longer deletes a shared provider world.

- Fixed a critical bug in Paper 1.20+ where joining a dungeon would spawn players in a randomly generated world instead of the dungeon template (caused by incorrect instance folder targeting).
- Enforced private dungeon visibility: regular members cannot join `public: false` dungeons, while admins can
  tab-complete and join them for testing.
- Routed command rewards through `SchedulerCompat` so reward command execution no longer calls the Bukkit scheduler
  directly.
- Replaced raw `printStackTrace()` calls with contextual plugin logger output.
- Closed default config resource streams after auto-update checks.
- Cleared reward session cleanup task references during shutdown to avoid stale static task handles.
- Added missing Premium hologram message keys used by `HologramManager`.
- Hardened Premium hologram updates by snapshotting config on the server thread, fetching leaderboard data
  asynchronously, and rendering holograms on the owning location scheduler.

### Added

- Added `dungeon.gameplay.empty-dungeon-timeout` (and per-dungeon `settings.empty-dungeon-timeout`, also in the Editor
  GUI). A player who disconnects mid-run keeps their spot for that many seconds; an empty run pauses and stays alive so
  a solo player can log back in and continue. Leave penalties are deferred until the timeout expires. Default `300`
  seconds; set `0` for the previous behaviour.
- Added `dungeon.world-flags` (and per-dungeon `settings.world-flags`) to switch off leaf decay, crop and tree growth,
  block spread/fade/form, fluid flow, natural mob spawning and natural weather in dungeon worlds. By default every flag
  except `fluid-flow` is off, so existing servers pick up static dungeon worlds on upgrade; set a flag to `true` to keep
  vanilla behaviour.
- Added `DungeonTemplate.Settings` components `emptyDungeonTimeout` and `worldFlags`; the previous constructor is kept.

- Added `settings.regenerate-default-templates` option in `config.yml` (default `false`). Setting this to false stops the plugin from automatically re-creating default template files (e.g. `example_dungeon.yml`) when server owners delete them.
- Added API overload `joinDungeon(Player, String, boolean)` for controlled private dungeon joins by integrations.
- Documented Premium schematic shared-world setup for Paper and Folia.
- Documented Premium `NPC_INTERACTION` action.
- Added GitHub Pages wiki under `docs/` with deployment workflow.

### Notes

- Premium `SCHEMATIC` shared-world mode works on Paper and Folia. Folia cannot create or load Bukkit worlds at runtime,
  so Folia deployments must preload the configured shared world before plugin startup.
