#!/usr/bin/env python3
"""
Тексты внутриигрового руководства Terra Fabrica.

Данные вынесены из generate_data.py, чтобы правки текста не тонули в коде
генератора. Структура: (id главы, заголовок RU, заголовок EN,
список пар «строка RU / строка EN»).
Число строк должно совпадать с GuideContent.java — это проверяет
DataPackIntegrityTest (нет дыр в нумерации ключей lang).
"""

GUIDE_CHAPTERS = [
    ("intro", "О моде", "About", [
        ("Научно-техническая прогрессия: от куска породы до электростанции. "
         "Эпоха I — пар и механика, Эпоха II — сталь, электричество и химия.",
         "A science and technology progression: from a rock chunk to a power plant. "
         "Era I is steam and mechanics, Era II is steel, electricity and chemistry."),
        ("Все величины в моде — СИ: энергия в джоулях (Дж), мощность в ваттах (Вт), "
         "температура в кельвинах (K), давление в паскалях (Па), напряжение в вольтах (В), ток в амперах (А).",
         "Every value is SI: energy in joules (J), power in watts (W), temperature in kelvin (K), "
         "pressure in pascals (Pa), voltage in volts (V), current in amperes (A)."),
        ("Внутренняя энергия машин хранится в джоулях. 1 FE = 2.5 Дж; конвертация округляет вниз, "
         "поэтому энергия не появляется из воздуха.",
         "Machine buffers store joules. 1 FE = 2.5 J; conversion rounds down, so energy never appears from nothing."),
        ("Порода заменяет руду: её нельзя просто расплавить — сначала дробление, потом обогащение.",
         "Rock replaces ore: it cannot be smelted directly — crush first, then enrich."),
        ("Прогресс держится на исследованиях: пока узел древа закрыт, машина отвечает «Нужно исследование».",
         "Progress is gated by research: while a tree node is locked a machine answers \"Research required\"."),
        ("Ванильный мир не сломан: руды, рецепты и генерация на месте, породы добавлены рядом.",
         "The vanilla world stays intact: ores, recipes and worldgen remain, rock appears beside them."),
    ]),
    ("units", "Единицы и формулы", "Units and formulas", [
        ("Мощность вала: P = (2π · RPM / 60) · M, где M — момент в Н·м. У каждого вала есть предел момента.",
         "Shaft power: P = (2π · RPM / 60) · M with torque in N·m. Every shaft has a torque limit."),
        ("20 тиков = 1 секунда; 1 кВт·ч = 3.6 МДж. Энергия процесса = мощность × время в секундах.",
         "20 ticks = 1 second; 1 kWh = 3.6 MJ. Process energy is power times seconds."),
        ("Тепло: Q = C · ΔT. У каждой печи своя теплоёмкость и свой предел температуры — перегрев невозможен.",
         "Heat: Q = C · ΔT. Each furnace has its own heat capacity and temperature ceiling."),
        ("КПД паровых машин: Ньюкомен 0.5–1 %, Уатт 5–8 %, турбины 15–25 %. КПД виден в подсказке привода.",
         "Steam efficiency: Newcomen 0.5–1 %, Watt 5–8 %, turbines 15–25 %. The value is shown in the tooltip."),
        ("Электричество: генератор даёт 50 Гц и 10 кВ; трансформаторы меняют напряжение, "
         "кабели трёх классов теряют разную долю мощности.",
         "Electricity: the generator produces 50 Hz at 10 kV; transformers change voltage and the three cable "
         "classes lose different shares of power."),
        ("Паровая турбина требует 1–3 МПа и 300–400 °C: ниже давление — ниже выработка.",
         "A steam turbine needs 1–3 MPa and 300–400 °C: lower pressure means lower output."),
        ("Все числа доступны прямо в GUI: температура, обороты, момент, мощность, статус.",
         "All numbers are visible in the machine GUI: temperature, RPM, torque, power, status."),
    ]),
    ("porodes", "Породы и обогащение", "Rock and enrichment", [
        ("Порода бывает бедная, обычная и богатая — 30 % / 60 % / 10 % месторождений. "
         "Встречается в камне и в глубинном сланце.",
         "Rock comes in poor, normal and rich variants — 30 % / 60 % / 10 % of veins, "
         "in stone and in deepslate."),
        ("Кирка даёт 1, 2 или 3 куска. Шёлковое касание оставляет блок целым, Fortune добавляет куски.",
         "A pickaxe yields 1, 2 or 3 chunks. Silk touch keeps the block intact, Fortune adds chunks."),
        ("Дробилка: 1 кусок → 1 порошок, 50 кДж и 5 секунд работы.",
         "Crusher: 1 chunk to 1 powder, 50 kJ and 5 seconds."),
        ("Сепаратор: 2 порошка → 1 концентрат, ещё 50 кДж. Итого 0.9 концентрата с одной породы — "
         "потеря 10 % видна честно.",
         "Separator: 2 powder to 1 concentrate for another 50 kJ. That is 0.9 concentrate per rock — "
         "a visible 10 % loss."),
        ("Концентрат плавится: медь и олово по одному, железо — по два концентрата на слиток чугуна.",
         "Concentrate smelts: copper and tin one to one, iron two concentrates per pig iron ingot."),
        ("Богатая порода даёт втрое больше кусков, но встречается редко — её стоит искать.",
         "Rich rock yields three times the chunks but is rare — worth hunting for."),
    ]),
    ("era1", "Эпоха I: пар и механика", "Era I: steam and mechanics", [
        ("Печи: примитивная (900 °C), домница (1300 °C), малая доменная 3×3×5 (1500 °C), "
         "пудлинговая 3×3×3, тигельная 2×2×2.",
         "Furnaces: primitive (900 °C), bloomery (1300 °C), small blast furnace 3×3×5 (1500 °C), "
         "puddling 3×3×3, crucible 2×2×2."),
        ("Приводы: водяное колесо 5–30 кВт (КПД 60 %), ветряк до 20 кВт (50 %), "
         "машина Ньюкомена 5–10 кВт, Уатта 20–100 кВт.",
         "Drives: water wheel 5–30 kW (60 % efficiency), windmill up to 20 kW (50 %), "
         "Newcomen engine 5–10 kW, Watt engine 20–100 kW."),
        ("Передача момента: вал, шестерня, ремень, шкив, редуктор с отношениями 1:1, 2:1, 1:2, 1:4, 1:8; "
         "ремень теряет 10–20 %.",
         "Transmission: shaft, gear, belt, pulley, gearbox with 1:1, 2:1, 1:2, 1:4, 1:8 ratios; "
         "a belt loses 10–20 %."),
        ("Станочный парк умышленно одноблочный: токарный, сверлильный, фрезерный, шлифовальный, молот. "
         "Мультиблочность здесь только мешала бы темпу.",
         "Machine tools are deliberately single-block: lathe, drill press, milling, grinding, power hammer. "
         "Multiblocks would only slow the era down."),
        ("Химия: обжиг пирита даёт SO₂, свинцовая камера — серную кислоту 78 %, Сольве — соду, "
         "стекловарка — стекло.",
         "Chemistry: pyrite roasting gives SO₂, the lead chamber gives 78 % sulfuric acid, "
         "Solvay gives soda, the glass furnace gives glass."),
        ("Транспорт: рельсы, вагонетки на три стака, конвейер 1 м/с, малый паровоз 2×3×2.",
         "Transport: rails, minecarts with three stacks, conveyor at 1 m/s, small steam locomotive 2×3×2."),
        ("Финал эпохи — узел «Переход к Эпохе II»: он открывает коксование и сталь.",
         "The era ends with the \"Transition to Era II\" node, which unlocks coking and steel."),
    ]),
    ("era2", "Эпоха II: сталь, ток, химия", "Era II: steel, power, chemistry", [
        ("Коксовая печь даёт кокс, каменноугольную смолу и светильный газ — побочные продукты не выбрасываются.",
         "The coke oven yields coke, coal tar and lighting gas — by-products are never wasted."),
        ("Металлургия: коксовая доменная 5×5×10, бессемер, мартен, дуговая печь 100 кВт и 2000 °C.",
         "Metallurgy: coke blast furnace 5×5×10, Bessemer converter, open hearth, arc furnace at 100 kW and 2000 °C."),
        ("Прокатка и волочение: листы, прутки, проволока трёх классов для разных напряжений.",
         "Rolling and drawing: plates, rods and three wire classes for different voltages."),
        ("Сплавы строгих составов: Fe-18Cr-8Ni, Ni-25Cr-20Co-1.5Nb-1.5Ti-0.5Al, Ti-6Al-4V.",
         "Alloys with strict compositions: Fe-18Cr-8Ni, Ni-25Cr-20Co-1.5Nb-1.5Ti-0.5Al, Ti-6Al-4V."),
        ("Энергия: паровая турбина 3×3×5, газовая турбина, ПГУ, генератор 3×3×3 (50 Гц, 10 кВ), "
         "трансформатор, электромотор, свинцовый аккумулятор.",
         "Power: steam turbine 3×3×5, gas turbine, combined cycle, generator 3×3×3 (50 Hz, 10 kV), "
         "transformer, electric motor, lead-acid battery."),
        ("Химия: воздухоразделение, электролиз воды, аммиак (400–500 °C, 20–30 МПа, Fe-катализатор, "
         "отравляется серой), Оствальд, контактная кислота, электролиз галита.",
         "Chemistry: air separation, water electrolysis, ammonia (400–500 °C, 20–30 MPa, iron catalyst "
         "poisoned by sulfur), Ostwald, contact acid, halite electrolysis."),
        ("Алюминий: электролиз при 960 °C в криолите, 13–15 МВт·ч на тонну, улавливание фтора обязательно.",
         "Aluminium: electrolysis at 960 °C in cryolite, 13–15 MWh per tonne, fluorine capture is mandatory."),
        ("Связь и автоматика: телеграф, телефон, радио, ламповый компьютер, "
         "цепочка датчик → контроллер → исполнительный механизм. Финал — «Переход к Эпохе III».",
         "Communication and automation: telegraph, telephone, radio, tube computer, and the "
         "sensor → controller → actuator chain. The last node is \"Transition to Era III\"."),
    ]),
    ("research", "Исследования", "Research", [
        ("ЛКМ по жёлтому узлу начинает исследование, если открыты предпосылки и хватает ресурсов.",
         "Left-click a yellow node to start research when prerequisites are met and resources are at hand."),
        ("Пока узел закрыт, машина не запускает рецепт и сообщает стоимость: рецепт просто заблокирован.",
         "While a node is locked the machine refuses the recipe and reports the cost — it is simply gated."),
        ("Прогресс идёт на сервере: время и стоимость видны в подсказке узла, клиент только отображает.",
         "Progress ticks on the server: time and costs are in the node tooltip, the client only renders it."),
        ("Эпоха III в этой версии намеренно не реализована — дерево заканчивается переходом к ней.",
         "Era III is intentionally not implemented in this version — the tree ends at its transition node."),
    ]),
    ("datapack", "Датапак и совместимость", "Datapack and compatibility", [
        ("Все процессы описаны рецептами датапака: data/terrafabrica/recipe/<машина>/<имя>.json.",
         "Every process is a datapack recipe: data/terrafabrica/recipe/<machine>/<name>.json."),
        ("Поля рецепта: ingredients, counts, results, energy (Дж), ticks, research, fluid_in, fluid_out, "
         "min_temperature, min_pressure.",
         "Recipe fields: ingredients, counts, results, energy (J), ticks, research, fluid_in, fluid_out, "
         "min_temperature, min_pressure."),
        ("Поле counts задаёт точное количество: так сепаратор требует два порошка на один концентрат.",
         "The counts field sets exact amounts: that is how the separator requires two powder per concentrate."),
        ("Теги: terrafabrica:porodes, ingots/<металл>, plates, wires, tools, а также minecraft:mineable/pickaxe и needs_*_tool.",
         "Tags: terrafabrica:porodes, ingots/<metal>, plates, wires, tools, plus minecraft:mineable/pickaxe and needs_*_tool."),
        ("Скрипты tools/validate_data.py и tools/audit_resources.py плюс тесты ловят расхождения "
         "до запуска игры.",
         "The tools/validate_data.py and tools/audit_resources.py scripts plus the tests catch "
         "mismatches before the game starts."),
    ]),
]

# Заголовок экрана и подписи кнопок
GUIDE_UI_RU = {
    "screen.terrafabrica.guide": "Руководство Terra Fabrica",
    "gui.terrafabrica.guide.page": "Страница %s / %s",
    "gui.terrafabrica.guide.research": "Исследования",
    "gui.terrafabrica.guide.close": "Закрыть",
}

GUIDE_UI_EN = {
    "screen.terrafabrica.guide": "Terra Fabrica Guide",
    "gui.terrafabrica.guide.page": "Page %s / %s",
    "gui.terrafabrica.guide.research": "Research",
    "gui.terrafabrica.guide.close": "Close",
}
