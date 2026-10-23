package com.terrafabrica.content.shared.power;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
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
 * Подстанция: узел распределения с трансформаторами, выключателями и защитой.
 * Работает как крупный буфер (10 МДж) между сетями; при перегрузке соседнего
 * кабеля отключает прилегающие линии.
 */
public class SubstationBlockEntity extends AbstractMachineBlockEntity {

    public SubstationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                10_000_000, 1_000_000, 1_000_000,
                5_000, 500,
                0, 0,
                4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        // Пассивный узел: энергия обменивается через capability кабелями.
        status = energy.getStoredEnergy() > 0 ? STATUS_WORKING : STATUS_IDLE;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.substation");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
