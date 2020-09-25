package universe;

import javax.annotation.processing.SupportedOptions;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.source.SupportedLintOptions;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstraintManager;

/**
 * The main checker class for the Generic Universe Types checker.
 *
 * @author wmdietl
 */
// Keep these synchronized with the superclass.
@SupportedOptions( { "warn" } )
@SupportedLintOptions({"allowLost", "checkOaM", "checkStrictPurity"})
public class UniverseInferenceChecker extends BaseInferrableChecker {

    @Override
    public BaseAnnotatedTypeFactory createRealTypeFactory() {
        // Return the UniverseInferenceAnnotatedTypeFactory so that it can carry
        // UniverseInferenceVariableAnnotator
        return new UniverseInferenceAnnotatedTypeFactory(this);
    }

    @Override
    public InferenceVisitor<?, ?> createVisitor(InferenceChecker checker,
            BaseAnnotatedTypeFactory factory, boolean infer) {
        return new UniverseInferenceVisitor(this, checker, factory, infer);
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
        return new UniverseInferenceAnnotatedTypeFactory(inferenceChecker,
        	withCombineConstraints(), realTypeFactory, realChecker,
                slotManager, constraintManager);
    }

}
