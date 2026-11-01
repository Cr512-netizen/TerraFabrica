package com.terrafabrica.content.era2;

import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFEntities;
import com.terrafabrica.content.TFItems;
import com.terrafabrica.content.shared.transport.AbstractVehicleEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Судно (корабль): дизельный двигатель с винтом, бак 50 л дизеля.
 * Движется по воде, не по рельсам. Управляется игроком (WASD).
 *
 * <p>Энергетика:
 * <ul>
 *   <li>дизель: 35.4 кДж/мБ, бак 50 л = 1.77 ГДж;</li>
 *   <li>мощность двигателя 500 кВт, КПД 35% → 175 кВт на винт;</li>
 *   <li>полного бака хватает на ~55 минут полной тяги;</li>
 *   <li>макс. скорость 12 узлов ≈ 6.2 м/с ≈ 0.31 б/т.</li>
 * </ul>
 */
public class ShipEntity extends AbstractVehicleEntity {

    /** Низшая теплота сгорания дизеля в джоулях на мБ. */
    public static final double DIESEL_J_PER_MB = 35_400.0;
    /** Тепловая мощность двигателя, Вт. */
    public static final double ENGINE_POWER_W = 500_000.0;
    /** КПД дизельного двигателя с винтом. */
    public static final double EFFICIENCY = 0.35;
    /** Ёмкость бака, мБ (50 л). */
    public static final int TANK_CAPACITY_MB = 50_000;
    /** Максимальная скорость, блоков за тик. */
    public static final double MAX_SPEED_BLOCKS_PER_TICK = 0.31;

    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TANK_FILL =
            SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);

    private static final String TAG_DRY = "DryTicks";
    private int dryTicks = 0;

    public ShipEntity(EntityType<? extends ShipEntity> type, Level level) {
        super(type, level);
    }

    public ShipEntity(Level level, double x, double y, double z) {
        super(TFEntities.SHIP.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected double enginePowerW() {
        return ENGINE_POWER_W;
    }

    @Override
    protected double efficiency() {
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
    protected boolean explodesOnDry() {
        return false;
    }

    @Override
    protected double fuelEnergyPerMb() {
        return DIESEL_J_PER_MB;
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
    protected boolean canRefillFromEnvironment() {
        return false;
    }

    @Override
    protected String tankContentKey() {
        return "terrafabrica:diesel";
    }

    @Override
    public Item getDropItem() {
        return TFItems.SHIP.get();
    }

    public BlockState getDefaultDisplayBlockState() {
        return TFBlocks.IRON_CASING.get().defaultBlockState();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ACTIVE, false);
        builder.define(DATA_TANK_FILL, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            clientTickEffects();
            return;
        }

        double power = tickEngine();

        if (power > 0 && this.tankMb() <= 0) {
            this.dryTicks++;
            if (this.dryTicks >= 300) {
                handleDryEngine();
                return;
            }
        } else {
            this.dryTicks = 0;
        }

        this.entityData.set(DATA_ACTIVE, power > 0 && this.tankMb() > 0);
        this.entityData.set(DATA_TANK_FILL, tankFillPercent());

        // Движение под управлением игрока
        if (power > 0 && this.getControllingPassenger() instanceof Player player) {
            moveUnderControl(player, power);
        } else {
            // Торможение по воде
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
    }

    private void handleDryEngine() {
        this.level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE,
                SoundSource.NEUTRAL, 1.0F, 0.5F);
        this.setTankMb(0);
    }

    @Override
    protected void clientTickEffects() {
        if (!this.entityData.get(DATA_ACTIVE)) {
            return;
        }
        if (this.random.nextInt(20) == 0) {
            this.level().addParticle(ParticleTypes.SPLASH,
                    this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                    this.getY() + 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                    0.0, 0.1, 0.0);
        }
    }

    @Override
    protected void moveUnderControl(Player player, double power) {
        Vec3 look = player.getLookAngle();
        double targetSpeed = maxSpeedBlocksPerTick();
        Vec3 motion = this.getDeltaMovement();
        double currentSpeed = motion.horizontalDistance();

        if (currentSpeed < targetSpeed) {
            double accel = 0.02;
            this.setDeltaMovement(
                    motion.x + look.x * accel,
                    motion.y,
                    motion.z + look.z * accel);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isFuelItem(stack)) {
            if (!this.level().isClientSide() && acceptFuelItem(stack, player)) {
                player.displayClientMessage(
                        Component.translatable("message.terrafabrica.locomotive.fuel", tankContentName()), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (!this.level().isClientSide()) {
            player.displayClientMessage(Component.translatable("message.terrafabrica.locomotive.status",
                    tankMb(), tankCapacityMb(),
                    Math.round(shaftPowerW() / 1000.0),
                    Math.round(targetSpeed() * 20.0 * 3.6)), false);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_DRY, this.dryTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.dryTicks = tag.getInt(TAG_DRY);
    }

    @Override
    public String debugState() {
        return String.format("бак=%d/%d мБ, P=%.1f кВт, v=%.2f б/т",
                this.tankMb(), tankCapacityMb(), shaftPowerW() / 1000.0, targetSpeed());
    }
}