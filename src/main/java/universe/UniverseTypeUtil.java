package universe;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TreePathUtil;
import org.checkerframework.javacutil.TypesUtils;
import universe.qual.Bottom;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import java.util.Arrays;
import java.util.List;

public class UniverseTypeUtil {

    private static boolean isInTypesOfImplicitForOfBottom(AnnotatedTypeMirror atm) {
        DefaultFor defaultFor = Bottom.class.getAnnotation(DefaultFor.class);
        assert defaultFor != null;
        assert defaultFor.typeKinds() != null;
        for (org.checkerframework.framework.qual.TypeKind typeKind : defaultFor.typeKinds()) {
            if (TypeKind.valueOf(typeKind.name()) == atm.getKind()) return true;
        }
        return false;
    }

    private static boolean isInTypeNamesOfImplicitForOfBottom(AnnotatedTypeMirror atm) {
        if (atm.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DefaultFor defaultFor = Bottom.class.getAnnotation(DefaultFor.class);
        assert defaultFor != null;
        assert defaultFor.types() != null;
        Class<?>[] typeNames = defaultFor.types();
        String fqn = TypesUtils.getQualifiedName((DeclaredType) atm.getUnderlyingType()).toString();
        for (int i = 0; i < typeNames.length; i++) {
            if (typeNames[i].getCanonicalName().toString().contentEquals(fqn)) return true;
        }
        return false;
    }

    public static boolean isImplicitlyBottomType(AnnotatedTypeMirror atm) {
        return isInTypesOfImplicitForOfBottom(atm) || isInTypeNamesOfImplicitForOfBottom(atm);
    }

    public static void applyConstant(AnnotatedTypeMirror type, AnnotationMirror am) {
        SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
        ConstraintManager constraintManager = InferenceMain.getInstance().getConstraintManager();
        // Might be null. It's normal. In typechecking side, we use addMissingAnnotations(). Only if
        // there is existing annotation in code, then here is non-null. Otherwise, VariableAnnotator
        // hasn't come into the picture yet, so no VarAnnot exists here, which is normal.
        Slot shouldBeAppliedTo = slotManager.getSlot(type);
        ConstantSlot constant = slotManager.createConstantSlot(am);
        if (shouldBeAppliedTo == null) {
            type.addAnnotation(slotManager.getAnnotation(constant));
        } else {
            constraintManager.addEqualityConstraint(shouldBeAppliedTo, constant);
        }
    }

    private static boolean isPure(DeclaredType anno) {
        return anno.toString().equals(universe.qual.Pure.class.getName())
                || anno.toString().equals(org.jmlspecs.annotation.Pure.class.getName());
    }

    public static boolean isPure(ExecutableElement executableElement) {
        boolean hasPure = false;
        List<? extends AnnotationMirror> anns = executableElement.getAnnotationMirrors();
        for (AnnotationMirror an : anns) {
            if (isPure(an.getAnnotationType())) {
                hasPure = true;
                break;
            }
        }
        return hasPure;
    }

    public static AnnotationMirror createEquivalentVarAnnotOfRealQualifier(final AnnotationMirror am) {
        final SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
        ConstantSlot constantSlot = slotManager.createConstantSlot(am);
        return slotManager.getAnnotation(constantSlot);
    }

    public static boolean inStaticScope(TreePath treePath) {
        boolean in = false;
        if (TreePathUtil.isTreeInStaticScope(treePath)) {
            in = true;
            // Exclude case in which enclosing class is static
            ClassTree classTree = TreePathUtil.enclosingClass(treePath);
            if (classTree != null && classTree.getModifiers().getFlags().contains((Modifier.STATIC))) {
                in = false;
            }
        }
        return in;
    }

    public static void defaultConstructorReturnToSelf(Element elt, AnnotatedTypeMirror type) {
        if (elt.getKind() == ElementKind.CONSTRUCTOR && type instanceof AnnotatedTypeMirror.AnnotatedExecutableType) {
            ((AnnotatedTypeMirror.AnnotatedExecutableType) type).getReturnType().addMissingAnnotations(Arrays.asList(UniverseAnnotationMirrorHolder.SELF));
        }
    }
}
