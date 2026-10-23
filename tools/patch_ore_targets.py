# -*- coding: utf-8 -*-
"""Оре-фичи: заменять не только чистый камень/сланец, а теги replaceable.

Ванильные руды используют `tag_match` с `minecraft:stone_ore_replaceables`
(камень, гранит, диорит, андезит, туф) и `deepslate_ore_replaceables`.
С `block_match: minecraft:stone` жила в гранитном массиве просто не появлялась.
"""
from __future__ import annotations

import io
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATH = os.path.join(ROOT, "tools", "generate_data.py")

text = io.open(PATH, encoding="utf-8").read()

old = """                "targets": [{
                    "target": {"predicate_type": "minecraft:block_match", "block": replace},
                    "state": {"Name": f"{MODID}:{block_id}"},
                }],"""

new = """                "targets": [{
                    "target": replace_predicate(replace),
                    "state": {"Name": f"{MODID}:{block_id}"},
                }],"""

if old not in text:
    sys.exit("не найден блок targets в write_ore")

text = text.replace(old, new, 1)

helper = '''def gen_worldgen():
'''
helper_new = '''def replace_predicate(block: str) -> dict:
    """Предикат цели жилы: теги replaceable, как у ванильных руд.

    Тег шире одного блока: жила садится и в гранит/диорит/андезит/туф (камень)
    или в глубинный сланец, а не пропадает в «неправильной» породе.
    """
    if block == "minecraft:stone":
        return {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}
    if block == "minecraft:deepslate":
        return {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}
    return {"predicate_type": "minecraft:block_match", "block": block}


def gen_worldgen():
'''

if helper not in text:
    sys.exit("не найдена сигнатура gen_worldgen")
text = text.replace(helper, helper_new, 1)

io.open(PATH, "w", encoding="utf-8", newline="").write(text)
print("patched generate_data.py: tag_match для целей жил")
