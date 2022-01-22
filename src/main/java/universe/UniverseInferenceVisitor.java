package universe;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
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
import org.checkerframework.framework.type.AnnotatedTypeFactory.ParameterizedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import static universe.UniverseAnnotationMirrorHolder.ANY;
import static universe.UniverseAnnotationMirrorHolder.BOTTOM;
import static universe.UniverseAnnotationMirrorHolder.LOST;
import static universe.UniverseAnnotationMirrorHolder.REP;
import static universe.UniverseAnnotationMirrorHolder.SELF;

/**
 * Type visitor to either enforce or infer the universe type rules.
 *
 * @author wmdietl
 */
public class UniverseInferenceVisitor extends InferenceVisitor<UniverseInferenceChecker, BaseAnnotatedTypeFactory> {

    private final boolean checkOaM;
    private final boolean checkStrictPurity;

    public UniverseInferenceVisitor(UniverseInferenceChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer) {
        super(checker, ichecker, factory, infer);

        checkOaM = checker.getLintOption("checkOaM", false);
        checkStrictPurity = checker.getLintOption("checkStrictPurity", false);
    }

    /**
     * The type validator to ensure correct usage of ownership modifiers.
     */
    @Override
    protected UniverseInferenceValidator createTypeValidator() {
        return new UniverseInferenceValidator(checker, this, atypeFactory);
    }

    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        return true;
    }

    /**
     * Ignore constructor receiver annotations as result type of the constructor is always SELF for universe.
     */
    @Override
    protected void checkConstructorInvocation(AnnotatedDeclaredType dt,
                                              AnnotatedExecutableType constructor, NewClassTree src) {}

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        if (infer) {
            // In infer mode, add preference constraint
            AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
            SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
            Slot slot = slotManager.getSlot(type);
            if (slot instanceof VariableSlot) {
                ConstraintManager constraintManager = InferenceMain.getInstance().getConstraintManager();
                ConstantSlot rep = slotManager.createConstantSlot(REP);
                constraintManager.addPreferenceConstraint((VariableSlot)slot, rep, 80);
            }
        }
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
            mainIs(constructorReturnType, SELF, "uts.constructor.not.self", node);
        } else {
            AnnotatedDeclaredType declaredReceiverType = executableType.getReceiverType();

            if (declaredReceiverType != null) {
                mainIs(declaredReceiverType, SELF, "uts.receiver.not.self", node);
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
            doesNotContain(parameterType, LOST, "uts.lost.parameter", node);
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
            effectiveIs(type, BOTTOM, "uts.new.ownership", node);
        } else {
            mainIsNoneOf(type, new AnnotationMirror[] { LOST, ANY, SELF, BOTTOM }, "uts.new.ownership", node);
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
            doesNotContain(parameterType, LOST, "uts.lost.parameter", node);
        }

        if (checkOaM) {
            ExpressionTree receiverTree = TreeUtils.getReceiverTree(node.getMethodSelect());
            if (receiverTree != null) {
                AnnotatedTypeMirror receiverType = atypeFactory.getAnnotatedType(receiverTree);

                if (receiverType != null) {
                    ExecutableElement methodElement = TreeUtils.elementFromUse(node);
                    if (!UniverseTypeUtil.isPure(methodElement)) {
                        mainIsNoneOf(receiverType, new AnnotationMirror[]{ LOST, ANY }, "oam.call.forbidden", node);
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
        doesNotContain(type, LOST, "uts.lost.lhs", node);

        if (checkOaM) {
            ExpressionTree receiverTree = TreeUtils.getReceiverTree(node.getVariable());
            if (receiverTree != null) {
                AnnotatedTypeMirror receiverType = atypeFactory.getAnnotatedType(receiverTree);

                if (receiverType != null) {
                        // TODO: do we need to treat "this" and "super" specially?
                        mainIsNoneOf(receiverType, new AnnotationMirror[]{ LOST, ANY }, "oam.assignment.forbidden", node);
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

        // I would say casting to any IS allowed.
        doesNotContain(castty, LOST, "uts.cast.type.warning", node);

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
            // This is only warning message, so even though enterred this line, it doesn't cause inference to exit.
            checker.reportWarning(node, "cast.unsafe", exprType.toString(true), castType.toString(true));
        }
    }

    private boolean isTypeCastSafe(AnnotatedTypeMirror castType, AnnotatedTypeMirror exprType, TypeCastTree node) {
        if (infer) {
            return isCompatibleCastInInfer(castType, exprType, node);
        } else {
            // Typechecking side standard implementation - warns if not upcasting
            return super.isTypeCastSafe(castType, exprType);
        }
    }

    private boolean isCompatibleCastInInfer(AnnotatedTypeMirror castType, AnnotatedTypeMirror exprType, TypeCastTree node) {
        // TODO: Can be shifted to CFI?
        // comparablecast
        final QualifierHierarchy qualHierarchy = InferenceMain.getInstance().getRealTypeFactory().getQualifierHierarchy();
        final SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
        final Slot castSlot = slotManager.getSlot(castType);
        final Slot exprSlot = slotManager.getSlot(exprType);

        if (castSlot instanceof ConstantSlot && exprSlot instanceof ConstantSlot) {
            ConstantSlot castCSSlot = (ConstantSlot) castSlot;
            ConstantSlot exprCSSlot = (ConstantSlot) exprSlot;
            // Special handling for case with two ConstantSlots: even though they may not be comparable,
            // but to infer more program, let this case fall back to "anycast" silently and continue
            // inference.
            return qualHierarchy.isSubtype(castCSSlot.getValue(), exprCSSlot.getValue())
                    || qualHierarchy.isSubtype(exprCSSlot.getValue(), castCSSlot.getValue());
        } else {
            // But if there is at least on Slot, inference guarantees that solutions don't include
            // incomparable casts.
            areComparable(castType, exprType, "uts.cast.type.error", node);
            return true;
        }
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

        if (!typeValidator.isValid(type, tree)) {
            if (!infer) {
                return false;
            }
        }
        // The initial purpose of always returning true in validateTypeOf in inference mode
        // might be that inference we want to generate constraints over all the ast location,
        // but not like in typechecking mode, if something is not valid, we abort checking the
        // remaining parts that are based on the invalid type. For example, in assignment, if
        // rhs is not valid, we don't check the validity of assignment. But in inference,
        // we always generate constraints on all places and let solver to decide if there is
        // solution or not. This might be the reason why we have a always true if statement and
        // validity check always returns true.
        return true;
    }

    @Override
    // Universe Type System does not need to check extends and implements
    protected void checkExtendsImplements(ClassTree classTree) {}
}
