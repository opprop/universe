package universe;


import javax.annotation.processing.SupportedOptions;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.SupportedLintOptions;
import org.checkerframework.framework.type.AnnotatedTypeMirror;


/**
 * The main checker class for the Generic Universe Types checker.
 *
 * @author wmdietl
 */
/*
 * Use this for warning messages:
 * messager.printMessage(javax.tools.Diagnostic.Kind.WARNING, "message");
 */
@SupportedOptions( { "warn" } )
@SupportedLintOptions({"allowLost", "checkOaM", "checkStrictPurity"})
public class UniverseChecker extends BaseTypeChecker {

    //TODO Clarify the purpose of this
    public static boolean isAnyDefault(AnnotatedTypeMirror type) {
        // if (!(type instanceof AnnotatedDeclaredType))
        return false;
        /*
        DeclaredType dtype = ((AnnotatedDeclaredType)type).getUnderlyingType();
        return TypesUtils.isDeclaredOfName(dtype, "java.lang.String") ||
        TypesUtils.isDeclaredOfName(dtype, "java.lang.Character");
        */
    }

    @Override
    public boolean withViewpointAdaptation() {
	return true;
    }

    /* TODO: purity/OaM checking
    @Override
    public boolean isAssignable(AnnotatedTypeMirror varType,
            AnnotatedTypeMirror receiverType, Tree varTree) {
        return super.isAssignable(varType, receiverType, varTree);
    }
    */

}
