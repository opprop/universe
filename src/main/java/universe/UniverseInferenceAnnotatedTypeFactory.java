package universe;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.util.InferenceViewpointAdapter;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.treeannotator.LiteralTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;

import javax.lang.model.element.AnnotationMirror;

import static universe.UniverseAnnotationMirrorHolder.ANY;
import static universe.UniverseAnnotationMirrorHolder.BOTTOM;
import static universe.UniverseAnnotationMirrorHolder.PEER;
import static universe.UniverseAnnotationMirrorHolder.REP;
import static universe.UniverseAnnotationMirrorHolder.SELF;

public class UniverseInferenceAnnotatedTypeFactory extends InferenceAnnotatedTypeFactory {

    public UniverseInferenceAnnotatedTypeFactory(InferenceChecker inferenceChecker,
                                                 boolean withCombineConstraints,
                                                 BaseAnnotatedTypeFactory realTypeFactory,
                                                 InferrableChecker realChecker, SlotManager slotManager,
                                                 ConstraintManager constraintManager) {
        super(inferenceChecker, withCombineConstraints, realTypeFactory,
                realChecker, slotManager, constraintManager);

        addAliasedTypeAnnotation(org.jmlspecs.annotation.Peer.class, PEER);
        addAliasedTypeAnnotation(org.jmlspecs.annotation.Rep.class, REP);
        addAliasedTypeAnnotation(org.jmlspecs.annotation.Readonly.class, ANY);
        postInit();
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(new LiteralTreeAnnotator(this),
                new UniverseInferencePropagationTreeAnnotater(this),
                new InferenceTreeAnnotator(this, realChecker, realTypeFactory, variableAnnotator, slotManager));
    }

    @Override
    public VariableAnnotator createVariableAnnotator() {
        return new UniverseVariableAnnotator(this, realTypeFactory, realChecker, slotManager, constraintManager);
    }

    /**
     * The type of "this" is always "self".
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        UniverseTypeUtil.applyConstant(type, SELF);
        return type;
    }

    @Override
    protected InferenceViewpointAdapter createViewpointAdapter() {
        return new UniverseInferenceViewpointAdapter(this);
    }

    class UniverseVariableAnnotator extends VariableAnnotator {

        public UniverseVariableAnnotator(
                InferenceAnnotatedTypeFactory inferenceTypeFactory,
                AnnotatedTypeFactory realTypeFactory,
                InferrableChecker realChecker, SlotManager slotManager,
                ConstraintManager constraintManager) {
            super(inferenceTypeFactory, realTypeFactory, realChecker,
                    slotManager, constraintManager);
        }

        @Override
        protected Slot getOrCreateDeclBound(AnnotatedDeclaredType type) {
            // Since we don't care about class declaration annotations in universe type system.
            // The superclass method would always create a source variable slot if there doesn't
            // exist one for the class declaration.
            return getTopConstant();
        }

        @Override
        public void handleBinaryTree(AnnotatedTypeMirror atm, BinaryTree binaryTree) {
            if (atm.isAnnotatedInHierarchy(getVarAnnot())) {
                // Happens for binary trees whose atm is implicitly immutable and already handled by
                // PICOInferencePropagationTreeAnnotator
                return;
            }
            super.handleBinaryTree(atm, binaryTree);
        }
    }

    private class UniverseInferencePropagationTreeAnnotater extends PropagationTreeAnnotator {
        public UniverseInferencePropagationTreeAnnotater(AnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
            applyBottomIfImplicitlyBottom(type);// Usually there isn't existing annotation on binary trees, but to be safe, run it first
            return super.visitBinary(node, type);
        }

        @Override
        public Void visitTypeCast(TypeCastTree node, AnnotatedTypeMirror type) {
            applyBottomIfImplicitlyBottom(type);// Must run before calling super method to respect existing annotation
            if (type.isAnnotatedInHierarchy(ANY)) {
                // VarAnnot is guarenteed to not exist on type, because PropagationTreeAnnotator has the highest previledge
                // So VarAnnot hasn't been inserted to cast type yet.
                UniverseTypeUtil.applyConstant(type, type.getAnnotationInHierarchy(ANY));
            }
            return super.visitTypeCast(node, type);
        }

        /**Because TreeAnnotator runs before ImplicitsTypeAnnotator, implicitly immutable types are not guaranteed
         to always have immutable annotation. If this happens, we manually add immutable to type. */
        private void applyBottomIfImplicitlyBottom(AnnotatedTypeMirror type) {
            if (UniverseTypeUtil.isImplicitlyBottomType(type)) {
                UniverseTypeUtil.applyConstant(type, BOTTOM);
            }
        }
    }

    public static class UniverseInferenceViewpointAdapter extends InferenceViewpointAdapter {

        public UniverseInferenceViewpointAdapter(AnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        protected AnnotatedTypeMirror combineAnnotationWithType(AnnotationMirror receiverAnnotation,
                                                                AnnotatedTypeMirror declared) {
            if (UniverseTypeUtil.isImplicitlyBottomType(declared)) {
                return declared;
            }
            return super.combineAnnotationWithType(receiverAnnotation, declared);
        }
    }
}
