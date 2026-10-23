#!/usr/bin/env python3
"""
Terra Fabrica — дорисовка отсутствующих текстур (16×16, в стиле остального набора).

Создаёт:
  * assets/terrafabrica/textures/item/superalloy_ingot.png   — слиток жаропрочного сплава;
  * assets/terrafabrica/textures/block/heat_resistant_glass.png — термостойкое стекло;
  * assets/terrafabrica/textures/block/optical_glass.png     — оптическое стекло.

Силуэт слитка берётся из steel_ingot.png (единый стиль слитков), цвет — собственный.
Стекло рисуется процедурно: полупрозрачная заливка + рамка + блик.

Запуск: python tools/make_missing_textures.py
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "terrafabrica", "textures")


def luminance(pixel):
    r, g, b, _ = pixel
    return (r * 299 + g * 587 + b * 114) // 1000


def tint_mask(src_name, dark, mid, light):
    """Перекрашивает слиток: сохраняет альфу и распределение яркости, меняет палитру."""
    im = Image.open(os.path.join(TEX, src_name)).convert("RGBA")
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    pixels = [im.getpixel((x, y)) for y in range(im.height) for x in range(im.width)]
    visible = [luminance(p) for p in pixels if p[3] > 0]
    lo, hi = (min(visible), max(visible)) if visible else (0, 255)
    span = max(1, hi - lo)
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = im.getpixel((x, y))
            if a == 0:
                out.putpixel((x, y), (0, 0, 0, 0))
                continue
            t = (luminance((r, g, b, a)) - lo) / span
            if t < 0.5:
                k = t / 0.5
                color = tuple(int(dark[i] + (mid[i] - dark[i]) * k) for i in range(3))
            else:
                k = (t - 0.5) / 0.5
                color = tuple(int(mid[i] + (light[i] - mid[i]) * k) for i in range(3))
            out.putpixel((x, y), color + (a,))
    return out


def glass(frame, fill_alpha, highlight):
    """Стеклянный блок: прозрачная середина, рамка по периметру и блик в левом верхнем углу."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    body = frame + (fill_alpha,)
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            im.putpixel((x, y), frame + (255,) if edge else body)
    for x, y in ((2, 2), (3, 2), (2, 3), (4, 2), (2, 4)):
        im.putpixel((x, y), highlight + (255,))
    for x, y in ((13, 12), (12, 13), (14, 13), (13, 14)):
        im.putpixel((x, y), frame + (200,))
    return im


def main():
    targets = {
        "item/superalloy_ingot.png": lambda: tint_mask(
            "item/steel_ingot.png", (86, 78, 52), (150, 138, 96), (208, 196, 148)),
        "block/heat_resistant_glass.png": lambda: glass((214, 198, 158), 46, (255, 246, 222)),
        "block/optical_glass.png": lambda: glass((188, 214, 226), 38, (240, 252, 255)),
    }
    for rel, maker in targets.items():
        path = os.path.join(TEX, *rel.split("/"))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        maker().save(path, "PNG")
        print(f"[tex] {rel} ({os.path.getsize(path)} байт)")


if __name__ == "__main__":
    main()
