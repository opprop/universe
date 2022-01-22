package universe.solver;

import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.backend.maxsat.MaxSatSolverFactory;
import checkers.inference.solver.frontend.Lattice;

public class UniverseSolverEngine extends SolverEngine {
    @Override
    protected SolverFactory createSolverFactory() {
        return new MaxSatSolverFactory(){
            @Override
            public MaxSatFormatTranslator createFormatTranslator(Lattice lattice) {
                return new UniverseFormatTranslator(lattice);
            }
        };
    }
}
