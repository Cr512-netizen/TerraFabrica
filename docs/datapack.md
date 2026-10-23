# Датапак: рецепты, теги, генерация

Всё, что описывает процессы и мир, лежит в датапаке: `src/main/resources/data/terrafabrica/`.
Мод не хардкодит рецепты в Java, поэтому их можно переопределять и добавлять без правки кода.

## Раскладка

```
data/terrafabrica/
  recipe/<машина>/<имя>.json     249 рецептов в 28 каталогах
  loot_table/blocks/*.json       244 таблицы добычи (породы, машины, обшивки, стекло)
  tags/block/porodes/**          61 тег: по минералам, по богатству, по эпохам
  tags/item/{rocks,powders,concentrates,ingots,plates,wires,tools}
  tags/fluid/gases.json
  worldgen/placed_feature/*.json 167 фич генерации
data/minecraft/tags/block/       mineable/pickaxe, needs_stone_tool, needs_iron_tool
data/neoforge/biome_modifier/    terrafabrica_ores.json — подключение фич к биомам
```

## Формат машинного рецепта

Имя каталога = идентификатор типа рецепта (`terrafabrica:separator`), имя файла — путь рецепта.

```json
{
  "type": "terrafabrica:separator",
  "ingredients": [{ "item": "terrafabrica:copper_powder" }],
  "counts": [2],
  "results": [{ "id": "terrafabrica:copper_concentrate", "count": 1 }],
  "energy": 50000,
  "ticks": 100,
  "research": "ore_enrichment"
}
```

| Поле | Тип | Смысл |
| --- | --- | --- |
| `type` | id | тип рецепта; должен совпадать с каталогом |
| `ingredients` | список предметов | входные слоты; поддерживаются теги (`"tag": "..."`) |
| `counts` | список чисел | точное количество каждого ингредиента (по умолчанию 1) |
| `results` | список `{id, count}` | выходные предметы |
| `energy` | Дж | энергия на цикл (0 — процесс идёт за счёт топлива) |
| `ticks` | тики | длительность цикла, 20 тиков = 1 секунда |
| `research` | id узла | рецепт скрыт, пока узел не открыт |
| `fluid_in` | `{id, amount}` | расход жидкости, мБ за цикл |
| `fluid_out` | `{id, amount}` | выход жидкости, мБ за цикл |
| `min_temperature` | K | минимальная температура машины |
| `min_pressure` | Па | минимальное давление (турбины, синтез) |
| `group` | строка | группировка в книге рецептов |

Поле `counts` — то, чем сепаратор отличается от дробилки: `"counts": [2]` при одном
ингредиенте означает «нужно два порошка». `MachineRecipe.matches()` проверяет
`stack.getCount() < counts[i]`, поэтому обойти это, положив один предмет, нельзя.

Рецепты крафта верстака лежат отдельно в `recipe/crafting/` и используют
`terrafabrica:research_shaped` / `terrafabrica:research_shapeless` — те же ванильные
формы с дополнительным полем `research`. Ванильные рецепты при этом не перезаписываются:
если у рецепта нет `research`, он доступен всегда.

## Примеры процессов

Обжиг пирита (жидкости + температура, без энергии):

```json
{
  "type": "terrafabrica:pyrite_roaster",
  "ingredients": [{ "item": "terrafabrica:pyrite_concentrate" }],
  "results": [],
  "ticks": 400,
  "research": "chemistry_sulfuric",
  "fluid_in":  { "id": "terrafabrica:oxygen", "amount": 500 },
  "fluid_out": { "id": "terrafabrica:sulfur_dioxide", "amount": 1000 },
  "min_temperature": 1073.0
}
```

Бронза (два ингредиента, точные количества, 10 слитков на выходе):

```json
{
  "type": "terrafabrica:bloomery",
  "ingredients": [
    { "item": "terrafabrica:copper_concentrate" },
    { "item": "terrafabrica:tin_concentrate" }
  ],
  "counts": [9, 1],
  "results": [{ "id": "terrafabrica:bronze_ingot", "count": 10 }],
  "energy": 0,
  "ticks": 600,
  "min_temperature": 1223.0,
  "research": "bronze_metallurgy"
}
```

