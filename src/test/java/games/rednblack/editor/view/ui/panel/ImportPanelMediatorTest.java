package games.rednblack.editor.view.ui.panel;

import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.util.Interests;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;

/**
 * Unit tests for {@link ImportPanelMediator#listNotificationInterests(Interests)}.
 * <p>
 * Uses mocked construction of {@link ImportPanel} to avoid VisUI initialization
 * requirements in unit tests.
 */
class ImportPanelMediatorTest {

    @Test
    void listNotificationInterests_registersExpectedNotifications() {
        // Given
        ImportPanelMediator mediator;
        try (MockedConstruction<ImportPanel> mocked = mockConstruction(ImportPanel.class)) {
            mediator = new ImportPanelMediator();
        }
        Interests interests = new Interests();

        // When
        mediator.listNotificationInterests(interests);

        // Then
        assertEquals(4, interests.size, "Should register exactly 4 notification interests");
        assertTrue(interests.contains(ResourcesMenu.IMPORT_TO_LIBRARY, false),
                "Should register IMPORT_TO_LIBRARY notification");
        assertTrue(interests.contains(ImportPanel.BROWSE_BTN_CLICKED, false),
                "Should register BROWSE_BTN_CLICKED notification");
        assertTrue(interests.contains(ImportPanel.IMPORT_FAILED, false),
                "Should register IMPORT_FAILED notification");
        assertTrue(interests.contains(MsgAPI.ACTION_FILES_DROPPED, false),
                "Should register ACTION_FILES_DROPPED notification");
    }

    @Test
    void listNotificationInterests_addsNotificationsInCorrectOrder() {
        // Given
        ImportPanelMediator mediator;
        try (MockedConstruction<ImportPanel> mocked = mockConstruction(ImportPanel.class)) {
            mediator = new ImportPanelMediator();
        }
        Interests interests = new Interests();

        // When
        mediator.listNotificationInterests(interests);

        // Then
        assertEquals(ResourcesMenu.IMPORT_TO_LIBRARY, interests.get(0));
        assertEquals(ImportPanel.BROWSE_BTN_CLICKED, interests.get(1));
        assertEquals(ImportPanel.IMPORT_FAILED, interests.get(2));
        assertEquals(MsgAPI.ACTION_FILES_DROPPED, interests.get(3));
    }
}
