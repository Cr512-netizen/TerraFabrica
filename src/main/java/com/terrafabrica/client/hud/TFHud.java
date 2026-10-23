package com.terrafabrica.client.hud;

import com.terrafabrica.config.TFClientConfig;
import com.terrafabrica.content.TFItems;
import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.core.util.Units;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD мода: параметры блока под прицелом при наличии прибора в руке
 * (мультиметр — энергия/напряжение, термометр — температура, тахометр —
 * вращение, счётчик Гейгера — радиация). Отключается в клиентском конфиге.
 */
public final class TFHud {

    private TFHud() {}

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!TFClientConfig.HUD_ENABLED.get()) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean hasMultimeter = mainHand.is(TFItems.MULTIMETER.get()) || offHand.is(TFItems.MULTIMETER.get());
        boolean hasThermometer = mainHand.is(TFItems.THERMOMETER.get()) || offHand.is(TFItems.THERMOMETER.get());
        boolean hasTachometer = mainHand.is(TFItems.TACHOMETER.get()) || offHand.is(TFItems.TACHOMETER.get());
        boolean hasGeiger = mainHand.is(TFItems.GEIGER_COUNTER.get()) || offHand.is(TFItems.GEIGER_COUNTER.get());
        if (!hasMultimeter && !hasThermometer && !hasTachometer && !hasGeiger) return;

        List<String> lines = new ArrayList<>();
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.hitResult;
            if (mc.level.getBlockEntity(hit.getBlockPos()) instanceof AbstractMachineBlockEntity machine) {
                if (hasMultimeter && TFClientConfig.HUD_SHOW_ENERGY.get()) {
                    lines.add(String.format("E: %d кДж / %d кДж",
                            machine.getStoredEnergy() / 1000, machine.getCapacity() / 1000));
                }
                if (hasThermometer && TFClientConfig.HUD_SHOW_TEMPERATURE.get()) {
                    lines.add(String.format("T: %.0f °C (%.0f K)",
                            Units.kelvinToCelsius(machine.getTemperature()), machine.getTemperature()));
                }
                if (hasTachometer && TFClientConfig.HUD_SHOW_ROTATION.get()) {
                    lines.add(String.format("R: %.0f RPM, %.0f Н·м, %.1f кВт",
                            machine.getRPM(), machine.getTorque(), machine.rotationStorage().getPower() / 1000.0));
                }
            }
        }
        if (hasGeiger && TFClientConfig.HUD_SHOW_RADIATION.get()) {
            double radiation = measureRadiation(mc, player);
            lines.add(String.format("RAD: %.2f мкЗв/ч", radiation));
        }

        int y = 6;
        for (String line : lines) {
            graphics.drawString(mc.font, line, 6, y, 0xFFFFFFFF);
            graphics.fill(4, y - 1, 6, y + mc.font.lineHeight, 0xFF909090);
            y += 11;
        }
    }

    /** Грубая оценка радиационного фона от урановых предметов/блоков рядом. */
    private static double measureRadiation(Minecraft mc, Player player) {
        double total = 0;
        var pos = player.blockPosition();
        for (var blockPos : net.minecraft.core.BlockPos.betweenClosed(pos.offset(-8, -4, -8), pos.offset(8, 4, 8))) {
            var state = mc.level.getBlockState(blockPos);
            var block = state.getBlock();
            if (block == com.terrafabrica.content.TFBlocks.PORODES.get("uraninite_porode_poor").get()
                    || block == com.terrafabrica.content.TFBlocks.PORODES.get("uraninite_porode_normal").get()
                    || block == com.terrafabrica.content.TFBlocks.PORODES.get("uraninite_porode_rich").get()
                    || block == com.terrafabrica.content.TFBlocks.PORODES.get("deepslate_uraninite_porode_poor").get()
                    || block == com.terrafabrica.content.TFBlocks.PORODES.get("deepslate_uraninite_porode_normal").get()
                    || block == com.terrafabrica.content.TFBlocks.PORODES.get("deepslate_uraninite_porode_rich").get()) {
                double d = Math.sqrt(blockPos.distSqr(pos));
                total += 200.0 / Math.max(1.0, d);
            }
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(TFItems.YELLOWCAKE.get())) {
                total += stack.getCount() * 50.0;
            }
        }
        return Math.min(100_000, total);
    }
}
