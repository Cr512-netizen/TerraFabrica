package com.terrafabrica.content.era2;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Датчик (температуры / давления / уровня / расхода): считывает параметр
 * машины, на которую смотрит, и выдаёт редстоун-сигнал 0–15 пропорционально.
 * Тип датчика определяется блоком.
 */
public class SensorBlockEntity extends AbstractMachineBlockEntity {

    public SensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0, 0, 0, 1_000, 400, 0, 0, 4, 4, 0, 0);
    }

    private String sensorType() {
        Block block = getBlockState().getBlock();
        if (block == com.terrafabrica.content.TFBlocks.SENSOR_PRESSURE.get()) return "pressure";
        if (block == com.terrafabrica.content.TFBlocks.SENSOR_LEVEL.get()) return "level";
        if (block == com.terrafabrica.content.TFBlocks.SENSOR_FLOW.get()) return "flow";
        return "temperature";
    }

    @Override
    protected void tickServer() {
        Direction facing = getBlockState().getValue(com.terrafabrica.content.shared.machine.MachineBlock.FACING);
        BlockEntity target = level.getBlockEntity(worldPosition.relative(facing));
        double value = 0;
        if (target instanceof AbstractMachineBlockEntity machine) {
            value = switch (sensorType()) {
                case "pressure" -> machine.pressurePa() / 1_000_000.0;
                case "level" -> machine.inputTank().getFluidAmount() / 1_000.0;
                case "flow" -> machine.getMaxInput() / 1_000.0;
                default -> Units.kelvinToCelsius(machine.getTemperature());
            };
        }
        int signal = switch (sensorType()) {
            case "pressure" -> (int) Units.clamp(value / 5.0 * 15.0, 0, 15);   // 5 МПа = 15
            case "level" -> (int) Units.clamp(value / 16.0 * 15.0, 0, 15);     // 16 kМБ = 15
            case "flow" -> (int) Units.clamp(value / 200.0 * 15.0, 0, 15);     // 200 кДж/тик = 15
            default -> (int) Units.clamp(value / 2000.0 * 15.0, 0, 15);        // 2000 °C = 15
        };
        level.setBlock(worldPosition, getBlockState(), 3);
        setSignalOutput(signal);
    }

    private void setSignalOutput(int signal) {
        // Редстоун-выход через соседний блок (эмулируется состоянием LIT).
        if (signal != lastSignal) {
            lastSignal = signal;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            setChanged();
        }
    }

    private int lastSignal;

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.sensor_" + sensorType());
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
