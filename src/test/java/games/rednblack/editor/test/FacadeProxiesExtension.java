package games.rednblack.editor.test;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.puremvc.Facade;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;

/**
 * JUnit 5 extension that sets up a {@link Facade} with ProjectManager, ResolutionManager,
 * ResourceManager, and SceneDataManager proxies (mocks) for each test and disposes
 * the Facade after each test. Use {@code @ExtendWith(FacadeProxiesExtension.class)}
 * and inject {@link ProjectManager}, {@link ResolutionManager}, {@link ResourceManager},
 * and/or {@link SceneDataManager} via the test class constructor to receive the mocks.
 *
 * <p><strong>Not safe for parallel execution.</strong> The Facade is a global singleton
 * ({@code Facade.getInstance()}); tests using this extension share and mutate it (dispose + register
 * proxies). Use {@link ExtendWithFacadeProxies} to apply this extension and {@code SAME_THREAD}
 * in one step; or add {@code @Execution(ExecutionMode.SAME_THREAD)} to the test class. If the
 * class runs with {@code CONCURRENT} mode, this extension throws in {@code beforeEach}.
 */
public final class FacadeProxiesExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    /** True after the four proxy mocks have been created (lazily, on first constructor parameter resolve). */
    private boolean proxiesCreated;

    private ProjectManager projectManager;
    private ResolutionManager resolutionManager;
    private ResourceManager resourceManager;
    private SceneDataManager sceneDataManager;

    private void registerProxiesWithFacade() {
        Facade facade = Facade.getInstance();
        facade.registerProxy(projectManager);
        facade.registerProxy(resolutionManager);
        facade.registerProxy(resourceManager);
        facade.registerProxy(sceneDataManager);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (context.getExecutionMode() == ExecutionMode.CONCURRENT) {
            throw new IllegalStateException(
                    "FacadeProxiesExtension cannot run with ExecutionMode.CONCURRENT because Facade is a global singleton. "
                            + "Use @ExtendWithFacadeProxies on the test class, or add @Execution(ExecutionMode.SAME_THREAD).");
        }
        Facade.dispose();
        if (proxiesCreated) {
            registerProxiesWithFacade();
            clearInvocations(projectManager, resolutionManager, resourceManager, sceneDataManager);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Facade.dispose();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == ProjectManager.class || type == ResolutionManager.class
                || type == ResourceManager.class || type == SceneDataManager.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        if (!proxiesCreated) {
            projectManager = Mockito.mock(ProjectManager.class, Mockito.withSettings().strictness(Strictness.LENIENT));
            resolutionManager = Mockito.mock(ResolutionManager.class, Mockito.withSettings().strictness(Strictness.LENIENT));
            resourceManager = Mockito.mock(ResourceManager.class, Mockito.withSettings().strictness(Strictness.LENIENT));
            sceneDataManager = Mockito.mock(SceneDataManager.class, Mockito.withSettings().strictness(Strictness.LENIENT));

            when(projectManager.getName()).thenReturn(ProjectManager.NAME);
            when(resolutionManager.getName()).thenReturn(ResolutionManager.NAME);
            when(resourceManager.getName()).thenReturn(ResourceManager.NAME);
            when(sceneDataManager.getName()).thenReturn(SceneDataManager.NAME);
            proxiesCreated = true;
        }

        Class<?> type = parameterContext.getParameter().getType();
        if (type == ProjectManager.class) return projectManager;
        if (type == ResolutionManager.class) return resolutionManager;
        if (type == ResourceManager.class) return resourceManager;
        if (type == SceneDataManager.class) return sceneDataManager;
        throw new IllegalStateException("Unsupported parameter type: " + type);
    }
}
