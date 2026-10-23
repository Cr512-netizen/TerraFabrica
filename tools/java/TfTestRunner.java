import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

/**
 * Запуск тестов Terra Fabrica без Gradle.
 *
 * <p>В этом окружении сеть закрыта, а кэш Gradle хранит артефакты JUnit без метаданных,
 * поэтому {@code gradle test} не может разрешить зависимости. Скрипт
 * {@code tools/run_tests_offline.ps1} собирает classpath из уже скачанных jar-ов и вызывает
 * этот раннер напрямую через JUnit Platform Launcher.
 */
public final class TfTestRunner {

    private TfTestRunner() {}

    public static void main(String[] args) {
        String basePackage = args.length > 0 ? args[0] : "com.terrafabrica";
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage(basePackage))
                .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);

        PrintWriter out = new PrintWriter(System.out, true);
        listener.getSummary().printTo(out);
        listener.getSummary().printFailuresTo(out);
        out.println("tests: " + listener.getSummary().getTestsSucceededCount() + " ok, "
                + listener.getSummary().getTestsFailedCount() + " failed");
        System.exit(listener.getSummary().getTotalFailureCount() == 0 ? 0 : 1);
    }
}
