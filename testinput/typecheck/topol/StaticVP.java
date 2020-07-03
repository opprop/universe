package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

public class StaticVP {
    // checking for warnings doesn't work, and needs extra lint option
    //TODO:: error: (uts.static.peer.warning)
    static @Peer Object p;
    static Object pp;

    // :: error: (uts.static.rep.forbidden)
    static @Rep Object r;

    // The error (uts.static.rep.forbidden) would also apply,
    // but this property is checked earlier.
    // :: error: (type.invalid.conflicting.annos)
    static @Peer @Rep Object pr;

    // ok
    static @Any Object a;

    static void bar() {
        // :: error: (uts.static.rep.forbidden)
       @Rep Object or;

        // :: error: (uts.static.rep.forbidden)
        or = new @Rep Object();
    }
    // :: error: (uts.static.rep.forbidden)
    static @Any Object ao = new @Rep Object();
    static {
        // :: error: (uts.static.rep.forbidden)
        new @Rep Object();
    }

    /* Syntax not supported.
    void m() {
        @Peer Object op = @Peer StaticVP.p;
        @Rep Object or = @Rep StaticVP.p;
        @Any Object oa = @Any StaticVP.p;

        //TODO:: (type.incompatible)
        @Rep Object pr = @Peer StaticVP.p;
    }
    */
}
