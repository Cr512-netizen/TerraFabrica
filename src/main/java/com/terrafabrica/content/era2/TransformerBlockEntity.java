package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.machine.MultiblockMachineBlockEntity;
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
 * Трансформатор (мультиблок 2×2×3): до 110 кВ, КПД 98–99%.
 *
 * <p>Первичная сторона — за блоком контроллера, вторичная — перед ним:
 * понижение 110 кВ → 10 кВ → 1 кВ и обратное повышение. Энергия передаётся
 * между сетями через внутренний буфер с потерями 1.5%.
 */
public class TransformerBlockEntity extends MultiblockMachineBlockEntity
        implements IVoltageSource, IVoltageConsumer {

    /** Номинальное напряжение вторичной (передней) стороны, В. */
    private double secondaryVoltage = 1_000.0;
    /** Коэффициент трансформации (первичная/вторичная). */
    private double ratio = 10.0;

    public TransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                5_000_000, 1_000_000, 1_000_000,
                10_000, 500,
                0, 0,
                0, 0,
                MultiblockHelper.solidBox(2, 3, 2, TFBlocks.IRON_CASING.get()));
    }

    @Override
    public double nominalVoltage() {
        return secondaryVoltage;
    }

    @Override
    public double maxVoltage() {
        return 110_000.0;
    }

    /** Цикл напряжений: 1 кВ → 10 кВ → 110 кВ (переключается ПКМ пустой рукой). */
    public void cycleVoltage() {
        secondaryVoltage = secondaryVoltage >= 110_000.0 ? 1_000.0 : secondaryVoltage * 10.0;
        ratio = 110_000.0 / secondaryVoltage;
        setChanged();
    }

    public double secondaryVoltage() {
        return secondaryVoltage;
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
        // Пассивный узел: потери 1.5% при передаче через буфер.
        long stored = energy.getStoredEnergy();
        if (stored > 0) {
            energy.extractEnergy(Math.max(1, stored / 1000), false);
        }
        status = STATUS_IDLE;
        updateBlockState(false);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.transformer");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
