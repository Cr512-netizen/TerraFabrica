package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.core.api.TerraFabricaCapabilities;
import com.terrafabrica.core.api.energy.ForgeEnergyAdapter;
import com.terrafabrica.core.api.energy.IEnergyHandler;
import com.terrafabrica.core.api.heat.IHeatHandler;
import com.terrafabrica.core.api.rotation.IRotationHandler;
import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Регистрация capabilities для всех машин мода.
 *
 * <p>Все BE-типы Terra Fabrica наследуют {@link AbstractMachineBlockEntity},
 * поэтому регистрация универсальна по списку BE-типов:
 * <ul>
 *   <li>энергия (Дж) — {@link TerraFabricaCapabilities#ENERGY}, по сторонам;</li>
 *   <li>тепло (K) — {@link TerraFabricaCapabilities#HEAT};</li>
 *   <li>вращение (RPM/Н·м) — {@link TerraFabricaCapabilities#ROTATION}, по сторонам;</li>
 *   <li>флюиды — NeoForge {@link Capabilities.FluidHandler#BLOCK};</li>
 *   <li>предметы — NeoForge {@link Capabilities.ItemHandler#BLOCK};</li>
 *   <li>FE-мост — {@link Capabilities.EnergyStorage#BLOCK} (1 FE = 2.5 Дж).</li>
 * </ul>
 */
@EventBusSubscriber(modid = TerraFabrica.MODID)
public final class CapabilityRegistration {

    private CapabilityRegistration() {}

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var entry : TerraFabrica.BLOCK_ENTITIES.getEntries()) {
            var type = entry.get();
            event.registerBlockEntity(TerraFabricaCapabilities.ENERGY, type,
                    (be, side) -> be instanceof AbstractMachineBlockEntity machine
                            && machine.canConnectEnergy(side) ? machine : null);
            event.registerBlockEntity(TerraFabricaCapabilities.HEAT, type,
                    (be, ctx) -> be instanceof IHeatHandler heat ? heat : null);
            event.registerBlockEntity(TerraFabricaCapabilities.ROTATION, type,
                    (be, side) -> be instanceof IRotationHandler rotation
                            && rotation.canConnect(side) ? rotation : null);
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type,
                    (be, ctx) -> be instanceof IFluidHandler fluid ? fluid : null);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                    (be, ctx) -> be instanceof AbstractMachineBlockEntity machine
                            ? machine.inventory() : null);
            // FE-мост: 1 FE = 2.5 Дж (совместимость с IEnergyStorage других модов).
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type,
                    (be, side) -> be instanceof IEnergyHandler energy
                            ? new ForgeEnergyAdapter(energy) : null);
        }
    }
}
