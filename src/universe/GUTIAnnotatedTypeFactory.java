package universe;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;

import com.sun.source.tree.Tree;

import universe.GUTAnnotatedTypeFactory;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.VariableSlot;

public class GUTIAnnotatedTypeFactory
        extends InferenceAnnotatedTypeFactory {

    public GUTIAnnotatedTypeFactory(InferenceChecker inferenceChecker,
            boolean withCombineConstraints,
            BaseAnnotatedTypeFactory realTypeFactory,
            InferrableChecker realChecker, SlotManager slotManager,
            ConstraintManager constraintManager) {
        super(inferenceChecker, withCombineConstraints, realTypeFactory,
                realChecker, slotManager, constraintManager);
        postInit();
    }

    /**
     * The type of "this" is always "self".
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        if (type != null) {
            GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory) InferenceMain
                    .getInstance().getRealTypeFactory();
            type.replaceAnnotation(gutATF.SELF);
        }
        return type;
    }
/*    @Override
    public VariableAnnotator createVariableAnnotator(
            InferenceAnnotatedTypeFactory inferenceTypeFactory,
            BaseAnnotatedTypeFactory realTypeFactory,
            InferrableChecker realChecker, SlotManager slotManager,
            ConstraintManager constraintManager) {
        return new GUTIVariableAnnotator(inferenceTypeFactory, realTypeFactory,
                realChecker, slotManager, constraintManager);
    }*/

    static class GUTIVariableAnnotator extends VariableAnnotator {

        public GUTIVariableAnnotator(
                InferenceAnnotatedTypeFactory inferenceTypeFactory,
                AnnotatedTypeFactory realTypeFactory,
                InferrableChecker realChecker, SlotManager slotManager,
                ConstraintManager constraintManager) {
            super(inferenceTypeFactory, realTypeFactory, realChecker,
                    slotManager, constraintManager);
        }

/*        @Override
        protected void handleClassDeclarationBound(
                AnnotatedDeclaredType classType) {
            return;
        }

        @Override
        protected void handleInstantiationConstraint(AnnotatedDeclaredType adt,
                VariableSlot instantiationSlot) {
            return;
        }*/

    }
}

