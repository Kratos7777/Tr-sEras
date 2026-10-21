"""
threeeras.pipeline.stage_s2
~~~~~~~~~~~~~~~~~~~~~~~~~~~
Runner do Estágio S2 — gera biomemap.npy, preview PNG e distribuição JSON.
"""

from __future__ import annotations

import json
import sys
import time
from pathlib import Path

import numpy as np

from .biome import biome_distribution, build_biome_map, render_biome_preview
from .influence import build_region_weights
from .schema import WorldPlanError, load_default_plan, summarize

ARTIFACTS = Path(__file__).resolve().parent.parent / "artifacts"


def main() -> int:
    print("=" * 66)
    print("  ThreeEras — Estágio S2: Biomas")
    print("=" * 66)

    try:
        plan = load_default_plan()
    except WorldPlanError as e:
        print(f"\nFALHA NA VALIDAÇÃO DO PLANO:\n  {e}", file=sys.stderr)
        return 1

    print(summarize(plan))
    print()

    extent_x = plan.meta["extent_blocks"]["x"]
    extent_z = plan.meta["extent_blocks"]["z"]
    shape = (extent_z, extent_x)

    t0 = time.time()

    print("[S2] calculando campo de influência (reuso de S1) ...")
    weights, origin = build_region_weights(plan, shape, verbose=True)
    print(f"     origem do extent: {origin}")

    biome_map, biomes = build_biome_map(plan, shape, weights)
    dt = time.time() - t0

    ARTIFACTS.mkdir(parents=True, exist_ok=True)
    np.save(ARTIFACTS / "biomemap.npy", biome_map)

    dist = biome_distribution(biome_map, biomes)
    (ARTIFACTS / "biome_distribution.json").write_text(
        json.dumps(dist, indent=2, ensure_ascii=False), encoding="utf-8",
    )

    print(f"\n[S2] biomemap salvo: {ARTIFACTS / 'biomemap.npy'}")
    print(f"[S2] shape={biome_map.shape} dtype={biome_map.dtype} tempo={dt:.1f}s")
    print(f"[S2] {len(dist)} biomas presentes:")
    for d in dist[:12]:
        print(f"       {d['percent']:6.2f}%  {d['label']:24s} [{d['region']}]")

    try:
        render_biome_preview(biome_map, biomes, str(ARTIFACTS / "biome_preview.png"))
        print(f"[S2] preview: {ARTIFACTS / 'biome_preview.png'}")
    except ImportError:
        print("[S2] matplotlib ausente — preview PNG ignorado", file=sys.stderr)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())