package com.terrafabrica.client.screen;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.core.network.StartResearchPayload;
import com.terrafabrica.core.research.ResearchNode;
import com.terrafabrica.core.research.ResearchProgress;
import com.terrafabrica.core.research.ResearchTree;
import com.terrafabrica.content.TFAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Экран древа исследований (открывается книгой-руководством).
 *
 * <p>Узлы рисуются по координатам {@code ResearchNode.posX/posY} (колонки —
 * эпохи/тиры). Статусы: закрыт (серый), доступен (жёлтая рамка, кнопка
 * «Исследовать»), в процессе (прогресс-бар), открыт (зелёная рамка).
 * Панорамирование — перетаскивание мышью. Описание и стоимость — в тултипе.
 */
public class ResearchScreen extends Screen {

    private static final int NODE_SIZE = 24;
    private static final int GRID = 46;

    /** «Чертёж» древа исследований: своя текстура 48×48, режется 9-слайсом. */
    private static final ResourceLocation PANEL =
            ResourceLocation.fromNamespaceAndPath(TerraFabrica.MODID, "textures/gui/research.png");

    private double panX;
    private double panY;
    private boolean dragging;
    private double dragStartX;
    private double dragStartY;
    private double panStartX;
    private double panStartY;
    private String hoveredNode;

    public ResearchScreen() {
        super(Component.translatable("screen.terrafabrica.research"));
    }

    private ResearchProgress progress() {
        var player = Minecraft.getInstance().player;
        return player == null ? new ResearchProgress() : player.getData(TFAttachments.RESEARCH_PROGRESS);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // ВАЖНО (проверено в живом клиенте): в 1.21.1 Screen.render сам вызывает
        // renderBackground, поэтому сначала super.render (фон уже нарисован), и только
        // потом наша панель. Если нарисовать панель до super.render, фон затрёт её —
        // экран останется серым, без ошибок в логе.
        super.render(graphics, mouseX, mouseY, partialTick);
        // Фон-«чертёж»: узлы рисуются поверх сетки, не сливаясь с ванильным фоном.
        GuiPanel.draw(graphics, PANEL, 4, 2, width - 8, height - 14);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFFFF);

        hoveredNode = null;
        ResearchProgress progress = progress();
        for (ResearchNode node : ResearchTree.all()) {
            int x = (int) (width / 2 + panX + (node.posX() - 6) * GRID);
            int y = (int) (height / 2 + panY + node.posY() * GRID - NODE_SIZE);
            renderNode(graphics, node, x, y, progress, mouseX, mouseY);
        }
        if (hoveredNode != null) {
            renderNodeTooltip(graphics, ResearchTree.get(hoveredNode), mouseX, mouseY);
        }
        graphics.drawString(font, Component.translatable("screen.terrafabrica.research.hint"),
                8, height - 16, 0xFFAAAAAA);
    }

    private void renderNode(GuiGraphics graphics, ResearchNode node, int x, int y,
                            ResearchProgress progress, int mouseX, int mouseY) {
        ResearchProgress.NodeState state = progress.getState(node.id());
        int border = switch (state) {
            case UNLOCKED -> 0xFF40C040;
            case AVAILABLE -> 0xFFE0C030;
            case IN_PROGRESS -> 0xFF3090F0;
            default -> 0xFF707070;
        };
        int fill = 0xC0202020;
        graphics.fill(x - 1, y - 1, x + NODE_SIZE + 1, y + NODE_SIZE + 1, border);
        graphics.fill(x, y, x + NODE_SIZE, y + NODE_SIZE, fill);

        // Прогресс-бар для идущего исследования.
        if (state == ResearchProgress.NodeState.IN_PROGRESS) {
            int widthBar = (int) (NODE_SIZE * progress.getProgressFraction(node.id()));
            graphics.fill(x, y + NODE_SIZE - 3, x + widthBar, y + NODE_SIZE, 0xFF3090F0);
        }

        String label = node.id().substring(0, Math.min(3, node.id().length()));
        graphics.drawString(font, label, x + (NODE_SIZE - font.width(label)) / 2,
                y + (NODE_SIZE - 8) / 2, 0xFFFFFFFF);

        if (mouseX >= x && mouseX < x + NODE_SIZE && mouseY >= y && mouseY < y + NODE_SIZE) {
            hoveredNode = node.id();
        }
    }

    private void renderNodeTooltip(GuiGraphics graphics, ResearchNode node, int mouseX, int mouseY) {
        ResearchProgress progress = progress();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(node.langKey()));
        lines.add(Component.translatable(node.langDescKey()).withStyle(net.minecraft.ChatFormatting.GRAY));
        ResearchProgress.NodeState state = progress.getState(node.id());
        lines.add(Component.translatable("screen.terrafabrica.research.state",
                Component.translatable("research_state.terrafabrica." + state.name().toLowerCase())));
        if (!node.prerequisites().isEmpty()) {
            List<net.minecraft.network.chat.MutableComponent> prereqs = node.prerequisites().stream()
                    .map(id -> Component.translatable(ResearchTree.get(id).langKey()))
                    .toList();
            lines.add(Component.translatable("screen.terrafabrica.research.prerequisites")
                    .withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
            for (Component p : prereqs) {
                lines.add(Component.literal("  • ").append(p).withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
            }
        }
        lines.add(Component.translatable("screen.terrafabrica.research.cost",
                formatTicks(node.timeTicks())));
        for (ResearchNode.ItemCost cost : node.costs()) {
            lines.add(Component.literal("  • ")
                    .append(Component.translatable(cost.item().asItem().getDescriptionId()))
                    .append(Component.literal(" × " + cost.count())));
        }
        graphics.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
    }

    private Component formatTicks(long ticks) {
        long seconds = ticks / 20;
        return seconds >= 60
                ? Component.literal((seconds / 60) + " мин " + (seconds % 60) + " с")
                : Component.literal(seconds + " с");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredNode != null) {
            ResearchProgress progress = progress();
            ResearchNode node = ResearchTree.get(hoveredNode);
            if (progress.getState(hoveredNode) == ResearchProgress.NodeState.AVAILABLE) {
                PacketDistributor.sendToServer(new StartResearchPayload(hoveredNode));
                return true;
            }
        }
        if (button == 0) {
            dragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            panStartX = panX;
            panStartY = panY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            panX = panStartX + (mouseX - dragStartX);
            panY = panStartY + (mouseY - dragStartY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
