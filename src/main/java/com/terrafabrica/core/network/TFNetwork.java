package com.terrafabrica.core.network;

import com.terrafabrica.TerraFabrica;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Регистрация сетевых пакетов мода.
 *
 * <p>Используется встроенная система NeoForge ({@code CustomPacketPayload} +
 * {@code PayloadRegistrar}); кастомных каналов не требуется.
 */
@EventBusSubscriber(modid = TerraFabrica.MODID)
public final class TFNetwork {

    private TFNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TerraFabrica.MODID).versioned("1");

        // Сервер → клиент: снимок прогресса исследований.
        registrar.playToClient(
                ResearchSyncPayload.TYPE,
                ResearchSyncPayload.STREAM_CODEC,
                ResearchSyncPayload::handleClient);

        // Клиент → сервер: старт исследования.
        registrar.playToServer(
                StartResearchPayload.TYPE,
                StartResearchPayload.STREAM_CODEC,
                StartResearchPayload::handleServer);
    }
}
