package typecheck.topol;

import universe.qual.*;


class GList<X extends @Any Object> {
    @Rep GNode<X> head;

    void add(X o) {
        head = new @Rep GNode<X>(o, head);
    }

    X get() {
        return head.val;
    }
}

class GNode<X extends @Any Object> {
    X val;
    @Peer GNode<X> next;

    GNode(X v, @Peer GNode<X> n) {
        val = v;
        next = n;
    }
}


public class Generics {
    @Peer GList<@Rep Object> pro = new @Peer GList<@Rep Object>();

    void bar() {
        @Peer Object po = pro;

        // :: error: (assignment.type.incompatible)
        @Rep Object ro = pro;

        // :: error: (assignment.type.incompatible)
        po = pro.get();
        ro = pro.get();

        // :: error: (argument.type.incompatible)
        pro.add(new @Peer Object());

        pro.add(new @Rep Object());

        pro.head.val = new @Rep Object();
    }

    void fooPeer() {
        @Peer Generics pg = new @Peer Generics();
        // :: error: (uts.lost.parameter)
        pg.pro.add(new @Rep Object());

        @Any Object ao = pg.pro.get();
        // :: error: (assignment.type.incompatible)
        @Rep Object ro = pg.pro;
        @Peer Object po = pg.pro;
    }

    void fooRep() {
        @Rep Generics rg = new @Rep Generics();
        // :: error: (uts.lost.parameter)
        rg.pro.add(new @Rep Object());

        @Any Object ao = rg.pro.get();
        // :: error: (assignment.type.incompatible)
        @Peer Object ro = rg.pro;
        @Rep Object po = rg.pro;
    }
}
