import universe.qual.*;
import java.util.List;

public class Member {

    void foo(E e) {
        // :: fixable-error: (assignment.type.incompatible)
        @Rep List<@Rep Object> l = e.le;
    }

    class E {
        List<Object> le;
    }

}
