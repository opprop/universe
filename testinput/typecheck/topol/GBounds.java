import universe.qual.*;

class GBounds {

    class Data {}
    class SubData extends Data {}

    class C1<X extends @Peer Object> {
        X f;
        @Peer Object m() {
            return f;
        }
    }

    class C2<X extends @Peer Object> {}
    class C3<Y extends @Peer Data> {
        @Peer C2<Y> f;
    }

    class C4 {
        @Peer C2<@Peer Object> f;
        @Rep C2<@Rep SubData> g;
        @Rep Object h = new @Rep C2<@Rep SubData>();

        // :: error: (type.argument.type.incompatible)
        @Rep C2<@Peer Object> er1;
        // :: error: (type.argument.type.incompatible)
        @Peer C2<@Rep Object> er2;
        // :: error: (type.argument.type.incompatible)
        @Rep C2<@Any Object> er3;
        // :: error: (uts.lost.in.bounds)
        @Any C2<@Peer Object> er4;

    }

    @Rep C2<@Rep SubData> grrr;
    @Rep Object ro = grrr;

    class C5<Y extends @Rep Object> {}
    class C6 extends C5<@Rep Data> {}
    // :: error: (type.argument.type.incompatible)
    class C7 extends C5<@Peer Data> {}

}
