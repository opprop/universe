import universe.qual.*;

class D<T extends Object>{}

public class TypeVariableUse{

	void foo() {
		// :: fixable-error: (assignment.type.incompatible)
		@Rep D<String> D = new D<String>();
	}
}
