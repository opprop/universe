package GUTI;
import GUT.qual.*;

public class ConstructorCall {

	void foo(@Peer Object op) {
		//:: fixable-error: (argument.type.incompatible)
		C c = new @Rep C(op);
	}

	class C {
		public C(Object o){}
	}
}
