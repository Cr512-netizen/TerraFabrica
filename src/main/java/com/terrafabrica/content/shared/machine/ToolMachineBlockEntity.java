package com.terrafabrica.content.shared.machine;

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
 * База станков Эпохи I: токарный, сверлильный, фрезерный, шлифовальный,
 * прокатный, механический молот (+ станки Эпохи II: тяжёлый прокатный,
 * волочильный).
 *
 * <p>Станки — одноблочные (осознанное решение, см. docs/DESIGN_DECISIONS.md).
 * Требуют привод в диапазоне оборотов {@code [minRpm, maxRpm]}; энергию
 * напрямую не принимают (перевод электричества в вращение — электродвигатель).
 */
public abstract class ToolMachineBlockEntity extends AbstractMachineBlockEntity {

    private final RecipeType<MachineRecipe> recipeType;
    private final double minRpm;
    private final double maxRpm;
    private final double requiredPower;

    protected ToolMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     RecipeType<MachineRecipe> recipeType,
                                     double minRpm, double maxRpm, double requiredPower) {
        super(type, pos, state,
                0, 0, 0,
                1_500, 700,
                maxRpm, 3_000,
                4, 4,
                0, 0);
        this.recipeType = recipeType;
        this.minRpm = minRpm;
        this.maxRpm = maxRpm;
        this.requiredPower = requiredPower;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return recipeType;
    }

    @Override
    protected double requiredPowerWatts() {
        return requiredPower;
    }

    @Override
    protected boolean tryWork(double availablePower, double availableRpm) {
        // Станок требует обороты в рабочем диапазоне.
        if (availableRpm < minRpm || availableRpm > maxRpm || availablePower < requiredPower) {
            status = STATUS_NO_POWER;
            progress = 0;
            currentRecipe = null;
            return false;
        }
        return super.tryWork(availablePower, availableRpm);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica." + machineId());
    }

    /** id машины (для названия). */
    protected abstract String machineId();

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
