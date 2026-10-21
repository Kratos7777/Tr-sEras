"""
threeeras.pipeline.surface
~~~~~~~~~~~~~~~~~~~~~~~~~~
EstÃ¡gio S3 â€” camadas de solo, exposiÃ§Ã£o de encosta e carve de subsolo.
"""

from __future__ import annotations

import numpy as np
from .schema import BiomeSpec, WorldPlan

BLOCK_IDS: dict[str, int] = {
    "minecraft:air": 0,
    "minecraft:stone": 1,
    "minecraft:deepslate": 2,
    "minecraft:grass_block": 9,
    "minecraft:dirt": 10,
    "minecraft:coarse_dirt": 11,
    "minecraft:podzol": 12,
    "minecraft:gravel": 13,
    "minecraft:sand": 24,
    "minecraft:water": 34,
    "minecraft:bedrock": 85,
    "minecraft:moss_block": 100,
    "minecraft:mud": 101,
    "minecraft:snow_block": 102,
}

BIOME_SURFACE: dict[str, tuple[str, str, str, str]] = {
    "plains":            ("minecraft:grass_block", "minecraft:dirt", "minecraft:stone", "minecraft:stone"),
    "deep_forest":       ("minecraft:grass_block", "minecraft:dirt", "minecraft:stone", "minecraft:stone"),
    "mountains":         ("minecraft:stone", "minecraft:gravel", "minecraft:stone", "minecraft:stone"),
    "swamp_edges":       ("minecraft:mud", "minecraft:dirt", "minecraft:stone", "minecraft:stone"),
    "karst":             ("minecraft:grass_block", "minecraft:gravel", "minecraft:stone", "minecraft:stone"),
    "vila_plains":       ("minecraft:grass_block", "minecraft:coarse_dirt", "minecraft:stone", "minecraft:stone"),
    "vila_snow_plains": ("minecraft:snow_block", "minecraft:coarse_dirt", "minecraft:stone", "minecraft:stone"),
    "capital_riverlands":("minecraft:grass_block", "minecraft:dirt", "minecraft:stone", "minecraft:stone"),
    "capital_plateau":   ("minecraft:grass_block", "minecraft:coarse_dirt", "minecraft:stone", "minecraft:stone"),
    "root_kingdom":      ("minecraft:moss_block", "minecraft:podzol", "minecraft:stone", "minecraft:stone"),
    "root_kingdom_deep": ("minecraft:podzol", "minecraft:podzol", "minecraft:stone", "minecraft:stone"),
    "fortress_alpine":   ("minecraft:gravel", "minecraft:gravel", "minecraft:deepslate", "minecraft:deepslate"),
    "fortress_snowcap":  ("minecraft:snow_block", "minecraft:gravel", "minecraft:deepslate", "minecraft:deepslate"),
    "monster_forest":    ("minecraft:podzol", "minecraft:coarse_dirt", "minecraft:stone", "minecraft:stone"),
    "monster_swamp":     ("minecraft:mud", "minecraft:coarse_dirt", "minecraft:stone", "minecraft:stone"),
    "karst_archive":     ("minecraft:grass_block", "minecraft:gravel", "minecraft:deepslate", "minecraft:deepslate"),
    "karst_caverns":     ("minecraft:coarse_dirt", "minecraft:gravel", "minecraft:deepslate", "minecraft:deepslate"),
    "wilderness":        ("minecraft:grass_block", "minecraft:coarse_dirt", "minecraft:stone", "minecraft:stone"),
}

SOIL_DEPTH = 4
BEDROCK_THICKNESS = 1
SLOPE_THRESHOLD = 4.0
COAST_BAND = 3
DEEPSLATE_Y = 0
COAST_TOP = "minecraft:sand"
COAST_SUB = "minecraft:gravel"
WATER_ID_NAME = "minecraft:water"


def _slope(heights: np.ndarray) -> np.ndarray:
    gy = np.zeros_like(heights, dtype=np.float32)
    gx = np.zeros_like(heights, dtype=np.float32)
    gy[1:-1, :] = (heights[2:, :] - heights[:-2, :]) * 0.5
    gx[:, 1:-1] = (heights[:, 2:] - heights[:, :-2]) * 0.5
    return np.maximum(np.abs(gx), np.abs(gy)).astype(np.float32)


def _resolve_palette_refs(
    plan: WorldPlan,
    biomes: tuple[BiomeSpec, ...],
) -> list[tuple[str, str, str, str]]:
    default = BIOME_SURFACE["wilderness"]
    return [BIOME_SURFACE.get(biome.id, default) for biome in biomes]

