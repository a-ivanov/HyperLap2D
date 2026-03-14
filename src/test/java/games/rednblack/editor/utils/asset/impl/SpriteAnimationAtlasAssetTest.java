package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.test.ExtendWithFacadeProxies;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWithFacadeProxies
@ExtendWith(MockitoExtension.class)
class SpriteAnimationAtlasAssetTest {

    @Mock
    private ProgressHandler progressHandler;

    private final ProjectManager projectManager;
    private final ResolutionManager resolutionManager;

    SpriteAnimationAtlasAssetTest(ProjectManager projectManager, ResolutionManager resolutionManager) {
        this.projectManager = projectManager;
        this.resolutionManager = resolutionManager;
    }

    @Test
    void matchMimeType_returnsTrueForValidAtlas() {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        File atlasFile = getResourceFile("projects/spriteAtlasProject/assets/sprite-animations/walk/walk.atlas");
        FileHandle handle = new FileHandle(atlasFile);

        assertTrue(asset.matchMimeType(handle));
    }

    @Test
    void matchMimeType_returnsFalseForInvalidOrCorruptAtlas() {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        File invalidFile = getResourceFile("projects/spriteAtlasProject/assets/sprite-animations/walk/empty.atlas");
        File corruptFile = getResourceFile("projects/spriteAtlasProject/assets/sprite-animations/walk/corrupt.atlas");

        assertFalse(asset.matchMimeType(new FileHandle(invalidFile)));
        assertFalse(asset.matchMimeType(new FileHandle(corruptFile)));
    }

    @Test
    void checkExistence_returnsTrueWhenAtlasExists(@TempDir File projectRoot) throws IOException {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        when(projectManager.getCurrentProjectPath()).thenReturn(projectRoot.getAbsolutePath());

        File spriteDir = new File(projectRoot, ProjectManager.SPRITE_DIR_PATH + "/walk");
        File atlas = new File(spriteDir, "walk.atlas");
        FileUtils.forceMkdir(spriteDir);
        FileUtils.writeStringToFile(atlas, "dummy", StandardCharsets.UTF_8);

        Array<FileHandle> files = new Array<>();
        files.add(new FileHandle(new File("walk.atlas")));

        assertTrue(asset.checkExistence(files));
    }

    @Test
    void checkExistence_returnsFalseWhenNoAtlasExists() {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        when(projectManager.getCurrentProjectPath()).thenReturn(new File(".").getAbsolutePath());

        Array<FileHandle> files = new Array<>();
        files.add(new FileHandle(new File("missing.atlas")));

        assertFalse(asset.checkExistence(files));
    }

    @Test
    void checkExistence_returnsFalseForEmptyArray() {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        Array<FileHandle> files = new Array<>();

        assertFalse(asset.checkExistence(files));
    }

    @Test
    void importAsset_happyPathCopiesFilesAndUpdatesProjectInfo(@TempDir File tmpDir) throws IOException {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        File projectRoot = new File(tmpDir, "project");
        FileUtils.forceMkdir(projectRoot);

        File walkResourceDir = getResourceFile("projects/spriteAtlasProject/assets/sprite-animations/walk").getAbsoluteFile();
        File atlasSource = new File(walkResourceDir, "walk.atlas");
        File atlasTargetDir = new File(projectRoot, ProjectManager.SPRITE_DIR_PATH);
        FileUtils.forceMkdir(atlasTargetDir);
        File atlasFile = new File(atlasTargetDir, "walk.atlas");
        FileUtils.copyFile(atlasSource, atlasFile);
        BufferedImage walkPng = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(walkPng, "png", new File(atlasTargetDir, "walk.png"));

        when(projectManager.getCurrentProjectPath()).thenReturn(projectRoot.getAbsolutePath());

        ProjectInfoVO projectInfoVO = new ProjectInfoVO();
        TexturePackVO mainPack = new TexturePackVO();
        mainPack.name = "main";
        projectInfoVO.animationsPacks.put("main", mainPack);
        when(projectManager.getCurrentProjectInfoVO()).thenReturn(projectInfoVO);

        Array<FileHandle> files = new Array<>();
        files.add(new FileHandle(atlasFile));

        asset.importAsset(files, progressHandler, false);

        verify(resolutionManager).rePackProjectImagesForAllResolutionsSync();
        verify(progressHandler, never()).progressFailed();
    }

