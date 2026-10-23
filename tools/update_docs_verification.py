# -*- coding: utf-8 -*-
"""Обновляет docs/testing.md и docs/decisions.md по итогам проверки в мире.

Что документально закрепляем (всё найдено запуском, а не чтением кода):
  * biome_modifier обязан лежать в data/<modid>/neoforge/biome_modifier/;
  * fluid_in/fluid_out — это FluidStack {"id", "amount"}, а не {"fluid", ...};
  * bronze_tongs был в ассетах/тегах, но не в Java;
  * подтверждённое распределение богатства 30.1 / 59.9 / 10.0 % на живом мире;
  * GameTest-мир плоский, worldgen проверяется на выделенном сервере.
"""
from __future__ import annotations

import io
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def patch(rel, pairs):
    path = os.path.join(ROOT, rel)
    text = io.open(path, encoding="utf-8").read()
    for old, new in pairs:
        assert old in text, (rel, old[:70])
        text = text.replace(old, new, 1)
    io.open(path, "w", encoding="utf-8", newline="").write(text)
    print("patched", rel)


TESTING_OLD_TAIL = """| `researchGatingWorks` | узел закрыт по умолчанию, открывается прогрессом, без игрока гейтинг не блокирует |

Ожидаемый вывод в логе: `All 6 required tests passed :)`."""

TESTING_NEW_TAIL = """| `researchGatingWorks` | узел закрыт по умолчанию, открывается прогрессом, без игрока гейтинг не блокирует |
| `worldgenPlacesPorodes` | форсированная загрузка 3×3 чанков и подсчёт пород в секциях |

Ожидаемый вывод в логе: `All 7 required tests passed :)`.

**Мир GameTest плоский (суперплоскость без камня), поэтому worldgen там проверить нельзя.**
`worldgenPlacesPorodes` честно измеряет это: если в загруженных чанках нет ни камня, ни
сланца, тест пишет `[gametest] в загруженных чанках нет камня … worldgen проверяется на
runServer` и завершается успешно. Реальная проверка пород — только на выделенном сервере
(следующий раздел)."""

patch("docs/testing.md", [
    ("## Чего в тестах нет", """## Проверка worldgen на выделенном сервере

GameTest этого не покрывает, поэтому породы проверяются на настоящем мире с настоящей
генерацией. Один раз настраиваем `run/` (EULA, RCON) и дальше повторяем:

```powershell
# 1. поднять сервер (в фоне), ждать "Done (...)! For help"
gradle --offline runServer

# 2. прогрузить площадь: 4 × forceload по 256 чанков
python tools\\rcon.py 25577 terrafabrica "forceload add -256 -256 -1 -1"
python tools\\rcon.py 25577 terrafabrica "save-all flush"

# 3. посчитать реальные блоки (NBT + распаковка палитр, не просто поиск строк)
python tools\\count_blocks.py "run/worldgen_check"

# 4. погасить сервер
python tools\\rcon.py 25577 terrafabrica "stop"
```

`tools/count_blocks.py` печатает распределение богатства — оно должно совпадать с
требованием 30/60/10. Замер на 1283 чанках (финальная сборка):

```
чанков: 1283, блоков посчитано: 126124032
пород: 2809140
  poor  :  844613 ( 30.1 %)
  normal: 1682299 ( 59.9 %)
  rich  :  282228 ( 10.0 %)
минералов: 27
```

`tools/scan_world.py` — быстрый вариант (ищет id в палитрах секций, без количеств):
годится, чтобы за секунды ответить «породы вообще есть или нет».

## Чего в тестах нет"""),
    (TESTING_OLD_TAIL, TESTING_NEW_TAIL),
])

DECISIONS_OLD = """## Совместимость с ванилью"""

DECISIONS_NEW = """## Найдено запуском в мире (а не чтением кода)

**`biome_modifier` обязан лежать в `data/<modid>/neoforge/biome_modifier/`.**
`neoforge:biome_modifier` — динамический реестр, и префикс пути задаёт пространство имён
*записи*, а не реестра. Файл в `data/neoforge/biome_modifier/terrafabrica_ores.json`
загружается без единой ошибки и просто не применяется: на сервере с 2704 чанками было
0 пород. После переноса в `data/terrafabrica/neoforge/biome_modifier/` — 27 минералов и
2.8 млн блоков пород. Проверять worldgen «на глаз» по логам бесполезно: ошибок нет.

**`fluid_in` / `fluid_out` — это `FluidStack`, а не `FluidIngredient`.**
`MachineRecipe` использует `FluidStack.CODEC`, поэтому поля называются
`{"id": ..., "amount": ...}`. С ключом `fluid` сервер писал
`No key id in MapLike[...]` и молча терял все химические процессы (12 рецептов).
Теперь это ловит и `validate_data.py`.

**Ассет без регистрации — тоже ошибка.** `bronze_tongs` был в `TOOLS` (модели, текстуры,
тег `terrafabrica:tools`, локализация), но не в `TFItems`: сервер ругался
`Couldn't load tag terrafabrica:tools … missing references`. `audit_resources.py` теперь
проверяет и обратное направление — «есть `models/item`, нет регистрации в Java».

**Мир GameTest плоский.** В нём нет ни камня, ни сланца, ни ванильных руд, поэтому
worldgen проверяется только на `runServer` + `tools/count_blocks.py`.

## Совместимость с ванилью"""

patch("docs/decisions.md", [(DECISIONS_OLD, DECISIONS_NEW)])
