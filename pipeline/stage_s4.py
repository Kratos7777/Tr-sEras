"""Estágio S4 — Instalação de estruturas e POIs (ancoragens + site-prep)."""

from __future__ import annotations

import argparse
import json
from pathlib import Path

import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .schema import WorldPlan, load_default_plan


def world_to_idx(plan, wx, wz):
    ox = plan.meta["extent_origin"]["world_x"]
    oz = plan.meta["extent_origin"]["world_z"]
    cs_x = plan.meta["extent_blocks"]["x"]
    cs_z = plan.meta["extent_blocks"]["z"]
    ix = int(wx - ox)
    iz = int(wz - oz)
    ix = max(0, min(cs_x - 1, ix))
    iz = max(0, min(cs_z - 1, iz))
    return iz, ix  # (row, col) para arrays (z, x)


def footprint_pixels(plan, anchor):
    ox = plan.meta["extent_origin"]["world_x"]
    oz = plan.meta["extent_origin"]["world_z"]
    e = anchor.extent
    r0, c0 = world_to_idx(plan, e["min_x"], e["min_z"])
    r1, c1 = world_to_idx(plan, e["max_x"], e["max_z"])
    if r0 > r1:
        r0, r1 = r1, r0
    if c0 > c1:
        c0, c1 = c1, c0
    # garantir ao menos 1x1
    r1 = max(r1, r0)
    c1 = max(c1, c0)
    return r0, r1, c0, c1


def analyze_slope(hm, r0, r1, c0, c1):
    patch = hm[r0:r1 + 1, c0:c1 + 1]
    if patch.size == 0 or patch.shape[0] < 2 or patch.shape[1] < 2:
        return (float(patch.mean()) if patch.size else 0.0), 0.0
    gz, gx = np.gradient(patch)
    slope = np.hypot(gz, gx)
    return float(patch.mean()), float(slope.max())


def terrace(hm, r0, r1, c0, c1, target_y, blend=0.6):
    """Nivela o footprint para target_y com transição suave nas bordas."""
    rows = np.arange(r0, r1 + 1)
    cols = np.arange(c0, c1 + 1)
    rr, cc = np.meshgrid(rows, cols, indexing="ij")
    # distância à borda (em células) para blend
    dr = np.minimum(rr - r0, r1 - rr)
    dc = np.minimum(cc - c0, c1 - cc)
    edge_dist = np.minimum(dr, dc).astype(np.float32)
    maxd = max(1.0, edge_dist.max())
    w = np.clip(edge_dist / maxd, 0.0, 1.0) ** 2  # centro mais forte
    w = (w * blend + (1 - blend))  # mantém parte natural
    patch = hm[r0:r1 + 1, c0:c1 + 1]
    new = patch * (1 - w) + target_y * w
    hm[r0:r1 + 1, c0:c1 + 1] = new.astype(np.float32)
    return hm


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--anchors", default=None, help="lista de anchors a instalar (csv)")
    ap.add_argument("--blend", type=float, default=0.7)
    args = ap.parse_args()

    plan = load_default_plan()
    all_anchors = []
    for region in plan.regions:
        for a in region.settlement_anchors:
            all_anchors.append((region.id, a))

    artifacts = Path("artifacts")
    hm_path = artifacts / "heightmap.npy"
    heightmap = np.load(hm_path).astype(np.float32).copy()
    original = heightmap.copy()

    selected = None
    if args.anchors:
        selected = set(a.strip() for a in args.anchors.split(","))

    report = []
    centers = []
    for region_id, anchor in all_anchors:
        if selected and anchor.id not in selected:
            continue
        r0, r1, c0, c1 = footprint_pixels(plan, anchor)
        mean_y, max_slope = analyze_slope(heightmap, r0, r1, c0, c1)
        target = round(mean_y)
        # terracing: nivelar para a média local arredondada
        terrace(heightmap, r0, r1, c0, c1, target, blend=args.blend)
        cx, cz = anchor.center
        cr, cc = world_to_idx(plan, cx, cz)
        centers.append((cr, cc, anchor.id))
        report.append({
            "region": region_id,
            "anchor": anchor.id,
            "center": [cx, cz],
            "footprint_px": {"r0": r0, "r1": r1, "c0": c0, "c1": c1},
            "footprint_size_px": [(r1 - r0 + 1), (c1 - c0 + 1)],
            "mean_y_before": round(mean_y, 2),
            "max_slope_before": round(max_slope, 3),
            "target_y": target,
            "blend": args.blend,
        })
        print(f"  - {anchor.id}: footprint=({r1-r0+1}x{c1-c0+1}) slope_max={max_slope:.3f} target_y={target}")

    out_hm = artifacts / "heightmap_s4.npy"
    np.save(out_hm, heightmap)
    report_path = artifacts / "s4_report.json"
    report_path.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")

    # preview
    fig, ax = plt.subplots(figsize=(10, 10), dpi=100)
    diff = heightmap - original
    im = ax.imshow(diff, cmap="RdBu_r", origin="upper", vmin=-5, vmax=5)
    for cr, cc, aid in centers:
        ax.plot(cc, cr, "ko", markersize=4)
        ax.text(cc + 4, cr, aid, fontsize=6, color="black")
    ax.set_title("S4 — site preparation (delta heightmap)")
    fig.colorbar(im, ax=ax, shrink=0.7, label="ΔY")
    fig.tight_layout()
    preview = artifacts / "structures_preview.png"
    fig.savefig(preview)
    plt.close(fig)

    print(f"[S4] {len(report)} anchors processados")
    print(f"[S4] heightmap salvo: {out_hm}")
    print(f"[S4] relatório: {report_path}")
    print(f"[S4] preview: {preview}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
