package universe;


import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.FrameworkViewpointAdaptor;
import org.checkerframework.javacutil.AnnotationUtils;

public class UniverseViewpointAdaptor extends FrameworkViewpointAdaptor{

    @Override
    protected AnnotationMirror getModifier(AnnotatedTypeMirror atm, AnnotatedTypeFactory f) {
        assert atm != null;
        UniverseAnnotatedTypeFactory univATF = (UniverseAnnotatedTypeFactory)f;

        if (atm.hasEffectiveAnnotation(univATF.PEER)) {
            return univATF.PEER;
        }
        if (atm.hasEffectiveAnnotation(univATF.REP)) {
            return univATF.REP;
        }
        if (atm.hasEffectiveAnnotation(univATF.ANY)) {
            return univATF.ANY;
        }
        if (atm.hasEffectiveAnnotation(univATF.SELF)) {
            return univATF.SELF;
        }
        if(atm.hasEffectiveAnnotation(univATF.BOTTOM)){
            return univATF.BOTTOM;
        }
        if (atm.hasEffectiveAnnotation(univATF.VPLOST)) {
            return univATF.VPLOST;
        }
        if (atm.hasEffectiveAnnotation(univATF.LOST)) {
            return univATF.LOST;
        }
        return null;
    }

    @Override
    protected AnnotationMirror combineModifierWithModifier(AnnotationMirror recvModifier, AnnotationMirror declModifier,
            AnnotatedTypeFactory f) {
        assert recvModifier != null;
        assert declModifier != null;

        UniverseAnnotatedTypeFactory univATF = (UniverseAnnotatedTypeFactory)f;
        if (AnnotationUtils.areSame(recvModifier, univATF.SELF)) {
            return declModifier;
        } else if (AnnotationUtils.areSame(recvModifier, univATF.PEER) &&
                AnnotationUtils.areSame(declModifier, univATF.PEER)) {
            return univATF.PEER;
        } else if (AnnotationUtils.areSame(recvModifier, univATF.REP) &&
                AnnotationUtils.areSame(declModifier, univATF.PEER)) {
            return univATF.REP;
        } else if (AnnotationUtils.areSame(declModifier, univATF.ANY)) {
            return univATF.ANY;
        } else if (AnnotationUtils.areSame(declModifier, univATF.LOST)) {
            return univATF.VPLOST;// Always use internal representation of lost - @VPLost
        } else if (AnnotationUtils.areSame(declModifier, univATF.BOTTOM)) {
            return univATF.BOTTOM;
        } else {
            return univATF.VPLOST;
        }
    }

    @Override
    public boolean shouldBeAdapted(AnnotatedTypeMirror type, Element element) {
        if (type.getKind() != TypeKind.DECLARED && type.getKind() != TypeKind.ARRAY) {
            return false;
        }
        if (element.getKind() == ElementKind.LOCAL_VARIABLE
                || element.getKind() == ElementKind.PARAMETER) {
            return false;
        }
        return true;
    }
}
