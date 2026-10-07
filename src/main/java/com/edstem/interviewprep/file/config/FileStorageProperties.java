package com.edstem.interviewprep.file.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.storage")
public record FileStorageProperties(Path location, DataSize maxFileSize) {}
