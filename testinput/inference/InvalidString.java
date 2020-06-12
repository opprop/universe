import java.util.Arrays;

public class InvalidString {
    void foo() {
        throwException("" + Arrays.asList("", ""));
    }

    void throwException(String s) {}
}
