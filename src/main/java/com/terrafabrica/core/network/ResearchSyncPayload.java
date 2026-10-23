package com.terrafabrica.core.network;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.TFAttachments;
import com.terrafabrica.core.research.ResearchProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Пакет «сервер → клиент»: полный снимок прогресса исследований игрока.
 *
 * <p>Отправляется при входе, старте и завершении исследования. На клиенте
 * данные пишутся в attachment игрока — клиент использует их только для
 * отображения (GUI, тултипы, предпросмотр рецептов); авторитетной является
 * серверная копия.
 */
public record ResearchSyncPayload(CompoundTag progress) implements CustomPacketPayload {

    public static final Type<ResearchSyncPayload> TYPE =
            new Type<>(TerraFabrica.id("research_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, ResearchSyncPayload::progress,
                    ResearchSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Обработчик на клиенте (выполняется в главном потоке). */
    public static void handleClient(ResearchSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ResearchProgress client = mc.player.getData(TFAttachments.RESEARCH_PROGRESS);
                ResearchProgress incoming = new ResearchProgress();
                incoming.deserializeNBT(mc.player.level().registryAccess(), payload.progress());
                client.copyFrom(incoming);
            }
        });
    }
}
