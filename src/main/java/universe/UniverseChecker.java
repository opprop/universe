package universe;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.SupportedLintOptions;

import javax.annotation.processing.SupportedOptions;

/**
 * The main checker class for the Generic Universe Types checker.
 *
 * @author wmdietl
 */
/*
 * Use this for warning messages:
 * messager.printMessage(javax.tools.Diagnostic.Kind.WARNING, "message");
 */
@SupportedOptions({"warn"})
@SupportedLintOptions({"allowLost", "checkOaM", "checkStrictPurity"})
public class UniverseChecker extends BaseTypeChecker {

    @Override
    public void initChecker() {
        super.initChecker();
    }

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new UniverseVisitor(this, new UniverseAnnotatedTypeFactory(this, false));
    }
}
