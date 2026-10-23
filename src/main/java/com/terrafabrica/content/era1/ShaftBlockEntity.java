package com.terrafabrica.content.era1;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.core.api.rotation.IRotationHandler;
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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Вал: передаёт вращение по сети валов от источника (колесо, ветряк, паровой
 * двигатель, турбина, электродвигатель) к потребителям (станки, дробилки...).
 *
 * <p>Каждые 10 тиков выполняется BFS к ближайшему источнику; потери — 1% на
 * блок вала (ремни — 10–20%, см. баланс в docs/BALANCE.md). Один блок вала
 * передаёт до 100 кВт; для больших мощностей — редукторы и прямые приводы.
 */
public class ShaftBlockEntity extends AbstractMachineBlockEntity {

    /** Интервал пересчёта сети, тик. */
    private static final int REFRESH_INTERVAL = 10;
    /** Максимальная длина линии валов. */
    private static final int MAX_DISTANCE = 64;
    /** Потери на блок вала, 1%. */
    private static final double LOSS_PER_BLOCK = 0.99;
    /** Предел передаваемой мощности, Вт. */
    public static final double MAX_POWER_W = 100_000;

    private int refreshCooldown;
    private double networkRpm;
    private double networkTorque;

    public ShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                0, 0, 0,
                1_000, 400,
                3_000, 5_000,
                4, 4, 0, 0);
    }

    @Override
    protected void tickServer() {
        if (--refreshCooldown <= 0) {
            refreshCooldown = REFRESH_INTERVAL;
            refreshNetwork();
        }
        // Отдаём вращение соседним машинам через capability.
        rotation.setPower(networkTorque > 0 && networkRpm > 0
                ? com.terrafabrica.core.util.Units.rotationPower(networkRpm, networkTorque) : 0, networkRpm);
        updateBlockState(rotation.getPower() > 0);
    }

    /** BFS к ближайшему источнику вращения. */
    private void refreshNetwork() {
        networkRpm = 0;
        networkTorque = 0;
        if (level == null) return;

        Queue<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, Integer> distance = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(worldPosition);
        distance.put(worldPosition, 0);
        visited.add(worldPosition);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            int dist = distance.get(current);
            if (dist >= MAX_DISTANCE) continue;

            for (Direction side : Direction.values()) {
                BlockPos next = current.relative(side);
                if (!visited.add(next)) continue;
                BlockEntity be = level.getBlockEntity(next);
                if (be instanceof ShaftBlockEntity) {
                    distance.put(next, dist + 1);
                    queue.add(next);
                } else if (be instanceof IRotationHandler handler && handler.getPower() > 0) {
                    double loss = Math.pow(LOSS_PER_BLOCK, dist + 1);
                    double power = Math.min(handler.getPower() * loss, MAX_POWER_W);
                    double rpm = handler.getRPM();
                    networkRpm = rpm;
                    networkTorque = rpm > 0 ? power * 60.0 / (2.0 * Math.PI * rpm) : 0;
                    return;
                }
            }
        }
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica.shaft");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }
}
