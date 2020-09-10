package universe;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.framework.type.AnnotatedTypeReplacer;
import org.checkerframework.framework.type.DefaultTypeHierarchy;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;

import universe.qual.Any;
import universe.qual.Bottom;
import universe.qual.Lost;
import universe.qual.Peer;
import universe.qual.Rep;
import universe.qual.Self;
import universe.qual.VPLost;

/**
 * Apply viewpoint adaptation and add implicit annotations to "this" and
 * "super".
 *
 * @author wmdietl
 */
public class UniverseAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    // TODO: change to getters?
    protected AnnotationMirror ANY, PEER, REP, LOST, VPLOST, SELF, BOTTOM, PURE;

    public UniverseAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);
        ANY = AnnotationUtils.fromClass(elements, Any.class);
        PEER = AnnotationUtils.fromClass(elements, Peer.class);
        REP = AnnotationUtils.fromClass(elements, Rep.class);
        LOST = AnnotationUtils.fromClass(elements, Lost.class);
        VPLOST = AnnotationUtils.fromClass(elements, VPLost.class);
        SELF = AnnotationUtils.fromClass(elements, Self.class);
        BOTTOM = AnnotationUtils.fromClass(elements, Bottom.class);
        PURE = AnnotationUtils.fromClass(elements, Pure.class);

        addAliasedAnnotation(org.jmlspecs.annotation.Peer.class, PEER);
        addAliasedAnnotation(org.jmlspecs.annotation.Rep.class, REP);
        addAliasedAnnotation(org.jmlspecs.annotation.Readonly.class, ANY);
        // see UniverseVisitor.isPure for handling of pure

        this.postInit();
    }

    /**
     * The type of "this" is always "self".
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        if (type != null) {
            type.replaceAnnotation(SELF);
        }
        return type;
    }

    /**
     * Create our own TreeAnnotator.
     * @return the new TreeAnnotator.
     */
    @Override
    protected TreeAnnotator createTreeAnnotator() {
        TreeAnnotator fromAbove = super.createTreeAnnotator();
        return new ListTreeAnnotator(
                new UniverseTreeAnnotator(),
                fromAbove
                );
    }

    /**
     * Create our own TypeAnnotator.
     * @return the new TreeAnnotator.
     */
    @Override
    protected TypeAnnotator createTypeAnnotator() {
        TypeAnnotator fromAbove = super.createTypeAnnotator();
        return new ListTypeAnnotator(
                fromAbove,
                new UniverseTypeAnnotator()
                );
    }

    @Override
    protected TypeHierarchy createTypeHierarchy() {
        return new UniverseTypeHierarchy(checker, getQualifierHierarchy());
    }


    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        Set<Class<? extends Annotation>> annotations = new HashSet<>(Arrays.asList(Any.class, Lost.class, VPLost.class,
                        Peer.class, Rep.class, Self.class, Bottom.class));
        return Collections.unmodifiableSet(annotations);
    }

    /**
     * Currently only needed to add the "self" modifier to "super".
     */
    private class UniverseTreeAnnotator extends TreeAnnotator {

        private UniverseTreeAnnotator() {
            super(UniverseAnnotatedTypeFactory.this);
        }

        /**
         * Add @Self to "super" identifier.
         *
         * @param node
         *            the identifier node.
         * @param p
         *            the AnnotatedTypeMirror to modify.
         */
        @Override
        public Void visitIdentifier(IdentifierTree node, AnnotatedTypeMirror p) {
            assert node != null;
            assert p != null;

            // There's no "super" kind, so make a string comparison.
            if (node.getName().contentEquals("super")) {
                p.replaceAnnotation(SELF);
            }

            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree node, AnnotatedTypeMirror p) {
            // System.out.println("visitParameterizedType: " + node + " " + p);
            Tree parent = getPath(node).getParentPath().getLeaf();
            if (TreeUtils.isClassTree(parent)) {
                p.replaceAnnotation(SELF);
            }
            return super.visitParameterizedType(node, p);
        }

        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror p) {
            // System.out.println("Binary: ");
            return super.visitBinary(node, p);
        }

        @Override
        public Void visitUnary(UnaryTree node, AnnotatedTypeMirror p) {
            // System.out.println("Unary: ");
            return super.visitUnary(node, p);
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node,
                AnnotatedTypeMirror p) {
            // System.out.println("Compound: ");
            return super.visitCompoundAssignment(node, p);
        }

        @Override
        public Void visitTypeCast(TypeCastTree node, AnnotatedTypeMirror p) {
            // System.out.println("Cast: ");
            return super.visitTypeCast(node, p);
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, AnnotatedTypeMirror type) {
            // System.out.println("Array type: " + type);
            super.visitNewArray(tree, type);
            // ((AnnotatedArrayType)type).getComponentType().addAnnotation(GUTChecker.BOTTOM);
            // System.out.println("Final Array type: " + type);
            return null;
        }

    }

    /**
     */
    private class UniverseTypeAnnotator extends TypeAnnotator {

        public UniverseTypeAnnotator() {
            super(UniverseAnnotatedTypeFactory.this);
        }
/*
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
            if (!type.isAnnotatedInHierarchy(ANY)) {
                if (GUTChecker.isAnyDefault(type)) {
                    type.addAnnotation(ANY);
                } else {
                    type.addAnnotation(PEER);
                }
            }
            return super.visitDeclared(type, p);
        }

        @Override
        public Void visitArray(AnnotatedArrayType type, Void p) {
            if (!type.isAnnotatedInHierarchy(ANY)) {
                if (GUTChecker.isAnyDefault(type)) {
                    type.addAnnotation(ANY);
                } else {
                    type.addAnnotation(PEER);
                }
            }
            return super.visitArray(type, p);
        }
        */
    }

    /*
    private final class GUTQualifierHierarchy extends GraphQualifierHierarchy {
        public GUTQualifierHierarchy(GraphQualifierHierarchy hierarchy) {
            super(hierarchy);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            return rhs == null || lhs == null
                   || super.isSubtype(rhs, lhs);
        }
    }
    */

    private final class UniverseTypeHierarchy extends DefaultTypeHierarchy {
        public UniverseTypeHierarchy(BaseTypeChecker checker, QualifierHierarchy qualifierHierarchy) {
            super(checker, qualifierHierarchy,
                    checker.hasOption("ignoreRawTypeArguments"),
                    checker.hasOption("invariantArrays"));
        }

        @Override
        public boolean isSubtype(AnnotatedTypeMirror sub, AnnotatedTypeMirror sup) {
            if (sub.getUnderlyingType().getKind().isPrimitive() ||
                    sup.getUnderlyingType().getKind().isPrimitive() ) {
                // TODO: Ignore boxing/unboxing
                return true;
            }
            if (TypesUtils.isString(sub.getUnderlyingType()) ||
                    TypesUtils.isString(sup.getUnderlyingType())) {
                return true;
            }

            return super.isSubtype(sub, sup);
        }

        @Override
        protected boolean isAnnoSubtype(AnnotationMirror subtypeAnno, AnnotationMirror supertypeAnno,
                boolean annosCanBeEmtpy) {
            if (subtypeAnno == null || supertypeAnno == null) {
                // TODO: lower bounds of wildcards in method parameters do not get defaulted correctly!
                return true;
            }

            return super.isAnnoSubtype(subtypeAnno, supertypeAnno, annosCanBeEmtpy);
        }

        /*
        @Override
        protected boolean isSubtypeAsTypeArgument(AnnotatedTypeMirror rhs, AnnotatedTypeMirror lhs) {
            return super.isSubtypeAsTypeArgument(rhs, lhs);
        }

        @Override
        protected boolean isSubtypeTypeArguments(AnnotatedDeclaredType rhs, AnnotatedDeclaredType lhs) {
            return super.isSubtypeTypeArguments(rhs, lhs);
        }
        */
    }
}
