package com.terrafabrica.core.api.rotation;

import com.terrafabrica.core.util.Units;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Реализация {@link IRotationHandler}: хранит обороты и момент с пределами.
 *
 * <p>Ограничения: {@code maxRpm} и {@code maxTorque}; при установке
 * {@link #setPower(double, double)} момент выводится из мощности, а не наоборот —
 * так сохраняется энергетический баланс (нельзя «создать» момент сверх мощности).
 */
public class RotationStorage implements IRotationHandler {

    private final double maxRpm;
    private final double maxTorque;
    private double rpm;
    private double torque;

    /** Простейшее хранилище без вращения (например, потребитель без привода). */
    public RotationStorage() {
        this(0, 0);
    }

    /**
     * @param maxRpm    максимальные обороты, RPM
     * @param maxTorque максимальный момент, Н·м
     */
    public RotationStorage(double maxRpm, double maxTorque) {
        this.maxRpm = maxRpm;
        this.maxTorque = maxTorque;
    }

    @Override
    public double getRPM() {
        return rpm;
    }

    @Override
    public double getTorque() {
        return torque;
    }

    @Override
    public boolean canConnect(@Nullable Direction side) {
        return true;
    }

    /**
     * Устанавливает обороты с ограничением. Момент при этом пересчитывается из
     * текущей мощности, чтобы сохранить энергию.
     */
    public void setRPM(double rpm) {
        double power = getPower();
        this.rpm = Units.clamp(rpm, 0, maxRpm);
        this.torque = this.rpm > 0 ? power * 60.0 / (2.0 * Math.PI * this.rpm) : 0;
    }

    /** Устанавливает момент с ограничением (мощность при этом не растёт). */
    public void setTorque(double torque) {
        this.torque = Units.clamp(torque, 0, maxTorque);
    }

    /**
     * Устанавливает целевую мощность и обороты; момент выводится как
     * {@code P·60/(2π·RPM)} с ограничением по {@code maxTorque}.
     *
     * @param power целевая мощность, Вт
     * @param rpm   целевые обороты, RPM
     */
    public void setPower(double power, double rpm) {
        this.rpm = Units.clamp(rpm, 0, maxRpm);
        double wantedTorque = this.rpm > 0 ? power * 60.0 / (2.0 * Math.PI * this.rpm) : 0;
        this.torque = Units.clamp(wantedTorque, 0, maxTorque);
    }

    @Override
    public boolean receiveRotation(double rpmIn, double torqueIn) {
        this.rpm = Units.clamp(rpmIn, 0, maxRpm);
        this.torque = Units.clamp(torqueIn, 0, maxTorque);
        return true;
    }

    /** Полный сброс (нет привода). */
    public void reset() {
        this.rpm = 0;
        this.torque = 0;
    }
}
