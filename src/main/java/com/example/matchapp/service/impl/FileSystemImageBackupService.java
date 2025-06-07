package com.example.matchapp.service.impl;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.service.ImageBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ImageBackupService that uses the file system for backup and restore operations.
 */
@Service
public class FileSystemImageBackupService implements ImageBackupService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemImageBackupService.class);
    private static final DateTimeFormatter BACKUP_FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String PROFILES_JSON_FILENAME = "profiles_with_images.json";

    private final BackupProperties backupProperties;

    @Autowired
    public FileSystemImageBackupService(BackupProperties backupProperties) {
        this.backupProperties = backupProperties;
    }

    /**
     * Creates a backup of images using the configured backup directory.
     *
     * @param sourceDir the directory containing images to backup
     * @return the number of files backed up
     * @throws IOException if there's an error during the backup process
     */
    @Override
    public int createBackup(Path sourceDir) throws IOException {
        Path backupDir = Paths.get(backupProperties.getBackupDir());
        int filesBackedUp = backupImages(sourceDir, backupDir);

        // Manage maximum number of backups if configured
        if (backupProperties.getMaxBackups() > 0) {
            cleanupOldBackups(backupDir, backupProperties.getMaxBackups());
        }

        return filesBackedUp;
    }

    @Override
    public int backupImages(Path sourceDir, Path backupDir) throws IOException {
        logger.info("Starting backup of images from {} to {}", sourceDir, backupDir);

        // Create a timestamped backup folder
        String timestamp = LocalDateTime.now().format(BACKUP_FOLDER_FORMAT);
        Path backupFolderPath = backupDir.resolve(timestamp);
        Files.createDirectories(backupFolderPath);

        logger.debug("Created backup folder: {}", backupFolderPath);

        // Copy all files from source directory to backup directory
        final int[] fileCount = {0};

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Get relative path from source directory
                Path relativePath = sourceDir.relativize(file);
                Path targetPath = backupFolderPath.resolve(relativePath);

                // Create parent directories if they don't exist
                Files.createDirectories(targetPath.getParent());

                // Copy the file
                Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                fileCount[0]++;

                logger.debug("Backed up file: {}", relativePath);
                return FileVisitResult.CONTINUE;
            }
        });

        logger.info("Backup completed. {} files backed up to {}", fileCount[0], backupFolderPath);
        return fileCount[0];
    }

    /**
     * Restores images from a backup directory to the target directory.
     *
     * @param backupDir the directory containing the backup
     * @param targetDir the directory where images will be restored
     * @param overwrite whether to overwrite existing files in the target directory
     * @return the number of files restored
     * @throws IOException if there's an error during the restore process
     */
    @Override
    public int restoreImages(Path backupDir, Path targetDir, boolean overwrite) throws IOException {
        logger.info("Starting restore of images from {} to {}", backupDir, targetDir);

        // Create target directory if it doesn't exist
        Files.createDirectories(targetDir);

        // Copy all files from backup directory to target directory
        final int[] fileCount = {0};

        Files.walkFileTree(backupDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Get relative path from backup directory
                Path relativePath = backupDir.relativize(file);
                Path targetPath = targetDir.resolve(relativePath);

                // Create parent directories if they don't exist
                Files.createDirectories(targetPath.getParent());

                // Check if file exists and if we should overwrite
                if (!Files.exists(targetPath) || overwrite) {
                    // Copy the file
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    fileCount[0]++;
                    logger.debug("Restored file: {}", relativePath);
                } else {
                    logger.debug("Skipped existing file: {}", relativePath);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        logger.info("Restore completed. {} files restored to {}", fileCount[0], targetDir);
        return fileCount[0];
    }

    /**
     * Lists available backups.
     *
     * @param backupsRootDir the root directory where backups are stored
     * @return an array of paths to available backups
     * @throws IOException if there's an error accessing the backups directory
     */
    @Override
    public Path[] listBackups(Path backupsRootDir) throws IOException {
        logger.info("Listing backups in {}", backupsRootDir);

        // Create the backups directory if it doesn't exist
        if (!Files.exists(backupsRootDir)) {
            Files.createDirectories(backupsRootDir);
            logger.debug("Created backups directory: {}", backupsRootDir);
            return new Path[0];
        }

        // List all directories in the backups root directory
        List<Path> backupFolders = Files.list(backupsRootDir)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());

        logger.info("Found {} backups", backupFolders.size());
        return backupFolders.toArray(new Path[0]);
    }

    /**
     * Lists available backups using the configured backup directory.
     *
     * @return an array of paths to available backups
     * @throws IOException if there's an error accessing the backups directory
     */
    @Override
    public Path[] listBackups() throws IOException {
        return listBackups(Paths.get(backupProperties.getBackupDir()));
    }

    /**
     * Restores images from the most recent backup to the target directory.
     *
     * @param targetDir the directory where images will be restored
     * @return the number of files restored, or -1 if no backups are available
     * @throws IOException if there's an error during the restore process
     */
    @Override
    public int restoreFromLatestBackup(Path targetDir) throws IOException {
        Path backupsRootDir = Paths.get(backupProperties.getBackupDir());
        Path[] backups = listBackups(backupsRootDir);

        if (backups.length == 0) {
            logger.warn("No backups available to restore from");
            return -1;
        }

        // Find the most recent backup (assuming timestamp-based folder names)
        Path latestBackup = backups[backups.length - 1];
        for (Path backup : backups) {
            if (backup.getFileName().toString().compareTo(latestBackup.getFileName().toString()) > 0) {
                latestBackup = backup;
            }
        }

        logger.info("Restoring from latest backup: {}", latestBackup);
        return restoreImages(latestBackup, targetDir, backupProperties.isDefaultOverwrite());
    }

    /**
     * Deletes old backups when the maximum number of backups is reached.
     *
     * @param backupsRootDir the root directory where backups are stored
     * @param maxBackups the maximum number of backups to keep
     * @throws IOException if there's an error accessing or deleting backups
     */
    private void cleanupOldBackups(Path backupsRootDir, int maxBackups) throws IOException {
        logger.debug("Checking if old backups need to be cleaned up (max: {})", maxBackups);

        Path[] backups = listBackups(backupsRootDir);
        if (backups.length <= maxBackups) {
            logger.debug("No cleanup needed, {} backups exist (max: {})", backups.length, maxBackups);
            return;
        }

        // Sort backups by name (which should be timestamp-based)
        List<Path> backupsList = List.of(backups);
        List<Path> sortedBackups = backupsList.stream()
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .collect(Collectors.toList());

        // Calculate how many backups to delete
        int backupsToDelete = backups.length - maxBackups;
        logger.info("Cleaning up {} old backups to maintain maximum of {}", backupsToDelete, maxBackups);

        // Delete the oldest backups
        for (int i = 0; i < backupsToDelete; i++) {
            Path backupToDelete = sortedBackups.get(i);
            deleteDirectory(backupToDelete);
            logger.info("Deleted old backup: {}", backupToDelete);
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param directory the directory to delete
     * @throws IOException if there's an error deleting the directory
     */
    private void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
