# Performance

## Expected entity counts

- Nuke (default config): 1 center + 668 ring charges = **669 TNT entities**
  spawned in one server tick, fuse 79 ticks (~4 s), all exploding within a
  short window across a ~100-block-diameter footprint.
- Stab (default config): 5 charges per 4-block step from `targetY + 5`
  down. A surface target at y=64 in a default world drills ~33 steps to
  y=-64, i.e. ~**165 TNT entities** with 1-tick fuses; deepslate-level
  targets stop earlier, bedrock/obsidian stop almost immediately.

## Main performance risks

1. **669 near-simultaneous vanilla explosions**: each explosion runs the
   vanilla ~1,400-ray block-density calculation on the server thread, plus
   block updates, lighting updates, and entity damage scans.
2. **Item-entity floods**: crater block drops become hundreds of item
   entities (vanilla behavior; the reference mod's 25% drop reduction is a
   mixin this port does not include — see Limitations).
3. **Client render load**: hundreds of TNT entities + explosion particles
   in one view; low-end clients may drop frames during the ~4 s fuse burn.
4. **Chunk loading**: strikes only fire in loaded chunks (unloaded targets
   are dropped with a warning), but the crater itself can trigger
   lighting/neighbor updates across chunk borders.

## Safeguards and configuration limits

- `StrikeScheduler`: server-thread-only queue, configurable
  `strikeDelayTicks` (default 10), per-player `playerCooldownTicks`
  (default 100), `maxActiveStrikes` (default 4), duplicate suppression
  (same caster + target + mode), and `nukePayloadLimit` (default 700,
  above the 669 baseline so the full pattern spawns uncapped by default).
- Coordinate/dimension validation at schedule time and revalidation
  (loaded chunk, world height) at fire time; state pruned on world unload
  and cleared on server shutdown with log lines for every rejection.
- Ring geometry is precomputed once per strike
  (`NukePattern.generatePlacements`); per-charge work is O(1).
- No off-thread world mutation anywhere: all entity spawns and block
  queries run on the server thread. (The reference mod's parallel
  ForkJoinPool explosion mixin was deliberately **not** ported — see
  Limitations.)

## CI tests and benchmark methodology

- `NukePatternTest` asserts the exact entity budget (668 + 1) and that
  every placement lands inside its ring band, so footprint regressions
  fail the build.
- `StabPatternTest` asserts the 5-per-step grouping and stopping rule,
  bounding worst-case column depth via `stabMaxSteps`.
- `StrikeRulesTest` covers cooldown, duplicate-window, range, and target
  sanity logic.
- Runtime method (not run in CI): profile a creative-world nuke with
  Spark (`/spark profiler --timeout 30`), record mean tick time and
  max-tick, repeat 3×, compare against the same world without the mod.
  CI cannot launch Minecraft, so no such numbers exist yet.

## Measured results

None yet — no runtime benchmark has been executed (CI-only rule forbids
local game launches; Actions runs `gradle build`, not a server). Any TPS
or timing claim added later must cite a Spark profile attached to the
workflow run.

## Limitations of performance claims

- "Safe by default" means *bounded and validated*, not *lag-free*: a full
  nuke on a weak server will still spike the tick. Operators on shared
  hardware should lower `nukePayloadLimit` / `maxActiveStrikes` or raise
  `playerCooldownTicks`.
- `terrainDestruction` and `reduceBlockDrops` are parsed and validated but
  **not enforced** in 1.0.0 (vanilla explosion behavior always applies);
  enforcing them requires an explosion mixin planned for a later release.
