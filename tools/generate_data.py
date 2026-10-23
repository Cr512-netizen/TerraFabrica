#!/usr/bin/env python3
"""
Terra Fabrica — генератор данных (recipes, tags, lang, models, blockstates,
loot tables, worldgen, sounds.json, pack.mcmeta).

Запуск: python tools/generate_data.py
Пишет в src/main/resources/assets/terrafabrica/ и src/main/resources/data/.
"""
import json
import os
import sys

# guide_text.py лежит рядом; запуск может быть из любого каталога.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from guide_text import GUIDE_CHAPTERS, GUIDE_UI_RU, GUIDE_UI_EN  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "terrafabrica")
DATA = os.path.join(RES, "data", "terrafabrica")
# Теги, расширяющие ванильные (mineable/*, needs_*_tool), обязаны лежать в
# пространстве имён minecraft, иначе они не влияют на ванильную логику.
MC_DATA = os.path.join(RES, "data", "minecraft")
MODID = "terrafabrica"

# Число строк в каждой главе руководства. Должно совпадать с GuideContent.java:
# ключи lang нумеруются подряд, и тест ловит расхождение.
GUIDE_LINE_COUNTS = {"intro": 6, "units": 7, "porodes": 6, "era1": 7,
                     "era2": 8, "research": 4, "datapack": 5}


# Все пути, записанные в этом прогоне: по ним в конце чистим осиротевшие файлы
# (иначе после переименования рецепта в data/ остаётся старый JSON и ломает загрузку).
WRITTEN = set()


def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
    WRITTEN.add(os.path.normcase(os.path.abspath(path)))


# Каталоги, содержимым которых владеет только этот генератор.
OWNED_DIRS = ["assets/terrafabrica/blockstates", "assets/terrafabrica/models",
              "assets/terrafabrica/lang", "data/terrafabrica/recipe",
              "data/terrafabrica/loot_table", "data/terrafabrica/tags",
              "data/terrafabrica/worldgen", "data/minecraft/tags",
              "data/terrafabrica/neoforge/biome_modifier"]


def prune_orphans():
    """Удаляет .json, которые генератор больше не пишет (переименованные/удалённые id)."""
    removed = 0
    for rel in OWNED_DIRS:
        base = os.path.join(RES, *rel.split("/"))
        for dirpath, _, files in os.walk(base):
            for name in files:
                if not name.endswith(".json"):
                    continue
                path = os.path.join(dirpath, name)
                if os.path.normcase(os.path.abspath(path)) not in WRITTEN:
                    os.remove(path)
                    removed += 1
    if removed:
        print(f"[data] удалено осиротевших файлов: {removed}")


# ---------------------------------------------------------------------------
# Таблицы контента
# ---------------------------------------------------------------------------

ERA1 = ["copper", "tin", "hematite", "magnetite", "galena", "pyrite", "limestone",
        "claystone", "coal", "sylvite"]
ERA2 = ["bauxite", "chromite", "pentlandite", "sphalerite", "molybdenite", "wolframite",
        "uraninite", "pyrolusite", "fluorite", "barite", "apatite", "gypsum", "magnesite",
        "sulfur", "graphite", "halite", "carnallite"]
MINERALS = ERA1 + ERA2

# Флюиды (порядок и id — как в TFFluids.SPECS). Блоки жидкостей регистрируются
# в TFFluids, но blockstate/модель обязаны существовать: иначе клиент пишет
# "missing model for variant" и в мире видна фиолетовая заглушка.
FLUID_IDS = ["steam_low", "steam_high", "sulfuric_acid", "sulfuric_acid_dilute",
             "nitric_acid", "hydrochloric_acid", "ammonia", "chlorine", "hydrogen",
             "oxygen", "nitrogen", "carbon_dioxide", "natural_gas", "crude_oil",
             "fuel_oil", "gasoline", "kerosene", "diesel", "molten_glass",
             "molten_metal", "sulfur_dioxide", "coal_gas"]
RICHNESS = ["poor", "normal", "rich"]

# Генерация: минерал → (размер жилы, жил на чанк, minY, maxY)
WORLDGEN = {
    "copper": (8, 8, -16, 64), "tin": (4, 3, 32, 96),
    "hematite": (9, 6, -16, 72), "magnetite": (7, 4, -48, 32),
    "galena": (6, 4, -32, 24), "pyrite": (6, 4, 20, 60),
    "limestone": (24, 12, 40, 80), "claystone": (20, 8, 50, 70),
    "coal": (17, 16, 30, 100), "sylvite": (6, 4, 20, 60),
    "bauxite": (8, 6, 0, 60), "chromite": (6, 4, -32, 32),
    "pentlandite": (6, 4, -32, 24), "sphalerite": (6, 4, -32, 24),
    "molybdenite": (4, 3, -40, 20), "wolframite": (4, 3, -40, 20),
    "uraninite": (4, 2, -60, 8), "pyrolusite": (6, 4, -32, 24),
    "fluorite": (6, 4, -32, 24), "barite": (6, 3, -20, 30),
    "apatite": (8, 4, -20, 40), "gypsum": (12, 6, -20, 50),
    "magnesite": (8, 4, -20, 40), "sulfur": (6, 3, -20, 40),
    "graphite": (6, 4, -40, 20), "halite": (12, 6, -40, 30),
    "carnallite": (8, 4, -40, 20),
}
WEIGHTS = {"poor": 0.3, "normal": 0.6, "rich": 0.1}
# chance для minecraft:rarity_filter (целое, 1/N); 1/2 : 1/1 : 1/6 = 30/60/10 %.
RARITY_CHANCE = {"poor": 2, "normal": 1, "rich": 6}

CASINGS = ["iron_casing", "steam_casing", "refractory_brick", "reactor_casing", "lead_casing"]

# Архетип фронта машины
ARCHETYPE = {
    "crusher": "machine", "separator": "machine",
    "primitive_furnace": "furnace", "bloomery": "furnace", "small_blast_furnace": "furnace",
    "puddling_furnace": "furnace", "crucible_furnace": "furnace", "calcination_furnace": "furnace",
    "pyrite_roaster": "furnace", "coke_oven": "furnace", "blast_furnace": "furnace",
    "open_hearth_furnace": "furnace", "electric_arc_furnace": "furnace",
    "water_wheel": "machine", "windmill": "machine",
    "steam_engine_newcomen": "turbine", "steam_engine_watt": "turbine",
    "shaft": "machine", "gear": "machine", "belt": "machine", "pulley": "machine",
    "gearbox": "machine", "conveyor": "machine",
    "lathe": "machine", "drill_press": "machine", "milling_machine": "machine",
    "grinding_machine": "machine", "rolling_mill": "machine", "power_hammer": "machine",
    "heavy_rolling_mill": "machine", "wire_drawing_machine": "machine",
    "weight_dispenser": "machine",
    "steam_turbine": "turbine", "gas_turbine": "turbine", "ccgt_plant": "turbine",
    "electric_generator": "electrical", "transformer": "electrical",
    "electric_motor": "electrical", "lead_acid_battery": "electrical",
    "cable_copper": "electrical", "cable_aluminum": "electrical", "cable_steel": "electrical",
    "power_pole_wood": "electrical", "power_pole_concrete": "electrical", "power_pole_steel": "electrical",
    "insulator_porcelain": "electrical", "insulator_glass": "electrical",
    "substation": "electrical", "circuit_breaker": "electrical",
    "air_separation_unit": "electrical", "water_electrolyzer": "electrical",
    "ammonia_synthesizer": "tank", "ostwald_reactor": "tank", "contact_acid_reactor": "tank",
    "chloralkali_cell": "tank", "aluminum_electrolyzer": "tank",
    "fertilizer_mixer": "tank", "oil_distillation_column": "tank",
    "bessemer_converter": "tank",
    "lead_chamber": "tank", "absorber": "tank", "solvay_reactor": "tank",
    "telegraph": "console", "telephone": "console", "radio": "console",
    "tube_computer": "console", "relay": "console", "plc": "console",
    "sensor_temperature": "console", "sensor_pressure": "console",
    "sensor_level": "console", "sensor_flow": "console", "valve": "console",
    "rail": "machine", "sleeper": "machine",
}

MACHINES = list(ARCHETYPE.keys())

# ---------------------------------------------------------------------------
# pack.mcmeta и sounds.json
# ---------------------------------------------------------------------------

# Собственные .ogg в поставку не входят (нет свободных записей под лицензией), поэтому
# каждое звуковое событие мода проигрывает проверенный ванильный файл из индекса
# ассетов 1.21.1. Пути нижесверены по assets/indexes/17.json: опечатка здесь означала бы
# молчащий звук и предупреждение в логе.
SOUND_FILES = {
    "machine_generic": "minecraft:tile/piston/out",
    "crusher_work": "minecraft:block/grindstone/grindstone1",
    "lathe_work": "minecraft:block/smithing_table/smithing_table1",
    "hammer_work": "minecraft:random/anvil_use",
    "steam_engine": "minecraft:block/campfire/crackle1",
    "turbine": "minecraft:ambient/nether/soulsand_valley/wind1",
    "generator": "minecraft:block/beacon/ambient",
    "chemical_bubble": "minecraft:ambient/underwater/additions/crackles1",
    "locomotive_steam": "minecraft:block/blastfurnace/blastfurnace1",
    "telegraph_click": "minecraft:random/click",
    "telephone_ring": "minecraft:block/beacon/activate",
    "radio_static": "minecraft:ambient/underwater/additions/crackles2",
    "conveyor_move": "minecraft:tile/piston/in",
    "boiler_explosion": "minecraft:random/explode1",
    "arc_furnace": "minecraft:block/blastfurnace/blastfurnace2",
    "converter_sparks": "minecraft:random/anvil_land",
    "windmill_turn": "minecraft:ambient/nether/soulsand_valley/wind2",
    "water_wheel_turn": "minecraft:block/campfire/crackle2",
}

SUBTITLES_RU = {
    "machine_generic": "Машина стучит", "crusher_work": "Дробилка работает",
    "lathe_work": "Станок работает", "hammer_work": "Молот бьёт",
    "steam_engine": "Паровая машина работает", "turbine": "Турбина вращается",
    "generator": "Генератор гудит", "chemical_bubble": "Химическая реакция",
    "locomotive_steam": "Локомотив выпускает пар", "telegraph_click": "Телеграф стучит",
    "telephone_ring": "Телефон звонит", "radio_static": "Радиопомехи",
    "conveyor_move": "Конвейер движется", "boiler_explosion": "Котёл взорвался",
    "arc_furnace": "Дуговая печь гудит", "converter_sparks": "Конвертер искрит",
    "windmill_turn": "Ветряк вращается", "water_wheel_turn": "Водяное колесо вращается",
}

SUBTITLES_EN = {
    "machine_generic": "Machine clanks", "crusher_work": "Crusher grinds",
    "lathe_work": "Lathe works", "hammer_work": "Hammer strikes",
    "steam_engine": "Steam engine runs", "turbine": "Turbine spins",
    "generator": "Generator hums", "chemical_bubble": "Chemical reaction",
    "locomotive_steam": "Locomotive vents steam", "telegraph_click": "Telegraph clicks",
    "telephone_ring": "Telephone rings", "radio_static": "Radio static",
    "conveyor_move": "Conveyor moves", "boiler_explosion": "Boiler explodes",
    "arc_furnace": "Arc furnace hums", "converter_sparks": "Converter sparks",
    "windmill_turn": "Windmill turns", "water_wheel_turn": "Water wheel turns",
}


def gen_pack_and_sounds():
    wj(os.path.join(RES, "pack.mcmeta"), {
        "pack": {
            "description": "Terra Fabrica resources",
            "pack_format": 34,
            # 1.21.1 = 34; допускаем и 1.21–1.21.1, чтобы набор не «ломался» на смежных версиях.
            "supported_formats": [34, 48],
        }})
    sounds = {e: {"sounds": [{"name": name}], "subtitle": f"subtitle.{MODID}.{e}"}
              for e, name in SOUND_FILES.items()}
    wj(os.path.join(ASSETS, "sounds.json"), sounds)


# ---------------------------------------------------------------------------
# Локализация
# ---------------------------------------------------------------------------

RU_PORODE = {
    "copper": "Медная порода (халькопирит)", "tin": "Оловянная порода (касситерит)",
    "hematite": "Богатая железная порода (гематит)", "magnetite": "Бедная железная порода (магнетит)",
    "galena": "Свинцовая порода (галенит)", "pyrite": "Пиритовая порода",
    "limestone": "Известняковая порода", "claystone": "Глинистая порода",
    "coal": "Угольная порода", "sylvite": "Сильвиновая порода",
    "bauxite": "Бокситовая порода", "chromite": "Хромитовая порода",
    "pentlandite": "Пентландитовая порода", "sphalerite": "Сфалеритовая порода",
    "molybdenite": "Молибденитовая порода", "wolframite": "Вольфрамовая порода",
    "uraninite": "Уранинитовая порода", "pyrolusite": "Пиролюзитовая порода",
    "fluorite": "Флюоритовая порода", "barite": "Баритовая порода",
    "apatite": "Апатитовая порода", "gypsum": "Гипсовая порода",
    "magnesite": "Магнезитовая порода", "sulfur": "Серная порода",
    "graphite": "Графитовая порода", "halite": "Галитовая порода",
    "carnallite": "Карналлитовая порода",
}
EN_PORODE = {k: v.split(" (")[0] for k, v in RU_PORODE.items()}

