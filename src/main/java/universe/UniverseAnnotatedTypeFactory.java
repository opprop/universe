package universe;

import checkers.inference.BaseInferenceRealTypeFactory;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.ViewpointAdapter;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.LiteralTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.TreeUtils;

import universe.qual.Any;
import universe.qual.Bottom;
import universe.qual.Lost;
import universe.qual.Peer;
import universe.qual.Rep;
import universe.qual.Self;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Apply viewpoint adaptation and add implicit annotations to "this" and "super".
 *
 * @author wmdietl
 */
public class UniverseAnnotatedTypeFactory extends BaseInferenceRealTypeFactory {

    public UniverseAnnotatedTypeFactory(BaseTypeChecker checker, boolean infer) {
        super(checker, infer);
        this.postInit();
    }

    /** The type of "this" is always "self". */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        if (type != null) {
            type.replaceAnnotation(UniverseAnnotationMirrorHolder.SELF);
        }
        return type;
    }

    @Override
    protected ViewpointAdapter createViewpointAdapter() {
        return new UniverseViewpointAdapter(this);
    }

    /**
     * Create our own TreeAnnotator.
     *
     * @return the new TreeAnnotator.
     */
    @Override
    protected TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new PropagationTreeAnnotator(this),
                new LiteralTreeAnnotator(this),
                new UniverseTreeAnnotator());
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        Set<Class<? extends Annotation>> annotations =
                new HashSet<>(
                        Arrays.asList(
                                Any.class,
                                Lost.class,
                                Peer.class,
                                Rep.class,
                                Self.class,
                                Bottom.class));
        return Collections.unmodifiableSet(annotations);
    }

    @Override
    public void addComputedTypeAnnotations(Element elt, AnnotatedTypeMirror type) {
        UniverseTypeUtil.defaultConstructorReturnToSelf(elt, type);
        super.addComputedTypeAnnotations(elt, type);
    }

    /** Replace annotation of extends or implements clause with SELF in Universe. */
    @Override
    public AnnotatedTypeMirror getTypeOfExtendsImplements(Tree clause) {
        AnnotatedTypeMirror s = super.getTypeOfExtendsImplements(clause);
        s.replaceAnnotation(UniverseAnnotationMirrorHolder.SELF);
        return s;
    }

    /** Currently only needed to add the "self" modifier to "super". */
    private class UniverseTreeAnnotator extends TreeAnnotator {

        private UniverseTreeAnnotator() {
            super(UniverseAnnotatedTypeFactory.this);
        }

        /**
         * Add @Self to "super" identifier.
         *
         * @param node the identifier node.
         * @param p the AnnotatedTypeMirror to modify.
         */
        @Override
        public Void visitIdentifier(IdentifierTree node, AnnotatedTypeMirror p) {
            assert node != null;
            assert p != null;

            // There's no "super" kind, so make a string comparison.
            if (node.getName().contentEquals("super")) {
                p.replaceAnnotation(UniverseAnnotationMirrorHolder.SELF);
            }

            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, AnnotatedTypeMirror p) {
            ExecutableElement executableElement = TreeUtils.elementFromDeclaration(node);
            UniverseTypeUtil.defaultConstructorReturnToSelf(executableElement, p);
            return super.visitMethod(node, p);
        }
    }
}
