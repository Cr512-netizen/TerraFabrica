#!/usr/bin/env python3
"""
Terra Fabrica — генератор текстур (процедурный пиксель-арт 16x16).

Создаёт ВСЕ текстуры мода в src/main/resources/assets/terrafabrica/textures/:
  * породы: <mineral>_porode_{poor,normal,rich} и deepslate_-варианты (свои,
    не ванильные: основа-камень с цветными вкраплениями минерала);
  * обшивки, машины (архетипы сторон), блоки (рельс, шпала, конвейер...);
  * предметы: породы, порошки, концентраты, слитки, пластины, проволока,
    инструменты (бронза), детали, химикаты, приборы, транспорт.

Требует Pillow (есть в bundled Python). Запуск:
    python tools/generate_textures.py
"""
import json
import math
import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "terrafabrica", "textures")
BLOCK = os.path.join(TEX, "block")
ITEM = os.path.join(TEX, "item")

random.seed(20261022)

# ---------------------------------------------------------------------------
# Минералы: имя, цвет вкраплений, лёгкий оттенок основы
# ---------------------------------------------------------------------------
MINERALS = {
    "copper":      ((184, 115, 51),  (120, 112, 96)),
    "tin":         ((201, 162, 39),  (118, 112, 98)),
    "hematite":    ((176, 48, 48),   (122, 106, 102)),
    "magnetite":   ((42, 42, 42),    (108, 106, 106)),
    "galena":      ((128, 128, 128), (112, 110, 110)),
    "pyrite":      ((212, 175, 55),  (116, 112, 94)),
    "limestone":   ((232, 232, 224), (124, 120, 112)),
    "claystone":   ((139, 125, 107), (114, 108, 100)),
    "coal":        ((26, 26, 26),    (110, 108, 104)),
    "sylvite":     ((230, 102, 102), (116, 110, 108)),
    "bauxite":     ((150, 82, 60),   (120, 108, 100)),
    "chromite":    ((24, 24, 28),    (108, 106, 108)),
    "pentlandite": ((196, 150, 60),  (114, 110, 100)),
    "sphalerite":  ((90, 80, 60),    (110, 108, 102)),
    "molybdenite": ((138, 142, 150), (112, 110, 110)),
    "wolframite":  ((50, 46, 42),    (110, 108, 106)),
    "uraninite":   ((40, 56, 34),    (108, 108, 102)),
    "pyrolusite":  ((40, 40, 44),    (108, 106, 106)),
    "fluorite":    ((96, 178, 96),   (114, 110, 106)),
    "barite":      ((226, 224, 218), (116, 112, 106)),
    "apatite":     ((158, 178, 128), (112, 110, 102)),
    "gypsum":      ((240, 238, 232), (116, 112, 106)),
    "magnesite":   ((214, 204, 190), (114, 110, 104)),
    "sulfur":      ((240, 220, 60),  (116, 110, 98)),
    "graphite":    ((74, 76, 80),    (110, 108, 106)),
    "halite":      ((238, 234, 228), (114, 110, 108)),
    "carnallite":  ((204, 120, 84),  (116, 108, 102)),
}

RICHNESS_COUNTS = {"poor": (2, 3), "normal": (5, 8), "rich": (10, 15)}


def speckle(px, x, y, color, rich):
    """Точка вкрапления 1-2 px; богатая порода — с бликом."""
    px[x, y] = color
    if rich and random.random() < 0.4:
        px[x + 1, y] = tuple(min(255, c + 90) for c in color)


def stone_base(tint):
    """Каменная основа: серый шум с лёгким оттенком и фактурой."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for x in range(16):
        for y in range(16):
            v = random.randint(96, 120)
            r = int(v * tint[0] / 118)
            g = int(v * tint[1] / 118)
            b = int(v * tint[2] / 118)
            # лёгкая вертикальная фактура
            if x % 3 == 0:
                r, g, b = max(0, r - 6), max(0, g - 6), max(0, b - 6)
            px[x, y] = (r, g, b, 255)
    return img


def deepslate_base(tint):
    """Глубинный сланец: темнее и холоднее."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for x in range(16):
        for y in range(16):
            v = random.randint(58, 74)
            r = int(v * (tint[0] + 20) / 128)
            g = int(v * (tint[1] + 20) / 128)
            b = int(v * (tint[2] + 24) / 128)
            if y % 4 == 0:
                r, g, b = min(255, r + 6), min(255, g + 6), min(255, b + 6)
            px[x, y] = (r, g, b, 255)
    return img


def gen_porode_textures():
    out = os.path.join(BLOCK, "porode")
    os.makedirs(out, exist_ok=True)
    for mineral, (color, tint) in MINERALS.items():
        for rich, (lo, hi) in RICHNESS_COUNTS.items():
            for deepslate in (False, True):
                img = deepslate_base(tint) if deepslate else stone_base(tint)
                px = img.load()
                count = random.randint(lo, hi)
                for _ in range(count):
                    x = random.randint(1, 14)
                    y = random.randint(1, 14)
                    speckle(px, x, y, color, rich == "rich")
                name = ("deepslate_" if deepslate else "") + f"{mineral}_porode_{rich}"
                img.save(os.path.join(out, name + ".png"))
    print(f"[textures] породы: {len(MINERALS) * 6}")


