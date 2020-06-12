package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

public class SimpleVP {
    @Peer Object p;
    @Rep Object r;
    @Any Object a;

    void onThis() {
        // implicit
        @Peer Object po = p;
        @Rep Object ro = r;
        @Any Object ao = a;

        // explicit
        @Peer Object poe = this.p;
        @Rep Object roe = this.r;
        @Any Object aoe = this.a;

        // :: error: (assignment.type.incompatible)
        @Rep Object epo = this.p;
        // :: error: (assignment.type.incompatible)
        @Peer Object ero = this.r;
    }

    void m() {
        @Peer SimpleVP psv = new @Peer SimpleVP();
        @Peer Object op = psv.p;

        @Rep SimpleVP rsv = new @Rep SimpleVP();
        @Rep Object or = rsv.p;

        @Any SimpleVP asv = new @Peer SimpleVP();
        @Any Object oa = asv.p;

        // :: error: (assignment.type.incompatible)
        @Rep Object pr = psv.r;

        @Any Object pra = psv.a;
    }

    class SubSimpleVP extends SimpleVP {
        void onSuper() {
            @Peer Object po = super.p;
            @Rep Object ro = super.r;
            @Any Object ao = super.a;

            // :: error: (assignment.type.incompatible)
            @Rep Object epo = super.p;
            // :: error: (assignment.type.incompatible)
            @Peer Object ero = super.r;
        }
    }
}
