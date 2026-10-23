package com.terrafabrica.content.era1;

import com.terrafabrica.config.TFServerConfig;
import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.core.util.Units;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Водяное колесо: 5–30 кВт, КПД 60%, RPM 20–60. Ставится в поток воды —
 * мощность зависит от силы потока под колесом (текущая вода в соседнем блоке
 * снизу/сбоку). Производит вращение для сети валов.
 */
public class WaterWheelBlockEntity extends AbstractMachineBlockEntity {

    public WaterWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                5_000, 500,
                60, 500,
                4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        double flow = measureFlow();
        double power = Units.clampLerp(flow, 0, 8,
                0, TFServerConfig.WATER_WHEEL_MAX_POWER.get())
                * TFServerConfig.WATER_WHEEL_EFFICIENCY.get();
        double rpm = Units.clampLerp(flow, 0, 8, 0, 60);
        rotation.setPower(power, rpm);
        updateBlockState(power > 100);
    }

    /** Сила потока: число соседних блоков с текущей водой (скорость 1–8). */
    private double measureFlow() {
        double flow = 0;
        Direction facing = getBlockState().getValue(com.terrafabrica.content.shared.machine.MachineBlock.FACING);
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            var fluid = level.getFluidState(worldPosition.relative(dir));
            if (!fluid.isEmpty() && fluid.is(net.minecraft.tags.FluidTags.WATER) && fluid.isSource()) {
                flow += 2;
            }
        }
        return flow;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null; // генератор — без рецептов
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.water_wheel");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
