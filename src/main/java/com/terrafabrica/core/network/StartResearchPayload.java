package com.terrafabrica.core.network;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.core.research.ResearchManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Пакет «клиент → сервер»: запрос на старт исследования.
 *
 * <p>Сервер проверяет доступность узла (предшественники, ресурсы) и валидность
 * ID; ни один клиент не может «прожать» исследование мимо серверной проверки.
 */
public record StartResearchPayload(String nodeId) implements CustomPacketPayload {

    public static final Type<StartResearchPayload> TYPE =
            new Type<>(TerraFabrica.id("start_research"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StartResearchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, StartResearchPayload::nodeId,
                    StartResearchPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Обработчик на сервере (выполняется в главном потоке). */
    public static void handleServer(StartResearchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ResearchManager.startResearch(serverPlayer, payload.nodeId());
            }
        });
    }
}
