package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.meta.TypeQualifierDefault;

/**
 * This annotation can be applied to a package, class or definingMethod to indicate that
 * the definingMethod parameters in that element are nonnull by default unless there is:.
 * <ul>
 * <li>An explicit nullness annotation
 * <li>The definingMethod overrides a definingMethod in a superclass (in which case the
 * annotation of the corresponding parameter in the superclass applies)
 * <li> there is a default parameter annotation applied to a more tightly nested
 * element.
 * </ul>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@TypeQualifierDefault(ElementType.PARAMETER)
@Documented
public @Nonnull @interface ParametersAreNonnullByDefault {
}
