# Reference research

Selected upstream revision: `067fbcddf6351608f4646c9f6ffaa9b6afae14c4`
(`RelmesStudios/orbitalstrike`, commit message "refactor: migrate build
directory and implement chunk-forced stasis unloading in entity mixins",
2026-07-15). Inspected 2026-10-16 via the GitHub API and raw file fetches.

## 1. References inspected

### 1a. Cubicmetre / Wemmbu video (concept only)

`https://www.youtube.com/watch?v=q78LRgHt_zU` — cited by the task as the
original Orbital Strike Cannon demonstration from the Unstable SMP context.
No frame-level mechanics (dispenser counts, timings, accelerator design)
could be extracted or verified from this session, so the video is treated
as a **concept reference only**. Nothing in this mod claims to reproduce
the original redstone contraption tick-for-tick.

### 1b. Wemmbu's Orbital Cannons (WOC) on CurseForge (behavioral only)

`https://www.curseforge.com/minecraft/mc-mods/wemmbus-orbital-cannons-woc` —
the page could not be fetched from this environment (HTTP 403 from the
fetch backend), and its license is listed as **All Rights Reserved**.
It is therefore treated as a **behavioral reference only**: no code,
assets, or text were taken from it.

### 1c. RelmesStudios/orbitalstrike (technical reference, Apache-2.0)

`https://github.com/RelmesStudios/orbitalstrike` — public Fabric mod
recreating the Orbital Strike Cannon. Its `LICENSE` file is the Apache
License, Version 2.0 (copyright 2026 Relmes Studios), with a `NOTICE`
file carrying the same attribution. Source files read in full:

- `src/main/java/pier4y/orbitalstrike/strike/StrikeManager.java`
  (nuke / stab / stasis / wolf / wither spawners, pending-strike queue)
- `src/main/java/pier4y/orbitalstrike/event/FishingRodUseHandler.java`
  (rod validation via `CUSTOM_DATA`, raycast targeting, 10-tick delay)
- `src/main/java/pier4y/orbitalstrike/config/OrbitalStrikeConfig.java`
  (all tuning values)
- `src/main/java/pier4y/orbitalstrike/command/OscCommand.java`
  (rod factory, `/osc` tree)
- `src/main/java/pier4y/orbitalstrike/Orbitalstrike.java` (entrypoint)
- `src/main/resources/fabric.mod.json`, `orbitalstrike.mixins.json`

## 2. Exact behavior found in the reference source

Nuke (`spawnNuke`):

- 1 stationary center TNT + 10 rings, radii
  `{6, 11, 16, 21, 26, 31, 36, 41, 46, 51}`, per-ring counts
  `{15, 27, 38, 49, 62, 73, 83, 95, 107, 119}`.
- All charges spawn 72 blocks above the target (`nukeSpawnHeight`),
  fuse 79 ticks (`nukeFuseTicks`), zero vertical velocity.
- Horizontal velocity per charge: `v = targetOffset / scale` with
  `scale = (1 - 0.98^fuse) / 0.02` (closed-form TNT drag integration).
- Misalignment: uniform point in a disc of radius `nukeMisalignRange`
  (`sqrt(rand) * range` for uniformity), then radial clamp into the ring
  band (midpoint to neighbours; first ring min 0, last ring max
  `radius + range`).
- Even angular spacing `(2 * PI * j) / count` per ring.

Stab (`spawnStab`):

- Column from `targetY + 5` downward in steps of 4 (`stabStepSize`),
  5 TNT per step (`stabTntPerStep`) at `y + 0.5 - t * 0.75`
  (`stabTntVerticalOffset`), fuse 1 (`stabFuseTicks`), `setNoGravity(true)`.
- Stops after spawning on the first block whose
  `getExplosionResistance() >= 1200.0` (`blastResistanceStopThreshold`),
  scanning down to world minimum.

Rods and activation:

- Plain fishing rod + `CUSTOM_DATA` NBT `{OrbitalStrikeRod: true,
  StrikeType: "nuke"|"stab"|..., [HasTargetPos, TargetX/Y/Z]}`.
- `UseItemCallback`: raycast `player.pick(120.0, ...)`; rod breaks
  immediately (`stack.shrink(1)` + break sound); strike fires exactly
  10 ticks (0.5 s) later via the pending queue.
- Commands: `/osc give nuke|stab [player]`, `/osc strike nuke|stab x y z`
  (permission level 2). Rod display name is plain white italic
  `"<type> shot"` with **no** enchantment glint.

Extras in the reference that this mod deliberately omits: stasis pearls,
wolf packs, wither-skull domes, the parallel ForkJoinPool explosion
raycast mixins, drop-rate reduction, and chunk-forcing.

## 3. Documented discrepancies (README vs source)

| Claim | README text | Source code | This mod |
|---|---|---|---|
| Nuke total | 661 (660 ring + 1) | ring counts sum to **668**, +1 = **669** | 669 (code wins), documented here |
| Ring range | 15 to 119 | `{15, …, 119}` inclusive | verbatim |
| Misalignment | 1.75 blocks (half of 3.5) | `nukeMisalignRange = 0.5` | 0.5 (code wins), documented here |

