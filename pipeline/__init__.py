"""
threeeras.pipeline
~~~~~~~~~~~~~~~~~~
Pipeline de geração procedural do universo ThreeEras.

Estágios:
  S0  schema         — carga e validação do plano de mundo
  S1  noise/influence — heightmap
  S2  biome          — biome map
  S3  surface        — camadas de solo e carve
  S4  structures     — posicionamento de estruturas
  S5  writer         — escrita Anvil (.mca)
  S6  verify         — integridade e hash do protótipo
"""

__version__ = "0.4.0"