# ---------------------------------------------------------------------------
# Блоки-обшивки и машины
# ---------------------------------------------------------------------------

def noise_block(base, spread, streak=False, metal=False):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for x in range(16):
        for y in range(16):
            v = random.randint(base - spread, base + spread)
            r = g = b = v
            if metal and random.random() < 0.15:
                r = g = b = min(255, v + 24)
            if streak and x % 4 == 0:
                r, g, b = max(0, r - 8), max(0, g - 8), max(0, b - 8)
            px[x, y] = (r, g, b, 255)
    return img


def border(img, color=(30, 30, 30)):
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=color)
    return img


CASINGS = {
    "iron_casing": (104, 8, metal := True),
    "steam_casing": (96, 8, True),
    "refractory_brick": (150, 14, False),
    "reactor_casing": (88, 8, True),
    "lead_casing": (72, 8, True),
}


def gen_machine_textures():
    os.makedirs(os.path.join(BLOCK, "machine"), exist_ok=True)
    for name, (base, spread, metal) in CASINGS.items():
        noise_block(base, spread, metal=metal).save(os.path.join(BLOCK, name + ".png"))
    side = border(noise_block(100, 10, metal=True))
    top = border(noise_block(110, 10, metal=True))
    side.save(os.path.join(BLOCK, "machine", "machine_side.png"))
    top.save(os.path.join(BLOCK, "machine", "machine_top.png"))
    # Архетипы фронтальных текстур машин.
    archetypes = {}
    d = ImageDraw.Draw(Image.new("RGBA", (16, 16)))
    # печной фронт: проём топки
    img = border(noise_block(120, 10))
    d = ImageDraw.Draw(img)
    d.rectangle([5, 6, 10, 11], fill=(20, 20, 20), outline=(60, 40, 30))
    d.rectangle([6, 7, 9, 10], fill=(255, 140, 20))
    archetypes["furnace"] = img
    # механический фронт: циферблат и люк
    img = border(noise_block(100, 10, metal=True))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 4, 11, 11], fill=(230, 230, 220), outline=(50, 50, 50))
    d.line([7, 7, 9, 5], fill=(60, 60, 60))
    d.rectangle([4, 12, 11, 14], fill=(70, 70, 70))
    archetypes["machine"] = img
    # электрический фронт: панель с клеммами
    img = border(noise_block(96, 10, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 3, 12, 12], fill=(40, 44, 52))
    for i in range(3):
        d.ellipse([4 + i * 3, 8, 6 + i * 3, 10], fill=(220, 200, 60))
    d.rectangle([5, 5, 10, 7], fill=(120, 180, 120))
    archetypes["electrical"] = img
    # бак: смотровое окно
    img = border(noise_block(100, 10, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 3, 11, 12], fill=(70, 74, 80))
    d.rectangle([5, 4, 10, 11], fill=(60, 120, 200))
    archetypes["tank"] = img
    # турбина: вентилятор
    img = border(noise_block(100, 10, metal=True))
    d = ImageDraw.Draw(img)
    d.ellipse([3, 3, 12, 12], fill=(30, 30, 30), outline=(90, 90, 90))
    d.pieslice([4, 4, 11, 11], 0, 360, fill=(150, 150, 150), width=1)
    for ang in range(0, 360, 45):
        d.line([7.5, 7.5, 7.5 + 5 * math.cos(math.radians(ang)), 7.5 + 5 * math.sin(math.radians(ang))],
               fill=(170, 170, 170))
    archetypes["turbine"] = img
    # консоль/компьютер: лампочки
    img = border(noise_block(94, 10, metal=True))
    d = ImageDraw.Draw(img)
    for i in range(4):
        d.ellipse([3 + i * 3, 4, 5 + i * 3, 6], fill=(255, 180, 60))
    d.rectangle([3, 9, 12, 13], fill=(60, 60, 60))
    archetypes["console"] = img
    for name, img in archetypes.items():
        img.save(os.path.join(BLOCK, "machine", name + ".png"))
    print("[textures] машины: side/top + " + ", ".join(archetypes))


