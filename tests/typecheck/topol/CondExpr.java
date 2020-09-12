package typecheck.topol;

import universe.qual.*;

public class CondExpr {

    void m() {
        @Peer Object o = true ? new @Peer Object() : this;
    }
}
