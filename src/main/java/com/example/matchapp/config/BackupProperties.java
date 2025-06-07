package com.example.matchapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for image backup and restore operations.
 */
@Configuration
@ConfigurationProperties(prefix = "backup")
public class BackupProperties {

    /**
     * Root directory for storing image backups.
     */
    private String backupDir = "backups/images";

    /**
     * Whether to automatically backup images after generation.
     */
    private boolean autoBackup = false;

    /**
     * Maximum number of backups to keep. Older backups will be deleted when this limit is reached.
     * Set to 0 for unlimited backups.
     */
    private int maxBackups = 10;

    /**
     * Whether to overwrite existing files during restore operations by default.
     */
    private boolean defaultOverwrite = false;

    public String getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(String backupDir) {
        this.backupDir = backupDir;
    }

    public boolean isAutoBackup() {
        return autoBackup;
    }

    public void setAutoBackup(boolean autoBackup) {
        this.autoBackup = autoBackup;
    }

    public int getMaxBackups() {
        return maxBackups;
    }

    public void setMaxBackups(int maxBackups) {
        this.maxBackups = maxBackups;
    }

    public boolean isDefaultOverwrite() {
        return defaultOverwrite;
    }

    public void setDefaultOverwrite(boolean defaultOverwrite) {
        this.defaultOverwrite = defaultOverwrite;
    }
}