package GUTI;

import GUT.GUTChecker;
import GUT.qual.Any;
import GUT.qual.Bottom;
import GUT.qual.Lost;
import GUT.qual.Peer;
import GUT.qual.Rep;
import GUT.qual.Self;
import GUT.qual.VPLost;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceValidator;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;

/**
 * Type visitor to either enforce or infer the gut type rules.
 *
 * @author wmdietl
 */
public class GUTIVisitor extends InferenceVisitor<GUTIChecker, BaseAnnotatedTypeFactory> {

    public static final Log logger = LogFactory.getLog(GUTIVisitor.class.getSimpleName());

    public AnnotationMirror ANY, PEER, REP, LOST, VPLOST, SELF, BOTTOM, PURE;

    private final boolean checkOaM;
    private final boolean checkStrictPurity;
    private final boolean allowLost;
    private final boolean warn_staticpeer;

    /*
     * We continue to use BaseTypeChecker as first parameter type, because this
     * class is instantiated by both the GUTIChecker and the InferenceChecker.
     */
    public GUTIVisitor(GUTIChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer) {
        super(checker, ichecker, factory, infer);

        this.allowLost = checker.getLintOption("allowLost", false);
        this.checkOaM = checker.getLintOption("checkOaM", false);
        this.checkStrictPurity = checker.getLintOption("checkStrictPurity", false);

        String warn = checker.getOption("warn", "");
        warn_staticpeer = warn.contains("staticpeer");
        // System.out.println("gutATF in constructor is: " + this.gutATF);

        ANY = AnnotationUtils.fromClass(elements, Any.class);
        PEER = AnnotationUtils.fromClass(elements, Peer.class);
        REP = AnnotationUtils.fromClass(elements, Rep.class);
        LOST = AnnotationUtils.fromClass(elements, Lost.class);
        VPLOST = AnnotationUtils.fromClass(elements, VPLost.class);
        SELF = AnnotationUtils.fromClass(elements, Self.class);
        BOTTOM = AnnotationUtils.fromClass(elements, Bottom.class);
        PURE = AnnotationUtils.fromClass(elements, Pure.class);
    }

    /**
     * The type validator to ensure correct usage of ownership modifiers.
     */
    @Override
    protected GUTIInferenceValidator createTypeValidator() {
        // System.out.println("GUTIInferenceValidator constructor is called!");
        //System.out.println("gutATF is: " + this.gutATF);
        // Overriding version of createTypeValidator() needs a parameter which is provided after the call to this method according to super constructor.
        // But gutATF is not set, so GUTIInferenceValidator's atypeFactory field is null, and causes nullpointer exception.
        // Fix is: get realtypefactory as soon as possible, and don't wait till gutATF is provided.

        // I think the following line is incorrect. We need
        // InferenceAnnotatedTypeFactory
        // rather than GUTAnnotatedTypeFactory.
        /*
        GUTAnnotatedTypeFactory gutATFParameter = (GUTAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
        return new GUTIInferenceValidator(checker, this, gutATFParameter);*/
        //return new GUTIInferenceValidator(checker, this, atypeFactory);
        return new GUTIInferenceValidator(checker, this, atypeFactory);
    }

