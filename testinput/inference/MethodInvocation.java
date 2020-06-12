import universe.qual.*;

class B {
	// :: fixable-error: (return.type.incompatible)
	@Rep Object foo (Object o) { return new Object();}
}

public class MethodInvocation {

	void foo(@Peer B b, @Rep Object op) {
		// :: fixable-error: (argument.type.incompatible)
		Object o = b.foo(op);
	}


}
