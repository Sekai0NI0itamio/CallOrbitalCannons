# CallOrbitalCannons

Call down Wemmbu-style **Orbital (Nuke)** and **Stab** strikes with a
special fishing rod — no redstone machinery required. Right-click the
block you are aiming at (up to 200 blocks away) and the cannon answers
from above.

Ballistics are ported from the public
[orbitalstrike](https://github.com/RelmesStudios/orbitalstrike) Fabric mod
(Apache-2.0, behavior port — no code copied) to Minecraft 1.21.1. See
`docs/REFERENCE_RESEARCH.md` for exactly what was verified, what was
approximated, and where this mod differs.

## Weapons

| Rod | Strike | Effect |
|---|---|---|
| Orbital Fishing Rod (aqua italic, glint) | Nuke Shot | 760 TNT (lone center + 10 rings at radii 6–51 with uniform 5-block gaps; charge counts front-loaded — 24, 48, 56 in the first three rings, then 52 → 128 outward — so the 2nd ring opens dense and outer rings keep breathing room, all per cubicmetre's Mk.6 proportional law) fall from 72 blocks up across a ~102-block footprint; every charge lands at a random angle around its ring with a tiny 0.2-block scatter, so rings render as dense clumps with gaps instead of even dots — matching how cubicmetre's Mk.6 cannon actually looks (see `docs/REFERENCE_RESEARCH.md` §13). Fires silently in chat — only the warning sound plays |
| Stab Shot Fishing Rod (aqua italic, glint) | Stab Shot | Full-height column at the target's x/z, drilled top-down with 1 TNT every 2 blocks and 1-tick fuses (up to 192 charges) — stops at the first blast-resistant block (≥ 1200) |

The mode is stored in item data, so renaming an ordinary fishing rod never
forges a weapon. Rods carry a single durability point: the first survival
use breaks them, and a spent rod is gone. There is no recharge cooldown —
the consumed rod is the cost.

## Crafting

Each recipe needs **one ordinary fishing rod + one full 64-TNT stack**
(short stacks never craft). Orientation decides the weapon; the patterns
are not mirrors of each other. `T` = TNT ×64, `R` = fishing rod, `.` =
empty.

Orbital (TNT on top):

```
. T .
. R .
. . .
```

Stab (TNT on bottom):

```
. . .
. R .
. T .
```

All 64 TNT and the rod are consumed; exactly one weapon rod is produced.

## Usage

1. Craft or `/orbital give` a rod.
2. Aim at a block within range and right-click.
3. The rod breaks, a warning sounds, and the strike lands after a short
   delay (10 ticks by default).

Clean misses inside loaded chunks (and full strike queues) never
consume the rod. Aims past the loaded edge fall back to the last loaded
voxel along the look ray — the nearest block position to what was aimed
at — and do consume the rod; the world bottom/top clamp the fallback Y,
so deep worlds (down to -64 and below) work.
Both rods fire silently in chat — only the warning sound plays; only
rejections print, and only on the stab rod.
The nuke reproduces cubicmetre's Mk.6 cannon render: front-loaded rings
(24, 48, 56 in the first three) whose charges land at random angles
around each radius — dense clumps with gaps, inner rings denser per unit
area — with a tiny 0.2-block scatter per charge clamped to its ring band.

## Commands (operator level 2)

```
 /orbital give nuke [player]
 /orbital give stab [player]
 /orbital strike nuke <x> <y> <z>
 /orbital strike stab <x> <y> <z>
 /orbital reload
```

## Configuration (`config/callorbitalcannons.json`)

| Key | Default | Meaning |
|---|---|---|
| targetingRange | 200.0 | Max rod targeting distance (blocks) |
| strikeDelayTicks | 10 | Launch delay after activation |
| playerCooldownTicks | 0 | Per-player recharge, in ticks (0 = off; rods are single-use) |
| maxActiveStrikes | 4 | Pending-strike queue cap |
| nukePayloadLimit | 800 | TNT cap per nuke (760 = full pattern) |
| nukeRingCount | 10 | Rings used (1–10, innermost first) |
| nukeSpawnHeight | 72.0 | Drop height above target |
| nukeFuseTicks | 79 | Nuke TNT fuse |
| nukeMisalignRange | 0.2 | Per-charge scatter-disc radius, flat for every ring (cubicmetre's Mk.6 prime-momentum kick is tiny, ~0.2 blocks), clamped to the ring band |
| nukeAngularJitter | 1.0 | Charge angle wander, fraction of one spacing (1.0 = random angles → Mk.6 clumpy rings; 0.0 = even rings) |
| stabMaxSteps | 192 | Max stab column depth (steps; 192 covers a default world at step 2) |
| stabTntPerStep | 1 | Charges per step |
| stabStepSize | 2 | Blocks between groups |
| stabVerticalOffset | 0.75 | Charge spacing inside a group |
| stabFuseTicks | 1 | Stab TNT fuse |
| blastResistanceStop | 1200.0 | Resistance that stops the drill |
| terrainDestruction | true | Reserved: not enforced in 1.x |
| reduceBlockDrops | false | Reserved: not enforced in 1.x |
| consumeInCreative | false | Break rods in creative mode |

Malformed files are quarantined as `callorbitalcannons.json.invalid` and
safe defaults are used; out-of-range values are reset with a log warning.
Configs from before the current generation (1.2.9 = generation 6; 1.2.8
was generation 5, 1.2.7 was generation 4, 1.2.6 was generation 3,
1.2.1–1.2.4 were generation 2,
no stamp) are backed up as `callorbitalcannons.json.v1.bak` and
regenerated — stale values from older releases (e.g. a 1.2.8
Mk.6-count nuke, or 1.0.0 stab depth) can never silently persist. Keep a single mod JAR in `mods/`; overlapping
versions fight over the mod id.

## Installation

1. Install Fabric Loader 0.16.14 for Minecraft 1.21.1.
2. Put Fabric API (0.102.0+1.21.1) and the CallOrbitalCannons JAR in
   `mods/`.
3. Start the server. Java 21 is required.

## Server requirements and performance

A full nuke spawns 760 live TNT entities and a full stab column up to
192 — expect a serious but bounded tick spike. See
`docs/PERFORMANCE.md` for budgets, safeguards, and tuning advice before
handing these rods to your players. On shared hardware, consider lowering
`nukePayloadLimit`/`maxActiveStrikes`.

## What this is not

This mod does not rebuild the original redstone cannon and does not claim
tick-for-tick fidelity to it. It reproduces the two documented strike
patterns (concentric bombardment, vertical drill) through server-spawned
vanilla TNT: same shapes and timing constants, vanilla explosions. The
reference mod's extra shots (stasis, wolves, wither dome), its parallel
explosion mixin, and its drop reduction are intentionally not included.
