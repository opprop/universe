package GUTI;

import checkers.inference.test.CFInferenceTest;
import org.checkerframework.framework.test.TestUtilities;
import org.checkerframework.javacutil.Pair;
import org.junit.runners.Parameterized.Parameters;
import universe.solver.GUTSolverEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUTInferenceTest extends CFInferenceTest {

    public GUTInferenceTest(File testFile) {
        super(testFile, universe.GUTChecker.class, "",
                "-Anomsgtext", "-Astubs=src/universe/jdk.astub", "-d", "testTmp");
    }

    @Override
    public Pair<String, List<String>> getSolverNameAndOptions() {
        return Pair.of(GUTSolverEngine.class.getCanonicalName(),
                new ArrayList<String>(Arrays.asList("useGraph=false", "collectStatistic=true")));
    }

    @Override
    public boolean useHacks() {
        return true;
    }

    @Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testinput", "inference"));
        return testfiles;
    }
}
