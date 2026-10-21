"""
threeeras.pipeline.biome
~~~~~~~~~~~~~~~~~~~~~~~~
Estágio S2 — mapa de biomas.
"""

from __future__ import annotations

import numpy as np

from .noise import fbm
from .schema import BiomeSpec, WorldPlan


def build_climate_fields(
    plan: WorldPlan,
    shape: tuple[int, int],
    weights: dict[str, np.ndarray],
    *,
    verbose: bool = True,
) -> tuple[np.ndarray, np.ndarray]:
    bm = plan.biome_noise
    seed = plan.meta["seed"]

    tn = bm["temperature_noise"]
    hn = bm["humidity_noise"]

    if verbose:
        print("[S2] 1/3 campos de clima (temperatura × umidade) ...")

    temperature = fbm(shape, tn["octaves"], 2.0, 0.5, tn["frequency"], seed=seed + 7_001)
    humidity = fbm(shape, hn["octaves"], 2.0, 0.5, hn["frequency"], seed=seed + 7_002)

    t_off = np.zeros(shape, dtype=np.float32)
    h_off = np.zeros(shape, dtype=np.float32)
    w_sum = np.zeros(shape, dtype=np.float32)

    for region_id, w in weights.items():
        region = plan.by_id[region_id]
        w_sum += w
        t_off += w * float(region.biome_offsets.get("temperature", 0.0))
        h_off += w * float(region.biome_offsets.get("humidity", 0.0))

    safe = np.maximum(w_sum, 1e-6)
    temperature += np.where(w_sum > 1e-6, t_off / safe, 0.0)
    humidity += np.where(w_sum > 1e-6, h_off / safe, 0.0)

    return (
        np.clip(temperature, 0.0, 1.0).astype(np.float32),
        np.clip(humidity, 0.0, 1.0).astype(np.float32),
    )


def dominant_region_index(
    plan: WorldPlan,
    shape: tuple[int, int],
    weights: dict[str, np.ndarray],
) -> tuple[np.ndarray, list[str]]:
    region_ids = list(weights.keys())
    if not region_ids:
        return np.full(shape, -1, dtype=np.int16), []

    stack = np.stack([weights[rid] for rid in region_ids], axis=0)
    best = np.argmax(stack, axis=0).astype(np.int16)
    max_w = np.max(stack, axis=0)

    best = np.where(max_w < 1e-3, np.int16(-1), best)
    return best, region_ids


def _mask_for_biome(
    biome: BiomeSpec,
    temperature: np.ndarray,
    humidity: np.ndarray,
) -> np.ndarray:
    return (
        (temperature >= biome.temp[0]) & (temperature <= biome.temp[1])
        & (humidity >= biome.hum[0]) & (humidity <= biome.hum[1])
    )

def build_biome_map(
    plan: WorldPlan,
    shape: tuple[int, int],
    weights: dict[str, np.ndarray],
    *,
    verbose: bool = True,
) -> tuple[np.ndarray, tuple[BiomeSpec, ...]]:
    if verbose:
        print("[S2] 2/3 clima â†’ Whittaker (2 passadas) ...")

    temperature, humidity = build_climate_fields(plan, shape, weights, verbose=verbose)
    index_of = {b.id: i for i, b in enumerate(plan.biomes)}
    biome_map = np.full(shape, 255, dtype=np.uint8)

    dominant, region_ids = dominant_region_index(plan, shape, weights)
    region_slot = {rid: i for i, rid in enumerate(region_ids)}

    regional = [b for b in plan.biomes if b.is_regional]
    for biome in regional:
        slot = region_slot.get(biome.region)
        if slot is None:
            continue
        in_region = dominant == slot
        if not in_region.any():
            continue
        mask = in_region & _mask_for_biome(biome, temperature, humidity)
        biome_map[mask] = index_of[biome.id]

    regional_filled = int((biome_map != 255).sum())

    unresolved = biome_map == 255
    globals_ = [b for b in plan.biomes if not b.is_regional]
    for biome in globals_:
        if not unresolved.any():
            break
        mask = unresolved & _mask_for_biome(biome, temperature, humidity)
        biome_map[mask] = index_of[biome.id]
        unresolved = biome_map == 255

    leftover = int(unresolved.sum())
    if leftover:
        fb = plan.biome_noise.get("fallback_biome")
        fb_idx = index_of.get(fb, 0)
        biome_map[unresolved] = fb_idx
        if verbose:
            print(f"       {leftover:,} pontos sem match â†’ fallback '{fb}'")

    if verbose:
        total = biome_map.size
        print(f"       regional: {regional_filled:,} ({regional_filled / total:.1%})")
        print(f"       global  : {total - regional_filled - leftover:,} "
              f"({(total - regional_filled - leftover) / total:.1%})")
        print(f"[S2] 3/3 {len(plan.biomes)} biomas avaliados")

    return biome_map, plan.biomes


def biome_distribution(
    biome_map: np.ndarray,
    biomes: tuple[BiomeSpec, ...],
) -> list[dict]:
    total = biome_map.size
    out: list[dict] = []
    for idx, biome in enumerate(biomes):
        count = int((biome_map == idx).sum())
        if count == 0:
            continue
        out.append({
            "id": biome.id,
            "label": biome.label or biome.id,
            "region": biome.region or "(global)",
            "pixels": count,
            "percent": round(count / total * 100, 3),
        })
    return sorted(out, key=lambda d: -d["pixels"])


_BIOME_COLORS = [
    "#a8d672", "#4f7942", "#8b8b8b", "#5c8a6e", "#b0a58f",
    "#c9e07a", "#dce8f0", "#6f9e5a", "#9fb46a", "#3f6b3a",
    "#4a7050", "#7d8a94", "#e8eef2", "#2d4a35", "#3a5c48",
    "#5a4a6b", "#7a6a8b", "#8d7f6a", "#a89a7c", "#b5aa90",
]


def render_biome_preview(
    biome_map: np.ndarray,
    biomes: tuple[BiomeSpec, ...],
    out_path: str,
    *,
    title: str = "ThreeEras â€” Mapa de Biomas (Era Medieval)",
) -> str:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.patches as mpatches
    import matplotlib.pyplot as plt
    from matplotlib.colors import ListedColormap

    n = len(biomes)
    colors = (_BIOME_COLORS * ((n // len(_BIOME_COLORS)) + 1))[:n]
    cmap = ListedColormap(colors)
    cmap.set_bad(color="#000000")

    fig, ax = plt.subplots(figsize=(15, 15), dpi=100)
    ax.imshow(biome_map, cmap=cmap, vmin=0, vmax=n - 1, origin="upper", interpolation="nearest")

    dist = biome_distribution(biome_map, biomes)
    color_of = {b.id: colors[i] for i, b in enumerate(biomes)}
    handles = [
        mpatches.Patch(
            color=color_of[d["id"]],
            label=f"{d['label']} [{d['region']}] â€” {d['percent']:.1f}%",
        )
        for d in dist
    ]
    ax.legend(handles=handles, loc="upper left", bbox_to_anchor=(1.01, 1.0), fontsize=8, framealpha=0.9)
    ax.set_title(f"{title}\n{len(dist)} biomas presentes de {n} declarados")
    ax.set_xlabel("X (blocos)")
    ax.set_ylabel("Z (blocos)")
    fig.tight_layout()
    fig.savefig(out_path, bbox_inches="tight")
    plt.close(fig)
    return out_path
