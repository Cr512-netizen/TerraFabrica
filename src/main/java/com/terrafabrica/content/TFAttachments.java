package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.core.research.ResearchProgress;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * Attachment'ы мода.
 *
 * <p>{@code RESEARCH_PROGRESS} — персональный прогресс исследований игрока
 * (сериализуется в NBT, копируется при смерти, синхронизируется сервером).
 */
public final class TFAttachments {

    public static final AttachmentType<ResearchProgress> RESEARCH_PROGRESS =
            AttachmentType.<ResearchProgress>builder(ResearchProgress::new)
                    .serialize(new ResearchProgressAttachmentSerializer())
                    .copyOnDeath()
                    .build();

    private TFAttachments() {}

    /** Регистрация attachment'ов в реестре {@code ATTACHMENT_TYPES}. */
    public static void init() {
        TerraFabrica.ATTACHMENTS.register("research_progress", () -> RESEARCH_PROGRESS);
    }

    /**
     * Сериализатор прогресса исследований. Реализован вручную (а не через
     * {@code AttachmentType.serializable}), чтобы явно документировать формат NBT.
     */
    private static final class ResearchProgressAttachmentSerializer
            implements net.neoforged.neoforge.attachment.IAttachmentSerializer<net.minecraft.nbt.CompoundTag, ResearchProgress> {

        @Override
        public ResearchProgress read(net.neoforged.neoforge.attachment.IAttachmentHolder holder,
                                     net.minecraft.nbt.CompoundTag tag,
                                     net.minecraft.core.HolderLookup.Provider provider) {
            ResearchProgress progress = new ResearchProgress();
            progress.deserializeNBT(provider, tag);
            return progress;
        }

        @Override
        public net.minecraft.nbt.CompoundTag write(ResearchProgress attachment,
                                                   net.minecraft.core.HolderLookup.Provider provider) {
            return attachment.serializeNBT(provider);
        }
    }
}
