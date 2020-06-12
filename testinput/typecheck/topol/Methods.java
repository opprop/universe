package typecheck.topol;

import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.Rep;

/**
 *
 * @author wmdietl
 */
public class Methods {

    // :: error: (uts.receiver.not.self)
    void receiver(@Any Methods this) {
    }

    @Peer Object m(@Peer Object o) { return null; }

    @Rep Object foo(@Peer Object o) { return null; }

    void ok() {
        @Peer Methods p = new Methods();
        @Peer Object mo = p.m( new @Peer Object() );

        @Any Object ma = p.foo( new @Peer Object() );

        @Rep Methods r = new @Rep Methods();
        @Rep Object ro = r.m( new @Rep Object() );

        @Any Object ra = r.foo( new @Rep Object() );
    }
    @Peer Methods p_field;
    @Rep Methods r_field;
    @Any Methods a_field;

    void errors() {

        // :: error: (assignment.type.incompatible)
        @Rep Object mo = p_field.m( new @Peer Object() );
        // :: error: (argument.type.incompatible)
        @Peer Object mo2 = p_field.m( new @Rep Object() );

        // :: error: (assignment.type.incompatible)
        @Rep Object ma = p_field.foo( new @Peer Object() );

        // :: error: (argument.type.incompatible)
        @Any Object ro = r_field.m( new @Peer Object() );

        // :: error: (assignment.type.incompatible)
        @Peer Object ro2 = r_field.m( null );

        // :: error: (argument.type.incompatible)
        @Any Object ra = r_field.foo( new @Peer Object() );

        // :: error: (uts.lost.parameter)
        a_field.m(new @Peer Object());
    }

    class SubMethods1 extends Methods {
        // :: error: (override.return.invalid)
        @Rep Object m(@Peer Object o) { return null; }
    }

    class SubMethods2 extends Methods {
        // :: error: (override.param.invalid)
        @Peer Object m(@Rep Object o) { return null; }
    }

    class SubMethods3 extends Methods {
        @Peer Object m(@Peer Object o) { return null; }
    }
}
