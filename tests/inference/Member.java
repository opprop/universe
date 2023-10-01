import java.util.List;

import universe.qual.*;

public class Member {

    void foo(E e) {
        // :: fixable-error: (assignment.type.incompatible)
        @Rep List<@Rep Object> l = e.le;
    }

    class E {
        List<Object> le;
    }
}
