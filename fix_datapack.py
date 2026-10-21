from pathlib import Path

dst = Path("artifacts/datapack_test/threeeras_prototipo")
(dst / "pack.mcmeta").write_text(
    '{"pack":{"description":"Three Eras Prototype World Generation","min_format":[1,0],"max_format":[81,0]}}',
    encoding="utf-8",
)
root = dst / "data" / "threeeras" / "function"
n = 0
for p in root.rglob("*.mcfunction"):
    t = p.read_text(encoding="utf-8")
    if "over motion_blocking_no_leaves" in t:
        p.write_text(t.replace(" over motion_blocking_no_leaves", ""), encoding="utf-8")
        n += 1
print("mcfunction corrigidos:", n)
print("pack.mcmeta:", (dst / "pack.mcmeta").read_text())
