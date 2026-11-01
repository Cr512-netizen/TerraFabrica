import json, os, collections

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src/main/resources/assets/terrafabrica/lang")

RU_PORODE = {
    "silica": "Кремнезёмная порода (кварц)",
    "zircon": "Цирконовая порода",
    "lithium": "Литиевая порода (сподумен)",
    "gold": "Золотая порода",
    "silicon": "Кремниевая порода",
}
EN_PORODE = {
    "silica": "Silica Ore (Quartz)",
    "zircon": "Zircon Ore",
    "lithium": "Lithium Ore (Spodumene)",
    "gold": "Gold Ore",
    "silicon": "Silicon Ore",
}

EXTRA_RU = {
    "item.terrafabrica.transistor": "Транзистор",
    "item.terrafabrica.microchip": "Микросхема",
}
EXTRA_EN = {
    "item.terrafabrica.transistor": "Transistor",
    "item.terrafabrica.microchip": "Microchip",
}


def load(name):
    with open(os.path.join(LANG, name), encoding="utf-8") as f:
        return json.load(f, object_pairs_hook=collections.OrderedDict)


def save(name, data):
    with open(os.path.join(LANG, name), "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


ru = load("ru_ru.json")
en = load("en_us.json")
added_ru = added_en = 0
for m, porode in RU_PORODE.items():
    base = porode.split(" (")[0]
    for suffix, tmpl in (("rock", "Кусок породы: {b}"), ("powder", "Порошок: {b}"), ("concentrate", "Концентрат: {b}")):
        k = f"item.terrafabrica.{m}_{suffix}"
        if k not in ru:
            ru[k] = tmpl.format(b=base)
            added_ru += 1
    eb = EN_PORODE[m]
    for suffix, tmpl in (("rock", "{b} Rock Chunk"), ("powder", "{b} Powder"), ("concentrate", "{b} Concentrate")):
        k = f"item.terrafabrica.{m}_{suffix}"
        if k not in en:
            en[k] = tmpl.format(b=eb)
            added_en += 1
for k, v in EXTRA_RU.items():
    if k not in ru:
        ru[k] = v
        added_ru += 1
for k, v in EXTRA_EN.items():
    if k not in en:
        en[k] = v
        added_en += 1

save("ru_ru.json", ru)
save("en_us.json", en)
print(f"ru +{added_ru} (total {len(ru)}), en +{added_en} (total {len(en)})")
