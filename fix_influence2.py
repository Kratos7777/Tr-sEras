"""Adicionar build_region_weights ao influence.py"""
from pathlib import Path

content = Path("pipeline/influence.py").read_text(encoding="utf-8")

if "def build_region_weights" not in content:
    addendum = '''

def build_region_weights(
    plan,
    shape: tuple[int, int],
    *,
    verbose: bool = True,
) -> tuple[dict, tuple[int, int]]:
    """Constrói o campo de influência por região (reutilizável em S1/S2)."""
    from .schema import Region, WorldPlan

    ox = plan.meta["extent_origin"]["world_x"]
    oz = plan.meta["extent_origin"]["world_z"]
    origin = (ox, oz)

    weights = {}
    for region in plan.regions:
        w = region_weight_field(
            shape,
            region.influence.center,
            float(region.influence.radius),
            float(region.influence.falloff),
            origin=origin,
        )
        weights[region.id] = w
        if verbose:
            print(f"       {region.id:28s} peso max={w.max():.3f}")
    return weights, origin
'''
    Path("pipeline/influence.py").write_text(content + addendum, encoding="utf-8")
    print("OK - build_region_weights adicionado")
else:
    print("build_region_weights ja existe")