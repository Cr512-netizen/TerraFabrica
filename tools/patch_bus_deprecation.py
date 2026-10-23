# -*- coding: utf-8 -*-
"""Убирает устаревший параметр bus у @EventBusSubscriber.

В NeoForge 1.21 `Bus.MOD` — единственный мод-бас, параметр помечен
`@Deprecated(forRemoval = true)`. Компилятор ругался тремя warning'ами.
"""
from __future__ import annotations

import io
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

FILES = [
    r"src\main\java\com\terrafabrica\content\CapabilityRegistration.java",
    r"src\main\java\com\terrafabrica\core\network\TFNetwork.java",
    r"src\main\java\com\terrafabrica\client\TFClientEvents.java",
]

for rel in FILES:
    path = os.path.join(ROOT, rel)
    text = io.open(path, encoding="utf-8").read()
    original = text
    text = text.replace(", bus = EventBusSubscriber.Bus.MOD", "")
    if text != original:
        io.open(path, "w", encoding="utf-8", newline="").write(text)
        print("patched", rel)
    else:
        print("без изменений", rel)
