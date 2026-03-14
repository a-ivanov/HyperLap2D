# VisUI Widget Integration Testing Guide

## Overview

This guide explains how to write integration tests for VisUI widgets in HyperLap2D without requiring a real graphics context. The solution uses Mockito to mock libGDX's `Gdx` class static fields.

## Problem

libGDX's `Gdx` class contains static fields that reference platform-specific implementations:

```java
public class Gdx {
    public static Application app;
    public static Graphics graphics;
    public static Input input;
    public static Audio audio;
    public static Files files;
    public static Net net;
    public static GL20 gl;
    // ...
}
```

VisUI widgets depend on these static fields for:
- Texture loading (`Gdx.graphics`)
- Input handling (`Gdx.input`)
- File access (`Gdx.files`)
- OpenGL calls (`Gdx.gl`)

Running tests without a real graphics context causes `NullPointerException`.

## Solution

The `GdxMockExtension` JUnit 5 extension mocks all `Gdx` static fields using Mockito before each test and restores them afterward.

## Files Created

| File | Purpose |
|------|---------|
| `src/test/java/games/rednblack/editor/test/GdxMockExtension.java` | JUnit 5 extension that mocks all Gdx static fields using Mockito |
| `src/test/java/games/rednblack/editor/test/ExtendWithGdxMock.java` | Convenience annotation for test classes |
| `src/test/java/games/rednblack/editor/test/VisUIWidgetIntegrationTest.java` | Example test class with comprehensive examples |

## Usage

### Basic Setup

```java
@ExtendWithGdxMock
class MyVisUiTest {

    private Stage stage;
    private Skin skin;

    @BeforeEach
    void setUp() {
        // Create minimal skin for VisUI
        skin = new Skin();
        skin.add("default-font", new BitmapFont());
        
        // Initialize VisUI
        VisUI.load(skin);
        
        // Create stage for widget rendering
        stage = new Stage();
    }

    @AfterEach
    void tearDown() {
        VisUI.dispose();
        stage.dispose();
    }

    @Test
    void testWidget() {
        VisTextButton button = new VisTextButton("Click me");
        stage.addActor(button);
        
        assertEquals("Click me", button.getText().toString());
    }
}
```

### Using with Existing Test Infrastructure

Combine with `FacadeProxiesExtension` for testing UI components that depend on HyperLap2D proxies:

```java
@ExtendWithFacadeProxies
@ExtendWith(GdxMockExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class MyComponentTest {

    @Mock
    private ProgressHandler progressHandler;

    private Stage stage;

    MyComponentTest(ProjectManager projectManager, 
                    ResolutionManager resolutionManager) {
        // Injected by FacadeProxiesExtension
    }

    @BeforeEach
    void setUp() {
        GdxMockExtension mockExtension = new GdxMockExtension();
        stage = mockExtension.createStage();
        VisUI.load(mockExtension.createMinimalVisUISkin());
    }

    @AfterEach
    void tearDown() {
        VisUI.dispose();
        stage.dispose();
    }

    @Test
    void testComponentWithDependencies() {
        // Test code using both mocked Gdx and mocked proxies
    }
}
```

## What Gets Mocked

### Gdx.app (Application)
- Preferences storage (in-memory)
- Log methods (print to System.out)
- Application lifecycle (no-op)
- Display mode (800x600, 60Hz)

### Gdx.graphics (Graphics)
- Dimensions (800x600)
- Delta time (1/60s)
- FPS (60)
- All OpenGL methods (no-op)
- Feature support queries (return true)

### Gdx.input (Input)
- Mouse position (400, 300)
- Key/button state tracking
- Input processor (stored reference)
- Vibration (no-op)

### Gdx.audio (Audio)
- Music (no-op implementation)
- Sound (no-op implementation)
- Audio devices (no-op)

### Gdx.files (Files)
- FileHandle creation (real File objects)
- Internal files resolve to `assets/` directory

### Gdx.net (Net)
- HTTP requests (no-op)
- Sockets (mock implementations)

### Gdx.gl / Gdx.gl20 / Gdx.gl30 (GL20)
- All OpenGL calls (no-op)
- Returns safe default values

## Helper Methods

`GdxMockExtension` provides helper methods for common test setups:

### createStage()
Creates a `Stage` with a mocked `SpriteBatch` for widget rendering:

