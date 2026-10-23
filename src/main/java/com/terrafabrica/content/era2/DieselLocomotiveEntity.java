package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFEntities;
import com.terrafabrica.content.TFItems;
import com.terrafabrica.content.shared.transport.AbstractLocomotiveEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Тепловоз: дизель-электрическая передача, бак 12 л, тяга ≈315 кВт.
 *
 * <p>Энергетика (СИ):
 * <ul>
 *   <li>дизель: низшая теплота сгорания 42.6 МДж/кг, плотность 0.83 кг/л →
 *       {@value #DIESEL_J_PER_MB} Дж на 1 мБ;</li>
 *   <li>топка 900 кВт тепловых, КПД дизель-генератора с тяговыми двигателями
 *       35 % → 315 кВт на ободе;</li>
 *   <li>полного бака 12 л (425 МДж) хватает на ≈8 минут полной тяги;</li>
 *   <li>скорость ограничена рельсовой физикой вагонеток (8 м/с ≈ 29 км/ч):
 *       магистральные 120 км/ч появятся только с отдельным типом пути
 *       (см. docs/machines.md, раздел «Транспорт»).</li>
 * </ul>
 *
 * <p>Без топлива тепловоз не взрывается — просто глохнет.
 */
public class DieselLocomotiveEntity extends AbstractLocomotiveEntity {

    /** Низшая теплота сгорания дизеля в джоулях на мБ. */
    public static final double DIESEL_J_PER_MB = 35_400.0;
    /** Тепловая мощность, Вт. */
    public static final double FIRE_POWER_W = 900_000.0;
    /** КПД дизель-электрической передачи. */
    public static final double EFFICIENCY = 0.35;
    /** Ёмкость бака, мБ (12 л). */
    public static final int TANK_CAPACITY_MB = 12_000;
    /** Предел рельсовой физики вагонетки, блоков за тик. */
    public static final double MAX_SPEED_BLOCKS_PER_TICK = 0.40;

    public DieselLocomotiveEntity(EntityType<? extends DieselLocomotiveEntity> type, Level level) {
        super(type, level);
    }

    public DieselLocomotiveEntity(Level level, double x, double y, double z) {
        super(TFEntities.DIESEL_LOCOMOTIVE.get(), level, x, y, z);
    }

    @Override
    protected double firePowerW() {
        return FIRE_POWER_W;
    }

    @Override
    protected double thermalEfficiency() {
        return EFFICIENCY;
    }

    @Override
    protected int tankCapacityMb() {
        return TANK_CAPACITY_MB;
    }

    @Override
    protected double maxSpeedBlocksPerTick() {
        return MAX_SPEED_BLOCKS_PER_TICK;
    }

    @Override
    protected boolean explodesOnDryFire() {
        return false;
    }

    @Override
    protected boolean emitsSteam() {
        return false;
    }

    @Override
    protected String tankContentKey() {
        return "fluid_type.terrafabrica.diesel";
    }

    /** У тепловоза нет котловой воды: бак и есть топливо. */
    @Override
    protected boolean consumesCoolant() {
        return false;
    }

    @Override
    protected double tickBurner() {
        double demandJ = firePowerW() / 20.0; // Дж за тик
        double availableJ = tankMb() * DIESEL_J_PER_MB;
        if (availableJ <= 0.0) {
            this.lastThermalPowerW = 0.0;
            return 0.0;
        }
        double usedJ = Math.min(demandJ, availableJ);
        drainTankMb(usedJ / DIESEL_J_PER_MB);
        this.lastThermalPowerW = usedJ * 20.0;
        return this.lastThermalPowerW;
    }

    @Override
    protected boolean isFuelItem(ItemStack stack) {
        return stack.is(TFItems.DIESEL_CANISTER.get());
    }

    @Override
    protected boolean acceptFuelItem(ItemStack stack, Player player) {
        if (tankMb() + TFItems.DIESEL_CANISTER_MB > tankCapacityMb()) {
            return false;
        }
        addTankMb(TFItems.DIESEL_CANISTER_MB);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return true;
    }

    @Override
    protected boolean fillTank(Player player, InteractionHand hand, ItemStack stack) {
        return false;
    }

    @Override
    protected boolean canRefillFromEnvironment() {
        return false;
    }

    @Override
    protected void refillFromEnvironment() {
        // Тепловоз не заправляется из окружения.
    }

    @Override
    protected Item getDropItem() {
        return TFItems.DIESEL_LOCOMOTIVE.get();
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return TFBlocks.IRON_CASING.get().defaultBlockState();
    }
}