def build_cell(
    plan: WorldPlan,
    heights: np.ndarray,
    biome_map: np.ndarray,
    biomes: tuple[BiomeSpec, ...],
    col: int,
    row: int,
    *,
    verbose: bool = True,
) -> dict[str, np.ndarray]:
    cell_size = plan.meta["cell_size"]
    y_min, y_max = plan.meta["y_min"], plan.meta["y_max"]
    height = y_max - y_min
    sea_level = plan.meta["sea_level"]

    origin_x = plan.meta["extent_origin"]["world_x"]
    origin_z = plan.meta["extent_origin"]["world_z"]

    x0 = origin_x + col * cell_size
    z0 = origin_z + row * cell_size
    ix0 = x0 - origin_x
    iz0 = z0 - origin_z
    ix1, iz1 = ix0 + cell_size, iz0 + cell_size

    if ix0 < 0 or iz0 < 0 or ix1 > heights.shape[1] or iz1 > heights.shape[0]:
        raise IndexError(f"cÃ©lula [{col},{row}] fora do extent do heightmap")

    h_cell = heights[iz0:iz1, ix0:ix1]
    b_cell = biome_map[iz0:iz1, ix0:ix1]
    slope = _slope(h_cell)

    if verbose:
        print(f"     cÃ©lula [{col},{row}] hâˆˆ[{h_cell.min():.0f},{h_cell.max():.0f}] "
              f"slope mÃ¡x={slope.max():.1f}")

    materials = _resolve_palette_refs(plan, biomes)

    used: list[str] = ["minecraft:air"]
    lookup: dict[str, int] = {"minecraft:air": 0}

    def slot(name: str) -> int:
        if name not in lookup:
            lookup[name] = len(used)
            used.append(name)
        return lookup[name]

    blocks = np.zeros((cell_size, height, cell_size), dtype=np.uint16)

    for idx in np.unique(b_cell):
        top, sub, stone, rock = materials[int(idx)]
        slot(top); slot(sub); slot(stone); slot(rock)
    slot("minecraft:bedrock")
    slot(COAST_TOP); slot(COAST_SUB); slot("minecraft:water")
    slot("minecraft:deepslate")

    for lx in range(cell_size):
        for lz in range(cell_size):
            surface = int(round(float(h_cell[lz, lx])))
            b_idx = int(b_cell[lz, lx])
            top_n, sub_n, stone_n, rock_n = materials[b_idx]

            steep = slope[lz, lx] > SLOPE_THRESHOLD
            coastal = abs(surface - sea_level) < COAST_BAND

            if coastal:
                top_mat, sub_mat = COAST_TOP, COAST_SUB
            elif steep:
                top_mat, sub_mat = rock_n, stone_n
            else:
                top_mat, sub_mat = top_n, sub_n

            y_base = y_min
            y_top = min(surface, y_max - 1)

            for y in range(y_base, y_top + 1):
                ly = y - y_min
                if y < y_base + BEDROCK_THICKNESS:
                    blocks[lx, ly, lz] = slot("minecraft:bedrock")
                elif y < surface - SOIL_DEPTH:
                    blocks[lx, ly, lz] = slot(
                        "minecraft:deepslate" if y < DEEPSLATE_Y else stone_n
                    )
                elif y < surface:
                    blocks[lx, ly, lz] = slot(sub_mat)
                else:
                    blocks[lx, ly, lz] = slot(top_mat)

            if surface < sea_level:
                for y in range(surface + 1, min(sea_level, y_max - 1) + 1):
                    blocks[lx, y - y_min, lz] = slot(WATER_ID_NAME)

    palette = np.array([BLOCK_IDS.get(name, 0) for name in used], dtype=np.int32)

    if verbose:
        nonzero = int((blocks != 0).sum())
        print(f"       volume: {nonzero:,} blocos nÃ£o-ar "
              f"({nonzero / blocks.size:.1%}), palette {len(used)} materiais")

    return {"palette": palette, "blocks": blocks}

def carve_subsurface(
    cell: dict[str, np.ndarray],
    region_spec: dict,
    seed: int,
    *,
    verbose: bool = True,
) -> int:
    blocks = cell["blocks"]
    depth_lo, depth_hi = region_spec["depth_range"]
    ratio = float(region_spec["volume_ratio"])

    rng = np.random.default_rng(seed & 0xFFFFFFFF)
    cell_size = blocks.shape[0]

    y_lo = max(1, int(depth_lo))
    y_hi = min(blocks.shape[1] - 1, int(depth_hi))
    if y_hi <= y_lo:
        return 0

    n_spheres = max(1, int(ratio * 400))
    removed = 0

    for _ in range(n_spheres):
        cx = int(rng.integers(4, cell_size - 4))
        cz = int(rng.integers(4, cell_size - 4))
        cy = int(rng.integers(y_lo, y_hi))
        r = int(rng.integers(3, 9))

        x0, x1 = max(0, cx - r), min(cell_size, cx + r)
        z0, z1 = max(0, cz - r), min(cell_size, cz + r)
        y0, y1 = max(0, cy - r), min(blocks.shape[1], cy + r)

        xx, yy, zz = np.mgrid[x0:x1, y0:y1, z0:z1]
        dist = ((xx - cx) ** 2 + (yy - cy) ** 2 + (zz - cz) ** 2) <= r * r
        sub = blocks[x0:x1, y0:y1, z0:z1]
        removed += int((sub[dist] != 0).sum())
        sub[dist] = 0

    if verbose:
        print(f"       carve: {n_spheres} salÃµes, {removed:,} blocos removidos")
    return removed