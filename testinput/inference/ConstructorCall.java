import universe.qual.*;

class C {
	public C(Object o){}
}

public class ConstructorCall {

	void foo(@Peer Object op) {
		// :: fixable-error: (argument.type.incompatible)
		C c = new @Rep C(op);
	}
}
