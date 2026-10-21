"""
threeeras.pipeline.noise
~~~~~~~~~~~~~~~~~~~~~~~~
Estágio S1 (parte 1)  primitivas de ruído e erosão.

Fornece:
  - fbm()            : ruído fractal Browniano vetorizado em Numpy
  - ridged()         : ruído de cristas afiadas
  - warp_field()     : domain warping (forma orgânica)
  - hydraulic_erode(): erosão hidráulica simplificada
  - smoothstep()     : interpolação suave
  - fill_gaps()      : interpolação de lacunas mascaradas

Todas as funções operam sobre arrays 2D float32 e são determinísticas
dada a seed. Nenhuma dependência de Minecraft aqui.
"""

from __future__ import annotations

import numpy as np

# -----------------------------------------------------------------------
# Ruído base
# -----------------------------------------------------------------------

def _value_noise(
    shape: tuple[int, int],
    frequency: float,
    rng: np.random.Generator,
) -> np.ndarray:
    """
    Value noise 2D com interpolação smoothstep.

    Implementação própria (sem dependência externa) para garantir
    determinismo total e reprodutibilidade entre máquinas.
    """
    h, w = shape
    gh = max(2, int(h * frequency) + 2)
    gw = max(2, int(w * frequency) + 2)

    grid = rng.random((gh, gw)).astype(np.float32)

    ys = np.linspace(0, gh - 1, h, endpoint=False, dtype=np.float32)
    xs = np.linspace(0, gw - 1, w, endpoint=False, dtype=np.float32)
    yi = ys.astype(np.int32)
    xi = xs.astype(np.int32)
    yf = (ys - yi).reshape(-1, 1)
    xf = (xs - xi).reshape(1, -1)

    # Smoothstep (3t² - 2t³)  evita artefatos direcionais do lerp linear
    sy = yf * yf * (3.0 - 2.0 * yf)
    sx = xf * xf * (3.0 - 2.0 * xf)

    y0 = np.clip(yi, 0, gh - 2).reshape(-1, 1)
    x0 = np.clip(xi, 0, gw - 2).reshape(1, -1)
    y1, x1 = y0 + 1, x0 + 1

    top = grid[y0, x0] * (1 - sx) + grid[y0, x1] * sx
    bot = grid[y1, x0] * (1 - sx) + grid[y1, x1] * sx
    return (top * (1 - sy) + bot * sy).astype(np.float32)


def fbm(
    shape: tuple[int, int],
    octaves: int,
    lacunarity: float,
    gain: float,
    frequency: float,
    seed: int = 0,
) -> np.ndarray:
    """Ruído fractal Browniano (fBm) normalizado para [0, 1]."""
    rng = np.random.default_rng(seed & 0xFFFFFFFF)
    total = np.zeros(shape, dtype=np.float32)
    amplitude = 1.0
    freq = frequency
    norm = 0.0

    for _ in range(octaves):
        total += amplitude * _value_noise(shape, freq, rng)
        norm += amplitude
        amplitude *= gain
        freq *= lacunarity

    out = total / max(norm, 1e-9)
    lo, hi = float(out.min()), float(out.max())
    return (out - lo) / max(hi - lo, 1e-9)


def ridged(
    shape: tuple[int, int],
    octaves: int,
    lacunarity: float,
    gain: float,
    frequency: float,
    seed: int = 0,
) -> np.ndarray:
    """
    Ruído ridged (1 - |n|)  produz cristas afiadas.

    Usado pela Fronteira das Fortalezas, onde roughness alto pede
    cordilheira com arestas, não colinas arredondadas.
    """
    base = fbm(shape, octaves, lacunarity, gain, frequency, seed)
    return (1.0 - np.abs(base * 2.0 - 1.0)).astype(np.float32)


# -----------------------------------------------------------------------
# Domain warping
# -----------------------------------------------------------------------

def warp_field(
    field: np.ndarray,
    frequency: float,
    strength: float,
    seed: int = 0,
) -> np.ndarray:
    """
    Domain warping: desloca cada amostra de `field` por um vetor de ruído.

    Sem isto, fBm produz relevo "ondulado genérico". Com warp, os vales
    se curvam e as cristas serpenteiam.
    """
    shape = field.shape
    dx = fbm(shape, 4, 2.0, 0.5, frequency, seed=seed + 101) * 2.0 - 1.0
    dy = fbm(shape, 4, 2.0, 0.5, frequency, seed=seed + 202) * 2.0 - 1.0

    h, w = shape
    ys, xs = np.mgrid[0:h, 0:w]
    sy = np.clip(ys + (dy * strength).astype(np.int32), 0, h - 1)
    sx = np.clip(xs + (dx * strength).astype(np.int32), 0, w - 1)

    return field[sy, sx].astype(np.float32)


# -----------------------------------------------------------------------
# Erosão
# -----------------------------------------------------------------------

def hydraulic_erode(
    heights: np.ndarray,
    iterations: int = 3,
    talus: float = 0.6,
    deposit: float = 0.35,
) -> np.ndarray:
    """
    Erosão hidráulica simplificada (modelo talus + transporte de sedimento).

    Para cada iteração, cada célula compara-se aos 4 vizinhos: diferenças
    acima de `talus` transferem material, com parte depositada no vizinho
    mais baixo (`deposit`) e o resto perdido.

    Não é simulação física  é um operador de suavização anisotrópica que
    remove picos irreais e cria encostas coerentes.
    """
    h = heights.astype(np.float32).copy()
    for _ in range(iterations):
        center = h[1:-1, 1:-1]
        n = h[:-2, 1:-1]
        s = h[2:, 1:-1]
        w = h[1:-1, :-2]
        e = h[1:-1, 2:]

        for neigh in (n, s, w, e):
            diff = center - neigh
            excess = np.where(diff > talus, diff - talus, 0.0)
            moved = excess * deposit
            center -= moved * 0.25
            neigh += moved * 0.25 * deposit

    return h


# -----------------------------------------------------------------------
# Utilitários
# -----------------------------------------------------------------------

def smoothstep(edge0: float, edge1: float, x: np.ndarray) -> np.ndarray:
    """Smoothstep vetorizado  usado pelo campo de influência."""
    t = np.clip((x - edge0) / max(edge1 - edge0, 1e-9), 0.0, 1.0)
    return (t * t * (3.0 - 2.0 * t)).astype(np.float32)


def fill_gaps(field: np.ndarray, mask: np.ndarray) -> np.ndarray:
    """
    Preenche posições onde `mask` é True com a média dos vizinhos válidos.

    Usado quando uma região é mascarada (ex.: terreno sob o protótipo
    read_only não é gerado, mas a altura ainda é necessária para a
    transição da vizinhança).
    """
    out = field.copy()
    for _ in range(64):
        if not mask.any():
            break
        padded = np.pad(out, 1, mode="edge")
        neigh_mean = (
            padded[:-2, 1:-1] + padded[2:, 1:-1]
            + padded[1:-1, :-2] + padded[1:-1, 2:]
        ) / 4.0
        out[mask] = neigh_mean[mask]
    return out

