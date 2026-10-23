#!/usr/bin/env python3
"""
Terra Fabrica — аудит согласованности кода и ресурсов.

Проверяет, что для каждого id, зарегистрированного в Java-коде, существуют:
  * blockstates/<id>.json            (для блоков)
  * models/block/<id>.json           (для блоков)
  * models/item/<id>.json            (для предметов)
  * текстуры, на которые ссылаются модели
  * ключи локализации block./item./fluid. + перевод в ru_ru и en_us
  * loot_table/blocks/<id>.json      (для блоков с дропом)
  * worldgen/placed_feature/<id>.json (для пород)

Запуск: python tools/audit_resources.py
Код возврата 0 — расхождений нет, 1 — есть.
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "java", "com", "terrafabrica")
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "terrafabrica")
DATA = os.path.join(RES, "data", "terrafabrica")

RICHNESS = ["poor", "normal", "rich"]


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


def uniq(seq):
    return sorted(set(seq))


def java_block_ids():
    """Все id блоков: породы (минерал × богатство × основа), машины, обшивки, стёкла."""
    tfblocks = read(os.path.join(SRC, "content", "TFBlocks.java"))
    ids = []

    def list_block(name):
        m = re.search(name + r"\s*=\s*List\.of\((.*?)\);", tfblocks, re.S)
        if not m:
            return []
        return re.findall(r'"([a-z0-9_]+)"', m.group(1))

    minerals = list_block("ERA1_MINERALS") + list_block("ERA2_MINERALS")
    for mineral in minerals:
        for r in RICHNESS:
            ids.append(f"{mineral}_porode_{r}")
            ids.append(f"deepslate_{mineral}_porode_{r}")

    ids += re.findall(r'machine\("([a-z0-9_]+)"\)', tfblocks)
    ids += re.findall(r'registerCasing\("([a-z0-9_]+)"', tfblocks)
    ids += re.findall(r'BLOCKS\.register\("([a-z0-9_]+)"', tfblocks)
    return uniq(ids), uniq(minerals)


def java_item_ids():
    items = read(os.path.join(SRC, "content", "TFItems.java"))
    ids = re.findall(r'item\("([a-z0-9_]+)"\)', items)
    ids += re.findall(r'ITEMS\.register\("([a-z0-9_]+)"', items)
    return uniq(ids)


def java_fluid_ids():
    """Жидкости: id из SPECS (TFFluids) — по одному на жидкость."""
    fluids = read(os.path.join(SRC, "content", "TFFluids.java"))
    specs = re.findall(r'Spec\.of\("([a-z0-9_]+)"', fluids)
    if not specs:
        specs = re.findall(r'new Spec\("([a-z0-9_]+)"', fluids)
    return uniq(specs)


def load_json(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def textures_in_model(model, missing):
    """Рекурсивно собирает ссылки на текстуры из модели."""
    tex = model.get("textures", {})
    for value in tex.values():
        if isinstance(value, str) and not value.startswith("#"):
            path = os.path.join(RES, "assets", "terrafabrica", "textures", value.split(":", 1)[-1] + ".png")
            if ":" in value and not value.startswith("terrafabrica:"):
                continue
            if not os.path.exists(path):
                missing.add(value)
    for child in model.get("elements", []):
        pass  # элементы задают только #-ссылки, они уже проверены через textures



def items_without_registration(problems):
    """Обратная проверка: ассет есть, а Java-регистрации нет (как было с bronze_tongs)."""
    tfitems = read(os.path.join(SRC, "content", "TFItems.java"))
    tfblocks = read(os.path.join(SRC, "content", "TFBlocks.java"))
    tffluids = read(os.path.join(SRC, "content", "TFFluids.java"))
    registered = set(re.findall(r'item\("([a-z0-9_]+)"', tfitems))
    registered |= set(re.findall(r'ITEMS\.register\("([a-z0-9_]+)"', tfitems))
    # блоки (у каждого BlockItem), машины, обшивки, жидкости
    registered |= set(java_block_ids()[0])
    registered |= set(re.findall(r'new Spec\("([a-z0-9_]+)"', tffluids))
    # динамические семейства: <минерал>_{rock,powder,concentrate,...}
    for mineral in set(re.findall(r'"([a-z0-9_]+)"', tfblocks)):
        for suffix in ("rock", "powder", "concentrate", "ingot", "dust", "plate"):
            registered.add(f"{mineral}_{suffix}")
    models_dir = os.path.join(ASSETS, "models", "item")
    if not os.path.isdir(models_dir):
        return
    for name in sorted(os.listdir(models_dir)):
        if not name.endswith(".json"):
            continue
        item = name[:-5]
        if item not in registered:
            problems.append(f"models/item/{item}.json: предмет не зарегистрирован в Java")


def textures_referenced_in_java(problems):
    """
    Каждый путь вида "textures/...png" из Java-кода обязан существовать в ассетах.

    Проверка появилась после того, как экраны руководства и исследований получили свои
    панели: опечатка в ResourceLocation даёт не ошибку компиляции, а тихо отсутствующий
    фон (текстура грузится только при первом показе экрана).
    """
    pattern = re.compile(r'"(textures/[a-z0-9_/]+\.png)"')
    seen = set()
    for dirpath, _, files in os.walk(SRC):
        for name in files:
            if not name.endswith(".java"):
                continue
            text = read(os.path.join(dirpath, name))
            for rel in pattern.findall(text):
                seen.add(rel)
                path = os.path.join(ASSETS, rel.replace("/", os.sep))
                if not os.path.isfile(path):
                    problems.append(f"{name}: в ассетах нет assets/terrafabrica/{rel}")
    return seen


def main():
    problems = []
    block_ids, minerals = java_block_ids()
    item_ids = java_item_ids()
    fluid_ids = java_fluid_ids()

    bs_dir = os.path.join(ASSETS, "blockstates")
    bm_dir = os.path.join(ASSETS, "models", "block")
    im_dir = os.path.join(ASSETS, "models", "item")
    loot_dir = os.path.join(DATA, "loot_table", "blocks")
    pl_dir = os.path.join(DATA, "worldgen", "placed_feature")

    have_bs = {f[:-5] for f in os.listdir(bs_dir)} if os.path.isdir(bs_dir) else set()
    have_bm = {f[:-5] for f in os.listdir(bm_dir)} if os.path.isdir(bm_dir) else set()
    have_im = {f[:-5] for f in os.listdir(im_dir)} if os.path.isdir(im_dir) else set()
    have_loot = {f[:-5] for f in os.listdir(loot_dir)} if os.path.isdir(loot_dir) else set()
    have_pl = {f[:-5] for f in os.listdir(pl_dir)} if os.path.isdir(pl_dir) else set()

    missing_bs = [i for i in block_ids if i not in have_bs]
    missing_bm = [i for i in block_ids if i not in have_bm]
    if missing_bs:
        problems.append(f"blockstates: нет {len(missing_bs)} → {missing_bs[:10]}")
    if missing_bm:
        problems.append(f"models/block: нет {len(missing_bm)} → {missing_bm[:10]}")

    # предметы: у каждого id должен быть models/item + (для блоков — их BlockItem)
    expected_items = set(item_ids) | set(block_ids)
    missing_im = [i for i in expected_items if i not in have_im]
    if missing_im:
        problems.append(f"models/item: нет {len(missing_im)} → {missing_im[:10]}")

    # лут: породы обязаны иметь loot table
    porodes = [i for i in block_ids if "_porode_" in i]
    missing_loot = [i for i in porodes if i not in have_loot]
    if missing_loot:
        problems.append(f"loot_table: нет {len(missing_loot)} → {missing_loot[:10]}")

    # worldgen: каждая порода — собственный placed feature
    missing_pl = [i for i in porodes if i not in have_pl]
    if missing_pl:
        problems.append(f"placed_feature: нет {len(missing_pl)} → {missing_pl[:10]}")

    # lang
    for lang in ("ru_ru", "en_us"):
        path = os.path.join(ASSETS, "lang", lang + ".json")
        if not os.path.exists(path):
            problems.append(f"lang: нет {lang}.json")
            continue
        table = load_json(path)
        keys = [f"block.terrafabrica.{i}" for i in block_ids]
        keys += [f"item.terrafabrica.{i}" for i in item_ids]
        keys += [f"fluid_type.terrafabrica.{i}" for i in fluid_ids]
        absent = [k for k in keys if k not in table]
        if absent:
            problems.append(f"lang/{lang}: нет {len(absent)} ключей → {absent[:8]}")

    # текстуры, на которые ссылаются модели
    missing_tex = set()
    for name in have_bm:
        textures_in_model(load_json(os.path.join(bm_dir, name + ".json")), missing_tex)
    for name in have_im:
        textures_in_model(load_json(os.path.join(im_dir, name + ".json")), missing_tex)
    if missing_tex:
        problems.append(f"текстуры: нет {len(missing_tex)} → {sorted(missing_tex)[:10]}")

    items_without_registration(problems)
    java_textures = textures_referenced_in_java(problems)

    print(f"блоков: {len(block_ids)} (пород {len(porodes)}), предметов: {len(item_ids)}, "
          f"жидкостей: {len(fluid_ids)}, текстур из кода: {len(java_textures)}")
    if problems:
        print("\nРАСХОЖДЕНИЯ:")
        for p in problems:
            print("  •", p)
        return 1
    print("расхождений нет")
    return 0


if __name__ == "__main__":
    sys.exit(main())
