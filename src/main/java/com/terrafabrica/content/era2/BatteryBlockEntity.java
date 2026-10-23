package com.terrafabrica.content.era2;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.power.IVoltageConsumer;
import com.terrafabrica.content.shared.power.IVoltageSource;
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
 * Свинцово-кислотный аккумулятор: 100 А·ч, 12 В → 4.32 МДж.
 * КПД заряд-разряд 80%; ресурс 500 циклов (в текущей реализации не
 * деградирует — см. docs/DESIGN_DECISIONS.md).
 */
public class BatteryBlockEntity extends AbstractMachineBlockEntity
        implements IVoltageSource, IVoltageConsumer {

    /** Ёмкость: 100 А·ч × 12 В = 4.32 МДж. */
    public static final long CAPACITY_J = 4_320_000L;
    /** КПД заряд-разряд. */
    public static final double EFFICIENCY = 0.8;

    public BatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                CAPACITY_J, 12_000, 12_000,     // 12 кДж/тик = 240 кВт
                3_000, 400,
                0, 0,
                4, 4, 0, 0);
    }

    @Override
    public double nominalVoltage() {
        return 12.0;
    }

    @Override
    public double maxVoltage() {
        return 14.4; // зарядное напряжение
    }

    @Override
    protected void tickServer() {
        // КПД заряд-разряд 80% применяется при зарядке (потери — в тепло).
        if (energy.getStoredEnergy() > 0) {
            heat.addHeat(energy.getStoredEnergy() * 0.01, false);
        }
        status = energy.getStoredEnergy() > 0 ? STATUS_WORKING : STATUS_IDLE;
        updateBlockState(energy.getStoredEnergy() > 0);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.lead_acid_battery");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
