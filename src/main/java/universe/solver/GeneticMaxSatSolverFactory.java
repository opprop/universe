package universe.solver;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;

import java.util.Collection;

public class GeneticMaxSatSolverFactory extends AbstractSolverFactory<MaxSatFormatTranslator> {

    @Override
    public MaxSatFormatTranslator createFormatTranslator(Lattice lattice) {
        return new UniverseFormatTranslator(lattice);
    }

    @Override
    public Solver<?> createSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Lattice lattice) {
        MaxSatFormatTranslator formatTranslator = createFormatTranslator(lattice);
        return new UniverseGeneticMaxSatSolver(
                solverEnvironment, slots, constraints, formatTranslator, lattice);
    }
}
