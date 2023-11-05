package universe.solver;

import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.backend.SolverFactory;

public class UniverseGASolverEngine extends SolverEngine {
    @Override
    protected SolverFactory createSolverFactory() {
        return new GeneticMaxSatSolverFactory();
    }
}
