package GUT.topol;

import GUT.qual.*;

public class CondExpr {

    void m() {
        @Peer Object o = true ? new @Peer Object() : this;
    }
}