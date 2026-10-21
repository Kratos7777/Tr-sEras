"""Render de voxels solidos da celula 3,9."""

from __future__ import annotations

import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from pathlib import Path

npz = Path("artifacts/consolidated/cell_3_9.npz")
if not npz.exists():
    npz = Path("artifacts/blockmap/cell_3_9.npz")
z = np.load(npz)
blocks = z["blocks"]
palette = [str(p) for p in z["palette"]]

# downsample pesado
step = 8
b = (blocks[::step, ::step, ::step] != 0).astype(bool)
# cores: verde para tudo nao-ar
facecolors = np.where(b, "green", "none")

fig = plt.figure(figsize=(10, 10), dpi=100)
ax = fig.add_subplot(111, projection="3d")
ax.voxels(b, facecolors=facecolors, edgecolor="k", linewidth=0.1)
ax.set_xlabel("X")
ax.set_ylabel("Y")
ax.set_zlabel("Z")
ax.view_init(elev=30, azim=45)
fig.tight_layout()
out = Path("artifacts/cell_3_9_solid.png")
fig.savefig(out)
plt.close(fig)
print(f"saved {out}")
