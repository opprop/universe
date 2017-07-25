import GUT.qual.*;

public class FieldAccess {
	void foo(A a) {
		//:: fixable-error: (assignment.type.incompatible)
		@Rep Object fo = a.o;
	}

	class A {
		Object o;
	}
}
