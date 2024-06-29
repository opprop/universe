import java.util.Arrays;

public class InvalidString {
    void foo() {
        throwException("" + Arrays.asList("", "") + "");
        throwException(3 + "");
    }

    void throwException(String s) {}
}
