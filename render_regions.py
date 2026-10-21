"""Valida regions_config.json (coordenadas em grid) e plota regioes/rotas."""

from __future__ import annotations

import json
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

cfg = json.load(open("config/regions_config.json", encoding="utf-8"))
hm = np.load("artifacts/heightmap.npy")

for r in cfg["regions"]:
    c = r["center"]
    assert 0 <= c["x"] < 3072 and 0 <= c["z"] < 3072, f"{r['id']} fora"
print("Validacao OK:", len(cfg["regions"]), "regioes,", len(cfg["routes"]), "rotas")

by_id = {r["id"]: r for r in cfg["regions"]}

fig, ax = plt.subplots(figsize=(10, 10), dpi=100)
ax.imshow(hm, cmap="terrain", origin="upper")
for r in cfg["regions"]:
    c = r["center"]
    ax.plot(c["x"], c["z"], "ro", markersize=8)
    ax.text(c["x"] + 10, c["z"], r["name"], fontsize=7, color="black")
for rt in cfg["routes"]:
    a = by_id[rt["from"]]["center"]
    b = by_id[rt["to"]]["center"]
    ax.plot([a["x"], b["x"]], [a["z"], b["z"]], "w--", linewidth=1)
fig.tight_layout()
out = Path("artifacts/regions_preview.png")
fig.savefig(out)
plt.close(fig)
print("preview salvo:", out)
