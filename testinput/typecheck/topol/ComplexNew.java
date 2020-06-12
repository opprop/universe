package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

/**
 *
 * @author wmdietl
 */
public class ComplexNew {

    ComplexNew(@Peer Object p) {}

    ComplexNew(@Rep Object p, int b) {}

    ComplexNew(@Any Object p, @Any Object b) {}

    // OK
    @Peer Object p = new @Peer ComplexNew(new @Peer Object());
    @Rep Object r = new @Rep ComplexNew(new @Rep Object());

    // :: error: (argument.type.incompatible)
    @Peer Object ep = new @Peer ComplexNew(new @Rep Object());

    // :: error: (argument.type.incompatible)
    @Rep Object er = new @Rep ComplexNew(new @Peer Object());

    // :: error: (uts.lost.parameter)
    @Peer Object ep2 = new @Peer ComplexNew(new @Rep Object(), 5);

    // :: error: (uts.new.ownership)
    @Any Object a = new @Any ComplexNew(new @Peer Object(), new @Peer Object());

    class SubComplexNew extends ComplexNew {
        SubComplexNew() {
            super(new @Rep Object(), 5);
        }

        SubComplexNew(int i) {
            // :: error: (argument.type.incompatible)
            super(new @Peer Object(), i);
        }
    }

}
