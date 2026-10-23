package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.era1.SteamLocomotiveEntity;
import com.terrafabrica.content.era2.DieselLocomotiveEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Реестр сущностей мода: пока только рельсовый транспорт. */
public final class TFEntities {

    private TFEntities() {
    }

    public static final DeferredHolder<EntityType<?>, EntityType<SteamLocomotiveEntity>> STEAM_LOCOMOTIVE =
            TerraFabrica.ENTITY_TYPES.register("steam_locomotive",
                    () -> EntityType.Builder.<SteamLocomotiveEntity>of(SteamLocomotiveEntity::new, MobCategory.MISC)
                            .sized(1.4F, 1.6F)
                            .eyeHeight(1.1F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build(TerraFabrica.idString("steam_locomotive")));

    public static final DeferredHolder<EntityType<?>, EntityType<DieselLocomotiveEntity>> DIESEL_LOCOMOTIVE =
            TerraFabrica.ENTITY_TYPES.register("diesel_locomotive",
                    () -> EntityType.Builder.<DieselLocomotiveEntity>of(DieselLocomotiveEntity::new, MobCategory.MISC)
                            .sized(1.4F, 1.7F)
                            .eyeHeight(1.2F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build(TerraFabrica.idString("diesel_locomotive")));

    /** Вызывается из {@link TerraFabrica} после создания реестров. */
    public static void init() {
        // Только загрузка класса.
    }
}
