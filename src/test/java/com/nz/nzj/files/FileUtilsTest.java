package com.nz.nzj.files;

import com.nz.nzj.file.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {

    @Test
    void getIncFile_addsIncrementAndExtension(@TempDir Path tempDir) {
        File incFile = FileUtils.getIncFile(tempDir.toFile(), "report.txt");

        assertEquals(tempDir.resolve("report-1.txt").toFile(), incFile);
        assertFalse(incFile.exists(), "getIncFile should not create the file on disk");
    }

    @Test
    void getIncFile_skipsExistingFiles(@TempDir Path tempDir) throws IOException {
        assertTrue(tempDir.resolve("report-1.txt").toFile().createNewFile());
        assertTrue(tempDir.resolve("report-2.txt").toFile().createNewFile());

        File incFile = FileUtils.getIncFile(tempDir.toFile(), "report.txt");

        assertEquals(tempDir.resolve("report-3.txt").toFile(), incFile);
    }

    @Test
    void getIncFile_handlesFilesWithoutExtension(@TempDir Path tempDir) {
        File incFile = FileUtils.getIncFile(tempDir.toFile(), "archive");

        assertEquals(tempDir.resolve("archive-1").toFile(), incFile);
    }
}
