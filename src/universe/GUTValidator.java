package universe;

import checkers.inference.InferenceValidator;
import checkers.inference.InferenceVisitor;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.TypeElement;
import java.util.List;

import static universe.GUTChecker.BOTTOM;
import static universe.GUTChecker.LOST;
import static universe.GUTChecker.REP;

/**
 * This type validator ensures correct usage of ownership modifiers or generates
 * constraints to ensure well-formedness.
 */
public class GUTValidator extends InferenceValidator {

    public GUTValidator(BaseTypeChecker checker,
                                  InferenceVisitor<?, ?> visitor,
                                  // GUTAnnotatedTypeFactory atypeFactory) {
                                  // Is it correct? Only debugging.
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
        checkImplicitlyBottomTypeError(type, p);
        checkStaticRepError(type, p);
        checkConflictingPrimaryAnnos(type, p);
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
            if (infer) {
                ((GUTVisitor)visitor).doesNotContain(atpb.getLowerBound(), LOST, "uts.lost.in.bounds", tree);
                ((GUTVisitor)visitor).doesNotContain(atpb.getUpperBound(), LOST, "uts.lost.in.bounds", tree);
            } else {
                // Previously, here also checks two bounds are not TypeKind.NULL. What's the reason?
                if ((AnnotatedTypes.containsModifier(atpb.getUpperBound(), LOST)) ||
                                AnnotatedTypes.containsModifier(atpb.getLowerBound(), LOST)) {
                    checker.report(Result.failure("uts.lost.in.bounds",
                            atpb.toString(), type.toString()), tree);
                }
            }
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
        checkImplicitlyBottomTypeError(type, tree);
        return super.visitPrimitive(type, tree);
    }

    private void checkStaticRepError(AnnotatedTypeMirror type, Tree tree) {
        if (GUTTypeUtil.inStaticScope(visitor.getCurrentPath())) {
            if (infer) {
                ((GUTVisitor)visitor).doesNotContain(type, REP, "uts.static.rep.forbidden", tree);
            } else {
                if (AnnotatedTypes.containsModifier(type, REP)) {
                    checker.report(Result.failure("uts.static.rep.forbidden",
                            type.getAnnotations(), type.toString()), tree);
                }
            }
        }
    }

    private void checkImplicitlyBottomTypeError(AnnotatedTypeMirror type, Tree tree) {
        if (GUTTypeUtil.isImplicitlyBottomType(type)) {
            if (infer) {
                ((GUTVisitor)visitor).mainIs(type, BOTTOM, "type.invalid.annotations.on.use", tree);
            } else {
                if (!type.hasAnnotation(BOTTOM)) {
                    reportInvalidAnnotationsOnUse(type, tree);
                }
            }
        }
    }
}
