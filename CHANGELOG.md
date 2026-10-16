# Changelog

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
