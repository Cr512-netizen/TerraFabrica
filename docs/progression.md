# Дерево исследований

Раздел собран автоматически из `ResearchTree.java`: время указано на сервере
(20 тиков = 1 секунда), названия берутся из `ru_ru.json`.

## Эпоха I — пар и механика

Узлов: **24**. Суммарное время исследований: **30.8 мин** (если открывать по одному).

| Узел | Название | Время | Предпосылки |
| --- | --- | --- | --- |
| `woodworking` | Обработка дерева | 15 с | — |
| `stone_working` | Обработка камня | 15 с | — |
| `copper_metallurgy` | Металлургия меди | 30 с | `woodworking`, `stone_working` |
| `ore_enrichment` | Обогащение руды | 30 с | `woodworking`, `stone_working` |
| `water_wheel` | Водяное колесо | 30 с | `woodworking` |
| `bronze_metallurgy` | Металлургия бронзы | 45 с | `ore_enrichment`, `copper_metallurgy` |
| `transmission` | Механическая трансмиссия | 30 с | `water_wheel` |
| `iron_metallurgy` | Металлургия железа | 60 с | `bronze_metallurgy` |
| `steam_newcomen` | Паровой двигатель Ньюкомена | 90 с | `iron_metallurgy`, `water_wheel` |
| `steam_watt` | Паровой двигатель Уатта | 120 с | `steam_newcomen`, `transmission` |
| `machine_tools_lathe` | Токарный станок | 60 с | `transmission`, `iron_metallurgy` |
| `machine_tools_drill` | Сверлильный станок | 60 с | `machine_tools_lathe` |
| `machine_tools_rolling` | Прокатный стан | 90 с | `machine_tools_lathe`, `iron_metallurgy` |
| `machine_tools_milling` | Фрезерный станок | 60 с | `machine_tools_drill` |
| `machine_tools_hammer` | Механический молот | 90 с | `machine_tools_rolling` |
| `machine_tools_grinding` | Шлифовальный станок | 60 с | `machine_tools_milling` |
| `chemistry_sulfuric` | Серная кислота (камерный способ) | 120 с | `machine_tools_grinding`, `steam_watt` |
| `transport_rails` | Рельсы и шпалы | 60 с | `machine_tools_rolling` |
| `chemistry_soda` | Сода (процесс Сольве) | 120 с | `chemistry_sulfuric` |
| `transport_minecart` | Вагонетки | 60 с | `transport_rails`, `machine_tools_lathe` |
| `chemistry_special_glass` | Специальные стёкла | 120 с | `chemistry_soda` |
| `transport_conveyor` | Ленточный транспортёр | 60 с | `transport_rails`, `transmission` |
| `transport_locomotive` | Малый паровоз | 180 с | `steam_watt`, `transport_minecart` |
| `era2_transition` | Переход к Эпохе II | 240 с | `steam_watt`, `machine_tools_hammer`, `chemistry_special_glass`, `transport_locomotive` |

## Эпоха II — сталь, ток, химия

Узлов: **34**. Суммарное время исследований: **136.0 мин** (если открывать по одному).

| Узел | Название | Время | Предпосылки |
| --- | --- | --- | --- |
| `coke_making` | Коксование | 60 с | `era2_transition` |
| `steam_turbine` | Паровая турбина | 240 с | `era2_transition`, `steam_watt` |
| `blast_furnace_coke` | Домна на коксе | 90 с | `coke_making` |
| `electric_generator` | Электрогенератор | 240 с | `steam_turbine` |
| `bessemer` | Бессемеровский конвертер | 120 с | `blast_furnace_coke` |
| `transformer` | Трансформатор | 180 с | `electric_generator` |
| `power_network` | Электросети | 120 с | `transformer`, `electric_generator` |
| `open_hearth` | Мартеновская печь | 120 с | `bessemer` |
| `electric_motor` | Электродвигатель | 180 с | `electric_generator` |
| `battery` | Аккумуляторы | 180 с | `electric_motor` |
| `electric_arc_furnace` | Электродуговая печь | 180 с | `open_hearth`, `electric_generator` |
| `heavy_rolling` | Тяжёлый прокатный стан | 120 с | `bessemer` |
| `air_separation` | Воздухоразделение | 240 с | `steam_turbine`, `chemistry_special_glass` |
| `wire_drawing` | Волочильный стан | 90 с | `heavy_rolling` |
| `water_electrolysis` | Электролиз воды | 180 с | `air_separation`, `power_network` |
| `oil_refining` | Перегонка нефти | 180 с | `blast_furnace_coke`, `power_network` |
| `gas_turbine` | Газовая турбина | 360 с | `steam_turbine`, `air_separation` |
| `ammonia` | Синтез аммиака | 300 с | `air_separation`, `chemistry_sulfuric` |
| `ccgt` | Парогазовая установка | 480 с | `gas_turbine`, `steam_turbine` |
| `nitric_acid` | Азотная кислота (Оствальд) | 300 с | `ammonia` |
| `contact_sulfuric` | Контактная серная кислота | 240 с | `chemistry_sulfuric`, `air_separation` |
| `chloralkali` | Каустическая сода и хлор | 180 с | `water_electrolysis`, `power_network` |
| `telegraph` | Телеграф | 120 с | `wire_drawing`, `battery` |
| `aluminum` | Электролиз алюминия | 300 с | `chloralkali`, `contact_sulfuric` |
| `fertilizers` | Удобрения | 180 с | `ammonia`, `contact_sulfuric` |
| `telephone` | Телефон | 180 с | `telegraph`, `electric_motor` |
| `diesel_locomotive` | Тепловоз | 360 с | `oil_refining`, `electric_motor` |
| `radio` | Радио | 240 с | `telephone`, `power_network` |
| `ships` | Суда | 360 с | `steam_turbine`, `heavy_rolling` |
| `tube_computer` | Ламповый компьютер | 480 с | `radio` |
| `submarine` | Подводные лодки | 420 с | `ships`, `battery` |
| `automation` | Автоматизация | 240 с | `tube_computer`, `electric_motor` |
| `car` | Автомобили | 300 с | `oil_refining`, `electric_motor` |
| `era3_transition` | Переход к Эпохе III | 600 с | `electric_arc_furnace`, `ccgt`, `tube_computer`, `diesel_locomotive`, `aluminum`, `fertilizers` |