RU_BLOCKS = {
    "iron_casing": "Железная обшивка", "steam_casing": "Паровая обшивка",
    "refractory_brick": "Огнеупорный кирпич", "reactor_casing": "Обшивка реактора",
    "lead_casing": "Свинцовая обшивка",
    "crusher": "Дробилка", "separator": "Сепаратор",
    "primitive_furnace": "Примитивная печь", "bloomery": "Горн с мехами",
    "small_blast_furnace": "Малая домна", "puddling_furnace": "Пудлинговая печь",
    "crucible_furnace": "Тигельная печь", "calcination_furnace": "Печь прокалки",
    "pyrite_roaster": "Печь обжига пирита", "water_wheel": "Водяное колесо",
    "windmill": "Ветряк", "steam_engine_newcomen": "Паровой двигатель Ньюкомена",
    "steam_engine_watt": "Паровой двигатель Уатта",
    "shaft": "Вал", "gear": "Шестерня", "belt": "Ремень", "pulley": "Шкив",
    "gearbox": "Редуктор", "conveyor": "Ленточный транспортёр",
    "lathe": "Токарный станок", "drill_press": "Сверлильный станок",
    "milling_machine": "Фрезерный станок", "grinding_machine": "Шлифовальный станок",
    "rolling_mill": "Прокатный стан", "power_hammer": "Механический молот",
    "heavy_rolling_mill": "Тяжёлый прокатный стан", "wire_drawing_machine": "Волочильный стан",
    "weight_dispenser": "Весовой дозатор", "steam_turbine": "Паровая турбина",
    "gas_turbine": "Газовая турбина", "ccgt_plant": "Парогазовая установка",
    "electric_generator": "Электрогенератор", "transformer": "Трансформатор",
    "electric_motor": "Электродвигатель", "lead_acid_battery": "Свинцово-кислотный аккумулятор",
    "cable_copper": "Медный кабель", "cable_aluminum": "Алюминиевый кабель",
    "cable_steel": "Стальной кабель", "power_pole_wood": "Деревянная опора",
    "power_pole_concrete": "Бетонная опора", "power_pole_steel": "Стальная опора",
    "insulator_porcelain": "Фарфоровый изолятор", "insulator_glass": "Стеклянный изолятор",
    "substation": "Подстанция", "circuit_breaker": "Автоматический выключатель",
    "air_separation_unit": "Воздухоразделительная установка",
    "water_electrolyzer": "Электролизёр воды", "ammonia_synthesizer": "Синтез аммиака (Габер)",
    "ostwald_reactor": "Реактор Оствальда", "contact_acid_reactor": "Контактный реактор серной кислоты",
    "chloralkali_cell": "Электролизёр рассола", "aluminum_electrolyzer": "Электролизёр алюминия",
    "fertilizer_mixer": "Смеситель удобрений", "oil_distillation_column": "Колонна перегонки нефти",
    "bessemer_converter": "Бессемеровский конвертер", "coke_oven": "Коксовая печь",
    "blast_furnace": "Домна на коксе", "open_hearth_furnace": "Мартеновская печь",
    "electric_arc_furnace": "Электродуговая печь",
    "lead_chamber": "Свинцовая камера", "absorber": "Абсорбер",
    "solvay_reactor": "Реактор Сольве",
    "telegraph": "Телеграф", "telephone": "Телефон", "radio": "Радио",
    "tube_computer": "Ламповый компьютер", "relay": "Реле", "plc": "Ламповый ПЛК",
    "sensor_temperature": "Датчик температуры", "sensor_pressure": "Датчик давления",
    "sensor_level": "Датчик уровня", "sensor_flow": "Датчик расхода",
    "valve": "Клапан", "rail": "Рельс", "sleeper": "Шпала",
    "heat_resistant_glass": "Термостойкое стекло", "optical_glass": "Оптическое стекло",
}
EN_BLOCKS = {
    "iron_casing": "Iron Casing", "steam_casing": "Steam Casing",
    "refractory_brick": "Refractory Brick", "reactor_casing": "Reactor Casing",
    "lead_casing": "Lead Casing", "crusher": "Crusher", "separator": "Separator",
    "primitive_furnace": "Primitive Furnace", "bloomery": "Bloomery with Bellows",
    "small_blast_furnace": "Small Blast Furnace", "puddling_furnace": "Puddling Furnace",
    "crucible_furnace": "Crucible Furnace", "calcination_furnace": "Calcination Furnace",
    "pyrite_roaster": "Pyrite Roaster", "water_wheel": "Water Wheel", "windmill": "Windmill",
    "steam_engine_newcomen": "Newcomen Steam Engine", "steam_engine_watt": "Watt Steam Engine",
    "shaft": "Shaft", "gear": "Gear", "belt": "Belt", "pulley": "Pulley",
    "gearbox": "Gearbox", "conveyor": "Belt Conveyor", "lathe": "Lathe",
    "drill_press": "Drill Press", "milling_machine": "Milling Machine",
    "grinding_machine": "Grinding Machine", "rolling_mill": "Rolling Mill",
    "power_hammer": "Power Hammer", "heavy_rolling_mill": "Heavy Rolling Mill",
    "wire_drawing_machine": "Wire Drawing Machine", "weight_dispenser": "Weight Dispenser",
    "steam_turbine": "Steam Turbine", "gas_turbine": "Gas Turbine",
    "ccgt_plant": "Combined Cycle Plant", "electric_generator": "Electric Generator",
    "transformer": "Transformer", "electric_motor": "Electric Motor",
    "lead_acid_battery": "Lead-Acid Battery", "cable_copper": "Copper Cable",
    "cable_aluminum": "Aluminum Cable", "cable_steel": "Steel Cable",
    "power_pole_wood": "Wooden Power Pole", "power_pole_concrete": "Concrete Power Pole",
    "power_pole_steel": "Steel Power Pole", "insulator_porcelain": "Porcelain Insulator",
    "insulator_glass": "Glass Insulator", "substation": "Substation",
    "circuit_breaker": "Circuit Breaker", "air_separation_unit": "Air Separation Unit",
    "water_electrolyzer": "Water Electrolyzer", "ammonia_synthesizer": "Ammonia Synthesizer",
    "ostwald_reactor": "Ostwald Reactor", "contact_acid_reactor": "Contact Acid Reactor",
    "chloralkali_cell": "Chlor-Alkali Cell", "aluminum_electrolyzer": "Aluminum Electrolyzer",
    "fertilizer_mixer": "Fertilizer Mixer", "oil_distillation_column": "Oil Distillation Column",
    "bessemer_converter": "Bessemer Converter", "coke_oven": "Coke Oven",
    "blast_furnace": "Coke Blast Furnace", "open_hearth_furnace": "Open-Hearth Furnace",
    "electric_arc_furnace": "Electric Arc Furnace", "lead_chamber": "Lead Chamber",
    "absorber": "Absorber", "solvay_reactor": "Solvay Reactor",
    "telegraph": "Telegraph", "telephone": "Telephone", "radio": "Radio",
    "tube_computer": "Vacuum Tube Computer", "relay": "Relay", "plc": "Tube PLC",
    "sensor_temperature": "Temperature Sensor", "sensor_pressure": "Pressure Sensor",
    "sensor_level": "Level Sensor", "sensor_flow": "Flow Sensor", "valve": "Valve",
    "rail": "Rail", "sleeper": "Sleeper",
    "heat_resistant_glass": "Heat-Resistant Glass", "optical_glass": "Optical Glass",
}

RU_ITEMS = {
    "copper_rock": "Кусок медной породы", "tin_rock": "Кусок оловянной породы",
    "copper_powder": "Медный порошок", "tin_powder": "Оловянный порошок",
    "copper_concentrate": "Медный концентрат", "tin_concentrate": "Оловянный концентрат",
    "tin_ingot": "Оловянный слиток", "bronze_ingot": "Бронзовый слиток",
    "lead_ingot": "Свинцовый слиток", "pig_iron_ingot": "Чугунный слиток",
    "brass_ingot": "Латунный слиток", "steel_ingot": "Стальной слиток",
    "stainless_steel_ingot": "Слиток нержавеющей стали", "superalloy_ingot": "Слиток жаропрочного сплава",
    "aluminum_ingot": "Алюминиевый слиток", "chromium_ingot": "Хромовый слиток",
    "nickel_ingot": "Никелевый слиток", "zinc_ingot": "Цинковый слиток",
    "molybdenum_ingot": "Молибденовый слиток", "tungsten_ingot": "Вольфрамовый слиток",
    "manganese_ingot": "Марганцевый слиток", "magnesium_ingot": "Магниевый слиток",
    "platinum_ingot": "Платиновый слиток", "cobalt_ingot": "Кобальтовый слиток",
    "niobium_ingot": "Ниобиевый слиток", "titanium_ingot": "Титановый слиток",
    "vanadium_ingot": "Ванадиевый слиток", "titanium_alloy_ingot": "Слиток Ti-6Al-4V",
    "slag": "Шлак", "coke": "Кокс", "coal_tar": "Каменноугольная смола",
    "lime": "Известь", "potash": "Поташ", "soda_ash": "Сода",
    "saltpeter": "Селитра", "ammonium_chloride": "Хлорид аммония",
    "caustic_soda": "Каустическая сода", "yellowcake": "Жёлтый кек",
    "cryolite": "Криолит", "fluoride_salt": "Фторидная соль",
    "ammonium_nitrate": "Аммиачная селитра", "superphosphate": "Суперфосфат",
    "potash_fertilizer": "Калийное удобрение",
    "sulfuric_acid_bottle": "Бутыль серной кислоты", "nitric_acid_bottle": "Бутыль азотной кислоты",
    "hydrochloric_acid_bottle": "Бутыль соляной кислоты", "graphite_electrode": "Графитовый электрод",
    "firebrick": "Огнеупорный кирпич", "salt": "Соль", "sodium_bicarbonate": "Гидрокарбонат натрия",
    "iron_catalyst": "Железный катализатор", "platinum_rhodium_net": "Платино-родиевая сетка",
    "vanadium_catalyst": "Ванадиевый катализатор",
    "iron_rod": "Железный вал", "bolt": "Болт", "nut": "Гайка",
    "precision_part": "Точная деталь (0.5 мм)", "machine_frame": "Станина машины",
    "piston": "Поршень", "cylinder": "Цилиндр", "condenser": "Конденсатор",
    "balance_beam": "Балансир", "pump": "Насос", "boiler_part": "Котёл",
    "flywheel": "Маховик", "steam_chest": "Золотник", "governor": "Центробежный регулятор",
    "bellows": "Меха", "copper_wire": "Медная проволока", "aluminum_wire": "Алюминиевая проволока",
    "steel_wire": "Стальная проволока", "copper_coil": "Медная катушка",
    "copper_plate": "Медный лист", "iron_plate": "Железный лист",
    "steel_plate": "Стальной лист", "aluminum_plate": "Алюминиевый лист",
    "tin_plate": "Оловянный лист", "bronze_plate": "Бронзовый лист",
    "steel_beam": "Стальная балка", "rotor": "Ротор", "stator": "Статор",
    "vacuum_tube": "Электронная лампа", "magnetic_core": "Магнитный сердечник",
    "carbon_microphone": "Угольный микрофон", "speaker": "Динамик",
    "commutator_board": "Коммутаторная панель", "antenna": "Антенна",
    "wheel": "Колесо", "wagon_body": "Кузов вагона", "rubber": "Резина",
    "multimeter": "Мультиметр", "thermometer": "Термометр", "tachometer": "Тахометр",
    "geiger_counter": "Счётчик Гейгера", "guide_book": "Руководство Terra Fabrica",
    "bronze_gear": "Бронзовая шестерня", "iron_gear": "Железная шестерня",
    "minecart": "Вагонетка", "steam_locomotive": "Малый паровоз",
    "diesel_locomotive": "Тепловоз", "car": "Автомобиль", "ship": "Судно",
    "submarine": "Подводная лодка", "ammonia_bottle": "Бутыль аммиака",
}
EN_ITEMS = {
    "copper_rock": "Copper Rock Chunk", "copper_powder": "Copper Powder",
    "copper_concentrate": "Copper Concentrate", "tin_ingot": "Tin Ingot",
    "bronze_ingot": "Bronze Ingot", "lead_ingot": "Lead Ingot",
    "pig_iron_ingot": "Pig Iron Ingot", "brass_ingot": "Brass Ingot",
    "steel_ingot": "Steel Ingot", "stainless_steel_ingot": "Stainless Steel Ingot",
    "superalloy_ingot": "Superalloy Ingot", "aluminum_ingot": "Aluminum Ingot",
    "chromium_ingot": "Chromium Ingot", "nickel_ingot": "Nickel Ingot",
    "zinc_ingot": "Zinc Ingot", "molybdenum_ingot": "Molybdenum Ingot",
    "tungsten_ingot": "Tungsten Ingot", "manganese_ingot": "Manganese Ingot",
    "magnesium_ingot": "Magnesium Ingot", "platinum_ingot": "Platinum Ingot",
    "cobalt_ingot": "Cobalt Ingot", "niobium_ingot": "Niobium Ingot",
    "titanium_ingot": "Titanium Ingot", "vanadium_ingot": "Vanadium Ingot",
    "titanium_alloy_ingot": "Ti-6Al-4V Ingot", "slag": "Slag", "coke": "Coke",
    "coal_tar": "Coal Tar", "lime": "Lime", "potash": "Potash",
    "soda_ash": "Soda Ash", "saltpeter": "Saltpeter",
    "ammonium_chloride": "Ammonium Chloride", "caustic_soda": "Caustic Soda",
    "yellowcake": "Yellowcake", "cryolite": "Cryolite", "fluoride_salt": "Fluoride Salt",
    "ammonium_nitrate": "Ammonium Nitrate", "superphosphate": "Superphosphate",
    "potash_fertilizer": "Potash Fertilizer", "sulfuric_acid_bottle": "Sulfuric Acid Bottle",
    "nitric_acid_bottle": "Nitric Acid Bottle", "hydrochloric_acid_bottle": "Hydrochloric Acid Bottle",
    "graphite_electrode": "Graphite Electrode", "firebrick": "Firebrick",
    "salt": "Salt", "sodium_bicarbonate": "Sodium Bicarbonate",
    "iron_catalyst": "Iron Catalyst", "platinum_rhodium_net": "Platinum-Rhodium Gauze",
    "vanadium_catalyst": "Vanadium Catalyst", "iron_rod": "Iron Rod", "bolt": "Bolt",
    "nut": "Nut", "precision_part": "Precision Part (0.5 mm)", "machine_frame": "Machine Frame",
    "piston": "Piston", "cylinder": "Cylinder", "condenser": "Condenser",
    "balance_beam": "Balance Beam", "pump": "Pump", "boiler_part": "Boiler",
    "flywheel": "Flywheel", "steam_chest": "Steam Chest", "governor": "Centrifugal Governor",
    "bellows": "Bellows", "copper_wire": "Copper Wire", "aluminum_wire": "Aluminum Wire",
    "steel_wire": "Steel Wire", "copper_coil": "Copper Coil",
    "copper_plate": "Copper Plate", "iron_plate": "Iron Plate",
    "steel_plate": "Steel Plate", "aluminum_plate": "Aluminum Plate",
    "tin_plate": "Tin Plate", "bronze_plate": "Bronze Plate", "steel_beam": "Steel Beam",
    "rotor": "Rotor", "stator": "Stator", "vacuum_tube": "Vacuum Tube",
    "magnetic_core": "Magnetic Core", "carbon_microphone": "Carbon Microphone",
    "speaker": "Speaker", "commutator_board": "Commutator Board", "antenna": "Antenna",
    "wheel": "Wheel", "wagon_body": "Wagon Body", "rubber": "Rubber",
    "multimeter": "Multimeter", "thermometer": "Thermometer", "tachometer": "Tachometer",
    "geiger_counter": "Geiger Counter", "guide_book": "Terra Fabrica Guide",
    "bronze_gear": "Bronze Gear", "iron_gear": "Iron Gear",
    "minecart": "Minecart", "steam_locomotive": "Small Steam Locomotive",
    "diesel_locomotive": "Diesel Locomotive", "car": "Automobile", "ship": "Ship",
    "submarine": "Submarine", "ammonia_bottle": "Ammonia Bottle",
}

