"""Script para adicionar build_heightmap e render_heightmap_png ao influence.py"""
from pathlib import Path

influence_path = Path("pipeline/influence.py")
content = influence_path.read_text(encoding="utf-8")

if "def build_heightmap" not in content:
    addendum = '''

def build_heightmap(plan, *, verbose: bool = True) -> np.ndarray:
    """Gera o heightmap completo do mundo (Estagio S1)."""
    from .noise import fbm, hydraulic_erode, warp_field

    meta = plan.meta
    extent_x = meta["extent_blocks"]["x"]
    extent_z = meta["extent_blocks"]["z"]
    shape = (extent_z, extent_x)
    seed = meta["seed"]

    if verbose:
        print(f"[S1] heightmap {extent_x}x{extent_z} ({extent_x * extent_z / 1e6:.1f}M pontos)")

    bn = plan.base_noise
    base = fbm(shape, bn["octaves"], bn["lacunarity"], bn["gain"], bn["frequency"], seed=seed)
    global_height = bn["base_height"] + (base - 0.5) * 2.0 * bn["amplitude"]

    if bn.get("warp", {}).get("enabled"):
        global_height = warp_field(
            global_height,
            frequency=bn["warp"]["frequency"],
            strength=bn["warp"]["strength"],
            seed=seed,
        )

    weights, origin = build_region_weights(plan, shape, verbose=False)
    height_acc = np.zeros(shape, dtype=np.float32)
    weight_sum = np.zeros(shape, dtype=np.float32)

    for region_id, w in weights.items():
        region = plan.by_id[region_id]
        alt = region.altitude_profile.get("base", meta["sea_level"])
        height_acc += w * alt
        weight_sum += w

    uncovered = weight_sum < 1e-6
    if uncovered.any():
        height_acc = np.where(uncovered, global_height, height_acc)
        weight_sum = np.where(uncovered, 1.0, weight_sum)

    heightmap = height_acc / weight_sum

    er = plan.erosion
    if er.get("enabled", False):
        heightmap = hydraulic_erode(heightmap, iterations=er.get("iterations", 10))

    y_min, y_max = meta["y_min"], meta["y_max"]
    heightmap = np.clip(heightmap, y_min + 2, y_max - 20)

    if verbose:
        print(f"[S1] concluido - range [{heightmap.min():.0f}, {heightmap.max():.0f}]")
    return heightmap.astype(np.float32)


def render_heightmap_png(heightmap, out_path, *, sea_level=63, title="ThreeEras - Heightmap"):
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
'''
    influence_path.write_text(content + addendum, encoding="utf-8")
    print("OK - build_heightmap e render_heightmap_png adicionados ao influence.py")
else:
    print("build_heightmap ja existe no influence.py")