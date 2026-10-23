#!/usr/bin/env python3
"""
Генерация справочных разделов документации из фактических данных проекта.

Пишет:
    docs/machines.md    — таблица машин: архетип, рецепты, энергия/время, исследования
    docs/progression.md — дерево исследований по эпохам (время, предпосылки)

Числа берутся из сгенерированного датапака и ResearchTree.java, поэтому
документация не расходится с кодом. Запуск: python tools/gen_docs.py
"""
import json
import os
import re

from machine_specs import COLUMNS, NOTES, SPECS

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data", "terrafabrica")
DOCS = os.path.join(ROOT, "docs")

ERA_NAMES = {1: "Эпоха I — пар и механика", 2: "Эпоха II — сталь, ток, химия"}

# Порядок и подписи групп машин для таблицы
GROUPS = [
    ("Обогащение", ["crusher", "separator"]),
    ("Печи и металлургия", ["primitive_furnace", "bloomery", "small_blast_furnace",
                            "puddling_furnace", "crucible_furnace", "calcination_furnace",
                            "pyrite_roaster", "coke_oven", "blast_furnace", "open_hearth_furnace",
                            "electric_arc_furnace", "bessemer_converter"]),
    ("Приводы и передача", ["water_wheel", "windmill", "steam_engine_newcomen", "steam_engine_watt",
                            "shaft", "gear", "belt", "pulley", "gearbox"]),
    ("Станки", ["lathe", "drill_press", "milling_machine", "grinding_machine", "rolling_mill",
                "power_hammer", "heavy_rolling_mill", "wire_drawing_machine", "weight_dispenser"]),
    ("Энергетика", ["steam_turbine", "gas_turbine", "ccgt_plant", "electric_generator",
                    "transformer", "electric_motor", "lead_acid_battery"]),
    ("Сети", ["cable_copper", "cable_aluminum", "cable_steel", "power_pole_wood",
              "power_pole_concrete", "power_pole_steel", "insulator_porcelain", "insulator_glass",
              "substation", "circuit_breaker"]),
    ("Химия", ["lead_chamber", "absorber", "solvay_reactor", "air_separation_unit",
               "water_electrolyzer", "ammonia_synthesizer", "ostwald_reactor",
               "contact_acid_reactor", "chloralkali_cell", "aluminum_electrolyzer",
               "fertilizer_mixer", "oil_distillation_column"]),
    ("Связь и автоматика", ["telegraph", "telephone", "radio", "tube_computer", "relay", "plc",
                            "sensor_temperature", "sensor_pressure", "sensor_level", "sensor_flow",
                            "valve"]),
    ("Транспорт", ["conveyor", "rail", "sleeper"]),
]