The per-ring counts were taken from source, never guessed; the totals
above are arithmetic on those verbatim values
(15+27+38+49+62+73+83+95+107+119 = 668).

## 4. Original vs reference vs this mod

- **Original redstone cannon** (Cubicmetre/Wemmbu): a manually built,
  redstone-driven TNT accelerator. Exact dispenser layout, timings, and
  duplication mechanics are unverified (see §1a).
- **Reference mod**: rod/command-triggered spawners on modern Minecraft
  (26.x, official mappings) with mixins, extra joke shots, fixed 120-block
  targeting, no cooldown, no payload cap.
- **This mod**: 1.21.1 + Fabric + Yarn port of the nuke/stab ballistics
  only, with no mixins (vanilla TNT entities and explosions on the server
  thread), orientation-distinct 64-TNT crafting recipes, a scheduler with
  cooldown and payload caps, 200-block default targeting, and
  `/orbital` admin commands.

## 6. 1.1.0 deviations (in-game feedback + Wemmbus scale)

- **Nuke scaled to 2000 TNT.** Community documentation of Wemmbus-scale
  strikes (vUnstable plugin: "2000 TNT entities, 10 concentric rings,
  100x100 destruction area"; Relmes Modrinth page: 661 over 10 rings)
  shows the real strike is a far bigger, ragged carpet than the 669-charge
  reference port. Ring counts are now
  `{45, 81, 114, 147, 186, 219, 249, 285, 321, 352}` (1999 + center =
  2000) with angular jitter (default 0.5 of one spacing) and a wider
  scatter disc (default 2.0). Exact Wemmbus dispenser mechanics remain
  unverified; this is a tuned approximation, not a measured recreation.
  1.1.2 thinned the carpet a further 70% on player feedback (rings
  `{14, …, 106}`, 601 + center = 602) while keeping the same proportions,
  jitter, and scatter; stab triplets every 8 blocks likewise cut its
  column 480 → 144.
- **Stab now spans the full world height.** Per player request the drill
  takes only the target's x/z and sweeps top-down from the world ceiling
  instead of clustering around the clicked Y — but the blast-resistance
  stop (≥ 1200) is kept exactly as the reference rule, so bedrock and
  obsidian still block it. The sweep runs top-down (not bottom-up) because
  a bottom-up sweep would halt immediately on the bedrock floor.
- Rods are single-durability break-on-use, the recharge defaults to off,
  and the Orbital rod is chat-silent: usability requests, not reference
  behavior.

## 7. 1.2.0 deviations (footage-matched rings + loaded-edge targeting)

- **Nuke redistributed dense-inner / sparse-outer.** Frame review of
  Wemmbbu cannon footage (top-down falling-TNT shot) shows a lone center
  charge, a dense second ring, then strictly thinning outer rings with
  wide visible gaps between rings — the 1.1.2 inner-to-outer growth ran
  backwards against this. Ring counts are now
  `{30, 102, 84, 68, 60, 52, 44, 38, 32, 28}` (538 + center = 539) on
  uniform 7-block radii `{6, 13, …, 69}` (was 5-block `{6, 11, …, 51}`),
  so each band stays visually separated under the kept 2.0 scatter and
  0.5 angular jitter. Per-ring charge counts remain an eyeballed
  approximation of the footage, not a measured recreation; the payload
  cap default tightens 650 → 600 (still above the 539 baseline).
- **Rod targeting falls back to the loaded edge.** Vanilla raycast misses
  past loaded chunks, so aims beyond the loaded edge now approximate the
  last loaded voxel along the look ray (Y clamped to the live world
  bottom/top, covering deep worlds at -64 and below) instead of fizzling.
  A clean miss inside loaded territory still keeps the rod. This is new
  behavior with no reference counterpart.
- **Stab stop-rule note (no sweep change).** The drill still halts after
  spawning on the first block with resistance ≥ 1200, exactly the
  reference rule — so a resistant layer at the aim (obsidian test
  platform, Nether ceiling, ancient debris) ends the column there and
  nothing below is hit.
- **1.2.1 stab diagnosis: staleness, not a sweep bug.** In-game report
  (superflat stone tower, aimed mid-tower, destruction only down to the
  aim) matches the 1.0.0 sweep exactly (start at target + 5, downward) —
  no 1.1.x sweep behaves that way. Two live staleness paths: a superseded
  JAR still sitting in `mods/`, or a 1.0.0-era config whose in-range old
  values (e.g. `stabMaxSteps` 80) survived validation. 1.2.1 closes the
  second path: configs carry a `configVersion` stamp and pre-stamp files
  are backed up + regenerated on load. Both rods are now chat-silent on
  success (warning sound only).

## 8. Unverifiable behavior (explicit approximations)

- Exact fidelity to the original redstone contraption (unverifiable).
- WOC behavior beyond its public description (page unfetchable).
- Video-derived timings or accelerator mechanics (none extracted).
- Client render performance and server TPS under a full strike (no
  runtime measurement in CI; see `docs/PERFORMANCE.md`).
