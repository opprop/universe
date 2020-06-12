import universe.qual.*;

class A {
	Object o;
}

public class FieldAccess {
	void foo(A a) {
		// :: fixable-error: (assignment.type.incompatible)
		@Rep Object fo = a.o;
	}
}
