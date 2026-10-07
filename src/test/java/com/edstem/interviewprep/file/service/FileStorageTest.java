package com.edstem.interviewprep.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.edstem.interviewprep.file.config.FileStorageProperties;
import com.edstem.interviewprep.file.exception.StorageLocationEscapeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.unit.DataSize;

class FileStorageTest {

  @TempDir Path root;

  private FileStorage fileStorage;

  @BeforeEach
  void setUp() {
    fileStorage = new FileStorage(new FileStorageProperties(root, DataSize.ofMegabytes(5)));
  }

  @Test
  void contentIsWrittenInsideTheStorageLocation() throws IOException {
    fileStorage.store(
        "abc.png", new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)));

    assertThat(Files.readString(root.resolve("abc.png"))).isEqualTo("payload");
  }

  @Test
  void aKeyThatClimbsOutOfTheStorageLocationIsRefused() {
    assertThrows(
        StorageLocationEscapeException.class,
        () ->
            fileStorage.store(
                "../escaped.png",
                new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  void aDeeplyClimbingKeyIsRefused() {
    assertThrows(
        StorageLocationEscapeException.class,
        () -> fileStorage.resolveWithin("../../../etc/passwd"));
  }

  @Test
  void anAbsoluteKeyOutsideTheStorageLocationIsRefused() {
    Path outside = root.getParent().resolve("outside.png").toAbsolutePath();

    assertThrows(
        StorageLocationEscapeException.class, () -> fileStorage.resolveWithin(outside.toString()));
  }

  @Test
  void aNestedKeyThatStaysInsideIsAllowed() {
    Path resolved = fileStorage.resolveWithin("nested/file.png");

    assertThat(resolved.startsWith(root.toAbsolutePath().normalize())).isTrue();
  }

  @Test
  void deletingContentRemovesItFromDisk() throws IOException {
    fileStorage.store("gone.png", new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)));

    fileStorage.delete("gone.png");

    assertThat(Files.exists(root.resolve("gone.png"))).isFalse();
  }
}
