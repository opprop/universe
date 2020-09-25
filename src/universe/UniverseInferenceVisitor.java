package universe;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
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

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceValidator;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;

/**
 * Type visitor to either enforce or infer the universe type rules.
 *
 * @author wmdietl
 */
public class UniverseInferenceVisitor extends InferenceVisitor<UniverseInferenceChecker, BaseAnnotatedTypeFactory> {

    private final boolean checkOaM;
    private final boolean checkStrictPurity;
    private final boolean allowLost;
    private final boolean warn_staticpeer;

    private final UniverseAnnotatedTypeFactory univATF;

    /*
     * We continue to use BaseTypeChecker as first parameter type, because this
     * class is instantiated by both the UniverseInferenceChecker and the InferenceChecker.
     */
    public UniverseInferenceVisitor(UniverseInferenceChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer) {
        super(checker, ichecker, factory, infer);

        this.allowLost = checker.getLintOption("allowLost", false);
        this.checkOaM = checker.getLintOption("checkOaM", false);
        this.checkStrictPurity = checker.getLintOption("checkStrictPurity", false);

        String warn = checker.getOption("warn", "");
        warn_staticpeer = warn.contains("staticpeer");

        this.univATF = (UniverseAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
        // System.out.println("univATF in constructor is: " + this.univATF);

    }

    /**
     * The type validator to ensure correct usage of ownership modifiers.
     */
    @Override
    protected UniverseInferenceValidator createTypeValidator() {
        // System.out.println("UniverseInferenceValidator constructor is called!");
        //System.out.println("univATF is: " + this.univATF);
        // Overriding version of createTypeValidator() needs a parameter which is provided after the call to this method according to super constructor.
        // But univATF is not set, so UniverseInferenceValidator's atypeFactory field is null, and causes nullpointer exception.
        // Fix is: get realtypefactory as soon as possible, and don't wait till univATF is provided.

        // I think the following line is incorrect. We need
        // InferenceAnnotatedTypeFactory
        // rather than UniverseAnnotatedTypeFactory.
        /*
        UniverseAnnotatedTypeFactory univATFParameter = (UniverseAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
        return new UniverseInferenceValidator(checker, this, univATFParameter);*/
        return new UniverseInferenceValidator(checker, this, this.atypeFactory);
    }

    // TODO: find a nicer way to set preferences
    @Override
    public Void visitVariable(VariableTree node, Void p) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        if (node.getInitializer() != null) {
            doesNotContain(type, univATF.LOST, "uts.lost.lhs", node);
            doesNotContain(type, univATF.VPLOST, "uts.vplost.lhs", node);
        }
        ConstraintManager cm = InferenceMain.getInstance().getConstraintManager();
        SlotManager sm = InferenceMain.getInstance().getSlotManager();
        Slot s = sm.getVariableSlot(type);
        if (s instanceof VariableSlot) {
            VariableSlot vs = (VariableSlot) s;
            ConstantSlot rep = InferenceMain.getInstance().getSlotManager().createConstantSlot(univATF.REP);
            cm.addPreferenceConstraint(vs, rep, 80);
        }
        return super.visitVariable(node, p);
    }

    /**
     * Universe typechecking does not use receiver annotations, forbid them.
     */
    @Override
    public Void visitMethod(MethodTree node, Void p) {
        // System.out.println("MethodTree: " + node);

        /* TODO:
        if (!node.getReceiverAnnotations().isEmpty()) {
            checker.report(Result.failure("uts.receiver.annotations.forbidden"), node);
        }
        */

        return super.visitMethod(node, p);
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
        // System.out.println("UniverseInferenceVisitor: visitNewClass is called!");
        assert node != null;
        //Using UniverseAnnotatedTypeFactory is OK! It can still get the VarAnnotations, and add
        //constraints on them
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> fromUse = atypeFactory.constructorFromUse(node);
        AnnotatedExecutableType constructor = fromUse.first;

        // Check for @Lost and @VPLost in combined parameter types.
        for (AnnotatedTypeMirror parameterType : constructor.getParameterTypes()) {
            doesNotContain(parameterType, univATF.LOST, "uts.lost.parameter", node);
            doesNotContain(parameterType, univATF.VPLOST, "uts.vplost.parameter", node);
        }

        // Check for @Peer or @Rep as top-level modifier.
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        mainIsNoneOf(type, new AnnotationMirror[] { univATF.LOST, univATF.VPLOST,
                univATF.ANY }, "uts.new.ownership", node);

        // Forbid rep in static context
        if (isContextStatic(atypeFactory.getPath(node))) {
            doesNotContain(type, univATF.REP, "uts.static.rep.forbidden", node);
        }

        return super.visitNewClass(node, p);
    }

