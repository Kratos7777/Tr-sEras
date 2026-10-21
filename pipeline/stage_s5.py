"""Estágio S5 — Exportador Sponge Schematic v2 (.schem) via nbtlib."""

from __future__ import annotations

import argparse
import gzip
from pathlib import Path

import numpy as np
from nbtlib import File, Compound, List, ByteArray, Int, Short, String

from nbtlib.tag import Compound as C


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--cell", default="3,9")
    ap.add_argument("--out", default="artifacts")
    args = ap.parse_args()

    col, row = (int(x) for x in args.cell.split(","))
    npz = Path(f"artifacts/consolidated/cell_{col}_{row}.npz")
    if not npz.exists():
        npz = Path(f"artifacts/blockmap/cell_{col}_{row}.npz")
    if not npz.exists():
        print(f"[S5] blockmap não encontrado: {npz}")
        return 1

    z = np.load(npz)
    blocks = z["blocks"]  # (x, y, z)
    sx, sy, sz = blocks.shape
    palette_list = [str(p) for p in z["palette"]]

    # Flatten blocks to palette indices (placeholder: all air=0)
    flat = [0] * (sx * sy * sz)

    # Build Sponge Schematic v2 NBT
    schematic = Compound({
        "Version": Int(2),
        "DataVersion": Int(4903),
        "Width": Short(sx),
        "Height": Short(sy),
        "Length": Short(sz),
        "PaletteMax": Int(len(palette_list)),
        "Palette": Compound({name: Int(i) for i, name in enumerate(palette_list)}),
        "BlockData": ByteArray(flat),
    })

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    target = out_dir / f"cell_{col}_{row}.schem"

    nbt_file = File(schematic, gzipped=True)
    nbt_file.save(str(target))
    print(f"[S5] schematic salvo: {target} ({target.stat().st_size} bytes)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
