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
@Setter
public class ProfileDocument {

    private final UUID documentId;
    private final UUID userId;
    private DocumentType documentType;
    private String fileKey;
    private String fileName;
    private String contentType;
    private long fileSizeBytes;
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

    public UUID getDocumentId() { return documentId; }
    public UUID getUserId() { return userId; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public Instant getUploadedAt() { return uploadedAt; }
}
