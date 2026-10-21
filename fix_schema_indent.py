"""Script para corrigir a indentacao do schema.py"""
from pathlib import Path

content = Path("pipeline/schema.py").read_text(encoding="utf-8")
lines = content.split("\n")
fixed_lines = []
for line in lines:
    stripped = line.lstrip()
    if stripped:
        leading = len(line) - len(stripped)
        new_leading = (leading // 4) * 4
        fixed_lines.append(" " * new_leading + stripped)
    else:
        fixed_lines.append("")

Path("pipeline/schema.py").write_text("\n".join(fixed_lines), encoding="utf-8")
print("OK - schema.py indentacao corrigida")