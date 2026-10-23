package com.terrafabrica.content.era1;

import com.terrafabrica.config.TFServerConfig;
import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.shared.machine.MachineBlock;
import com.terrafabrica.content.shared.machine.MultiblockHelper;
import com.terrafabrica.content.shared.machine.MultiblockMachineBlockEntity;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import com.terrafabrica.core.util.Units;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Паровой двигатель (мультиблок 3×3×4 из паровой обшивки).
 *
 * <p>Компоненты (в крафте): котёл, цилиндр (Уатт — двойного действия),
 * конденсатор (Уатт — отдельный), балансир (Ньюкомен), насос, золотник и
 * центробежный регулятор (Уатт) — см. рецепты.
 *
 * <p>Физика: котёл греется топливом (уголь) до образования пара; давление пара
 * растёт с температурой; мощность — функция давления. Вода расходуется;
 * работа «всухую» перегревает котёл → редкий взрыв с уроном (конфигурируется).
 *
 * <p>Параметры — из TFServerConfig (мощность, КПД, расход угля, время запуска).
 */
public class SteamEngineBlockEntity extends MultiblockMachineBlockEntity {

    public static final int FUEL_SLOT = 3;
    /** Рабочее давление, Па (максимум при полном прогреве). */
    private final double maxPressurePa;
    private final SteamEngineType engineType;
    private final Random random = new Random();

    private int startupProgress;
    private int burnTicksRemaining;

    public SteamEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, SteamEngineType engineType) {
        super(type, pos, state,
                0, 0, 0,
                120_000, 623.15,          // котёл до 350 °C
                0, 0,
                16_000, 0,
                MultiblockHelper.solidBox(3, 4, 3, TFBlocks.STEAM_CASING.get()));
        this.engineType = engineType;
        this.maxPressurePa = engineType == SteamEngineType.NEWCOMEN ? 150_000.0 : 400_000.0;
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
        heat.coolToAmbient(false);

        // Топка.
        if (burnTicksRemaining > 0) {
            burnTicksRemaining--;
            heat.addHeat(8_000 / 20.0, false);
            // Расход воды на пар (мБ/тик). Без воды котёл перегревается.
            if (inputTank.getFluidAmount() >= 10) {
                inputTank.drain(10, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            }
        }
        if (burnTicksRemaining <= 0) {
            ItemStack fuel = inventory.getStackInSlot(FUEL_SLOT);
            int burnTime = fuel.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0 && heat.getTemperature() < heat.getMaxTemperature() * 0.98) {
                fuel.shrink(1);
                burnTicksRemaining = burnTime;
            }
        }

        // Перегрев котла (работа всухую) → редкий взрыв с уроном.
        if (TFServerConfig.BOILER_EXPLOSIONS_ENABLED.get()
                && heat.getTemperature() > 600
                && inputTank.getFluidAmount() == 0
                && random.nextInt(600) == 0) {
            explode();
            return;
        }

        // Запуск: давление нарастает со временем.
        int startupTicks = engineType == SteamEngineType.NEWCOMEN
                ? TFServerConfig.NEWCOMEN_STARTUP_TICKS.get() : 1_200;
        double pressure = pressurePa();
        if (pressure > 80_000 && startupProgress < startupTicks) {
            startupProgress++;
        } else if (pressure < 50_000 && startupProgress > 0) {
            startupProgress = Math.max(0, startupProgress - 2);
        }
        double startFraction = (double) startupProgress / startupTicks;

        double minP = engineType == SteamEngineType.NEWCOMEN
                ? TFServerConfig.NEWCOMEN_POWER_MIN.get() : TFServerConfig.WATT_POWER_MIN.get();
        double maxP = engineType == SteamEngineType.NEWCOMEN
                ? TFServerConfig.NEWCOMEN_POWER_MAX.get() : TFServerConfig.WATT_POWER_MAX.get();
        double power = Units.clampLerp(pressure, 90_000, maxPressurePa, 0, maxP)
                * Math.max(0.05, startFraction);
        double rpm = engineType == SteamEngineType.NEWCOMEN ? 30 : 60;
        rotation.setPower(Math.min(power, maxP), rpm);
        updateBlockState(power > minP * 0.1);
        status = power > 0 ? STATUS_WORKING : STATUS_IDLE;
    }

    /** Давление пара: функция температуры и наличия воды. */
    @Override
    public double pressurePa() {
        double t = heat.getTemperature();
        if (t <= 373.15) return 101_325.0;
        boolean hasWater = inputTank.getFluidAmount() > 0;
        double hot = Units.clamp((t - 373.15) / (600 - 373.15), 0, 1);
        return 101_325.0 + hot * (maxPressurePa - 101_325.0) * (hasWater ? 1.0 : 0.6);
    }

    private void explode() {
        if (level == null) return;
        level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5, 3.0F, Level.ExplosionInteraction.TNT);
    }

    @Override
    public RecipeType<MachineRecipe> recipeType() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terrafabrica." + (engineType == SteamEngineType.NEWCOMEN
                ? "steam_engine_newcomen" : "steam_engine_watt"));
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.terrafabrica.content.shared.machine.MachineMenu(containerId, playerInventory, this, dataAccess);
    }

    /** Публичный доступ к текущему давлению для GUI. */
    public double currentPressurePa() {
        return pressurePa();
    }
}
