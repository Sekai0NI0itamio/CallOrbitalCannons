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
| Orbital Fishing Rod (aqua italic, glint) | Nuke Shot | 669 TNT (center + 10 rings, radii 6–51) fall from 72 blocks up and detonate across a ~100-block footprint |
| Stab Shot Fishing Rod (aqua italic, glint) | Stab Shot | 5-TNT groups step down every 4 blocks from 5 above the target, 1-tick fuses, drilling until blast resistance ≥ 1200 |

The mode is stored in item data, so renaming an ordinary fishing rod never
forges a weapon. Rods are single-use: one activation consumes the rod.

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

Misses (no targeted block), cooldowns, and full strike queues give a chat
message and never consume the rod.

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
| playerCooldownTicks | 100 | Per-player cooldown between strikes |
| maxActiveStrikes | 4 | Pending-strike queue cap |
| nukePayloadLimit | 700 | TNT cap per nuke (669 = full pattern) |
| nukeRingCount | 10 | Rings used (1–10, innermost first) |
| nukeSpawnHeight | 72.0 | Drop height above target |
| nukeFuseTicks | 79 | Nuke TNT fuse |
| nukeMisalignRange | 0.5 | Per-charge scatter disc radius |
| stabMaxSteps | 80 | Max stab column depth (steps) |
| stabTntPerStep | 5 | Charges per step |
| stabStepSize | 4 | Blocks between groups |
| stabVerticalOffset | 0.75 | Charge spacing inside a group |
| stabFuseTicks | 1 | Stab TNT fuse |
| blastResistanceStop | 1200.0 | Resistance that stops the drill |
| terrainDestruction | true | Reserved: not enforced in 1.0.0 |
| reduceBlockDrops | false | Reserved: not enforced in 1.0.0 |
| consumeInCreative | false | Consume rods in creative mode |

Malformed files are quarantined as `callorbitalcannons.json.invalid` and
safe defaults are used; out-of-range values are reset with a log warning.

## Installation

1. Install Fabric Loader 0.16.14 for Minecraft 1.21.1.
2. Put Fabric API (0.102.0+1.21.1) and the CallOrbitalCannons JAR in
   `mods/`.
3. Start the server. Java 21 is required.

## Server requirements and performance

A full nuke spawns 669 live TNT entities and detonates them within
seconds — expect a serious but bounded tick spike. See
`docs/PERFORMANCE.md` for budgets, safeguards, and tuning advice before
handing these rods to your players. On shared hardware, consider lowering
`nukePayloadLimit`/`maxActiveStrikes` or raising `playerCooldownTicks`.

## What this is not

This mod does not rebuild the original redstone cannon and does not claim
tick-for-tick fidelity to it. It reproduces the two documented strike
patterns (concentric bombardment, vertical drill) through server-spawned
vanilla TNT: same shapes and timing constants, vanilla explosions. The
reference mod's extra shots (stasis, wolves, wither dome), its parallel
explosion mixin, and its drop reduction are intentionally not included.
