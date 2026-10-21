from pathlib import Path

root = Path("artifacts/datapack_test/threeeras_prototipo")
replace_files = []
nautilus = []
for p in root.rglob("*.json"):
    t = p.read_text(encoding="utf-8")
    if '"replace": true' in t or '"replace":true' in t:
        replace_files.append(str(p))
    if "zombie_nautilus" in t:
        nautilus.append(str(p))
print("replace:true files:", len(replace_files))
for f in replace_files:
    print("  ", f)
print("zombie_nautilus files:", len(nautilus))
for f in nautilus:
    print("  ", f)
# write pack.mcmeta exactly
pack = '{"pack":{"pack_format":107,"description":"Three Eras Prototype World Generation"}}'
(root / "pack.mcmeta").write_text(pack, encoding="utf-8")
print("pack.mcmeta written")