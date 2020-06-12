package universe;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ImplicitsTypeAnnotator;
import org.checkerframework.javacutil.Pair;
import universe.qual.Any;
import universe.qual.Bottom;
import universe.qual.Lost;
import universe.qual.Peer;
import universe.qual.Rep;
import universe.qual.Self;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.ViewpointAdapter;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static universe.GUTChecker.ANY;
import static universe.GUTChecker.BOTTOM;
import static universe.GUTChecker.PEER;
import static universe.GUTChecker.REP;
import static universe.GUTChecker.SELF;

/**
 * Apply viewpoint adaptation and add implicit annotations to "this" and
 * "super".
 *
 * @author wmdietl
 */
public class GUTAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    public GUTAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);

        addAliasedAnnotation(org.jmlspecs.annotation.Peer.class, PEER);
        addAliasedAnnotation(org.jmlspecs.annotation.Rep.class, REP);
        addAliasedAnnotation(org.jmlspecs.annotation.Readonly.class, ANY);

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

    @Override
    protected ViewpointAdapter createViewpointAdapter() {
        return new GUTViewpointAdapter(this);
    }

    /**
     * Create our own TreeAnnotator.
     * @return the new TreeAnnotator.
     */
    @Override
    protected TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new GUTPropagationTreeAnnotator(this),
                new ImplicitsTreeAnnotator(this),
                new GUTTreeAnnotator()
                );
    }

    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        Set<Class<? extends Annotation>> annotations = new HashSet<>(
                Arrays.asList(Any.class, Lost.class,
                        Peer.class, Rep.class, Self.class, Bottom.class));
        return Collections.unmodifiableSet(annotations);
    }

    @Override
    public void addComputedTypeAnnotations(Element elt, AnnotatedTypeMirror type) {
        GUTTypeUtil.defaultConstructorReturnToSelf(elt, type);
        super.addComputedTypeAnnotations(elt, type);
    }

    /**
     * Currently only needed to add the "self" modifier to "super".
     */
    private class GUTTreeAnnotator extends TreeAnnotator {

        private GUTTreeAnnotator() {
            super(GUTAnnotatedTypeFactory.this);
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
            TreePath path = atypeFactory.getPath(node);
            if (path != null) {
                final TreePath parentPath = path.getParentPath();
                if (parentPath != null) {
                    final Tree parentNode = parentPath.getLeaf();
                    if (TreeUtils.isClassTree(parentNode)) {
                        p.replaceAnnotation(SELF);
                    }
                }
            }
            return super.visitParameterizedType(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, AnnotatedTypeMirror p) {
            ExecutableElement executableElement = TreeUtils.elementFromDeclaration(node);
            GUTTypeUtil.defaultConstructorReturnToSelf(executableElement, p);
            return super.visitMethod(node, p);
        }
    }

    private class GUTPropagationTreeAnnotator extends PropagationTreeAnnotator {
        /**
         * Creates a {@link ImplicitsTypeAnnotator}
         * from the given checker, using that checker's type hierarchy.
         *
         * @param atypeFactory
         */
        public GUTPropagationTreeAnnotator(AnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        // TODO This is very ugly. Why is array component type from lhs propagates to rhs?!
        @Override
        public Void visitNewArray(NewArrayTree tree, AnnotatedTypeMirror type) {
            // Below is copied from super
            assert type.getKind() == TypeKind.ARRAY
                    : "PropagationTreeAnnotator.visitNewArray: should be an array type";

            AnnotatedTypeMirror componentType = ((AnnotatedTypeMirror.AnnotatedArrayType) type).getComponentType();

            Collection<? extends AnnotationMirror> prev = null;
            if (tree.getInitializers() != null && tree.getInitializers().size() != 0) {
                // We have initializers, either with or without an array type.

                for (ExpressionTree init : tree.getInitializers()) {
                    AnnotatedTypeMirror initType = atypeFactory.getAnnotatedType(init);
                    // initType might be a typeVariable, so use effectiveAnnotations.
                    Collection<AnnotationMirror> annos = initType.getEffectiveAnnotations();

                    prev = (prev == null) ? annos : atypeFactory.getQualifierHierarchy().leastUpperBounds(prev, annos);
                }
            } else {
                prev = componentType.getAnnotations();
            }

            assert prev != null
                    : "PropagationTreeAnnotator.visitNewArray: violated assumption about qualifiers";

            Pair<Tree, AnnotatedTypeMirror> context =
                    atypeFactory.getVisitorState().getAssignmentContext();
            Collection<? extends AnnotationMirror> post;

            if (context != null
                    && context.second != null
                    && context.second instanceof AnnotatedTypeMirror.AnnotatedArrayType) {
                AnnotatedTypeMirror contextComponentType =
                        ((AnnotatedTypeMirror.AnnotatedArrayType) context.second).getComponentType();
                // Only compare the qualifiers that existed in the array type
                // Defaulting wasn't performed yet, so prev might have fewer qualifiers than
                // contextComponentType, which would cause a failure.
                // TODO: better solution?
                boolean prevIsSubtype = true;
                for (AnnotationMirror am : prev) {
                    if (contextComponentType.isAnnotatedInHierarchy(am)
                            && !atypeFactory.getQualifierHierarchy().isSubtype(
                            am, contextComponentType.getAnnotationInHierarchy(am))) {
                        prevIsSubtype = false;
                    }
                }
                // TODO: checking conformance of component kinds is a basic sanity check
                // It fails for array initializer expressions. Those should be handled nicer.
                if (contextComponentType.getKind() == componentType.getKind()
                        && (prev.isEmpty()
                        || (!contextComponentType.getAnnotations().isEmpty()
                        && prevIsSubtype))) {
                    post = contextComponentType.getAnnotations();
                } else {
                    // The type of the array initializers is incompatible with the
                    // context type!
                    // Somebody else will complain.
                    post = prev;
                }
            } else {
                // No context is available - simply use what we have.
                post = prev;
            }

            // Below line is the only difference from super implementation
            applyImmutableIfImplicitlyBottom(componentType);
            // Above line is the only difference from super implementation
            componentType.addMissingAnnotations(post);

            return null;
            // Above is copied from super
        }

        /**Add immutable to the result type of a binary operation if the result type is implicitly immutable*/
        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
            applyImmutableIfImplicitlyBottom(type);// Usually there isn't existing annotation on binary trees, but to be safe, run it first
            super.visitBinary(node, type);
            return null;
        }

        /**Add immutable to the result type of a cast if the result type is implicitly immutable*/
        @Override
        public Void visitTypeCast(TypeCastTree node, AnnotatedTypeMirror type) {
            applyImmutableIfImplicitlyBottom(type);// Must run before calling super method to respect existing annotation
            return super.visitTypeCast(node, type);
        }

        /**Because TreeAnnotator runs before ImplicitsTypeAnnotator, implicitly immutable types are not guaranteed
         to always have immutable annotation. If this happens, we manually add immutable to type. We use
         addMissingAnnotations because we want to respect existing annotation on type*/
        private void applyImmutableIfImplicitlyBottom(AnnotatedTypeMirror type) {
            if (GUTTypeUtil.isImplicitlyBottomType(type)) {
                type.addMissingAnnotations(new HashSet<>(Arrays.asList(BOTTOM)));
            }
        }
    }
}
