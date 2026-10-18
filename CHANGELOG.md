# Changelog

## 1.2.6 — cubicmetre-proportional rings: 636 TNT (was 1101)

- Nuke ring counts now follow cubicmetre's Mk.6 law — "the amount of TNT
  we add to the ring is proportional to its radius" — replacing the
  uniform 100-charge rings of 1.2.5. New counts {15, 24, 32, 41, 49, 58,
  66, 75, 83, 92, 100} (2.5 charges per block of radius, rounded) hold a
  constant ~2.5-block arc spacing on all eleven rings; radii and gaps are
  unchanged (6.0..40.0, step 3.4). Total payload 1101 → 636 (1 center +
  635), −42% — in-game feedback called 1101 "tooo much tnt". Inner rings
  now read as clean dotted circles instead of solid walls.
- Research notes in docs/REFERENCE_RESEARCH.md §11: Mk.6 diameter scale
  (12 blocks per size point; >16 "enormous"), the 171-TNT smallest test
  shot, payloads held "a constant relative distance of about 140 blocks",
  and cubicmetre's statement that Wemmbu's on-screen strikes were a
  staged Skript simulation — so the uniform 100-charge read was not
  literal.
- Defaults updated: `nukePayloadLimit` 1200 → 700 (above the 636
  baseline); config generation stamp 3 → 4, so stored 1.2.x configs are
  backed up + regenerated on load.

## 1.2.5 — 11-ring formation: uniform 100-charge rings, 1101 TNT

- Nuke reshaped to the manually counted reference formation: 1 center
  charge + 11 rings of 100 charges each = 1101 TNT (was 225), on radii
  {6.0, 9.4, …, 40.0} (uniform 3.4-block gaps; was 7-block, 6–48).
- Count derived mathematically from the observed outer-ring gaps:
  circumference 2π×40 ≈ 251 blocks over ~2.5-block charge spacing
  (1–2-block visible gaps) → 100 charges on the outermost ring, reused
  for every ring. Uniform count → inner rings pack solid, outer fringe
  reads pointillist.
- Defaults updated: `nukeRingCount` 7 → 11, `nukePayloadLimit` 250 →
  1200 (still above the 1101 baseline); config generation stamp 2 → 3,
  so stored 1.2.x configs are backed up + regenerated on load.

## 1.2.4 — match Wemmbbu formation: 7 rings, 225 TNT, tighter

- Nuke reshaped to the Wemmbbu reference formation: 1 center charge +
  7 rings of {8, 16, 24, 32, 40, 48, 56} = 225 TNT (was 539) on radii
  {6, 13, …, 48} (was 6–69), uniform 7-block gaps so bands stay separated
  on a ~96-block footprint (was ~140).
- Nuke defaults tightened: `nukePayloadLimit` 600 → 250 (still above the
  225 baseline), `nukeRingCount` 10 → 7, `nukeMisalignRange` 1.5 → 1.0,
  `nukeAngularJitter` 0.3 → 0.2; per-ring messiness ramp capped at
  0.1–0.2 (~80-95% circular even on the outer fringe) so rings stay
  clearly circular rather than an unorganized carpet.

## 1.2.3 — stab every 2 blocks / 1 TNT; nuke rings stay circular

- Stab drill thinned to 1 TNT charge every 2 blocks down the full column
  (up to 192 charges; `stabMaxSteps` default 64 → 192 so a default world
  still covers full height at step 2). Blast-resistance stop (≥ 1200)
  unchanged.
- Nuke rings now stay ~70-90% circular everywhere including the outer
  fringe: per-ring jitter/scatter ramp capped at 0.1–0.3 messiness, so
  even the last rings are just lightly scattered rather than ragged.

## 1.2.2 — circular nuke rings with a ragged outer fringe

