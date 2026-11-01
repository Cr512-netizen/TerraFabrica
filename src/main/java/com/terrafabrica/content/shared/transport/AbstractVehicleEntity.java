package com.terrafabrica.content.shared.transport;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Базовый класс для водных/наземных транспортных средств (не рельсовых).
 * Управляется игроком через WASD, имеет бак топлива и двигатель.
 */
public abstract class AbstractVehicleEntity extends Entity {

    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(AbstractVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TANK_FILL =
            SynchedEntityData.defineId(AbstractVehicleEntity.class, EntityDataSerializers.INT);

    protected int tankMb = 0;

    protected AbstractVehicleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    // Параметры двигателя (задают наследники)
    protected abstract double enginePowerW();
    protected abstract double efficiency();
    protected abstract int tankCapacityMb();
    protected abstract double maxSpeedBlocksPerTick();
    protected abstract boolean explodesOnDry();
    protected abstract double fuelEnergyPerMb();
    protected abstract boolean isFuelItem(ItemStack stack);
    protected abstract boolean acceptFuelItem(ItemStack stack, Player player);
    protected abstract boolean canRefillFromEnvironment();
    protected abstract String tankContentKey();
    public abstract Item getDropItem();

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Don't call super - Entity.defineSynchedData is abstract in mappings
        builder.define(DATA_ACTIVE, false);
        builder.define(DATA_TANK_FILL, 0);
    }

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    public int tankFillPercent() {
        int cap = tankCapacityMb();
        return cap <= 0 ? 0 : (int) Math.round(100.0 * this.tankMb / cap);
    }

    public double shaftPowerW() {
        return enginePowerW() * efficiency();
    }

    public double targetSpeed() {
        return maxSpeedBlocksPerTick();
    }

    public int tankMb() {
        return this.tankMb;
    }

    public void setTankMb(int mb) {
        this.tankMb = Math.max(0, Math.min(tankCapacityMb(), mb));
    }

    protected void addTankMb(int mb) {
        this.tankMb = Math.min(tankCapacityMb(), this.tankMb + mb);
    }

    protected Component tankContentName() {
        return Component.literal(tankContentKey());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            clientTickEffects();
            return;
        }

        double power = tickEngine();

        if (power > 0 && this.tankMb <= 0) {
            handleDry();
            return;
        }

        this.entityData.set(DATA_ACTIVE, power > 0 && this.tankMb > 0);
        this.entityData.set(DATA_TANK_FILL, tankFillPercent());

        // Движение под управлением игрока
        if (power > 0 && this.getControllingPassenger() instanceof Player player) {
            moveUnderControl(player, power);
        } else {
            // Естественное торможение
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
    }

    protected double tickEngine() {
        if (this.tankMb <= 0) {
            return 0.0;
        }
        double demandJ = enginePowerW() / 20.0; // Дж за тик
        double availableJ = tankMb * fuelEnergyPerMb();
        if (availableJ <= 0.0) {
            return 0.0;
        }
        double usedJ = Math.min(demandJ, availableJ);
        this.tankMb = Math.max(0, this.tankMb - (int) Math.ceil(usedJ / fuelEnergyPerMb()));
        return usedJ * 20.0;
    }

    protected void handleDry() {
        if (explodesOnDry()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.BLOCK);
            this.discard();
        }
    }

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
    protected void addAdditionalSaveData(CompoundTag tag) {
        // super.addAdditionalSaveData(tag); // abstract
        tag.putInt("TankMb", this.tankMb);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // super.readAdditionalSaveData(tag); // abstract
        this.tankMb = tag.getInt("TankMb");
    }

    public String debugState() {
        return String.format("бак=%d/%d мБ, P=%.1f кВт, v=%.2f б/т",
                this.tankMb, tankCapacityMb(), shaftPowerW() / 1000.0, targetSpeed());
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }
}