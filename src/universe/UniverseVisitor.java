package universe;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeValidator;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;

/**
 * Type visitor to enforce the universe type rules.
 *
 * @author wmdietl
 */
public class UniverseVisitor extends BaseTypeVisitor<UniverseAnnotatedTypeFactory> {

    public final boolean checkOaM;
    public final boolean checkStrictPurity;
    public final boolean allowLost;
    public final boolean warn_staticpeer;

    public UniverseVisitor(BaseTypeChecker checker) {
        super(checker);

        this.allowLost = checker.getLintOption("allowLost", false);
        this.checkOaM = checker.getLintOption("checkOaM", false);
        this.checkStrictPurity = checker.getLintOption("checkStrictPurity", false);
        String warn = checker.getOption("warn", "");
        warn_staticpeer = warn.contains("staticpeer");
    }

    /**
     * The type validator to ensure correct usage of ownership modifiers.
     */
    @Override
    protected BaseTypeValidator createTypeValidator() {
        return new UniverseTypeValidator(checker, this, atypeFactory);
    }

    /**
     * The supermethod is currently OK, it only checks for a subtype relationship between the erased types.
     * Depends on what we use as default modifier. Super meth is ok for Any default, but not for peer.
     * TODO Is this really correct?
     */
    @Override
    public boolean isValidUse(AnnotatedDeclaredType elemType, AnnotatedDeclaredType use, Tree tree) {
        // return super.isValidUse(elemType, use, tree);
        return true;
    }

