# Performance

## Expected entity counts

- Nuke (default config): 1 center + 224 ring charges = **225 TNT
  entities** spawned in one server tick, fuse 79 ticks (~4 s), all
  exploding within a short window across a ~96-block-diameter footprint.
- Stab (default config): 1 charge per 2-block step across the full world
  height, top-down. A default world (-64..319) needs up to 192 steps, i.e.
  up to **192 TNT entities** with 1-tick fuses — fewer when the drill
  meets blast-resistant rock early.

## Main performance risks

1. **225 near-simultaneous vanilla explosions**: each explosion runs the
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
  `strikeDelayTicks` (default 10), optional `playerCooldownTicks`
  (default 0 = off; rods are single-use), `maxActiveStrikes` (default 4),
   duplicate suppression, and `nukePayloadLimit` (default 250, above the
   225 baseline so the full pattern spawns uncapped by default).
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

- `NukePatternTest` asserts the exact entity budget (224 + 1) and that
  every placement lands inside its ring band, so footprint regressions
  fail the build.
- `StabPatternTest` asserts the full bottom-to-top column coverage,
  the 5-per-step grouping, and the `stabMaxSteps` cap.
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
  225-TNT nuke or a 192-charge full-column stab on a weak server will
  still spike the tick. Operators on shared hardware should lower
  `nukePayloadLimit`/`maxActiveStrikes`/`stabMaxSteps`.
- `terrainDestruction` and `reduceBlockDrops` are parsed and validated but
  **not enforced** in 1.0.0 (vanilla explosion behavior always applies);
  enforcing them requires an explosion mixin planned for a later release.
