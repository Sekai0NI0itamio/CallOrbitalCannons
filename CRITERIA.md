# Criteria: CallOrbitalCannons

## 1.2.1 (silent stab + config self-healing)
- [ ] Stab success fully chat-silent; config version stamp migrates pre-1.2.1 files
- [ ] CI green on 1.2.1, JAR refreshed locally

## 1.2.0 (footage-matched rings + loaded-edge targeting)
- [x] Nuke 539 (rings {30,102,84,68,60,52,44,38,32,28}, radii 6..69 step 7, dense second ring, decreasing outward), cap 600; stab unchanged
- [x] Raycast miss past loaded edge approximates last loaded voxel (Y clamped); clean miss keeps rod; stab chat notes approximation
- [x] CI green on 1.2.0 (run 38982124804, 44/44 tests), JAR refreshed locally

## 1.1.2 (payload trim)
- [x] Nuke 602 (rings {14..106}), cap 650; stab 144 (triplets every 8, cap 64)
- [x] CI green on 1.1.2, JAR refreshed locally

## 1.1.1 (all verified green on CI run 38907271451)
- [x] Nuke: 2000 payload, ring counts {45..352}, jitter + scatter defaults, ragged carpet
- [x] Stab: full-height column at x/z swept top-down, blast-resistance stop kept
- [x] Rods spawn at 1 durability and break on first survival use
- [x] Cooldown default 0 (mechanism kept, disabled); nuke rod chat-silent
- [x] CI green on 1.1.0, JAR refreshed locally

## 1.0.0 (all verified green on CI run 38901389959)

- [x] `settings.gradle`, `build.gradle`, `gradle.properties` follow sibling (DuraPing) conventions: MC 1.21.1, yarn 1.21.1+build.3, loader 0.16.14, fabric-api 0.102.0+1.21.1, loom 1.7.4, Java 21
- [x] `fabric.mod.json` present with id `callorbitalcannons`
- [x] Orbital recipe matches ONLY: TNTx64 top-center + rod middle-center, rest empty; produces NUKE rod; consumes 64 TNT + 1 rod
- [x] Stab recipe matches ONLY: rod middle-center + TNTx64 bottom-center, rest empty; produces STAB rod
- [x] Mirrored/swapped patterns do NOT cross-match; incomplete TNT (<64) does NOT match; renaming ordinary rod does NOT make a weapon
- [x] Nuke geometry: radii {6..51 step 5}, ring counts verbatim {15,27,38,49,62,73,83,95,107,119}, height 72, fuse 79, drag-compensated velocity, disc+clamp misalignment 0.5
- [x] Stab geometry: start target+5, step 4, 5 TNT/step, offsets 0.75 overlap, fuse 1, no gravity, stop at blast resistance >= 1200
- [x] Rod right-click: weapon validation, raycast <= configured range (default 200), server-side schedule, feedback, exactly-once consume, miss handled safely
- [x] Scheduler: delay, per-player cooldown, duplicate prevention, max active strikes, payload cap, coordinate/dimension validation, cleanup on stop
- [x] Commands `/orbital give/strike/reload` with permission level 2 for strike/give; config file with validated safe defaults
- [x] LICENSE (MIT) + NOTICE (upstream attribution) + docs/LICENSE_COMPLIANCE.md + docs/REFERENCE_RESEARCH.md + docs/PERFORMANCE.md + README.md + CHANGELOG.md
- [x] Tests cover: both recipes, incomplete stack rejection, consumption, weapon-mode data, nuke total/radii/distribution, stab spacing/groups, range/invalid targets, cooldown/duplicates, config validation, cleanup
- [x] `.github/workflows/build.yml` (push/PR/manual, Java 21, wrapper build, JAR exists+non-empty, artifacts) passes on GitHub Actions
- [x] No local Gradle/test/Minecraft execution; only source edits, git, gh locally
