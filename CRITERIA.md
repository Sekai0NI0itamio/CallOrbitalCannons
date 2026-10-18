# Criteria: CallOrbitalCannons

## 1.2.8 (Mk.6 payload formation: 715 TNT)

- [x] Nuke 715 TNT (1 center + 10 rings, radii {6,11,…,51} step 5, counts {15,28,40,52,65,78,90,103,115,128} proportional to radius), random-angle clumpy rings (jitter default 1.0), scatter disc 0.2, ringCount 10 (clamp 1–10); config generation 5 → 6; payload limit 800
- [x] CI green on 1.2.8 (run 39091427990, 51/51 tests); JAR 1.2.8 + sources downloaded to project folder

## 1.2.7 (staged formation restored: 669 TNT)

- [x] Nuke 669 TNT (1 stationary center + 10 rings, radii {6,11,…,51} step 5, counts {15,27,38,49,62,73,83,95,107,119}), flat 0.5 scatter, jitter default 0.0, ringCount 10 (clamp 1–10); config generation 4 → 5
- [x] CI green on 1.2.7 (runs 39087879138 + 39088031703, 51/51 tests); JAR 1.2.7 + sources downloaded to project folder

## 1.2.6 (cubicmetre-proportional rings: 636 TNT)

- [x] Nuke 636 TNT (center + rings {15,24,32,41,49,58,66,75,83,92,100} ∝ radius at ~2.5-block arc spacing, radii 6.0..40.0 step 3.4), cap 700, ringCount 11; config generation 3 → 4
- [x] CI green on 1.2.6 (run 39086693381); JAR 1.2.6 + sources downloaded to project folder

## 1.2.5 (11-ring uniform formation)

- [x] Nuke 1101 TNT (center + 11 rings × 100, radii 6.0..40.0 step 3.4), cap 1200, ringCount 11; config generation 2 → 3
- [x] CI green on 1.2.5 (run 39038310309); JAR 1.2.5 + sources downloaded to project folder

## 1.2.4 (match Wemmbbu formation: 7 rings, 225 TNT, tighter)

- [x] Nuke 225 TNT (center + rings {8,16,24,32,40,48,56}, radii 6..48 step 7, charges +8 per ring outward), cap 250
- [x] Nuke rings ~80-95% circular everywhere incl. outer fringe; per-ring messiness cap 0.2; defaults jitter 0.2 / scatter 1.0
- [x] CI green on 1.2.4 (run 39034413098); JAR 1.2.4 + sources downloaded to project folder

## 1.2.3 (stab every 2 blocks / 1 TNT; nuke rings stay circular)
- [x] Stab 1 TNT every 2 blocks down full column (maxSteps 192), blast stop unchanged
- [x] Nuke rings ~70-90% circular everywhere incl. outer fringe; per-ring messiness cap 0.3
- [x] CI green on 1.2.3 (run 39027501961), JAR artifact on run (Downloads copy blocked by sandbox write rule)

## 1.2.2 (circular nuke rings with a ragged outer fringe)
- [x] Nuke rings very circular inward, ragged only toward outer fringe; per-ring jitter/scatter scaling; defaults 0.3/1.5
- [x] CI green on 1.2.2 (run 39023143299); JAR artifact on the run (Downloads copy blocked by sandbox write rule)

## 1.2.1 (silent stab + config self-healing)
- [x] Stab success fully chat-silent; config version stamp migrates pre-1.2.1 files
- [x] CI green on 1.2.1 (run 38983986976), JAR refreshed locally

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