    /**
     * Ignore method receiver annotations.
     */
    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method,
            MethodInvocationTree node) {
        return;
    }

    /**
     * Universe type checking does not use receiver annotations, forbid them.
     */
    @Override
    public Void visitMethod(MethodTree node, Void p) {
        // System.out.println("MethodTree: " + node);

        if (node.getReceiverParameter() != null &&
                !node.getReceiverParameter().getModifiers().getAnnotations()
                        .isEmpty()) {
                    checker.report(Result.failure("uts.receiver.annotations.forbidden"),node);
    }

        return super.visitMethod(node, p);
    }

    /**
     * Ignore constructor receiver annotations.
     */
    @Override
    protected boolean checkConstructorInvocation(AnnotatedDeclaredType dt,
            AnnotatedExecutableType constructor, NewClassTree src) {
        return true;
    }

    /**
     * Validate a new object creation.
     *
     * @param node
     *            the new object creation.
     * @param p
     *            not used.
     */
    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        assert node != null;

        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> fromUse = atypeFactory.constructorFromUse(node);
        AnnotatedExecutableType constructor = fromUse.first;

        // Check for @Lost and @VPLost in combined parameter types.
        for (AnnotatedTypeMirror parameterType : constructor.getParameterTypes()) {
            if (AnnotatedTypes.containsModifier(parameterType, atypeFactory.LOST)) {
                checker.report(Result.failure("uts.lost.parameter"), node);
            } else if (AnnotatedTypes.containsModifier(parameterType, atypeFactory.VPLOST)) {
                checker.report(Result.failure("uts.vplost.parameter"), node);
            }
        }

        // Check for @Peer or @Rep as top-level modifier.
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        if (!(type.hasEffectiveAnnotation(atypeFactory.PEER) || type.hasEffectiveAnnotation(atypeFactory.REP))) {
            checker.report(Result.failure("uts.new.ownership"), node);
        }

        // Forbid @Rep in static context
        if (isInStaticContext() && type.hasEffectiveAnnotation(atypeFactory.REP)) {
                checker.report(Result.failure("uts.static.rep.forbidden"), node);
        }

        return super.visitNewClass(node, p);
    }

    private boolean isInStaticContext(){
	boolean isstatic = false;
        MethodTree meth = TreeUtils.enclosingMethod(getCurrentPath());
        if(meth != null){
            ExecutableElement methel = TreeUtils.elementFromDeclaration(meth);
            isstatic = ElementUtils.isStatic(methel);
        } else {
            VariableTree vartree = TreeUtils.enclosingVariable(getCurrentPath());
            if (vartree != null) {
                ModifiersTree mt = vartree.getModifiers();
                isstatic = mt.getFlags().contains(Modifier.STATIC);
            } else {
                BlockTree blcktree = TreeUtils.enclosingTopLevelBlock(getCurrentPath());
                if (blcktree != null) {
                    isstatic = blcktree.isStatic();
                }
            }
        }
        return isstatic;
    }

    /**
     * Validate a method invocation.
     *
     * @param node
     *            the method invocation.
     * @param p
     *            not used.
     */
    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        assert node != null;

        AnnotatedExecutableType method = atypeFactory.methodFromUse(node).first;

        // Check for @VPLost in combined parameter types.
        for (AnnotatedTypeMirror parameterType : method.getParameterTypes()) {
            if (AnnotatedTypes.containsModifier(parameterType, atypeFactory.LOST)) {
                checker.report(Result.failure("uts.lost.parameter"), node);
            } else if (AnnotatedTypes.containsModifier(parameterType, atypeFactory.VPLOST)) {
                checker.report(Result.failure("uts.vplost.parameter"), node);
            }
        }

        if (checkOaM) {
            ExpressionTree recvTree = TreeUtils.getReceiverTree(node.getMethodSelect());
            if (recvTree != null) {
                AnnotatedTypeMirror recvType = atypeFactory.getAnnotatedType(recvTree);
                    if (recvType.hasEffectiveAnnotation(atypeFactory.LOST) ||
                            recvType.hasEffectiveAnnotation(atypeFactory.VPLOST) ||
                                recvType.hasEffectiveAnnotation(atypeFactory.ANY)) {
                        ExecutableElement exelem = TreeUtils.elementFromUse(node);
                        java.util.List<? extends AnnotationMirror> anns = exelem.getAnnotationMirrors();

                        // purity
                        boolean hasPure = false;
                        for (AnnotationMirror an : anns) {
                            if (isPure(an.getAnnotationType())) {
                                hasPure = true;
                            }
                        }
                        if (anns.isEmpty() || !hasPure) {
                            checker.report(Result.failure("oam.call.forbidden"), node);
                        }
                    }
            }
        }

        return super.visitMethodInvocation(node, p);
    }

    private boolean isPure(DeclaredType anno) {
        // TODO: Is this the simplest way to do this?
        return anno.toString().equals(universe.qual.Pure.class.getName())
                || anno.toString().equals(org.jmlspecs.annotation.Pure.class.getName());
    }

    /**
     * Validate an assignment.
     *
     * @param node
     *            the assignment.
     * @param p
     *            not used.
     */
    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        assert node != null;

        // Check for @Lost and @VPLost in left hand side of assignment.
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getVariable());
        if (AnnotatedTypes.containsModifier(type, atypeFactory.LOST)) {
            checker.report(Result.failure("uts.lost.lhs"), node);
        } else if (AnnotatedTypes.containsModifier(type, atypeFactory.VPLOST)) {
            checker.report(Result.failure("uts.vplost.lhs"), node);
        }

        if (checkOaM) {
            ExpressionTree recvTree = TreeUtils.getReceiverTree(node.getVariable());
            if (recvTree != null) {
                AnnotatedTypeMirror recvType = atypeFactory.getAnnotatedType(recvTree);

                if (recvType != null) {
                    if (recvType.hasEffectiveAnnotation(atypeFactory.LOST) ||
                            recvType.hasEffectiveAnnotation(atypeFactory.VPLOST)
                                || recvType.hasEffectiveAnnotation(atypeFactory.ANY)) {
                        checker.report(Result.failure("oam.assignment.forbidden"), node);
                    }
                }
            }
        }

        if (checkStrictPurity && true /*TODO environment pure*/) {
            checker.report(Result.failure("purity.assignment.forbidden"), node);
        }

        return super.visitAssignment(node, p);
    }

    @Override
    public Void visitTypeCast(TypeCastTree node, Void p) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getType());

        if ((AnnotatedTypes.containsModifier(type, atypeFactory.LOST)
                || AnnotatedTypes.containsModifier(type, atypeFactory.VPLOST)
                        || AnnotatedTypes.containsModifier(type, atypeFactory.ANY))
                                && !UniverseChecker.isAnyDefault(type)) {
            checker.report(Result.warning("uts.cast.type.warning", type), node);
        }

        // The only part from the super call that we want.
        validateTypeOf(node.getType());

        // calling super would check for cast safety... is there a different way
        // to unset cast:unsafe?
        return null;
        // return super.visitTypeCast(node, p);
    }

    /**
     * This type validator ensures correct usage of ownership modifiers.
     * It must be run before implicits/defaults, because it should only
     * validate explicitly written qualifiers.
     * TODO The above statement is incorrect. Defaults are already applied.
     */
    private final class UniverseTypeValidator extends BaseTypeValidator {

        public UniverseTypeValidator(BaseTypeChecker checker,
                BaseTypeVisitor<?> visitor, AnnotatedTypeFactory atypeFactory) {
            super(checker, visitor, atypeFactory);
        }

        /**
         * Ensure that only one ownership modifier is used, that ownership
         * modifiers are correctly used in static contexts, and check for
         * explicit use of lost.
         */
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Tree p) {
            if (p.getKind() == Kind.VARIABLE) {
                if (isInStaticContext()) {
                    if (AnnotatedTypes.containsModifier(type, UniverseVisitor.this.atypeFactory.REP)) {
                        checker.report(Result.failure("uts.static.rep.forbidden",
                                type.getAnnotations(), type.toString()), p);
                    }
                    if (AnnotatedTypes.containsModifier(type, UniverseVisitor.this.atypeFactory.PEER)
                            && warn_staticpeer) {
                        // TODO: I would really like to only give the warning if
                        // the modifier was explicit.
                        checker.report(Result.warning("uts.static.peer.warning",
                                type.getAnnotations(), type.toString()), p);
                    }
                }
            }

            if (!allowLost &&
                    AnnotatedTypes.containsModifier(type, UniverseVisitor.this.atypeFactory.LOST)) {
                 checker.report(Result.failure("uts.explicit.lost.forbidden",
                       type.getAnnotations(), type.toString()), p);
            }

            if (type.getAnnotations().size() > 1) {
                reportError(type, p);
            }
            return super.visitDeclared(type, p);
        }

        @Override
        protected Void visitParameterizedType(AnnotatedDeclaredType type, ParameterizedTypeTree tree) {
            final TypeElement element =
                (TypeElement)type.getUnderlyingType().asElement();

            List<AnnotatedTypeParameterBounds> typevars = atypeFactory.typeVariablesFromUse(type, element);

            for (AnnotatedTypeParameterBounds atpb : typevars) {
                if ((atpb.getUpperBound().getKind() != TypeKind.NULL &&
                        AnnotatedTypes.containsModifier(atpb.getUpperBound(), UniverseVisitor.this.atypeFactory.VPLOST)) ||
                        (atpb.getLowerBound().getKind() != TypeKind.NULL &&
                        AnnotatedTypes.containsModifier(atpb.getLowerBound(), UniverseVisitor.this.atypeFactory.VPLOST))) {
                    checker.report(Result.failure("uts.vplost.in.bounds",
                            atpb.toString(), type.toString()), tree);
                }
            }
            return super.visitParameterizedType(type, tree);
        }

        /**
         * Forbid explicit annotations on primitive types.
         */
        @Override
        public Void visitPrimitive(AnnotatedPrimitiveType type, Tree p) {
            if (type.isAnnotatedInHierarchy(UniverseVisitor.this.atypeFactory.ANY)) {
                Set<AnnotationMirror> ann = type.getAnnotations();
                if (ann.size() > 1
                        || (ann.size() == 1 && !ann.contains(UniverseVisitor.this.atypeFactory.BOTTOM))) {
                    // the implicit default is BOTTOM, which cannot be used
                    // explicitly.
                    // if there are explicit annotations -> error.
                    reportError(type, p);
                }
            }
            return super.visitPrimitive(type, p);
        }

        /**
         * Each array dimension can only use at most one ownership modifier. The
         * check of the component type is done by the super method.
         */
        @Override
        public Void visitArray(AnnotatedArrayType type, Tree p) {
            if (type.getAnnotations().size() > 1) {
                // only one Universe modifier is allowed on the array
                reportError(type, p);
            }
            return super.visitArray(type, p);
        }
    }
}
