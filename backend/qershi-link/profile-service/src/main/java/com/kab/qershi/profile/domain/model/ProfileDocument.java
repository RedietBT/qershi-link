package com.kab.qershi.profile.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an attachment file reference stored in Object Storage (S3 / MinIO).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
public class ProfileDocument {

    private final UUID documentId;
    private final UUID userId;
    @Setter private DocumentType documentType;
    @Setter private String fileKey;
    @Setter private String fileName;
    @Setter private String contentType;
    @Setter private long fileSizeBytes;
    private final Instant uploadedAt;

    public ProfileDocument(UUID documentId, UUID userId, DocumentType documentType, String fileKey,
                           String fileName, String contentType, long fileSizeBytes) {
        this.documentId = documentId != null ? documentId : UUID.randomUUID();
        this.userId = userId;
        this.documentType = documentType;
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedAt = Instant.now();
    }
}