FLUIDS_RU = {
    "steam_low": "Пар низкого давления", "steam_high": "Пар высокого давления",
    "sulfuric_acid": "Серная кислота", "sulfuric_acid_dilute": "Разбавленная серная кислота",
    "nitric_acid": "Азотная кислота", "hydrochloric_acid": "Соляная кислота",
    "ammonia": "Аммиак", "chlorine": "Хлор", "hydrogen": "Водород",
    "oxygen": "Кислород", "nitrogen": "Азот", "carbon_dioxide": "Углекислый газ",
    "natural_gas": "Природный газ", "crude_oil": "Нефть", "fuel_oil": "Мазут",
    "gasoline": "Бензин", "kerosene": "Керосин", "diesel": "Дизельное топливо",
    "molten_glass": "Расплав стекла", "molten_metal": "Расплав металла",
    "sulfur_dioxide": "Диоксид серы", "coal_gas": "Светильный газ",
}
FLUIDS_EN = {
    "steam_low": "Low-Pressure Steam", "steam_high": "High-Pressure Steam",
    "sulfuric_acid": "Sulfuric Acid", "sulfuric_acid_dilute": "Dilute Sulfuric Acid",
    "nitric_acid": "Nitric Acid", "hydrochloric_acid": "Hydrochloric Acid",
    "ammonia": "Ammonia", "chlorine": "Chlorine", "hydrogen": "Hydrogen",
    "oxygen": "Oxygen", "nitrogen": "Nitrogen", "carbon_dioxide": "Carbon Dioxide",
    "natural_gas": "Natural Gas", "crude_oil": "Crude Oil", "fuel_oil": "Fuel Oil",
    "gasoline": "Gasoline", "kerosene": "Kerosene", "diesel": "Diesel",
    "molten_glass": "Molten Glass", "molten_metal": "Molten Metal",
    "sulfur_dioxide": "Sulfur Dioxide", "coal_gas": "Coal Gas",
}

RESEARCH_RU = {
    "woodworking": "Обработка дерева", "stone_working": "Обработка камня",
    "copper_metallurgy": "Металлургия меди", "ore_enrichment": "Обогащение руды",
    "water_wheel": "Водяное колесо", "bronze_metallurgy": "Металлургия бронзы",
    "transmission": "Механическая трансмиссия", "iron_metallurgy": "Металлургия железа",
    "steam_newcomen": "Паровой двигатель Ньюкомена", "steam_watt": "Паровой двигатель Уатта",
    "machine_tools_lathe": "Токарный станок", "machine_tools_drill": "Сверлильный станок",
    "machine_tools_rolling": "Прокатный стан", "machine_tools_milling": "Фрезерный станок",
    "machine_tools_hammer": "Механический молот", "machine_tools_grinding": "Шлифовальный станок",
    "chemistry_sulfuric": "Серная кислота (камерный способ)", "transport_rails": "Рельсы и шпалы",
    "chemistry_soda": "Сода (процесс Сольве)", "transport_minecart": "Вагонетки",
    "chemistry_special_glass": "Специальные стёкла", "transport_conveyor": "Ленточный транспортёр",
    "transport_locomotive": "Малый паровоз", "era2_transition": "Переход к Эпохе II",
    "coke_making": "Коксование", "steam_turbine": "Паровая турбина",
    "blast_furnace_coke": "Домна на коксе", "electric_generator": "Электрогенератор",
    "bessemer": "Бессемеровский конвертер", "transformer": "Трансформатор",
    "power_network": "Электросети", "open_hearth": "Мартеновская печь",
    "electric_motor": "Электродвигатель", "battery": "Аккумуляторы",
    "electric_arc_furnace": "Электродуговая печь", "heavy_rolling": "Тяжёлый прокатный стан",
    "air_separation": "Воздухоразделение", "wire_drawing": "Волочильный стан",
    "water_electrolysis": "Электролиз воды", "oil_refining": "Перегонка нефти",
    "gas_turbine": "Газовая турбина", "ammonia": "Синтез аммиака",
    "ccgt": "Парогазовая установка", "nitric_acid": "Азотная кислота (Оствальд)",
    "contact_sulfuric": "Контактная серная кислота", "chloralkali": "Каустическая сода и хлор",
    "telegraph": "Телеграф", "aluminum": "Электролиз алюминия",
    "fertilizers": "Удобрения", "telephone": "Телефон", "diesel_locomotive": "Тепловоз",
    "radio": "Радио", "ships": "Суда", "tube_computer": "Ламповый компьютер",
    "submarine": "Подводные лодки", "automation": "Автоматизация", "car": "Автомобили",
    "era3_transition": "Переход к Эпохе III",
}
RESEARCH_EN = {
    "woodworking": "Woodworking", "stone_working": "Stoneworking",
    "copper_metallurgy": "Copper Metallurgy", "ore_enrichment": "Ore Enrichment",
    "water_wheel": "Water Wheel", "bronze_metallurgy": "Bronze Metallurgy",
    "transmission": "Mechanical Transmission", "iron_metallurgy": "Iron Metallurgy",
    "steam_newcomen": "Newcomen Steam Engine", "steam_watt": "Watt Steam Engine",
    "machine_tools_lathe": "Lathe", "machine_tools_drill": "Drill Press",
    "machine_tools_rolling": "Rolling Mill", "machine_tools_milling": "Milling Machine",
    "machine_tools_hammer": "Power Hammer", "machine_tools_grinding": "Grinding Machine",
    "chemistry_sulfuric": "Sulfuric Acid (Lead Chamber)", "transport_rails": "Rails and Sleepers",
    "chemistry_soda": "Soda Ash (Solvay)", "transport_minecart": "Minecarts",
    "chemistry_special_glass": "Special Glasses", "transport_conveyor": "Belt Conveyor",
    "transport_locomotive": "Small Steam Locomotive", "era2_transition": "Transition to Era II",
    "coke_making": "Coke Making", "steam_turbine": "Steam Turbine",
    "blast_furnace_coke": "Coke Blast Furnace", "electric_generator": "Electric Generator",
    "bessemer": "Bessemer Converter", "transformer": "Transformer",
    "power_network": "Power Networks", "open_hearth": "Open-Hearth Furnace",
    "electric_motor": "Electric Motor", "battery": "Batteries",
    "electric_arc_furnace": "Electric Arc Furnace", "heavy_rolling": "Heavy Rolling Mill",
    "air_separation": "Air Separation", "wire_drawing": "Wire Drawing",
    "water_electrolysis": "Water Electrolysis", "oil_refining": "Oil Distillation",
    "gas_turbine": "Gas Turbine", "ammonia": "Ammonia Synthesis",
    "ccgt": "Combined Cycle Plant", "nitric_acid": "Nitric Acid (Ostwald)",
    "contact_sulfuric": "Contact Sulfuric Acid", "chloralkali": "Chlor-Alkali",
    "telegraph": "Telegraph", "aluminum": "Aluminum Electrolysis",
    "fertilizers": "Fertilizers", "telephone": "Telephone", "diesel_locomotive": "Diesel Locomotive",
    "radio": "Radio", "ships": "Ships", "tube_computer": "Vacuum Tube Computer",
    "submarine": "Submarines", "automation": "Automation", "car": "Automobiles",
    "era3_transition": "Transition to Era III",
}

RESEARCH_DESC_RU = {
    "woodworking": "Основы обработки древесины: доски, инструменты, простые механизмы.",
    "stone_working": "Обработка камня: примитивные печи и первые машины.",
    "copper_metallurgy": "Плавка меди в горне с мехами. Медные слитки и детали.",
    "ore_enrichment": "Дробление породы и сепарация: куски породы → порошок → концентрат. Дробилка и сепаратор.",
    "water_wheel": "Механическая энергия из потока воды: 5–30 кВт при КПД 60%.",
    "bronze_metallurgy": "Сплав меди и олова: бронзовые инструменты и детали машин.",
    "transmission": "Валы, шестерни, ремни, шкивы и редукторы — передача вращения.",
    "iron_metallurgy": "Чугун из железных концентратов: домна и пудлингование в ковкое железо.",
    "steam_newcomen": "Атмосферный паровой двигатель: 5–10 кВт, КПД ~1%, запуск 20 минут.",
    "steam_watt": "Двигатель двойного действия с конденсатором: 20–100 кВт, КПД 5–8%.",
    "machine_tools_lathe": "Токарный станок: валы, оси, болты, гайки (точность 0.5 мм).",
    "machine_tools_drill": "Сверлильный станок: отверстия в металле и дереве.",
    "machine_tools_rolling": "Прокатный стан: листы, прутки, рельсы (заготовка 900 °C).",
    "machine_tools_milling": "Фрезерный станок: плоскости и пазы, требует смазки.",
    "machine_tools_hammer": "Механический молот: ковка крупных деталей.",
    "machine_tools_grinding": "Шлифовальный станок: финишная обработка, качество деталей.",
    "chemistry_sulfuric": "Камерный способ: обжиг пирита → SO₂ → свинцовая камера → H₂SO₄.",
    "transport_rails": "Рельсы, шпалы и стрелки для железных дорог.",
    "chemistry_soda": "Процесс Сольве: соль + аммиак + CO₂ → сода (замкнутый цикл).",
    "transport_minecart": "Вагонетки: грузовые, пассажирские, цистерны.",
    "chemistry_special_glass": "Термостойкое и оптическое стекло в тигельной печи.",
    "transport_conveyor": "Ленточный транспортёр: 1 м/с, привод от вала, сортировка.",
    "transport_locomotive": "Малый паровоз: уголь и вода, до 20 км/ч, буксирует 5 вагонеток.",
    "era2_transition": "Ключ к промышленной революции: сталь, турбины, электричество.",
    "coke_making": "Коксование угля без доступа воздуха: кокс, смола, светильный газ.",
    "steam_turbine": "Паровая турбина: 1–3 МПа, 100 кВт–10 МВт, КПД 15–25%.",
    "blast_furnace_coke": "Большая домна с горячим дутьём: 10 т чугуна в сутки.",
    "electric_generator": "Синхронный трёхфазный генератор: 50 Гц, 10 кВ.",
    "bessemer": "Конвертер Бессемера: продувка кислородом, чугун → сталь за 15–20 минут.",
    "transformer": "Повышение/понижение напряжения до 110 кВ: передача без потерь.",
    "power_network": "Кабели трёх классов, опоры, изоляторы, защита от КЗ.",
    "open_hearth": "Мартеновская печь: 1700 °C, переплав лома с рудой.",
    "electric_motor": "Асинхронный электродвигатель: 0.5–500 кВт, выход — вращение.",
    "battery": "Свинцово-кислотный аккумулятор: 100 А·ч, 12 В.",
    "electric_arc_furnace": "Электродуговая печь: 100 кВт, 2000 °C, точное легирование.",
    "heavy_rolling": "Тяжёлый прокатный стан: рельсы, балки, листы (валки изнашиваются).",
    "air_separation": "Криогенное разделение воздуха: азот и кислород.",
    "wire_drawing": "Волочение проволоки: медь, алюминий, сталь.",
    "water_electrolysis": "Электролиз воды: водород и кислород.",
    "oil_refining": "Перегонка нефти: бензин, керосин, дизель, мазут.",
    "gas_turbine": "Газовая турбина: 1–50 МВт, КПД 25–35%.",
    "ammonia": "Синтез Габера: 400–500 °C, 20–30 МПа, железный катализатор.",
    "ccgt": "Парогазовая установка: 50–500 МВт, КПД 45–50%.",
    "nitric_acid": "Оствальд: окисление аммиака на платино-родиевых сетках.",
    "contact_sulfuric": "Контактный способ: ванадиевый катализатор, 450 °C, до 98%.",
    "chloralkali": "Электролиз рассола: NaOH, хлор, водород (замкнутые циклы).",
    "telegraph": "Телеграф: медные провода, код Морзе, до 1000 блоков.",
    "aluminum": "Электролиз глинозёма в криолите при 960 °C: 13–15 МВт·ч/т.",
    "fertilizers": "Аммиачная селитра, суперфосфат, калийные удобрения.",
    "telephone": "Телефон: угольный микрофон, динамик, коммутатор.",
    "diesel_locomotive": "Тепловоз: дизель → генератор → тяговые двигатели, до 120 км/ч.",
    "radio": "Ламповый передатчик: связь до 10 000 блоков.",
    "ships": "Судовые турбины до 100 МВт, винт через редуктор.",
    "tube_computer": "Ламповый компьютер: 100–500 кВт, память на сердечниках до 100 КБ.",
    "submarine": "Дизель-электрические подводные лодки.",
    "automation": "Датчики → ламповый ПЛК → клапаны и двигатели: автоматические циклы.",
    "car": "Автомобили: бензиновые и дизельные, до 20 т.",
    "era3_transition": "Открывает суперкритические турбины и полупроводники (Эпоха III).",
}
RESEARCH_DESC_EN = {k: "Research node." for k in RESEARCH_RU}


