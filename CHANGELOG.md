# Changelog

## 1.1.1 — stab drill correction

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
