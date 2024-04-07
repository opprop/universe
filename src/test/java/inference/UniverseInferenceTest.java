package universe;

import org.checkerframework.framework.test.TestUtilities;
import org.plumelib.util.IPair;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import checkers.inference.test.CFInferenceTest;
import universe.solver.UniverseSolverEngine;

public class UniverseInferenceTest extends CFInferenceTest {

    public UniverseInferenceTest(File testFile) {
        super(
                testFile,
                UniverseInferenceChecker.class,
                "",
                "-Anomsgtext",
                "-Astubs=src/main/java/universe/jdk.astub",
                "-d",
                "testTmp",
                "-doe");
    }

    @Override
    public IPair<String, List<String>> getSolverNameAndOptions() {
        return IPair.of(
                UniverseSolverEngine.class.getCanonicalName(),
                new ArrayList<String>(Arrays.asList("useGraph=false", "collectStatistic=true")));
    }

    @Override
    public boolean useHacks() {
        return true;
    }

    @Parameters
    public static List<File> getTestFiles() {
        List<File> testfiles = new ArrayList<>();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("tests", "inference"));
        return testfiles;
    }
}
