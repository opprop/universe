package universe.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a side-effect (pure) method. See the <a
 * href="http://www.eecs.ucf.edu/~leavens/JML/jmlrefman/jmlrefman_7.html#SEC59">JML Reference
 * Manual</a> for a definition.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Pure {}
