# Changelog

Releases follow [Semantic Versioning](https://semver.org/) and are cut by
`.github/workflows/auto-release.yml` as `choicedungeons/v<version>` whenever `version=` in
`gradle.properties` changes on `master`.

## [Unreleased]

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
