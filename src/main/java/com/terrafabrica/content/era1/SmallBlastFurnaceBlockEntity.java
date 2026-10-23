package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.MultiblockFurnaceBlockEntity;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Малая домна (мультиблок 3×3×5 из огнеупорного кирпича): до 1500 °C.
 * 1 слиток чугуна за 30 с: 4 концентрата + 2 кокса + 1 известняк → 2 чугуна + 1 шлак.
 */
public class SmallBlastFurnaceBlockEntity extends MultiblockFurnaceBlockEntity {

    public SmallBlastFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1773.15, 80_000, 12_000,
                MultiblockHelper.solidBox(3, 5, 3, TFBlocks.REFRACTORY_BRICK.get()));
    }

    @Override
    protected String machineId() {
        return "small_blast_furnace";
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.SMALL_BLAST_FURNACE.get();
    }
}
