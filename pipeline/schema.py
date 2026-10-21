"""
threeeras.pipeline.schema
~~~~~~~~~~~~~~~~~~~~~~~~~~
Carregamento e validação fail-fast dos arquivos de planejamento de mundo.

Responsabilidades:
- Ler worldplan_medieval.json e palette_medieval.json
- Validar estrutura, coerência semântica e referências cruzadas
- Expor objetos tipados (dataclasses) para o resto da pipeline
- Abortar cedo e alto, com mensagem acionável, em qualquer inconsistência

Nada aqui gera terreno. Este módulo é puramente declarativo.
"""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Literal

MC_VERSION_TARGET = "26.2"
WORLD_Y_MIN = -64
WORLD_Y_MAX = 320
WORLD_HEIGHT = WORLD_Y_MAX - WORLD_Y_MIN
SEA_LEVEL_DEFAULT = 63
CELL_SIZE_DEFAULT = 256
WORLD_LAYOUT = "modern_dimensions"
DATAVERSION_TARGET = 4903

Falloff = Literal["smoothstep", "linear", "gaussian"]
TransitionStyle = Literal[
    "road_grade", "forest_thinning", "cliff_masked",
    "ridge", "ruin_scatter", "wilderness_blend",
]

class WorldPlanError(Exception):
    """Erro de validação do plano de mundo."""

@dataclass(frozen=True)
class NoiseSpec:
    type: str
    octaves: int
    lacunarity: float
    gain: float
    frequency: float
    amplitude: float
    base_height: float
    warp: dict[str, Any] | None = None


@dataclass(frozen=True)
class ErosionSpec:
    type: str
    iterations: int
    talus_angle: float
    sediment_deposit: float


@dataclass(frozen=True)
class BiomeSpec:
    id: str
    temp: tuple[float, float]
    hum: tuple[float, float]
    region: str | None = None
    palette_ref: str | None = None
    label: str = ""

    @property
    def is_regional(self) -> bool:
        return self.region is not None


@dataclass(frozen=True)
class Influence:
    center: tuple[int, int] | None
    radius: int | None
    falloff: Falloff
    mode: str | None = None


@dataclass(frozen=True)
class SubsurfaceDomain:
    id: str
    name: str
    anchor: tuple[int, int]
    depth_range: tuple[int, int]
    volume_ratio: float
    palette_ref: str
    protected: bool = False
    stage: int = 1


@dataclass(frozen=True)
class SettlementAnchor:
    id: str
    center: tuple[int, int]
    extent: dict[str, int]
    stage: int = 1

    @property
    def bounds(self) -> tuple[int, int, int, int]:
        e = self.extent
        return (e["min_x"], e["max_x"], e["min_z"], e["max_z"])


@dataclass(frozen=True)
class AltitudeProfile:
    base: float
    amplitude: float
    roughness: float
    type: str
    warp: dict[str, Any] | None = None


@dataclass(frozen=True)
class Density:
    structures_per_cell: int
    min_spacing: int


@dataclass(frozen=True)
class Adjacency:
    to: str
    transition_style: TransitionStyle
    width: int


@dataclass(frozen=True)
class Region:
    id: str
    name: str
    ring: int
    read_only: bool
    influence: Influence
    altitude_profile: AltitudeProfile
    biome_offsets: dict[str, float]
    palette_ref: str
    density: Density
    allowed_structures: tuple[str, ...]
    adjacency: tuple[Adjacency, ...]
    cells: tuple[tuple[int, int], ...] = ()
    settlement_anchors: tuple[SettlementAnchor, ...] = ()
    subsurface_domains: tuple[SubsurfaceDomain, ...] = ()

    @property
    def is_protected(self) -> bool:
        return self.read_only


@dataclass(frozen=True)
class Palette:
    base: tuple[str, ...]
    detail: tuple[str, ...]
    accent: tuple[str, ...]
    roof: tuple[str, ...]
    ground: tuple[str, ...]


