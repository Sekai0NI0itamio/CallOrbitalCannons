# License compliance

## This mod's license

CallOrbitalCannons is licensed under the **MIT License** (see `LICENSE`),
copyright 2026 Sekai0NI0itamio, consistent with its sibling mods in this
workspace. The full license text ships inside the mod JAR.

## Upstream reference and attribution

Technical behavior was ported from
`https://github.com/RelmesStudios/orbitalstrike` at commit
`067fbcddf6351608f4646c9f6ffaa9b6afae14c4`, whose `LICENSE` file is the
**Apache License, Version 2.0** (copyright 2026 Relmes Studios) with a
matching `NOTICE` file. Attribution is preserved in this repository's
`NOTICE` and in `docs/REFERENCE_RESEARCH.md` (revision, files studied,
algorithms reused, modifications).

Reuse method: **clean-room behavior port, no verbatim copying.** No source
file, asset, icon, or text was copied from the reference repository. The
ported elements are uncopyrightable facts and simple mathematics: ring
radii/counts, the TNT drag closed form `(1 − 0.98^fuse) / 0.02`, the
disc-plus-clamp misalignment procedure, stab step/offset/fuse constants,
and the 1200.0 blast-resistance stopping threshold. All implementation
files in this repository were written fresh against the 1.21.1 Yarn API,
which additionally forced structural differences (different entity,
component, recipe, and command classes).

Because no Apache-2.0-licensed code is redistributed verbatim, no Apache
license text is bundled; the `NOTICE` attribution is kept as a courtesy
and provenance record, which exceeds the requirement for a behavior port.

## Other references

- Wemmbu's Orbital Cannons (CurseForge, All Rights Reserved): behavioral
  reference only. No code or assets taken; no endorsement implied by any
  party. Wemmbu, Cubicmetre, and Relmes Studios do not endorse this mod.
- Thetonic's video and the original redstone contraption: concept only.

## Dependency license audit

| Dependency | License | Notes |
|---|---|---|
| Fabric Loader 0.16.14 | Apache-2.0 | loader only, not bundled |
| Fabric API 0.102.0+1.21.1 | Apache-2.0 | not bundled |
| Yarn mappings 1.21.1+build.3 | CC0-1.0 | mappings only |
| fabric-loom 1.7.4 | MIT | build-time only |
| Gson | Apache-2.0 | via Fabric Loader classpath |
| SLF4J | MIT | via Loader/Minecraft |
| JUnit 5.10.3 / platform 1.10.3 | EPL-2.0 | test scope only, not shipped |

License labels above follow each project's published metadata
(fabricmc.net / GitHub). No dependency is bundled into the mod JAR
beyond normal Fabric mod compilation; there are no vendored sources.
