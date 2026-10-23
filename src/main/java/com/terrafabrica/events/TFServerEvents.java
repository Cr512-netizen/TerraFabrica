package com.terrafabrica.events;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.core.research.ResearchManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Серверные события мода: тик исследований, синхронизация при входе.
 */
@EventBusSubscriber(modid = TerraFabrica.MODID)
public final class TFServerEvents {

    private TFServerEvents() {}

    /** Продвижение исследований у всех онлайн-игроков (1 тик = 1 тик исследования). */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ResearchManager.tickServer(player);
        }
    }

    /** При входе: пересчёт доступности и полная синхронизация прогресса. */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResearchManager.recomputeAvailability(player);
            ResearchManager.sync(player);
        }
    }
}
