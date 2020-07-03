package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

public class ArrayWF {
    // :: error: (type.invalid.annotations.on.use)
    @Peer int[] epi;
    // :: error: (type.invalid.annotations.on.use)
    @Rep int[] eri;
    // :: error: (type.invalid.annotations.on.use)
    @Any int[] eai;

    // :: error: (type.invalid.conflicting.annos)
    @Peer @Rep Object[] pro;

    // :: error: (type.invalid.conflicting.annos)
    @Peer Object @Rep @Peer[] porp;

    // :: error: (type.invalid.conflicting.annos)
    @Peer Object @Rep[] @Rep @Peer[] porrp;

    // :: error: (type.invalid.conflicting.annos)
    @Peer Object [] [] @Rep @Peer[] poaarp;
}
