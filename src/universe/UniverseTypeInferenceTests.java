package universe;

public class UniverseTypeInferenceTests extends UniverseTests {
    public static void main(String[] args) {
      UniverseTests.checkerClass = universe.UniverseTypeInferenceChecker.class;
      UniverseTests.main(args);
    }
}