- Nuke rings now stay very circular inward and only get ragged toward the
  outer fringe: angular jitter (0.3) and scatter (1.5) are applied fully
  to the outermost ring and scaled down per-ring toward the center, so the
  inner bands hold clean circles instead of an unorganized carpet.
  Jitter/scatter defaults changed (0.5/2.0 → 0.3/1.5) because they are now
  outer-ring maxima.

## 1.2.1 — silent stab + config self-healing

- Stab success chat message removed: both rods now fire silently (warning
  sound only); rejections still report on the stab rod.
- Configs carry a `configVersion` stamp (generation 2): pre-1.2.1 files
  are backed up as `callorbitalcannons.json.v1.bak` and regenerated, so
  stale 1.0.0-era values can never silently persist. Fixes the
  in-game "stab stops at the aimed Y" report, which matches the 1.0.0
  aim-downward sweep / stale depth exactly — no 1.1.x sweep behaves so.

## 1.2.0 — footage-matched rings + loaded-edge targeting

- Nuke redistributed 602 → 539 TNT (rings
  `{30, 102, 84, 68, 60, 52, 44, 38, 32, 28}` — lone center, dense
  second ring, strictly thinning outward) on wider 7-block radii
  `{6, 13, …, 69}` (was 5-block `{6, 11, …, 51}`); jitter and scatter
  kept, footprint now ~140 blocks. Payload cap default 650 → 600.
- Rod aims past the loaded edge now strike the last loaded voxel along
  the look ray (Y clamped to world bounds) instead of fizzling; clean
  misses inside loaded chunks still keep the rod. Stab chat notes the
  approximation; the Orbital rod stays silent.
- Stab drill unchanged (full column, ≥ 1200 stop); docs now spell out
  that a resistant layer at the aim ends the column there.

## 1.1.2 — 70% payload trim

- Nuke thinned 2000 → 602 TNT (rings `{14, …, 106}`, same proportions,
  jitter and scatter kept); payload cap default 650.
- Stab thinned 480 → 144 TNT (3-charge groups every 8 blocks, full
  world height, blast-resistance stop kept); depth cap default 64 steps.

- Stab keeps the blast-resistance stop (≥ 1200): the drill takes only the
  target's x/z, sweeps the full world height top-down, and halts at the
  first blast-resistant block — bedrock and obsidian still block it.

## 1.1.0 — in-game feedback tuning

- Nuke is now a Wemmbus-scale ragged carpet: 2000 TNT (center + 10 rings
  of 45–352), per-charge angular jitter plus a wider scatter disc, so the
  footprint reads as a torn bombardment zone instead of clean circles.
- Stab sweeps the full column at the target's x/z from the world top
  down (no longer clustered around the clicked Y); the blast-resistance
  stop is kept, so every non-resistant block in the column is hit.
- Weapon rods spawn with a single durability point and truly break on the
  first survival use (break sound included).
- Per-player recharge is off by default (`playerCooldownTicks = 0`); the
  consumed rod is the cost.
- The Orbital rod is fully silent in chat on every outcome (warning sound
  kept); the Stab rod still reports its target.
- Fixed a recipe-sync crash on login (`PacketCodec.unit` rejects
  datapack-loaded instances on encode; serializers now use an explicit
  stateless codec).

## 1.0.0 — initial release

- Orbital (Nuke) Shot: 669 TNT, center charge + 10 rings (radii 6–51),
  72-block drop, 79-tick fuse, drag-compensated velocity, clamped scatter.
- Stab Shot: 5-TNT groups every 4 blocks from 5 above target, 1-tick fuse,
  no gravity, stops at blast resistance ≥ 1200.
- Two glinting weapon rods forged by orientation-distinct 64-TNT recipes;
  mode stored in item data, single-use on activation.
- Server-side scheduler: launch delay, per-player cooldown, duplicate
  suppression, payload and queue caps, unload/shutdown cleanup.
- `/orbital give|strike|reload` admin commands (level 2) and validated
  JSON config with safe defaults.
- Documented reference research, performance notes, and license audit.
