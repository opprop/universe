package universe;

import org.checkerframework.framework.type.AbstractViewpointAdapter;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;

public class UniverseViewpointAdapter extends AbstractViewpointAdapter {

    public UniverseViewpointAdapter(AnnotatedTypeFactory atypeFactory) {
        super(atypeFactory);
    }

    @Override
    protected AnnotationMirror extractAnnotationMirror(AnnotatedTypeMirror atm) {
        assert atm != null;
        return atm.getAnnotationInHierarchy(((UniverseAnnotatedTypeFactory)atypeFactory).ANY);
    }

    @Override
    protected AnnotationMirror combineAnnotationWithAnnotation(
            AnnotationMirror receiverAnnotation, AnnotationMirror declaredAnnotation) {
        assert receiverAnnotation != null;
        assert declaredAnnotation != null;

        if (AnnotationUtils.areSame(receiverAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).SELF)) {
            return declaredAnnotation;
        } else if (AnnotationUtils.areSame(declaredAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).BOTTOM)) {
            return ((UniverseAnnotatedTypeFactory)atypeFactory).BOTTOM;
        } else if (AnnotationUtils.areSame(declaredAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).ANY)) {
            return ((UniverseAnnotatedTypeFactory)atypeFactory).ANY;
        } else if (AnnotationUtils.areSame(declaredAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).SELF)) {
            return receiverAnnotation;
        } else if (AnnotationUtils.areSame(receiverAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).BOTTOM)) {
            // If receiver is bottom, has no ownership information. Any member
            // of it from the viewpoint of self is any, except when declared
            // type is bottom.
            return ((UniverseAnnotatedTypeFactory)atypeFactory).ANY;
        } else if (AnnotationUtils.areSame(declaredAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).LOST)) {
            return ((UniverseAnnotatedTypeFactory)atypeFactory).LOST;
        } else if (AnnotationUtils.areSame(declaredAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).PEER)) {
            if (AnnotationUtils.areSame(receiverAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).PEER)) {
                return ((UniverseAnnotatedTypeFactory)atypeFactory).PEER;
            } else if (AnnotationUtils.areSame(receiverAnnotation, ((UniverseAnnotatedTypeFactory)atypeFactory).REP)) {
                return ((UniverseAnnotatedTypeFactory)atypeFactory).REP;
            }
        }
        return ((UniverseAnnotatedTypeFactory)atypeFactory).LOST;
    }
}
