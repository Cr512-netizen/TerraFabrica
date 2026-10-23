# -*- coding: utf-8 -*-
"""Сканирует регионы сохранённого мира и считает блоки Terra Fabrica.

Зачем: подтвердить, что worldgen действительно ставит породы в реальном мире
(а не только в реестре). Читаем .mca, распаковываем чанки (zlib/gzip) и ищем
id блоков в палитрах секций — этого достаточно, чтобы понять, что породы есть.

Запуск: python tools/scan_world.py "run/saves/New World"
"""
from __future__ import annotations

import collections
import gzip
import io
import os
import re
import struct
import sys
import zlib

PORODE = re.compile(rb"terrafabrica:([a-z_]+)_porode_(poor|normal|rich)")


def chunk_payloads(path: str):
    """Отдаёт распакованные NBT-чанки региона."""
    with open(path, "rb") as f:
        data = f.read()
    for i in range(0, 1024):
        off = i * 4
        if off + 4 > len(data):
            break
        header = struct.unpack(">I", data[off:off + 4])[0]
        if header == 0:
            continue
        sector = header >> 8
        length_off = sector * 4096
        if length_off + 5 > len(data):
            continue
        length = struct.unpack(">I", data[length_off:length_off + 4])[0]
        if length <= 0 or length > 1 << 24:
            continue
        comp = data[length_off + 4]
        blob = data[length_off + 5:length_off + 4 + length]
        try:
            if comp == 1:
                yield gzip.decompress(blob)
            elif comp == 2:
                yield zlib.decompress(blob)
            elif comp == 3:
                yield blob
        except Exception:
            continue


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: scan_world.py <world dir>")
        return 2
    world = sys.argv[1]
    region = os.path.join(world, "region")
    if not os.path.isdir(region):
        print("нет каталога регионов:", region)
        return 1

    porodes = collections.Counter()
    others = collections.Counter()
    chunks = 0
    for name in sorted(os.listdir(region)):
        if not name.endswith(".mca"):
            continue
        for payload in chunk_payloads(os.path.join(region, name)):
            chunks += 1
            for m in PORODE.finditer(payload):
                porodes[(m.group(1).decode(), m.group(2).decode())] += 1
            for m in re.finditer(rb"terrafabrica:([a-z_]+)", payload):
                others[m.group(1).decode()] += 1

    minerals = sorted({k[0] for k in porodes})
    rich = collections.Counter(k[1] for k in porodes)
    print(f"чанков прочитано: {chunks}")
    print(f"минералов с породой: {len(minerals)} → {minerals}")
    print(f"вхождений по богатству: {dict(rich)}")
    if others:
        top = ", ".join(f"{k}×{v}" for k, v in others.most_common(8))
        print(f"прочие id мода в мире: {top}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
