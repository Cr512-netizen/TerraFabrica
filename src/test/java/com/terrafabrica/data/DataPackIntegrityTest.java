package com.terrafabrica.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ссылочная целостность датапака, проверяемая на тестовом прогоне.
 *
 * <p>Minecraft валится на таких ошибках только при загрузке мира, поэтому здесь те же
 * инварианты проверяются до сборки:
 * <ul>
 *   <li>каждый {@code research} в рецепте имеет подпись в lang (значит, узел существует);</li>
 *   <li>все {@code terrafabrica:} предметы из рецептов имеют подпись в lang;</li>
 *   <li>ссылки на теги мода указывают на существующие файлы тегов;</li>
 *   <li>{@code counts} совпадает по длине с {@code ingredients};</li>
 *   <li>типы рецептов не «выдуманы» — только известные наборы.</li>
 * </ul>
 */
class DataPackIntegrityTest {

    private static final Set<String> KNOWN_MACHINE_TYPES = Set.of(
            "crusher", "separator", "primitive_furnace", "bloomery", "small_blast_furnace",
            "puddling_furnace", "crucible_furnace", "lathe", "drill_press", "milling_machine",
            "grinding_machine", "rolling_mill", "power_hammer", "pyrite_roaster", "lead_chamber",
            "absorber", "solvay_reactor", "calcination_furnace", "coke_oven", "blast_furnace",
            "bessemer_converter", "open_hearth_furnace", "electric_arc_furnace",
            "heavy_rolling_mill", "wire_drawing_machine", "weight_dispenser",
            "air_separation_unit", "water_electrolyzer", "ammonia_synthesizer",
            "ostwald_reactor", "contact_acid_reactor", "chloralkali_cell",
            "aluminum_electrolyzer", "fertilizer_mixer", "oil_distillation_column");

    private static Path resourceRoot() throws URISyntaxException {
        URL url = DataPackIntegrityTest.class.getClassLoader()
                .getResource("data/terrafabrica/recipe");
        assertTrue(url != null, "датапак должен быть на тестовом classpath (processResources)");
        Path recipeRoot = Path.of(url.toURI());
        return recipeRoot.getParent().getParent().getParent();   // .../data/<ns>/recipe → resources
    }

    private static Stream<Path> jsonFiles(Path root, String sub) throws IOException {
        Path dir = root.resolve(sub);
        if (!Files.isDirectory(dir)) return Stream.empty();
        return Files.walk(dir).filter(p -> p.toString().endsWith(".json"));
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }

    private static Set<String> langKeys(Path root, String locale) throws IOException {
        JsonObject lang = readJson(root.resolve("assets/terrafabrica/lang/" + locale + ".json"));
        return lang.keySet();
    }

    @Test
    @DisplayName("Рецепты ссылаются только на существующие исследования и предметы")
    void recipesReferenceRealThings() throws Exception {
        Path root = resourceRoot();
        Set<String> ru = langKeys(root, "ru_ru");
        Set<String> en = langKeys(root, "en_us");

        List<String> problems = new ArrayList<>();
        int recipes = 0;

        try (Stream<Path> files = jsonFiles(root, "data/terrafabrica/recipe")) {
            for (Path file : files.collect(Collectors.toList())) {
                recipes++;
                JsonObject recipe = readJson(file);
                String where = root.relativize(file).toString();

                String type = recipe.get("type").getAsString();
                String path = type.contains(":") ? type.substring(type.indexOf(':') + 1) : type;
                boolean known = type.startsWith("minecraft:")
                        || KNOWN_MACHINE_TYPES.contains(path)
                        || path.equals("research_shaped") || path.equals("research_shapeless");
                if (!known) problems.add(where + ": неизвестный тип " + type);

                if (recipe.has("research")) {
                    String node = recipe.get("research").getAsString();
                    if (!ru.contains("research.terrafabrica." + node)) {
                        problems.add(where + ": нет узла исследования " + node);
                    }
                }

                for (String ref : collectRefs(recipe)) {
                    if (!ref.startsWith("terrafabrica:")) continue;
                    String id = ref.substring("terrafabrica:".length());
                    if (!ru.contains("item.terrafabrica." + id)
                            && !ru.contains("block.terrafabrica." + id)
                            && !ru.contains("fluid_type.terrafabrica." + id)) {
                        problems.add(where + ": нет предмета/блока " + ref);
                    }
                }

                for (String tag : collectTagRefs(recipe)) {
                    String tagPath = tag.replace(':', '/');
                    if (!Files.exists(root.resolve("data/" + tagPath + ".json"))) {
                        problems.add(where + ": нет тега #" + tag);
                    }
                }

                if (recipe.has("counts") && recipe.has("ingredients")) {
                    JsonArray counts = recipe.getAsJsonArray("counts");
                    JsonArray ingredients = recipe.getAsJsonArray("ingredients");
                    if (counts.size() != ingredients.size()) {
                        problems.add(where + ": counts и ingredients разной длины");
                    }
                }

                if (recipe.has("ticks") && recipe.get("ticks").getAsInt() <= 0) {
                    problems.add(where + ": ticks <= 0");
                }

                if (!en.contains("itemGroup.terrafabrica")) {
                    problems.add("en_us: потерян заголовок творческой вкладки");
                }
            }
        }

        assertTrue(recipes > 200, "рецептов должно быть больше двухсот, найдено " + recipes);
        assertTrue(problems.isEmpty(), "проблемы датапака:\n" + String.join("\n", problems));
    }

