package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Звуковые события мода (сами файлы .ogg генерируются скриптом tools/,
 * см. docs/BUILDING.md — при отсутствии файлов события молчат, но не ломают игру).
 */
public final class TFSounds {

    private TFSounds() {}

    public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_GENERIC = sound("machine_generic");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRUSHER_WORK = sound("crusher_work");
    public static final DeferredHolder<SoundEvent, SoundEvent> LATHE_WORK = sound("lathe_work");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_WORK = sound("hammer_work");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEAM_ENGINE = sound("steam_engine");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE = sound("turbine");
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR = sound("generator");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHEMICAL_BUBBLE = sound("chemical_bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOCOMOTIVE_STEAM = sound("locomotive_steam");
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_CLICK = sound("telegraph_click");
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEPHONE_RING = sound("telephone_ring");
    public static final DeferredHolder<SoundEvent, SoundEvent> RADIO_STATIC = sound("radio_static");
    public static final DeferredHolder<SoundEvent, SoundEvent> CONVEYOR_MOVE = sound("conveyor_move");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOILER_EXPLOSION = sound("boiler_explosion");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARC_FURNACE = sound("arc_furnace");
    public static final DeferredHolder<SoundEvent, SoundEvent> CONVERTER_SPARKS = sound("converter_sparks");
    public static final DeferredHolder<SoundEvent, SoundEvent> WINDMILL_TURN = sound("windmill_turn");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_WHEEL_TURN = sound("water_wheel_turn");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String id) {
        return TerraFabrica.SOUND_EVENTS.register(id,
                () -> SoundEvent.createVariableRangeEvent(TerraFabrica.id(id)));
    }

    public static void init() {
    }
}
