# -*- coding: utf-8 -*-
"""Правки гейтинга исследований: null-безопасность и владелец машины.

См. docs/decisions.md — решение «гейтинг машинных рецептов: проверяем при
известном игроке (владелец/последний открывший GUI), иначе рецепт работает».
"""
from __future__ import annotations

import io
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def patch(rel: str, old: str, new: str) -> None:
    path = os.path.join(ROOT, rel)
    text = io.open(path, encoding="utf-8").read()
    if old not in text:
        sys.exit("не найдено в %s: %r" % (rel, old[:60]))
    io.open(path, "w", encoding="utf-8", newline="").write(text.replace(old, new, 1))
    print("patched", rel)


patch(
    "src/main/java/com/terrafabrica/core/research/RecipeGating.java",
    "        if (nodeId == null || nodeId.isEmpty()) return true;\n"
    "        if (player.getAbilities().instabuild) return true; // креатив обходит гейтинг",
    "        if (nodeId == null || nodeId.isEmpty()) return true;\n"
    "        // Нет игрока — нечем проверить прогресс (машина без владельца,\n"
    "        // автоматизация). Рецепт доступен: прогрессию держит крафт самой\n"
    "        // машины, а не её внутренние рецепты (см. docs/decisions.md).\n"
    "        if (player == null) return true;\n"
    "        if (player.getAbilities().instabuild) return true; // креатив обходит гейтинг",
)

patch(
    "src/main/java/com/terrafabrica/content/shared/recipe/MachineRecipeFinder.java",
    "            if (!recipe.matches(input, level)) continue;\n"
    "            if (!RecipeGating.isUnlocked(player, recipe.requiredNode())) continue;",
    "            if (!recipe.matches(input, level)) continue;\n"
    "            // Гейтинг проверяется только при известном игроке (владелец машины\n"
    "            // или последний открывший GUI): иначе автоматизация ломалась бы\n"
    "            // при выходе игрока из мира.\n"
    "            if (player != null && !RecipeGating.isUnlocked(player, recipe.requiredNode())) continue;",
)

patch(
    "src/main/java/com/terrafabrica/content/shared/recipe/MachineRecipeFinder.java",
    "            if (recipe.matches(input, level) && !RecipeGating.isUnlocked(player, recipe.requiredNode())) {",
    "            if (player != null && recipe.matches(input, level)\n"
    "                    && !RecipeGating.isUnlocked(player, recipe.requiredNode())) {",
)
