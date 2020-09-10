import universe.qual.*;

class C {
	public C(Object o){}
}
public class ConstructorCall {
	Object op;
	//:: fixable-error: (assignment.type.incompatible)
	//:: fixable-error: (argument.type.incompatible)
	C c = new @Rep C(op);
}