    private static boolean isContextStatic(TreePath path) {
        // TODO: test this. fields, methods, local vars, ... Checker.isValid?
        MethodTree encm = TreeUtils.enclosingMethod(path);
        if (encm!=null) {
            // we are within a method
            return ElementUtils.isStatic(TreeUtils.elementFromDeclaration(encm));
        } else {
            VariableTree encv = TreeUtils.enclosingVariable(path);
            if (encv!=null) {
                // we are within a field initializer
                return ElementUtils.isStatic(TreeUtils.elementFromDeclaration(encv));
            } else {
                // is it a static initializer?
                BlockTree encb = TreeUtils.enclosingTopLevelBlock(path);
                if (encb!=null) {
                    return encb.isStatic();
                } else {
                    // We are somewhere in a class declaration already, e.g. the extends clause
                    // System.out.println("UniverseInferenceVisitor::isContextStatic: lost in the tree, help! Path leaf: " + path.getLeaf());
                    return false;
                }
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
        assert node != null;
        AnnotatedExecutableType method = atypeFactory.methodFromUse(node).first;
        // Check for @Lost and @VPLost in combined parameter types.
        for (AnnotatedTypeMirror parameterType : method.getParameterTypes()) {
            doesNotContain(parameterType, univATF.LOST, "uts.lost.parameter", node);
            doesNotContain(parameterType, univATF.VPLOST, "uts.lost.parameter", node);
        }

        if (checkOaM) {
            ExpressionTree recvTree = TreeUtils.getReceiverTree(node.getMethodSelect());
            if (recvTree != null) {
                AnnotatedTypeMirror recvType = atypeFactory.getAnnotatedType(recvTree);

                if (recvType != null) {
                    ExecutableElement exelem = TreeUtils.elementFromUse(node);
                    if (!hasPure(exelem)) {
                        mainIsNoneOf(recvType, new AnnotationMirror[] {univATF.LOST, univATF.ANY},
                                "oam.call.forbidden", node);
                    }
                }
            }
        }

        // TODO purity

        return super.visitMethodInvocation(node, p);
    }

    private static boolean hasPure(ExecutableElement meth) {
        boolean hasPure = false;
        java.util.List<? extends AnnotationMirror> anns = meth.getAnnotationMirrors();
        for (AnnotationMirror an : anns) {
            if (isPure(an.getAnnotationType())) {
                hasPure = true;
                break;
            }
        }
        return hasPure;
    }

    private static boolean isPure(DeclaredType anno) {
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
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getVariable());
        // Check for @Lost and @VPLost in left hand side of assignment.
        doesNotContain(type, univATF.LOST, "uts.lost.lhs", node);
        doesNotContain(type, univATF.VPLOST, "uts.vplost.lhs", node);

        if (checkOaM) {
            ExpressionTree recvTree = TreeUtils.getReceiverTree(node.getVariable());
            if (recvTree != null) {
                AnnotatedTypeMirror recvType = atypeFactory.getAnnotatedType(recvTree);

                if (recvType != null) {
                    // TODO: do we need to treat "this" and "super" specially?
                    mainIsNoneOf(recvType,
                            new AnnotationMirror[] { univATF.LOST, univATF.VPLOST, univATF.ANY },
                            "oam.assignment.forbidden", node);
                }
            }
        }

        if (checkStrictPurity && true /* TODO environment pure */) {
            checker.report(Result.failure("purity.assignment.forbidden"), node);
        }

        return super.visitAssignment(node, p);
    }

    @Override
    public Void visitTypeCast(TypeCastTree node, Void p) {
        AnnotatedTypeMirror castty = atypeFactory.getAnnotatedType(node.getType());
        AnnotatedTypeMirror exprty = atypeFactory.getAnnotatedType(node.getExpression());

        /* TODO: this warning will only be useful in type checking mode.
         * Would it be helpful to somehow support such warnings for type inference, e.g.
         * by reducing weighting?
         */
        if ((AnnotatedTypes.containsModifier(castty, univATF.LOST) ||
                AnnotatedTypes.containsModifier(castty, univATF.VPLOST) ||
                    AnnotatedTypes.containsModifier(castty, univATF.ANY)) &&
                        !UniverseChecker.isAnyDefault(castty)) {
            checker.report(Result.warning("uts.cast.type.warning", castty), node);
            // checker.getProcessingEnvironment().getMessager().printMessage(javax.tools.Diagnostic.Kind.WARNING,
            // "Casting to " + type + " is not recommended.");
        }

        areComparable(castty, exprty, "uts.cast.type.error", node);

        // The only part from the super call that we want.
        validateTypeOf(node.getType());

        // calling super would check for cast safety... is there a different way
        // to unset cast:unsafe?
        return null;
        // return super.visitTypeCast(node, p);
    }

    @Override
    public Void visitInstanceOf(InstanceOfTree node, Void p) {
        AnnotatedTypeMirror ioty = atypeFactory.getAnnotatedType(node.getType());
        AnnotatedTypeMirror exprty = atypeFactory.getAnnotatedType(node.getExpression());

        // TODO: See comment at casts above.
        if ((AnnotatedTypes.containsModifier(ioty, univATF.LOST) ||
                AnnotatedTypes.containsModifier(ioty, univATF.VPLOST) ||
                    AnnotatedTypes.containsModifier(ioty, univATF.ANY)) &&
                        !UniverseChecker.isAnyDefault(ioty)) {
            checker.report(Result.warning("uts.instanceof.type.warning", ioty), node);
            // checker.getProcessingEnvironment().getMessager().printMessage(javax.tools.Diagnostic.Kind.WARNING,
            // "Casting to " + type + " is not recommended.");
        }

        areComparable(ioty, exprty, "uts.instanceof.type.error", node);

        return super.visitInstanceOf(node, p);
    }

    /* TODO: what do we want to enforce for strings? They don't really matter anyway.
    @Override
    public Void visitBinary(BinaryTree node, Void p) {
        if (node.getKind() == Kind.PLUS || node.getKind() == Kind.PLUS_ASSIGNMENT) {
            // TODO: only for Strings, but adding it for primitives shouldn't be a problem

            AnnotatedTypeMirror ltype = atypeFactory.getAnnotatedType(node.getLeftOperand());
            AnnotatedTypeMirror rtype = atypeFactory.getAnnotatedType(node.getRightOperand());

            areEqual(ltype, rtype, "uts.append.type.error", node);
        }
        return super.visitBinary(node, p);
    }
    */

    /* TODO Do I really need this?
    @Override
    protected void checkTypeArguments(Tree t,
            java.util.List<? extends AnnotatedTypeVariable> typevars,
            java.util.List<? extends AnnotatedTypeMirror> typeargs,
            java.util.List<? extends Tree> typeargTrees) {
        super.checkTypeArguments(t, typevars, typeargs, typeargTrees);
    }
    */

    /**
     * This type validator ensures correct usage of ownership modifiers.
     */
    private final class UniverseInferenceValidator extends InferenceValidator {
        public UniverseInferenceValidator(BaseTypeChecker checker,
                InferenceVisitor<?, ?> visitor,
                // UniverseAnnotatedTypeFactory atypeFactory) {
                // Is it correct? Only debugging.
                AnnotatedTypeFactory atypeFactory) {

            super(checker, visitor, atypeFactory);
            // System.out.println("atypeFactory in constructor: " +
            // atypeFactory);
        }

        /**
         * Ensure that only one ownership modifier is used, that ownership
         * modifiers are correctly used in static contexts, and check for
         * explicit use of lost.
         */
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Tree p) {
            // System.out.println("==========visitDeclared in UniverseInferenceValidator is called!");
            // System.out.println(
            // "UniverseInferenceValidator: visitDeclared is called!");
            // ModifiersTree mt = ((VariableTree) p).getModifiers();
            // if (mt.getFlags().contains(Modifier.STATIC)) {
            // System.out.println("\n========p is: " + p);
            //System.out.println("p is null: " + p == null);
            // System.out.println("atypeFactory is null: ");
            // System.out.println(atypeFactory == null);
            // System.out.println("----------------atypeFactory.getPath(p) is: "
            // + atypeFactory.getPath(p));
            if (UniverseInferenceVisitor.isContextStatic(atypeFactory.getPath(p))) {
                doesNotContain(type, univATF.REP, "uts.static.rep.forbidden", p);

                if (warn_staticpeer) {
                    // TODO: I would really like to only give the warning if
                    // the modifier was explicit.
                    // TODO: Only want a warning, not an error.
                    doesNotContain(type, univATF.PEER, "uts.static.peer.warning", p);
                }
            }

            if (!allowLost) {
                doesNotContain(type, univATF.LOST, "uts.explicit.lost.forbidden", p);
                doesNotContain(type, univATF.VPLOST, "uts.explicit.lost.forbidden", p);
            }
            // Me: Is it allowed to have multiple universe modifiers? What does
            // it mean?
            if (type.getAnnotations().size() > 2) {
                System.out.println(
                        "UniverseVisitor$UniverseInferenceValidator: Don't know if it's correct: type is: "
                                + type);
                reportError(type, p);
            }
            // System.out.println("super.visitDeclared for: " + type);
            // System.out.println("Reached super call!");
            return super.visitDeclared(type, p);
        }

        @Override
        protected Void visitParameterizedType(AnnotatedDeclaredType type, ParameterizedTypeTree tree) {
            // For debuggin purpose, move the last line to here
            // return super.visitParameterizedType(type, tree);
            // System.out.println(
            // "UniverseInferenceVaildator: visitParameterizedType is called!");

            final TypeElement element = (TypeElement) type.getUnderlyingType().asElement();
            List<AnnotatedTypeParameterBounds> typeParamBounds = atypeFactory.typeVariablesFromUse(type, element);
            for (AnnotatedTypeParameterBounds atpb : typeParamBounds) {
                doesNotContain(atpb.getLowerBound(), univATF.LOST, "uts.lost.in.bounds", tree);
                doesNotContain(atpb.getUpperBound(), univATF.LOST, "uts.lost.in.bounds", tree);
                doesNotContain(atpb.getLowerBound(), univATF.VPLOST, "uts.vplost.in.bounds", tree);
                doesNotContain(atpb.getUpperBound(), univATF.VPLOST, "uts.vplost.in.bounds", tree);
            }
            for(AnnotatedTypeMirror atm: type.getTypeArguments()){
                doesNotContain(atm, univATF.LOST,"uts.lost.in.type.arguments",tree);
                doesNotContain(atm, univATF.VPLOST,"uts.lost.in.type.arguments",tree);
            }
            // For debugging purpose, move the following line to first. To see if super not overridden
            // method works or not.
            // Overriding a method doesn't necessarily mean that it has not
            // relationship with super method. If overriding version constains
            // super.method, then we need to look at the super method's
            // source code too!
            return super.visitParameterizedType(type, tree);
            // return null;
        }

        /**
         * Forbid explicit annotations on primitive types.
         */
        @Override
        public Void visitPrimitive(AnnotatedPrimitiveType type, Tree p) {
            if (type.isAnnotatedInHierarchy(univATF.ANY)) {
                Set<AnnotationMirror> ann = type.getAnnotations();
                if (ann.size() > 1
                        || (ann.size() == 1 && !ann.contains(univATF.BOTTOM))) {
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
        /*
        @Override
        protected void checkValidUse(AnnotatedDeclaredType type, Tree tree) {
            return;// Doesn't check valid use of type in Generic Universe type system
        }*/
    }
}
