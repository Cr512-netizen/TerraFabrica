package com.terrafabrica.content.shared.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * База многоблочных машин: раз в {@link #STRUCTURE_CHECK_INTERVAL} тиков (и по
 * запросу) проверяет раскладку {@link MultiblockHelper.Part} вокруг контроллера.
 *
 * <p>Пока структура не собрана, машина не работает (статус
 * {@code STATUS_NO_STRUCTURE}). Проверка кэшируется, чтобы не сканировать мир
 * каждый тик.
 */
public abstract class MultiblockMachineBlockEntity extends AbstractMachineBlockEntity {

    /** Интервал повторной проверки структуры, тик (2 с). */
    public static final int STRUCTURE_CHECK_INTERVAL = 40;

    /** Раскладка структуры в локальных координатах. */
    protected final List<MultiblockHelper.Part> structure;
    protected boolean formed;
    protected int checkCooldown;

    protected MultiblockMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                           long capacityJ, long maxInputJ, long maxOutputJ,
                                           double heatCapacity, double maxTempK,
                                           double maxRpm, double maxTorque,
                                           int inputTankCap, int outputTankCap,
                                           List<MultiblockHelper.Part> structure) {
        super(type, pos, state, capacityJ, maxInputJ, maxOutputJ, heatCapacity, maxTempK,
                maxRpm, maxTorque, 4, 4, inputTankCap, outputTankCap);
        this.structure = new java.util.ArrayList<>(structure);
    }

    /** Собрана ли структура (по последней проверке). */
    public boolean isFormed() {
        return formed;
    }

    /** Перепроверка структуры (вызывается при изменении соседних блоков). */
    public void invalidateStructure() {
        checkCooldown = 0;
    }

    @Override
    protected void tickServer() {
        if (--checkCooldown <= 0) {
            checkCooldown = STRUCTURE_CHECK_INTERVAL;
            formed = checkStructure();
        }
        if (!formed) {
            status = STATUS_NO_STRUCTURE;
            progress = 0;
            currentRecipe = null;
            updateBlockState(false);
            return;
        }
        super.tickServer();
    }

    /** Проверка раскладки (поворот по направлению контроллера). */
    protected boolean checkStructure() {
        var state = getBlockState();
        var facing = state.hasProperty(MachineBlock.FACING)
                ? state.getValue(MachineBlock.FACING)
                : net.minecraft.core.Direction.NORTH;
        return MultiblockHelper.matches(level, worldPosition, facing, structure);
    }

    /** Помечает структуру невалидной при изменении соседних блоков. */
    public static <T extends BlockEntity> void onNeighborChange(Level level, BlockPos pos, BlockState state, T be) {
        if (be instanceof MultiblockMachineBlockEntity multiblock) {
            multiblock.invalidateStructure();
        }
    }
}
