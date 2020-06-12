package typecheck.topol;

import universe.qual.*;

class Defaults {
    // default for fields is peer
    @Any Object a;

    // :: error: (assignment.type.incompatible)
    Object a2 = a;

    @Rep Object r;
    // default for fields is peer
    // :: error: (assignment.type.incompatible)
    Object r2 = r;
    // :: error: (assignment.type.incompatible)
    @Rep Object r3 = r2;

    void m() {
        // flow from RHS, should be @Any
        Object loc1 = a;
        // :: error: (assignment.type.incompatible)
        @Rep Object rloc1 = loc1;
        // :: error: (assignment.type.incompatible)
        @Peer Object ploc1 = loc1;

        //flow from RHS, should be @Rep
        Object loc2 = r;
        @Rep Object rloc2 = loc2;

        // :: error: (assignment.type.incompatible)
        @Peer Object ploc2 = loc2;
    }
}
