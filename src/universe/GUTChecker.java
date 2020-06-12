package universe;

import universe.qual.Any;
import universe.qual.Bottom;
import universe.qual.Lost;
import universe.qual.Peer;
import universe.qual.Rep;
import universe.qual.Self;
import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstraintManager;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.framework.source.SupportedLintOptions;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;


/**
 * The main checker class for the Generic Universe Types checker.
 *
 * @author wmdietl
 */
@SupportedLintOptions({"checkOaM", "checkStrictPurity"})
public class GUTChecker extends BaseInferrableChecker {

    public static AnnotationMirror ANY, PEER, REP, LOST, SELF, BOTTOM, PURE;

    @Override
    public void initChecker() {
        super.initChecker();
        ANY = AnnotationBuilder.fromClass(getElementUtils(), Any.class);
        PEER = AnnotationBuilder.fromClass(getElementUtils(), Peer.class);
        REP = AnnotationBuilder.fromClass(getElementUtils(), Rep.class);
        LOST = AnnotationBuilder.fromClass(getElementUtils(), Lost.class);
        SELF = AnnotationBuilder.fromClass(getElementUtils(), Self.class);
        BOTTOM = AnnotationBuilder.fromClass(getElementUtils(), Bottom.class);
        PURE = AnnotationBuilder.fromClass(getElementUtils(), Pure.class);
    }

    @Override
    public BaseAnnotatedTypeFactory createRealTypeFactory() {
        return new GUTAnnotatedTypeFactory(this);
    }

    @Override
    public InferenceVisitor<?, ?> createVisitor(InferenceChecker checker,
            BaseAnnotatedTypeFactory factory, boolean infer) {
        return new GUTVisitor(this, checker, factory, infer);
    }

    @Override
    public boolean withCombineConstraints() {
        return true;
    }

    @Override
    public InferenceAnnotatedTypeFactory createInferenceATF(
            InferenceChecker inferenceChecker, InferrableChecker realChecker,
            BaseAnnotatedTypeFactory realTypeFactory, SlotManager slotManager,
            ConstraintManager constraintManager) {
        return new GUTInferenceAnnotatedTypeFactory(inferenceChecker,
                withCombineConstraints(), realTypeFactory, realChecker,
                slotManager, constraintManager);
    }

    @Override
    public boolean isInsertMainModOfLocalVar() {
        return true;
    }
}
