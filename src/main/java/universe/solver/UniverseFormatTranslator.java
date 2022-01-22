package universe.solver;

import checkers.inference.solver.backend.encoder.ConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATConstraintEncoderFactory;
import checkers.inference.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

public class UniverseFormatTranslator extends MaxSatFormatTranslator {

    public UniverseFormatTranslator(Lattice lattice) {
        super(lattice);

    }

    @Override
    protected ConstraintEncoderFactory<VecInt[]> createConstraintEncoderFactory() {
        return new MaxSATConstraintEncoderFactory(lattice, typeToInt, this){
            @Override
            public CombineConstraintEncoder<VecInt[]> createCombineConstraintEncoder() {
                return new UniverseCombineConstraintEncoder(lattice, typeToInt);
            }
        };
    }
}
