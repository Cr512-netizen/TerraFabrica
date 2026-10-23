package com.terrafabrica.content.shared.transport;

import com.terrafabrica.content.TFSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Базовый рельсовый локомотив: топка, бак, тяга и перегрев.
 *
 * <p>Физика в СИ, все величины выводятся из джоулей:
 * <ul>
 *   <li>твёрдое топливо хранится как химическая энергия {@code fuelJ}; предмет с
 *       ванильным временем горения переводится в джоули по калорийности угля
 *       ({@link #COAL_ENERGY_J} на {@link #COAL_BURN_TICKS} тиков);</li>
 *   <li>топка отдаёт {@link #firePowerW()} тепловой мощности, пока есть топливо;</li>
 *   <li>полезная мощность на колёсах = тепловая × КПД установки
 *       ({@link #thermalEfficiency()});</li>
 *   <li>скорость на рельсах пропорциональна мощности: тяга задаётся целевой
 *       скоростью и накапливается как импульс вагонетки ({@code moveAlongTrack}),
 *       так же, как у ванильной печной вагонетки.</li>
 * </ul>
 *
 * <p>Бак: у паровоза это вода котла (испаряется, латентная теплота
 * {@link #WATER_LATENT_HEAT_J_PER_MB}), у тепловоза — дизельное топливо.
 *
 * <p>Перегрев: сухая топка. Паровоз при этом взрывается
 * ({@link #explodesOnDryFire()}), тепловоз просто глохнет.
 */
public abstract class AbstractLocomotiveEntity extends AbstractMinecart {

    /** Теплота парообразования воды: 2.26 МДж/кг, 1 мБ воды = 1 г. */
    protected static final double WATER_LATENT_HEAT_J_PER_MB = 2_260.0;

    /** Калорийность угля: 24 МДж на предмет (ванильные 1600 тиков горения). */
    protected static final double COAL_ENERGY_J = 24_000_000.0;
    protected static final int COAL_BURN_TICKS = 1600;

    /** Сухая топка: предупреждение и авария. */
    protected static final int DRY_WARNING_TICKS = 100;
    protected static final int DRY_EXPLODE_TICKS = 300;

    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(AbstractLocomotiveEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TANK_FILL =
            SynchedEntityData.defineId(AbstractLocomotiveEntity.class, EntityDataSerializers.INT);

    private static final String TAG_FUEL = "FuelJ";
    private static final String TAG_TANK = "TankMb";
    private static final String TAG_DRY = "DryTicks";

    /** Остаток химической энергии твёрдого топлива, Дж. */
    private double fuelJ;
    /** Содержимое бака, мБ (вода котла либо дизель). */
    private int tankMb;
    /** Дробный остаток бака: расход за тик меньше 1 мБ. */
    private double tankResidueMb;
    /** Тики работы без содержимого бака. */
    private int dryTicks;
    /** Тепловая мощность прошлого тика, Вт. */
    protected double lastThermalPowerW;

    protected AbstractLocomotiveEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected AbstractLocomotiveEntity(EntityType<?> type, Level level, double x, double y, double z) {
        super(type, level, x, y, z);
    }

    // ------------------------------------------------------------------
    // Параметры машины (задают наследники)
    // ------------------------------------------------------------------

    /** Тепловая мощность топки, Вт. */
    protected abstract double firePowerW();

    /** КПД установки: доля теплоты, доходящая до колёс. */
    protected abstract double thermalEfficiency();

    /** Ёмкость бака, мБ. */
    protected abstract int tankCapacityMb();

    /** Максимальная скорость на рельсах, блоков за тик. */
    protected abstract double maxSpeedBlocksPerTick();

    /** Взрывается ли при сухой топке. */
    protected abstract boolean explodesOnDryFire();

    /** Подходит ли предмет как твёрдое топливо. */
    protected abstract boolean isFuelItem(ItemStack stack);

    /** Принять предмет-топливо; вернуть true, если предмет израсходован. */
    protected abstract boolean acceptFuelItem(ItemStack stack, Player player);

    /** Можно ли добирать содержимое бака из окружения (вода/дождь). */
    protected abstract boolean canRefillFromEnvironment();

    /** Ключ локализации содержимого бака. */
    protected abstract String tankContentKey();

    /** Показывать пар вместо дыма. */
    protected abstract boolean emitsSteam();

    /** Требуется ли охлаждающая жидкость (котловая вода). */
    protected boolean consumesCoolant() {
        return true;
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.RIDEABLE;
    }

    // ------------------------------------------------------------------
    // Синхронизация
    // ------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVE, false);
        builder.define(DATA_TANK_FILL, 0);
    }

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    /** Заполнение бака в процентах (0..100). */
    public int tankFillPercent() {
        int cap = tankCapacityMb();
        return cap <= 0 ? 0 : (int) Math.round(100.0 * this.tankMb / cap);
    }

    public int tankMb() {
        return this.tankMb;
    }

    /** Полезная мощность на колёсах, Вт. */
    public double shaftPowerW() {
        return this.lastThermalPowerW * thermalEfficiency();
    }

    // ------------------------------------------------------------------
    // Тик
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            clientTickEffects();
            return;
        }

        double thermal = tickBurner();

        if (thermal > 0 && consumesCoolant() && this.tankMb <= 0) {
            this.dryTicks++;
            if (this.dryTicks == DRY_WARNING_TICKS) {
                warnDryBoiler();
            }
            if (this.dryTicks >= DRY_EXPLODE_TICKS) {
                handleDryFireAccident();
                return;
            }
        } else if (thermal <= 0) {
            this.dryTicks = Math.max(0, this.dryTicks - 2);
        } else {
            this.dryTicks = 0;
        }

        if (canRefillFromEnvironment() && this.tankMb < tankCapacityMb()) {
            refillFromEnvironment();
        }

        this.entityData.set(DATA_ACTIVE, thermal > 0 && this.tankMb > 0);
        this.entityData.set(DATA_TANK_FILL, tankFillPercent());
    }

    /**
     * Сжигает топливо за тик.
     *
     * @return тепловая мощность тика, Вт
     */
    protected double tickBurner() {
        double power = 0.0;
        if (this.fuelJ > 0) {
            double perTick = firePowerW() / 20.0; // Вт → Дж за тик
            double burned = Math.min(perTick, this.fuelJ);
            this.fuelJ -= burned;
            power = burned * 20.0;
        }
        this.lastThermalPowerW = power;
        if (power <= 0.0 || !consumesCoolant() || this.tankMb <= 0) {
            return power;
        }
        // Испарение: теплота тика делится на латентную теплоту воды.
        drainTankMb((power / 20.0) / WATER_LATENT_HEAT_J_PER_MB);
        return power;
    }

    protected abstract void refillFromEnvironment();

    protected void warnDryBoiler() {
        this.level().playSound(null, this.blockPosition(), TFSounds.LOCOMOTIVE_STEAM.get(),
                SoundSource.NEUTRAL, 1.0F, 0.6F);
        for (Player nearby : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8.0))) {
            nearby.displayClientMessage(
                    Component.translatable("message.terrafabrica.locomotive.dry", tankContentName()), true);
        }
    }

    private void handleDryFireAccident() {
        if (explodesOnDryFire()) {
            this.level().playSound(null, this.blockPosition(), TFSounds.BOILER_EXPLOSION.get(),
                    SoundSource.NEUTRAL, 2.0F, 1.0F);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 3.5F,
                    Level.ExplosionInteraction.BLOCK);
            this.discard();
        } else {
            this.fuelJ = 0.0;
            this.dryTicks = DRY_WARNING_TICKS;
            this.level().playSound(null, this.blockPosition(), TFSounds.MACHINE_GENERIC.get(),
                    SoundSource.NEUTRAL, 1.0F, 0.5F);
        }
    }

    private void clientTickEffects() {
        if (!isActive()) {
            return;
        }
        if (this.random.nextInt(4) == 0) {
            this.level().addParticle(emitsSteam() ? ParticleTypes.CAMPFIRE_COSY_SMOKE : ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getY() + 1.2,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.02, 0.0);
        }
        if (this.random.nextInt(60) == 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    TFSounds.LOCOMOTIVE_STEAM.get(), SoundSource.NEUTRAL,
                    0.7F, 0.9F + this.random.nextFloat() * 0.2F, false);
        }
    }

    // ------------------------------------------------------------------
    // Движение по рельсам
    // ------------------------------------------------------------------

    @Override
    public float getMaxCartSpeedOnRail() {
        return (float) maxSpeedBlocksPerTick();
    }

    @Override
    protected double getMaxSpeed() {
        return maxSpeedBlocksPerTick();
    }

    @Override
    protected void moveAlongTrack(BlockPos pos, BlockState state) {
        super.moveAlongTrack(pos, state);
        if (!isActive()) {
            return;
        }
        double target = targetSpeed();
        if (target <= 0.0) {
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        double horizontal = motion.horizontalDistanceSqr();
        if (horizontal < 1.0E-6) {
            // Стоящий локомотив сам не поедет: ванильная вагонетка разгоняется
            // только толчком. Даём начальный импульс вдоль рельса в ту сторону,
            // куда смотрит локомотив.
            Vec3 axis = trackAxis(state);
            if (axis == null) {
                return;
            }
            Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
            double dot = axis.x * forward.x + axis.z * forward.z;
            double sign = dot < 0.0 ? -1.0 : 1.0;
            double start = Math.min(target, 0.06);
            this.setDeltaMovement(axis.x * sign * start, motion.y, axis.z * sign * start);
            return;
        }
        double speed = Math.sqrt(horizontal);
        if (speed < target) {
            double scale = (target - speed) / speed;
            this.setDeltaMovement(motion.x + motion.x * scale, motion.y, motion.z + motion.z * scale);
        }
    }

    /**
     * Горизонтальное направление рельса (единичный вектор) или {@code null},
     * если блок не рельс. Уклон учитывается как горизонтальное направление
     * подъёма: вертикаль добавляет ванильная физика вагонетки.
     */
    private static Vec3 trackAxis(BlockState state) {
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.RailBlock)) {
            return null;
        }
        net.minecraft.world.level.block.state.properties.RailShape shape =
                state.getValue(net.minecraft.world.level.block.RailBlock.SHAPE);
        Vec3 axis = switch (shape) {
            case NORTH_SOUTH, ASCENDING_SOUTH -> new Vec3(0.0, 0.0, 1.0);
            case ASCENDING_NORTH -> new Vec3(0.0, 0.0, -1.0);
            case EAST_WEST, ASCENDING_EAST -> new Vec3(1.0, 0.0, 0.0);
            case ASCENDING_WEST -> new Vec3(-1.0, 0.0, 0.0);
            case NORTH_EAST -> new Vec3(1.0, 0.0, -1.0);
            case SOUTH_EAST -> new Vec3(1.0, 0.0, 1.0);
            case SOUTH_WEST -> new Vec3(-1.0, 0.0, 1.0);
            case NORTH_WEST -> new Vec3(-1.0, 0.0, -1.0);
        };
        return axis.normalize();
    }

    /** Целевая скорость из полезной мощности, блоков за тик. */
    public double targetSpeed() {
        double power = shaftPowerW();
        if (power <= 0.0) {
            return 0.0;
        }
        // Тяга на ободе F = P / v; скорость выходит на насыщение при постоянном
        // сопротивлении. 60 кВт полезной мощности дают полную скорость, поэтому
        // паровоз (≈36 кВт на полной топке) идёт около 18 км/ч.
        double fraction = Math.min(1.0, power / 60_000.0);
        return maxSpeedBlocksPerTick() * fraction;
    }

    // ------------------------------------------------------------------
    // Взаимодействие
    // ------------------------------------------------------------------

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

        if (fillTank(player, hand, stack)) {
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        InteractionResult base = super.interact(player, hand);
        if (!base.consumesAction() && !this.level().isClientSide()) {
            player.displayClientMessage(Component.translatable("message.terrafabrica.locomotive.status",
                    tankMb(), tankCapacityMb(),
                    Math.round(shaftPowerW() / 1000.0),
                    Math.round(targetSpeed() * 20.0 * 3.6)), false);
        }
        return base;
    }

    /** Наполнение бака предметом (вода). Наследники переопределяют при необходимости. */
    protected boolean fillTank(Player player, InteractionHand hand, ItemStack stack) {
        return false;
    }

    protected Component tankContentName() {
        return Component.translatable(tankContentKey());
    }

    protected void addFuelJoules(double joules) {
        this.fuelJ = Math.min(this.fuelJ + joules, COAL_ENERGY_J * 32.0);
    }

    protected boolean addTankMb(int mb) {
        if (this.tankMb >= tankCapacityMb()) {
            return false;
        }
        this.tankMb = Math.min(tankCapacityMb(), this.tankMb + mb);
        this.dryTicks = 0;
        return true;
    }

    /**
     * Точный расход бака: за тик сгорает меньше миллилитра, поэтому дробный
     * остаток копится отдельно и не теряется при округлении.
     */
    protected void drainTankMb(double mb) {
        double total = this.tankMb + this.tankResidueMb - Math.max(0.0, mb);
        if (total <= 0.0) {
            this.tankMb = 0;
            this.tankResidueMb = 0.0;
            return;
        }
        this.tankMb = (int) Math.floor(total);
        this.tankResidueMb = total - this.tankMb;
    }

    protected double fuelJ() {
        return this.fuelJ;
    }

    protected int dryTicks() {
        return this.dryTicks;
    }

    // ------------------------------------------------------------------
    // Сохранение
    // ------------------------------------------------------------------

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble(TAG_FUEL, this.fuelJ);
        tag.putInt(TAG_TANK, this.tankMb);
        tag.putDouble("TankResidue", this.tankResidueMb);
        tag.putInt(TAG_DRY, this.dryTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fuelJ = tag.getDouble(TAG_FUEL);
        this.tankMb = tag.getInt(TAG_TANK);
        this.tankResidueMb = tag.getDouble("TankResidue");
        this.dryTicks = tag.getInt(TAG_DRY);
    }

    /** Энергия предмета-топлива в джоулях по ванильному времени горения. */
    protected static double energyOf(ItemStack stack) {
        int burnTicks = stack.getBurnTime(RecipeType.SMELTING);
        if (burnTicks <= 0) {
            return 0.0;
        }
        return COAL_ENERGY_J * burnTicks / COAL_BURN_TICKS;
    }

    /** Состояние для GameTest и логов. */
    public String debugState() {
        return String.format("fuel=%.2f МДж, бак=%d/%d мБ, сухих тиков=%d, P=%.1f кВт, v=%.2f б/т",
                this.fuelJ / 1e6, this.tankMb, tankCapacityMb(), this.dryTicks,
                shaftPowerW() / 1000.0, targetSpeed());
    }

    /** По умолчанию груз не показываем: корпус задаёт модель вагонетки. */
    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.AIR.defaultBlockState();
    }
}
