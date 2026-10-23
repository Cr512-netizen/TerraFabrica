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
 * Паровая турбина (мультиблок 3×3×5): давление 1–3 МПа, температура 300–400 °C,
 * мощность 100 кВт–10 МВт, КПД 15–25%, выход — вращение 1500–3000 RPM.
 *
 * <p>Потребляет пар высокого давления из входного бака. Мощность — функция
 * давления и температуры пара; при недогретом паре КПД падает.
 */
public class SteamTurbineBlockEntity extends MultiblockMachineBlockEntity {

    public SteamTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                30_000, 673.15,
                3_000, 10_000,
                16_000, 0,
                MultiblockHelper.solidBox(3, 5, 3, TFBlocks.STEAM_CASING.get()));
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
        // Давление и температура входного пара.
        double pressure = pressurePa();
        double temp = heat.getTemperature();
        double steamPerTick = Units.clamp(pressure / 3_000_000.0, 0, 1) * 200; // мБ/тик
        if (inputTank.getFluidAmount() >= steamPerTick && pressure > 1_000_000 && temp > 573.15) {
            inputTank.drain((int) steamPerTick, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            double power = Units.clampLerp(pressure, 1_000_000, 3_000_000, 100_000, 10_000_000);
            // КПД 15–25%: растёт с температурой пара.
            double efficiency = Units.clampLerp(temp, 573.15, 673.15, 0.15, 0.25);
            double rpm = Units.clampLerp(temp, 573.15, 673.15, 1_500, 3_000);
            rotation.setPower(power * efficiency, rpm);
            status = STATUS_WORKING;
            updateBlockState(true);
        } else {
            rotation.reset();
            status = pressure <= 1_000_000 ? STATUS_LOW_PRESSURE : STATUS_NO_FLUID;
            updateBlockState(false);
        }
    }

    @Override
    public double pressurePa() {
        // Давление пара в баке растёт с его температурой (пар подаётся котлом).
        double t = heat.getTemperature();
        if (t <= 373.15) return 101_325.0;
        return 101_325.0 + Units.clamp((t - 373.15) / (673.15 - 373.15), 0, 1) * 3_000_000;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.steam_turbine");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
