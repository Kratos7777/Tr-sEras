# importado/ — MERGIDO em 11/10/2026

O conteúdo desta pasta foi analisado e integrado a `ThreeEras/src`
(branch `organizacao/documentos-e-importado`). Nada foi descartado às
cegas: cada arquivo foi comparado antes do merge.

Pontos que a versão supervisionada trazia e agora estão no projeto:
- `StorageManager`: dados.yml inválido preservado (não sobrescrito), lock
  de E/S, flag `encerrando`, `isSaving()`
- `PlayerData`: flag `tutorialConcluido` + `reiniciarTutorial()`
- `PlayerManager`: tutorial no reset sem reentregar kit, missão inicial
  configurável, posicionamento no spawn da era, `clearCache()`
- `Era`/`EraManager`: `requisito-missao` por era
- `QuestProgressListener`: verificação no tick seguinte (correção real de
  entrega) + pickup de item
- `QuestManager.objetivoAtual()` + comandos `threeeras diario` e
  `threeeras tutorial reiniciar`
- Recarregamento de dados bloqueado com jogadores online ou gravação pendente

Esta pasta pode ser removida após conferência.
