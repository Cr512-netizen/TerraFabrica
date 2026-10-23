package com.terrafabrica.core.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terrafabrica.content.TFRecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * Shaped-рецепт крафта, заблокированный исследованием.
 *
 * <p>Формат JSON идентичен ванильному shaped + поле {@code "research": "<node>"}.
 * Проверка доступности выполняется в {@code CraftingMenuMixin} по attachment
 * игрока (в мультиплеере — у каждого игрока свой прогресс).
 *
 * <p>Реализация: поля родителя {@code ShapedRecipe} ({@code group}, {@code result},
 * {@code category}, {@code showNotification}) имеют package-private доступ, поэтому
 * мод хранит собственные копии и использует их в кодеках.
 */
public class ResearchShapedRecipe extends ShapedRecipe implements IResearchGatedRecipe {

    private final String node;
    private final String groupValue;
    private final CraftingBookCategory categoryValue;
    private final ItemStack resultValue;
    private final boolean notifyValue;

    public ResearchShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                                ItemStack result, boolean showNotification, String node) {
        super(group, category, pattern, result, showNotification);
        this.node = node;
        this.groupValue = group;
        this.categoryValue = category;
        this.resultValue = result;
        this.notifyValue = showNotification;
    }

    @Override
    public String requiredNode() {
        return node;
    }

    /** Группа рецепта (для кодека). */
    public String groupValue() {
        return groupValue;
    }

    /** Категория книги рецептов (для кодека). */
    public CraftingBookCategory categoryValue() {
        return categoryValue;
    }

    /** Результат крафта (для кодека). */
    public ItemStack resultValue() {
        return resultValue;
    }

    /** Показывать ли уведомление о новом рецепте (для кодека). */
    public boolean notifyValue() {
        return notifyValue;
    }

    @Override
    public String getGroup() {
        return groupValue;
    }

    @Override
    public CraftingBookCategory category() {
        return categoryValue;
    }

    @Override
    public boolean showNotification() {
        return notifyValue;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TFRecipeSerializers.RESEARCH_SHAPED.get();
    }

    public static class Serializer implements RecipeSerializer<ResearchShapedRecipe> {

        public static final MapCodec<ResearchShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ResearchShapedRecipe::groupValue),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                        .forGetter(ResearchShapedRecipe::categoryValue),
                ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
                ItemStack.CODEC.fieldOf("result").forGetter(ResearchShapedRecipe::resultValue),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ResearchShapedRecipe::notifyValue),
                Codec.STRING.fieldOf("research").forGetter(ResearchShapedRecipe::requiredNode)
        ).apply(instance, ResearchShapedRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ResearchShapedRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ResearchShapedRecipe::groupValue,
                        CraftingBookCategory.STREAM_CODEC, ResearchShapedRecipe::categoryValue,
                        ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
                        ItemStack.STREAM_CODEC, ResearchShapedRecipe::resultValue,
                        ByteBufCodecs.BOOL, ResearchShapedRecipe::notifyValue,
                        ByteBufCodecs.STRING_UTF8, ResearchShapedRecipe::requiredNode,
                        ResearchShapedRecipe::new);

        @Override
        public MapCodec<ResearchShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ResearchShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
