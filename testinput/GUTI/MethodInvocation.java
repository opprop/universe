package GUTI;
import GUT.qual.*;



public class MethodInvocation {

	void foo(@Peer B b, @Rep Object op) {
		//:: fixable-error: (argument.type.incompatible)
		Object o = b.foo(op);
	}

	class B {
		//:: fixable-error: (return.type.incompatible)
		@Rep Object foo ( Object o) { return new Object();}
	}
}
