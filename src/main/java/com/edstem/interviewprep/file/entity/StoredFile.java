package com.edstem.interviewprep.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stored_files")
public class StoredFile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "original_name", nullable = false, length = 255)
  private String originalName;

  @Enumerated(EnumType.STRING)
  @Column(name = "file_type", nullable = false, length = 20)
  private FileType fileType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "storage_key", nullable = false, unique = true, length = 100)
  private String storageKey;

  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private Instant uploadedAt;

  protected StoredFile() {}

  private StoredFile(
      String originalName,
      FileType fileType,
      long sizeBytes,
      String storageKey,
      Instant uploadedAt) {
    this.originalName = originalName;
    this.fileType = fileType;
    this.sizeBytes = sizeBytes;
    this.storageKey = storageKey;
    this.uploadedAt = uploadedAt;
  }

  public static StoredFile create(
      String originalName,
      FileType fileType,
      long sizeBytes,
      String storageKey,
      Instant uploadedAt) {
    return new StoredFile(originalName, fileType, sizeBytes, storageKey, uploadedAt);
  }

  public UUID getId() {
    return id;
  }

  public String getOriginalName() {
    return originalName;
  }

  public FileType getFileType() {
    return fileType;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }
}
