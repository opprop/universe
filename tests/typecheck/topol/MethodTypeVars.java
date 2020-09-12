package typecheck.topol;

import universe.qual.*;

public class MethodTypeVars {
void foobar(Object p) {}
{foobar(null);}
    <T extends @Any Object> T transform(T a) { return null; }

    <T extends @Any Object> T [] toArray(T [] a) { return null; }

    // Arrays are not in upper bounds :-(
    // <T extends Object[]> T toArray2(T a) { return null; }

    void m() {
        Object o = this.transform(new Object());
        String [] oa = this.toArray(new String[10]);
    }

    void mUniverse() {
        @Rep Object o = this.transform(new @Rep Object());
        @Rep Object @Peer [] oa = this.toArray(new @Rep Object @Peer [10]);

        // note that the following is not allowed, as "T[]" means "T @Peer[]"!
        // Hmm, maybe do add the @Poly annotation, mostly for arrays?
        //:: error: (argument.type.incompatible)
        @Rep String @Any [] oa2 = this.toArray(new @Rep String @Any [10]);
    }
}
