package universe;

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
import checkers.inference.util.InferenceViewpointAdapter;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import java.util.Arrays;
import java.util.List;

import static universe.GUTChecker.ANY;
import static universe.GUTChecker.BOTTOM;
import static universe.GUTChecker.PEER;
import static universe.GUTChecker.SELF;

public class GUTInferenceAnnotatedTypeFactory
        extends InferenceAnnotatedTypeFactory {

    public GUTInferenceAnnotatedTypeFactory(InferenceChecker inferenceChecker,
                                            boolean withCombineConstraints,
                                            BaseAnnotatedTypeFactory realTypeFactory,
                                            InferrableChecker realChecker, SlotManager slotManager,
                                            ConstraintManager constraintManager) {
        super(inferenceChecker, withCombineConstraints, realTypeFactory,
                realChecker, slotManager, constraintManager);
        postInit();
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(new ImplicitsTreeAnnotator(this),
                new GUTIInferencePropagationTreeAnnotater(this),
                new InferenceTreeAnnotator(this, realChecker, realTypeFactory, variableAnnotator, slotManager));
    }

    @Override
    public VariableAnnotator createVariableAnnotator() {
        return new GUTVariableAnnotator(this, realTypeFactory, realChecker, slotManager, constraintManager);
    }

    /**
     * The type of "this" is always "self".
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        GUTTypeUtil.applyConstant(type, SELF);
        return type;
    }

    @Override
    protected InferenceViewpointAdapter createViewpointAdapter() {
        return new GUTInferenceViewpointAdapter(this);
    }

    class GUTVariableAnnotator extends VariableAnnotator {

        public GUTVariableAnnotator(
                InferenceAnnotatedTypeFactory inferenceTypeFactory,
                AnnotatedTypeFactory realTypeFactory,
                InferrableChecker realChecker, SlotManager slotManager,
                ConstraintManager constraintManager) {
            super(inferenceTypeFactory, realTypeFactory, realChecker,
                    slotManager, constraintManager);
        }

        @Override
        protected void handleClassDeclarationBound(AnnotatedDeclaredType classType) {
            return;
        }

        @Override
        protected void handleInstantiationConstraint(AnnotatedDeclaredType adt, VariableSlot instantiationSlot, Tree tree) {
            return;
        }

        // Copied from super implementation
        @Override
        protected boolean handleWasRawDeclaredTypes(AnnotatedDeclaredType adt) {
            if (adt.wasRaw() && adt.getTypeArguments().size() != 0) {
                // the type arguments should be wildcards AND if I get the real type of "tree"
                // it corresponds to the declaration of adt.getUnderlyingType
                Element declarationEle = adt.getUnderlyingType().asElement();
                final AnnotatedDeclaredType declaration =
                        (AnnotatedDeclaredType) inferenceTypeFactory.getAnnotatedType(declarationEle);

                final List<AnnotatedTypeMirror> declarationTypeArgs = declaration.getTypeArguments();
                final List<AnnotatedTypeMirror> rawTypeArgs = adt.getTypeArguments();

                for (int i = 0; i < declarationTypeArgs.size(); i++) {

                    if (InferenceMain.isHackMode(rawTypeArgs.get(i).getKind() != TypeKind.WILDCARD)) {
                        return false;
                    }

                    final AnnotatedTypeMirror.AnnotatedWildcardType rawArg = (AnnotatedTypeMirror.AnnotatedWildcardType) rawTypeArgs.get(i);

                    // The only difference starts: instead of copying bounds of declared type variable to
                    // type argument wildcard bound, apply default @Mutable(of course equivalent VarAnnot)
                    // just like the behaviour in typechecking side.
                    // Previsouly, the behaviour is: "E extends @Readonly Object super @Bottom null".
                    // Type argument is "? extends Object", so it became "? extends @Readonly Object".
                    // This type argument then flows to local variable, and passed as actual method receiver.
                    // Since declared receiver is defaulted to @Mutable, it caused inference to give no solution.
                    rawArg.getExtendsBound().addMissingAnnotations(
                            Arrays.asList(GUTTypeUtil.createEquivalentVarAnnotOfRealQualifier(PEER)));
                    rawArg.getSuperBound().addMissingAnnotations(
                            Arrays.asList(GUTTypeUtil.createEquivalentVarAnnotOfRealQualifier(BOTTOM)));
                    // The only different ends
                }
                return true;
            } else {
                return false;
            }
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

    private class GUTIInferencePropagationTreeAnnotater extends PropagationTreeAnnotator {
        public GUTIInferencePropagationTreeAnnotater(AnnotatedTypeFactory atypeFactory) {
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
                GUTTypeUtil.applyConstant(type, type.getAnnotationInHierarchy(ANY));
            }
            return super.visitTypeCast(node, type);
        }

        /**Because TreeAnnotator runs before ImplicitsTypeAnnotator, implicitly immutable types are not guaranteed
         to always have immutable annotation. If this happens, we manually add immutable to type. */
        private void applyBottomIfImplicitlyBottom(AnnotatedTypeMirror type) {
            if (GUTTypeUtil.isImplicitlyBottomType(type)) {
                GUTTypeUtil.applyConstant(type, BOTTOM);
            }
        }
    }

    public static class GUTInferenceViewpointAdapter extends InferenceViewpointAdapter {

        public GUTInferenceViewpointAdapter(AnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        protected AnnotatedTypeMirror combineAnnotationWithType(AnnotationMirror receiverAnnotation,
                AnnotatedTypeMirror declared) {
            if (GUTTypeUtil.isImplicitlyBottomType(declared)) {
                return declared;
            }
            return super.combineAnnotationWithType(receiverAnnotation, declared);
        }
    }
}

