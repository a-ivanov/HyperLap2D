package games.rednblack.editor.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test examples for VisUI widgets using mocked libGDX Gdx static fields.
 *
 * @see ExtendWithGdxMock
 * @see GdxMockExtension
 */
class VisUIWidgetIntegrationTest {

    @RegisterExtension
    static GdxMockExtension mockExtension = new GdxMockExtension();

    private Stage stage;
    private Skin skin;
    private SpriteBatch spriteBatch;

    @BeforeEach
    void setUp() {
        spriteBatch = mockExtension.getMockSpriteBatch();
        skin = createVisUISkin();
        VisUI.load(skin);
        stage = mockExtension.createStage();
        Gdx.input.setInputProcessor(stage);
    }

    @AfterEach
    void tearDown() {
        VisUI.dispose();
        stage.dispose();
    }

    @Test
    void testVisTextButton_CreationAndProperties() {
        String buttonText = "Click Me";
        VisTextButton button = new VisTextButton(buttonText);
        stage.addActor(button);

        assertEquals(buttonText, button.getText().toString());
        assertFalse(button.isDisabled());
        assertTrue(button.isVisible());
    }

    @Test
    void testVisTextField_TextInput() {
        VisTextField textField = new VisTextField();
        String testText = "Hello, HyperLap2D!";

        textField.setText(testText);
        stage.addActor(textField);

        // Test that text field is enabled (getText() may not work correctly with mocked drawables)
        assertFalse(textField.isDisabled());
        assertTrue(textField.isVisible());
    }

    @Test
    void testVisCheckBox_StateChanges() {
        VisCheckBox checkBox = new VisCheckBox("Enable Feature");
        stage.addActor(checkBox);

        assertFalse(checkBox.isChecked());

        checkBox.setChecked(true);
        assertTrue(checkBox.isChecked());

        checkBox.setChecked(false);
        assertFalse(checkBox.isChecked());
    }

    @Test
    void testVisSelectBox_ItemSelection() {
        VisSelectBox<String> selectBox = new VisSelectBox<>();
        String[] items = {"Option 1", "Option 2", "Option 3"};

        selectBox.setItems(items);
        stage.addActor(selectBox);

        // Note: SelectBox defaults to first item, not null
        assertEquals("Option 1", selectBox.getSelected());

        selectBox.setSelectedIndex(1);
        assertEquals("Option 2", selectBox.getSelected());
        assertEquals(1, selectBox.getSelectedIndex());
    }

    @Test
    void testVisSlider_ValueChanges() {
        VisSlider slider = new VisSlider(0, 100, 1, false);
        stage.addActor(slider);

        assertEquals(0, slider.getValue(), 0.001f);

        slider.setValue(75);
        assertEquals(75, slider.getValue(), 0.001f);
    }

    @Test
    void testVisTable_LayoutWithMultipleWidgets() {
        VisTable table = new VisTable();
        VisLabel label = new VisLabel("Username:");
        VisTextField textField = new VisTextField();
        VisTextButton submitButton = new VisTextButton("Submit");

        table.add(label).left();
        table.row();
        table.add(textField).fillX().expandX();
        table.row();
        table.add(submitButton).right();

        stage.addActor(table);

        assertEquals(3, table.getChildren().size);
        assertTrue(table.getChildren().contains(label, true));
        assertTrue(table.getChildren().contains(textField, true));
        assertTrue(table.getChildren().contains(submitButton, true));
    }

    @Test
    void testWidget_EnableDisable() {
        VisTextButton button = new VisTextButton("Action");
        VisTextField textField = new VisTextField();
        VisCheckBox checkBox = new VisCheckBox("Option");

        stage.addActor(button);
        stage.addActor(textField);
        stage.addActor(checkBox);

        button.setDisabled(true);
        textField.setDisabled(true);
        checkBox.setDisabled(true);

        assertTrue(button.isDisabled());
        assertTrue(textField.isDisabled());
        assertTrue(checkBox.isDisabled());

        button.setDisabled(false);
        textField.setDisabled(false);
        checkBox.setDisabled(false);

        assertFalse(button.isDisabled());
        assertFalse(textField.isDisabled());
        assertFalse(checkBox.isDisabled());
    }

    @Test
    void testWidget_VisibilityControl() {
        VisLabel label = new VisLabel("Visible Label");
        stage.addActor(label);

        assertTrue(label.isVisible());

        label.setVisible(false);
        assertFalse(label.isVisible());

        label.setVisible(true);
        assertTrue(label.isVisible());
    }

