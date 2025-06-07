package com.example.matchapp.service.impl;

import com.example.matchapp.config.BackupProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemImageBackupServiceTest {

    private FileSystemImageBackupService backupService;
    private BackupProperties backupProperties;

    @TempDir
    Path tempSourceDir;

    @TempDir
    Path tempBackupDir;

    @BeforeEach
    void setUp() {
        backupProperties = new BackupProperties();
        backupProperties.setBackupDir(tempBackupDir.toString());
        backupProperties.setMaxBackups(3);
        backupProperties.setAutoBackup(true);
        backupProperties.setDefaultOverwrite(false);

        backupService = new FileSystemImageBackupService(backupProperties);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up any files created during tests
        if (Files.exists(tempSourceDir)) {
            Files.walk(tempSourceDir)
                    .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        }

        if (Files.exists(tempBackupDir)) {
            Files.walk(tempBackupDir)
                    .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        }
    }

    @Test
    void backupImages_shouldCopyAllFilesFromSourceToBackup() throws IOException {
        // Arrange
        createTestFiles(tempSourceDir, "test1.jpg", "test2.jpg", "profiles_with_images.json");

        // Act
        int count = backupService.backupImages(tempSourceDir, tempBackupDir);

        // Assert
        assertEquals(3, count, "Should have backed up 3 files");
        
        // Verify files were copied to a timestamped subdirectory
        Path[] backups = backupService.listBackups(tempBackupDir);
        assertEquals(1, backups.length, "Should have created 1 backup");
        
        // Check that all files were copied
        List<Path> backupFiles = Files.walk(backups[0])
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
        assertEquals(3, backupFiles.size(), "Backup should contain 3 files");
    }

    @Test
    void createBackup_shouldUseConfiguredBackupDirectory() throws IOException {
        // Arrange
        createTestFiles(tempSourceDir, "test1.jpg", "test2.jpg");

        // Act
        int count = backupService.createBackup(tempSourceDir);

        // Assert
        assertEquals(2, count, "Should have backed up 2 files");
        
        // Verify files were copied to the configured backup directory
        Path[] backups = backupService.listBackups();
        assertEquals(1, backups.length, "Should have created 1 backup");
    }

    @Test
    void restoreImages_shouldCopyFilesFromBackupToTarget() throws IOException {
        // Arrange
        createTestFiles(tempSourceDir, "test1.jpg", "test2.jpg");
        backupService.backupImages(tempSourceDir, tempBackupDir);
        
        // Delete source files
        Files.deleteIfExists(tempSourceDir.resolve("test1.jpg"));
        Files.deleteIfExists(tempSourceDir.resolve("test2.jpg"));
        
        // Get the backup directory
        Path[] backups = backupService.listBackups(tempBackupDir);
        
        // Act
        int count = backupService.restoreImages(backups[0], tempSourceDir, true);
        
        // Assert
        assertEquals(2, count, "Should have restored 2 files");
        assertTrue(Files.exists(tempSourceDir.resolve("test1.jpg")), "test1.jpg should be restored");
        assertTrue(Files.exists(tempSourceDir.resolve("test2.jpg")), "test2.jpg should be restored");
    }

    @Test
    void cleanupOldBackups_shouldDeleteOldestBackupsWhenLimitReached() throws IOException {
        // Arrange - Create multiple backups
        createTestFiles(tempSourceDir, "test.jpg");
        
        // Create 5 backups (exceeding the max of 3)
        for (int i = 0; i < 5; i++) {
            backupService.backupImages(tempSourceDir, tempBackupDir);
            // Sleep a bit to ensure different timestamps
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Act - This will trigger cleanup as part of the backup process
        backupService.createBackup(tempSourceDir);
        
        // Assert
        Path[] backups = backupService.listBackups();
        assertEquals(3, backups.length, "Should have kept only 3 backups (max limit)");
    }

    private void createTestFiles(Path directory, String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            Path filePath = directory.resolve(fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, ("Test content for " + fileName).getBytes());
        }
    }
}