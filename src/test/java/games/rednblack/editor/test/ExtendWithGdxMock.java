package games.rednblack.editor.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Convenience annotation that combines {@link GdxMockExtension} with
 * {@link org.junit.jupiter.api.parallel.ExecutionMode#SAME_THREAD} to ensure
 * safe execution of tests that mock libGDX's global {@link com.badlogic.gdx.Gdx} static fields.
 *
 * <p>Use this annotation on test classes that need to test VisUI widgets or other
 * libGDX-dependent code without a real graphics context.
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * @ExtendWithGdxMock
 * class VisTextButtonTest {
 *
 *     private Stage stage;
 *     private Skin skin;
 *
 *     @BeforeEach
 *     void setUp() {
 *         GdxMockExtension mockExtension = new GdxMockExtension();
 *         skin = mockExtension.createMinimalVisUISkin();
 *         VisUI.load(skin);
 *         stage = mockExtension.createStage();
 *     }
 *
 *     @AfterEach
 *     void tearDown() {
 *         VisUI.dispose();
 *         stage.dispose();
 *     }
 *
 *     @Test
 *     void testButtonCreation() {
 *         VisTextButton button = new VisTextButton("Click me");
 *         stage.addActor(button);
 *
 *         // Test button properties
 *         assertEquals("Click me", button.getText().toString());
 *     }
 * }
 * }</pre>
 *
 * @see GdxMockExtension
 * @see com.badlogic.gdx.Gdx
 * @see com.kotcrab.vis.ui.VisUI
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(GdxMockExtension.class)
public @interface ExtendWithGdxMock {
}
