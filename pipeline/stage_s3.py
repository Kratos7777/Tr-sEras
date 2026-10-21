"""
threeeras.pipeline.stage_s3
~~~~~~~~~~~~~~~~~~~~~~~~~~~
Runner do Estágio S3 — blockmap chunked por célula + preview de perfil.

Requer heightmap.npy (S1) e biomemap.npy (S2) em artifacts/.
"""

from __future__ import annotations

import argparse
import sys
import time
from pathlib import Path

import numpy as np

from .schema import WorldPlanError, load_default_plan, summarize
from .surface import build_cell, carve_subsurface

ARTIFACTS = Path(__file__).resolve().parent.parent / "artifacts"
BLOCKMAP_DIR = ARTIFACTS / "blockmap"


def _parse_cells(raw: list[str]) -> list[tuple[int, int]] | None:
    if not raw:
        return None
    out: list[tuple[int, int]] = []
    for item in raw:
        col, row = item.split(",")
        out.append((int(col), int(row)))
    return out


def main() -> int:
    ap = argparse.ArgumentParser(description="ThreeEras — Estágio S3 (superfície)")
    ap.add_argument("--cells", nargs="*", default=None,
                    help="células específicas, ex: 3,9 (padrão: todas)")
    ap.add_argument("--skip-carve", action="store_true",
                    help="não escavar subsolo")
    args = ap.parse_args()

    print("=" * 66)
    print("  ThreeEras — Estágio S3: Superfície / Blockmap")
    print("=" * 66)

    try:
        plan = load_default_plan()
    except WorldPlanError as e:
        print(f"\nFALHA NA VALIDAÇÃO DO PLANO:\n  {e}", file=sys.stderr)
        return 1

    print(summarize(plan))

    h_path = ARTIFACTS / "heightmap.npy"
    b_path = ARTIFACTS / "biomemap.npy"
    if not h_path.exists():
        print(f"\nFALTA {h_path} — rode: python -m pipeline.stage_s1", file=sys.stderr)
        return 1
    if not b_path.exists():
        print(f"\nFALTA {b_path} — rode: python -m pipeline.stage_s2", file=sys.stderr)
        return 1

    heights = np.load(h_path)
    biome_map = np.load(b_path)
    biomes = plan.biomes

    print(f"\n[S3] heightmap {heights.shape} | biomemap {biome_map.shape}")

    cols = plan.meta["grid"]["cols"]
    rows = plan.meta["grid"]["rows"]
    requested = _parse_cells(args.cells) if args.cells else None

    targets: list[tuple[int, int]] = []
    for row in range(rows):
        for col in range(cols):
            if requested is not None and (col, row) not in requested:
                continue
            targets.append((col, row))

    if not targets:
        print("Nenhuma célula selecionada.", file=sys.stderr)
        return 1

    BLOCKMAP_DIR.mkdir(parents=True, exist_ok=True)

    cs = plan.meta["cell_size"]
    print(f"\n[S3] {len(targets)} célula(s) de "
          f"{cs}² × {plan.meta['y_max'] - plan.meta['y_min']} blocos")
    print(f"     destino: {BLOCKMAP_DIR}\n")

    def cell_is_protected(col: int, row: int) -> bool:
        wx, wz = plan.cell_to_world(col, row)
        for (bx0, bx1, bz0, bz1) in plan.protected_boxes:
            if wx + cs > bx0 and wx < bx1 and wz + cs > bz0 and wz < bz1:
                return True
        return False

    total_blocks = 0
    t_all = time.time()

    for col, row in targets:
        prot = cell_is_protected(col, row)
        tag = " [PROTEGIDA]" if prot else ""
        print(f"[{col},{row}]{tag} gerando camadas de solo...")

        cell = build_cell(plan, heights, biome_map, biomes, col, row, verbose=True)

        if not args.skip_carve:
            for region in plan.regions:
                for sub in region.subsurface_domains:
                    carve_subsurface(cell, {
                        "depth_range": (
                            sub.depth_range[0] - plan.meta["y_min"],
                            sub.depth_range[1] - plan.meta["y_min"],
                        ),
                        "volume_ratio": sub.volume_ratio,
                    }, seed=plan.meta["seed"] + col * 31 + row * 17)

        out_file = BLOCKMAP_DIR / f"cell_{col}_{row}.npz"
        np.savez_compressed(out_file, palette=cell["palette"], blocks=cell["blocks"])
        sz = out_file.stat().st_size / 1e6
        total_blocks += cell["blocks"].size
        print(f"       salvo: {out_file.name} ({sz:.1f} MB)\n")

    dt = time.time() - t_all
    print(f"[S3] concluído em {dt:.1f}s ({total_blocks / max(dt, 1e-9) / 1e6:.2f} Mblks/s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())