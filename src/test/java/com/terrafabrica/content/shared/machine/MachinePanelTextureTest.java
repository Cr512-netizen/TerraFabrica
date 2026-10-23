package com.terrafabrica.content.shared.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Проверяет, что текстура панели машины совпадает по размеру с {@link MachineLayout}.
 *
 * <p>Зачем отдельный тест: раскладка задана числами в {@code MachineLayout}, а рамки слотов
 * нарисованы в PNG. Если текстура окажется другого размера, слоты разъедутся с картинкой —
 * именно так выглядит «поехавший GUI». Размер текстуры вдобавок обязан совпадать с тем,
 * что передаётся в {@code GuiGraphics.blit}: короткая перегрузка {@code blit} считает
 * текстуру 256x256 и растягивает панель, поэтому размер вынесен в константы и сверяется здесь.
 *
 * <p>Тест читает PNG напрямую через {@code ImageIO}, без Minecraft, поэтому работает в
 * офлайн-прогоне JUnit.
 */
class MachinePanelTextureTest {

    private static final Path TEXTURE =
            Path.of("src/main/resources/assets/terrafabrica/textures/gui/machine.png");

    @Test
    @DisplayName("Текстура панели машины совпадает по размеру с MachineLayout")
    void textureMatchesLayoutSize() throws IOException {
        assertTrue(Files.isRegularFile(TEXTURE), "нет текстуры панели: " + TEXTURE.toAbsolutePath());
        BufferedImage image;
        try (InputStream in = Files.newInputStream(TEXTURE)) {
            image = ImageIO.read(in);
        }
        assertEquals(MachineLayout.GUI_WIDTH, image.getWidth(),
                "ширина текстуры панели расходится с MachineLayout.GUI_WIDTH");
        assertEquals(MachineLayout.GUI_HEIGHT, image.getHeight(),
                "высота текстуры панели расходится с MachineLayout.GUI_HEIGHT");
    }

    @Test
    @DisplayName("Рамки слотов в текстуре панели стоят там же, где слоты меню")
    void slotFramesAreDrawnAtSlotPositions() throws IOException {
        BufferedImage image;
        try (InputStream in = Files.newInputStream(TEXTURE)) {
            image = ImageIO.read(in);
        }
        // Рамка слота — тёмный пиксель (55,55,55) в левом верхнем углу и светлая
        // внутренность (139,139,139) через один пиксель: так рисует генератор текстур.
        for (int i = 0; i < MachineLayout.SLOTS; i++) {
            int x = MachineLayout.slotX(i);
            int y = MachineLayout.slotY(i);
            int frame = image.getRGB(x, y) & 0xFFFFFF;
            int inner = image.getRGB(x + 1, y + 1) & 0xFFFFFF;
            assertEquals(0x373737, frame,
                    "слот " + i + " в (" + x + "," + y + "): нет тёмной рамки в текстуре");
            assertEquals(0x8B8B8B, inner,
                    "слот " + i + " в (" + x + "," + y + "): нет светлой внутренности в текстуре");
        }
    }
}
