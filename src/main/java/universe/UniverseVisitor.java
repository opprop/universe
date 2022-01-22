package universe;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeFactory.ParameterizedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import static universe.UniverseAnnotationMirrorHolder.ANY;
import static universe.UniverseAnnotationMirrorHolder.BOTTOM;
import static universe.UniverseAnnotationMirrorHolder.LOST;
import static universe.UniverseAnnotationMirrorHolder.PEER;
import static universe.UniverseAnnotationMirrorHolder.REP;
import static universe.UniverseAnnotationMirrorHolder.SELF;

/**
 * Type visitor to either enforce or infer the universe type rules.
 *
 * @author wmdietl
 */
public class UniverseVisitor extends BaseTypeVisitor<UniverseAnnotatedTypeFactory> {

    private final boolean checkOaM;
    private final boolean checkStrictPurity;

    public UniverseVisitor(UniverseChecker checker, BaseAnnotatedTypeFactory factory) {
        super(checker, (UniverseAnnotatedTypeFactory) factory);

        checkOaM = checker.getLintOption("checkOaM", false);
        checkStrictPurity = checker.getLintOption("checkStrictPurity", false);
    }

    /**
     * The type validator to ensure correct usage of ownership modifiers.
     */
    @Override
    protected UniverseTypeValidator createTypeValidator() {
        return new UniverseTypeValidator(checker, this, atypeFactory);
    }

    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        return true;
    }

    /**
     * Ignore method receiver annotations.
     */
    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method, MethodInvocationTree node) {
        return;
    }

    /**
     * Ignore constructor receiver annotations.
     */
    @Override
    protected void checkConstructorInvocation(AnnotatedDeclaredType dt,
                                              AnnotatedExecutableType constructor, NewClassTree src) {}

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        return super.visitVariable(node, p);
    }

    /**
     * Universe does not use receiver annotations, forbid them.
     */
    @Override
    public Void visitMethod(MethodTree node, Void p) {
        AnnotatedExecutableType executableType = atypeFactory.getAnnotatedType(node);

        if (TreeUtils.isConstructor(node)) {
            AnnotatedDeclaredType constructorReturnType = (AnnotatedDeclaredType) executableType.getReturnType();
            if (!constructorReturnType.hasAnnotation(SELF)) {
                checker.reportError(node, "uts.constructor.not.self");
            }
        } else {
            AnnotatedDeclaredType declaredReceiverType = executableType.getReceiverType();
            if (declaredReceiverType != null) {
                if (!declaredReceiverType.hasAnnotation(SELF)) {
                    checker.reportError(node, "uts.receiver.not.self");
                }
            }
        }
        return super.visitMethod(node, p);
    }

    @Override
    protected OverrideChecker createOverrideChecker(
            Tree overriderTree, AnnotatedExecutableType overrider,AnnotatedTypeMirror overridingType,
            AnnotatedTypeMirror overridingReturnType, AnnotatedExecutableType overridden, AnnotatedDeclaredType overriddenType, AnnotatedTypeMirror overriddenReturnType) {

        return new OverrideChecker(
                overriderTree,
                overrider,
                overridingType,
                overridingReturnType,
                overridden,
                overriddenType,
                overriddenReturnType) {
            @Override
            protected boolean checkReceiverOverride() {
                return true;
            }
        };
    }

    /**
     * There is no need to issue a warning if the result type of the constructor is not top in Universe.
     */
    @Override
    protected void checkConstructorResult(
            AnnotatedTypeMirror.AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {}

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
        ParameterizedExecutableType fromUse = atypeFactory.constructorFromUse(node);
        AnnotatedExecutableType constructor = fromUse.executableType;

        // Check for @Lost in combined parameter types deeply.
        for (AnnotatedTypeMirror parameterType : constructor.getParameterTypes()) {
            if (AnnotatedTypes.containsModifier(parameterType, LOST)) {
                checker.reportError(node, "uts.lost.parameter");
            }
        }

        checkNewInstanceCreation(node);

        // There used to be code to check static rep error. But that's already moved to Validator.
        return super.visitNewClass(node, p);
    }

    @Override
    public Void visitNewArray(NewArrayTree node, Void p) {
        checkNewInstanceCreation(node);
        return super.visitNewArray(node, p);
    }

    private void checkNewInstanceCreation(Tree node) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        // Check for @Peer or @Rep as top-level modifier.
        // TODO I would say here by top-level, it's really main modifier instead of upper bounds of
        // type variables, as there is no "new T()" to create a new instance.
        if (UniverseTypeUtil.isImplicitlyBottomType(type)) {
            if (!type.hasAnnotation(BOTTOM)) {
                checker.reportError(node, "uts.new.ownership");
            }
        } else {
            if (!(type.hasAnnotation(PEER) || type.hasAnnotation(REP))) {
                checker.reportError(node, "uts.new.ownership");
            }
        }
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

        AnnotatedExecutableType methodType = atypeFactory.methodFromUse(node).executableType;
        // Check for @Lost in combined parameter types deeply.
        for (AnnotatedTypeMirror parameterType : methodType.getParameterTypes()) {
            if (AnnotatedTypes.containsModifier(parameterType, LOST)) {
                checker.reportError(node, "uts.lost.parameter");
            }
        }

        if (checkOaM) {
            ExpressionTree receiverTree = TreeUtils.getReceiverTree(node.getMethodSelect());
            if (receiverTree != null) {
                AnnotatedTypeMirror receiverType = atypeFactory.getAnnotatedType(receiverTree);

                if (receiverType != null) {
                    ExecutableElement methodElement = TreeUtils.elementFromUse(node);
                    if (!UniverseTypeUtil.isPure(methodElement)) {
                        // I would say this non-lost and non-any restriction is really for declared
                        // types, not for type variables. As type variables can't have methods to invoke.
                        if (receiverType.hasAnnotation(LOST) || receiverType.hasAnnotation(ANY)) {
                            checker.reportError(node, "oam.call.forbidden");
                        }
                    }
                }
            }
        }

        return super.visitMethodInvocation(node, p);
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
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getVariable());
        // Check for @Lost in left hand side of assignment deeply.
        if (AnnotatedTypes.containsModifier(type, LOST)) {
            checker.reportError(node, "uts.lost.lhs");
        }

        if (checkOaM) {
            ExpressionTree receiverTree = TreeUtils.getReceiverTree(node.getVariable());
            if (receiverTree != null) {
                AnnotatedTypeMirror receiverType = atypeFactory.getAnnotatedType(receiverTree);

                if (receiverType != null) {
                    // Still, I think receiver can still only be declared types, so effectiveAnnotation
                    // is not needed.
                    if (receiverType.hasAnnotation(LOST) || receiverType.hasAnnotation(ANY)) {
                        checker.reportError(node, "oam.assignment.forbidden");
                    }
                }
            }
        }

        if (checkStrictPurity && true /* TODO environment pure */) {
            checker.reportError(node, "purity.assignment.forbidden");
        }

        return super.visitAssignment(node, p);
    }

    @Override
    public Void visitTypeCast(TypeCastTree node, Void p) {
        AnnotatedTypeMirror castty = atypeFactory.getAnnotatedType(node.getType());

        if ((AnnotatedTypes.containsModifier(castty, LOST))) {
            checker.reportWarning(node, "uts.cast.type.warning", castty);
        }

        return super.visitTypeCast(node, p);
    }

    protected void checkTypecastSafety(TypeCastTree node, Void p) {
        if (!checker.getLintOption("cast:unsafe", true)) {
            return;
        }
        AnnotatedTypeMirror castType = atypeFactory.getAnnotatedType(node);
        AnnotatedTypeMirror exprType = atypeFactory.getAnnotatedType(node.getExpression());

        // We cannot do a simple test of casting, as isSubtypeOf requires
        // the input types to be subtypes according to Java
        if (!isTypeCastSafe(castType, exprType, node)) {
            checker.reportWarning(node, "cast.unsafe", exprType.toString(true), castType.toString(true));
        }
    }

    private boolean isTypeCastSafe(AnnotatedTypeMirror castType, AnnotatedTypeMirror exprType, TypeCastTree node) {
        // Typechecking side standard implementation - warns if not upcasting
        return super.isTypeCastSafe(castType, exprType);
    }

    @Override
    public boolean validateTypeOf(Tree tree) {
        AnnotatedTypeMirror type;
        // It's quite annoying that there is no TypeTree
        switch (tree.getKind()) {
            case PRIMITIVE_TYPE:
            case PARAMETERIZED_TYPE:
            case TYPE_PARAMETER:
            case ARRAY_TYPE:
            case UNBOUNDED_WILDCARD:
            case EXTENDS_WILDCARD:
            case SUPER_WILDCARD:
            case ANNOTATED_TYPE:
                type = atypeFactory.getAnnotatedTypeFromTypeTree(tree);
                break;
            case METHOD:
                type = atypeFactory.getMethodReturnType((MethodTree) tree);
                if (type == null ||
                        type.getKind() == TypeKind.VOID) {
                    // Nothing to do for void methods.
                    // Note that for a constructor the AnnotatedExecutableType does
                    // not use void as return type.
                    return true;
                }
                break;
            default:
                type = atypeFactory.getAnnotatedType(tree);
        }

        return validateType(tree, type);
    }

    // TODO This might not be correct for infer mode. Maybe returning as it is
    @Override
    public boolean validateType(Tree tree, AnnotatedTypeMirror type) {

        return typeValidator.isValid(type, tree);
        // The initial purpose of always returning true in validateTypeOf in inference mode
        // might be that inference we want to generate constraints over all the ast location,
        // but not like in typechecking mode, if something is not valid, we abort checking the
        // remaining parts that are based on the invalid type. For example, in assignment, if
        // rhs is not valid, we don't check the validity of assignment. But in inference,
        // we always generate constraints on all places and let solver to decide if there is
        // solution or not. This might be the reason why we have a always true if statement and
        // validity check always returns true.
    }

    @Override
    // Universe Type System does not need to check extends and implements
    protected void checkExtendsImplements(ClassTree classTree) {}
}
