package GUTI;

import GUT.GUTAnnotatedTypeFactory;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.ErrorReporter;

import javax.lang.model.element.AnnotationMirror;

public class GUTIAnnotatedTypeFactory
        extends InferenceAnnotatedTypeFactory {

    public static final Log logger = LogFactory.getLog(GUTIAnnotatedTypeFactory.class.getSimpleName());

    public GUTIAnnotatedTypeFactory(InferenceChecker inferenceChecker,
            boolean withCombineConstraints,
            BaseAnnotatedTypeFactory realTypeFactory,
            InferrableChecker realChecker, SlotManager slotManager,
            ConstraintManager constraintManager) {
        super(inferenceChecker, withCombineConstraints, realTypeFactory,
                realChecker, slotManager, constraintManager);
        postInit();
    }

    @Override
    public AnnotatedTypeMirror getAnnotatedType(Tree tree) {
        AnnotatedTypeMirror atm = super.getAnnotatedType(tree);
        if (atm instanceof AnnotatedPrimitiveType) {
            GUTAnnotatedTypeFactory gutAnnotatedTypeFactory = (GUTAnnotatedTypeFactory) realTypeFactory;
            Slot bottomConstantSlot = slotManager.getSlot(gutAnnotatedTypeFactory.BOTTOM);
            AnnotationMirror bottomVarAnnot = slotManager.getAnnotation(bottomConstantSlot);
            atm.replaceAnnotation(bottomVarAnnot);
        }
        return atm;
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(new ImplicitsTreeAnnotator(this), new GUTIInferenceTreeAnnotater(this,
                realChecker, realTypeFactory, new GUTIVariableAnnotator(this, realTypeFactory, realChecker, slotManager, constraintManager), slotManager));
    }
/*    @Override
    public AnnotatedTypeMirror getAnnotatedType(Tree tree) {
        AnnotatedTypeMirror atm = super.getAnnotatedType(tree);
        return atm;
    }*/

    /**
     * The type of "this" is always "self".
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory) realTypeFactory;
        //type.replaceAnnotation(gutATF.SELF);
        try {
            VariableSlot variableSlot = slotManager.getVariableSlot(type);
            constraintManager.addEqualityConstraint(variableSlot, slotManager.getSlot(gutATF.SELF));
            /*SlotManager sm = InferenceMain.getInstance().getSlotManager();
            type.replaceAnnotation(sm.getAnnotation(sm.getSlot(gutATF.SELF)));*/
            logger.info("Added equality constraint between @Self and receiver \"this\" when visiting tree: \n" + tree);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Missing VarAnnot annotation:")) {
                logger.fatal(type + " doesn't contain VarAnnot!", e);
            } else {
                logger.fatal("Unknwon runtime exception!", e);
            }
            ErrorReporter.errorAbort("Error when getting self type", tree);
        }

        //type.replaceAnnotation(gutATF.SELF);
        //InferenceMain.getInstance().getVisitor().mainIs(type, gutATF.SELF, "uts.cast.type.error", tree);
            /*InferenceMain.getInstance().getVisitor().doesNotContain(type, new AnnotationMirror[]{gutATF.BOTTOM, gutATF.PEER, gutATF.REP,
            gutATF.LOST, gutATF.VPLOST, gutATF.ANY}, "uts.cast.type.error", tree);*/
        return type;
    }

/*    @Override
    protected ViewpointAdaptor<?> createViewpointAdaptor() {
        return null;
    }*/

    /*    @Override
    public VariableAnnotator createVariableAnnotator(
            InferenceAnnotatedTypeFactory inferenceTypeFactory,
            BaseAnnotatedTypeFactory realTypeFactory,
            InferrableChecker realChecker, SlotManager slotManager,
            ConstraintManager constraintManager) {
        return new GUTIVariableAnnotator(inferenceTypeFactory, realTypeFactory,
                realChecker, slotManager, constraintManager);
    }*/

    static class GUTIInferenceTreeAnnotater extends InferenceTreeAnnotator {

        public GUTIInferenceTreeAnnotater(InferenceAnnotatedTypeFactory atypeFactory, InferrableChecker realChecker, AnnotatedTypeFactory realAnnotatedTypeFactory, VariableAnnotator variableAnnotator, SlotManager slotManager) {
            super(atypeFactory, realChecker, realAnnotatedTypeFactory, variableAnnotator, slotManager);
        }

        @Override
        public Void visitLiteral(LiteralTree literalTree, AnnotatedTypeMirror atm) {
            super.visitLiteral(literalTree, atm);
            GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
            atm.replaceAnnotation(gutATF.BOTTOM);
            return null;
        }

        @Override
        public Void visitTypeCast(TypeCastTree typeCast, AnnotatedTypeMirror atm) {
            super.visitTypeCast(typeCast, atm);
            if (!(atm instanceof AnnotatedPrimitiveType)) {
                GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
                SlotManager sm = InferenceMain.getInstance().getSlotManager();
                ConstraintManager cm = InferenceMain.getInstance().getConstraintManager();
                cm.addInequalityConstraint(sm.getVariableSlot(atm), sm.getSlot(gutATF.BOTTOM));
                cm.addInequalityConstraint(sm.getVariableSlot(atm), sm.getSlot(gutATF.LOST));
                cm.addInequalityConstraint(sm.getVariableSlot(atm), sm.getSlot(gutATF.VPLOST));
                cm.addInequalityConstraint(sm.getVariableSlot(atm), sm.getSlot(gutATF.ANY));
            }

            return null;
        }
    }

    static class GUTIVariableAnnotator extends VariableAnnotator {

        public GUTIVariableAnnotator(
                InferenceAnnotatedTypeFactory inferenceTypeFactory,
                AnnotatedTypeFactory realTypeFactory,
                InferrableChecker realChecker, SlotManager slotManager,
                ConstraintManager constraintManager) {
            super(inferenceTypeFactory, realTypeFactory, realChecker,
                    slotManager, constraintManager);
        }

        @Override
        public Void visitPrimitive(AnnotatedPrimitiveType primitiveType, Tree tree) {
            super.visitPrimitive(primitiveType, tree);
            GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
            //primitiveType.replaceAnnotation(gutATF.BOTTOM);
            try {
                ConstraintManager cm = InferenceMain.getInstance().getConstraintManager();
                SlotManager sm = InferenceMain.getInstance().getSlotManager();
                VariableSlot variableSlot = sm.getVariableSlot(primitiveType);
                //cm.addEqualityConstraint(variableSlot, sm.getSlot(gutATF.BOTTOM));
                primitiveType.replaceAnnotation(sm.getAnnotation(sm.getSlot(gutATF.BOTTOM)));
                logger.debug("Added equality constraint between @Bottom and variable slot: " + variableSlot + " when visiting tree: \n" + tree);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Missing VarAnnot annotation:")) {
                    logger.fatal(primitiveType + " doesn't contain VarAnnot!", e);
                } else {
                    logger.fatal("Unknwon runtime exception!", e);
                }
                ErrorReporter.errorAbort("Error when getting primitive type", tree);
            }
            return null;
        }

/*        @Override
        protected void handleClassDeclarationBound(AnnotatedDeclaredType classType) {
            return;
        }

        @Override
        protected void handleInstantiationConstraint(AnnotatedDeclaredType adt, VariableSlot instantiationSlot) {
            return;
        }*/

    }
}

