package games.rednblack.editor.test;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.nio.ByteBuffer;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Simplified JUnit 5 extension that mocks libGDX {@link Gdx} static fields using Mockito
 * to enable unit/integration testing of VisUI widgets without a real graphics context.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @ExtendWith(GdxMockExtension.class)
 * class VisUiWidgetTest {
 *     private Stage stage;
 *     private Skin skin;
 *
 *     @BeforeEach
 *     void setUp() {
 *         skin = new Skin();
 *         skin.add("default-font", new BitmapFont());
 *         VisUI.load(skin);
 *         stage = new Stage();
 *     }
 *
 *     @AfterEach
 *     void tearDown() {
 *         VisUI.dispose();
 *         stage.dispose();
 *     }
 *
 *     @Test
 *     void testVisButton() {
 *         VisTextButton button = new VisTextButton("Click me");
 *         assertEquals("Click me", button.getText().toString());
 *     }
 * }
 * }</pre>
 *
 * <h2>Important Notes</h2>
 * <ul>
 *     <li>Not safe for parallel execution - Gdx is a global singleton</li>
 *     <li>Use {@code @Execution(ExecutionMode.SAME_THREAD)} or {@link ExtendWithGdxMock}</li>
 *     <li>Call {@link VisUI#dispose()} after each test to clean up</li>
 * </ul>
 *
 * @see ExtendWithGdxMock
 */
public class GdxMockExtension implements BeforeEachCallback, AfterEachCallback {

    protected Application mockApp;
    protected Graphics mockGraphics;
    protected Input mockInput;
    protected Audio mockAudio;
    protected Files mockFiles;
    protected Net mockNet;
    protected GL20 mockGL20;
    protected GL30 mockGL30;
    protected SpriteBatch mockSpriteBatch;
    protected Viewport mockViewport;

    private Application originalApp;
    private Graphics originalGraphics;
    private Input originalInput;
    private Audio originalAudio;
    private Files originalFiles;
    private Net originalNet;
    private GL20 originalGL;

    @Override
    public void beforeEach(ExtensionContext context) {
        originalApp = Gdx.app;
        originalGraphics = Gdx.graphics;
        originalInput = Gdx.input;
        originalAudio = Gdx.audio;
        originalFiles = Gdx.files;
        originalNet = Gdx.net;
        originalGL = Gdx.gl;

        mockApp = mock(Application.class);
        mockGraphics = mock(Graphics.class);
        mockInput = mock(Input.class);
        mockAudio = mock(Audio.class);
        mockFiles = mock(Files.class);
        mockNet = mock(Net.class);
        mockGL20 = mock(GL20.class);
        mockGL30 = mock(GL30.class);
        mockSpriteBatch = mock(SpriteBatch.class);
        mockViewport = mock(Viewport.class, RETURNS_DEEP_STUBS);

        when(mockGraphics.getWidth()).thenReturn(800);
        when(mockGraphics.getHeight()).thenReturn(600);
        when(mockGraphics.getDeltaTime()).thenReturn(1f / 60f);
        when(mockGraphics.getFramesPerSecond()).thenReturn(60);

        when(mockApp.getType()).thenReturn(Application.ApplicationType.Desktop);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockApp).postRunnable(any(Runnable.class));

        // Mock files for VisUI skin loading
        when(mockFiles.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return new FileHandle(new File("assets/" + path));
        });

        when(mockFiles.classpath(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            // Use actual classpath resource loading for test resources
            java.net.URL url = GdxMockExtension.class.getClassLoader().getResource(path);
            if (url == null) {
                throw new com.badlogic.gdx.utils.GdxRuntimeException("File not found: " + path + " (Classpath)");
            }
            return new FileHandle(new java.io.File(url.getFile()));
        });

        Gdx.app = mockApp;
        Gdx.graphics = mockGraphics;
        Gdx.input = mockInput;
        Gdx.audio = mockAudio;
        Gdx.files = mockFiles;
        Gdx.net = mockNet;
        Gdx.gl = mockGL20;
        Gdx.gl20 = mockGL20;
        Gdx.gl30 = mockGL30;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Gdx.app = originalApp;
        Gdx.graphics = originalGraphics;
        Gdx.input = originalInput;
        Gdx.audio = originalAudio;
        Gdx.files = originalFiles;
        Gdx.net = originalNet;
        Gdx.gl = originalGL;
        Gdx.gl20 = originalGL;
        Gdx.gl30 = null;

        mockApp = null;
        mockGraphics = null;
        mockInput = null;
        mockAudio = null;
        mockFiles = null;
        mockNet = null;
        mockGL20 = null;
        mockGL30 = null;
        mockSpriteBatch = null;
        mockViewport = null;
    }

    /**
     * Creates a Stage for VisUI widget testing using a mock SpriteBatch and Viewport.
     */
    public Stage createStage() {
        return new Stage(mockViewport, mockSpriteBatch);
    }

    /**
     * Gets the mock SpriteBatch for custom Stage creation.
     */
    public SpriteBatch getMockSpriteBatch() {
        return mockSpriteBatch;
    }

    /**
     * Gets the mock Viewport for custom Stage creation.
     */
    public Viewport getMockViewport() {
        return mockViewport;
    }

    /**
     * Creates a minimal Skin for VisUI initialization.
     */
    public Skin createMinimalVisUISkin() {
        Skin skin = new Skin();
        skin.add("default-font", new BitmapFont());
        return skin;
    }
}
