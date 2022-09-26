package com.example.s3.controller;

import com.example.s3.S3ClientConfigurarionProperties;
import com.example.s3.model.UploadResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.nio.ByteBuffer;

@RestController
@RequestMapping("/delete")
@Slf4j
@AllArgsConstructor
public class DeleteResource {
    private final S3AsyncClient s3client;
    private final S3ClientConfigurarionProperties s3config;

    @DeleteMapping("{objectKey}")
    public Mono<ResponseEntity<String>> deleteResource(@RequestHeader HttpHeaders headers, @PathVariable String objectKey) {
        String bucketName = s3config.getBucket();
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(objectKey)
                .build();
        s3client.deleteObject(deleteObjectRequest);

        return Mono.just(ResponseEntity.ok().body(objectKey));
    }

}
