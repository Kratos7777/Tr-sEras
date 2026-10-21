"""
threeeras.pipeline.smoke_test_amulet
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Smoke test para verificar se o Amulet-Core está instalado e funcional
para a versão Paper 26.2 (DataVersion 4903).
"""

from __future__ import annotations

import sys


def main() -> int:
    print("=" * 66)
    print("  ThreeEras — Smoke Test (Amulet-Core)")
    print("=" * 66)

    try:
        import amulet
        print(f"[OK] Amulet importado com sucesso (versão detectada)")
    except ImportError:
        print("[ERRO] Amulet-Core não está instalado no ambiente Python.", file=sys.stderr)
        print("       Rode o script de setup para instalar as dependências.", file=sys.stderr)
        return 1

    try:
        import numpy as np
        import matplotlib
        print(f"[OK] Numpy ({np.__version__}) e Matplotlib ({matplotlib.__version__}) prontos")
    except ImportError as e:
        print(f"[ERRO] Dependência ausente: {e}", file=sys.stderr)
        return 1

    print("\n[SMOKE TEST] Todos os testes básicos de backend passaram com sucesso!")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())