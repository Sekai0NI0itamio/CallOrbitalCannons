# Changelog

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
