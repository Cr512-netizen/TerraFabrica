# -*- coding: utf-8 -*-
"""fluid_in / fluid_out: формат FluidStack, а не FluidIngredient.

Выделенный сервер писал при загрузке рецептов:
    Parsing error loading recipe terrafabrica:lead_chamber/chamber_acid
    JsonParseException: No key id in MapLike[{"fluid":"terrafabrica:sulfur_dioxide","amount":1000}]

MachineRecipe использует NeoForge FluidStack.CODEC, у которого поля
{"id": <fluid id>, "amount": <мБ>}. Ключ "fluid" относится к другому коду
(FluidIngredient), из-за чего химические процессы молча выпадали из игры.
"""
from __future__ import annotations

import io
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

FLUIDS_FN = "\n".join([
    "def known_fluids():",
    '    """Жидкости: id из TFFluids.SPECS."""',
    '    text = read(os.path.join(SRC, "content", "TFFluids.java"))',
    "    return set(re.findall(r'new Spec\\(\"([a-z0-9_]+)\"', text))",
    "",
    "",
    "def known_research():",
])


def patch(rel, pairs):
    path = os.path.join(ROOT, rel)
    text = io.open(path, encoding="utf-8").read()
    for old, new in pairs:
        assert old in text, (rel, old[:60])
        text = text.replace(old, new, 1)
    io.open(path, "w", encoding="utf-8", newline="").write(text)
    print("patched", rel)


patch("tools/generate_data.py", [
    ('        r["fluid_in"] = {"fluid": f"{MODID}:{fluid_in[0]}", "amount": fluid_in[1]}',
     '        # FluidStack.CODEC (NeoForge 1.21.1): {"id": ..., "amount": ...},\n'
     '        # а не {"fluid": ...} — второй вариант относится к FluidIngredient\n'
     '        # и роняет загрузку рецепта (No key id in MapLike).\n'
     '        r["fluid_in"] = {"id": f"{MODID}:{fluid_in[0]}", "amount": fluid_in[1]}'),
    ('        r["fluid_out"] = {"fluid": f"{MODID}:{fluid_out[0]}", "amount": fluid_out[1]}',
     '        r["fluid_out"] = {"id": f"{MODID}:{fluid_out[0]}", "amount": fluid_out[1]}'),
])

patch("src/main/java/com/terrafabrica/content/shared/recipe/MachineRecipe.java", [
    (' *   "fluid_in":  {"fluid": "terrafabrica:water", "amount": 500},  // опц., мБ',
     ' *   "fluid_in":  {"id": "terrafabrica:steam_low", "amount": 500},   // опц., мБ'),
    (' *   "fluid_out": {"fluid": "terrafabrica:sulfuric_acid", "amount": 1200},',
     ' *   "fluid_out": {"id": "terrafabrica:sulfuric_acid", "amount": 1200},'),
])

patch("docs/datapack.md", [
    ("| `fluid_in` | `{fluid, amount}` | расход жидкости, мБ за цикл |",
     "| `fluid_in` | `{id, amount}` | расход жидкости, мБ за цикл |"),
    ("| `fluid_out` | `{fluid, amount}` | выход жидкости, мБ за цикл |",
     "| `fluid_out` | `{id, amount}` | выход жидкости, мБ за цикл |"),
    ('  "fluid_in":  { "fluid": "terrafabrica:oxygen", "amount": 500 },',
     '  "fluid_in":  { "id": "terrafabrica:oxygen", "amount": 500 },'),
    ('  "fluid_out": { "fluid": "terrafabrica:sulfur_dioxide", "amount": 1000 },',
     '  "fluid_out": { "id": "terrafabrica:sulfur_dioxide", "amount": 1000 },'),
])

CHECK_OLD = """                if data.get("energy", 0) < 0 or data.get("ticks", 0) <= 0:
                    problems.append(f"{where}: странные energy/ticks")"""

CHECK_NEW = CHECK_OLD + """
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
                            problems.append(f"{where}: нет жидкости {fid}")"""

patch("tools/validate_data.py", [
    ("def known_research():", FLUIDS_FN),
    ("    research = known_research()", "    research = known_research()\n    fluids = known_fluids()"),
    (CHECK_OLD, CHECK_NEW),
])