```java
GdxMockExtension mockExtension = new GdxMockExtension();
Stage stage = mockExtension.createStage();
```

### createMinimalVisUISkin()
Creates a minimal `Skin` with default font and white pixel texture:

```java
Skin skin = mockExtension.createMinimalVisUISkin();
VisUI.load(skin);
```

## Important Notes

### Thread Safety
**Not safe for parallel execution!** The `Gdx` class is a global singleton. Always use:

```java
@ExtendWith(GdxMockExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
```

Or use the convenience annotation:

```java
@ExtendWithGdxMock  // Includes SAME_THREAD execution
```

### VisUI Cleanup
Always call `VisUI.dispose()` after each test to prevent state leakage:

```java
@AfterEach
void tearDown() {
    VisUI.dispose();
    stage.dispose();
}
```

### Skin Requirements
VisUI requires certain styles to be present in the skin. At minimum:

```java
Skin skin = new Skin();
skin.add("default-font", new BitmapFont());

// Add white pixel for backgrounds
Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
pixmap.setColor(Color.WHITE);
pixmap.fill();
Texture texture = new Texture(pixmap);
pixmap.dispose();

TextureAtlas.AtlasRegion region = new TextureAtlas.AtlasRegion(
    new TextureRegion(texture), 0, 0, 1, 1);
region.name = "white-pixel";

TextureAtlas atlas = new TextureAtlas();
atlas.addRegion("white-pixel", region);
skin.addRegions(atlas);

VisUI.load(skin);
```

### Limitations

1. **No Real Rendering**: Widgets are created and can be tested, but no actual rendering occurs.

2. **No File I/O**: File operations use real `File` objects but won't access classpath resources unless explicitly set up.

3. **No Real Input**: Input events must be simulated programmatically (e.g., calling `fire()` on buttons).

4. **No Shaders**: OpenGL shader operations are no-ops.

5. **No Texture Loading**: Textures must be created programmatically (Pixmap → Texture).

## Example Test Patterns

### Testing Widget Properties

```java
@Test
void testWidgetProperties() {
    VisTextField textField = new VisTextField();
    textField.setText("Hello");
    textField.setDisabled(true);
    
    assertEquals("Hello", textField.getText());
    assertTrue(textField.isDisabled());
}
```

### Testing Widget Interactions

```java
@Test
void testButtonInteraction() {
    VisTextButton button = new VisTextButton("Click");
    final boolean[] clicked = {false};
    
    button.addListener(new ChangeListener() {
        public void changed(ChangeEvent event, Actor actor) {
            clicked[0] = true;
        }
    });
    
    stage.addActor(button);
    button.fire(new ChangeEvent());  // Simulate click
    
    assertTrue(clicked[0]);
}
```

### Testing Layout

```java
@Test
void testTableLayout() {
    VisTable table = new VisTable();
    table.add(new VisLabel("Label"));
    table.add(new VisTextField()).expandX().fillX();
    
    stage.addActor(table);
    stage.act();
    stage.draw();  // Triggers layout calculation
    
    assertTrue(table.getWidth() > 0);
}
```

## Dependencies

Ensure these test dependencies are in `build.gradle`:

```groovy
testImplementation "org.junit.jupiter:junit-jupiter:$junitVersion"
testImplementation "org.mockito:mockito-core:$mockitoVersion"
testImplementation "org.mockito:mockito-junit-jupiter:$mockitoVersion"
```

## Troubleshooting

### NullPointerException in VisUI widget
**Cause**: VisUI not initialized or skin missing required styles.

**Solution**:
```java
@BeforeEach
void setUp() {
    Skin skin = new Skin();
    skin.add("default-font", new BitmapFont());
    // Add more styles as needed
    VisUI.load(skin);
}
```

### Test fails with "Cannot run with CONCURRENT"
**Cause**: Gdx is a global singleton, tests must run sequentially.

**Solution**: Add `@Execution(ExecutionMode.SAME_THREAD)` or use `@ExtendWithGdxMock`.

### State leakage between tests
**Cause**: VisUI or Stage not properly disposed.

**Solution**: Always dispose in `@AfterEach`:
```java
@AfterEach
void tearDown() {
    VisUI.dispose();
    stage.dispose();
}
```

## See Also

- [VisUI GitHub Repository](https://github.com/kotcrab/vis-ui)
- [libGDX Testing Wiki](https://github.com/libgdx/libgdx/wiki/Unit-testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/)
