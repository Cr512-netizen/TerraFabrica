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
 * Парогазовая установка (мультиблок 8×8×12): газовая турбина + котёл-утилизатор
 * + паровая турбина. Мощность 50–500 МВт, КПД 45–50%. Топливо — природный газ,
 * вода — для котла-утилизатора. Выход — вращение.
 */
public class CcgtPlantBlockEntity extends MultiblockMachineBlockEntity {

    public CcgtPlantBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                200_000, 1773.15,
                3_000, 300_000,
                64_000, 0,
                MultiblockHelper.solidBox(8, 12, 8, TFBlocks.STEAM_CASING.get()));
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
        int gasPerTick = 120; // мБ/тик
        int waterPerTick = 40;
        boolean ok = inputTank.getFluidAmount() >= gasPerTick;
        if (ok) {
            inputTank.drain(gasPerTick, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            heat.addHeat(60_000_000 / 20.0 * 0.55, false);
            double temp = heat.getTemperature();
            double power = Units.clampLerp(temp, 1_273.15, 1_773.15, 50_000_000, 500_000_000);
            double efficiency = Units.clampLerp(temp, 1_273.15, 1_773.15, 0.45, 0.50);
            rotation.setPower(power * efficiency, 3_000);
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
        return Component.translatable("block.terrafabrica.ccgt_plant");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
