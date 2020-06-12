import universe.qual.Rep;
import universe.qual.Peer;

public class Test2 {
    Object foo;

    public void init() {
        // :: fixable-error: (assignment.type.incompatible)
        this.foo = new @Rep Object();

        do_something(this.foo);
        if(foo == null) {
		}
    }

    void do_something(Object a) {}
}
