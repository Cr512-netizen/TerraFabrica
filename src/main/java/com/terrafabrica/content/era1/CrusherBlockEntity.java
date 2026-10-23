package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.config.TFServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Дробилка: 1 кусок породы → 1 порошок. 50 кДж/цикл, 5 с.
 * Привод — вращение (≥10 кВт) ИЛИ электричество; энергозатраты те же.
 */
public class CrusherBlockEntity extends AbstractMachineBlockEntity {

    public CrusherBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                400_000, 200_000, 0,     // буфер 400 кДж, приём 200 кДж/тик (4 МВт)
                2_000, 500,              // теплоёмкость, K
                1_200, 1_500,            // макс RPM, макс момент
                4, 4,
                0, 0);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return TFRecipeTypes.CRUSHER.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.crusher");
    }

    @Override
    protected double requiredPowerWatts() {
        // 50 кДж за 100 тик = 500 Дж/тик = 10 кВт.
        return (double) TFServerConfig.CRUSHER_ENERGY_PER_CYCLE.get()
                / TFServerConfig.CRUSHER_CYCLE_TICKS.get() * 20.0;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
