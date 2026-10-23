package com.terrafabrica.content.shared.power;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.core.api.TerraFabricaCapabilities;
import com.terrafabrica.core.api.energy.IEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Кабель электросети.
 *
 * <p>Модель потока энергии — диффузионная: энергия за тик перетекает от
 * производителей (генератор, аккумулятор, трансформатор) через цепочку кабелей
 * к потребителям (двигатели, печи, электролизёры). На каждом блоке кабеля
 * считаются потери {@code I²·R} (I = P/U), при превышении предельного тока
 * кабель отключается на 5 с (защита), а соседний автоматический выключатель
 * выдаёт сигнал редстоуна.
 */
public class CableBlockEntity extends AbstractMachineBlockEntity {

    /** Буфер кабеля: 1 МДж. */
    private static final long CABLE_BUFFER_J = 1_000_000L;
    /** Время отключения при перегрузке, тик. */
    private static final int OVERLOAD_COOLDOWN = 100;

    private final CableTier tier;
    private int overloaded;

    public CableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, CableTier tier) {
        super(type, pos, state,
                CABLE_BUFFER_J, 200_000, 200_000,
                1_000, 400,
                0, 0,
                4, 4, 0, 0);
        this.tier = tier;
    }

    public CableTier tier() {
        return tier;
    }

    public boolean isOverloaded() {
        return overloaded > 0;
    }

    @Override
    protected void tickServer() {
        if (overloaded > 0) {
            overloaded--;
            updateBlockState(false);
            status = STATUS_NO_POWER;
            return;
        }
        long maxPerTransfer = (long) (tier.maxPower() / 20.0); // Вт → Дж/тик

        // 1) Приём от производителей (по всем сторонам).
        for (Direction side : Direction.values()) {
            if (energy.getStoredEnergy() >= CABLE_BUFFER_J) break;
            BlockPos neighborPos = worldPosition.relative(side);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be == null || be == this) continue;
            if (be instanceof AbstractMachineBlockEntity machine
                    && machine.getMaxOutput() > 0
                    && machineCanFeed(be)) {
                long wanted = Math.min(maxPerTransfer, CABLE_BUFFER_J - energy.getStoredEnergy());
                long got = machine.extractEnergy(wanted, false);
                long moved = transferWithLoss(got, true);
                energy.receiveEnergy(moved, false);
            }
        }

        // 2) Отдача потребителям и соседним кабелям.
        long stored = energy.getStoredEnergy();
        if (stored <= 0) {
            status = STATUS_IDLE;
            return;
        }
        for (Direction side : Direction.values()) {
            if (energy.getStoredEnergy() <= 0) break;
            BlockPos neighborPos = worldPosition.relative(side);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be == null || be == this) continue;
            if (be instanceof CableBlockEntity other) {
                if (other.overloaded > 0 || other.energy.getStoredEnergy() >= CABLE_BUFFER_J) continue;
                long move = Math.min(maxPerTransfer / 2, energy.getStoredEnergy());
                long delivered = transferWithLoss(move, false);
                energy.extractEnergy(move, false);
                other.energy.receiveEnergy(delivered, false);
            } else if (be instanceof AbstractMachineBlockEntity machine
                    && machine.getMaxInput() > 0
                    && machineCanAccept(be)) {
                long move = Math.min(maxPerTransfer / 2, energy.getStoredEnergy());
                long delivered = transferWithLoss(move, false);
                long accepted = machine.receiveEnergy(delivered, false);
                energy.extractEnergy(move - (delivered - accepted), false);
            }
        }
        status = energy.getStoredEnergy() > 0 ? STATUS_WORKING : STATUS_IDLE;
        updateBlockState(energy.getStoredEnergy() > 0);
    }

    /** Источник напряжения должен соответствовать классу кабеля. */
    private boolean machineCanFeed(BlockEntity be) {
        double v = be instanceof IVoltageSource source ? source.nominalVoltage() : 1_000.0;
        return v <= tier.voltage;
    }

    /** Потребитель принимает напряжение кабеля? */
    private boolean machineCanAccept(BlockEntity be) {
        if (be instanceof IVoltageConsumer consumer) {
            return consumer.maxVoltage() >= tier.voltage;
        }
        return tier.voltage <= 1_000.0; // машины без класса — низковольтные
    }

    /**
     * Передача с потерями: {@code loss = I²·R}, I = P/U. При I > Imax кабель
     * перегружается и отключается.
     */
    private long transferWithLoss(long joules, boolean inbound) {
        double power = joules * 20.0;
        double current = power / tier.voltage;
        if (current > tier.maxCurrent) {
            overloaded = OVERLOAD_COOLDOWN;
            notifyBreakers();
            return 0;
        }
        double lossWatts = current * current * tier.resistance;
        double lossJoules = lossWatts / 20.0;
        return Math.max(0, joules - (long) lossJoules);
    }

    /** Сигнал редстоуна от соседнего выключателя при перегрузке. */
    private void notifyBreakers() {
        for (Direction side : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(side));
            if (be instanceof CircuitBreakerBlockEntity breaker) {
                breaker.onTrip();
            }
        }
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        return true;
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.cable_" + tier.name().toLowerCase());
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }

    /** Получает энергию от соседнего кабеля (для проверки класса). */
    public boolean canAcceptFrom(CableBlockEntity other) {
        return other.tier.voltage <= tier.voltage || tier == CableTier.STEEL;
    }
}
