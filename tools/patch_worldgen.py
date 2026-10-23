# -*- coding: utf-8 -*-
"""Переписывает gen_worldgen(): разделяет configured_feature и placed_feature.

Причина: в 1.21.1 поле "feature" у placed_feature — ссылка на id
configured_feature, а не inline-объект. Инлайн-объект валит загрузку реестров
(Failed to parse terrafabrica:worldgen/placed_feature/...). См. docs/decisions.md.
"""
from __future__ import annotations

import io
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GEN = os.path.join(ROOT, "tools", "generate_data.py")

NEW = '''def gen_worldgen():
    """Породы в мире: configured_feature + placed_feature + biome_modifier.

    1.21.1: placed_feature ссылается на configured_feature по id строкой,
    поэтому на каждую породу пишутся два файла (иначе реестры не грузятся).
    """
    wg_dir = os.path.join(DATA, "worldgen", "placed_feature")
    cf_dir = os.path.join(DATA, "worldgen", "configured_feature")
    os.makedirs(wg_dir, exist_ok=True)
    os.makedirs(cf_dir, exist_ok=True)
    placed = []

    def write_ore(fid, size, replace, count, chance, lo=None, hi=None, heightmap=None):
        """Пишет пару configured/placed для жилы породы."""
        wj(os.path.join(cf_dir, fid + ".json"), {
            "type": "minecraft:ore",
            "config": {
                "discard_chance_on_air_exposure": 0.0,
                "size": size,
                "targets": [{
                    "target": {"predicate_type": "minecraft:block_match", "block": replace},
                    "state": {"Name": f"{MODID}:{fid}"},
                }],
            },
        })
        placement = [{"type": "minecraft:count", "count": count},
                     {"type": "minecraft:in_square"}]
        if heightmap:
            placement.append({"type": "minecraft:heightmap", "heightmap": heightmap})
        else:
            placement.append({"type": "minecraft:height_range",
                              "height": {"type": "minecraft:uniform",
                                         "min_inclusive": {"absolute": lo},
                                         "max_inclusive": {"absolute": hi}}})
        placement.append({"type": "minecraft:rarity_filter", "chance": chance})
        placement.append({"type": "minecraft:biome"})
        wj(os.path.join(wg_dir, fid + ".json"),
           {"feature": f"{MODID}:{fid}", "placement": placement})
        placed.append(f"{MODID}:{fid}")

    for m, (size, count, min_y, max_y) in WORLDGEN.items():
        variants = []
        if max_y > 0:
            variants.append(("", "minecraft:stone", max(0, min_y), max_y))
        # Глубинносланцевый вариант есть у каждой породы, иначе блоки
        # deepslate_<минерал>_porode_* было бы невозможно добыть в мире.
        ds_lo, ds_hi = min(min_y, 0), min(0, max_y)
        if ds_lo >= ds_hi:
            ds_lo, ds_hi = -64, -8
        variants.append(("deepslate_", "minecraft:deepslate", ds_lo, ds_hi))
        for base_prefix, replace, lo, hi in variants:
            for r, weight in WEIGHTS.items():
                write_ore(f"{base_prefix}{m}_porode_{r}", size, replace, count, weight, lo=lo, hi=hi)

    # поверхностные выходы для заметных минералов
    for m in ["copper", "tin", "coal", "hematite", "limestone"]:
        write_ore(f"outcrop_{m}", 2, "minecraft:stone", 1, 0.2,
                  heightmap="MOTION_BLOCKING")

    # biome_modifier: добавляем все фичи в верхний мир
    bm_dir = os.path.join(RES, "data", "neoforge", "biome_modifier")
    wj(os.path.join(bm_dir, "terrafabrica_ores.json"), {
        "type": "neoforge:add_features",
        "biomes": "#minecraft:is_overworld",
        "features": placed,
        "step": "underground_ores",
    })
    print(f"[data] worldgen: {len(placed)} placed features + biome_modifier")
'''


def main() -> None:
    text = io.open(GEN, encoding="utf-8").read()
    start = text.index("def gen_worldgen():")
    end_marker = 'print(f"[data] worldgen: {len(placed)} placed features + biome_modifier")\n'
    end = text.index(end_marker) + len(end_marker)
    text = text[:start] + NEW + text[end:]
    if "configured_feature" not in text:
        sys.exit("патч не применился")
    io.open(GEN, "w", encoding="utf-8", newline="").write(text)
    print("patched tools/generate_data.py")


if __name__ == "__main__":
    main()
