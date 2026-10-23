package com.terrafabrica.content.shared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terrafabrica.core.research.IResearchGatedRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Универсальный машинный рецепт Terra Fabrica (датапак-driven).
 *
 * <p>Один класс на все машины (дробилка, сепаратор, печи, станки, химия,
 * турбины...); различаются они {@code RecipeType}, чьи экземпляры объявлены в
 * {@code TFRecipeTypes}, а JSON-сериализатор зарегистрирован по одному на тип.
 * Тип и сериализатор подставляются в объект при чтении (см. codec/streamCodec
 * ниже) — {@link #getType()} и {@link #getSerializer()} всегда возвращают
 * реальные значения, как того требует интерфейс {@link Recipe}.
 *
 * <p>Формат JSON (в {@code data/terrafabrica/recipe/&lt;type&gt;/}):
 * <pre>{@code
 * {
 *   "type": "terrafabrica:crusher",
 *   "ingredients": [{"item": "terrafabrica:copper_rock"}],
 *   "results":    [{"item": "terrafabrica:copper_powder", "count": 1}],
 *   "energy": 50000,        // Дж на цикл
 *   "ticks": 100,           // длительность цикла (5 с)
 *   "research": "ore_enrichment",   // опционально: требуемое исследование
 *   "fluid_in":  {"id": "terrafabrica:steam_low", "amount": 500},   // опц., мБ
 *   "fluid_out": {"id": "terrafabrica:sulfuric_acid", "amount": 1200},
 *   "min_temperature": 293.0,  // опц., K — минимальная температура процесса
 *   "min_pressure": 20000000.0 // опц., Па — минимальное давление процесса
 * }
 * }</pre>
 *
 * <p>Вход — {@link MachineRecipeInput}; валидация исследования выполняется
 * машиной (BE) через {@link com.terrafabrica.core.research.RecipeGating}.
 */
public class MachineRecipe implements Recipe<MachineRecipeInput>, IResearchGatedRecipe {

    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;
    private final String group;
    private final List<Ingredient> ingredients;
    /** Точные количества каждого ингредиента (по умолчанию 1) — для весовых дозаторов. */
    private final List<Integer> counts;
    private final List<ItemStack> results;
    private final long energy;          // Дж на цикл
    private final int ticks;            // тик на цикл
    @Nullable private final String node;            // требуемое исследование
    @Nullable private final FluidStack inputFluid;  // мБ
    @Nullable private final FluidStack outputFluid; // мБ
    private final double minTemperature; // K
    private final double minPressure;    // Па

    public MachineRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group,
                         List<Ingredient> ingredients, List<Integer> counts, List<ItemStack> results,
                         long energy, int ticks, Optional<String> node,
                         Optional<FluidStack> inputFluid, Optional<FluidStack> outputFluid,
                         double minTemperature, double minPressure) {
        this.type = type;
        this.serializer = serializer;
        this.group = group;
        this.ingredients = List.copyOf(ingredients);
        this.counts = List.copyOf(counts);
        this.results = List.copyOf(results);
        this.energy = energy;
        this.ticks = ticks;
        this.node = node.orElse(null);
        this.inputFluid = inputFluid.orElse(null);
        this.outputFluid = outputFluid.orElse(null);
        this.minTemperature = minTemperature;
        this.minPressure = minPressure;
    }

    @Override
    public boolean matches(MachineRecipeInput input, net.minecraft.world.level.Level level) {
        if (input.size() < ingredients.size()) return false;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(input.getItem(i))) return false;
            if (input.getItem(i).getCount() < count(i)) return false;
        }
        return true;
    }

    /** Точное количество ингредиента {@code i} (1 по умолчанию). */
    public int count(int i) {
        return i >= 0 && i < counts.size() ? counts.get(i) : 1;
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, net.minecraft.core.HolderLookup.Provider registries) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0).copy();
    }

    /** Результат для слота результата {@code index} (уже скопирован). */
    public ItemStack getResultItem(int index) {
        return index >= 0 && index < results.size() ? results.get(index).copy() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registries) {
        return assemble(null, registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    // ------------------------------------------------------------------
    // Геттеры данных рецепта
    // ------------------------------------------------------------------

    public String group() {
        return group;
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    /** Точные количества ингредиентов (для весовых дозаторов). */
    public List<Integer> counts() {
        return counts;
    }

    public List<ItemStack> results() {
        return results;
    }

    /** Энергия на цикл, Дж. */
    public long energy() {
        return energy;
    }

    /** Длительность цикла, тик. */
    public int ticks() {
        return ticks;
    }

    @Override
    @Nullable
    public String requiredNode() {
        return node;
    }

    @Nullable
    public FluidStack inputFluid() {
        return inputFluid;
    }

    @Nullable
    public FluidStack outputFluid() {
        return outputFluid;
    }

    /** Минимальная температура процесса, K (0 — без требования). */
    public double minTemperature() {
        return minTemperature;
    }

    /** Минимальное давление процесса, Па (0 — без требования). */
    public double minPressure() {
        return minPressure;
    }

    // ------------------------------------------------------------------
    // Codec / StreamCodec. Тип и сериализатор подставляются при чтении.
    // ------------------------------------------------------------------

    public static com.mojang.serialization.MapCodec<MachineRecipe> codec(RecipeType<?> type, RecipeSerializer<?> serializer) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(MachineRecipe::group),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(MachineRecipe::ingredients),
                Codec.INT.listOf().optionalFieldOf("counts").forGetter(r -> r.counts.isEmpty()
                        ? Optional.empty() : Optional.of(r.counts)),
                ItemStack.CODEC.listOf().fieldOf("results").forGetter(MachineRecipe::results),
                Codec.LONG.fieldOf("energy").forGetter(MachineRecipe::energy),
                Codec.INT.fieldOf("ticks").forGetter(MachineRecipe::ticks),
                Codec.STRING.optionalFieldOf("research").forGetter(r -> Optional.ofNullable(r.node)),
                FluidStack.CODEC.optionalFieldOf("fluid_in").forGetter(r -> Optional.ofNullable(r.inputFluid)),
                FluidStack.CODEC.optionalFieldOf("fluid_out").forGetter(r -> Optional.ofNullable(r.outputFluid)),
                Codec.DOUBLE.optionalFieldOf("min_temperature", 0.0).forGetter(MachineRecipe::minTemperature),
                Codec.DOUBLE.optionalFieldOf("min_pressure", 0.0).forGetter(MachineRecipe::minPressure)
        ).apply(instance, (group, ingredients, counts, results, energy, ticks, node, fluidIn, fluidOut, minT, minP) ->
                new MachineRecipe(type, serializer, group, ingredients,
                        counts.orElseGet(() -> java.util.Collections.nCopies(ingredients.size(), 1)),
                        results, energy, ticks, node, fluidIn, fluidOut, minT, minP)));
    }

    /*
     * Потоковый кодек написан «руками»: у StreamCodec нет перегрузки composite
     * на 11 полей, а Ordering-подход с вложенными record'ами здесь избыточен.
     * Порядок чтения/записи строго одинаков (см. ниже).
     */
    public static StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec(RecipeType<?> type,
                                                                                  RecipeSerializer<?> serializer) {
        var ingredientsCodec = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());
        var countsCodec = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list());
        var resultsCodec = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list());
        var nodeCodec = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional);
        var fluidCodec = FluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional);

        return StreamCodec.of(
                (buf, r) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buf, r.group);
                    ingredientsCodec.encode(buf, r.ingredients);
                    countsCodec.encode(buf, r.counts);
                    resultsCodec.encode(buf, r.results);
                    ByteBufCodecs.VAR_LONG.encode(buf, r.energy);
                    ByteBufCodecs.VAR_INT.encode(buf, r.ticks);
                    nodeCodec.encode(buf, Optional.ofNullable(r.node));
                    fluidCodec.encode(buf, Optional.ofNullable(r.inputFluid));
                    fluidCodec.encode(buf, Optional.ofNullable(r.outputFluid));
                    ByteBufCodecs.DOUBLE.encode(buf, r.minTemperature);
                    ByteBufCodecs.DOUBLE.encode(buf, r.minPressure);
                },
                buf -> {
                    String group = ByteBufCodecs.STRING_UTF8.decode(buf);
                    List<Ingredient> ingredients = ingredientsCodec.decode(buf);
                    List<Integer> counts = countsCodec.decode(buf);
                    List<ItemStack> results = resultsCodec.decode(buf);
                    long energy = ByteBufCodecs.VAR_LONG.decode(buf);
                    int ticks = ByteBufCodecs.VAR_INT.decode(buf);
                    Optional<String> node = nodeCodec.decode(buf);
                    Optional<FluidStack> fluidIn = fluidCodec.decode(buf);
                    Optional<FluidStack> fluidOut = fluidCodec.decode(buf);
                    double minT = ByteBufCodecs.DOUBLE.decode(buf);
                    double minP = ByteBufCodecs.DOUBLE.decode(buf);
                    return new MachineRecipe(type, serializer, group, ingredients, counts, results,
                            energy, ticks, node, fluidIn, fluidOut, minT, minP);
                });
    }
}
