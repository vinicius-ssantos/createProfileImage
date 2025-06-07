# Image Backup and Restore

This document describes the backup and restore functionality for generated profile images in the create_ia_profiles application.

## Overview

The application includes a comprehensive backup and restore system for generated profile images. This system allows you to:

1. Create backups of all generated images
2. Restore images from backups
3. Manage multiple backups with automatic cleanup of old backups
4. Configure backup settings through application properties

## Configuration

Backup functionality can be configured through the following properties in `application.properties`:

```properties
# Backup Configuration
backup.backup-dir=backups/images
backup.auto-backup=false
backup.max-backups=10
backup.default-overwrite=false
```

### Configuration Options

- `backup.backup-dir`: The directory where backups will be stored
- `backup.auto-backup`: Whether to automatically create a backup after generating images
- `backup.max-backups`: Maximum number of backups to keep (oldest backups will be deleted when this limit is reached)
- `backup.default-overwrite`: Whether to overwrite existing files during restore operations by default

## Using the Backup API

The application provides a REST API for managing backups:

### Create a Backup

```
POST /api/backups
```

Creates a backup of all generated profile images.

**Response:**
```json
{
  "status": "success",
  "message": "Backup created successfully",
  "filesBackedUp": 10
}
```

### List Backups

```
GET /api/backups
```

Lists all available backups.

**Response:**
```json
{
  "status": "success",
  "backups": [
    "2023-05-15_14-30-45",
    "2023-05-16_09-12-33"
  ],
  "count": 2
}
```

### Restore from Backup

```
POST /api/backups/restore?backupName=2023-05-16_09-12-33&overwrite=true
```

Restores images from a specified backup.

**Parameters:**
- `backupName`: (Optional) The name of the backup to restore from. If not provided, restores from the latest backup.
- `overwrite`: (Optional, default: false) Whether to overwrite existing files during restore.

**Response:**
```json
{
  "status": "success",
  "message": "Restore completed successfully",
  "backupName": "2023-05-16_09-12-33",
  "filesRestored": 10
}
```

## Automatic Backups

When `backup.auto-backup` is set to `true`, the application will automatically create a backup after generating images. This happens in the `ProfileService.generateImages()` method.

## Backup Storage Structure

Backups are stored in timestamped directories under the configured backup directory. For example:

```
backups/images/
  ├── 2023-05-15_14-30-45/
  │   ├── image1.jpg
  │   ├── image2.jpg
  │   └── profiles_with_images.json
  └── 2023-05-16_09-12-33/
      ├── image1.jpg
      ├── image2.jpg
      └── profiles_with_images.json
```

Each backup includes all images and the `profiles_with_images.json` file, which contains the profile data associated with the images.

## Backup Rotation

When the number of backups exceeds the configured maximum (`backup.max-backups`), the oldest backups will be automatically deleted. This helps manage disk space while keeping recent backups available.

## Programmatic Usage

In addition to the REST API, you can use the `ImageBackupService` in your code:

```java
@Autowired
private ImageBackupService backupService;

// Create a backup
Path sourceDir = Paths.get("src/main/resources/static/images");
int filesBackedUp = backupService.createBackup(sourceDir);

// List backups
Path[] backups = backupService.listBackups();

// Restore from a backup
Path backupDir = Paths.get("backups/images/2023-05-16_09-12-33");
Path targetDir = Paths.get("src/main/resources/static/images");
int filesRestored = backupService.restoreImages(backupDir, targetDir, true);

// Restore from latest backup
int filesRestored = backupService.restoreFromLatestBackup(targetDir);
```