package GUTI;

import GUT.GUTAnnotatedTypeFactory;
import checkers.inference.InferenceMain;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import constraintsolver.Lattice;
import constraintsolver.VariableCombos;
import maxsatbackend.MaxSatSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;
import org.sat4j.core.VecInt;
import util.MathUtils;
import util.VectorUtils;

import javax.lang.model.element.AnnotationMirror;
import java.util.ArrayList;
import java.util.List;

public class GUTIMaxSatSerializer extends MaxSatSerializer {

    private static final Log logger = LogFactory.getLog(GUTIMaxSatSerializer.class.getName());

    private AnnotationMirror ANY, PEER, REP, LOST, VPLOST, BOTTOM, SELF;

    public GUTIMaxSatSerializer(Lattice lattice) {
        super(lattice);
        GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory) InferenceMain
                .getInstance().getRealTypeFactory();
        ANY = gutATF.ANY;
        PEER = gutATF.PEER;
        REP = gutATF.REP;
        LOST = gutATF.LOST;
        VPLOST = gutATF.VPLOST;
        BOTTOM = gutATF.BOTTOM;
        SELF = gutATF.SELF;
    }

    @Override
    public VecInt[] serialize(CombineConstraint combineConstraint) {
        return new VariableCombos<CombineConstraint, VecInt[]>(new VecInt[0]) {

            public VecInt[] accept(Slot target, Slot decl, Slot result,
                    CombineConstraint constraint) {

                final VecInt[] resultClauses;
/*                if (target instanceof CombVariableSlot) {
                    Slot realTypeWithoutClassBound = ((CombVariableSlot)target).getSecond();
                        target = realTypeWithoutClassBound;
                }*/
                if (target instanceof ConstantSlot) {
                    if (decl instanceof ConstantSlot) {
                        resultClauses = constant_constant((ConstantSlot) target,
                                (ConstantSlot) decl, (VariableSlot) result,
                                constraint);
                    } else {
                        resultClauses = constant_variable((ConstantSlot) target,
                                (VariableSlot) decl, (VariableSlot) result,
                                constraint);
                    }
                } else if (decl instanceof ConstantSlot) {
                    resultClauses = variable_constant((VariableSlot) target,
                            (ConstantSlot) decl, (VariableSlot) result,
                            constraint);
                } else {
                    resultClauses = variable_variable((VariableSlot) target,
                            (VariableSlot) decl, (VariableSlot) result,
                            constraint);
                }

                /*for (int i=0; i<resultClauses.length; i++) {
                    System.out.print(resultClauses[i] + "\n");
                }
                System.out.print("\n");*/

                return resultClauses;
            }

            protected VecInt[] constant_constant(ConstantSlot target,
                    ConstantSlot decl, VariableSlot result,
                    CombineConstraint combineConstraint) {
                List<VecInt> resultClauses = new ArrayList<VecInt>();

                if (AnnotationUtils.areSame(decl.getValue(), ANY)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), BOTTOM)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), LOST)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), VPLOST)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                }  else if (AnnotationUtils.areSame(decl.getValue(), PEER)) {
                    if (AnnotationUtils.areSame(target.getValue(), REP)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), SELF)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), PEER)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), ANY)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), LOST)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), VPLOST)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), BOTTOM)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));
                    }
                } else if (AnnotationUtils.areSame(decl.getValue(), REP)) {
                    if (AnnotationUtils.areSame(target.getValue(), SELF)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                    } else {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    }
                } else if (AnnotationUtils.areSame(decl.getValue(), SELF)) {
                    if (AnnotationUtils.areSame(target.getValue(), REP)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), SELF)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), SELF, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), PEER)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), ANY)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), LOST)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), VPLOST)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    } else if (AnnotationUtils.areSame(target.getValue(), BOTTOM)) {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));
                    }
                } else {
                    ErrorReporter.errorAbort("Error: Unknown declared type!");
                }

                logger.debug("Serialized combine constraint: " + combineConstraint);
                return resultClauses.toArray(new VecInt[resultClauses.size()]);
            }

            protected VecInt[] constant_variable(ConstantSlot target,
                    VariableSlot decl, VariableSlot result,
                CombineConstraint combineConstraint) {
                List<VecInt> resultClauses = new ArrayList<VecInt>();
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), ANY, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), BOTTOM, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), LOST, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), VPLOST, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));

                if (AnnotationUtils.areSame(target.getValue(), PEER)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST, lattice)));
                } else if (AnnotationUtils.areSame(target.getValue(), REP)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST, lattice)));
                } else if (AnnotationUtils.areSame(target.getValue(), ANY)) {
                    resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(),PEER, lattice),
                                MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(),REP, lattice),
                                MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                } else if (AnnotationUtils.areSame(target.getValue(), SELF)) {
                    resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), REP, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(),REP, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), SELF, lattice)));
                } else if (AnnotationUtils.areSame(target.getValue(), VPLOST)) {
                    resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                } else if (AnnotationUtils.areSame(target.getValue(), LOST)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                } else if (AnnotationUtils.areSame(target.getValue(), BOTTOM)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(),BOTTOM, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(),BOTTOM, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                } else {
                    ErrorReporter.errorAbort("Error: Unknown target type!");
                }

                logger.debug("Serialized combine constraint: " + combineConstraint);
                return resultClauses.toArray(new VecInt[resultClauses.size()]);
                }


            protected VecInt[] variable_constant(VariableSlot target,
                    ConstantSlot decl, VariableSlot result,
                    CombineConstraint combineConstraint) {

                List<VecInt> resultClauses = new ArrayList<VecInt>();

                if (AnnotationUtils.areSame(decl.getValue(), ANY)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), BOTTOM)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), LOST)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), VPLOST)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                }  else if (AnnotationUtils.areSame(decl.getValue(), PEER)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), REP, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), PEER, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), ANY, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), LOST, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), VPLOST, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), BOTTOM, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), REP)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                } else if (AnnotationUtils.areSame(decl.getValue(), SELF)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), REP, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), SELF, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), PEER, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), ANY, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), LOST, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), VPLOST, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), BOTTOM, lattice),
                            MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));
                } else {
                    ErrorReporter.errorAbort("Error: Unknown declared type!");
                }

                logger.debug("Serialized combine constraint: " + combineConstraint);
                return resultClauses.toArray(new VecInt[resultClauses.size()]);
        }

            protected VecInt[] variable_variable(VariableSlot target,
                    VariableSlot decl, VariableSlot result,
                    CombineConstraint combineConstraint) {
                List<VecInt> resultClauses = new ArrayList<VecInt>();
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), ANY, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), LOST, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), VPLOST, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), BOTTOM, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));

                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), REP, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), REP, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));

                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), ANY, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), ANY, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), LOST, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), LOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), VPLOST, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), PEER, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), REP, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), SELF, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), BOTTOM, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));


                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), REP, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), REP, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), SELF, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), PEER, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), ANY, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), LOST, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), VPLOST, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST, lattice)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), BOTTOM, lattice),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER, lattice),
                        MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM, lattice)));

                logger.debug("Serialized combine constraint: " + combineConstraint);
                return resultClauses.toArray(new VecInt[resultClauses.size()]);
        }
    }.accept(combineConstraint.getTarget(), combineConstraint.getDeclared(), combineConstraint.getResult(), combineConstraint);
    }
}
