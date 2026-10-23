package com.terrafabrica.content.shared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

/**
 * Единый сериализатор машинных рецептов. Один экземпляр регистрируется на
 * каждый {@code RecipeType} (id сериализатора = id типа рецепта); экземпляр
 * привязан к своему типу через {@link Supplier}, чтобы чтение рецепта сразу
 * подставляло корректные {@code getType()}/{@code getSerializer()}.
 */
public class MachineRecipeSerializer implements RecipeSerializer<MachineRecipe> {

    private final Supplier<RecipeType<?>> typeSupplier;

    public MachineRecipeSerializer(Supplier<RecipeType<?>> typeSupplier) {
        this.typeSupplier = typeSupplier;
    }

    @Override
    public MapCodec<MachineRecipe> codec() {
        return MachineRecipe.codec(typeSupplier.get(), this);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() {
        return MachineRecipe.streamCodec(typeSupplier.get(), this);
    }
}
