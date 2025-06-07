package com.example.matchapp.controller;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.service.ImageBackupService;
import com.example.matchapp.service.impl.FileSystemImageBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing image backups.
 */
@RestController
@RequestMapping("/backups")
@Tag(name = "Backup Operations", description = "API endpoints for managing image backups")
public class BackupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);
    private static final String DEFAULT_IMAGE_DIR = "src/main/resources/static/images";

    private final ImageBackupService backupService;
    private final BackupProperties backupProperties;

    public BackupController(ImageBackupService backupService, BackupProperties backupProperties) {
        this.backupService = backupService;
        this.backupProperties = backupProperties;
    }

    /**
     * Create a backup of all images.
     *
     * @return information about the created backup
     */
    @PostMapping
    @Operation(summary = "Create a backup", description = "Creates a backup of all generated profile images")
    @ApiResponse(responseCode = "200", description = "Backup created successfully")
    @ApiResponse(responseCode = "500", description = "Error creating backup")
    public ResponseEntity<Map<String, Object>> createBackup() {
        try {
            Path sourceDir = Paths.get(DEFAULT_IMAGE_DIR);
            int filesBackedUp = backupService.createBackup(sourceDir);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Backup created successfully");
            response.put("filesBackedUp", filesBackedUp);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error creating backup", e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error creating backup: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * List all available backups.
     *
     * @return a list of available backups
     */
    @GetMapping
    @Operation(summary = "List backups", description = "Lists all available backups")
    @ApiResponse(responseCode = "200", description = "Backups listed successfully")
    @ApiResponse(responseCode = "500", description = "Error listing backups")
    public ResponseEntity<Map<String, Object>> listBackups() {
        try {
            Path[] backups = backupService.listBackups(Paths.get(backupProperties.getBackupDir()));

            List<String> backupNames = Arrays.stream(backups)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("backups", backupNames);
            response.put("count", backupNames.size());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error listing backups", e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error listing backups: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Restore images from a backup.
     *
     * @param backupName the name of the backup to restore from
     * @param overwrite whether to overwrite existing files
     * @return information about the restore operation
     */
    @PostMapping("/restore")
    @Operation(summary = "Restore from backup", description = "Restores images from a specified backup")
    @ApiResponse(responseCode = "200", description = "Restore completed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid backup name")
    @ApiResponse(responseCode = "404", description = "Backup not found")
    @ApiResponse(responseCode = "500", description = "Error during restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(
            @RequestParam(required = false) String backupName,
            @RequestParam(defaultValue = "false") boolean overwrite) {

        try {
            Path backupDir;
            Path targetDir = Paths.get(DEFAULT_IMAGE_DIR);

            if (backupName == null || backupName.isEmpty()) {
                // Restore from latest backup
                int filesRestored = backupService.restoreFromLatestBackup(targetDir);

                if (filesRestored == -1) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "No backups available to restore from");
                    return ResponseEntity.status(404).body(response);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Restored from latest backup");
                response.put("filesRestored", filesRestored);

                return ResponseEntity.ok(response);
            } else {
                // Restore from specified backup
                Path backupsRootDir = Paths.get(backupProperties.getBackupDir());
                backupDir = backupsRootDir.resolve(backupName);

                if (!backupDir.toFile().exists() || !backupDir.toFile().isDirectory()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Backup not found: " + backupName);
                    return ResponseEntity.status(404).body(response);
                }

                int filesRestored = backupService.restoreImages(backupDir, targetDir, overwrite);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Restore completed successfully");
                response.put("backupName", backupName);
                response.put("filesRestored", filesRestored);

                return ResponseEntity.ok(response);
            }
        } catch (IOException e) {
            logger.error("Error restoring from backup", e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error restoring from backup: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
