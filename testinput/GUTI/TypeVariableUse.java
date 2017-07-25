import GUT.qual.*;

public class TypeVariableUse{

	void foo() {
		//:: fixable-error: (assignment.type.incompatible) :: fixable-error: (uts.vplost.in.bounds)
		D<String> D = new @Peer D<@Rep String>();
	}

	class D<T extends Object>{}
}
