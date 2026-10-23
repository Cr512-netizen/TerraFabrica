package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.FurnaceBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Примитивная печь (8 булыжников + 1 глина): до 900 °C — олово, свинец, стекло. */
public class PrimitiveFurnaceBlockEntity extends FurnaceBlockEntity {

    public PrimitiveFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1173.15, 30_000, 6_000);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.PRIMITIVE_FURNACE.get();
    }

    @Override
    protected String blockId() {
        return "primitive_furnace";
    }
}
