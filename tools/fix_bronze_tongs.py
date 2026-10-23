# -*- coding: utf-8 -*-
"""bronze_tongs: предмет был в ассетах/тегах/локализации, но не в Java.

Выделенный сервер писал:
    Couldn't load tag terrafabrica:tools ... missing references:
        terrafabrica:bronze_tongs (from mod/terrafabrica)

tongs входили в TOOLS (модели, текстуры, тег, локализация) и в паттерны
рецептов, но регистрация в TFItems отсутствовала: остальные инструменты
добавлялись по списку, а клещи — нет. Добавляем регистрацию, рецепт и
обратную проверку в аудите, чтобы «ассет без кода» больше не проходил.
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


# 1. Рецепт клещей (у остальных инструментов рецепты есть).
patch("tools/generate_data.py", [
    ('''    wr("crafting", "bronze_shears", shaped(''',
     '''    wr("crafting", "bronze_tongs", shaped(
        ["B B", "B B", " S "],
        {"B": {"item": f"{MODID}:bronze_ingot"}, "S": {"item": "minecraft:stick"}},
        "bronze_tongs", research="bronze_metallurgy"))
    wr("crafting", "bronze_shears", shaped('''),
])

# 2. Аудит: модели/текстуры предметов без регистрации в Java.
AUDIT_TAIL = "\n\ndef items_without_registration(problems):\n" \
    '    """Обратная проверка: ассет есть, а Java-регистрации нет (как было с bronze_tongs)."""\n' \
    '    tfitems = read(os.path.join(SRC, "content", "TFItems.java"))\n' \
    '    tfblocks = read(os.path.join(SRC, "content", "TFBlocks.java"))\n' \
    '    tffluids = read(os.path.join(SRC, "content", "TFFluids.java"))\n' \
    '    registered = set(re.findall(r\'item\\("([a-z0-9_]+)"\', tfitems))\n' \
    '    registered |= set(re.findall(r\'ITEMS\\.register\\("([a-z0-9_]+)"\', tfitems))\n' \
    '    # блоки (у каждого BlockItem), машины, обшивки, жидкости\n' \
    '    registered |= set(java_block_ids())\n' \
    '    registered |= set(re.findall(r\'new Spec\\("([a-z0-9_]+)"\', tffluids))\n' \
    '    # динамические семейства: <минерал>_{rock,powder,concentrate,...}\n' \
    '    for mineral in set(re.findall(r\'"([a-z0-9_]+)"\', tfblocks)):\n' \
    '        for suffix in ("rock", "powder", "concentrate", "ingot", "dust", "plate"):\n' \
    '            registered.add(f"{mineral}_{suffix}")\n' \
    '    models_dir = os.path.join(ASSETS, "models", "item")\n' \
    '    if not os.path.isdir(models_dir):\n' \
    '        return\n' \
    '    for name in sorted(os.listdir(models_dir)):\n' \
    '        if not name.endswith(".json"):\n' \
    '            continue\n' \
    '        item = name[:-5]\n' \
    '        if item not in registered:\n' \
    '            problems.append(f"models/item/{item}.json: предмет не зарегистрирован в Java")\n'

patch("tools/audit_resources.py", [
    ("\ndef main():", AUDIT_TAIL + "\n\ndef main():"),
    ('    print(f"блоков: ', '    items_without_registration(problems)\n\n    print(f"блоков: '),
])
