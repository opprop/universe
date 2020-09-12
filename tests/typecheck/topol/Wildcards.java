package typecheck.topol;

import universe.qual.*;

public class Wildcards<E extends @Any Object> {

    boolean addAll(Wildcards<? extends E> c) {
        return true;
    }

    void m() {
        Wildcards<Object> wo = new Wildcards<Object>();
        wo.addAll(wo);
        Wildcards<? extends Object> wo2 = new Wildcards<String>();
        wo.addAll(wo2);
    }
}
