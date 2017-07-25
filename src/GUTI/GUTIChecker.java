package GUTI;

import GUT.GUTAnnotatedTypeFactory;
import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstraintManager;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.source.SupportedLintOptions;

import javax.annotation.processing.SupportedOptions;


/**
 * The main checker class for the Generic Universe Types checker.
 *
 * @author wmdietl
 */
// Keep these synchronized with the superclass.
@SupportedOptions( { "warn" } )
@SupportedLintOptions({"allowLost", "checkOaM", "checkStrictPurity"})
public class GUTIChecker extends BaseInferrableChecker {

    @Override
    public BaseAnnotatedTypeFactory createRealTypeFactory() {
        // Return the GUTIAnnotatedTypeFactory so that it can carry
        // GUTIVariableAnnotator
        return new GUTAnnotatedTypeFactory(this);
    }

    @Override
    public InferenceVisitor<?, ?> createVisitor(InferenceChecker checker,
            BaseAnnotatedTypeFactory factory, boolean infer) {
        return new GUTIVisitor(this, checker, factory, infer);
    }

    @Override
    public boolean withCombineConstraints() {
        return true;
    }

    /*    @Override
    public boolean withExistentialVariables() {
        return false;
    }*/

    @Override
    public InferenceAnnotatedTypeFactory createInferenceATF(
            InferenceChecker inferenceChecker, InferrableChecker realChecker,
            BaseAnnotatedTypeFactory realTypeFactory, SlotManager slotManager,
            ConstraintManager constraintManager) {
        return new GUTIAnnotatedTypeFactory(inferenceChecker,
        	withCombineConstraints(), realTypeFactory, realChecker,
                slotManager, constraintManager);
    }

    @Override
    public boolean isInsertMainModOfLocalVar() {
        return true;
    }


}