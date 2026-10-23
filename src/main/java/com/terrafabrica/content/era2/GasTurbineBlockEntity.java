package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.machine.MultiblockMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.core.util.Units;
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
 * Газовая турбина (мультиблок 4×4×6): компрессор + камера сгорания + турбина.
 * Топливо — природный газ или дизель (флюиды); температура газов 1000–1300 °C,
 * степень сжатия 10–20, мощность 1–50 МВт, КПД 25–35%. Выход — вращение.
 */
public class GasTurbineBlockEntity extends MultiblockMachineBlockEntity {

    public GasTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                60_000, 1573.15,
                6_000, 50_000,
                32_000, 0,
                MultiblockHelper.solidBox(4, 6, 4, TFBlocks.REACTOR_CASING.get()));
    }

    @Override
    protected void tickServer() {
        if (--checkCooldown <= 0) {
            checkCooldown = STRUCTURE_CHECK_INTERVAL;
            formed = checkStructure();
        }
        if (!formed) {
            status = STATUS_NO_STRUCTURE;
            updateBlockState(false);
            return;
        }
        // Топливо: природный газ или дизель.
        int fuelPerTick = 20; // мБ/тик
        boolean hasFuel = inputTank.getFluidAmount() >= fuelPerTick;
        if (hasFuel) {
            inputTank.drain(fuelPerTick, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            // Тепловыделение: газы 1000–1300 °C.
            heat.addHeat(12_000_000 / 20.0 * 0.5, false);
            double temp = heat.getTemperature();
            double power = Units.clampLerp(temp, 1_273.15, 1_573.15, 1_000_000, 50_000_000);
            double efficiency = Units.clampLerp(temp, 1_273.15, 1_573.15, 0.25, 0.35);
            rotation.setPower(power * efficiency, 3_600);
            status = STATUS_WORKING;
            updateBlockState(true);
        } else {
            rotation.reset();
            status = STATUS_NO_FLUID;
            updateBlockState(false);
        }
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.gas_turbine");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
