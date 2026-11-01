import json, os, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RU = os.path.join(ROOT, "src/main/resources/assets/terrafabrica/lang/ru_ru.json")
EN = os.path.join(ROOT, "src/main/resources/assets/terrafabrica/lang/en_us.json")

ru = json.load(open(RU, encoding="utf-8"))
en = json.load(open(EN, encoding="utf-8"))

missing = sorted(k for k in ru if k not in en)
extra = sorted(k for k in en if k not in ru)
print("ru keys:", len(ru), "en keys:", len(en))
print("missing in en:", len(missing))
for k in missing:
    print("  +", k, "=", ru[k])
print("extra in en:", len(extra))
for k in extra[:40]:
    print("  -", k)
