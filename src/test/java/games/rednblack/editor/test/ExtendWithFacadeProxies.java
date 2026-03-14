package games.rednblack.editor.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composed annotation that registers {@link FacadeProxiesExtension} and forces
 * {@link ExecutionMode#SAME_THREAD} in one step. Use this instead of
 * {@code @ExtendWith(FacadeProxiesExtension.class)} plus {@code @Execution(ExecutionMode.SAME_THREAD)},
 * so that tests using the Facade singleton automatically run in the same thread.
 *
 * <p>Example:
 * <pre>
 * &#64;ExtendWithFacadeProxies
 * &#64;ExtendWith(MockitoExtension.class)
 * class MyFacadeTest {
 *     MyFacadeTest(ProjectManager projectManager, ResolutionManager resolutionManager) { ... }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FacadeProxiesExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
public @interface ExtendWithFacadeProxies {
}
