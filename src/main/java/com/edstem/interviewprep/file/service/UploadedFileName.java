package com.edstem.interviewprep.file.service;

public final class UploadedFileName {

  private static final String FALLBACK = "upload";
  private static final int MAX_LENGTH = 255;

  private UploadedFileName() {}

  public static String sanitise(String submitted) {
    if (submitted == null || submitted.isBlank()) {
      return FALLBACK;
    }
    String withoutSeparators = submitted.replace('\\', '/');
    String lastSegment = withoutSeparators.substring(withoutSeparators.lastIndexOf('/') + 1);
    String withoutControlCharacters = lastSegment.replaceAll("[\\p{Cntrl}]", "");
    String trimmed = withoutControlCharacters.trim();
    if (trimmed.isEmpty() || isTraversalSegment(trimmed)) {
      return FALLBACK;
    }
    return trimmed.length() > MAX_LENGTH ? trimmed.substring(0, MAX_LENGTH) : trimmed;
  }

  private static boolean isTraversalSegment(String candidate) {
    return candidate.equals(".") || candidate.equals("..");
  }
}
