package universe;

import static universe.GUTChecker.ANY;
import static universe.GUTChecker.BOTTOM;
import static universe.GUTChecker.LOST;
import static universe.GUTChecker.PEER;
import static universe.GUTChecker.REP;
import static universe.GUTChecker.SELF;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.type.AbstractViewpointAdapter;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;

public class GUTViewpointAdapter extends AbstractViewpointAdapter {

    public GUTViewpointAdapter(AnnotatedTypeFactory atypeFactory) {
        super(atypeFactory);
    }

    @Override
    protected AnnotationMirror extractAnnotationMirror(AnnotatedTypeMirror atm) {
        assert atm != null;
        return atm.getAnnotationInHierarchy(ANY);
    }

    @Override
    protected AnnotationMirror combineAnnotationWithAnnotation(AnnotationMirror receiverAnnotation,
            AnnotationMirror declaredAnnotation) {
        assert receiverAnnotation != null;
        assert declaredAnnotation != null;

        if (AnnotationUtils.areSame(receiverAnnotation, SELF)) {
            return declaredAnnotation;
        } else if (AnnotationUtils.areSame(declaredAnnotation, BOTTOM)) {
            return BOTTOM;
        } else if (AnnotationUtils.areSame(declaredAnnotation, ANY)) {
            return ANY;
        } else if (AnnotationUtils.areSame(receiverAnnotation, BOTTOM)) {
            // If receiver is bottom, has no ownership information. Any member
            // of it from the viewpoint of self is any, except when declared
            // type is bottom.
            return ANY;
        } else if (AnnotationUtils.areSame(declaredAnnotation, LOST)) {
            return LOST;
        } else if (AnnotationUtils.areSame(declaredAnnotation, PEER)) {
            if (AnnotationUtils.areSame(receiverAnnotation, PEER)) {
                return PEER;
            } else if (AnnotationUtils.areSame(receiverAnnotation, REP)) {
                return REP;
            }
        }
        return LOST;
    }
}