@dataclass(frozen=True)
class WorldPlan:
    meta: dict[str, Any]
    safety: dict[str, Any]
    base_noise: NoiseSpec
    erosion: ErosionSpec
    biome_noise: dict[str, Any]
    biomes: tuple[BiomeSpec, ...]
    regions: tuple[Region, ...]
    palettes: dict[str, Palette]
    rules: dict[str, Any]

    by_id: dict[str, Region] = field(default_factory=dict)
    anchors_by_id: dict[str, SettlementAnchor] = field(default_factory=dict)
    protected_boxes: tuple[tuple[int, int, int, int], ...] = ()
    chunk_size: int = 16

    def region_at_anchor(self, anchor_id: str) -> Region | None:
        for region in self.regions:
            if any(a.id == anchor_id for a in region.settlement_anchors):
                return region
        return None

    def world_to_cell(self, world_x: int, world_z: int) -> tuple[int, int]:
        cs = self.meta["cell_size"]
        ox = self.meta["extent_origin"]["world_x"]
        oz = self.meta["extent_origin"]["world_z"]
        return ((world_x - ox) // cs, (world_z - oz) // cs)

    def cell_to_world(self, col: int, row: int) -> tuple[int, int]:
        cs = self.meta["cell_size"]
        ox = self.meta["extent_origin"]["world_x"]
        oz = self.meta["extent_origin"]["world_z"]
        return (ox + col * cs, oz + row * cs)

    def chunk_is_protected(self, chunk_x: int, chunk_z: int) -> bool:
        x0, z0 = chunk_x * self.chunk_size, chunk_z * self.chunk_size
        x1, z1 = x0 + self.chunk_size, z0 + self.chunk_size
        for (bx0, bx1, bz0, bz1) in self.protected_boxes:
            if x1 > bx0 and x0 < bx1 and z1 > bz0 and z0 < bz1:
                return True
        return False

# ═══════════════════════════════════════════════════════════════════════
# Helpers de validação
# ═══════════════════════════════════════════════════════════════════════

def _require(cond: bool, msg: str, *, where: str) -> None:
    if not cond:
        raise WorldPlanError(f"[{where}] {msg}")


def _require_keys(d: dict, keys: list[str], *, where: str) -> None:
    missing = [k for k in keys if k not in d]
    _require(not missing, f"campos obrigatórios ausentes: {missing}", where=where)


def _load_json(path: Path) -> dict:
    if not path.exists():
        raise WorldPlanError(f"arquivo não encontrado: {path}")
    try:
        with path.open("r", encoding="utf-8-sig") as fh:
            return json.load(fh)
    except json.JSONDecodeError as exc:
        raise WorldPlanError(
            f"JSON inválido em {path.name} (linha {exc.lineno}, col {exc.colno}): {exc.msg}"
        ) from exc


# ═══════════════════════════════════════════════════════════════════════
# Parsing
# ═══════════════════════════════════════════════════════════════════════

def _parse_palette(raw: dict, *, where: str) -> Palette:
    _require_keys(raw, ["base", "detail", "accent", "roof", "ground"], where=where)
    for key in ("base", "detail", "accent", "roof", "ground"):
        blocks = raw[key]
        _require(isinstance(blocks, list), f"'{key}' deve ser lista", where=where)
        for blk in blocks:
            _require(
                isinstance(blk, str) and ":" in blk,
                f"bloco '{blk}' deve ter namespace (ex: minecraft:stone)",
                where=where,
            )
    return Palette(
        base=tuple(raw["base"]), detail=tuple(raw["detail"]),
        accent=tuple(raw["accent"]), roof=tuple(raw["roof"]),
        ground=tuple(raw["ground"]),
    )


def _parse_region(raw: dict, *, where: str) -> Region:
    _require_keys(
        raw,
        ["id", "name", "ring", "read_only", "influence", "altitude_profile",
        "biome_offsets", "palette_ref", "density", "allowed_structures", "adjacency"],
        where=where,
    )

    if "cell" in raw:
        cells = (tuple(raw["cell"]),)
    elif "cells" in raw:
        cells = tuple(tuple(c) for c in raw["cells"])
    else:
        cells = ()
    for c in cells:
        _require(len(c) == 2, f"célula {c} deve ser [col, row]", where=where)

    infl = raw["influence"]
    influence = Influence(
        center=tuple(infl["center"]) if infl.get("center") else None,
        radius=infl.get("radius"),
        falloff=infl.get("falloff", "smoothstep"),
        mode=infl.get("mode"),
    )
    if influence.mode is None:
        _require(
            influence.center is not None and influence.radius is not None,
            "influence precisa de 'center' e 'radius' quando 'mode' está ausente",
            where=where,
        )

    alt = raw["altitude_profile"]
    _require_keys(alt, ["base", "amplitude", "roughness", "type"], where=f"{where}.altitude_profile")

    dens = raw["density"]
    _require_keys(dens, ["structures_per_cell", "min_spacing"], where=f"{where}.density")

    adjacency = tuple(
        Adjacency(to=a["to"], transition_style=a["transition_style"], width=a["width"])
        for a in raw["adjacency"]
    )

    anchors: list[SettlementAnchor] = []
    for a in raw.get("settlement_anchors", []):
        aw = f"{where}.settlement_anchors.{a['id']}"
        _require_keys(a, ["id", "center", "extent"], where=aw)
        _require_keys(a["extent"], ["min_x", "max_x", "min_z", "max_z"], where=f"{aw}.extent")
        e = a["extent"]
        _require(e["min_x"] <= e["max_x"], "extent min_x > max_x", where=aw)
        _require(e["min_z"] <= e["max_z"], "extent min_z > max_z", where=aw)
        cx, cz = a["center"]
        _require(
            e["min_x"] <= cx <= e["max_x"] and e["min_z"] <= cz <= e["max_z"],
            f"centro ({cx},{cz}) fora do proprio extent",
            where=aw,
        )
        anchors.append(SettlementAnchor(
            id=a["id"], center=tuple(a["center"]),
            extent=dict(a["extent"]), stage=int(a.get("stage", 1)),
        ))

    subs: list[SubsurfaceDomain] = []
    for s in raw.get("subsurface_domains", []):
        sw = f"{where}.subsurface_domains.{s['id']}"
        _require_keys(
            s,
            ["id", "name", "anchor", "depth_range", "volume_ratio", "palette_ref"],
            where=sw,
        )
        d_lo, d_hi = s["depth_range"]
        _require(d_lo < d_hi, f"depth_range invertido: [{d_lo},{d_hi}]", where=sw)
        _require(
            0.0 <= s["volume_ratio"] <= 1.0,
            f"volume_ratio {s['volume_ratio']} fora de [0,1]",
            where=sw,
        )
        subs.append(SubsurfaceDomain(
            id=s["id"], name=s["name"], anchor=tuple(s["anchor"]),
            depth_range=tuple(s["depth_range"]),
            volume_ratio=float(s["volume_ratio"]),
            palette_ref=s["palette_ref"],
            protected=bool(s.get("protected", False)),
            stage=int(s.get("stage", 1)),
        ))

    return Region(
        id=raw["id"], name=raw["name"], cells=cells, ring=raw["ring"],
        read_only=bool(raw["read_only"]), influence=influence,
        altitude_profile=AltitudeProfile(**alt), biome_offsets=dict(raw["biome_offsets"]),
        palette_ref=raw["palette_ref"], density=Density(**dens),
        allowed_structures=tuple(raw["allowed_structures"]),
        adjacency=adjacency, settlement_anchors=tuple(anchors),
        subsurface_domains=tuple(subs),
    )

# ═══════════════════════════════════════════════════════════════════════
# Validação semântica e cruzada
# ═══════════════════════════════════════════════════════════════════════

def _validate_semantics(plan_raw: dict) -> None:
    meta = plan_raw["meta"]

    declared = str(meta.get("minecraft_version", ""))
    _require(
        declared.startswith(("1.20", "1.21", "26.")),
        f"minecraft_version '{declared}' fora do alvo suportado (1.20+/26.x)",
        where="meta.minecraft_version",
    )
    dv = meta.get("dataversion")
    _require(
        isinstance(dv, int) and dv >= 3465,
        f"dataversion '{dv}' ausente ou inválido (esperado int >= 3465)",
        where="meta.dataversion",
    )

    y_min, y_max = meta["y_min"], meta["y_max"]
    _require(y_min == WORLD_Y_MIN, f"y_min deve ser {WORLD_Y_MIN}, veio {y_min}", where="meta.y_min")
    _require(y_max == WORLD_Y_MAX, f"y_max deve ser {WORLD_Y_MAX}, veio {y_max}", where="meta.y_max")
    _require(
        y_min < meta["sea_level"] < y_max,
        f"sea_level {meta['sea_level']} fora de ({y_min}, {y_max})",
        where="meta.sea_level",
    )
    _require(
        meta.get("heightmap_mode") == "absolute_blocks",
        "heightmap_mode deve ser 'absolute_blocks'",
        where="meta.heightmap_mode",
    )

    ero = plan_raw["global_layers"].get("erosion")
    _require(ero is not None, "global_layers.erosion ausente", where="global_layers")
    _require_keys(ero, ["type", "iterations", "talus_angle", "sediment_deposit"],
                where="global_layers.erosion")
    _require(ero["iterations"] >= 0, "erosion.iterations deve ser >= 0",
            where="global_layers.erosion")
    _require(0.0 < ero["talus_angle"] < 90.0,
            f"erosion.talus_angle {ero['talus_angle']} fora de (0, 90)",
            where="global_layers.erosion")
    _require(0.0 <= ero["sediment_deposit"] <= 1.0,
            f"erosion.sediment_deposit {ero['sediment_deposit']} fora de [0, 1]",
            where="global_layers.erosion")

    cols, rows = meta["grid"]["cols"], meta["grid"]["rows"]
    ext = meta["extent_blocks"]
    cell_size = meta["cell_size"]
    _require(
        cols * cell_size == ext["x"] and rows * cell_size == ext["z"],
        f"grid {cols}x{rows} × cell_size {cell_size} ≠ extent {ext}",
        where="meta.grid",
    )

    org = meta["extent_origin"]
    ox, oz = org["world_x"], org["world_z"]
    _require(
        org.get("corner") == "northwest",
        f"extent_origin.corner deve ser 'northwest', veio {org.get('corner')}",
        where="meta.extent_origin",
    )
    bb = plan_raw.get("safety", {}).get("prototype_bounding_box")
    if bb is not None:
        _require(
            ox <= bb["min_x"] and bb["max_x"] <= ox + ext["x"]
            and oz <= bb["min_z"] and bb["max_z"] <= oz + ext["z"],
            f"prototype_bounding_box [{bb['min_x']},{bb['max_x']}]x"
            f"[{bb['min_z']},{bb['max_z']}] não cabe no extent "
            f"[{ox},{ox + ext['x']}]x[{oz},{oz + ext['z']}]",
            where="meta.extent_origin",
        )

    origin = meta["origin"]
    _require(
        ox <= origin["world_x"] <= ox + ext["x"]
        and oz <= origin["world_z"] <= oz + ext["z"],
        f"origin ({origin['world_x']},{origin['world_z']}) fora do extent",
        where="meta.origin",
    )

    for r in plan_raw["regions"]:
        w = r["altitude_profile"].get("warp")
        if w is not None:
            _require_keys(w, ["frequency", "strength"],
                        where=f"regions.{r['id']}.altitude_profile.warp")

    for r in plan_raw["regions"]:
        ap = r["altitude_profile"]
        top = ap["base"] + ap["amplitude"]
        bottom = ap["base"] - ap["amplitude"]
        _require(
            bottom > y_min and top < y_max,
            f"região '{r['id']}': perfil de altitude [{bottom:.0f}, {top:.0f}] "
            f"extrapola o mundo [{y_min}, {y_max}]",
            where=f"regions.{r['id']}",
        )

def _validate_cross_references(plan: WorldPlan, plan_raw: dict) -> None:
    for region in plan.regions:
        _require(
            region.palette_ref in plan.palettes,
            f"região '{region.id}' aponta para paleta inexistente '{region.palette_ref}'",
where="palettes",
        )

    for biome in plan.biomes:
        if biome.region is not None:
            _require(
                biome.region in plan.by_id,
                f"bioma '{biome.id}' declara região inexistente '{biome.region}'",
                where=f"biomes.{biome.id}",
            )
        ref = biome.palette_ref
        if ref is None and biome.region is not None:
            ref = plan.by_id[biome.region].palette_ref
        # Biomas globais sem palette_ref: não exigimos paleta (usam padrão)
        if ref is not None:
            _require(
                ref in plan.palettes,
                f"bioma '{biome.id}' resolve para paleta inexistente '{ref}'",
                where=f"biomes.{biome.id}",
            )
        _require(
            biome.temp[0] <= biome.temp[1] and biome.hum[0] <= biome.hum[1],
            f"bioma '{biome.id}': faixa invertida temp={biome.temp} hum={biome.hum}",
            where=f"biomes.{biome.id}",
        )

    mode = plan_raw.get("partitioning", {}).get("mode", "planning_hint")
    _require(
        mode in ("planning_hint", "exclusive"),
        f"partitioning.mode '{mode}' inválido (use planning_hint ou exclusive)",
        where="partitioning",
    )
    seen: dict[tuple[int, int], list[str]] = {}
    for region in plan.regions:
        for cell in region.cells:
            _require(
                len(cell) == 2 and all(isinstance(v, int) for v in cell),
                f"célula {cell} inválida em '{region.id}'",
                where="partitioning",
            )
            seen.setdefault(cell, []).append(region.id)
    if mode == "exclusive":
        for cell, owners in seen.items():
            _require(
                len(owners) == 1,
                f"célula {cell} reivindicada por {owners} (mode=exclusive)",
                where="partitioning",
            )

    extent = plan.meta["extent_blocks"]
    ox = plan.meta["extent_origin"]["world_x"]
    oz = plan.meta["extent_origin"]["world_z"]
    seen_anchors: set[str] = set()
    for region in plan.regions:
        for a in region.settlement_anchors:
            _require(
                a.id not in seen_anchors,
                f"anchor '{a.id}' declarado em mais de uma região",
                where="settlement_anchors",
            )
            seen_anchors.add(a.id)
            x0, x1, z0, z1 = a.bounds
            _require(
                x0 >= ox and x1 <= ox + extent["x"]
                and z0 >= oz and z1 <= oz + extent["z"],
                f"anchor '{a.id}' extent [{x0},{x1}]x[{z0},{z1}] fora do extent",
                where=f"regions.{region.id}.settlement_anchors",
            )

    safety = plan_raw["safety"]
    for aid in safety.get("protected_anchors", []):
        _require(
            aid in seen_anchors,
            f"protected_anchors cita '{aid}' inexistente",
            where="safety.protected_anchors",
        )

    subs_ids: set[str] = set()
    for region in plan.regions:
        for s in region.subsurface_domains:
            _require(
                s.id not in subs_ids,
                f"subsurface_domain '{s.id}' duplicado",
                where="subsurface_domains",
            )
            subs_ids.add(s.id)
            _require(
                s.palette_ref in plan.palettes,
                f"subsurface_domain '{s.id}' aponta para paleta inexistente '{s.palette_ref}'",
                where="subsurface_domains",
            )
            # Subsurface domains são ancorados em settlement_anchors
            # mas a validação de existência é opcional para permitir
            # domains autônomos (ex: arquivos_entrada sem anchor de superfície)
            _require(
                s.depth_range[0] > plan.meta["y_min"],
                f"subsurface_domain '{s.id}' inicia abaixo de y_min",
                where="subsurface_domains",
            )
            _require(
                s.depth_range[1] <= plan.meta["y_max"],
                f"subsurface_domain '{s.id}' estende acima de y_max",
                where="subsurface_domains",
            )

    for aid in safety.get("protected_subsurface", []):
        _require(
            aid in subs_ids,
            f"protected_subsurface cita '{aid}' inexistente",
            where="safety.protected_subsurface",
        )

    # ── Regiões read_only: settlement_anchors protegidos ──
    for region in plan.regions:
        if region.read_only:
            _require(
                region.settlement_anchors,
                f"região read_only '{region.id}' não declara settlement_anchors",
                where=f"regions.{region.id}",
            )

    # ── protected_regions: cada uma deve existir como read_only ──
    for rid in safety.get("protected_regions", []):
        _require(
            rid in plan.by_id,
            f"protected_regions cita '{rid}' inexistente",
            where="safety.protected_regions",
        )
        _require(
            plan.by_id[rid].read_only,
            f"protected_regions cita '{rid}' que NAO é read_only",
            where="safety.protected_regions",
        )

    # ── adjacency: regiões adjacentes existem ──
    for region in plan.regions:
        for adj in region.adjacency:
            _require(
                adj.to in plan.by_id or adj.to == "*",
                f"região '{region.id}' adjacente a '{adj.to}' inexistente (use '*' para wildcards)",
                where=f"regions.{region.id}.adjacency",
            )
            _require(
                adj.width > 0,
                f"adjacency '{region.id}'->{adj.to}' width={adj.width} deve ser > 0",
                where=f"regions.{region.id}.adjacency",
            )

# ═══════════════════════════════════════════════════════════════════════
# Entry point
# ═══════════════════════════════════════════════════════════════════════

def load_worldplan(
    path: Path | str,
    *,
    palette_path: Path | str | None = None,
) -> WorldPlan:
    """
    Carga e valida o worldplan_medieval.json (e palette_medieval.json).

    Raises WorldPlanError em qualquer inconsistência — fail-fast,
    sem silenciar.
    """
    path = Path(path)
    where = f"worldplan:{path.name}"
    raw = _load_json(path)

    _require_keys(raw, ["meta", "safety", "base_noise", "erosion", "biome_noise",
                        "biomes", "regions", "palettes", "rules", "global_layers"],
                where=where)

    meta = raw["meta"]
    _require_keys(meta, ["project", "era", "schema_version", "minecraft_version",
                        "dataversion", "server", "world_height", "y_min", "y_max",
                        "sea_level", "heightmap_mode", "cell_size", "grid",
                        "extent_blocks", "extent_origin", "global_target_blocks",
                        "seed", "origin", "world_name", "dimension",
                        "world_layout", "region_path_template"],
                where="meta")

    # ── Palettes ──
    palettes: dict[str, Palette] = {}
    for pid, p in raw["palettes"].items():
        palettes[pid] = _parse_palette(p, where=f"palettes.{pid}")

    # ── Noise / erosion ──
    bn = raw["base_noise"]
    _require_keys(bn, ["type", "octaves", "lacunarity", "gain", "frequency",
                    "amplitude", "base_height"],
                where="base_noise")
    base_noise = NoiseSpec(**bn)

    ero = raw["erosion"]
    _require_keys(ero, ["type", "iterations", "talus_angle", "sediment_deposit"],
                where="erosion")
    erosion = ErosionSpec(**ero)

    # ── Biomas ──
    biomes: list[BiomeSpec] = []
    for b in raw["biomes"]:
        _require_keys(b, ["id", "temp", "hum"], where=f"biomes.{b.get('id','?')}")
        biomes.append(BiomeSpec(
            id=b["id"], temp=tuple(b["temp"]), hum=tuple(b["hum"]),
            region=b.get("region"), palette_ref=b.get("palette_ref"),
            label=b.get("label", ""),
        ))

    # ── Regiões ──
    regions: list[Region] = []
    for r in raw["regions"]:
        regions.append(_parse_region(r, where=f"regions.{r['id']}"))

    # ── Validação semântica ──
    _validate_semantics(raw)

    # ── Constrói o WorldPlan com os índices derivados ──
    by_id = {r.id: r for r in regions}
    anchors_by_id: dict[str, SettlementAnchor] = {}
    for region in regions:
        for a in region.settlement_anchors:
            anchors_by_id[a.id] = a

    # ── protected_boxes: envelope de cada anchor protegido ──
    safety = raw["safety"]
    protected_ids = set(safety.get("protected_anchors", []))
    protected_boxes: list[tuple[int, int, int, int]] = []
    for aid in protected_ids:
        a = anchors_by_id.get(aid)
        if a is None:
            continue
        x0, x1, z0, z1 = a.bounds
        protected_boxes.append((x0, x1, z0, z1))

    plan = WorldPlan(
        meta=meta, safety=safety, base_noise=base_noise, erosion=erosion,
        biome_noise=dict(raw["biome_noise"]), biomes=tuple(biomes),
        regions=tuple(regions), palettes=palettes, rules=dict(raw["rules"]),
        by_id=by_id, anchors_by_id=anchors_by_id,
        protected_boxes=tuple(protected_boxes),
    )

    # ── Validação cruzada (precisa do WorldPlan completo) ──
    _validate_cross_references(plan, raw)

    return plan


def load_palette(path: Path | str) -> dict[str, Palette]:
    """Carga apenas o arquivo de paletas (para tools que não precisam do plano)."""
    path = Path(path)
    raw = _load_json(path)
    return {pid: _parse_palette(p, where=f"palettes.{pid}") for pid, p in raw.items()}


def load_default_plan(path: str | Path = "config/worldplan_medieval.json") -> WorldPlan:
    """Carrega e valida o plano de mundo medieval padrão."""
    return load_worldplan(path)


def summarize(plan: WorldPlan) -> str:
    """Retorna um sumário textual legível do WorldPlan carregado."""
    m = plan.meta
    s = plan.safety
    return (
        f"WorldPlan: {m['project']} ({m['era']}) | "
        f"Versao: {m['schema_version']} (DataVersion {m['dataversion']})\n"
        f"Dimensoes: {m['extent_blocks']['x']}x{m['extent_blocks']['z']} blocos | "
        f"Seed: {m['seed']} | Nivel do Mar: {m['sea_level']}\n"
        f"Regioes: {len(plan.regions)} declaradas | Biomas: {len(plan.biomes)} declarados | "
        f"Protegidas: {', '.join(s['protected_regions'])}"
    )
