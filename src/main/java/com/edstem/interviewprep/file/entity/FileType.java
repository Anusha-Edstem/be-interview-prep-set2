package com.edstem.interviewprep.file.entity;

import java.util.Arrays;
import java.util.Optional;

public enum FileType {
  JPEG("image/jpeg", ".jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
  PNG(
      "image/png",
      ".png",
      new byte[] {
        (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
        (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
      }),
  PDF("application/pdf", ".pdf", new byte[] {0x25, 0x50, 0x44, 0x46, 0x2D});

  public static final int SIGNATURE_LENGTH = 8;

  private final String contentType;
  private final String extension;
  private final byte[] signature;

  FileType(String contentType, String extension, byte[] signature) {
    this.contentType = contentType;
    this.extension = extension;
    this.signature = signature;
  }

  public static Optional<FileType> detect(byte[] header) {
    return Arrays.stream(values()).filter(candidate -> candidate.matches(header)).findFirst();
  }

  public boolean matches(byte[] header) {
    if (header == null || header.length < signature.length) {
      return false;
    }
    return Arrays.equals(header, 0, signature.length, signature, 0, signature.length);
  }

  public String getContentType() {
    return contentType;
  }

  public String getExtension() {
    return extension;
  }
}
