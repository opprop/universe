import universe.qual.*;
class A {
	Object o;
}

public class FieldAccess {
	A a = new A();
	//:: fixable-error: (assignment.type.incompatible)
	@Rep Object fo = a.o; 
}
