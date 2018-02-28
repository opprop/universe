package universe.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A special annotation to distinguish the current object "this" from other
 * objects.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@TargetLocations({TypeUseLocation.RECEIVER})
@SubtypeOf({ Peer.class })
@DefaultFor({TypeUseLocation.RECEIVER})
public @interface Self {}