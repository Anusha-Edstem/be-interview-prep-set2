package com.edstem.interviewprep.file.service;

import com.edstem.interviewprep.file.config.FileStorageProperties;
import com.edstem.interviewprep.file.exception.FileStorageException;
import com.edstem.interviewprep.file.exception.StorageLocationEscapeException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FileStorage {

  private final Path root;

  public FileStorage(FileStorageProperties properties) {
    this.root = properties.location().toAbsolutePath().normalize();
    try {
      Files.createDirectories(root);
    } catch (IOException exception) {
      throw new FileStorageException("The storage location could not be created");
    }
  }

  public void store(String storageKey, InputStream content) {
    Path target = resolveWithin(storageKey);
    try {
      Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      throw new FileStorageException("The file could not be written to the storage location");
    }
  }

  public Resource load(String storageKey) {
    Path source = resolveWithin(storageKey);
    if (!Files.isRegularFile(source)) {
      throw new FileStorageException("The stored content is no longer present");
    }
    return new FileSystemResource(source);
  }

  public void delete(String storageKey) {
    Path target = resolveWithin(storageKey);
    try {
      Files.deleteIfExists(target);
    } catch (IOException exception) {
      throw new FileStorageException("The file could not be removed from the storage location");
    }
  }

  Path resolveWithin(String storageKey) {
    Path resolved = root.resolve(storageKey).normalize();
    if (!resolved.startsWith(root)) {
      throw new StorageLocationEscapeException();
    }
    return resolved;
  }
}