## Как добавить свой рецепт

1. Создайте `data/<ваш_датапак>/recipe/separator/my_recipe.json`.
2. Укажите `type` существующей машины (`terrafabrica:crusher`, `terrafabrica:separator`,
   `terrafabrica:bloomery`, ...). Свой тип рецепта требует Java-регистрации.
3. Если нужен гейтинг — поставьте `research` с id существующего узла или своего
   (свои узлы добавляются расширением дерева исследований через API `ResearchTree`).
4. Перезагрузите датапак командой `/reload`.

## Теги

| Тег | Что содержит |
| --- | --- |
| `terrafabrica:porodes` | все блоки пород (камень и глубинный сланец) |
| `terrafabrica:porodes/<минерал>` | все варианты одного минерала: 3 богатства × 2 породы |
| `terrafabrica:porodes/poor`, `normal`, `rich` | все породы данной категории богатства |
| `terrafabrica:rocks`, `powders`, `concentrates` | предметы цепочки обогащения |
| `terrafabrica:ingots/<металл>` | слитки одного металла (для рецептов «любой слиток») |
| `terrafabrica:plates`, `wires`, `tools` | прокат, проволока, инструменты |
| `terrafabrica:fluid/gases` | газы (кислород, азот, SO₂, аммиак и др.) |

Теги в пространстве имён `minecraft` (`mineable/pickaxe`, `needs_stone_tool`,
`needs_iron_tool`) расширяют ванильные множества, а не заменяют их: кирки, уровни
инструментов и дроп работают как в ванили.

## Генерация пород

Каждая порода — отдельная `placed_feature` с инлайн-конфигом руды
(отдельного `configured_feature` нет, чтобы не плодить файлы):

```json
{
  "feature": {
    "type": "minecraft:ore",
    "config": {
      "size": 8,
      "discard_chance_on_air_exposure": 0,
      "targets": [
        { "target": { "predicate_type": "minecraft:block_match", "block": "minecraft:stone" },
          "state": { "Name": "terrafabrica:copper_porode_poor" } }
      ]
    }
  },
  "placement": [
    { "type": "minecraft:count", "count": 8 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:height_range",
      "height": { "type": "minecraft:uniform",
                  "min_inclusive": { "absolute": 0 },
                  "max_inclusive": { "absolute": 64 } } },
    { "type": "minecraft:biome" },
    { "type": "minecraft:rarity_filter", "chance": 0.3 }
  ]
}
```

Богатство задаётся именно `rarity_filter.chance`: 0.3 для бедной, 0.6 для обычной,
0.1 для богатой. Так распределение 30/60/10 % получается без дополнительной логики.
Камень и глубинный сланец — разные `targets` (0–64 и −16–0 по Y), редкие минералы
(уранинит и подобные) имеют меньшие `count`/`size` и узкий Y-диапазон.

Все 167 фич подключены одним `data/neoforge/biome_modifier/terrafabrica_ores.json`,
поэтому мод не трогает ванильные биомы и работает с любыми мод-биомами через тег.

## Плотность и совместимость

* `TFServerConfig.GLOBAL_VEIN_MULTIPLIER` — глобальный множитель, если датапак не хочется править.
* Ванильные руды не удалены: медь, железо, уголь и всё остальное генерируются как раньше.
* Рецепты ванильного крафта не перезаписаны: спорные предметы (слитки, стекло, палки)
  получаются и старыми, и новыми путями.

## Проверка целостности

```bash
python tools/validate_data.py    # ссылки: предметы, теги, исследования, типы рецептов
python tools/audit_resources.py  # id в Java против файлов ресурсов
```

Плюс тест `DataPackIntegrityTest` (5 тестов) проверяет то же самое из Java: типы рецептов,
ключи локализации для исследований и предметов, разрешимость тегов, равенство
`counts.size()` и `ingredients.size()`, 162 блока пород, 167 фич, паритет ru/en и звуки.
