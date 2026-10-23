package com.terrafabrica.core.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terrafabrica.content.TFRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

/**
 * Shapeless-рецепт крафта, заблокированный исследованием.
 * Формат: ванильный shapeless + поле {@code "research": "<node>"}.
 */
public class ResearchShapelessRecipe extends ShapelessRecipe implements IResearchGatedRecipe {

    private final String node;

    public ResearchShapelessRecipe(String group, CraftingBookCategory category, ItemStack result,
                                   List<Ingredient> ingredients, String node) {
        super(group, category, result, NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)));
        this.node = node;
    }

    @Override
    public String requiredNode() {
        return node;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TFRecipeSerializers.RESEARCH_SHAPELESS.get();
    }

    public static class Serializer implements RecipeSerializer<ResearchShapelessRecipe> {

        public static final MapCodec<ResearchShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(ShapelessRecipe::getIngredients),
                Codec.STRING.fieldOf("research").forGetter(ResearchShapelessRecipe::requiredNode)
        ).apply(instance, ResearchShapelessRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ResearchShapelessRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ShapelessRecipe::getGroup,
                        CraftingBookCategory.STREAM_CODEC, ShapelessRecipe::category,
                        ItemStack.STREAM_CODEC, r -> r.getResultItem(null),
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ShapelessRecipe::getIngredients,
                        ByteBufCodecs.STRING_UTF8, ResearchShapelessRecipe::requiredNode,
                        ResearchShapelessRecipe::new);

        @Override
        public MapCodec<ResearchShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ResearchShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
