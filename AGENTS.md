# AGENTS.md

Notes for anyone (human or agent) changing this repository. Architecture and
configuration are documented under `docs/`.

## Layout

- `Core/` — the free plugin, `SinceDungeon` (package `net.danh.sinceDungeon`).
- `Premium/` — the paid add-on, `SinceDungeon-PremiumAddon`. It compiles against
  Core and swaps in its own systems at startup (notably `SchematicInstanceProvider`,
  which runs every dungeon inside one shared, always-loaded void world and is the
  only mode that works on Folia).

## Versioning & release

- `version=` in `gradle.properties` is the Core (SinceDungeon) version and the
  single source of truth. Premium keeps its own version in `Premium/build.gradle`.
- Every user-facing change bumps `version` (SemVer) and adds a `CHANGELOG.md` entry.
  Pure-internal changes (refactors, CI tweaks) need neither.
- **A version bump that lands on `master` ships.** `.github/workflows/auto-release.yml`
  cuts a GitHub Release (tag `choicedungeons/v<version>`, asset `SinceDungeon.jar`)
  on the next push, and BeaconOps' plugin sync then flags it for deployment to the
  servers running it. Only merge a `version=` bump when the build is meant to go out.
- The tag prefix `choicedungeons/v` and the asset name `SinceDungeon.jar` are matched
  by BeaconOps' `minecraft_plugin_registry` row (seeded in
  `db/migrate-mc-custom-plugin-sync.js` in the platform repo), keyed on the name the
  jar declares (`SinceDungeon`, from `paper-plugin.yml`). Changing any of these here
  silently stops ingestion — releases publish, nothing deploys.

## Testing

- There are no automated tests, and CI only runs `./gradlew clean build`. Anything
  touching gameplay has to be checked by hand on a Paper server, and on Folia too
  when it affects Premium's `SCHEMATIC` mode.
- Scheduling must go through `SchedulerCompat`: world work on the global scheduler,
  player work on the entity scheduler, or it breaks on Folia.

## Git

- Develop on a feature branch; never push to `master` without explicit permission.
- Clear commit messages explaining the *why*.