    @Test
    void importAsset_onIOExceptionSignalsFailure(@TempDir File tmpDir) throws IOException {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        // Project path points to a *file*, so creating "tmp" under it fails with IOException.
        File projectAsFile = new File(tmpDir, "project");
        projectAsFile.createNewFile();

        when(projectManager.getCurrentProjectPath()).thenReturn(projectAsFile.getAbsolutePath());

        File atlasFile = getResourceFile("projects/spriteAtlasProject/assets/sprite-animations/walk/walk.atlas");
        Array<FileHandle> files = new Array<>();
        files.add(new FileHandle(atlasFile));

        asset.importAsset(files, progressHandler, false);

        verify(progressHandler).progressFailed();
        verify(resolutionManager, never()).rePackProjectImagesForAllResolutionsSync();
    }

    @Test
    void deleteAsset_successfulDeletionAcrossResolutions(@TempDir File projectRoot) throws IOException {
        File spriteWalkDir = new File(projectRoot, ProjectManager.SPRITE_DIR_PATH + "/walk");
        FileUtils.forceMkdir(spriteWalkDir);
        FileUtils.forceMkdir(new File(projectRoot, "assets/orig/images"));

        ProjectInfoVO projectInfoVO = new ProjectInfoVO();
        projectInfoVO.animationsPacks.put("main", new TexturePackVO());
        when(projectManager.getCurrentProjectInfoVO()).thenReturn(projectInfoVO);
        when(projectManager.getCurrentProjectPath()).thenReturn(projectRoot.getAbsolutePath());

        // Use subclass to avoid Sandbox/UIStageMediator in unit test while still testing delete flow
        SpriteAnimationAtlasAsset deleteAsset = new SpriteAnimationAtlasAsset() {
            @Override
            protected void postDeleteSpriteAnimation(int root, String spriteAnimationName) {}
        };
        boolean result = deleteAsset.deleteAsset(1, "walk");

        assertTrue(result);
    }

    @Test
    void deleteAsset_returnsFalseWhenDirectoryDeletionFails(@TempDir File projectRoot) {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        ProjectInfoVO projectInfoVO = new ProjectInfoVO();
        ResolutionEntryVO res1 = new ResolutionEntryVO();
        res1.name = "orig";
        projectInfoVO.resolutions.add(res1);
        projectInfoVO.animationsPacks.put("main", new TexturePackVO());
        when(projectManager.getCurrentProjectInfoVO()).thenReturn(projectInfoVO);
        when(projectManager.getCurrentProjectPath()).thenReturn(projectRoot.getAbsolutePath());

        boolean result = asset.deleteAsset(1, "walk");

        assertFalse(result);
    }

    @Test
    void exportAsset_copiesAtlasDirectoryAndRegistersExportedAsset(@TempDir File projectRoot,
                                                                   @TempDir File tmpDir) throws IOException {
        SpriteAnimationAtlasAsset asset = new SpriteAnimationAtlasAsset();
        File spriteDir = new File(projectRoot, ProjectManager.SPRITE_DIR_PATH + "/walk");
        FileUtils.forceMkdir(spriteDir);
        File atlasFile = new File(spriteDir, "walk.atlas");
        FileUtils.writeStringToFile(atlasFile, "dummy", StandardCharsets.UTF_8);

        when(projectManager.getCurrentProjectPath()).thenReturn(projectRoot.getAbsolutePath() + File.separator);

        SpriteAnimationVO spriteAnimationVO = new SpriteAnimationVO();
        spriteAnimationVO.animationName = "walk";
        ExportMapperVO exportMapperVO = new ExportMapperVO();

        boolean result = asset.exportAsset(spriteAnimationVO, exportMapperVO, tmpDir);

        assertTrue(result);
        assertFalse(exportMapperVO.mapper.isEmpty());
        ExportMapperVO.ExportedAsset exported = exportMapperVO.mapper.get(0);
        assertEquals(AssetsUtils.TYPE_SPRITE_ANIMATION_ATLAS, exported.type);
        assertEquals("walk.atlas", exported.fileName);
    }

    private File getResourceFile(String relativePath) {
        File base = new File("src/test/resources");
        return new File(base, relativePath);
    }
}
