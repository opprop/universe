package universe;

import checkers.inference.InferenceValidator;
import checkers.inference.InferenceVisitor;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.TypeElement;
import java.util.List;

import static universe.UniverseAnnotationMirrorHolder.BOTTOM;
import static universe.UniverseAnnotationMirrorHolder.LOST;
import static universe.UniverseAnnotationMirrorHolder.REP;

/**
 * This type validator ensures correct usage of ownership modifiers or generates
 * constraints to ensure well-formedness.
 */
public class UniverseInferenceValidator extends InferenceValidator {

    public UniverseInferenceValidator(BaseTypeChecker checker,
                             InferenceVisitor<?, ?> visitor,
                             AnnotatedTypeFactory atypeFactory) {
        super(checker, visitor, atypeFactory);
    }

    /**
     * Ensure that only one ownership modifier is used, that ownership
     * modifiers are correctly used in static contexts, and check for
     * explicit use of lost.
     */
    @Override
    public Void visitDeclared(AnnotatedTypeMirror.AnnotatedDeclaredType type, Tree p) {
        if (checkTopLevelDeclaredOrPrimitiveType){
            checkImplicitlyBottomTypeError(type, p);
        }
        checkStaticRepError(type, p);
        // @Peer is allowed in static context

        // This will be handled at higher level
        // doesNotContain(type, LOST, "uts.explicit.lost.forbidden", p);
        return super.visitDeclared(type, p);
    }

    @Override
    protected Void visitParameterizedType(AnnotatedTypeMirror.AnnotatedDeclaredType type, ParameterizedTypeTree tree) {
        if (TreeUtils.isDiamondTree(tree)) {
            return null;
        }
        final TypeElement element = (TypeElement) type.getUnderlyingType().asElement();
        if (checker.shouldSkipUses(element)) {
            return null;
        }

        List<AnnotatedTypeParameterBounds> typeParamBounds = atypeFactory.typeVariablesFromUse(type, element);
        for (AnnotatedTypeParameterBounds atpb : typeParamBounds) {
            ((UniverseInferenceVisitor)visitor).doesNotContain(atpb.getLowerBound(), LOST, "uts.lost.in.bounds", tree);
            ((UniverseInferenceVisitor)visitor).doesNotContain(atpb.getUpperBound(), LOST, "uts.lost.in.bounds", tree);
        }

        return super.visitParameterizedType(type, tree);
    }

    @Override
    public Void visitArray(AnnotatedTypeMirror.AnnotatedArrayType type, Tree tree) {
        checkStaticRepError(type, tree);
        return super.visitArray(type, tree);
    }

    @Override
    public Void visitPrimitive(AnnotatedTypeMirror.AnnotatedPrimitiveType type, Tree tree) {
        if (checkTopLevelDeclaredOrPrimitiveType) {
            checkImplicitlyBottomTypeError(type, tree);
        }
        return super.visitPrimitive(type, tree);
    }

    private void checkStaticRepError(AnnotatedTypeMirror type, Tree tree) {
        if (UniverseTypeUtil.inStaticScope(visitor.getCurrentPath())) {
            ((UniverseInferenceVisitor)visitor).doesNotContain(type, REP, "uts.static.rep.forbidden", tree);
        }
    }

    private void checkImplicitlyBottomTypeError(AnnotatedTypeMirror type, Tree tree) {
        if (UniverseTypeUtil.isImplicitlyBottomType(type)) {
            ((UniverseInferenceVisitor)visitor).effectiveIs(type, BOTTOM, "type.invalid.annotations.on.use", tree);
        }
    }
}
