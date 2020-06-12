package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

public class ArrayVP {
    int @Peer[] pi;
    int @Rep[] ri;
    int @Any[] ai;

    void primOnThis() {
        int @Peer[] xpi = this.pi;
        int @Rep[] xri = this.ri;
        int @Any[] xai = this.ai;

        @Peer Object xpo = this.pi;
        @Rep Object xro = this.ri;
        @Any Object xao = this.ai;

        // :: error: (assignment.type.incompatible)
        @Rep Object epo = this.pi;

        // :: error: (assignment.type.incompatible)
        @Peer Object ero = this.ri;

        int i = pi[0];
        i = ri[0];
        i = ai[0];
    }

    void primOnReceiver() {
        @Rep ArrayVP rav = new @Rep ArrayVP();
        int @Rep[] o = rav.pi;

        @Peer ArrayVP pav = new @Peer ArrayVP();
        // :: error: (assignment.type.incompatible)
        int @Rep[] o2 = pav.ri;
        // ok
        int @Any[] o3 = pav.ri;
    }

    @Peer Object @Rep[] por;
    @Rep Object @Peer[] rop;

    @Any Object @Peer[] aop;
    @Any Object @Rep[] aor;

    @Peer Object @Any[] poa;
    @Rep Object @Any[] roa;

    void objOnReceiver() {
        @Peer ArrayVP pavp = new @Peer ArrayVP();
        @Rep ArrayVP ravp = new @Rep ArrayVP();

        @Peer Object po;
        @Rep Object ro;

        // ok
        po = pavp.por[0];
        po = pavp.rop;

        // :: error: (assignment.type.incompatible)
        po = pavp.rop[0];

        // :: error: (uts.lost.lhs)
        pavp.rop = rop;

        // ok
        roa = ravp.por;
        ro = ravp.por[0];

        // :: error: (assignment.type.incompatible)
        ro = pavp.rop[0];
    }

    void objOnThis() {
        //ok
        @Peer Object po = this.por[0];
        // :: error: (assignment.type.incompatible)
        po = this.rop[0];

        // ok
        @Rep Object ro = this.rop[0];
        // :: error: (assignment.type.incompatible)
        ro = this.por[0];

        // ok
        po = rop;
        po = aop;
        ro = por;
        ro = aor;

        // :: error: (assignment.type.incompatible)
        po = por;
        // :: error: (assignment.type.incompatible)
        po = aor;
        // :: error: (assignment.type.incompatible)
        po = poa;

        // :: error: (assignment.type.incompatible)
        ro = rop;
        // :: error: (assignment.type.incompatible)
        ro = aop;
        // :: error: (assignment.type.incompatible)
        ro = roa;
    }

    class SubArrayVP extends ArrayVP {
        void onSuper() {
            @Peer Object po = super.pi;
            @Rep Object ro = super.ri;
            @Any Object ao = super.ai;

            // :: error: (assignment.type.incompatible)
            @Rep Object epo = super.pi;
            // :: error: (assignment.type.incompatible)
            @Peer Object ero = super.ri;
        }
    }

    // there is a problem with defaulting of arrays :-(
    void testIntArr(int[] t) {}
    void demoIntArr() {
        int[] x = null;
        testIntArr(x);
        testIntArr(new int[] { 5 });
    }
}
