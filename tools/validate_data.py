#!/usr/bin/env python3
"""
Terra Fabrica — валидация датапака (рецепты, лут, worldgen, теги).

Проверяет ссылочную целостность, которую Minecraft обнаружит только в рантайме:
  * тип рецепта зарегистрирован в коде;
  * все terrafabrica:-идентификаторы предметов/блоков существуют;
  * требуемое исследование есть в дереве исследовании;
  * ссылки на теги мода существуют как файлы;
  * рецепт содержит обязательные поля для своего типа (иначе Codec упадёт при загрузке);
  * лут-таблицы ссылаются на существующие предметы;
  * placed_feature/biome_modifier ссылаются на существующие блоки и фичи,
    а диапазоны высот не вывернуты.

Запуск: python tools/validate_data.py
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "java", "com", "terrafabrica")
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data", "terrafabrica")
ASSETS = os.path.join(RES, "assets", "terrafabrica")
MODID = "terrafabrica"

# Рецепты, которые намеренно не закрыты исследованием:
#  * книга-руководство — точка входа в мод, иначе игрок не начнёт;
#  * плавка песка в обычное стекло — дублирует ванильную печь, гейтить строже
#    ванили смысла нет (решение зафиксировано в docs/decisions.md).
UNGATED = {
    "recipe/crafting/guide_book.json",
    "recipe/primitive_furnace/smelt_sand_glass.json",
}


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


def walk_json(root):
    for dirpath, _, files in os.walk(root):
        for name in sorted(files):
            if name.endswith(".json"):
                yield os.path.join(dirpath, name)


def known_recipe_types():
    """Типы рецептов, для которых в коде есть сериализатор."""
    text = read(os.path.join(SRC, "content", "TFRecipeSerializers.java"))
    ids = set(re.findall(r'machine\("([a-z0-9_]+)"\)', text))
    ids |= set(re.findall(r'RECIPE_SERIALIZERS\.register\("([a-z0-9_]+)"', text))
    return {f"{MODID}:{i}" for i in ids} | {
        "minecraft:crafting_shaped", "minecraft:crafting_shapeless",
        "minecraft:smelting", "minecraft:blasting", "minecraft:campfire_cooking",
        "minecraft:stonecutting", "minecraft:smoking",
    }


def known_fluids():
    """Жидкости: id из TFFluids.SPECS."""
    text = read(os.path.join(SRC, "content", "TFFluids.java"))
    return set(re.findall(r'new Spec\("([a-z0-9_]+)"', text))


def known_research():
    text = read(os.path.join(SRC, "core", "research", "ResearchTree.java"))
    return set(re.findall(r'node\("([a-z0-9_]+)"', text))


def known_items():
    """Все предметы мода = модели предметов + блоки (у каждого блока есть BlockItem)."""
    items = set()
    for dirpath, _, files in os.walk(os.path.join(ASSETS, "models", "item")):
        for name in files:
            if name.endswith(".json"):
                items.add(name[:-5])
    return items


def known_tags():
    tags = set()
    for dirpath, _, files in os.walk(os.path.join(RES, "data")):
        for name in files:
            if not name.endswith(".json"):
                continue
            path = os.path.join(dirpath, name)
            rel = os.path.relpath(path, os.path.join(RES, "data")).replace("\\", "/")
            namespace, _, rest = rel.partition("/")
            if not rest.startswith("tags/"):
                continue
            tag_path = rest[len("tags/"):-len(".json")]
            tags.add(f"{namespace}:{tag_path}")
    return tags


def check_ingredient(value, items, tags, problems, where):
    if isinstance(value, dict):
        if "item" in value:
            check_id(value["item"], items, problems, where)
        elif "tag" in value:
            ref = value["tag"]
            if ref.startswith(MODID + ":") and ref not in tags:
                problems.append(f"{where}: нет тега {ref}")
        elif "items" in value:
            for one in value["items"]:
                check_id(one, items, problems, where)
    elif isinstance(value, str):
        if value.startswith("#"):
            ref = value[1:]
            if ref.startswith(MODID + ":") and ref not in tags:
                problems.append(f"{where}: нет тега {ref}")
        else:
            check_id(value, items, problems, where)


def check_id(ref, items, problems, where):
    if not isinstance(ref, str):
        return
    if ref.startswith(MODID + ":"):
        if ref.split(":", 1)[1] not in items:
            problems.append(f"{where}: нет предмета {ref}")


def main():
    problems = []
    items = known_items()
    tags = known_tags()
    types = known_recipe_types()
    research = known_research()
    fluids = known_fluids()

    configured = {os.path.basename(p)[:-5]
                  for p in walk_json(os.path.join(DATA, "worldgen", "configured_feature"))}
    counts = {"recipe": 0, "loot": 0, "placed_feature": 0, "configured_feature": 0}
    for path in walk_json(DATA):
        rel = os.path.relpath(path, DATA).replace("\\", "/")
        data = json.load(open(path, encoding="utf-8"))
        where = rel

        if rel.startswith("recipe/"):
            counts["recipe"] += 1
            rtype = data.get("type", "")
            if rtype not in types:
                problems.append(f"{where}: неизвестный тип рецепта {rtype!r}")
                continue
            node = data.get("research")
            if node and node not in research:
                problems.append(f"{where}: нет исследования {node!r}")
            if not node and rel not in UNGATED:
                problems.append(f"{where}: нет research — рецепт не закрыт исследованием")
            if rtype.endswith("crafting_shaped") or rtype.endswith("research_shaped"):
                if "pattern" not in data or "key" not in data:
                    problems.append(f"{where}: нет pattern/key")
                for value in data.get("key", {}).values():
                    check_ingredient(value, items, tags, problems, where)
                check_id(data.get("result", {}).get("id"), items, problems, where)
            elif rtype.endswith("crafting_shapeless") or rtype.endswith("research_shapeless"):
                if "ingredients" not in data:
                    problems.append(f"{where}: нет ingredients")
                for value in data.get("ingredients", []):
                    check_ingredient(value, items, tags, problems, where)
                check_id(data.get("result", {}).get("id"), items, problems, where)
            elif rtype in ("minecraft:smelting", "minecraft:blasting", "minecraft:smoking",
                           "minecraft:campfire_cooking"):
                for key in ("ingredient", "result", "cookingtime"):
                    if key not in data:
                        problems.append(f"{where}: нет {key}")
                check_ingredient(data.get("ingredient", {}), items, tags, problems, where)
                result = data.get("result")
                if isinstance(result, dict):
                    check_id(result.get("id"), items, problems, where)
                else:
                    check_id(result, items, problems, where)
            else:
                # машинный рецепт: единый формат MachineRecipe
                for key in ("ingredients", "results", "energy", "ticks"):
                    if key not in data:
                        problems.append(f"{where}: нет {key}")
                for value in data.get("ingredients", []):
                    check_ingredient(value, items, tags, problems, where)
                for stack in data.get("results", []):
                    check_id(stack.get("id"), items, problems, where)
                if "counts" in data and len(data["counts"]) != len(data.get("ingredients", [])):
                    problems.append(f"{where}: counts не совпадает с ingredients")
                if data.get("energy", 0) < 0 or data.get("ticks", 0) <= 0:
                    problems.append(f"{where}: странные energy/ticks")
                # FluidStack.CODEC: ключ обязан быть "id", а не "fluid"
                for field in ("fluid_in", "fluid_out"):
                    fluid = data.get(field)
                    if fluid is None:
                        continue
                    if "id" not in fluid:
                        problems.append(f"{where}: {field} должен быть {{id, amount}}")
                    if fluid.get("amount", 0) <= 0:
                        problems.append(f"{where}: {field}.amount должно быть > 0")
                    fid = fluid.get("id", "")
                    if isinstance(fid, str) and fid.startswith(MODID + ":"):
                        if fid.split(":", 1)[1] not in fluids:
                            problems.append(f"{where}: нет жидкости {fid}")

        elif rel.startswith("loot_table/"):
            counts["loot"] += 1
            for pool in data.get("pools", []):
                for entry in walk_entries(pool.get("entries", [])):
                    name = entry.get("name")
                    if name:
                        check_id(name, items, problems, where)

        elif rel.startswith("worldgen/placed_feature/"):
            counts["placed_feature"] += 1
            # 1.21.1: feature — строка-ссылка на configured_feature, не объект.
            ref = data.get("feature")
            if isinstance(ref, str):
                if ref.startswith(MODID + ":") and ref.split(":", 1)[1] not in configured:
                    problems.append(f"{where}: нет configured_feature {ref}")
            else:
                problems.append(f"{where}: feature должен быть ссылкой (строкой)")
            for mod in data.get("placement", []):
                if mod.get("type") == "minecraft:count" and mod.get("count", 1) < 1:
                    problems.append(f"{where}: count < 1")
                if mod.get("type") == "minecraft:height_range":
                    height = mod.get("height", {})
                    lo = height.get("min_inclusive", {}).get("absolute", 0)
                    hi = height.get("max_inclusive", {}).get("absolute", 0)
                    if lo > hi:
                        problems.append(f"{where}: min_y {lo} > max_y {hi}")
                # rarity_filter в 1.21.1 — целое 1/N, поэтому проверяем >= 1.
                if mod.get("type") == "minecraft:rarity_filter" and mod.get("chance", 1) < 1:
                    problems.append(f"{where}: chance должен быть целым >= 1 (1/N)")

        elif rel.startswith("worldgen/configured_feature/"):
            counts["configured_feature"] += 1
            for target in data.get("config", {}).get("targets", []):
                state = target.get("state", {}).get("Name")
                if state and state.startswith(MODID + ":") and state.split(":", 1)[1] not in items:
                    problems.append(f"{where}: нет блока {state}")

    # biome_modifier ссылается на placed_feature
    bm_dir = os.path.join(RES, "data", "terrafabrica", "neoforge", "biome_modifier")
    placed = {os.path.basename(p)[:-5] for p in walk_json(os.path.join(DATA, "worldgen", "placed_feature"))}
    for path in walk_json(bm_dir):
        data = json.load(open(path, encoding="utf-8"))
        for ref in data.get("features", []):
            name = ref.split(":", 1)[1]
            if name not in placed:
                problems.append(f"biome_modifier: нет placed_feature {ref}")

    print(f"рецептов: {counts['recipe']}, лут-таблиц: {counts['loot']}, "
          f"placed_feature: {counts['placed_feature']}, "
          f"configured_feature: {counts['configured_feature']}")
    print(f"предметов: {len(items)}, тегов мода: {sum(1 for t in tags if t.startswith(MODID + ':'))}, "
          f"исследований: {len(research)}, типов рецептов: {len(types)}")
    if problems:
        print(f"\nПРОБЛЕМЫ ({len(problems)}):")
        for p in problems[:40]:
            print("  •", p)
        return 1
    print("датапак ссылочно целостен")
    return 0


def walk_entries(entries):
    for entry in entries:
        yield entry
        for child in entry.get("children", []):
            yield from walk_entries([child])


if __name__ == "__main__":
    sys.exit(main())
