package typecheck.topol;

import universe.qual.Peer;
import universe.qual.Rep;

public class Cast {
    void foo() {
        @Peer Object p;
        // :: warning: (cast.unsafe)
        p = (@Peer Object) new @Rep Object();
    }
}
