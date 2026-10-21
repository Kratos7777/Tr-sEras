"""
threeeras.pipeline.influence
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Funções de influência regional e composição do heightmap (Estágio S1).
"""

from __future__ import annotations

from typing import TYPE_CHECKING

import numpy as np

from .noise import fbm, hydraulic_erode, smoothstep, warp_field

if TYPE_CHECKING:
    from .schema import WorldPlan


def region_weight_field(
    shape: tuple[int, int],
    center: tuple[int, int],
    radius: float,
    falloff: str,
    origin: tuple[int, int] = (0, 0),
) -> np.ndarray:
    """
    Mapa de peso w(x,z) ∈ [0,1] para uma região.

    - center + radius: falloff radial a partir do centro
    - falloff: 'smoothstep' (padrão), 'linear', ou 'gaussian'
    """
    rz, rx = shape
    ox, oz = origin
    z = np.arange(rz, dtype=np.float32) + oz
    x = np.arange(rx, dtype=np.float32) + ox
    zz, xx = np.meshgrid(z, x, indexing="ij")

    cx, cz = center
    dist = np.hypot(xx - cx, zz - cz)

    r_inner = radius * 0.7
    r_outer = radius

    if falloff == "linear":
        t = np.clip((dist - r_inner) / max(r_outer - r_inner, 1e-5), 0.0, 1.0)
    elif falloff == "gaussian":
        sigma = (r_outer - r_inner) / 2.0
        t = 1.0 - np.exp(-0.5 * ((dist - r_inner) / max(sigma, 1e-5)) ** 2)
    else:  # smoothstep (padrão)
        t = np.clip((dist - r_inner) / max(r_outer - r_inner, 1e-5), 0.0, 1.0)
        t = t * t * (3.0 - 2.0 * t)
    weights = 1.0 - t
    return weights.astype(np.float32)


def build_region_weights(
    plan: "WorldPlan",
    shape: tuple[int, int],
    *,
    verbose: bool = True,
) -> tuple[dict, tuple[int, int]]:
    """Constrói o campo de influência por região (reutilizável em S1/S2)."""
    ox = plan.meta["extent_origin"]["world_x"]
    oz = plan.meta["extent_origin"]["world_z"]
    origin = (ox, oz)

    weights = {}
    for region in plan.regions:
        infl = region.influence
        if infl.mode == "perimeter_buffer":
            weights[region.id] = np.full(shape, 0.15, dtype=np.float32)
            continue
        if infl.center is None or infl.radius is None:
            continue
        w = region_weight_field(
            shape,
            infl.center,
            float(infl.radius),
            infl.falloff,
            origin=origin,
        )
        weights[region.id] = w
        if verbose:
            print(f"       {region.id:28s} peso max={w.max():.3f}")
    return weights, origin


def build_heightmap(plan: "WorldPlan", *, verbose: bool = True) -> np.ndarray:
    """Gera o heightmap completo do mundo (Estágio S1)."""
    meta = plan.meta
    extent_x = meta["extent_blocks"]["x"]
    extent_z = meta["extent_blocks"]["z"]
    shape = (extent_z, extent_x)
    seed = meta["seed"] & 0xFFFFFFFF  # converter int32 negativo para uint32

    if verbose:
        print(f"[S1] heightmap {extent_x}x{extent_z} ({extent_x * extent_z / 1e6:.1f}M pontos)")

    bn = plan.base_noise
    base = fbm(shape, bn.octaves, bn.lacunarity, bn.gain, bn.frequency, seed=seed)
    global_height = bn.base_height + (base - 0.5) * 2.0 * bn.amplitude

    if bn.warp and bn.warp.get("enabled"):
        global_height = warp_field(
            global_height,
            frequency=bn.warp["frequency"],
            strength=bn.warp["strength"],
            seed=seed,
        )

    weights, origin = build_region_weights(plan, shape, verbose=False)
    height_acc = np.zeros(shape, dtype=np.float32)
    weight_sum = np.zeros(shape, dtype=np.float32)

    for region_id, w in weights.items():
        region = plan.by_id[region_id]
        alt = region.altitude_profile.base
        height_acc += w * alt
        weight_sum += w

    uncovered = weight_sum < 1e-6
    if uncovered.any():
        height_acc = np.where(uncovered, global_height, height_acc)
        weight_sum = np.where(uncovered, 1.0, weight_sum)

    heightmap = height_acc / weight_sum

    er = plan.erosion
    if er.type and er.iterations > 0:
        heightmap = hydraulic_erode(
            heightmap,
            iterations=er.iterations,
            talus=er.talus_angle,
            deposit=er.sediment_deposit,
        )

    y_min, y_max = meta["y_min"], meta["y_max"]
    heightmap = np.clip(heightmap, y_min + 2, y_max - 20)

    if verbose:
        print(f"[S1] concluido - range [{heightmap.min():.0f}, {heightmap.max():.0f}]")
    return heightmap.astype(np.float32)


def render_heightmap_png(
    heightmap: np.ndarray,
    out_path: str,
    *,
    sea_level: int = 63,
    title: str = "ThreeEras - Heightmap",
) -> str:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    fig, ax = plt.subplots(figsize=(14, 14), dpi=100)
    im = ax.imshow(heightmap, cmap="terrain", origin="upper")
    ax.contour(heightmap, levels=12, colors="black", linewidths=0.3, alpha=0.35)
    cbar = fig.colorbar(im, ax=ax, shrink=0.75, label="Altura (Y absoluto)")
    cbar.ax.axhline(sea_level, color="blue", linewidth=2)
    ax.set_title(
        f"{title} | sea_level={sea_level} | "
        f"min={heightmap.min():.0f} max={heightmap.max():.0f}"
    )
    ax.set_xlabel("X (blocos)")
    ax.set_ylabel("Z (blocos)")
    fig.tight_layout()
    fig.savefig(out_path)
    plt.close(fig)
    return out_path