"""Генерация тестовой структуры GameTest для Terra Fabrica.

Структуры GameTest — бинарный NBT (gzip), путь
`src/main/resources/data/terrafabrica/structure/<name>.nbt`.
Формат (1.21.1): корневой compound со списками `size`, `palette`, `blocks`,
`entities`. Платформа 5x5 с каменным полом на y=0: тесты ставят блоки на y>=1.

Запуск: python tools/gen_test_structure.py
"""
from __future__ import annotations

import gzip
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "data", "terrafabrica", "structure")

DATA_VERSION = 3955  # 1.21.1
SIZE = (5, 5, 4)     # x, y, z


# --- минимальный NBT-писатель -------------------------------------------
def _tag(tag_id: int, name: str, payload: bytes) -> bytes:
    name_bytes = name.encode("utf-8")
    return bytes([tag_id]) + len(name_bytes).to_bytes(2, "big") + name_bytes + payload


def _int(value: int, signed: bool = True) -> bytes:
    return int(value).to_bytes(4, "big", signed=signed)


def _string(value: str) -> bytes:
    data = value.encode("utf-8")
    return len(data).to_bytes(2, "big") + data


def _compound(name: str, children: list[bytes]) -> bytes:
    # compound всегда закрывается TAG_End (0x00)
    return _tag(10, name, b"".join(children) + b"\x00")


def _end() -> bytes:
    return b"\x00"


def _list_int(name: str, values: list[int]) -> bytes:
    """TAG_List (тип 3 = TAG_Int)."""
    payload = _int(len(values))
    for v in values:
        payload += _int(v)
    return _tag(9, name, bytes([3]) + payload)


def _list_compound(name: str, items: list[bytes]) -> bytes:
    """TAG_List (тип 10 = TAG_Compound); элементы — готовые payload'ы compound."""
    payload = _int(len(items))
    for item in items:
        payload += item
    return _tag(9, name, bytes([10]) + payload)


def _compound_payload(children: list[bytes]) -> bytes:
    """Содержимое compound без заголовка (для элементов списка), с TAG_End."""
    return b"".join(children) + b"\x00"


def build_structure(path: str) -> None:
    size = _list_int("size", list(SIZE))
    palette = _list_compound(
        "palette",
        [_compound_payload([_tag(8, "Name", _string("minecraft:stone"))])],
    )
    blocks = []
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            blocks.append(
                _compound_payload([
                    _list_int("pos", [x, 0, z]),
                    _tag(3, "state", _int(0)),
                ])
            )
    blocks_list = _list_compound("blocks", blocks)
    entities = _list_compound("entities", [])

    root_children = [
        _tag(3, "DataVersion", _int(DATA_VERSION)),
        size,
        palette,
        blocks_list,
        entities,
    ]
    payload = _compound("", root_children)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with gzip.open(path, "wb") as handle:
        handle.write(payload)
    print(f"[structure] {os.path.relpath(path, ROOT)} ({len(payload)} B до сжатия)")


def main() -> None:
    build_structure(os.path.join(OUT, "test_platform.nbt"))


if __name__ == "__main__":
    main()