def gen_lang():
    ru = {}
    en = {}
    # блоки пород
    for m in MINERALS:
        for r in RICHNESS:
            for ds in (False, True):
                bid = ("deepslate_" if ds else "") + f"{m}_porode_{r}"
                base_ru = RU_PORODE[m].split(" (")[0] if False else RU_PORODE[m]
                rl = {"poor": "бедная", "normal": "обычная", "rich": "богатая"}
                ru[f"block.{MODID}.{bid}"] = f"{base_ru} ({rl[r]})"
                en[f"block.{MODID}.{bid}"] = f"{'Deepslate ' if ds else ''}{EN_PORODE[m]} ({r.capitalize()})"
    # блоки машин и обшивки
    for bid, name in RU_BLOCKS.items():
        ru[f"block.{MODID}.{bid}"] = name
        en[f"block.{MODID}.{bid}"] = EN_BLOCKS[bid]
    # флюиды
    for fid, name in FLUIDS_RU.items():
        ru[f"fluid_type.{MODID}.{fid}"] = name
        en[f"fluid_type.{MODID}.{fid}"] = FLUIDS_EN[fid]
    # предметы (породы/порошки/концентраты по минералам)
    for m in MINERALS:
        mru = RU_PORODE[m].split(" (")[0]
        men = EN_PORODE[m]
        ru[f"item.{MODID}.{m}_rock"] = f"Кусок породы: {mru}"
        ru[f"item.{MODID}.{m}_powder"] = f"Порошок: {mru}"
        ru[f"item.{MODID}.{m}_concentrate"] = f"Концентрат: {mru}"
        en[f"item.{MODID}.{m}_rock"] = f"{men} Rock Chunk"
        en[f"item.{MODID}.{m}_powder"] = f"{men} Powder"
        en[f"item.{MODID}.{m}_concentrate"] = f"{men} Concentrate"
    for iid, name in RU_ITEMS.items():
        ru[f"item.{MODID}.{iid}"] = name
        en[f"item.{MODID}.{iid}"] = EN_ITEMS.get(iid, iid.replace("_", " ").title())
    # инструменты
    tools_ru = {"pickaxe": "Кирка", "axe": "Топор", "shovel": "Лопата", "hoe": "Мотыга",
                "hammer": "Молот", "shears": "Ножницы", "file": "Напильник",
                "chisel": "Зубило", "tongs": "Клещи", "saw": "Пила"}
    tools_en = {"pickaxe": "Pickaxe", "axe": "Axe", "shovel": "Shovel", "hoe": "Hoe",
                "hammer": "Hammer", "shears": "Shears", "file": "File",
                "chisel": "Chisel", "tongs": "Tongs", "saw": "Saw"}
    for t, tr in tools_ru.items():
        ru[f"item.{MODID}.bronze_{t}"] = f"Бронзовая {tr.lower()}"
        en[f"item.{MODID}.bronze_{t}"] = f"Bronze {tools_en[t]}"
    for t in ["hammer", "file", "chisel", "tongs", "saw"]:
        ru[f"item.{MODID}.iron_{t}"] = f"Железный {tools_ru[t].lower()}"
        en[f"item.{MODID}.iron_{t}"] = f"Iron {tools_en[t]}"
    # исследования
    for rid, name in RESEARCH_RU.items():
        ru[f"research.{MODID}.{rid}"] = name
        ru[f"research.{MODID}.{rid}.desc"] = RESEARCH_DESC_RU[rid]
        en[f"research.{MODID}.{rid}"] = RESEARCH_EN[rid]
        en[f"research.{MODID}.{rid}.desc"] = f"Unlocks {RESEARCH_EN[rid].lower()} recipes."
    # статусы, экран, сообщения
    ru.update({
        "itemGroup.terrafabrica": "Terra Fabrica",
        "tooltip.terrafabrica.porode_block": "Требует дробления и обогащения",
        "status.terrafabrica.0": "Ожидание", "status.terrafabrica.1": "Работает",
        "status.terrafabrica.2": "Нет энергии", "status.terrafabrica.3": "Нужно исследование",
        "status.terrafabrica.4": "Выход заполнен", "status.terrafabrica.5": "Холодно",
        "status.terrafabrica.6": "Нет флюида", "status.terrafabrica.7": "Низкое давление",
        "status.terrafabrica.8": "Структура не собрана",
        "screen.terrafabrica.research": "Древо исследований",
        "screen.terrafabrica.research.hint": "ЛКМ по доступному узлу — начать исследование. Перетаскивание — панорама.",
        "screen.terrafabrica.research.state": "Статус: %s",
        "screen.terrafabrica.research.prerequisites": "Требуется:",
        "screen.terrafabrica.research.cost": "Время: %s",
        "research_state.terrafabrica.locked": "Закрыт", "research_state.terrafabrica.available": "Доступен",
        "research_state.terrafabrica.in_progress": "В процессе", "research_state.terrafabrica.unlocked": "Открыт",
        "gui.terrafabrica.temperature": "Т: %s", "gui.terrafabrica.rotation": "Вращ: %s",
        "gui.terrafabrica.power": "Мощн: %s",
        "message.terrafabrica.recipe_locked": "Рецепт заблокирован: требуется исследование «%s»",
        "message.terrafabrica.research_missing_resources": "Не хватает ресурсов для «%s»",
        "message.terrafabrica.research_started": "Исследование начато: %s",
        "message.terrafabrica.research_completed": "Исследование завершено: %s!",
        "message.terrafabrica.phone_no_connection": "Нет соединения: рядом нет другого телефона",
    })
    en.update({
        "itemGroup.terrafabrica": "Terra Fabrica",
        "tooltip.terrafabrica.porode_block": "Requires crushing and enrichment",
        "status.terrafabrica.0": "Idle", "status.terrafabrica.1": "Working",
        "status.terrafabrica.2": "No power", "status.terrafabrica.3": "Research required",
        "status.terrafabrica.4": "Output full", "status.terrafabrica.5": "Too cold",
        "status.terrafabrica.6": "No fluid", "status.terrafabrica.7": "Low pressure",
        "status.terrafabrica.8": "Structure incomplete",
        "screen.terrafabrica.research": "Research Tree",
        "screen.terrafabrica.research.hint": "LMB on an available node to research. Drag to pan.",
        "screen.terrafabrica.research.state": "State: %s",
        "screen.terrafabrica.research.prerequisites": "Requires:",
        "screen.terrafabrica.research.cost": "Time: %s",
        "research_state.terrafabrica.locked": "Locked", "research_state.terrafabrica.available": "Available",
        "research_state.terrafabrica.in_progress": "In progress", "research_state.terrafabrica.unlocked": "Unlocked",
        "gui.terrafabrica.temperature": "T: %s", "gui.terrafabrica.rotation": "Rot: %s",
        "gui.terrafabrica.power": "P: %s",
        "message.terrafabrica.recipe_locked": "Recipe locked: requires research \"%s\"",
        "message.terrafabrica.research_missing_resources": "Not enough resources for \"%s\"",
        "message.terrafabrica.research_started": "Research started: %s",
        "message.terrafabrica.research_completed": "Research completed: %s!",
        "message.terrafabrica.phone_no_connection": "No connection: no other telephone nearby",
    })
    # руководство: заголовки глав, строки текста и подписи экрана
    ru.update(GUIDE_UI_RU)
    en.update(GUIDE_UI_EN)
    for cid, ru_title, en_title, lines in GUIDE_CHAPTERS:
        ru[f"guide.terrafabrica.{cid}.title"] = ru_title
        en[f"guide.terrafabrica.{cid}.title"] = en_title
        for idx, (ru_line, en_line) in enumerate(lines, 1):
            ru[f"guide.terrafabrica.{cid}.line.{idx}"] = ru_line
            en[f"guide.terrafabrica.{cid}.line.{idx}"] = en_line
    actual = {cid: len(lines) for cid, _, _, lines in GUIDE_CHAPTERS}
    assert actual == GUIDE_LINE_COUNTS, (
        "главы руководства разошлись с GuideContent.java: " + repr(actual))
    # субтитры звуковых событий (sounds.json ссылается на эти ключи)
    for eid in SOUND_FILES:
        ru[f"subtitle.{MODID}.{eid}"] = SUBTITLES_RU[eid]
        en[f"subtitle.{MODID}.{eid}"] = SUBTITLES_EN[eid]
    os.makedirs(os.path.join(ASSETS, "lang"), exist_ok=True)
    wj(os.path.join(ASSETS, "lang", "ru_ru.json"), ru)
    wj(os.path.join(ASSETS, "lang", "en_us.json"), en)
    print(f"[data] lang: {len(ru)} ru / {len(en)} en")


# ---------------------------------------------------------------------------
# Блок-состояния и модели
# ---------------------------------------------------------------------------

def gen_blockstates_and_models():
    bs_dir = os.path.join(ASSETS, "blockstates")
    models_dir = os.path.join(ASSETS, "models", "block")
    os.makedirs(bs_dir, exist_ok=True)
    os.makedirs(models_dir, exist_ok=True)
    # породы и обшивки: cube_all
    cube_blocks = []
    for m in MINERALS:
        for r in RICHNESS:
            for ds in (False, True):
                bid = ("deepslate_" if ds else "") + f"{m}_porode_{r}"
                cube_blocks.append((bid, f"block/porode/{bid}"))
    for c in CASINGS:
        cube_blocks.append((c, f"block/{c}"))
    cube_blocks += [("heat_resistant_glass", "block/heat_resistant_glass"),
                    ("optical_glass", "block/optical_glass")]
    # Стекло рисуется в translucent-проходе (как ванильное), иначе фон за ним не виден.
    translucent = {"heat_resistant_glass", "optical_glass"}
    for bid, tex in cube_blocks:
        wj(os.path.join(bs_dir, bid + ".json"), {"variants": {"": {"model": f"{MODID}:block/{bid}"}}})
        model = {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MODID}:{tex}"}}
        if bid in translucent:
            model["render_type"] = "minecraft:translucent"
        wj(os.path.join(models_dir, bid + ".json"), model)
    # машины: ориентируемый куб с фронтом-архетипом
    for machine in MACHINES:
        wj(os.path.join(bs_dir, machine + ".json"), {
            "variants": {
                "facing=north": {"model": f"{MODID}:block/{machine}"},
                "facing=east": {"model": f"{MODID}:block/{machine}", "y": 90},
                "facing=south": {"model": f"{MODID}:block/{machine}", "y": 180},
                "facing=west": {"model": f"{MODID}:block/{machine}", "y": 270},
            }})
        wj(os.path.join(models_dir, machine + ".json"), {
            "parent": "minecraft:block/cube",
            "textures": {
                "particle": f"{MODID}:block/machine/machine_side",
                "down": f"{MODID}:block/machine/machine_top",
                "up": f"{MODID}:block/machine/machine_top",
                "north": f"{MODID}:block/machine/{ARCHETYPE[machine]}",
                "south": f"{MODID}:block/machine/machine_side",
                "east": f"{MODID}:block/machine/machine_side",
                "west": f"{MODID}:block/machine/machine_side",
            }})
    # Блоки жидкостей: один вариант на все уровни (как minecraft:blockstates/water.json).
    # Модель нужна только ради частиц — поверхность жидкости рисует FluidRenderer
    # по текстурaм, которые регистрирует TFClientEvents.
    for fid in FLUID_IDS:
        wj(os.path.join(bs_dir, fid + '.json'),
           {'variants': {'': {'model': f'{MODID}:block/{fid}'}}})
        wj(os.path.join(models_dir, fid + '.json'),
           {'textures': {'particle': f'{MODID}:block/fluid/{fid}_still'}})
    print(f"[data] blockstates/models: {len(cube_blocks)} кубов + {len(MACHINES)} машин "
          f"+ {len(FLUID_IDS)} жидкостей")


# ---------------------------------------------------------------------------
# Модели предметов
# ---------------------------------------------------------------------------

TOOLS = ["pickaxe", "axe", "shovel", "hoe", "hammer", "shears", "file", "chisel", "tongs", "saw"]

def gen_item_models():
    models_dir = os.path.join(ASSETS, "models", "item")
    os.makedirs(models_dir, exist_ok=True)
    items = set()
    for m in MINERALS:
        items.update([f"{m}_rock", f"{m}_powder", f"{m}_concentrate"])
    items.update(RU_ITEMS.keys())
    items.update(["bronze_gear", "iron_gear"])
    for t in TOOLS:
        items.add(f"bronze_{t}")
    for t in ["hammer", "file", "chisel", "tongs", "saw"]:
        items.add(f"iron_{t}")
    items.add("titanium_alloy_ingot")
    for iid in items:
        parent = "minecraft:item/handheld" if (iid.startswith("bronze_") or iid.startswith("iron_")) \
            else "minecraft:item/generated"
        wj(os.path.join(models_dir, iid + ".json"),
           {"parent": parent, "textures": {"layer0": f"{MODID}:item/{iid}"}})
    # модели блоков для BlockItem'ов
    for m in MINERALS:
        for r in RICHNESS:
            for ds in (False, True):
                bid = ("deepslate_" if ds else "") + f"{m}_porode_{r}"
                wj(os.path.join(models_dir, bid + ".json"),
                   {"parent": f"{MODID}:block/{bid}"})
    for c in CASINGS:
        wj(os.path.join(models_dir, c + ".json"), {"parent": f"{MODID}:block/{c}"})
    # стекло тоже имеет BlockItem — модель предмета обязательна
    for g in ("heat_resistant_glass", "optical_glass"):
        wj(os.path.join(models_dir, g + ".json"), {"parent": f"{MODID}:block/{g}"})
    for machine in MACHINES:
        wj(os.path.join(models_dir, machine + ".json"), {"parent": f"{MODID}:block/{machine}"})
    print(f"[data] item models: {len(items) + 162 + len(CASINGS) + 2 + len(MACHINES)}")


# ---------------------------------------------------------------------------
# Loot tables (породы)
# ---------------------------------------------------------------------------

