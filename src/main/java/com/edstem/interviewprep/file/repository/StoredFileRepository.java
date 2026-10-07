package com.edstem.interviewprep.file.repository;

import com.edstem.interviewprep.file.entity.StoredFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {}