def gen_misc_blocks():
    # рельс
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 7, 15, 8], fill=(60, 50, 40))
    d.rectangle([0, 4, 15, 5], fill=(140, 130, 120))
    d.rectangle([0, 10, 15, 11], fill=(140, 130, 120))
    d.line([2, 4, 2, 11], fill=(80, 70, 60))
    d.line([7, 4, 7, 11], fill=(80, 70, 60))
    d.line([12, 4, 12, 11], fill=(80, 70, 60))
    img.save(os.path.join(BLOCK, "rail.png"))
    # шпала
    img = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 4, 15, 11], fill=(96, 70, 44))
    for x in range(16):
        for y in range(4, 12):
            if (x + y) % 5 == 0:
                d.point((x, y), fill=(76, 54, 32))
    d.rectangle([0, 4, 15, 4], fill=(120, 90, 60))
    img.save(os.path.join(BLOCK, "sleeper.png"))
    # конвейер (бок)
    img = border(noise_block(100, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 5, 14, 10], fill=(40, 40, 44))
    for x in range(1, 15, 2):
        d.line([x, 5, x, 10], fill=(90, 90, 90))
    img.save(os.path.join(BLOCK, "conveyor.png"))
    # вал
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([0, 8, 15, 8], fill=(150, 150, 150), width=3)
    d.line([0, 8, 15, 8], fill=(200, 200, 200))
    img.save(os.path.join(BLOCK, "shaft.png"))
    # шестерня
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.pieslice([2, 2, 13, 13], 0, 360, fill=(150, 150, 150), width=2)
    d.ellipse([2, 2, 13, 13], outline=(190, 190, 190))
    d.ellipse([6, 6, 9, 9], fill=(40, 40, 40))
    for ang in range(0, 360, 30):
        d.line([7.5 + 6 * math.cos(math.radians(ang)), 7.5 + 6 * math.sin(math.radians(ang)),
                7.5 + 8 * math.cos(math.radians(ang)), 7.5 + 8 * math.sin(math.radians(ang))],
               fill=(150, 150, 150), width=2)
    img.save(os.path.join(BLOCK, "gear.png"))
    for name in ("belt", "pulley"):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        d.rectangle([0, 6, 15, 9], fill=(94, 62, 40))
        d.ellipse([6, 5, 9, 10], outline=(150, 150, 150))
        d.ellipse([6, 6, 9, 9], fill=(40, 40, 40))
        img.save(os.path.join(BLOCK, name + ".png"))
    # редуктор
    img = border(noise_block(104, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.ellipse([3, 5, 6, 8], fill=(180, 180, 180))
    d.ellipse([9, 3, 12, 6], fill=(140, 140, 140))
    img.save(os.path.join(BLOCK, "gearbox.png"))
    # опоры
    for name, color in (("power_pole_wood", (110, 78, 46)), ("power_pole_concrete", (150, 150, 150)),
                        ("power_pole_steel", (120, 126, 130))):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        d.rectangle([6, 0, 9, 15], fill=color)
        d.rectangle([3, 2, 12, 4], fill=color)
        img.save(os.path.join(BLOCK, name + ".png"))
    for name, color in (("insulator_porcelain", (210, 190, 160)), ("insulator_glass", (150, 190, 210))):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        d.ellipse([5, 5, 10, 10], fill=color, outline=(60, 60, 60))
        d.ellipse([6, 2, 9, 13], fill=color, outline=(60, 60, 60))
        img.save(os.path.join(BLOCK, name + ".png"))
    for name, color in (("cable_copper", (196, 120, 50)), ("cable_aluminum", (180, 180, 190)),
                        ("cable_steel", (110, 116, 120))):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        d.line([0, 8, 15, 8], fill=color, width=2)
        d.line([8, 0, 8, 15], fill=color, width=2)
        img.save(os.path.join(BLOCK, name + ".png"))
    # телеграф/телефон/радио
    img = border(noise_block(96, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 4, 11, 12], fill=(50, 44, 34))
    d.rectangle([5, 6, 10, 10], fill=(220, 210, 190))
    img.save(os.path.join(BLOCK, "telegraph.png"))
    img = border(noise_block(96, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 6, 11, 12], fill=(30, 30, 30))
    d.ellipse([5, 8, 10, 12], fill=(80, 80, 80))
    img.save(os.path.join(BLOCK, "telephone.png"))
    img = border(noise_block(96, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 3, 12, 12], fill=(40, 40, 44))
    for i in range(3):
        d.ellipse([4 + i * 3, 5, 6 + i * 3, 7], fill=(255, 200, 80))
    d.line([0, 0, 15, 0], fill=(140, 140, 140))
    img.save(os.path.join(BLOCK, "radio.png"))
    # датчики
    img = border(noise_block(100, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.ellipse([5, 5, 10, 10], fill=(230, 230, 230))
    d.line([7, 7, 9, 5], fill=(200, 40, 40))
    img.save(os.path.join(BLOCK, "sensor.png"))
    # реле / ПЛК / клапан / подстанция / выключатель / батарея
    img = border(noise_block(100, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 3, 12, 12], fill=(60, 60, 64))
    d.rectangle([5, 6, 10, 9], fill=(220, 200, 60))
    img.save(os.path.join(BLOCK, "relay.png"))
    img = border(noise_block(98, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 3, 12, 12], fill=(50, 50, 54))
    for i in range(4):
        d.ellipse([4 + i * 2, 5, 5 + i * 2, 6], fill=(220, 200, 60))
    img.save(os.path.join(BLOCK, "plc.png"))
    img = border(noise_block(100, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 4, 11, 11], fill=(120, 120, 124))
    d.line([7, 6, 7, 9], fill=(220, 40, 40))
    img.save(os.path.join(BLOCK, "valve.png"))
    img = border(noise_block(104, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 4, 11, 11], fill=(70, 74, 80))
    d.rectangle([6, 6, 9, 9], fill=(200, 190, 60))
    img.save(os.path.join(BLOCK, "substation.png"))
    img = border(noise_block(104, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 3, 11, 12], fill=(70, 70, 74))
    d.rectangle([6, 6, 9, 9], fill=(220, 60, 60) if random.random() < 0.5 else (60, 200, 60))
    img.save(os.path.join(BLOCK, "circuit_breaker.png"))
    img = border(noise_block(100, 8, metal=True))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 6, 12, 11], fill=(50, 50, 52))
    d.rectangle([5, 7, 10, 10], fill=(220, 200, 60))
    d.rectangle([6, 4, 9, 6], fill=(40, 40, 40))
    img.save(os.path.join(BLOCK, "lead_acid_battery.png"))
    print("[textures] прочие блоки")


# ---------------------------------------------------------------------------
# Предметы
# ---------------------------------------------------------------------------

def tinted(base, color, alpha=255):
    img = base.copy().convert("RGBA")
    px = img.load()
    for x in range(img.width):
        for y in range(img.height):
            r, g, b, a = px[x, y]
            if a > 0:
                px[x, y] = (int(r * color[0] / 200), int(g * color[1] / 200),
                            int(b * color[2] / 200), a)
    return img


INGOT_BASE = None


def ingot_shape():
    global INGOT_BASE
    if INGOT_BASE is not None:
        return INGOT_BASE
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # классический слиток
    d.polygon([(2, 3), (13, 3), (15, 6), (15, 9), (13, 12), (2, 12), (0, 9), (0, 6)],
              fill=(220, 220, 220, 255))
    d.polygon([(2, 3), (4, 5), (4, 10), (2, 12)], fill=(170, 170, 170, 255))
    d.polygon([(0, 6), (15, 6), (15, 7), (0, 7)], fill=(190, 190, 190, 255))
    INGOT_BASE = img
    return img


def chunk_item(color, tint):
    """Кусок породы: 3-4 неровных куска камня с вкраплениями."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for (cx, cy, w, h) in [(3, 3, 5, 5), (9, 2, 5, 6), (2, 9, 6, 5), (9, 10, 5, 4)]:
        d.polygon([(cx, cy + 1), (cx + w, cy), (cx + w + 1, cy + h - 1), (cx + 1, cy + h)],
                  fill=(tint[0], tint[1], tint[2], 255))
        for _ in range(2):
            d.point((cx + random.randint(1, w - 1), cy + random.randint(1, h - 1)), fill=color)
    return img


def powder_item(color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for y in range(8, 16):
        for x in range(2, 14):
            if random.random() < 0.7:
                v = random.randint(-24, 24)
                d.point((x, y), fill=tuple(max(0, min(255, c + v)) for c in color))
    for x in range(2, 14):
        d.point((x, 8 + (x % 3)), fill=tuple(min(255, c + 30) for c in color))
    return img


def plate_item(color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([1, 5, 14, 10], radius=2, fill=color)
    d.line([1, 5, 14, 5], fill=tuple(min(255, c + 50) for c in color))
    d.line([1, 10, 14, 10], fill=tuple(max(0, c - 60) for c in color))
    return img


def wire_item(color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for i in range(4):
        y = 4 + i * 3
        d.arc([2, y - 3, 13, y + 3], 180, 360, fill=color, width=2)
    return img


def rod_item(color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([3, 13, 12, 3], fill=color, width=3)
    d.line([3, 13, 12, 3], fill=tuple(min(255, c + 60) for c in color))
    return img


def coil_item(color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for y in range(2, 14, 2):
        d.ellipse([2, y, 13, y + 2], outline=color)
    return img


def gear_item(color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.pieslice([2, 2, 13, 13], 0, 360, fill=color, width=2)
    d.ellipse([5, 5, 10, 10], fill=(30, 30, 30))
    for ang in range(0, 360, 30):
        d.line([7.5 + 6 * math.cos(math.radians(ang)), 7.5 + 6 * math.sin(math.radians(ang)),
                7.5 + 8 * math.cos(math.radians(ang)), 7.5 + 8 * math.sin(math.radians(ang))],
               fill=color, width=2)
    return img


BRONZE = (205, 127, 50)
IRON = (190, 190, 195)
STEEL = (150, 158, 166)


def tool_shapes():
    """Контурные формы инструментов (тень + бронзовый/железный металл)."""
    shapes = {}
    d = ImageDraw.Draw(Image.new("RGBA", (16, 16)))
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([2, 14, 13, 3], fill=(120, 80, 40), width=2)          # рукоять кирки
    d.line([9, 7, 13, 3], fill=(190, 190, 195), width=3)         # голова
    d.line([9, 7, 5, 11], fill=(190, 190, 195), width=3)
    shapes["pickaxe"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([7, 1, 8, 14], fill=(120, 80, 40), width=2)           # рукоять молота
    d.rectangle([3, 2, 13, 6], fill=(190, 190, 195))             # боёк
    shapes["hammer"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([2, 13, 12, 3], fill=(120, 80, 40), width=2)          # топорище
    d.polygon([(12, 3), (14, 6), (13, 10), (10, 8), (8, 8), (6, 10), (4, 6), (6, 4)],
              fill=(190, 190, 195))                              # лезвие
    shapes["axe"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([7, 3, 7, 14], fill=(120, 80, 40), width=2)           # черенок
    d.rounded_rectangle([4, 2, 10, 6], radius=1, fill=(190, 190, 195))
    shapes["shovel"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([7, 5, 7, 14], fill=(120, 80, 40), width=2)           # рукоять мотыги
    d.rounded_rectangle([5, 2, 13, 6], radius=1, fill=(190, 190, 195))
    shapes["hoe"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.arc([3, 2, 9, 12], 90, 270, fill=(190, 190, 195), width=3)  # лезвие ножниц
    d.arc([7, 2, 13, 12], 270, 90, fill=(190, 190, 195), width=3)
    d.ellipse([4, 10, 6, 12], outline=(120, 80, 40))
    d.ellipse([10, 10, 12, 12], outline=(120, 80, 40))
    shapes["shears"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([3, 12, 12, 3], fill=(190, 190, 195), width=3)         # напильник
    d.line([5, 11, 11, 5], fill=(150, 150, 155))
    d.line([4, 13, 13, 4], fill=(150, 150, 155))
    shapes["file"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([4, 12, 11, 4], fill=(190, 190, 195), width=3)         # зубило
    d.polygon([(11, 4), (13, 3), (14, 2), (12, 5)], fill=(220, 220, 220))
    shapes["chisel"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([8, 2, 8, 14], fill=(190, 190, 195), width=2)          # ось клещей
    d.line([3, 8, 8, 8], fill=(190, 190, 195), width=2)
    d.line([8, 6, 12, 3], fill=(190, 190, 195), width=2)
    d.line([8, 10, 12, 13], fill=(190, 190, 195), width=2)
    d.ellipse([11, 2, 13, 4], outline=(190, 190, 195))
    d.ellipse([11, 12, 13, 14], outline=(190, 190, 195))
    shapes["tongs"] = img
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([4, 12, 13, 3], fill=(120, 80, 40), width=2)           # рукоять пилы
    d.polygon([(3, 8), (12, 3), (14, 4), (5, 9)], fill=(190, 190, 195))
    for x in range(4, 12, 2):
        d.point((x, 9 - (x - 4) // 2), fill=(240, 240, 240))
    shapes["saw"] = img
    return shapes


def bottle_item(color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([5, 2, 10, 5], radius=1, outline=(160, 200, 220))
    d.rounded_rectangle([4, 5, 11, 14], radius=2, outline=(160, 200, 220))
    d.rounded_rectangle([5, 8, 10, 13], radius=1, fill=(*color, 255))
    return img


def book_item():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(2, 3), (13, 3), (13, 13), (2, 13)], fill=(60, 90, 130))
    d.rectangle([2, 3, 4, 13], fill=(40, 60, 90))
    d.rectangle([5, 5, 11, 11], fill=(230, 220, 180))
    return img


def gadget_item(symbol_color=(220, 200, 60)):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([3, 2, 12, 14], radius=2, fill=(40, 40, 44))
    d.rectangle([4, 3, 11, 6], fill=(200, 210, 200))
    d.ellipse([5, 9, 7, 11], fill=symbol_color)
    d.ellipse([8, 9, 10, 11], fill=(200, 60, 60))
    return img


def vehicle_item(color=(150, 158, 166), kind="loco"):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([1, 6, 14, 11], radius=2, fill=color)
    if kind == "loco":
        d.rectangle([3, 2, 8, 6], fill=color)
        d.rectangle([4, 3, 7, 5], fill=(220, 220, 220))
    if kind in ("car",):
        d.rectangle([3, 2, 10, 6], fill=color)
        d.rectangle([4, 3, 9, 5], fill=(220, 220, 220))
    if kind in ("ship", "submarine"):
        d.polygon([(2, 7), (8, 3), (13, 7), (8, 10)], fill=color)
    d.ellipse([3, 11, 6, 14], fill=(40, 40, 40))
    d.ellipse([10, 11, 13, 14], fill=(40, 40, 40))
    return img


ITEM_KINDS = {
    "ingot": lambda c: tinted(ingot_shape(), c),
    "rock": lambda c, t: chunk_item(c, t),
    "powder": lambda c: powder_item(c),
    "concentrate": lambda c: powder_item(tuple(min(255, x + 40) for x in c)),
    "plate": lambda c: plate_item(c),
    "wire": lambda c: wire_item(c),
    "rod": lambda c: rod_item(c),
    "coil": lambda c: coil_item(c),
    "gear": lambda c: gear_item(c),
    "bottle": lambda c: bottle_item(c),
    "book": lambda: book_item(),
    "gadget": lambda: gadget_item(),
    "vehicle": lambda c, k="loco": vehicle_item(c, k),
}


def item_color(name):
    """Цвет предмета по имени."""
    m = {
        "copper": (196, 120, 50), "tin": (201, 162, 39), "bronze": (205, 127, 50),
        "lead": (128, 128, 136), "pig_iron": (110, 110, 112), "brass": (190, 170, 90),
        "steel": (150, 158, 166), "stainless": (170, 176, 180), "aluminum": (200, 202, 208),
        "chromium": (150, 160, 170), "nickel": (170, 170, 160), "zinc": (140, 150, 160),
        "molybdenum": (138, 142, 150), "tungsten": (90, 92, 96), "manganese": (120, 110, 120),
        "magnesium": (190, 190, 200), "platinum": (220, 226, 230), "cobalt": (70, 90, 160),
        "niobium": (170, 170, 190), "titanium": (160, 160, 170), "vanadium": (170, 170, 180),
        "graphite": (70, 72, 76), "coke": (40, 40, 42), "slag": (100, 96, 92),
        "coal_tar": (30, 26, 24), "lime": (225, 225, 215), "potash": (200, 170, 170),
        "soda_ash": (235, 235, 235), "saltpeter": (230, 225, 210), "salt": (240, 240, 238),
        "ammonium_chloride": (235, 235, 240), "caustic_soda": (240, 240, 240),
        "yellowcake": (240, 220, 80), "cryolite": (235, 240, 245), "fluoride_salt": (180, 220, 200),
        "ammonium_nitrate": (240, 240, 240), "superphosphate": (170, 170, 160),
        "potash_fertilizer": (220, 170, 170), "firebrick": (170, 110, 70),
        "iron_catalyst": (70, 70, 76), "platinum_rhodium_net": (220, 226, 230),
        "vanadium_catalyst": (220, 150, 60), "graphite_electrode": (60, 62, 66),
        "vacuum_tube": (200, 200, 210), "magnetic_core": (90, 90, 96),
        "carbon_microphone": (60, 60, 66), "speaker": (70, 70, 74),
        "commutator_board": (70, 90, 60), "antenna": (150, 150, 150),
        "wheel": (90, 90, 96), "wagon_body": (120, 90, 60), "rubber": (60, 60, 62),
        "sodium_bicarbonate": (240, 240, 240),
    }
    for key, value in m.items():
        if key in name:
            return value
    return (170, 170, 170)


def gen_item_textures():
    os.makedirs(ITEM, exist_ok=True)
    items = []
    # минералы: порода/порошок/концентрат
    for mineral, (color, tint) in MINERALS.items():
        items.append((f"{mineral}_rock", "rock", color, tint))
        items.append((f"{mineral}_powder", "powder", color, None))
        items.append((f"{mineral}_concentrate", "concentrate", color, None))
    # слитки
    for ingot in ["tin", "bronze", "lead", "pig_iron", "brass", "steel", "stainless_steel",
                  "aluminum", "chromium", "nickel", "zinc", "molybdenum", "tungsten",
                  "manganese", "magnesium", "platinum", "cobalt", "niobium", "titanium",
                  "vanadium"]:
        items.append((f"{ingot}_ingot", "ingot", item_color(ingot), None))
    # пластины/проволока/детали
    for plate in ["copper", "iron", "steel", "aluminum", "tin", "bronze"]:
        items.append((f"{plate}_plate", "plate", item_color(plate), None))
    for wire in ["copper", "aluminum", "steel"]:
        items.append((f"{wire}_wire", "wire", item_color(wire), None))
    items.append(("iron_rod", "rod", IRON, None))
    items.append(("copper_coil", "coil", item_color("copper"), None))
    items.append(("bronze_gear", "gear", BRONZE, None))
    items.append(("iron_gear", "gear", IRON, None))
    items.append(("steel_beam", "rod", STEEL, None))
    items.append(("ammonia_bottle", "bottle", (210, 220, 230), None))
    items.append(("titanium_alloy_ingot", "ingot", (150, 160, 170), None))
    items.append(("bolt", "rod", (150, 150, 155), None))
    items.append(("nut", "gear", (150, 150, 155), None))
    items.append(("precision_part", "gear", (170, 170, 180), None))
    # химикаты
    for chem in ["slag", "coke", "coal_tar", "lime", "potash", "soda_ash", "saltpeter",
                 "ammonium_chloride", "caustic_soda", "yellowcake", "cryolite",
                 "fluoride_salt", "ammonium_nitrate", "superphosphate", "potash_fertilizer",
                 "salt", "sodium_bicarbonate"]:
        items.append((chem, "powder", item_color(chem), None))
    for catalyst in ["iron_catalyst", "platinum_rhodium_net", "vanadium_catalyst"]:
        items.append((catalyst, "powder", item_color(catalyst), None))
    items.append(("graphite_electrode", "rod", (60, 62, 66), None))
    items.append(("firebrick", "plate", (170, 110, 70), None))
    for acid in ["sulfuric", "nitric", "hydrochloric"]:
        items.append((f"{acid}_acid_bottle", "bottle", item_color("soda_ash") if acid == "hydrochloric"
                     else item_color("yellowcake") if acid == "sulfuric" else (217, 201, 110), None))
    # детали машин
    for part in ["piston", "cylinder", "condenser", "balance_beam", "pump", "boiler_part",
                 "flywheel", "steam_chest", "governor", "bellows", "machine_frame"]:
        items.append((part, "gear", (150, 150, 155), None))
    for part in ["rotor", "stator"]:
        items.append((part, "gear", (170, 170, 180), None))
    for part in ["vacuum_tube", "magnetic_core", "carbon_microphone", "speaker",
                 "commutator_board", "antenna", "wheel", "wagon_body", "rubber"]:
        items.append((part, "rod" if part in ("antenna",) else "gear", item_color(part), None))
    # инструменты (бронза и железо)
    shapes = tool_shapes()
    for tool, shape in shapes.items():
        items.append((f"bronze_{tool}", "tool", BRONZE, shape))
    for tool in ["hammer", "file", "chisel", "tongs", "saw"]:
        items.append((f"iron_{tool}", "tool", IRON, shapes[tool]))
    # приборы и книга
    for gadget in ["multimeter", "thermometer", "tachometer", "geiger_counter"]:
        items.append((gadget, "gadget", None, None))
    items.append(("guide_book", "book", None, None))
    # транспорт
    items.append(("minecart", "vehicle", (120, 100, 70), "loco"))
    items.append(("steam_locomotive", "vehicle", (110, 110, 112), "loco"))
    items.append(("diesel_locomotive", "vehicle", (150, 120, 60), "loco"))
    items.append(("car", "vehicle", (120, 150, 170), "car"))
    items.append(("ship", "vehicle", (120, 120, 130), "ship"))
    items.append(("submarine", "vehicle", (90, 100, 110), "submarine"))

    seen = set()
    for name, kind, color, extra in items:
        if name in seen:
            continue
        seen.add(name)
        if kind == "tool":
            img = extra  # уже готовая форма
        elif kind == "rock":
            img = ITEM_KINDS[kind](color, extra)
        elif kind == "vehicle":
            img = ITEM_KINDS[kind](color, extra)
        elif kind in ("book", "gadget"):
            img = ITEM_KINDS[kind]()
        else:
            img = ITEM_KINDS[kind](color)
        img.save(os.path.join(ITEM, name + ".png"))
    print(f"[textures] предметы: {len(seen)}")


def gen_gui():
    gui = os.path.join(TEX, "gui")
    os.makedirs(gui, exist_ok=True)
    # Панель машины 176x166 — ровно ванильный размер, чтобы она не вылезала за экран
    # при крупном масштабе интерфейса. Раскладка ОБЯЗАНА совпадать с MachineLayout
    # (slotX/slotY, INV_ROW_Y, HOTBAR_Y, SEPARATOR_Y, INFO_Y): входы 2x2 слева,
    # выходы 2x2 справа, между ними стрелка, слева полосы энергии и двух баков,
    # под слотами одна строка телеметрии, дальше ванильный инвентарь игрока.
    W, H = 176, 166
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, W - 1, H - 1], fill=(198, 198, 198))
    d.rectangle([3, 3, W - 4, H - 4], fill=(139, 139, 139), outline=(55, 55, 55))
    d.rectangle([6, 6, W - 7, H - 7], fill=(198, 198, 198), outline=(55, 55, 55))
    # слоты машины: входы 2x2 (50,18)/(68,18)/(50,36)/(68,36), выходы 2x2 (110/128)
    for i in range(4):
        dx, dy = 18 * (i % 2), 18 * (i // 2)
        for x, y in ((50 + dx, 18 + dy), (110 + dx, 18 + dy)):
            d.rectangle([x, y, x + 16, y + 16], fill=(55, 55, 55))
            d.rectangle([x + 1, y + 1, x + 15, y + 15], fill=(139, 139, 139))
    # индикаторы: энергия x=8, бак входа x=22, бак выхода x=36; y=18..58, ширина 10
    for x in (8, 22, 36):
        d.rectangle([x, 18, x + 10, 58], fill=(40, 40, 40))
        d.rectangle([x + 1, 19, x + 9, 57], fill=(28, 28, 28))
    # стрелка прогресса (90,34)-(106,40)
    d.rectangle([90, 34, 106, 40], fill=(40, 40, 40))
    d.rectangle([91, 35, 105, 39], fill=(120, 120, 120))
    # разделитель между зоной машины (телеметрия y=60) и подписью инвентаря (y=72)
    d.rectangle([6, 82, W - 7, 82], fill=(139, 139, 139))
    d.rectangle([6, 83, W - 7, 83], fill=(163, 163, 163))
    # слоты инвентаря игрока: строки 84/102/120, хотбар 142 (см. MachineLayout)
    for row in range(3):
        for col in range(9):
            x, y = 8 + col * 18, 84 + row * 18
            d.rectangle([x, y, x + 16, y + 16], fill=(55, 55, 55))
            d.rectangle([x + 1, y + 1, x + 15, y + 15], fill=(139, 139, 139))
    for col in range(9):
        x, y = 8 + col * 18, 142
        d.rectangle([x, y, x + 16, y + 16], fill=(55, 55, 55))
        d.rectangle([x + 1, y + 1, x + 15, y + 15], fill=(139, 139, 139))
    img.save(os.path.join(gui, "machine.png"))
    print("[textures] gui/machine.png")

    # Панели экранов руководства и древа исследований — 9-слайс 3x3 тайла по 16px.
    gen_panel(gui, "guide.png", frame=(138, 123, 90), edge=(58, 48, 34),
              inner=(24, 21, 18), accent=(168, 148, 104), grain=7)
    gen_panel(gui, "research.png", frame=(96, 120, 148), edge=(28, 38, 52),
              inner=(20, 28, 40), accent=(84, 132, 168), grid=True)
    print("[textures] gui/guide.png, gui/research.png")


def gen_panel(out_dir, name, frame, edge, inner, accent, grain=0, grid=False):
    """Текстура панели 48x48 для client/screen/GuiPanel.java.

    Раскладка тайлов 16x16 (её же читает GuiPanel):
        (0,0) ЛВ   (16,0) верх   (32,0) ПВ
        (0,16) лево (16,16) центр (32,16) право
        (0,32) ЛН   (16,32) низ   (32,32) ПН

    Рамка рисуется прямоугольниками по всей ширине и высоте, поэтому полосы
    однородны вдоль ребра и обрезка неполного тайла не видна. Зерно считается
    по абсолютным координатам с периодом 16, поэтому центр стыкуется при
    тайлинге.
    """
    img = Image.new("RGBA", (48, 48), (*inner, 255))
    px = img.load()
    for x in range(48):
        for y in range(48):
            if grain:
                v = (x * 5 + y * 11) % grain
                if v == 0:
                    px[x, y] = (*accent, 255)
                elif v < grain // 2:
                    px[x, y] = (min(255, inner[0] + 9), min(255, inner[1] + 8),
                                min(255, inner[2] + 7), 255)
            if grid and (x % 8 == 0 or y % 8 == 0):
                r, g, b, _ = px[x, y]
                px[x, y] = (int(r * 0.55 + accent[0] * 0.45),
                            int(g * 0.55 + accent[1] * 0.45),
                            int(b * 0.55 + accent[2] * 0.45), 255)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 47, 47], outline=(*edge, 255))
    d.rectangle([1, 1, 46, 46], outline=(*frame, 255))
    d.rectangle([2, 2, 45, 45], outline=(*accent, 255))
    d.rectangle([3, 3, 44, 44], outline=(*frame, 255))
    d.rectangle([4, 4, 43, 43], outline=(*edge, 255))
    # заклёпки в углах: попадают только в угловые тайлы и при растяжке ребра не плывут
    for cx, cy in ((8, 8), (39, 8), (8, 39), (39, 39)):
        d.rectangle([cx, cy, cx + 1, cy + 1], fill=(*accent, 255))
    img.save(os.path.join(out_dir, name))


def gen_fluid_textures():
    out = os.path.join(BLOCK, "fluid")
    os.makedirs(out, exist_ok=True)
    fluids = {
        "steam_low": (230, 230, 235), "steam_high": (220, 225, 235),
        "sulfuric_acid": (190, 170, 60), "sulfuric_acid_dilute": (200, 190, 120),
        "nitric_acid": (217, 201, 110), "hydrochloric_acid": (184, 217, 192),
        "ammonia": (210, 220, 230), "chlorine": (200, 220, 90), "hydrogen": (190, 210, 230),
        "oxygen": (180, 210, 230), "nitrogen": (200, 210, 225), "carbon_dioxide": (205, 210, 215),
        "natural_gas": (200, 205, 215), "crude_oil": (40, 34, 30),
        "fuel_oil": (50, 44, 40), "gasoline": (220, 190, 120), "kerosene": (210, 205, 180),
        "diesel": (200, 180, 120), "molten_glass": (230, 150, 60), "molten_metal": (240, 120, 40),
        # id обязаны совпадать с TFFluids.SPECS, иначе блок жидкости останется без текстуры
        "sulfur_dioxide": (210, 214, 200), "coal_gas": (92, 98, 104),
    }
    for name, color in fluids.items():
        img = Image.new("RGBA", (16, 16), (*color, 235))
        px = img.load()
        for x in range(16):
            for y in range(16):
                if (x + y) % 4 == 0:
                    px[x, y] = tuple(min(255, c + 14) for c in color) + (235,)
        img.save(os.path.join(out, f"{name}_still.png"))
        flow = Image.new("RGBA", (32, 32), (*color, 235))
        fpx = flow.load()
        for x in range(32):
            for y in range(32):
                if (x // 2 + y) % 4 == 0:
                    fpx[x, y] = tuple(min(255, c + 16) for c in color) + (235,)
        flow.save(os.path.join(out, f"{name}_flow.png"))
    print(f"[textures] флюиды: {len(fluids)}")


def main():
    gen_porode_textures()
    gen_machine_textures()
    gen_misc_blocks()
    gen_item_textures()
    gen_gui()
    gen_fluid_textures()
    print("OK: текстуры сгенерированы в", TEX)


if __name__ == "__main__":
    main()