    // TODO: find a nicer way to set preferences
    @Override
    public Void visitVariable(VariableTree node, Void p) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        if (node.getInitializer() != null) {
            doesNotContain(type, LOST, "uts.lost.lhs", node);
            doesNotContain(type, VPLOST, "uts.vplost.lhs", node);
        }
        Element element = TreeUtils.elementFromDeclaration(node);
        if (element.getKind() == ElementKind.PARAMETER) {
            doesNotContain(type, LOST, "uts.lost.lhs", node);
            doesNotContain(type, VPLOST, "uts.vplost.lhs", node);
            if (!element.getSimpleName().contentEquals("this")) {
                // Generate inequality constraint if this is a normal parameter tree(non receiver tree)
                doesNotContain(type, SELF, "uts.vplost.lhs", node);
            }
        }
        if (element.getKind() == ElementKind.FIELD) {
            doesNotContain(type, LOST, "uts.lost.lhs", node);
            doesNotContain(type, VPLOST, "uts.vplost.lhs", node);
            doesNotContain(type, SELF, "uts.vplost.lhs", node);
        }
        if (node.getType().getKind() != Kind.PRIMITIVE_TYPE) {
            mainIsNot(type, BOTTOM, "uts.vplost.lhs", node);
        }

///*        InferenceMain inferenceMain = new InferenceMain();
//        inferenceMain.recordInferenceCheckerInstance((InferenceChecker) checker);*/
        ConstraintManager cm = InferenceMain.getInstance().getConstraintManager();
        SlotManager sm = InferenceMain.getInstance().getSlotManager();
        Slot s = sm.getVariableSlot(type);
        if (s instanceof VariableSlot) {
            VariableSlot vs = (VariableSlot) s;
            ConstantSlot rep = sm.createConstantSlot(REP);
            cm.addPreferenceConstraint(vs, rep, 80);
        }
        return super.visitVariable(node, p);
    }

    /**
     * gut does not use receiver annotations, forbid them.
     */
    @Override
    public Void visitMethod(MethodTree node, Void p) {
        // System.out.println("MethodTree: " + node);
        /*Tree receiver = node.getReceiverParameter();
        if (receiver != null) {
            AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(receiver);
            ConstraintManager cm = InferenceMain.getInstance().getConstraintManager();
            SlotManager sm = InferenceMain.getInstance().getSlotManager();
            VariableSlot variableSlot = sm.getVariableSlot(type);
            cm.addEqualityConstraint(variableSlot, sm.getSlot(SELF));
        }*/
        /*if (TreeUtils.isConstructor(node)) {
            Tree returnTree = node.getReturnType();
            AnnotatedTypeMirror returnType = atypeFactory.getAnnotatedType(returnTree);
            mainIs(returnType, SELF, "uts.lost.lhs", returnTree);
        } else {
            VariableTree receiverParameter = node.getReceiverParameter();
            mainIs(atypeFactory.getAnnotatedType(receiverParameter), SELF, "uts.lost.lhs", receiverParameter);
        }*/
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

    @Override
    public Void visitLiteral(LiteralTree node, Void aVoid) {
        return super.visitLiteral(node, aVoid);
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
        // System.out.println("GUTIVisitor: visitNewClass is called!");
        assert node != null;
        //Using GUTAnnotatedTypeFactory is OK! It can still get the VarAnnotations, and add
        //constraints on them
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> fromUse = atypeFactory.constructorFromUse(node);
        AnnotatedExecutableType constructor = fromUse.first;

        // Check for @Lost and @VPLost in combined parameter types.
        for (AnnotatedTypeMirror parameterType : constructor.getParameterTypes()) {
            doesNotContain(parameterType, LOST, "uts.lost.parameter", node);
            doesNotContain(parameterType, VPLOST, "uts.vplost.parameter", node);
        }

        // Check for @Peer or @Rep as top-level modifier.
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        mainIsNoneOf(type, new AnnotationMirror[] { LOST, VPLOST,
                ANY }, "uts.new.ownership", node);
        mainIsNoneOf(type, new AnnotationMirror[] { BOTTOM, SELF }, "uts.new.ownership", node);

        // Forbid rep in static context
        if (isContextStatic(atypeFactory.getPath(node))) {
            doesNotContain(type, REP, "uts.static.rep.forbidden", node);
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
                    // System.out.println("GUTIVisitor::isContextStatic: lost in the tree, help! Path leaf: " + path.getLeaf());
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

        /*AnnotatedTypeMirror receiver = atypeFactory.getReceiverType(node);
        doesNotContain(receiver, new AnnotationMirror[]{VPLOST, LOST}, "uts.lost.receiver", node);*/

        AnnotatedExecutableType method = atypeFactory.methodFromUse(node).first;
        // Check for @Lost and @VPLost in combined parameter types.
        for (AnnotatedTypeMirror parameterType : method.getParameterTypes()) {
            doesNotContain(parameterType, LOST, "uts.lost.parameter", node);
            doesNotContain(parameterType, VPLOST, "uts.lost.parameter", node);
        }

        if (checkOaM) {
            ExpressionTree recvTree = TreeUtils.getReceiverTree(node.getMethodSelect());
            if (recvTree != null) {
                AnnotatedTypeMirror recvType = atypeFactory.getAnnotatedType(recvTree);

                if (recvType != null) {
                    ExecutableElement exelem = TreeUtils.elementFromUse(node);
                    if (!hasPure(exelem)) {
                        mainIsNoneOf(recvType, new AnnotationMirror[] {LOST, VPLOST, ANY},
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
        return anno.toString().equals(GUT.qual.Pure.class.getName())
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
        doesNotContain(type, LOST, "uts.lost.lhs", node);
        doesNotContain(type, VPLOST, "uts.vplost.lhs", node);

        if (checkOaM) {
            ExpressionTree recvTree = TreeUtils.getReceiverTree(node.getVariable());
            if (recvTree != null) {
                AnnotatedTypeMirror recvType = atypeFactory.getAnnotatedType(recvTree);

                if (recvType != null) {
                    // TODO: do we need to treat "this" and "super" specially?
                    mainIsNoneOf(recvType,
                            new AnnotationMirror[] { LOST, VPLOST, ANY },
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
         * Would it be helpful to somehow support such warnings for type inference----, e.g.
         * by reducing weighting?
         */
        //doesNotContain(castty, new AnnotationMirror[]{LOST, VPLOST}, "uts.cast.type.warning", node);
        if ((AnnotatedTypes.containsModifier(castty, LOST) ||
                AnnotatedTypes.containsModifier(castty, VPLOST) ||
                    AnnotatedTypes.containsModifier(castty, ANY)) &&
                        !GUTChecker.isAnyDefault(castty)) {
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
        if ((AnnotatedTypes.containsModifier(ioty, LOST) ||
                AnnotatedTypes.containsModifier(ioty, VPLOST) ||
                    AnnotatedTypes.containsModifier(ioty, ANY)) &&
                        !GUTChecker.isAnyDefault(ioty)) {
            checker.report(Result.warning("uts.instanceof.type.warning", ioty), node);
            // checker.getProcessingEnvironment().getMessager().printMessage(javax.tools.Diagnostic.Kind.WARNING,
            // "Casting to " + type + " is not recommended.");
        }

        areComparable(ioty, exprty, "uts.instanceof.type.error", node);

        return super.visitInstanceOf(node, p);
    }

    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        return true;
    }
/* TODO: what do we want to enforce for strings? They don't really matter anyway.
    @Override
    public Void visitBinary( node, Void p) {
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
    private final class GUTIInferenceValidator extends InferenceValidator {
        public GUTIInferenceValidator(BaseTypeChecker checker,
                InferenceVisitor<?, ?> visitor,
                // GUTAnnotatedTypeFactory atypeFactory) {
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
            // System.out.println("==========visitDeclared in GUTIInferenceValidator is called!");
            // System.out.println(
            // "GUTIvalidator: visitDeclared is called!");
            // ModifiersTree mt = ((VariableTree) p).getModifiers();
            // if (mt.getFlags().contains(Modifier.STATIC)) {
            // System.out.println("\n========p is: " + p);
            //System.out.println("p is null: " + p == null);
            // System.out.println("atypeFactory is null: ");
            // System.out.println(atypeFactory == null);
            // System.out.println("----------------atypeFactory.getPath(p) is: "
            // + atypeFactory.getPath(p));
            if (GUTIVisitor.isContextStatic(atypeFactory.getPath(p))) {
                doesNotContain(type, REP, "uts.static.rep.forbidden", p);
            
                if (warn_staticpeer) {
                    // TODO: I would really like to only give the warning if
                    // the modifier was explicit.
                    // TODO: Only want a warning, not an error.
                    doesNotContain(type, PEER, "uts.static.peer.warning", p);
                }
            }

            if (!allowLost) {
                doesNotContain(type, LOST, "uts.explicit.lost.forbidden", p);
                // We allow vplost in declared type, for example method return type
                //doesNotContain(type, VPLOST, "uts.explicit.lost.forbidden", p);
            }
            // Me: Is it allowed to have multiple universe modifiers? What does
            // it mean?
            if (type.getAnnotations().size() > 2) {
                System.out.println(
                        "GUTVisitor$GUTIValidator: Don't know if it's correct: type is: "
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
            // "GUTIvalidator: visitParameterizedType is called!");

            final TypeElement element = (TypeElement) type.getUnderlyingType().asElement();
            List<AnnotatedTypeParameterBounds> typeParamBounds = atypeFactory.typeVariablesFromUse(type, element);
            for (AnnotatedTypeParameterBounds atpb : typeParamBounds) {
                doesNotContain(atpb.getLowerBound(), LOST, "uts.lost.in.bounds", tree);
                doesNotContain(atpb.getUpperBound(), LOST, "uts.lost.in.bounds", tree);
                doesNotContain(atpb.getLowerBound(), VPLOST, "uts.vplost.in.bounds", tree);
                doesNotContain(atpb.getUpperBound(), VPLOST, "uts.vplost.in.bounds", tree);
            }
            for(AnnotatedTypeMirror atm: type.getTypeArguments()){
                doesNotContain(atm, LOST,"uts.lost.in.type.arguments",tree);
                doesNotContain(atm, VPLOST,"uts.lost.in.type.arguments",tree);
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
        /*@Override
        public Void visitPrimitive(AnnotatedPrimitiveType type, Tree p) {
            try {
                SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
                ConstraintManager constraintManager = InferenceMain.getInstance().getConstraintManager();
                VariableSlot variableSlot = slotManager.getVariableSlot(type);
                constraintManager.addEqualityConstraint(variableSlot, slotManager.getSlot(BOTTOM));
                logger.info("Added equality constraint between @Bottom and primitive type: " + type + " when visiting tree: \n" + p);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Missing VarAnnot annotation:")) {
                    logger.fatal(type + " doesn't contain VarAnnot!", e);
                } else {
                    logger.fatal("Unknwon runtime exception!", e);
                }
                ErrorReporter.errorAbort("Error when getting self type", p);
            }
            *//*if (type.isAnnotatedInHierarchy(ANY)) {
                Set<AnnotationMirror> ann = type.getAnnotations();
                if (ann.size() > 1
                        || (ann.size() == 1 && !ann.contains(BOTTOM))) {
                    // the implicit default is BOTTOM, which cannot be used
                    // explicitly.
                    // if there are explicit annotations -> error.
                    reportError(type, p);
                }
            }*//*
            return super.visitPrimitive(type, p);
        }*/

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