    @Test
    @DisplayName("Породы покрыты: 27 минералов × 3 категории × камень/глубинный сланец")
    void porodeCoverage() throws Exception {
        Path root = resourceRoot();
        Set<String> blockstates = new LinkedHashSet<>();
        try (Stream<Path> files = jsonFiles(root, "assets/terrafabrica/blockstates")) {
            for (Path file : files.collect(Collectors.toList())) {
                blockstates.add(file.getFileName().toString().replace(".json", ""));
            }
        }
        long porodes = blockstates.stream().filter(n -> n.contains("_porode_")).count();
        assertTrue(porodes == 162, "ожидается 162 блока пород (27 × 3 × 2), найдено " + porodes);
        assertTrue(blockstates.contains("copper_porode_poor"));
        assertTrue(blockstates.contains("deepslate_copper_porode_rich"));
        assertTrue(blockstates.contains("uraninite_porode_normal"));
    }

    @Test
    @DisplayName("Мир: у каждой породы есть placed_feature, биом-модификатор их все знает")
    void worldgenCoverage() throws Exception {
        Path root = resourceRoot();
        Set<String> placed = new LinkedHashSet<>();
        try (Stream<Path> files = jsonFiles(root, "data/terrafabrica/worldgen/placed_feature")) {
            for (Path file : files.collect(Collectors.toList())) {
                placed.add(file.getFileName().toString().replace(".json", ""));
            }
        }
        assertTrue(placed.size() >= 160, "placed_feature должно быть много, найдено " + placed.size());

        // biome_modifier — динамический реестр neoforge:biome_modifier, поэтому
        // запись terrafabrica:<name> лежит в data/<modid>/neoforge/biome_modifier/.
        // Старый путь data/neoforge/... грузится без ошибок, но породы в мир не
        // попадают — проверено на выделенном сервере (docs/decisions.md).
        Path modifier = root.resolve("data/terrafabrica/neoforge/biome_modifier/terrafabrica_ores.json");
        assertTrue(Files.exists(modifier), "нужен biome_modifier, иначе пород не будет в мире");
        assertFalse(Files.exists(root.resolve("data/neoforge/biome_modifier/terrafabrica_ores.json")),
                "biome_modifier не должен лежать в data/neoforge/ — там запись не применяется");
        JsonArray features = readJson(modifier).getAsJsonArray("features");
        for (JsonElement element : features) {
            String ref = element.getAsString().replace("terrafabrica:", "");
            assertTrue(placed.contains(ref), "biome_modifier ссылается на неизвестную фичу " + ref);
        }
    }

    @Test
    @DisplayName("Локализация: наборы ключей ru/en совпадают, плейсхолдеры не потеряны")
    void localizationParity() throws Exception {
        Path root = resourceRoot();
        Set<String> ru = langKeys(root, "ru_ru");
        Set<String> en = langKeys(root, "en_us");
        assertTrue(ru.equals(en), "ru_ru и en_us должны содержать одинаковые ключи");
        assertFalse(ru.isEmpty());

        long subtitles = ru.stream().filter(k -> k.startsWith("subtitle.terrafabrica.")).count();
        assertTrue(subtitles >= 18, "у каждого звукового события должен быть субтитр, есть " + subtitles);

        JsonObject ruJson = readJson(root.resolve("assets/terrafabrica/lang/ru_ru.json"));
        JsonObject enJson = readJson(root.resolve("assets/terrafabrica/lang/en_us.json"));
        for (String key : ru) {
            int ruArgs = countFormatArgs(ruJson.get(key).getAsString());
            int enArgs = countFormatArgs(enJson.get(key).getAsString());
            assertTrue(ruArgs == enArgs,
                    "разное число %s в " + key + ": ru=" + ruArgs + ", en=" + enArgs);
        }
    }

