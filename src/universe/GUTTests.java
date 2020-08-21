package universe;

import java.io.File;

import javax.annotation.processing.AbstractProcessor;

import org.checkerframework.framework.test.CheckerFrameworkPerFileTest;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized.Parameters;

/**
 * JUnit tests for the GUT checker, which tests the GUT annotations.
 *
 * @author wmdietl
 */
public class GUTTests {
    /* The class name of the Checker to use.
     * Careful, class CheckerTest has also a field by this name.
     * Set this field in a subclass to a different checker, e.g. see GUTITests.
     */
    protected static Class<? extends AbstractProcessor> checkerClass = GUT.GUTChecker.class;

    public static void main(String[] args) {
        // org.junit.runner.JUnitCore.main("GUT.GUTTests");
        org.junit.runner.JUnitCore jc = new org.junit.runner.JUnitCore();
        Result run = jc.run(GUTTestsTopology.class,
                GUTTestsStrictPurity.class,
                GUTTestsLostYes.class,
                GUTTestsLostNo.class);

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


    public static class GUTTestsTopology extends CheckerFrameworkPerFileTest {
        public GUTTestsTopology (File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT/topol", "-Anomsgtext");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"GUT/topol"};
        }
    }

    public static class GUTTestsStrictPurity extends CheckerFrameworkPerFileTest {
        public GUTTestsStrictPurity(File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT/strictpurity", "-Anomsgtext", "-Alint=checkStrictPurity");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"GUT/strictpurity"};
        }
    }

    public static class GUTTestsLostYes extends CheckerFrameworkPerFileTest {
        public GUTTestsLostYes(File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT/lostyes", "-Anomsgtext", "-Alint=allowLost");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"GUT/lostyes"};
        }
    }

    public static class GUTTestsLostNo extends CheckerFrameworkPerFileTest {
        public GUTTestsLostNo(File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT/lostno", "-Anomsgtext", "-Alint=-allowLost");
        }
        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"GUT/lostno"};
        }
    }
}