def read_json(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def recipe_index():
    """{машина: {рецептов, энергии, тики, исследования, типы}}"""
    base = os.path.join(DATA, "recipe")
    index = {}
    for machine in sorted(os.listdir(base)):
        folder = os.path.join(base, machine)
        if not os.path.isdir(folder):
            continue
        energies, ticks, nodes = [], [], set()
        files = [f for f in sorted(os.listdir(folder)) if f.endswith(".json")]
        for name in files:
            recipe = read_json(os.path.join(folder, name))
            if recipe.get("energy"):
                energies.append(int(recipe["energy"]))
            if recipe.get("ticks"):
                ticks.append(int(recipe["ticks"]))
            if recipe.get("research"):
                nodes.add(recipe["research"])
        index[machine] = {
            "count": len(files),
            "energy": (min(energies), max(energies)) if energies else None,
            "ticks": (min(ticks), max(ticks)) if ticks else None,
            "nodes": sorted(nodes),
        }
    return index


def fmt_kj(value):
    if value is None:
        return "—"
    if value >= 1_000_000:
        return f"{value / 1_000_000:.2f} МДж"
    if value >= 1_000:
        return f"{value / 1_000:g} кДж"
    return f"{value} Дж"


def fmt_energy(pair):
    if not pair:
        return "—"
    low, high = pair
    return fmt_kj(low) if low == high else f"{fmt_kj(low)} – {fmt_kj(high)}"


def fmt_ticks(pair):
    if not pair:
        return "—"
    low, high = pair
    seconds = lambda t: f"{t / 20:g} с"
    return seconds(low) if low == high else f"{seconds(low)} – {seconds(high)}"


def gen_machines(recipes, lang):
    lines = [
        "# Машины и оборудование",
        "",
        "Файл строится автоматически (`tools/gen_docs.py`) из двух источников:",
        "",
        "1. **Рецепты** — из датапака: число рецептов, диапазон энергии и времени цикла,",
        "   необходимые исследования. Это проверяемые данными факты.",
        "2. **Параметры** — из `tools/machine_specs.py`: мощность, КПД, давление, температура,",
        "   напряжение, ток, потери, расход топлива, скорость, ёмкость буфера. Значения сняты",
        "   с констант кода и `TFServerConfig` (в тексте указано, где именно).",
        "",
        "Типы блоков:",
        "",
        "- **машина с рецептами** — есть свой тип рецептов, таблица рецептов непустая;",
        "- **источник вращения** — вырабатывает вращение (колесо, ветряк, паровые машины);",
        "- **трансмиссия** — передаёт вращение (вал, шестерня, ремень, шкив, редуктор);",
        "- **только способности** — собственных рецептов нет, блок обменивается энергией,",
        "  теплом или жидкостью через capability (`IEnergyHandler`, `IVoltageSource` и т. п.);",
        "- **конструкция** — блок без логики: опоры, изоляторы, шпала;",
        "- **путь** — рельс, по которому движутся сущности.",
        "",
        "«—» в таблице параметров значит «не применимо к этому блоку», а не «неизвестно».",
        "",
        "Пробелы (что заявлено, но не работает или не проверено) — в [gaps.md](gaps.md).",
        "",
    ]
    total = 0
    for group, machines in GROUPS:
        lines.append(f"## {group}")
        lines.append("")
        rows = [(m, recipes[m]) for m in machines if m in recipes]
        if rows:
            lines.append("| Машина | Название | Тип | Рецептов | Энергия цикла | Время цикла | Исследования |")
            lines.append("| --- | --- | --- | --- | --- | --- | --- |")
            for machine, info in rows:
                total += info["count"]
                title = lang.get(f"block.terrafabrica.{machine}", machine.replace("_", " "))
                nodes = ", ".join(f"`{n}`" for n in info["nodes"]) or "—"
                lines.append(f"| `{machine}` | {title} | машина с рецептами | {info['count']} | "
                             f"{fmt_energy(info['energy'])} | {fmt_ticks(info['ticks'])} | {nodes} |")
            lines.append("")

        spec_ids = [m for m in machines if m in SPECS]
        if spec_ids:
            columns = COLUMNS[group]
            header = "| Машина | Название | Тип | " + " | ".join(columns) + " |"
            lines.append(header)
            lines.append("| --- | --- | --- |" + " --- |" * len(columns))
            for machine in spec_ids:
                kind, cells = SPECS[machine]
                title = lang.get(f"block.terrafabrica.{machine}", machine.replace("_", " "))
                if len(cells) != len(columns):
                    raise ValueError(
                        f"{group}/{machine}: {len(cells)} значений на {len(columns)} колонок")
                lines.append(f"| `{machine}` | {title} | {kind} | " + " | ".join(cells) + " |")
            lines.append("")

        note = NOTES.get(group)
        if note:
            lines.extend(note)
            lines.append("")

        missing = [m for m in machines if m not in recipes and m not in SPECS]
        if missing:
            lines.append("Без рецептов и без спецификации (проверить): "
                         + ", ".join(f"`{m}`" for m in missing))
            lines.append("")

    lines.append(f"Всего рецептов в перечисленных машинах: **{total}**.")
    lines.append("")
    write(os.path.join(DOCS, "machines.md"), lines)


def gen_progression(lang):
    tree = read_text(os.path.join(ROOT, "src", "main", "java", "com", "terrafabrica",
                                  "core", "research", "ResearchTree.java"))
    pattern = re.compile(
        r'node\(\s*"([a-z0-9_]+)"\s*,\s*(\d)\s*,\s*List\.of\((.*?)\)\s*,\s*([0-9_]+)', re.S)
    nodes = []
    for match in pattern.finditer(tree):
        node_id, era, prereqs_raw, ticks = match.groups()
        prereqs = re.findall(r'"([a-z0-9_]+)"', prereqs_raw)
        nodes.append({
            "id": node_id,
            "era": int(era),
            "prereqs": prereqs,
            "ticks": int(ticks.replace("_", "")),
        })

    lines = [
        "# Дерево исследований",
        "",
        "Раздел собран автоматически из `ResearchTree.java`: время указано на сервере",
        "(20 тиков = 1 секунда), названия берутся из `ru_ru.json`.",
        "",
    ]
    for era in (1, 2):
        era_nodes = [n for n in nodes if n["era"] == era]
        total_ticks = sum(n["ticks"] for n in era_nodes)
        lines.append(f"## {ERA_NAMES[era]}")
        lines.append("")
        lines.append(f"Узлов: **{len(era_nodes)}**. Суммарное время исследований: "
                     f"**{total_ticks / 20 / 60:.1f} мин** (если открывать по одному).")
        lines.append("")
        lines.append("| Узел | Название | Время | Предпосылки |")
        lines.append("| --- | --- | --- | --- |")
        for node in era_nodes:
            title = lang.get(f"research.terrafabrica.{node['id']}", node["id"])
            prereqs = ", ".join(f"`{p}`" for p in node["prereqs"]) or "—"
            lines.append(f"| `{node['id']}` | {title} | {node['ticks'] / 20:g} с | {prereqs} |")
        lines.append("")
    write(os.path.join(DOCS, "progression.md"), lines)
    return nodes


def read_text(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def write(path, lines):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines))
    print("[docs]", os.path.relpath(path, ROOT))


def main():
    lang = read_json(os.path.join(RES, "assets", "terrafabrica", "lang", "ru_ru.json"))
    gen_machines(recipe_index(), lang)
    nodes = gen_progression(lang)
    print(f"[docs] узлов исследований: {len(nodes)}")
    print("OK: документация обновлена")


if __name__ == "__main__":
    main()
