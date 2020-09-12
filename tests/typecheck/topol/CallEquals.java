package typecheck.topol;

import universe.qual.*;

// This test only works, when we change the default annotation for
// Object.equals to @Any.
//@skip-test
public class CallEquals {
    void m(@Peer Object o, @Any Object o2) {
        o.equals(o2);
        o2.equals(o);
    }
}
