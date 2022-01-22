package universe;

import org.checkerframework.framework.source.SourceChecker;
import universe.qual.Any;
import universe.qual.Bottom;
import universe.qual.Lost;
import universe.qual.Peer;
import universe.qual.Rep;
import universe.qual.Self;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

/**
 * Class that declares the AnnotationMirrors used by typecheck and inference
 *
 */

public class UniverseAnnotationMirrorHolder {

    public static AnnotationMirror ANY, PEER, REP, LOST, SELF, BOTTOM, PURE;

    public static void init(SourceChecker checker) {
        Elements elements = checker.getElementUtils();
        ANY = AnnotationBuilder.fromClass(elements, Any.class);
        PEER = AnnotationBuilder.fromClass(elements, Peer.class);
        REP = AnnotationBuilder.fromClass(elements, Rep.class);
        LOST = AnnotationBuilder.fromClass(elements, Lost.class);
        SELF = AnnotationBuilder.fromClass(elements, Self.class);
        BOTTOM = AnnotationBuilder.fromClass(elements, Bottom.class);
        PURE = AnnotationBuilder.fromClass(elements, Pure.class);
    }

}
