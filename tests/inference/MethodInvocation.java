import universe.qual.*;

class B {
	//:: fixable-error: (return.type.incompatible)
	@Rep Object foo ( Object o) { return new Object();}
}
public class MethodInvocation {
	@Peer B b = new B();
	@Rep Object op;
	//:: fixable-error: (assignment.type.incompatible)
	//:: fixable-error: (argument.type.incompatible)
	Object o = b.foo(op);
}