    @Test
    @DisplayName("Руководство: семь глав, заголовки и непрерывная нумерация строк")
    void guideChaptersIntact() throws Exception {
        Path root = resourceRoot();
        Set<String> ru = langKeys(root, "ru_ru");
        assertTrue(ru.contains("screen.terrafabrica.guide"), "нет заголовка экрана руководства");

        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("guide\\.terrafabrica\\.([a-z0-9_]+)\\.line\\.(\\d+)");
        java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
        for (String key : ru) {
            var matcher = pattern.matcher(key);
            if (matcher.matches()) {
                counts.merge(matcher.group(1), Integer.parseInt(matcher.group(2)), Math::max);
            }
        }
        assertEquals(7, counts.size(), "ожидается семь глав руководства, найдено " + counts.keySet());

        JsonObject ruJson = readJson(root.resolve("assets/terrafabrica/lang/ru_ru.json"));
        for (var entry : counts.entrySet()) {
            String id = entry.getKey();
            String title = "guide.terrafabrica." + id + ".title";
            assertTrue(ru.contains(title), "нет заголовка главы " + id);

            long present = ru.stream()
                    .filter(k -> k.startsWith("guide.terrafabrica." + id + ".line."))
                    .count();
            assertEquals(entry.getValue(), (int) present, "в главе " + id + " дыра в нумерации строк");

            for (int i = 1; i <= entry.getValue(); i++) {
                String text = ruJson.get("guide.terrafabrica." + id + ".line." + i).getAsString();
                assertTrue(text.length() > 25, "строка " + id + ".line." + i + " слишком короткая");
            }
        }
    }

    @Test
    @DisplayName("Каждое звуковое событие мода ссылается на существующий звук")
    void soundsAreBackedByRealFiles() throws Exception {
        Path root = resourceRoot();
        JsonObject sounds = readJson(root.resolve("assets/terrafabrica/sounds.json"));
        assertTrue(sounds.size() >= 18, "ожидается минимум 18 звуковых событий");
        for (String event : sounds.keySet()) {
            JsonObject definition = sounds.getAsJsonObject(event);
            JsonArray entries = definition.getAsJsonArray("sounds");
            assertTrue(entries != null && !entries.isEmpty(), "у события " + event + " нет файлов");
            for (JsonElement element : entries) {
                JsonObject entry = element.isJsonObject() ? element.getAsJsonObject() : null;
                String name = entry != null ? entry.get("name").getAsString() : element.getAsString();
                assertTrue(name.contains(":"), "звук " + name + " должен быть с неймспейсом");
                assertFalse(name.startsWith("terrafabrica:"),
                        "мод не поставляет .ogg — событие " + event + " должно брать ванильный файл");
            }
            assertTrue(definition.has("subtitle"), "у события " + event + " нет субтитра");
        }
    }

    // ------------------------------------------------------------------

    private static List<String> collectRefs(JsonElement element) {
        List<String> refs = new ArrayList<>();
        gatherRefs(element, refs);
        return refs;
    }

    private static void gatherRefs(JsonElement element, List<String> out) {
        if (element == null) return;
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) gatherRefs(child, out);
        } else if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (("item".equals(key) || "id".equals(key)) && value.isJsonPrimitive()) {
                    out.add(value.getAsString());
                } else if ("items".equals(key) && value.isJsonArray()) {
                    for (JsonElement child : value.getAsJsonArray()) {
                        if (child.isJsonPrimitive()) out.add(child.getAsString());
                    }
                } else {
                    gatherRefs(value, out);
                }
            }
        }
    }

    private static List<String> collectTagRefs(JsonElement element) {
        List<String> tags = new ArrayList<>();
        gatherTags(element, tags);
        return tags;
    }

    private static void gatherTags(JsonElement element, List<String> out) {
        if (element == null) return;
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) gatherTags(child, out);
        } else if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                JsonElement value = entry.getValue();
                if ("tag".equals(entry.getKey()) && value.isJsonPrimitive()) {
                    out.add(value.getAsString());
                } else if ("item".equals(entry.getKey()) && value.isJsonPrimitive()
                        && value.getAsString().startsWith("#")) {
                    out.add(value.getAsString().substring(1));
                } else {
                    gatherTags(value, out);
                }
            }
        }
    }

    private static int countFormatArgs(String text) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf("%s", index)) >= 0) {
            count++;
            index += 2;
        }
        return count;
    }
}
