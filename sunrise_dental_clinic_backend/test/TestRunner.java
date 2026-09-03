import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Master test runner for the Sunrise Dental Clinic backend.
 *
 * Discovers and executes every JUnit 5 (Jupiter) @Test in the
 * controller, dao and util packages using the JUnit Platform Launcher API
 * (already on javac.test.classpath / run.test.classpath - no new jars
 * required), and prints one plain-English PASS/FAIL/SKIP line per test
 * (using each test's @DisplayName) plus a final Total/Passed/Failed summary.
 *
 * This class is intentionally not itself a JUnit test (no @Test methods,
 * no "Test" suffix), so `ant test` (which only selects **&#47;*Test.java)
 * never picks it up; it is a supplementary, manually / CI invoked entry
 * point that sits alongside, not instead of, `ant test`.
 *
 * PREREQUISITE: several of the discovered tests (dao.*IntegrationTest and
 * util.AppointmentNumberGeneratorTest) connect to a real local MySQL
 * "sunrise_dental_clinic" database (see src/db/DBConnection.java) - the
 * same prerequisite those tests already assume under `ant test`. This
 * runner does not start/mock that database; if it is unreachable, the
 * affected tests will simply report FAIL with the resulting SQLException
 * as the reason.
 *
 * HOW TO RUN
 *   - NetBeans: right-click this file -> Run File.
 *   - Command line, from the project root (after `ant compile-test`):
 *       java -cp "build\classes;build\test\classes;lib\*" TestRunner
 *     (use ':' as the path separator instead of ';' on non-Windows shells).
 */
public class TestRunner {

    private static final String LINE = "=".repeat(60);

    public static void main(String[] args) {
        System.out.println(LINE);
        System.out.println("SUNRISE DENTAL CLINIC - TEST SUITE");
        System.out.println(LINE);
        System.out.println();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage("controller"),
                        selectPackage("dao"),
                        selectPackage("util"))
                .build();

        Launcher launcher = LauncherFactory.create();

        ConsoleReportingListener consoleListener = new ConsoleReportingListener();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();

        launcher.execute(request, consoleListener, summaryListener);

        TestExecutionSummary summary = summaryListener.getSummary();
        boolean allPassed = printFinalSummary(summary);

        System.exit(allPassed ? 0 : 1);
    }

    /** Prints a Total/Passed/Failed summary block and returns true iff everything passed. */
    private static boolean printFinalSummary(TestExecutionSummary summary) {
        long total = summary.getTestsFoundCount();
        long passed = summary.getTestsSucceededCount();
        long failed = summary.getTestsFailedCount();
        long skipped = summary.getTestsSkippedCount();

        System.out.println();
        System.out.println(LINE);
        System.out.println("SUMMARY");
        System.out.println(LINE);
        System.out.println("Total: " + total + "   Passed: " + passed + "   Failed: " + failed
                + (skipped > 0 ? "   Skipped: " + skipped : ""));

        System.out.println(LINE);

        return failed == 0 && summary.getContainersFailedCount() == 0;
    }

    /** Prints one terse PASS/FAIL/SKIP line per @Test method as it finishes executing. */
    private static class ConsoleReportingListener implements TestExecutionListener {

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
            if (testIdentifier.isTest()) {
                String label = describe(testIdentifier);
                switch (result.getStatus()) {
                    case SUCCESSFUL -> System.out.println("[PASS] " + label);
                    case FAILED -> System.out.println("[FAIL] " + label);
                    case ABORTED -> System.out.println("[SKIP] " + label);
                }
            } else if (result.getStatus() == TestExecutionResult.Status.FAILED
                    && testIdentifier.getParentId().isPresent()) {
                // A container (e.g. a test class whose @BeforeEach/@BeforeAll blew up,
                // such as a lost DB connection) failed before its @Test methods could run.
                System.out.println("[FAIL] " + describe(testIdentifier));
            }
        }

        @Override
        public void executionSkipped(TestIdentifier testIdentifier, String reason) {
            if (testIdentifier.isTest()) {
                System.out.println("[SKIP] " + describe(testIdentifier));
            }
        }
    }

    /**
     * Plain-English label for a test: prefers the JUnit 5 @DisplayName text.
     * If a @Test method has no explicit @DisplayName, Jupiter's own default
     * display name generator already falls back to the method name (e.g.
     * "someMethod()") - that fallback is legible enough on its own, so it is
     * used as-is rather than adding extra logic to reformat it. Every
     * discovered @Test in this project carries a real @DisplayName, so this
     * fallback only exists as a safety net.
     */
    private static String describe(TestIdentifier testIdentifier) {
        return testIdentifier.getDisplayName();
    }
}
