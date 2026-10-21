"""
threeeras.pipeline.surface_preview
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Diagnóstico visual do Estágio S3.
"""

from __future__ import annotations

import numpy as np

from .schema import BiomeSpec, WorldPlan
from .surface import SLOPE_THRESHOLD, _slope


def render_surface_preview(
    plan: WorldPlan,
    heights: np.ndarray,
    biome_map: np.ndarray,
    biomes: tuple[BiomeSpec, ...],
    out_path: str,
    *,
    title: str = "ThreeEras — Superfície (Era Medieval)",
) -> str:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    sea = plan.meta["sea_level"]
    origin_x = plan.meta["extent_origin"]["world_x"]

    row_idx = heights.shape[0] // 2
    profile = heights[row_idx, :]
    slope = _slope(heights)

    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(16, 10), dpi=100)

    x = np.arange(heights.shape[1]) + origin_x
    ax1.fill_between(x, plan.meta["y_min"], profile, color="#6b5b4a", alpha=0.9, label="Terreno")
    ax1.axhline(sea, color="#2b6cb0", linewidth=1.5, linestyle="--", label=f"Nível do mar ({sea})")
    ax1.plot(x, profile, color="#3d2f24", linewidth=0.8)

    cell = plan.meta["cell_size"]
    for c in range(1, plan.meta["grid"]["cols"]):
        ax1.axvline(origin_x + c * cell, color="#888", linewidth=0.7, linestyle=":", alpha=0.7)

    ox = plan.meta["origin"]["world_x"]
    ax1.axvline(ox, color="#c53030", linewidth=1.2, linestyle="-", label=f"Vila Inicial (X={ox})")

    ax1.set_title(f"{title} — Corte transversal em Z={row_idx} (h∈[{profile.min():.0f}, {profile.max():.0f}])")
    ax1.set_xlabel("X (blocos, coordenadas de mundo)")
    ax1.set_ylabel("Y (absoluto)")
    ax1.legend(loc="upper right", fontsize=8)
    ax1.grid(alpha=0.15)

    im = ax2.imshow(slope, cmap="magma", origin="upper", vmin=0, vmax=max(SLOPE_THRESHOLD * 2, float(slope.max())))
    ax2.contour(slope, levels=[SLOPE_THRESHOLD], colors="#00ffcc", linewidths=0.8)
    ax2.set_title(f"Exposição de encosta — ciano marca o limiar ({SLOPE_THRESHOLD:.1f} blocos/bloco)")
    ax2.set_xlabel("X (blocos)")
    ax2.set_ylabel("Z (blocos)")
    fig.colorbar(im, ax=ax2, shrink=0.7, label="|gradiente| máx")

    fig.tight_layout()
    fig.savefig(out_path)
    plt.close(fig)
    return out_path