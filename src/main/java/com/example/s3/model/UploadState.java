package com.example.s3.model;

import lombok.Data;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.HashMap;
import java.util.Map;

@Data
public class UploadState {
    final String bucket;
    final String fileKey;

    String uploadId;
    int partCounter;
    Map<Integer, CompletedPart> completedParts = new HashMap<>();
    int buffered = 0;

    public UploadState(String bucket, String fileKey) {
        this.bucket = bucket;
        this.fileKey = fileKey;
    }
}