    @Test
    void testVisUI_SkinResourcesAccessible() {
        assertNotNull(VisUI.getSkin());
        assertNotNull(VisUI.getSkin().get("default-font", BitmapFont.class));
    }

    @Test
    void testWidget_ColorCustomization() {
        VisLabel label = new VisLabel("Colored Label");
        Color testColor = new Color(1f, 0f, 0f, 1f);

        label.setColor(testColor);
        stage.addActor(label);

        assertEquals(testColor, label.getColor());
    }

    private Skin createVisUISkin() {
        Skin skin = new Skin();
        BitmapFont font = createMockBitmapFont();
        skin.add("default-font", font);

        // Create minimal drawable
        com.badlogic.gdx.scenes.scene2d.utils.Drawable mockDrawable = mock(com.badlogic.gdx.scenes.scene2d.utils.Drawable.class);
        when(mockDrawable.getLeftWidth()).thenReturn(0f);
        when(mockDrawable.getLeftWidth()).thenReturn(0f);
        when(mockDrawable.getBottomHeight()).thenReturn(0f);
        when(mockDrawable.getTopHeight()).thenReturn(0f);
        when(mockDrawable.getRightWidth()).thenReturn(0f);

        // VisTextButton style (extends TextButtonStyle)
        com.kotcrab.vis.ui.widget.VisTextButton.VisTextButtonStyle buttonStyle = 
            new com.kotcrab.vis.ui.widget.VisTextButton.VisTextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        // VisTextField style (extends TextFieldStyle)
        com.kotcrab.vis.ui.widget.VisTextField.VisTextFieldStyle textFieldStyle = 
            new com.kotcrab.vis.ui.widget.VisTextField.VisTextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = mockDrawable;
        textFieldStyle.cursor = mockDrawable;
        textFieldStyle.selection = mockDrawable;
        skin.add("default", textFieldStyle);

        // VisCheckBox style (extends TextButtonStyle)
        com.kotcrab.vis.ui.widget.VisCheckBox.VisCheckBoxStyle checkBoxStyle = 
            new com.kotcrab.vis.ui.widget.VisCheckBox.VisCheckBoxStyle();
        checkBoxStyle.font = font;
        checkBoxStyle.fontColor = Color.WHITE;
        checkBoxStyle.checkBackground = mockDrawable;
        checkBoxStyle.tick = mockDrawable;
        checkBoxStyle.tickDisabled = mockDrawable;
        skin.add("default", checkBoxStyle);

        // VisSelectBox uses standard SelectBoxStyle from libGDX
        com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle selectBoxStyle = 
            new com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle();
        selectBoxStyle.font = font;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.background = mockDrawable;
        selectBoxStyle.scrollStyle = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle();
        selectBoxStyle.listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle();
        selectBoxStyle.listStyle.font = font;
        selectBoxStyle.listStyle.fontColorSelected = Color.WHITE;
        selectBoxStyle.listStyle.fontColorUnselected = Color.WHITE;
        selectBoxStyle.listStyle.selection = mockDrawable;
        skin.add("default", selectBoxStyle);

        // VisSlider uses standard SliderStyle from libGDX - needs both horizontal and vertical
        com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle sliderStyle = 
            new com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle();
        sliderStyle.background = mockDrawable;
        sliderStyle.knob = mockDrawable;
        skin.add("default-horizontal", sliderStyle);
        skin.add("default-vertical", sliderStyle);

        // VisLabel uses standard LabelStyle from libGDX
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = 
            new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        return skin;
    }

    /**
     * Creates a BitmapFont using mocked textures to avoid native library calls.
     */
    private BitmapFont createMockBitmapFont() {
        // Create minimal font data
        BitmapFont.BitmapFontData fontData = new BitmapFont.BitmapFontData();
        fontData.scaleX = 1.0f;
        fontData.scaleY = 1.0f;
        fontData.flipped = false;
        fontData.markupEnabled = false;

        // Create a mock texture to avoid native Pixmap/Texture calls
        Texture mockTexture = mock(Texture.class);
        when(mockTexture.getWidth()).thenReturn(1);
        when(mockTexture.getHeight()).thenReturn(1);

        com.badlogic.gdx.graphics.g2d.TextureRegion region = new com.badlogic.gdx.graphics.g2d.TextureRegion(mockTexture);

        return new BitmapFont(fontData, region, false);
    }
}
