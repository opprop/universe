package universe.solver;

import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.maxsat.MathUtils;
import checkers.inference.solver.backend.maxsat.VectorUtils;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATAbstractConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.sat4j.core.VecInt;

import javax.lang.model.element.AnnotationMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static universe.UniverseAnnotationMirrorHolder.ANY;
import static universe.UniverseAnnotationMirrorHolder.BOTTOM;
import static universe.UniverseAnnotationMirrorHolder.LOST;
import static universe.UniverseAnnotationMirrorHolder.PEER;
import static universe.UniverseAnnotationMirrorHolder.REP;
import static universe.UniverseAnnotationMirrorHolder.SELF;

public class UniverseCombineConstraintEncoder extends MaxSATAbstractConstraintEncoder implements CombineConstraintEncoder<VecInt[]> {

    public UniverseCombineConstraintEncoder(Lattice lattice, Map<AnnotationMirror, Integer> typeToInt) {
        super(lattice, typeToInt);
    }

    /**
     * Wrapper method to get integer id of an AnnotationMirror to avoid Map get operations
     */
    private final int id(AnnotationMirror am) {
        return typeToInt.get(am);
    }

    private final boolean is(ConstantSlot cs, AnnotationMirror am) {
        return AnnotationUtils.areSame(cs.getValue(), am);
    }

    @Override
    public VecInt[] encodeVariable_Variable(VariableSlot target, VariableSlot declared, CombVariableSlot result) {
        List<VecInt> resultClauses = new ArrayList<VecInt>();
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(ANY), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(BOTTOM), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(BOTTOM), lattice)));

        resultClauses.add(VectorUtils.asVec(
                MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(LOST), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(LOST), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));

        // declared is rep
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(PEER), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(REP), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(SELF), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(REP), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(ANY), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(LOST), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));

        // declared is self
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(PEER), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(REP), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(SELF), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(SELF), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(ANY), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(LOST), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));

        // declared is peer
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(PEER), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(PEER), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(REP), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(REP), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(SELF), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(PEER), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(ANY), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(target.getId(), id(LOST), lattice),
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));

        return resultClauses.toArray(new VecInt[resultClauses.size()]);
    }

    @Override
    public VecInt[] encodeVariable_Constant(VariableSlot target, ConstantSlot declared, CombVariableSlot result) {
        List<VecInt> resultClauses = new ArrayList<VecInt>();

        if (is(declared, ANY)) {
            resultClauses.add(VectorUtils.asVec(
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else if (is(declared, BOTTOM)) {
            resultClauses.add(VectorUtils.asVec(
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(BOTTOM), lattice)));
        } else if (is(declared, PEER)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(PEER), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(REP), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(PEER), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(ANY), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else if (is(declared, REP)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(REP), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(ANY), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else if (is(declared, SELF)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(SELF), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(ANY), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else if (is(declared, LOST)) {
            resultClauses.add(VectorUtils.asVec(
                    MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(target.getId(), id(BOTTOM), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else {
            throw new BugInCF("Error: Unknown declared type: " + declared.getValue());
        }

        return resultClauses.toArray(new VecInt[resultClauses.size()]);
    }

    @Override
    public VecInt[] encodeConstant_Variable(ConstantSlot target, VariableSlot declared, CombVariableSlot result) {
        List<VecInt> resultClauses = new ArrayList<>();

        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(ANY), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        resultClauses.add(VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(declared.getId(), id(BOTTOM), lattice),
                MathUtils.mapIdToMatrixEntry(result.getId(), id(BOTTOM), lattice)));

        if (is(target, PEER)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(PEER), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        } else if (is(target, REP)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(REP), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        } else if (is(target, SELF)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(PEER), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(REP), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(SELF), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        } else if (is(target, ANY) || is(target, LOST)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(),id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        } else if (is(target, BOTTOM)) {
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(),id(PEER), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(REP), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(SELF), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
            resultClauses.add(VectorUtils.asVec(
                    -MathUtils.mapIdToMatrixEntry(declared.getId(), id(LOST), lattice),
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else {
            throw new BugInCF("Error: Unknown target type: " + target.getValue());
        }

        return resultClauses.toArray(new VecInt[resultClauses.size()]);
    }

    @Override
    public VecInt[] encodeConstant_Constant(ConstantSlot target, ConstantSlot declared, CombVariableSlot result) {
        List<VecInt> resultClauses = new ArrayList<>();

        if (is(declared, ANY)) {
            resultClauses.add(VectorUtils.asVec(
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else if (is(declared, BOTTOM)) {
            resultClauses.add(VectorUtils.asVec(
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(BOTTOM), lattice)));
        } else if (is(target, BOTTOM)) {
            resultClauses.add(VectorUtils.asVec(
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(ANY), lattice)));
        } else if (is(declared, LOST)) {
            resultClauses.add(VectorUtils.asVec(
                    MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
        } else if (is(declared, PEER)) {
            if (is(target, REP)) {
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), id(REP), lattice)));
            } else if (is(target, SELF) ||
                            AnnotationUtils.areSame(target.getValue(), PEER)) {
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), id(PEER), lattice)));
            } else {
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            }
        } else if (is(declared, REP) || is(declared, SELF)) {
            if (is(target, SELF)) {
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), id(declared.getValue()), lattice)));
            } else {
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), id(LOST), lattice)));
            }
        } else {
            throw new BugInCF("Error: Unknown declared or target type: " + declared.getValue() + target.getValue());
        }

        return resultClauses.toArray(new VecInt[resultClauses.size()]);
    }
}
