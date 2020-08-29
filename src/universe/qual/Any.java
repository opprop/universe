package universe.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Any modifier expresses no static ownership information, the referenced
 * object can have any owner.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@SubtypeOf({})
@DefaultFor({ TypeUseLocation.LOCAL_VARIABLE, TypeUseLocation.RESOURCE_VARIABLE,
    TypeUseLocation.IMPLICIT_UPPER_BOUND })
public @interface Any {}
