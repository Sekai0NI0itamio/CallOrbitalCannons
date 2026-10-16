# Criteria: CallOrbitalCannons

- [ ] `settings.gradle`, `build.gradle`, `gradle.properties` follow sibling (DuraPing) conventions: MC 1.21.1, yarn 1.21.1+build.3, loader 0.16.14, fabric-api 0.102.0+1.21.1, loom 1.7.4, Java 21
- [ ] `fabric.mod.json` present with id `callorbitalcannons`
- [ ] Orbital recipe matches ONLY: TNTx64 top-center + rod middle-center, rest empty; produces NUKE rod; consumes 64 TNT + 1 rod
- [ ] Stab recipe matches ONLY: rod middle-center + TNTx64 bottom-center, rest empty; produces STAB rod
- [ ] Mirrored/swapped patterns do NOT cross-match; incomplete TNT (<64) does NOT match; renaming ordinary rod does NOT make a weapon
- [ ] Nuke geometry: radii {6..51 step 5}, ring counts verbatim {15,27,38,49,62,73,83,95,107,119}, height 72, fuse 79, drag-compensated velocity, disc+clamp misalignment 0.5
- [ ] Stab geometry: start target+5, step 4, 5 TNT/step, offsets 0.75 overlap, fuse 1, no gravity, stop at blast resistance >= 1200
- [ ] Rod right-click: weapon validation, raycast <= configured range (default 200), server-side schedule, feedback, exactly-once consume, miss handled safely
- [ ] Scheduler: delay, per-player cooldown, duplicate prevention, max active strikes, payload cap, coordinate/dimension validation, cleanup on stop
- [ ] Commands `/orbital give/strike/reload` with permission level 2 for strike/give; config file with validated safe defaults
- [ ] LICENSE (MIT) + NOTICE (upstream attribution) + docs/LICENSE_COMPLIANCE.md + docs/REFERENCE_RESEARCH.md + docs/PERFORMANCE.md + README.md + CHANGELOG.md
- [ ] Tests cover: both recipes, incomplete stack rejection, consumption, weapon-mode data, nuke total/radii/distribution, stab spacing/groups, range/invalid targets, cooldown/duplicates, config validation, cleanup
- [ ] `.github/workflows/build.yml` (push/PR/manual, Java 21, wrapper build, JAR exists+non-empty, artifacts) passes on GitHub Actions
- [ ] No local Gradle/test/Minecraft execution; only source edits, git, gh locally