def gen_loot():
    loot_dir = os.path.join(DATA, "loot_table", "blocks")
    os.makedirs(loot_dir, exist_ok=True)
    base = {"poor": 1, "normal": 2, "rich": 3}
    for m in MINERALS:
        for r in RICHNESS:
            for ds in (False, True):
                bid = ("deepslate_" if ds else "") + f"{m}_porode_{r}"
                table = {
                    "type": "minecraft:block",
                    "pools": [{
                        "bonus_rolls": 0.0,
                        "conditions": [{"condition": "minecraft:survives_explosion"}],
                        "entries": [{
                            "type": "minecraft:alternatives",
                            "children": [
                                {
                                    "type": "minecraft:item",
                                    "conditions": [{
                                        "condition": "minecraft:match_tool",
                                        "predicate": {"predicates": {"minecraft:enchantments": [{
                                            "enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}},
                                    }],
                                    "name": f"{MODID}:{bid}",
                                },
                                {
                                    "type": "minecraft:item",
                                    "name": f"{MODID}:{m}_rock",
                                    "functions": [
                                        {"function": "minecraft:set_count",
                                         "count": {"type": "minecraft:constant", "value": base[r]}},
                                        {"function": "minecraft:apply_bonus",
                                         "enchantment": "minecraft:fortune",
                                         "formula": "minecraft:uniform_bonus_count",
                                         "parameters": {"bonusMultiplier": 1}},
                                    ],
                                },
                            ],
                        }],
                        "rolls": 1.0,
                    }],
                }
                wj(os.path.join(loot_dir, bid + ".json"), table)

    # Обшивки и машины: обычный дроп самого блока.
    def self_drop(bid, silk_only=False):
        entry = {"type": "minecraft:item", "name": f"{MODID}:{bid}"}
        if silk_only:
            entry["conditions"] = [{
                "condition": "minecraft:match_tool",
                "predicate": {"predicates": {"minecraft:enchantments": [{
                    "enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}},
            }]
        wj(os.path.join(loot_dir, bid + ".json"), {
            "type": "minecraft:block",
            "pools": [{
                "bonus_rolls": 0.0,
                "conditions": [{"condition": "minecraft:survives_explosion"}],
                "entries": [entry],
                "rolls": 1.0,
            }],
        })

    for c in CASINGS:
        self_drop(c)
    for m in MACHINES:
        self_drop(m)
    # Стекло, как ванильное: выпадает только при «шёлковом касании».
    for g in ("heat_resistant_glass", "optical_glass"):
        self_drop(g, silk_only=True)
    print(f"[data] loot: {len(MINERALS) * 6 + len(CASINGS) + len(MACHINES) + 2}")


# ---------------------------------------------------------------------------
# Теги
# ---------------------------------------------------------------------------

def gen_tags():
    tags_dir = os.path.join(DATA, "tags")
    blocks = os.path.join(tags_dir, "block")
    items = os.path.join(tags_dir, "item")
    os.makedirs(blocks, exist_ok=True)
    os.makedirs(items, exist_ok=True)
    all_porodes = []
    porode_by_mineral = {}
    porode_by_rich = {"poor": [], "normal": [], "rich": []}
    for m in MINERALS:
        lst = []
        for r in RICHNESS:
            for ds in (False, True):
                bid = ("deepslate_" if ds else "") + f"{m}_porode_{r}"
                lst.append(f"{MODID}:{bid}")
                all_porodes.append(f"{MODID}:{bid}")
                porode_by_rich[r].append(f"{MODID}:{bid}")
        porode_by_mineral[m] = lst
        wj(os.path.join(blocks, "porodes", m + ".json"), {"replace": False, "values": lst})
    wj(os.path.join(blocks, "porodes.json"), {"replace": False, "values": all_porodes})
    for r, lst in porode_by_rich.items():
        wj(os.path.join(blocks, "porodes", r + ".json"), {"replace": False, "values": lst})
    # mineable/pickaxe: все блоки мода
    mineable = [f"{MODID}:{m}_porode_{r}" for m in MINERALS for r in RICHNESS]
    mineable += [f"{MODID}:deepslate_{m}_porode_{r}" for m in MINERALS for r in RICHNESS]
    mineable += [f"{MODID}:{c}" for c in CASINGS]
    mineable += [f"{MODID}:{m}" for m in MACHINES]
    mineable += [f"{MODID}:heat_resistant_glass", f"{MODID}:optical_glass"]
    mc_blocks = os.path.join(MC_DATA, "tags", "block")
    os.makedirs(os.path.join(mc_blocks, "mineable"), exist_ok=True)
    wj(os.path.join(mc_blocks, "mineable", "pickaxe.json"), {"replace": False, "values": mineable})
    # Уровни инструментов: породы копаются любым (как ванильная руда), обшивки и
    # машины требуют каменного топора-кирки, тяжёлое оборудование Эпохи II — железного.
    needs_stone = [f"{MODID}:{c}" for c in CASINGS] + [f"{MODID}:{m}" for m in MACHINES] \
        + [f"{MODID}:heat_resistant_glass", f"{MODID}:optical_glass"]
    needs_iron = [f"{MODID}:{m}" for m in (
        "blast_furnace", "bessemer_converter", "open_hearth_furnace", "electric_arc_furnace",
        "heavy_rolling_mill", "steam_turbine", "gas_turbine", "ccgt_plant",
        "electric_generator", "transformer", "substation", "circuit_breaker",
        "air_separation_unit", "ammonia_synthesizer", "aluminum_electrolyzer",
    ) if m in MACHINES]
    os.makedirs(mc_blocks, exist_ok=True)
    wj(os.path.join(mc_blocks, "needs_stone_tool.json"), {"replace": False, "values": needs_stone})
    wj(os.path.join(mc_blocks, "needs_iron_tool.json"), {"replace": False, "values": needs_iron})
    # предметные теги
    rocks = [f"{MODID}:{m}_rock" for m in MINERALS]
    powders = [f"{MODID}:{m}_powder" for m in MINERALS]
    concentrates = [f"{MODID}:{m}_concentrate" for m in MINERALS]
    wj(os.path.join(items, "rocks.json"), {"replace": False, "values": rocks})
    wj(os.path.join(items, "powders.json"), {"replace": False, "values": powders})
    wj(os.path.join(items, "concentrates.json"), {"replace": False, "values": concentrates})
    ingots = ["tin", "bronze", "lead", "pig_iron", "brass", "steel", "stainless_steel", "superalloy",
              "aluminum", "chromium", "nickel", "zinc", "molybdenum", "tungsten", "manganese",
              "magnesium", "platinum", "cobalt", "niobium", "titanium", "vanadium", "titanium_alloy"]
    for ingot in ingots:
        wj(os.path.join(items, "ingots", ingot + ".json"),
           {"replace": False, "values": [f"{MODID}:{ingot}_ingot"]})
    wj(os.path.join(items, "ingots.json"), {"replace": False,
        "values": [f"{MODID}:{i}_ingot" for i in ingots]})
    plates = ["copper", "iron", "steel", "aluminum", "tin", "bronze"]
    wj(os.path.join(items, "plates.json"), {"replace": False,
        "values": [f"{MODID}:{p}_plate" for p in plates]})
    wires = ["copper", "aluminum", "steel"]
    wj(os.path.join(items, "wires.json"), {"replace": False,
        "values": [f"{MODID}:{w}_wire" for w in wires]})
    wj(os.path.join(items, "tools.json"), {"replace": False,
        "values": [f"{MODID}:bronze_{t}" for t in TOOLS]
                  + [f"{MODID}:iron_{t}" for t in ["hammer", "file", "chisel", "tongs", "saw"]]})
    # теги флюидов (газы)
    gases = ["steam_low", "steam_high", "ammonia", "chlorine", "hydrogen", "oxygen", "nitrogen",
             "carbon_dioxide", "natural_gas", "sulfur_dioxide", "coal_gas"]
    fluid_tags = os.path.join(tags_dir, "fluid")
    os.makedirs(fluid_tags, exist_ok=True)
    wj(os.path.join(fluid_tags, "gases.json"), {"replace": False,
        "values": [f"{MODID}:{g}" for g in gases]})
    print("[data] tags")


# ---------------------------------------------------------------------------
# Рецепты
# ---------------------------------------------------------------------------

# Часть материалов мод намеренно не дублирует, а берёт из ванили: так не ломаются
# ванильные рецепты и дерево прогрессии опирается на знакомые предметы.
VANILLA_ITEMS = {"copper_ingot", "iron_ingot", "gold_ingot", "glass", "sand",
                 "coal", "charcoal", "clay_ball", "stick", "sugar"}


def shaped(pattern, keys, result, count=1, research=None):
    r = {"type": f"{MODID}:research_shaped" if research else "minecraft:crafting_shaped",
         "pattern": pattern, "key": keys,
         "result": {"id": f"{MODID}:{result}", "count": count}}
    if research:
        r["research"] = research
    return r


def shapeless(ingredients, result, count=1, research=None):
    r = {"type": f"{MODID}:research_shapeless" if research else "minecraft:crafting_shapeless",
         "ingredients": ingredients,
         "result": {"id": f"{MODID}:{result}", "count": count}}
    if research:
        r["research"] = research
    return r


def machine(mtype, ingredients, results, energy=0, ticks=100, research=None,
            fluid_in=None, fluid_out=None, min_temp=0, counts=None):
    def item_ref(i):
        if ":" in i:
            return i
        return ("minecraft:" if i in VANILLA_ITEMS else f"{MODID}:") + i
    r = {"type": f"{MODID}:{mtype}",
         "ingredients": [{"item": item_ref(i)} for i in ingredients],
         "results": [{"id": item_ref(i), "count": c} for i, c in results],
         "energy": energy, "ticks": ticks}
    if research:
        r["research"] = research
    if fluid_in:
        # FluidStack.CODEC (NeoForge 1.21.1): {"id": ..., "amount": ...},
        # а не {"fluid": ...} — второй вариант относится к FluidIngredient
        # и роняет загрузку рецепта (No key id in MapLike).
        r["fluid_in"] = {"id": f"{MODID}:{fluid_in[0]}", "amount": fluid_in[1]}
    if fluid_out:
        r["fluid_out"] = {"id": f"{MODID}:{fluid_out[0]}", "amount": fluid_out[1]}
    if min_temp:
        r["min_temperature"] = min_temp
    if counts:
        r["counts"] = counts
    return r


def gen_recipes():
    rec_dir = os.path.join(DATA, "recipe")
    craft = os.path.join(rec_dir, "crafting")
    os.makedirs(craft, exist_ok=True)
    n = 0

    def wr(subdir, name, recipe):
        nonlocal n
        wj(os.path.join(rec_dir, subdir, name + ".json"), recipe)
        n += 1

    # --- Крафты Эпохи I ---
    wr("crafting", "guide_book", shapeless(
        [{"item": "minecraft:book"}, {"item": "minecraft:paper"}], "guide_book"))
    wr("crafting", "primitive_furnace", shaped(
        ["CCC", "CgC", "CCC"],
        {"C": {"item": "minecraft:cobblestone"}, "g": {"item": "minecraft:clay_ball"}},
        "primitive_furnace", research="stone_working"))
    wr("crafting", "bellows", shaped(
        ["LL ", "PP ", "LL "],
        {"L": {"item": "minecraft:leather"}, "P": {"item": "minecraft:oak_planks"}},
        "bellows", research="woodworking"))
    wr("crafting", "bloomery", shaped(
        ["CBC", "C C", "CCC"],
        {"C": {"item": "minecraft:cobblestone"}, "B": {"item": f"{MODID}:bronze_plate"}},
        "bloomery", research="copper_metallurgy"))
    wr("crafting", "crusher", shaped(
        ["CGC", "CBC", "CPC"],
        {"C": {"item": "minecraft:cobblestone"}, "G": {"item": f"{MODID}:bronze_gear"},
         "B": {"item": f"{MODID}:bronze_ingot"}, "P": {"item": "minecraft:oak_planks"}},
        "crusher", research="ore_enrichment"))
    wr("crafting", "separator", shaped(
        ["PGP", "PBP", "PLP"],
        {"P": {"item": "minecraft:oak_planks"}, "G": {"item": f"{MODID}:bronze_gear"},
         "B": {"item": f"{MODID}:bronze_ingot"}, "L": {"item": f"{MODID}:lead_ingot"}},
        "separator", research="ore_enrichment"))
    wr("crafting", "crucible_furnace", shaped(
        ["RB", "RC"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "B": {"item": f"{MODID}:bronze_ingot"},
         "C": {"item": "minecraft:clay_ball"}},
        "crucible_furnace", research="bronze_metallurgy"))
    wr("crafting", "small_blast_furnace", shaped(
        ["RRR", "RBR", "RRR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "B": {"item": f"{MODID}:bellows"}},
        "small_blast_furnace", research="iron_metallurgy"))
    wr("crafting", "puddling_furnace", shaped(
        ["RRR", "RSR", "RRR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "S": {"item": f"{MODID}:shaft"}},
        "puddling_furnace", research="iron_metallurgy"))
    # детали
    wr("crafting", "bronze_gear", shapeless(
        [{"item": f"{MODID}:bronze_ingot"}] * 4, "bronze_gear", research="bronze_metallurgy"))
    wr("crafting", "iron_gear", shapeless(
        [{"item": "minecraft:iron_ingot"}] * 4, "iron_gear", research="transmission"))
    wr("crafting", "gear_block", shapeless([{"item": f"{MODID}:iron_gear"}], "gear",
                                           research="transmission"))
    wr("crafting", "shaft", shapeless([{"item": f"{MODID}:bronze_ingot"}], "shaft",
                                      research="transmission"))
    wr("crafting", "belt", shapeless([{"item": "minecraft:leather"}] * 3, "belt",
                                     research="transmission"))
    wr("crafting", "pulley", shapeless(
        [{"item": f"{MODID}:bronze_ingot"}, {"item": f"{MODID}:belt"}], "pulley",
        research="transmission"))
    wr("crafting", "gearbox", shaped(
        ["G G", "G G", "GMG"],
        {"G": {"item": f"{MODID}:bronze_gear"}, "M": {"item": f"{MODID}:machine_frame"}},
        "gearbox", research="transmission"))
    wr("crafting", "water_wheel", shaped(
        ["SPS", "SGS", "SPS"],
        {"S": {"item": f"{MODID}:shaft"}, "P": {"item": "minecraft:oak_planks"},
         "G": {"item": f"{MODID}:bronze_gear"}},
        "water_wheel", research="water_wheel"))
    wr("crafting", "windmill", shaped(
        [" L ", "PSP", " L "],
        {"L": {"item": "minecraft:leather"}, "P": {"item": "minecraft:oak_planks"},
         "S": {"item": f"{MODID}:shaft"}},
        "windmill", research="water_wheel"))
    # компоненты паровых двигателей
    wr("crafting", "boiler_part", shaped(
        ["III", "I I", "III"],
        {"I": {"item": f"{MODID}:iron_plate"}}, "boiler_part", research="iron_metallurgy"))
    wr("crafting", "cylinder", shaped(
        ["I I", "I I", "I I"],
        {"I": {"item": "minecraft:iron_ingot"}}, "cylinder", research="iron_metallurgy"))
    wr("crafting", "condenser", shaped(
        ["ICI", "I I", "ICI"],
        {"I": {"item": "minecraft:iron_ingot"}, "C": {"item": "minecraft:copper_ingot"}},
        "condenser", research="iron_metallurgy"))
    wr("crafting", "balance_beam", shaped(
        ["  I", " WW", "I  "],
        {"I": {"item": "minecraft:iron_ingot"}, "W": {"item": "minecraft:oak_planks"}},
        "balance_beam", research="iron_metallurgy"))
    wr("crafting", "pump", shaped(
        ["I I", "ILI", "III"],
        {"I": {"item": "minecraft:iron_ingot"}, "L": {"item": "minecraft:leather"}},
        "pump", research="iron_metallurgy"))
    wr("crafting", "flywheel", shaped(
        [" I ", "I I", " I "],
        {"I": {"item": "minecraft:iron_ingot"}}, "flywheel", research="transmission"))
    wr("crafting", "steam_chest", shaped(
        ["ICI", "I I", " I "],
        {"I": {"item": "minecraft:iron_ingot"}, "C": {"item": "minecraft:copper_ingot"}},
        "steam_chest", research="steam_watt"))
    wr("crafting", "governor", shaped(
        ["C ", "IB", "C "],
        {"C": {"item": "minecraft:copper_ingot"}, "I": {"item": "minecraft:iron_ingot"},
         "B": {"item": f"{MODID}:bronze_ingot"}},
        "governor", research="steam_watt"))
    wr("crafting", "piston", shaped(
        ["III", "IRI", "III"],
        {"I": {"item": "minecraft:iron_ingot"}, "R": {"item": f"{MODID}:iron_rod"}},
        "piston", research="machine_tools_lathe"))
    wr("crafting", "steam_casing", shaped(
        ["IPI", "P P", "IPI"],
        {"I": {"item": f"{MODID}:iron_plate"}, "P": {"item": "minecraft:cobblestone"}},
        "steam_casing", 2, research="steam_newcomen"))
    wr("crafting", "steam_engine_newcomen", shaped(
        ["SBS", "CYP", "SRS"],
        {"S": {"item": f"{MODID}:steam_casing"}, "B": {"item": f"{MODID}:boiler_part"},
         "C": {"item": f"{MODID}:cylinder"}, "Y": {"item": f"{MODID}:condenser"},
         "P": {"item": f"{MODID}:pump"}, "R": {"item": f"{MODID}:balance_beam"}},
        "steam_engine_newcomen", research="steam_newcomen"))
    wr("crafting", "steam_engine_watt", shaped(
        ["SBS", "CYS", "SGS"],
        {"S": {"item": f"{MODID}:steam_casing"}, "B": {"item": f"{MODID}:boiler_part"},
         "C": {"item": f"{MODID}:cylinder"}, "Y": {"item": f"{MODID}:condenser"},
         "G": {"item": f"{MODID}:governor"}},
        "steam_engine_watt", research="steam_watt"))
    # станки
    wr("crafting", "lathe", shaped(
        ["IFI", "FSF", "IGI"],
        {"I": {"item": f"{MODID}:iron_plate"}, "F": {"item": f"{MODID}:machine_frame"},
         "S": {"item": f"{MODID}:shaft"}, "G": {"item": f"{MODID}:iron_gear"}},
        "lathe", research="machine_tools_lathe"))
    wr("crafting", "drill_press", shaped(
        ["I I", "GSG", " F "],
        {"I": {"item": "minecraft:iron_ingot"}, "G": {"item": f"{MODID}:iron_gear"},
         "S": {"item": f"{MODID}:shaft"}, "F": {"item": f"{MODID}:machine_frame"}},
        "drill_press", research="machine_tools_drill"))
    wr("crafting", "milling_machine", shaped(
        ["IFI", "GSG", "IFI"],
        {"I": {"item": f"{MODID}:iron_plate"}, "F": {"item": f"{MODID}:machine_frame"},
         "G": {"item": f"{MODID}:iron_gear"}, "S": {"item": f"{MODID}:shaft"}},
        "milling_machine", research="machine_tools_milling"))
    wr("crafting", "grinding_machine", shaped(
        ["STS", "GSG", " F "],
        {"S": {"item": "minecraft:stone"}, "T": {"item": "minecraft:smooth_stone"},
         "G": {"item": f"{MODID}:iron_gear"}, "F": {"item": f"{MODID}:machine_frame"}},
        "grinding_machine", research="machine_tools_grinding"))
    wr("crafting", "rolling_mill", shaped(
        ["IFI", "WSW", "IGI"],
        {"I": {"item": f"{MODID}:iron_plate"}, "F": {"item": f"{MODID}:flywheel"},
         "W": {"item": f"{MODID}:iron_rod"}, "S": {"item": f"{MODID}:shaft"},
         "G": {"item": f"{MODID}:iron_gear"}},
        "rolling_mill", research="machine_tools_rolling"))
    wr("crafting", "power_hammer", shaped(
        ["III", "ISI", "IGI"],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": f"{MODID}:shaft"},
         "G": {"item": f"{MODID}:iron_gear"}},
        "power_hammer", research="machine_tools_hammer"))
    # химия Эпохи I
    wr("crafting", "pyrite_roaster", shaped(
        ["RRR", "RIR", "RRR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "I": {"item": f"{MODID}:iron_plate"}},
        "pyrite_roaster", research="chemistry_sulfuric"))
    wr("crafting", "lead_casing", shaped(
        ["LL ", "LI ", "   "],
        {"L": {"item": f"{MODID}:lead_ingot"}, "I": {"item": "minecraft:iron_ingot"}},
        "lead_casing", 2, research="chemistry_sulfuric"))
    wr("crafting", "lead_chamber", shaped(
        ["LGL", "LIL", "LGL"],
        {"L": {"item": f"{MODID}:lead_casing"}, "G": {"item": "minecraft:glass"},
         "I": {"item": f"{MODID}:iron_plate"}},
        "lead_chamber", research="chemistry_sulfuric"))
    wr("crafting", "absorber", shaped(
        ["LGL", "LIL", "LLL"],
        {"L": {"item": f"{MODID}:lead_casing"}, "G": {"item": "minecraft:glass"},
         "I": {"item": f"{MODID}:iron_plate"}},
        "absorber", research="chemistry_sulfuric"))
    wr("crafting", "reactor_casing", shaped(
        ["IPI", "P P", "IPI"],
        {"I": {"item": f"{MODID}:iron_plate"}, "P": {"item": "minecraft:cobblestone"}},
        "reactor_casing", 2, research="chemistry_soda"))
    wr("crafting", "solvay_reactor", shaped(
        ["RGR", "RPR", "RRR"],
        {"R": {"item": f"{MODID}:reactor_casing"}, "G": {"item": "minecraft:glass"},
         "P": {"item": f"{MODID}:pump"}},
        "solvay_reactor", research="chemistry_soda"))
    wr("crafting", "calcination_furnace", shaped(
        ["RR", "CR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "C": {"item": "minecraft:cobblestone"}},
        "calcination_furnace", research="chemistry_soda"))
    # транспорт Эпохи I
    wr("crafting", "rail", shaped(
        ["I I", "I I", "I I"],
        {"I": {"item": "minecraft:iron_ingot"}}, "rail", 4, research="transport_rails"))
    wr("crafting", "sleeper", shapeless([{"item": "minecraft:oak_planks"}], "sleeper", 2,
                                        research="transport_rails"))
    wr("crafting", "wheel", shaped(
        [" I ", "ISI", " I "],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": f"{MODID}:shaft"}},
        "wheel", research="transport_minecart"))
    wr("crafting", "wagon_body", shaped(
        ["IPI", "IPI", "IPI"],
        {"I": {"item": f"{MODID}:iron_plate"}, "P": {"item": "minecraft:oak_planks"}},
        "wagon_body", research="transport_minecart"))
    wr("crafting", "minecart", shaped(
        [" W ", "WBW", " W "],
        {"W": {"item": f"{MODID}:wheel"}, "B": {"item": f"{MODID}:wagon_body"}},
        "minecart", research="transport_minecart"))
    wr("crafting", "conveyor", shaped(
        ["PPP", "PLP", "PPP"],
        {"P": {"item": "minecraft:oak_planks"}, "L": {"item": "minecraft:leather"}},
        "conveyor", 10, research="transport_conveyor"))
    wr("crafting", "steam_locomotive", shaped(
        ["BWB", "CWC", "BWB"],
        {"B": {"item": f"{MODID}:boiler_part"}, "W": {"item": f"{MODID}:wheel"},
         "C": {"item": f"{MODID}:cylinder"}},
        "steam_locomotive", research="transport_locomotive"))
    # инструменты
    for tool, pat in [("pickaxe", ["BBB", " S ", " S "]), ("axe", ["BB ", "BS ", " S "]),
                      ("shovel", [" B ", " S ", " S "]), ("hoe", ["BB ", " S ", " S "])]:
        wr("crafting", f"bronze_{tool}", shaped(
            pat, {"B": {"item": f"{MODID}:bronze_ingot"}, "S": {"item": "minecraft:stick"}},
            f"bronze_{tool}", research="bronze_metallurgy"))
    wr("crafting", "bronze_hammer", shaped(
        ["BBB", "BBB", " S "],
        {"B": {"item": f"{MODID}:bronze_ingot"}, "S": {"item": "minecraft:stick"}},
        "bronze_hammer", research="bronze_metallurgy"))
    wr("crafting", "bronze_tongs", shaped(
        ["B B", "B B", " S "],
        {"B": {"item": f"{MODID}:bronze_ingot"}, "S": {"item": "minecraft:stick"}},
        "bronze_tongs", research="bronze_metallurgy"))
    wr("crafting", "bronze_shears", shaped(
        [" B", "B "],
        {"B": {"item": f"{MODID}:bronze_ingot"}},
        "bronze_shears", research="bronze_metallurgy"))
    wr("crafting", "bronze_file", shaped(
        ["BB", "BB", " S"],
        {"B": {"item": f"{MODID}:bronze_ingot"}, "S": {"item": "minecraft:stick"}},
        "bronze_file", research="bronze_metallurgy"))
    wr("crafting", "bronze_chisel", shaped(
        ["B", "B", "S"],
        {"B": {"item": f"{MODID}:bronze_ingot"}, "S": {"item": "minecraft:stick"}},
        "bronze_chisel", research="bronze_metallurgy"))
    wr("crafting", "bronze_saw", shaped(
        ["BBB", " SS"],
        {"B": {"item": f"{MODID}:bronze_ingot"}, "S": {"item": "minecraft:stick"}},
        "bronze_saw", research="bronze_metallurgy"))
    wr("crafting", "iron_hammer", shaped(
        ["III", "III", " S "],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": "minecraft:stick"}},
        "iron_hammer", research="iron_metallurgy"))
    wr("crafting", "iron_file", shaped(
        ["II", "II", " S"],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": "minecraft:stick"}},
        "iron_file", research="iron_metallurgy"))
    wr("crafting", "iron_chisel", shaped(
        ["I", "I", "S"],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": "minecraft:stick"}},
        "iron_chisel", research="iron_metallurgy"))
    wr("crafting", "iron_tongs", shaped(
        ["I I", " S "],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": "minecraft:stick"}},
        "iron_tongs", research="iron_metallurgy"))
    wr("crafting", "iron_saw", shaped(
        ["III", " SS"],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": "minecraft:stick"}},
        "iron_saw", research="iron_metallurgy"))
    # приборы
    wr("crafting", "thermometer", shaped(
        [" G ", "GCG"],
        {"G": {"item": "minecraft:glass"}, "C": {"item": "minecraft:copper_ingot"}},
        "thermometer", research="copper_metallurgy"))
    wr("crafting", "tachometer", shaped(
        ["I ", "GR"],
        {"I": {"item": "minecraft:iron_ingot"}, "G": {"item": f"{MODID}:bronze_gear"},
         "R": {"item": "minecraft:redstone"}},
        "tachometer", research="transmission"))
    wr("crafting", "multimeter", shaped(
        ["IWI", "IGI"],
        {"I": {"item": "minecraft:iron_ingot"}, "W": {"item": f"{MODID}:copper_wire"},
         "G": {"item": "minecraft:glass"}},
        "multimeter", research="electric_generator"))
    wr("crafting", "geiger_counter", shaped(
        ["IWI", "IGI"],
        {"I": {"item": "minecraft:iron_ingot"}, "W": {"item": f"{MODID}:copper_wire"},
         "G": {"item": "minecraft:glass"}},
        "geiger_counter", research="era2_transition"))
    # --- Крафты Эпохи II ---
    wr("crafting", "coke_oven", shaped(
        ["RRR", "RIR", "RRR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "I": {"item": f"{MODID}:iron_plate"}},
        "coke_oven", research="coke_making"))
    wr("crafting", "blast_furnace", shaped(
        ["RRR", "RPR", "RRR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "P": {"item": f"{MODID}:pump"}},
        "blast_furnace", research="blast_furnace_coke"))
    wr("crafting", "bessemer_converter", shaped(
        ["RRR", "RVR", "RRR"],
        {"R": {"item": f"{MODID}:reactor_casing"}, "V": {"item": f"{MODID}:valve"}},
        "bessemer_converter", research="bessemer"))
    wr("crafting", "open_hearth_furnace", shaped(
        ["RRR", "RSR", "RRR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "S": {"item": f"{MODID}:steel_ingot"}},
        "open_hearth_furnace", research="open_hearth"))
    wr("crafting", "electric_arc_furnace", shaped(
        ["RRR", "RER", "RGR"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "E": {"item": f"{MODID}:graphite_electrode"},
         "G": {"item": f"{MODID}:copper_coil"}},
        "electric_arc_furnace", research="electric_arc_furnace"))
    wr("crafting", "heavy_rolling_mill", shaped(
        ["ISI", "FSF", "IGI"],
        {"I": {"item": f"{MODID}:steel_plate"}, "S": {"item": f"{MODID}:shaft"},
         "F": {"item": f"{MODID}:flywheel"}, "G": {"item": f"{MODID}:gearbox"}},
        "heavy_rolling_mill", research="heavy_rolling"))
    wr("crafting", "wire_drawing_machine", shaped(
        ["IPI", "GPG", "IPI"],
        {"I": {"item": f"{MODID}:steel_plate"}, "P": {"item": f"{MODID}:piston"},
         "G": {"item": f"{MODID}:iron_gear"}},
        "wire_drawing_machine", research="wire_drawing"))
    wr("crafting", "weight_dispenser", shaped(
        ["ICI", "FPF", "ICI"],
        {"I": {"item": "minecraft:iron_ingot"}, "C": {"item": "minecraft:copper_ingot"},
         "F": {"item": f"{MODID}:precision_part"}, "P": {"item": f"{MODID}:piston"}},
        "weight_dispenser", research="electric_arc_furnace"))
    # энергетика
    wr("crafting", "iron_casing", shaped(
        ["IPI", "P P", "IPI"],
        {"I": {"item": f"{MODID}:iron_plate"}, "P": {"item": "minecraft:stone"}},
        "iron_casing", 2, research="electric_generator"))
    wr("crafting", "rotor", shaped(
        [" I ", "ISI", " I "],
        {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": f"{MODID}:shaft"}},
        "rotor", research="electric_generator"))
    wr("crafting", "stator", shaped(
        ["IWI", "I I", "IWI"],
        {"I": {"item": "minecraft:iron_ingot"}, "W": {"item": f"{MODID}:copper_wire"}},
        "stator", research="electric_generator"))
    wr("crafting", "copper_coil", shaped(
        ["WWW", "WRW", "WWW"],
        {"W": {"item": f"{MODID}:copper_wire"}, "R": {"item": f"{MODID}:iron_rod"}},
        "copper_coil", research="electric_generator"))
    wr("crafting", "steam_turbine", shaped(
        ["SFS", "SRS", "SFS"],
        {"S": {"item": f"{MODID}:steam_casing"}, "F": {"item": f"{MODID}:flywheel"},
         "R": {"item": f"{MODID}:rotor"}},
        "steam_turbine", research="steam_turbine"))
    wr("crafting", "gas_turbine", shaped(
        ["RRR", "RPR", "RFR"],
        {"R": {"item": f"{MODID}:reactor_casing"}, "P": {"item": f"{MODID}:piston"},
         "F": {"item": f"{MODID}:flywheel"}},
        "gas_turbine", research="gas_turbine"))
    wr("crafting", "ccgt_plant", shaped(
        ["SRS", "SGS", "SRS"],
        {"S": {"item": f"{MODID}:steam_casing"}, "R": {"item": f"{MODID}:reactor_casing"},
         "G": {"item": f"{MODID}:steel_ingot"}},
        "ccgt_plant", research="ccgt"))
    wr("crafting", "electric_generator", shaped(
        ["ICI", "CRC", "ISI"],
        {"I": {"item": f"{MODID}:iron_casing"}, "C": {"item": f"{MODID}:copper_coil"},
         "R": {"item": f"{MODID}:rotor"}, "S": {"item": f"{MODID}:stator"}},
        "electric_generator", research="electric_generator"))
    wr("crafting", "transformer", shaped(
        ["ICI", "CRC", "ICI"],
        {"I": {"item": f"{MODID}:iron_casing"}, "C": {"item": f"{MODID}:copper_coil"},
         "R": {"item": f"{MODID}:iron_rod"}},
        "transformer", research="transformer"))
    wr("crafting", "electric_motor", shaped(
        [" C ", "IRI", " C "],
        {"C": {"item": f"{MODID}:copper_coil"}, "I": {"item": "minecraft:iron_ingot"},
         "R": {"item": f"{MODID}:rotor"}},
        "electric_motor", research="electric_motor"))
    wr("crafting", "lead_acid_battery", shaped(
        ["PPP", "PGP", "PPP"],
        {"P": {"item": f"{MODID}:lead_ingot"}, "G": {"item": "minecraft:glass"}},
        "lead_acid_battery", research="battery"))
    # сети
    wr("crafting", "copper_cable", shaped(
        ["WWW", "LLL"],
        {"W": {"item": f"{MODID}:copper_wire"}, "L": {"item": "minecraft:leather"}},
        "cable_copper", 2, research="power_network"))
    wr("crafting", "aluminum_cable", shaped(
        ["WWW", "LLL"],
        {"W": {"item": f"{MODID}:aluminum_wire"}, "L": {"item": "minecraft:leather"}},
        "cable_aluminum", 2, research="power_network"))
    wr("crafting", "steel_cable", shaped(
        ["WWW", "LLL"],
        {"W": {"item": f"{MODID}:steel_wire"}, "L": {"item": "minecraft:leather"}},
        "cable_steel", 2, research="power_network"))
    wr("crafting", "power_pole_wood", shaped(
        ["S", "P", "S"],
        {"S": {"item": "minecraft:stick"}, "P": {"item": "minecraft:oak_planks"}},
        "power_pole_wood", research="power_network"))
    wr("crafting", "power_pole_concrete", shaped(
        ["G", "C", "G"],
        {"G": {"item": "minecraft:gravel"}, "C": {"item": "minecraft:clay_ball"}},
        "power_pole_concrete", 2, research="power_network"))
    wr("crafting", "power_pole_steel", shaped(
        ["S", "P", "S"],
        {"S": {"item": f"{MODID}:steel_plate"}, "P": {"item": f"{MODID}:iron_rod"}},
        "power_pole_steel", research="power_network"))
    wr("crafting", "circuit_breaker", shaped(
        ["IPI", "CRC"],
        {"I": {"item": f"{MODID}:iron_plate"}, "P": {"item": "minecraft:copper_ingot"},
         "C": {"item": f"{MODID}:copper_wire"}, "R": {"item": f"{MODID}:iron_rod"}},
        "circuit_breaker", research="power_network"))
    wr("crafting", "substation", shaped(
        ["TBT", "TST", "TBT"],
        {"T": {"item": f"{MODID}:transformer"}, "B": {"item": f"{MODID}:circuit_breaker"},
         "S": {"item": f"{MODID}:steel_plate"}},
        "substation", research="power_network"))
    # химия Эпохи II
    wr("crafting", "graphite_electrode", shaped(
        ["G", "G", "G"],
        {"G": {"item": f"{MODID}:graphite_concentrate"}},
        "graphite_electrode", research="water_electrolysis"))
    wr("crafting", "air_separation_unit", shaped(
        ["ICI", "CPC", "ICI"],
        {"I": {"item": f"{MODID}:iron_casing"}, "C": {"item": f"{MODID}:copper_coil"},
         "P": {"item": f"{MODID}:pump"}},
        "air_separation_unit", research="air_separation"))
    wr("crafting", "water_electrolyzer", shaped(
        ["IGI", "IEI", "IGI"],
        {"I": {"item": "minecraft:iron_ingot"}, "G": {"item": "minecraft:glass"},
         "E": {"item": f"{MODID}:graphite_electrode"}},
        "water_electrolyzer", research="water_electrolysis"))
    wr("crafting", "iron_catalyst", shapeless(
        [{"item": f"{MODID}:hematite_powder"}, {"item": f"{MODID}:magnetite_concentrate"}],
        "iron_catalyst", research="ammonia"))
    wr("crafting", "ammonia_synthesizer", shaped(
        ["SCS", "SPS", "SCS"],
        {"S": {"item": f"{MODID}:steam_casing"}, "C": {"item": f"{MODID}:copper_coil"},
         "P": {"item": f"{MODID}:pump"}},
        "ammonia_synthesizer", research="ammonia"))
    wr("crafting", "platinum_rhodium_net", shapeless(
        [{"item": f"{MODID}:platinum_ingot"}, {"item": f"{MODID}:platinum_ingot"}],
        "platinum_rhodium_net", research="nitric_acid"))
    wr("crafting", "ostwald_reactor", shaped(
        ["RCR", "RPR", "RCR"],
        {"R": {"item": f"{MODID}:reactor_casing"}, "C": {"item": f"{MODID}:copper_coil"},
         "P": {"item": f"{MODID}:platinum_rhodium_net"}},
        "ostwald_reactor", research="nitric_acid"))
    wr("crafting", "vanadium_catalyst", shapeless(
        [{"item": f"{MODID}:vanadium_ingot"}, {"item": f"{MODID}:sulfur_concentrate"}],
        "vanadium_catalyst", research="contact_sulfuric"))
    wr("crafting", "contact_acid_reactor", shaped(
        ["RCR", "RPR", "RCR"],
        {"R": {"item": f"{MODID}:reactor_casing"}, "C": {"item": f"{MODID}:copper_coil"},
         "P": {"item": f"{MODID}:vanadium_catalyst"}},
        "contact_acid_reactor", research="contact_sulfuric"))
    wr("crafting", "chloralkali_cell", shaped(
        ["RGR", "RER", "RGR"],
        {"R": {"item": f"{MODID}:reactor_casing"}, "G": {"item": "minecraft:glass"},
         "E": {"item": f"{MODID}:graphite_electrode"}},
        "chloralkali_cell", research="chloralkali"))
    wr("crafting", "aluminum_electrolyzer", shaped(
        ["RER", "RCR", "RER"],
        {"R": {"item": f"{MODID}:refractory_brick"}, "E": {"item": f"{MODID}:graphite_electrode"},
         "C": {"item": f"{MODID}:copper_coil"}},
        "aluminum_electrolyzer", research="aluminum"))
    wr("crafting", "fertilizer_mixer", shaped(
        ["IPI", "IVI", "IPI"],
        {"I": {"item": "minecraft:iron_ingot"}, "P": {"item": f"{MODID}:pump"},
         "V": {"item": f"{MODID}:valve"}},
        "fertilizer_mixer", research="fertilizers"))
    wr("crafting", "oil_distillation_column", shaped(
        ["SGS", "SPS", "SGS"],
        {"S": {"item": f"{MODID}:steam_casing"}, "G": {"item": "minecraft:glass"},
         "P": {"item": f"{MODID}:pump"}},
        "oil_distillation_column", research="oil_refining"))
    # связь и автоматизация
    wr("crafting", "vacuum_tube", shapeless(
        [{"item": "minecraft:glass"}, {"item": f"{MODID}:copper_wire"},
         {"item": f"{MODID}:tungsten_ingot"}],
        "vacuum_tube", research="radio"))
    wr("crafting", "carbon_microphone", shapeless(
        [{"item": f"{MODID}:coal_powder"}, {"item": f"{MODID}:copper_plate"},
         {"item": "minecraft:glass"}],
        "carbon_microphone", research="telephone"))
    wr("crafting", "speaker", shapeless(
        [{"item": "minecraft:iron_ingot"}, {"item": f"{MODID}:copper_coil"},
         {"item": "minecraft:oak_planks"}],
        "speaker", research="telephone"))
    wr("crafting", "antenna", shaped(
        ["R", "R", "W"],
        {"R": {"item": f"{MODID}:iron_rod"}, "W": {"item": f"{MODID}:copper_wire"}},
        "antenna", research="radio"))
    wr("crafting", "magnetic_core", shapeless(
        [{"item": "minecraft:iron_ingot"}, {"item": f"{MODID}:copper_wire"}],
        "magnetic_core", research="tube_computer"))
    wr("crafting", "telegraph", shaped(
        ["IW", "P "],
        {"I": {"item": "minecraft:iron_ingot"}, "W": {"item": f"{MODID}:copper_wire"},
         "P": {"item": "minecraft:oak_planks"}},
        "telegraph", research="telegraph"))
    wr("crafting", "telephone", shaped(
        ["IM", "IS"],
        {"I": {"item": "minecraft:iron_ingot"}, "M": {"item": f"{MODID}:carbon_microphone"},
         "S": {"item": f"{MODID}:speaker"}},
        "telephone", research="telephone"))
    wr("crafting", "radio", shaped(
        ["ATA", "VTS", "ATA"],
        {"A": {"item": f"{MODID}:antenna"}, "T": {"item": f"{MODID}:vacuum_tube"},
         "V": {"item": f"{MODID}:aluminum_plate"}, "S": {"item": f"{MODID}:speaker"}},
        "radio", research="radio"))
    wr("crafting", "tube_computer", shaped(
        ["VTV", "TMT", "VTV"],
        {"V": {"item": f"{MODID}:vacuum_tube"}, "T": {"item": f"{MODID}:vacuum_tube"},
         "M": {"item": f"{MODID}:magnetic_core"}},
        "tube_computer", research="tube_computer"))
    wr("crafting", "relay", shaped(
        ["IPI", "WCW"],
        {"I": {"item": f"{MODID}:iron_plate"}, "P": {"item": f"{MODID}:piston"},
         "W": {"item": f"{MODID}:copper_wire"}, "C": {"item": f"{MODID}:copper_coil"}},
        "relay", research="power_network"))
    wr("crafting", "plc", shaped(
        ["IVI", "VWV"],
        {"I": {"item": f"{MODID}:iron_plate"}, "V": {"item": f"{MODID}:vacuum_tube"},
         "W": {"item": f"{MODID}:copper_wire"}},
        "plc", research="automation"))
    for sensor in ["temperature", "pressure", "level", "flow"]:
        wr("crafting", f"sensor_{sensor}", shaped(
            ["I", "W", "G"],
            {"I": {"item": "minecraft:iron_ingot"}, "W": {"item": f"{MODID}:copper_wire"},
             "G": {"item": "minecraft:glass"}},
            f"sensor_{sensor}", research="automation"))
    wr("crafting", "valve", shaped(
        ["IPI", " R "],
        {"I": {"item": "minecraft:iron_ingot"}, "P": {"item": f"{MODID}:piston"},
         "R": {"item": f"{MODID}:iron_rod"}},
        "valve", research="steam_watt"))
    # транспорт Эпохи II
    wr("crafting", "rubber", shapeless(
        [{"item": f"{MODID}:coal_tar"}, {"item": f"{MODID}:sulfur_concentrate"}],
        "rubber", research="oil_refining"))
    wr("crafting", "diesel_locomotive", shaped(
        ["PWP", "MWM", "PWP"],
        {"P": {"item": f"{MODID}:piston"}, "W": {"item": f"{MODID}:wheel"},
         "M": {"item": f"{MODID}:electric_motor"}},
        "diesel_locomotive", research="diesel_locomotive"))
    wr("crafting", "ship", shaped(
        ["TTT", "SGS"],
        {"T": {"item": f"{MODID}:steam_turbine"}, "S": {"item": f"{MODID}:steel_plate"},
         "G": {"item": f"{MODID}:gearbox"}},
        "ship", research="ships"))
    wr("crafting", "submarine", shaped(
        ["BPB", "SSS", "BPB"],
        {"B": {"item": f"{MODID}:lead_acid_battery"}, "P": {"item": f"{MODID}:pump"},
         "S": {"item": f"{MODID}:steel_plate"}},
        "submarine", research="submarine"))
    wr("crafting", "car", shaped(
        ["PWP", "SRW", "PWP"],
        {"P": {"item": f"{MODID}:piston"}, "W": {"item": f"{MODID}:wheel"},
         "S": {"item": f"{MODID}:steel_plate"}, "R": {"item": f"{MODID}:rubber"}},
        "car", research="car"))

    # --- Машинные рецепты ---
    K = 50_000  # 50 кДж на цикл обогащения
    for m in MINERALS:
        wr("crusher", f"{m}_rock", machine(
            "crusher", [f"{m}_rock"], [(f"{m}_powder", 1)],
            energy=K, ticks=100, research="ore_enrichment"))
        # Обогащение двухступенчатое: дробилка 1 порода → 1 порошок (50 кДж),
        # сепаратор 2 порошка → 1 концентрат (ещё 50 кДж). Итого 100 кДж на концентрат.
        wr("separator", f"{m}_powder", machine(
            "separator", [f"{m}_powder"], [(f"{m}_concentrate", 1)],
            energy=K, ticks=100, research="ore_enrichment", counts=[2]))
    # миграция старых руд: 1 блок ванильной руды → 2 куска породы
    for vanilla, rock in [("copper_ore", "copper"), ("iron_ore", "hematite"),
                          ("deepslate_copper_ore", "copper"), ("deepslate_iron_ore", "magnetite")]:
        wr("crusher", f"migrate_{vanilla}", {
            "type": f"{MODID}:crusher",
            "ingredients": [{"item": f"minecraft:{vanilla}"}],
            "results": [{"id": f"{MODID}:{rock}_rock", "count": 2}],
            "energy": K, "ticks": 100, "research": "ore_enrichment"})
    # плавка концентратов
    for mineral, out, temp in [("tin", "tin_ingot", 505.0), ("galena", "lead_ingot", 600.0)]:
        wr("primitive_furnace", f"smelt_{mineral}", machine(
            "primitive_furnace", [f"{mineral}_concentrate"], [(out, 1)],
            ticks=600, min_temp=temp, research="ore_enrichment"))
    wr("primitive_furnace", "smelt_sand_glass", machine(
        "primitive_furnace", ["minecraft:sand"], [("minecraft:glass", 1)],
        ticks=400, min_temp=1173.0))
    wr("primitive_furnace", "smelt_sylvite", machine(
        "primitive_furnace", ["sylvite_concentrate"], [("potash", 1)],
        ticks=400, min_temp=700.0, research="ore_enrichment"))
    wr("primitive_furnace", "smelt_claystone", machine(
        "primitive_furnace", ["claystone_concentrate"], [("minecraft:clay_ball", 1)],
        ticks=400, min_temp=400.0, research="ore_enrichment"))
    wr("primitive_furnace", "smelt_limestone", machine(
        "primitive_furnace", ["limestone_concentrate"], [("lime", 1)],
        ticks=400, min_temp=1100.0, research="ore_enrichment"))
    # горн: медь, бронза, чугун
    wr("bloomery", "smelt_copper", machine(
        "bloomery", ["copper_concentrate"], [("copper_ingot", 1)],
        ticks=600, min_temp=1357.0, research="copper_metallurgy"))
    wr("bloomery", "smelt_hematite", machine(
        "bloomery", ["hematite_concentrate", "hematite_concentrate"], [("pig_iron_ingot", 1)],
        ticks=800, min_temp=1573.0, research="iron_metallurgy"))
    wr("bloomery", "smelt_magnetite", machine(
        "bloomery", ["magnetite_concentrate", "magnetite_concentrate"], [("pig_iron_ingot", 1)],
        ticks=800, min_temp=1573.0, research="iron_metallurgy"))
    wr("bloomery", "alloy_bronze", machine(
        "bloomery", ["copper_ingot", "tin_ingot"],
        [("bronze_ingot", 10)], ticks=600, min_temp=1223.0, research="bronze_metallurgy",
        counts=[9, 1]))
    # малая домна: 4 концентрата + 2 кокса + 1 известняк → 2 чугуна + 1 шлак
    wr("small_blast_furnace", "pig_iron_hematite", machine(
        "small_blast_furnace",
        ["hematite_concentrate", "hematite_concentrate", "hematite_concentrate", "hematite_concentrate",
         "coke", "coke", "limestone_concentrate"],
        [("pig_iron_ingot", 2), ("slag", 1)],
        ticks=600, min_temp=1773.0, research="iron_metallurgy",
        counts=[1, 1, 1, 1, 1, 1, 1]))
    wr("small_blast_furnace", "pig_iron_magnetite", machine(
        "small_blast_furnace",
        ["magnetite_concentrate", "magnetite_concentrate", "magnetite_concentrate", "magnetite_concentrate",
         "coke", "coke", "limestone_concentrate"],
        [("pig_iron_ingot", 2), ("slag", 1)],
        ticks=600, min_temp=1773.0, research="iron_metallurgy"))
    # пудлингование: 1 чугун + 1 уголь → 0.8 железа (выход в BE)
    wr("puddling_furnace", "wrought_iron", machine(
        "puddling_furnace", ["pig_iron_ingot", "minecraft:coal"], [],
        ticks=600, min_temp=1373.0, research="iron_metallurgy"))
    # тигель: бронза 9:1 → 10, стекло, латунь, специальные стёкла
    wr("crucible_furnace", "alloy_bronze", machine(
        "crucible_furnace", ["copper_ingot", "tin_ingot"],
        [("bronze_ingot", 10)], ticks=600, min_temp=1223.0, research="bronze_metallurgy",
        counts=[9, 1]))
    wr("crucible_furnace", "glass_batch", machine(
        "crucible_furnace", ["sand", "sand", "sand", "soda_ash"],
        [("glass", 3)], ticks=600, min_temp=1773.0, research="chemistry_soda",
        counts=[1, 1, 1, 1]))
    wr("crucible_furnace", "alloy_brass", machine(
        "crucible_furnace", ["copper_ingot", "copper_ingot", "zinc_ingot"],
        [("brass_ingot", 3)], ticks=600, min_temp=1223.0, research="era2_transition",
        counts=[1, 1, 1]))
    wr("crucible_furnace", "heat_resistant_glass", machine(
        "crucible_furnace", ["glass", "glass", "magnesite_concentrate"],
        [("heat_resistant_glass", 2)], ticks=600, min_temp=1773.0, research="chemistry_special_glass"))
    wr("crucible_furnace", "optical_glass", machine(
        "crucible_furnace", ["glass", "glass", "lead_ingot", "soda_ash"],
        [("optical_glass", 2)], ticks=600, min_temp=1773.0, research="chemistry_special_glass"))
    # печь прокалки: NaHCO3 → сода + CO2
    wr("calcination_furnace", "soda_ash", machine(
        "calcination_furnace", ["sodium_bicarbonate"], [("soda_ash", 1)],
        ticks=400, min_temp=473.0, research="chemistry_soda",
        fluid_out=("carbon_dioxide", 500)))
    # обжиг пирита: концентрат + кислород → SO2
    wr("pyrite_roaster", "roast_pyrite", machine(
        "pyrite_roaster", ["pyrite_concentrate"], [],
        ticks=400, min_temp=1073.0, research="chemistry_sulfuric",
        fluid_in=("oxygen", 500), fluid_out=("sulfur_dioxide", 1000)))
    # свинцовая камера: SO2 + селитра → разб. H2SO4
    wr("lead_chamber", "chamber_acid", machine(
        "lead_chamber", ["saltpeter"], [],
        ticks=600, research="chemistry_sulfuric",
        fluid_in=("sulfur_dioxide", 1000), fluid_out=("sulfuric_acid_dilute", 1200)))
    # абсорбер: разб. → конц. H2SO4 (нагрев не требуется — экзотермический процесс)
    wr("absorber", "concentrate_acid", machine(
        "absorber", [], [],
        ticks=600, research="chemistry_sulfuric",
        fluid_in=("sulfuric_acid_dilute", 1000), fluid_out=("sulfuric_acid", 780)))
    # Сольве: 2 соли + органика + NH3(бутыль) + CO2 → NaHCO3 + NH4Cl
    wr("solvay_reactor", "solvay", machine(
        "solvay_reactor", ["salt", "salt", "coal_powder", "ammonia_bottle"],
        [("sodium_bicarbonate", 1), ("ammonium_chloride", 1)],
        ticks=600, research="chemistry_soda",
        fluid_in=("carbon_dioxide", 500), counts=[1, 1, 1, 1]))
    # коксовая печь
    wr("coke_oven", "coking", machine(
        "coke_oven", ["coal_concentrate"], [("coke", 1), ("coal_tar", 1)],
        ticks=900, min_temp=1273.0, research="coke_making",
        fluid_out=("coal_gas", 500)))
    # домна на коксе: холодная и горячая
    for suffix, out, temp, ticks in [("cold", 2, 1273.0, 1200), ("hot", 3, 1773.0, 900)]:
        wr("blast_furnace", f"pig_iron_{suffix}", machine(
            "blast_furnace",
            ["hematite_concentrate"] * 4 + ["coke", "coke", "limestone_concentrate"],
            [("pig_iron_ingot", out), ("slag", 1)],
            ticks=ticks, min_temp=temp, research="blast_furnace_coke"))
    # бессемер: 10 чугуна + O2 → 9 стали + шлак (15–20 мин)
    wr("bessemer_converter", "steel", machine(
        "bessemer_converter", ["pig_iron_ingot"] * 4,
        [("steel_ingot", 4), ("slag", 1)], ticks=21_600, research="bessemer",
        fluid_in=("oxygen", 2_000), counts=[3, 3, 3, 1]))
    # мартен: лом (железо) с рудой → сталь
    wr("open_hearth_furnace", "steel_scrap", machine(
        "open_hearth_furnace", ["iron_ingot", "iron_ingot", "hematite_concentrate", "limestone_concentrate"],
        [("steel_ingot", 3)], ticks=1_200, min_temp=1973.0, research="open_hearth"))
    # ЭДП: переплав в сталь
    wr("electric_arc_furnace", "steel_refine", machine(
        "electric_arc_furnace", ["iron_ingot", "iron_ingot", "iron_ingot", "limestone_concentrate"],
        [("steel_ingot", 4)], ticks=600, min_temp=2273.0, research="electric_arc_furnace",
        counts=[1, 1, 1, 1]))
    # весовой дозатор: сплавы по точному составу
    wr("weight_dispenser", "stainless_steel", machine(
        "weight_dispenser",
        ["iron_ingot", "chromium_ingot", "nickel_ingot"],
        [("stainless_steel_ingot", 2)], ticks=200, research="electric_arc_furnace",
        counts=[74, 18, 8]))
    wr("weight_dispenser", "superalloy", machine(
        "weight_dispenser",
        ["nickel_ingot", "chromium_ingot", "cobalt_ingot", "niobium_ingot"],
        [("superalloy_ingot", 2)], ticks=200, research="electric_arc_furnace",
        counts=[103, 50, 40, 3]))
    wr("weight_dispenser", "titanium_alloy", machine(
        "weight_dispenser",
        ["titanium_ingot", "aluminum_ingot", "vanadium_ingot"],
        [("titanium_alloy_ingot", 1)], ticks=200, research="electric_arc_furnace",
        counts=[90, 6, 4]))
    # прокатка листов
    for metal, plate in [("copper", "copper_plate"), ("iron", "iron_plate"), ("tin", "tin_plate"),
                         ("bronze", "bronze_plate"), ("steel", "steel_plate"),
                         ("aluminum", "aluminum_plate")]:
        wr("rolling_mill", f"plate_{metal}", machine(
            "rolling_mill", [f"{metal}_ingot"], [(plate, 1)],
            ticks=100, min_temp=1173.0, research="machine_tools_rolling"))
    # литьё бронзовых листов в тигле (ранний доступ)
    wr("crucible_furnace", "cast_bronze_plate", machine(
        "crucible_furnace", ["bronze_ingot", "bronze_ingot", "bronze_ingot"],
        [("bronze_plate", 1)], ticks=400, min_temp=1223.0, research="bronze_metallurgy",
        counts=[1, 1, 1]))
    # тяжёлый прокат: рельсы и балки
    wr("heavy_rolling_mill", "rails", machine(
        "heavy_rolling_mill", ["steel_ingot", "steel_ingot"],
        [("rail", 4)], ticks=200, min_temp=1173.0, research="heavy_rolling"))
    wr("heavy_rolling_mill", "beam", machine(
        "heavy_rolling_mill", ["steel_ingot", "steel_ingot"],
        [("steel_beam", 1)], ticks=200, min_temp=1173.0, research="heavy_rolling"))
    # волочение проволоки
    for metal, wire in [("copper", "copper_wire"), ("aluminum", "aluminum_wire"),
                        ("steel", "steel_wire")]:
        wr("wire_drawing_machine", f"wire_{metal}", machine(
            "wire_drawing_machine", [f"{metal}_ingot"], [(wire, 2)],
            ticks=100, research="wire_drawing"))
    # токарный станок
    wr("lathe", "shaft", machine("lathe", ["iron_ingot"], [("shaft", 1)],
                                 ticks=200, research="machine_tools_lathe"))
    wr("lathe", "iron_rod", machine("lathe", ["iron_ingot"], [("iron_rod", 1)],
                                    ticks=100, research="machine_tools_lathe"))
    wr("lathe", "bolts", machine("lathe", ["iron_rod"], [("bolt", 4)],
                                 ticks=100, research="machine_tools_lathe"))
    wr("lathe", "nuts", machine("lathe", ["iron_rod"], [("nut", 4)],
                                ticks=100, research="machine_tools_lathe"))
    wr("lathe", "precision", machine("lathe", ["iron_rod", "bolt"], [("precision_part", 1)],
                                     ticks=300, research="machine_tools_grinding"))
    # сверлильный/фрезерный: станина
    wr("drill_press", "frame", machine("drill_press", ["iron_plate", "iron_plate"],
                                       [("machine_frame", 1)], ticks=200,
                                       research="machine_tools_drill"))
    wr("milling_machine", "frame", machine("milling_machine", ["iron_plate"],
                                           [("precision_part", 2)], ticks=200,
                                           research="machine_tools_milling"))
    wr("grinding_machine", "precision", machine("grinding_machine", ["iron_plate"],
                                                [("precision_part", 2)], ticks=200,
                                                research="machine_tools_grinding"))
    wr("power_hammer", "plate", machine("power_hammer", ["iron_ingot", "iron_ingot"],
                                        [("iron_plate", 1)], ticks=200,
                                        research="machine_tools_hammer"))
    # удобрения
    wr("fertilizer_mixer", "ammonium_nitrate", machine(
        "fertilizer_mixer", ["nitric_acid_bottle"], [("ammonium_nitrate", 2)],
        ticks=200, research="fertilizers", fluid_in=("ammonia", 1000)))
    wr("fertilizer_mixer", "superphosphate", machine(
        "fertilizer_mixer", ["apatite_concentrate", "apatite_concentrate", "sulfuric_acid_bottle"],
        [("superphosphate", 2)], ticks=200, research="fertilizers"))
    wr("fertilizer_mixer", "potash", machine(
        "fertilizer_mixer", ["sylvite_concentrate", "sylvite_concentrate"],
        [("potash_fertilizer", 2)], ticks=200, research="fertilizers"))
    wr("fertilizer_mixer", "potash_carnallite", machine(
        "fertilizer_mixer", ["carnallite_concentrate", "carnallite_concentrate", "carnallite_concentrate"],
        [("potash_fertilizer", 1), ("magnesium_ingot", 1)], ticks=300, research="fertilizers"))
    # бутыли кислот
    wr("fertilizer_mixer", "sulfuric_bottle", machine(
        "fertilizer_mixer", [], [("sulfuric_acid_bottle", 1)],
        ticks=100, research="fertilizers", fluid_in=("sulfuric_acid", 250)))
    wr("fertilizer_mixer", "nitric_bottle", machine(
        "fertilizer_mixer", [], [("nitric_acid_bottle", 1)],
        ticks=100, research="fertilizers", fluid_in=("nitric_acid", 250)))
    wr("fertilizer_mixer", "hydrochloric_bottle", machine(
        "fertilizer_mixer", [], [("hydrochloric_acid_bottle", 1)],
        ticks=100, research="chloralkali", fluid_in=("hydrochloric_acid", 250)))
    # урановый кек и криолит
    wr("fertilizer_mixer", "yellowcake", machine(
        "fertilizer_mixer", ["uraninite_concentrate", "sulfuric_acid_bottle"],
        [("yellowcake", 1)], ticks=400, research="era2_transition"))
    wr("fertilizer_mixer", "cryolite", machine(
        "fertilizer_mixer", ["fluorite_concentrate", "fluorite_concentrate", "soda_ash", "bauxite_concentrate"],
        [("cryolite", 1)], ticks=400, research="aluminum"))
    print(f"[data] recipes: {n}")


# ---------------------------------------------------------------------------
# Worldgen
# ---------------------------------------------------------------------------

def replace_predicate(block: str) -> dict:
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
    """Породы в мире: configured_feature + placed_feature + biome_modifier.

    1.21.1: placed_feature ссылается на configured_feature по id строкой,
    поэтому на каждую породу пишутся два файла (иначе реестры не грузятся).
    """
    wg_dir = os.path.join(DATA, "worldgen", "placed_feature")
    cf_dir = os.path.join(DATA, "worldgen", "configured_feature")
    os.makedirs(wg_dir, exist_ok=True)
    os.makedirs(cf_dir, exist_ok=True)
    placed = []

    def write_ore(fid, size, replace, count, chance, lo=None, hi=None, heightmap=None,
                  block_id=None):
        """Пишет пару configured/placed для жилы породы.

        block_id — id блока-заполнителя: обычно совпадает с fid, но у выходов
        на поверхность (outcrop_*) фича называется иначе, чем блок породы.
        """
        block_id = block_id or fid
        wj(os.path.join(cf_dir, fid + ".json"), {
            "type": "minecraft:ore",
            "config": {
                "discard_chance_on_air_exposure": 0.0,
                "size": size,
                "targets": [{
                    "target": replace_predicate(replace),
                    "state": {"Name": f"{MODID}:{block_id}"},
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
        # rarity_filter в 1.21.1 принимает ЦЕЛОЕ chance = 1/N, а не долю.
        # Чтобы сохранить распределение богатства 30/60/10, вероятности должны
        # относиться как 3:6:1, то есть chance = 2 / 1 / 6 (1/2 : 1/1 : 1/6).
        # Попыток на чанк берём count * 3/5, чтобы суммарная плотность жил
        # осталась прежней (0.5 + 1.0 + 1/6 = 5/3 на «единичную» попытку).
        attempts = max(1, round(count * 3 / 5))
        for base_prefix, replace, lo, hi in variants:
            for r, chance in RARITY_CHANCE.items():
                write_ore(f"{base_prefix}{m}_porode_{r}", size, replace, attempts, chance, lo=lo, hi=hi)

    # поверхностные выходы для заметных минералов
    for m in ["copper", "tin", "coal", "hematite", "limestone"]:
        write_ore(f"outcrop_{m}", 2, "minecraft:stone", 1, 5,
                  heightmap="MOTION_BLOCKING", block_id=f"{m}_porode_normal")

    # biome_modifier: добавляем все фичи в верхний мир
    # ВАЖНО: biome_modifier — динамический реестр neoforge:biome_modifier,
    # поэтому запись <modid>:<name> лежит в data/<modid>/neoforge/biome_modifier/.
    bm_dir = os.path.join(RES, "data", MODID, "neoforge", "biome_modifier")
    wj(os.path.join(bm_dir, "terrafabrica_ores.json"), {
        "type": "neoforge:add_features",
        "biomes": "#minecraft:is_overworld",
        "features": placed,
        "step": "underground_ores",
    })
    print(f"[data] worldgen: {len(placed)} placed features + biome_modifier")


def main():
    gen_pack_and_sounds()
    gen_lang()
    gen_blockstates_and_models()
    gen_item_models()
    gen_loot()
    gen_tags()
    gen_recipes()
    gen_worldgen()
    prune_orphans()
    print("OK: данные сгенерированы")


if __name__ == "__main__":
    main()
