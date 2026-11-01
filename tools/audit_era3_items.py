import json, os, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REC = os.path.join(ROOT, "src/main/resources/data/terrafabrica/recipe")

items = set()
# collect all item ids produced by recipes
produced = set()
for dirpath, _, files in os.walk(REC):
    for f in files:
        if not f.endswith(".json"):
            continue
        p = os.path.join(dirpath, f)
        try:
            d = json.load(open(p, encoding="utf-8"))
        except Exception as e:
            print("BAD JSON", p, e)
            continue
        for r in d.get("results", []) or []:
            rid = r.get("id") or r.get("item")
            if rid:
                produced.add(rid)
        res = d.get("result")
        if isinstance(res, dict):
            rid = res.get("id") or res.get("item")
            if rid:
                produced.add(rid)

era3 = ["carbon_fiber","silicon","silica","gold_ingot","zircon","lithium_ingot",
        "germanium","indium","gallium_arsenide","photovoltaic_cell","superconductor",
        "fusion_fuel","deuterium","tritium","nuclear_fuel_rod","reactor_shield",
        "satellite_part","ev_battery","solar_panel","ai_core","robot","drone",
        "quantum_computer","transistor","microchip"]

print("=== Era III item production audit ===")
missing = []
for it in era3:
    rid = "terrafabrica:" + it
    if rid in produced:
        print("OK     ", it)
    else:
        print("MISSING", it)
        missing.append(it)

print()
print("missing count:", len(missing))
sys.exit(1 if missing else 0)
