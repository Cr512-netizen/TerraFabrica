package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.FurnaceBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Горн с мехами: до 1300 °C — медь, бронза, чугун. Требует привода мехов
 * (вращение ≥ 500 Вт), иначе температура падает и плавка останавливается.
 */
public class BloomeryBlockEntity extends FurnaceBlockEntity {

    /** Мощность привода мехов, Вт. */
    public static final double BELLOWS_POWER_W = 500;

    public BloomeryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1573.15, 40_000, 8_000);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.BLOOMERY.get();
    }

    @Override
    protected String blockId() {
        return "bloomery";
    }

    @Override
    protected void tickServer() {
        // Мехи качаются только при наличии привода.
        double available = rotation.getPower();
        rotation.reset();
        if (available >= BELLOWS_POWER_W) {
            heat.coolToAmbient(false);
            burner.tick(inventory, FUEL_SLOT);
            tryWork(available, rotation.getRPM());
        } else {
            heat.coolToAmbient(false);
            status = STATUS_NO_POWER;
            progress = 0;
            currentRecipe = null;
        }
        updateBlockState(burner.isBurning());
    }
}
