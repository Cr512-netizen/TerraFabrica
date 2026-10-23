package com.terrafabrica.content.era2;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.power.IVoltageConsumer;
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
 * Электродвигатель (2×2×2): асинхронный трёхфазный, 0.5–500 кВт, КПД 85–92%.
 * Выход — вращение (до 3000 RPM). Питание — из сети кабелей ≤ 1 кВ.
 */
public class ElectricMotorBlockEntity extends AbstractMachineBlockEntity implements IVoltageConsumer {

    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                500_000, 100_000, 0,
                2_000, 500,
                3_000, 1_500,
                4, 4, 0, 0);
    }

    @Override
    public double maxVoltage() {
        return 1_000.0; // ≤ 1 кВ (медные кабели/низкая сторона трансформатора)
    }

    @Override
    protected void tickServer() {
        // Электричество → вращение: КПД 85–92% (растёт с нагрузкой).
        long drawn = energy.extractEnergy(500_000 / 20L, false); // до 500 кВт
        if (drawn > 0) {
            double efficiency = 0.85 + 0.07 * Math.min(1.0, (double) drawn / (500_000 / 20.0));
            double power = drawn * 20.0 * efficiency;
            double rpm = 750 + 2_250 * Math.min(1.0, (double) drawn / (500_000 / 20.0));
            rotation.setPower(power, rpm);
            status = STATUS_WORKING;
            updateBlockState(true);
        } else {
            rotation.reset();
            status = STATUS_NO_POWER;
            updateBlockState(false);
        }
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.electric_motor");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
