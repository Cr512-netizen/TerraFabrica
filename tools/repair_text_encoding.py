#!/usr/bin/env python3
"""
Починка «двойной кодировки» в текстовых файлах проекта.

Симптом: комментарий на русском выглядит как «Р’РЅСѓС‚СЂРёРёРіСЂРѕРІРѕРµ» —
исходный UTF-8 прочитали как CP1251 и снова записали в UTF-8. Преобразование
обратимо: берём текст, кодируем в CP1251 и декодируем как UTF-8.

Некоторые файлы испорчены дважды (после первой починки снова видно «Р В Р Вµ»),
поэтому проход повторяется до устойчивого результата.

Запуск:
    python tools/repair_text_encoding.py --check   # только показать файлы
    python tools/repair_text_encoding.py           # починить
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TARGET_DIRS = ("src", "tools")
SUFFIXES = (".java", ".py", ".gradle", ".properties", ".md", ".json", ".ps1", ".txt")
SKIP_DIRS = {"build", ".gradle", "ref-src", "ref-neoforge", "ref-fml", ".git", "run"}

# Биграммы, которых не бывает в настоящем русском тексте: именно они выдают
# «UTF-8, прочитанный как CP1251». Второй символ — след байта 0x80–0xBF.
_CP1251_TAILS = ("\u0402\u0403\u0405\u0409\u040a\u040c\u040e\u040f"
                 "\u0452\u0453\u0455\u0459\u045a\u045c\u045e\u045f"
                 "\u2018\u2019\u201a\u201c\u201d\u201e\u2020\u2021\u2026"
                 "\u2030\u2039\u203a\u2122\u02dc\u00b5\u00ab\u00bb")
MOJIBAKE = re.compile(
    "Р[" + _CP1251_TAILS + "]"
    "|С[" + _CP1251_TAILS + "]"
    "|в[\u0402\u0403]"      # «вЂ™», «вЂ”», «вЂњ» — следы байтов 0x80–0x82
    "|\ufffd"
)


def looks_broken(text):
    return bool(MOJIBAKE.search(text))


def repair_once(text):
    try:
        return text.encode("cp1251").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return None


def repair(text):
    """Столько проходов, сколько нужно; None, если починить не удалось."""
    current = text
    for _ in range(4):
        if not looks_broken(current):
            return current if current != text else None
        fixed = repair_once(current)
        if fixed is None:
            return None
        current = fixed
    return current if not looks_broken(current) else None


def main():
    check_only = "--check" in sys.argv
    repaired, failed = [], []
    for target in TARGET_DIRS:
        for dirpath, dirnames, files in os.walk(os.path.join(ROOT, target)):
            dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
            for name in files:
                if not name.endswith(SUFFIXES):
                    continue
                path = os.path.join(dirpath, name)
                rel = os.path.relpath(path, ROOT)
                if name == os.path.basename(__file__):
                    continue        # в этом файле маркеры перечислены намеренно
                try:
                    with open(path, encoding="utf-8") as f:
                        original = f.read()
                except UnicodeDecodeError:
                    failed.append(rel + " (не UTF-8)")
                    continue
                if not looks_broken(original):
                    continue
                fixed = repair(original)
                if fixed is None:
                    failed.append(rel + " (починка невозможна)")
                    continue
                if not check_only:
                    with open(path, "w", encoding="utf-8", newline="") as f:
                        f.write(fixed)
                repaired.append(rel)

    print(("требуют починки: " if check_only else "починено: ") + str(len(repaired)))
    for rel in repaired:
        print("  -", rel)
    if failed:
        print("не удалось: " + str(len(failed)))
        for rel in failed:
            print("  !", rel)
    return 0


if __name__ == "__main__":
    sys.exit(main())
