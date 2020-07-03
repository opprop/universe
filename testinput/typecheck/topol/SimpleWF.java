package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

import java.util.List;

// check that duplicate modifiers are handled correctly
class SimpleWF {
    // OK
    @Any Object f;

    // :: error: (type.invalid.conflicting.annos)
    @Any @Peer Object g;

    // this is ensured by Java already
    // @Peer @Peer Object h;

    // :: error: (type.invalid.conflicting.annos)
    @Rep @Peer Object i;

    // :: error: (type.invalid.conflicting.annos)
    List<@Rep @Peer Object> l;

    void m() {
        @Peer Object o = null;
        // :: error: (type.invalid.conflicting.annos)
        if (o instanceof @Peer @Rep Object) {
            //ha!
        }
    }

    // :: error: (type.invalid.conflicting.annos)
    @Rep @Peer int rpi;
    // :: error: (type.invalid.annotations.on.use)
    @Peer int pi;
    // :: error: (type.invalid.annotations.on.use)
    @Rep long rl;
    // :: error: (type.invalid.annotations.on.use)
    @Any boolean ab;

    // :: error: (type.invalid.annotations.on.use)
    @Peer Boolean bw;
    boolean sb;

    @Peer Object pothis = this;

    // :: error: (type.invalid.conflicting.annos)
    List<@Rep @Peer Object> error;

    void testerror() {
        if (this.hashCode() > 3) {
            // Tests that an invalid type in a store
            // can be merged without problem - ignore field.
        }
    }

}
