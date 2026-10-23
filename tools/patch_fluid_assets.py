# -*- coding: utf-8 -*-
"""Добавляет ассеты блоков жидкостей: две недостающие текстуры и
blockstate/модели для всех 22 LiquidBlock.

Причина: клиент писал 352 предупреждения
`missing model for variant: 'terrafabrica:<fluid>#level=N'` и рисовал
фиолетовую заглушку вместо жидкости в мире.
"""
from __future__ import annotations

import io
import sys

ROOT = "."


def patch(rel: str, old: str, new: str) -> None:
    path = rel
    text = io.open(path, encoding="utf-8").read()
    if old not in text:
        sys.exit("не найдено в %s: %r" % (rel, old[:80]))
    io.open(path, "w", encoding="utf-8", newline="").write(text.replace(old, new, 1))
    print("patched", rel)


# --- 1) текстуры: два флюида без текстур ------------------------------------
patch(
    r"tools\generate_textures.py",
    '        "diesel": (200, 180, 120), "molten_glass": (230, 150, 60), "molten_metal": (240, 120, 40),\n    }',
    '        "diesel": (200, 180, 120), "molten_glass": (230, 150, 60), "molten_metal": (240, 120, 40),\n'
    '        # id обязаны совпадать с TFFluids.SPECS, иначе блок жидкости останется без текстуры\n'
    '        "sulfur_dioxide": (210, 214, 200), "coal_gas": (92, 98, 104),\n    }',
)

# --- 2) данные: blockstate + модель для блоков жидкостей --------------------
patch(
    r"tools\generate_data.py",
    "MINERALS = ERA1 + ERA2\n",
    'MINERALS = ERA1 + ERA2\n'
    "\n"
    "# Флюиды (порядок и id — как в TFFluids.SPECS). Блоки жидкостей регистрируются\n"
    "# в TFFluids, но blockstate/модель обязаны существовать: иначе клиент пишет\n"
    '# "missing model for variant" и в мире видна фиолетовая заглушка.\n'
    'FLUID_IDS = ["steam_low", "steam_high", "sulfuric_acid", "sulfuric_acid_dilute",\n'
    '             "nitric_acid", "hydrochloric_acid", "ammonia", "chlorine", "hydrogen",\n'
    '             "oxygen", "nitrogen", "carbon_dioxide", "natural_gas", "crude_oil",\n'
    '             "fuel_oil", "gasoline", "kerosene", "diesel", "molten_glass",\n'
    '             "molten_metal", "sulfur_dioxide", "coal_gas"]\n',
)

patch(
    r"tools\generate_data.py",
    '    print(f"[data] blockstates/models: {len(cube_blocks)} кубов + {len(MACHINES)} машин")',
    "    # Блоки жидкостей: один вариант на все уровни (как minecraft:blockstates/water.json).\n"
    "    # Модель нужна только ради частиц — поверхность жидкости рисует FluidRenderer\n"
    "    # по текстурaм, которые регистрирует TFClientEvents.\n"
    "    for fid in FLUID_IDS:\n"
    "        wj(os.path.join(bs_dir, fid + '.json'),\n"
    "           {'variants': {'': {'model': f'{MODID}:block/{fid}'}}})\n"
    "        wj(os.path.join(models_dir, fid + '.json'),\n"
    "           {'textures': {'particle': f'{MODID}:block/fluid/{fid}_still'}})\n"
    '    print(f"[data] blockstates/models: {len(cube_blocks)} кубов + {len(MACHINES)} машин "\n'
    '          f"+ {len(FLUID_IDS)} жидкостей")',
)
