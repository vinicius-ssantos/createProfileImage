package com.example.matchapp.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Service for backing up and restoring generated profile images.
 */
public interface ImageBackupService {

    /**
     * Creates a backup of all images in the specified source directory.
     *
     * @param sourceDir the directory containing images to backup
     * @param backupDir the directory where the backup will be stored
     * @return the number of files backed up
     * @throws IOException if there's an error during the backup process
     */
    int backupImages(Path sourceDir, Path backupDir) throws IOException;

    /**
     * Creates a backup of images using the configured backup directory.
     *
     * @param sourceDir the directory containing images to backup
     * @return the number of files backed up
     * @throws IOException if there's an error during the backup process
     */
    int createBackup(Path sourceDir) throws IOException;

    /**
     * Restores images from a backup directory to the target directory.
     *
     * @param backupDir the directory containing the backup
     * @param targetDir the directory where images will be restored
     * @param overwrite whether to overwrite existing files in the target directory
     * @return the number of files restored
     * @throws IOException if there's an error during the restore process
     */
    int restoreImages(Path backupDir, Path targetDir, boolean overwrite) throws IOException;

    /**
     * Lists available backups.
     *
     * @param backupsRootDir the root directory where backups are stored
     * @return an array of paths to available backups
     * @throws IOException if there's an error accessing the backups directory
     */
    Path[] listBackups(Path backupsRootDir) throws IOException;

    /**
     * Lists available backups using the configured backup directory.
     *
     * @return an array of paths to available backups
     * @throws IOException if there's an error accessing the backups directory
     */
    Path[] listBackups() throws IOException;

    /**
     * Restores images from the most recent backup to the target directory.
     *
     * @param targetDir the directory where images will be restored
     * @return the number of files restored, or -1 if no backups are available
     * @throws IOException if there's an error during the restore process
     */
    int restoreFromLatestBackup(Path targetDir) throws IOException;
}
