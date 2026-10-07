package com.edstem.interviewprep.file.dto.response;

import java.time.Instant;
import java.util.UUID;

public record StoredFileResponse(
    UUID id, String originalName, String contentType, long sizeBytes, Instant uploadedAt) {}
