package com.edstem.interviewprep.file.service;

import org.springframework.core.io.Resource;

public record DownloadableFile(Resource content, String originalName, String contentType) {}
