package GUT.topol;

import GUT.qual.Any;
import GUT.qual.Peer;
import GUT.qual.Rep;

import java.util.List;

// check that duplicate modifiers are handled correctly
class SimpleWF {
    // OK
    @Any Object f;

    //:: error: (type.invalid)
    @Any @Peer Object g;

    // this is ensured by Java already
    // @Peer @Peer Object h;

    //:: error: (type.invalid)
    @Rep @Peer Object i;

    //:: error: (type.invalid)
    List<@Rep @Peer Object> l;

    void m() {
        @Peer Object o = null;
        //:: error: (type.invalid)
        if (o instanceof @Peer @Rep Object) { 
            //ha!
        }
    }

    //:: error: (type.invalid)
    @Rep @Peer int rpi;
    //:: error: (type.invalid)
    @Peer int pi;
    //:: error: (type.invalid)
    @Rep long rl;
    //:: error: (type.invalid)
    @Any boolean ab;

    // OK
    @Peer Boolean bw;
    boolean sb;

    @Peer Object pothis = this;

    //:: error: (type.invalid)
    List<@Rep @Peer Object> error;

    void testerror() {
        if (this.hashCode() > 3) {
            // Tests that an invalid type in a store
            // can be merged without problem - ignore field.
        }
    }

}
