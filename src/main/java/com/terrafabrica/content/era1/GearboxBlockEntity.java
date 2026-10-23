package com.terrafabrica.content.era1;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.core.api.rotation.IRotationHandler;
import com.terrafabrica.core.util.Units;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Редуктор: меняет передаточное отношение (1:1, 2:1, 1:2, 1:4, 1:8).
 * Переключение — ПКМ с зажатым Shift. Потери редуктора — 5%.
 * Передаточное отношение: выходные обороты = входные × ratio.
 */
public class GearboxBlockEntity extends AbstractMachineBlockEntity {

    private static final double[] RATIOS = {1.0, 2.0, 0.5, 0.25, 0.125};
    private static final double GEARBOX_LOSS = 0.95;

    private int ratioIndex;

    public GearboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                1_000, 400,
                3_000, 5_000,
                4, 4, 0, 0);
    }

    /** Текущее передаточное отношение (выход/вход). */
    public double ratio() {
        return RATIOS[ratioIndex];
    }

    /** Переключение передаточного отношения (ПКМ + Shift). */
    public void cycleRatio() {
        ratioIndex = (ratioIndex + 1) % RATIOS.length;
        setChanged();
    }

    @Override
    protected void tickServer() {
        // Вход — от соседнего источника/вала.
        double inputRpm = 0;
        double inputTorque = 0;
        for (Direction side : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(side));
            if (be instanceof IRotationHandler handler && handler != this && handler.getPower() > 0) {
                inputRpm = handler.getRPM();
                inputTorque = handler.getTorque();
                break;
            }
        }
        double outRpm = inputRpm * ratio();
        double power = Units.rotationPower(inputRpm, inputTorque) * GEARBOX_LOSS;
        rotation.setPower(power, outRpm);
        updateBlockState(power > 0);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ratioIndex = tag.getInt("ratio");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ratio", ratioIndex);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.gearbox");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
