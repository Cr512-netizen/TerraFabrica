package com.terrafabrica.content.era1;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFItems;
import com.terrafabrica.content.shared.transport.AbstractLocomotiveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Малый паровоз: уголь + вода, котёл 16 л, тяга ≈36 кВт на полной топке.
 *
 * <p>Тепловой баланс (параметры подобраны из СИ-физики):
 * <ul>
 *   <li>топка 450 кВт тепловых — один уголёк (24 МДж) горит ≈53 с;</li>
 *   <li>испарение 0.2 л/с (теплота парообразования 2.26 МДж/кг), значит полного
 *       котла 16 л хватает примерно на один уголёк;</li>
 *   <li>КПД паровой машины 8 % — на колёсах ≈36 кВт, что даёт ≈18 км/ч;</li>
 *   <li>сухой котёл: пар уходит, вода кончается, через 15 с — взрыв котла.</li>
 * </ul>
 */
public class SteamLocomotiveEntity extends AbstractLocomotiveEntity {

    /** Тепловая мощность топки, Вт. */
    public static final double FIRE_POWER_W = 450_000.0;
    /** КПД паровой машины. */
    public static final double EFFICIENCY = 0.08;
    /** Ёмкость котла, мБ (16 л). */
    public static final int BOILER_CAPACITY_MB = 16_000;
    /** Паспортная скорость 20 км/ч = 0.278 б/т, с запасом 0.30. */
    public static final double MAX_SPEED_BLOCKS_PER_TICK = 0.30;

    public SteamLocomotiveEntity(EntityType<? extends SteamLocomotiveEntity> type, Level level) {
        super(type, level);
    }

    public SteamLocomotiveEntity(Level level, double x, double y, double z) {
        super(com.terrafabrica.content.TFEntities.STEAM_LOCOMOTIVE.get(), level, x, y, z);
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
        return BOILER_CAPACITY_MB;
    }

    @Override
    protected double maxSpeedBlocksPerTick() {
        return MAX_SPEED_BLOCKS_PER_TICK;
    }

    @Override
    protected boolean explodesOnDryFire() {
        return true;
    }

    @Override
    protected boolean emitsSteam() {
        return true;
    }

    @Override
    protected String tankContentKey() {
        return "fluid.terrafabrica.boiler_water";
    }

    @Override
    protected boolean isFuelItem(ItemStack stack) {
        return energyOf(stack) > 0.0;
    }

    @Override
    protected boolean acceptFuelItem(ItemStack stack, Player player) {
        double joules = energyOf(stack);
        if (joules <= 0.0) {
            return false;
        }
        addFuelJoules(joules);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return true;
    }

    @Override
    protected boolean fillTank(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            if (addTankMb(1000) && !player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean canRefillFromEnvironment() {
        return true;
    }

    @Override
    protected void refillFromEnvironment() {
        BlockPos pos = this.blockPosition();
        if (this.isInWater() || this.level().isRainingAt(pos)) {
            // Конденсатор добирает воду из окружения: 25 мБ за тик (0.5 л/с).
            addTankMb(25);
        }
    }

    @Override
    protected Item getDropItem() {
        return TFItems.STEAM_LOCOMOTIVE.get();
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return TFBlocks.STEAM_CASING.get().defaultBlockState();
    }
}
