"""
threeeras.pipeline.stage_s1
~~~~~~~~~~~~~~~~~~~~~~~~~~~
Runner do Estágio S1 — gera heightmap.npy e o PNG de diagnóstico.
"""

from __future__ import annotations

import argparse
import sys
import time
from pathlib import Path

import numpy as np

from .influence import build_heightmap, render_heightmap_png
from .schema import WorldPlanError, load_default_plan, summarize

ARTIFACTS = Path(__file__).resolve().parent.parent / "artifacts"


def main() -> int:
    ap = argparse.ArgumentParser(description="ThreeEras — Estágio S1 (heightmap)")
    ap.add_argument("--out", default=str(ARTIFACTS / "heightmap.npy"))
    ap.add_argument("--png", default=str(ARTIFACTS / "heightmap_preview.png"))
    args = ap.parse_args()

    print("=" * 66)
    print("  ThreeEras — Estágio S1: Heightmap")
    print("=" * 66)

    try:
        plan = load_default_plan()
    except WorldPlanError as e:
        print(f"\nFALHA NA VALIDAÇÃO DO PLANO:\n  {e}", file=sys.stderr)
        return 1

    print(summarize(plan))

    t0 = time.time()
    heightmap = build_heightmap(plan)
    dt = time.time() - t0

    Path(args.out).parent.mkdir(parents=True, exist_ok=True)
    np.save(args.out, heightmap)

    print(f"\n[S1] salvo: {args.out}")
    print(f"[S1] shape={heightmap.shape} dtype={heightmap.dtype} "
          f"tempo={dt:.1f}s ({heightmap.size / max(dt, 1e-9) / 1e6:.2f} Mpts/s)")

    try:
        render_heightmap_png(heightmap, args.png, sea_level=plan.meta["sea_level"])
        print(f"[S1] preview: {args.png}")
    except ImportError:
        print("[S1] matplotlib ausente — preview PNG ignorado", file=sys.stderr)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())