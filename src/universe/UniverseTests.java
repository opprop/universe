package universe;

import java.io.File;

import javax.annotation.processing.AbstractProcessor;

import org.checkerframework.framework.test.CheckerFrameworkPerFileTest;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized.Parameters;

/**
 * JUnit tests for the universe checker, which tests the universe annotations.
 *
 * @author wmdietl
 */
public class UniverseTests {
    /* The class name of the Checker to use.
     * Careful, class CheckerTest has also a field by this name.
     * Set this field in a subclass to a different checker, e.g. see UniverseInferenceTests.
     */
    protected static Class<? extends AbstractProcessor> checkerClass = universe.UniverseChecker.class;

    public static void main(String[] args) {
        // org.junit.runner.JUnitCore.main("universe.UniverseTests");
        org.junit.runner.JUnitCore jc = new org.junit.runner.JUnitCore();
        Result run = jc.run(UniverseTestsTopology.class,
                UniverseTestsStrictPurity.class,
                UniverseTestsLostYes.class,
                UniverseTestsLostNo.class);

        if( run.wasSuccessful() ) {
            System.out.println("Run was successful with " + run.getRunCount() + " test(s)!");
        } else {
            System.out.println("Run had " + run.getFailureCount() + " failure(s) out of " +
                    run.getRunCount() + " run(s)!");

            for( Failure f : run.getFailures() ) {
                System.out.println(f.toString());
            }
        }
    }


    public static class UniverseTestsTopology extends CheckerFrameworkPerFileTest {
        public UniverseTestsTopology (File testFile) {
            super(testFile, UniverseTests.checkerClass, "typecheck/topol", "-Anomsgtext");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"typecheck/topol"};
        }
    }

    public static class UniverseTestsStrictPurity extends CheckerFrameworkPerFileTest {
        public UniverseTestsStrictPurity(File testFile) {
            super(testFile, UniverseTests.checkerClass, "typecheck/strictpurity", "-Anomsgtext", "-Alint=checkStrictPurity");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"typecheck/strictpurity"};
        }
    }

    public static class UniverseTestsLostYes extends CheckerFrameworkPerFileTest {
        public UniverseTestsLostYes(File testFile) {
            super(testFile, UniverseTests.checkerClass, "typecheck/lostyes", "-Anomsgtext", "-Alint=allowLost");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"typecheck/lostyes"};
        }
    }

    public static class UniverseTestsLostNo extends CheckerFrameworkPerFileTest {
        public UniverseTestsLostNo(File testFile) {
            super(testFile, UniverseTests.checkerClass, "typecheck/lostno", "-Anomsgtext", "-Alint=-allowLost");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"typecheck/lostno"};
        }
    }
}
