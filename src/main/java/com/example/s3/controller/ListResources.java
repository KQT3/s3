package com.example.s3.controller;

import com.example.s3.S3ClientConfigurarionProperties;
import com.example.s3.service.UploadService;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/list")
@Slf4j
@AllArgsConstructor
public class ListResources {
    private final S3AsyncClient s3client;
    private final S3ClientConfigurarionProperties s3config;
    private final UploadService uploadService;

    @GetMapping
    public Mono<List<BucketDTO>> listBucket() {
        CompletableFuture<ListBucketsResponse> listBucketsResponseCompletableFuture = s3client.listBuckets();
        return Mono.fromFuture(listBucketsResponseCompletableFuture)
                .flatMap(response -> Flux.fromIterable(response.buckets()).map(this::toDTO).collectList());
    }

    public BucketDTO toDTO(Bucket bucket) {
        return new BucketDTO(bucket.name());
    }

    @Value
    public static class BucketDTO {
        String bucketName;
    }

}
