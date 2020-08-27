import universe.qual.*;

class D<T extends Object>{

}

public class TypeVariableUse{
	//:: fixable-error: (assignment.type.incompatible)
	D<String> D = new @Rep D<@Any String>();

}
