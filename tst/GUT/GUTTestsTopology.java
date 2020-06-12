package GUT;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.framework.test.CheckerFrameworkPerFileTest;
import org.checkerframework.framework.test.TestUtilities;
import org.junit.runners.Parameterized.Parameters;

public class GUTTestsTopology extends CheckerFrameworkPerFileTest {
    public GUTTestsTopology (File testFile) {
        super(testFile, universe.GUTChecker.class,
                "", "-Anomsgtext", "-AconservativeUninferredTypeArguments", "-d", "testTmp");
    }

    @Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testinput", "typecheck/topol"));
        return testfiles;
    }
}
