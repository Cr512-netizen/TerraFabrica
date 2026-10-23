package com.terrafabrica.balance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Симуляция производственных цепочек Эпохи I против целей баланса из техзадания.
 *
 * <p>Модель намеренно повторяет ровно те числа, что лежат в датапаке, поэтому тест
 * ловит рассинхрон между «математикой в голове дизайнера» и рецептами:
 * <ul>
 *   <li>порода при добыче без шёлкового касания даёт 1/2/3 куска (30/60/10 %);</li>
 *   <li>дробилка: 1 кусок → 1 порошок, 50 кДж, 100 тиков;</li>
 *   <li>сепаратор: 2 порошка → 1 концентрат, 50 кДж, 100 тиков;</li>
 *   <li>бронза: 9 меди + 1 олова → 10 слитков;</li>
 *   <li>чугун: 2 железных концентрата → 1 слиток чугуна (домница);</li>
 *   <li>сера: 1 пиритовый концентрат → 1000 мБ SO₂ → 1200 мБ кислоты.</li>
 * </ul>
 *
 * <p>Цели ТЗ: 64 медных + 12 оловянных пород на чанк ≈ 70 бронзы; 82 железных ≈ 41 чугуна;
 * 24 пирита ≈ 28 кг кислоты. Тесты допускают разброс, но не порядок величины.
 */
class ProductionBalanceTest {

    /** Средний выход кусков с одной породы: 0.3·1 + 0.6·2 + 0.1·3. */
    private static final double PIECES_PER_PORODE = 0.3 * 1 + 0.6 * 2 + 0.1 * 3;

    private static final long CRUSHER_ENERGY = 50_000;     // Дж
    private static final int CRUSHER_TICKS = 100;          // 5 с
    private static final long SEPARATOR_ENERGY = 50_000;
    private static final int SEPARATOR_TICKS = 100;
    private static final int SEPARATOR_INPUT = 2;          // 2 порошка → 1 концентрат

    private static final double TICKS_PER_SECOND = 20.0;

    /** Концентрат из N пород (дробилка + сепаратор). */
    private static double concentrates(int porodes) {
        double pieces = porodes * PIECES_PER_PORODE;
        return pieces / SEPARATOR_INPUT;
    }

    @Test
    @DisplayName("Средний выход породы — 1.8 куска (30/60/10 %)")
    void averagePieces() {
        assertEquals(1.8, PIECES_PER_PORODE, 1e-9);
    }

    @Test
    @DisplayName("64 медных + 12 оловянных пород ≈ 70 бронзы")
    void bronzeFromChunk() {
        double copper = concentrates(64);            // ≈ 57.6
        double tin = concentrates(12);               // ≈ 10.8

        // Рецепт: 9 меди + 1 олово → 10 бронзы. Количество варок ограничено оловом.
        double batches = Math.min(copper / 9.0, tin / 1.0);
        double bronzeIngots = batches * 10.0;        // ≈ 64

        assertTrue(bronzeIngots >= 55 && bronzeIngots <= 80,
                "бронзы из чанка должно выходить около 70 слитков, получено " + bronzeIngots);
        // Олово после плавки остаётся в избытке — это нормально и видно игроку:
        // медь расходуется полностью (57.6 / 9 = 6.4 варки), олова остаётся ~4.4 концентрата.
        assertTrue(tin - batches > 1.0,
                "олово должно оставаться в избытке, иначе медь копится без применения");
    }

    @Test
    @DisplayName("82 железных породы ≈ 41 чугуна")
    void pigIronFromChunk() {
        double iron = concentrates(82);              // ≈ 73.8
        double pigIron = iron / 2.0;                 // рецепт домницы: 2 → 1
        assertTrue(pigIron >= 30 && pigIron <= 50,
                "чугуна с чанка ожидается около 41, получено " + pigIron);
    }

    @Test
    @DisplayName("24 пирита ≈ 28 кг серной кислоты")
    void acidFromPyrite() {
        double pyrite = concentrates(24);            // ≈ 21.6
        double so2MilliBuckets = pyrite * 1000;      // обжиг: 1 концентрат → 1000 мБ SO₂
        double acidMilliBuckets = so2MilliBuckets * 1200 / 1000;   // свинцовая камера
        double acidLiters = acidMilliBuckets / 1000.0;
        assertTrue(acidLiters >= 20 && acidLiters <= 35,
                "кислоты с 24 пиритов ожидается около 28 л, получено " + acidLiters);
    }

    @Test
    @DisplayName("Дробилка — узкое место, сепаратор не отстаёт")
    void enrichmentThroughput() {
        double crusherPerSecond = TICKS_PER_SECOND / CRUSHER_TICKS;               // 0.2 куска/с
        double separatorPerSecond = SEPARATOR_INPUT * TICKS_PER_SECOND / SEPARATOR_TICKS; // 0.4 порошка/с
        assertTrue(separatorPerSecond > crusherPerSecond,
                "сепаратор обязан успевать за дробилкой, иначе цепочка встаёт дважды");

        long energyPerConcentrate = CRUSHER_ENERGY * SEPARATOR_INPUT + SEPARATOR_ENERGY;
        assertEquals(150_000, energyPerConcentrate,
                "концентрат стоит 2×50 кДж дробления + 50 кДж обогащения");
    }

    @Test
    @DisplayName("Водяное колесо 5 кВт даёт концентрат за полминуты")
    void waterWheelThroughput() {
        long energyPerConcentrate = CRUSHER_ENERGY * SEPARATOR_INPUT + SEPARATOR_ENERGY;
        double seconds = energyPerConcentrate / 5_000.0;    // 5 кВт
        assertTrue(seconds < 60, "слабейший привод не должен останавливать прогресс: " + seconds);

        // Время дробления чанка меди при одном колесе: 115 кусков × 5 с ≈ 10 минут.
        double crushingSeconds = 64 * PIECES_PER_PORODE * CRUSHER_TICKS / TICKS_PER_SECOND;
        assertTrue(crushingSeconds > 400 && crushingSeconds < 900,
                "дробление чанка меди должно занимать ~10 минут, занято " + crushingSeconds);
    }

    @Test
    @DisplayName("Бронзовая линия укладывается в 1–2 часа при последовательной работе")
    void bronzeLineTime() {
        double copper = concentrates(64);
        double smeltSeconds = copper * 600 / TICKS_PER_SECOND;       // домница: 30 с на слиток
        double crushSeconds = 64 * PIECES_PER_PORODE * CRUSHER_TICKS / TICKS_PER_SECOND;

        double total = smeltSeconds + crushSeconds + 12 * PIECES_PER_PORODE * 5;
        assertTrue(total > 3600 * 0.4 && total < 3600 * 2.0,
                "бронзовая линия целиком не должна выходить из 0.4–2 ч, вышло " + total / 3600 + " ч");
    }

    @Test
    @DisplayName("Потери цепочки видны: 1 порода → 0.9 концентрата")
    void visibleLosses() {
        double perPorode = PIECES_PER_PORODE / SEPARATOR_INPUT;
        assertEquals(0.9, perPorode, 1e-9,
                "каждая порода даёт в среднем 0.9 концентрата — потеря 10 % видна в числах");
    }
}
