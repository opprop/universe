package GUT;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.framework.test.CheckerFrameworkPerFileTest;
import org.checkerframework.framework.test.TestUtilities;
import org.junit.runners.Parameterized.Parameters;

public class GUTTestsLostNo extends CheckerFrameworkPerFileTest {
    public GUTTestsLostNo(File testFile) {
        super(testFile, GUT.GUTChecker.class, "", "-Anomsgtext", "-Alint=-allowLost", "-d", "testTmp");
    }

    @Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testinput", "GUT/lostno"));
        return testfiles;
    }
}