# -*- coding: utf-8 -*-
"""Считает реальные блоки Terra Fabrica в сохранённом мире (не палитры).

Зачем: палитра секции говорит лишь «такой блок в чанке встречается», а нам
нужны количества — чтобы проверить требование к распределению богатства
пород 30 % бедных / 60 % обычных / 10 % богатых на настоящем мире.

Реализация: минимальный NBT-ридер + распаковка `block_states`
(палитра + упакованные индексы, bits = max(4, ceil(log2(size)))).

Запуск: python tools/count_blocks.py "run/worldgen_check" [--porodes]
"""
from __future__ import annotations

import collections
import gzip
import os
import struct
import sys
import zlib

MODID = "terrafabrica"


# --------------------------------------------------------------------------
# NBT
# --------------------------------------------------------------------------

class Nbt:
    """Очень маленький NBT-ридер: ровно то, что нужно для block_states."""

    def __init__(self, data: bytes):
        self.data = data
        self.pos = 0

    def u(self, n: int) -> int:
        value = int.from_bytes(self.data[self.pos:self.pos + n], "big", signed=False)
        self.pos += n
        return value

    def i(self, n: int) -> int:
        value = int.from_bytes(self.data[self.pos:self.pos + n], "big", signed=True)
        self.pos += n
        return value

    def string(self) -> str:
        length = self.u(2)
        raw = self.data[self.pos:self.pos + length]
        self.pos += length
        return raw.decode("utf-8", "replace")

    def payload(self, tag: int):
        if tag == 1:
            return self.i(1)
        if tag == 2:
            return self.i(2)
        if tag == 3:
            return self.i(4)
        if tag == 4:
            return self.i(8)
        if tag == 5:
            self.pos += 4
            return 0.0
        if tag == 6:
            self.pos += 8
            return 0.0
        if tag == 7:
            n = self.u(4)
            self.pos += n
            return None
        if tag == 8:
            return self.string()
        if tag == 9:
            inner = self.u(1)
            n = self.u(4)
            return [self.payload(inner) for _ in range(n)]
        if tag == 10:
            out = {}
            while True:
                t = self.u(1)
                if t == 0:
                    return out
                name = self.string()
                out[name] = self.payload(t)
        if tag == 11:
            n = self.u(4)
            return [self.i(4) for _ in range(n)]
        if tag == 12:
            n = self.u(4)
            return [self.i(8) for _ in range(n)]
        raise ValueError(f"неизвестный NBT-тег {tag}")

    def root(self):
        tag = self.u(1)
        if tag != 10:
            raise ValueError("корень не compound")
        self.string()
        return self.payload(10)


def chunk_payloads(region_path: str):
    with open(region_path, "rb") as f:
        data = f.read()
    for index in range(1024):
        off = index * 4
        if off + 4 > len(data):
            return
        header = struct.unpack(">I", data[off:off + 4])[0]
        if header == 0:
            continue
        sector = header >> 8
        start = sector * 4096
        if start + 5 > len(data):
            continue
        length = struct.unpack(">I", data[start:start + 4])[0]
        if length <= 0 or length > (1 << 24):
            continue
        comp = data[start + 4]
        blob = data[start + 5:start + 4 + length]
        try:
            if comp == 1:
                yield gzip.decompress(blob)
            elif comp == 2:
                yield zlib.decompress(blob)
            elif comp == 3:
                yield blob
        except (OSError, zlib.error):
            continue


# --------------------------------------------------------------------------
# block_states
# --------------------------------------------------------------------------

def section_counts(section: dict, counter: collections.Counter) -> None:
    states = section.get("block_states")
    if not isinstance(states, dict):
        return
    palette = states.get("palette") or []
    names = [entry.get("Name", "") if isinstance(entry, dict) else ""
             for entry in palette]
    if not names:
        return
    if len(names) == 1:
        counter[names[0]] += 4096
        return
    data = states.get("data")
    if not isinstance(data, list) or not data:
        return
    bits = max(4, (len(names) - 1).bit_length())
    per_long = 64 // bits
    mask = (1 << bits) - 1
    for index in range(4096):
        long_index = index // per_long
        if long_index >= len(data):
            break
        shift = (index % per_long) * bits
        value = (data[long_index] >> shift) & mask
        if value < len(names):
            counter[names[value]] += 1


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: count_blocks.py <world dir> [--all]")
        return 2
    world = sys.argv[1]
    show_all = "--all" in sys.argv
    region = os.path.join(world, "region")
    if not os.path.isdir(region):
        print("нет каталога регионов:", region)
        return 1

    counter = collections.Counter()
    chunks = 0
    for name in sorted(os.listdir(region)):
        if not name.endswith(".mca"):
            continue
        for payload in chunk_payloads(os.path.join(region, name)):
            try:
                root = Nbt(payload).root()
            except Exception:
                continue
            chunks += 1
            for section in root.get("sections", []):
                if isinstance(section, dict):
                    section_counts(section, counter)

    print(f"чанков: {chunks}, блоков посчитано: {sum(counter.values())}")

    porodes = {k: v for k, v in counter.items() if k.endswith("_porode_poor")
               or k.endswith("_porode_normal") or k.endswith("_porode_rich")}
    total = sum(porodes.values())
    print(f"пород: {total}")
    if total:
        wealth = collections.Counter()
        minerals = collections.Counter()
        for key, value in porodes.items():
            mineral, _, rest = key.partition("_porode_")
            wealth[rest] += value
            minerals[mineral.replace("deepslate_", "")] += value
        for level in ("poor", "normal", "rich"):
            share = 100.0 * wealth[level] / total
            print(f"  {level:6s}: {wealth[level]:7d} ({share:5.1f} %)")
        print(f"минералов: {len(minerals)}")
        for mineral, value in minerals.most_common():
            print(f"  {mineral:16s} {value}")

    mod_blocks = {k: v for k, v in counter.items() if k.startswith(MODID + ":")}
    if show_all:
        for key, value in sorted(mod_blocks.items(), key=lambda kv: -kv[1]):
            print(f"  {key} {value}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
