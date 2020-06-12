import universe.qual.*;

public class BottomReceiver {
    void foo(char [] a) {
        String s = "Hello";
        // :: fixable-error: (assignment.type.incompatible)
        a = s.toCharArray();
    }
}
