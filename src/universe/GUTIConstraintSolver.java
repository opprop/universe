package universe;

import checkers.inference.model.Serializer;
import constraintsolver.ConstraintSolver;
import constraintsolver.Lattice;

public class GUTIConstraintSolver extends ConstraintSolver {

    @Override
    protected Serializer<?, ?> createSerializer(String value, Lattice lattice) {
        return new GUTIConstraintSerializer<>(value, lattice);
    }
}
