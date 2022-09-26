package com.example.s3.service;

import com.example.s3.S3ClientConfigurarionProperties;
import com.example.s3.exceptions.UploadFailedException;
import com.example.s3.model.UploadState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@AllArgsConstructor
public class UploadService {
    private final S3AsyncClient s3client;
    private final S3ClientConfigurarionProperties s3config;

    public Mono<String> saveFile(HttpHeaders headers, String bucket, FilePart part) {
        String filekey = UUID.randomUUID().toString();
        log.info("[I137] saveFile: filekey={}, filename={}", filekey, part.filename());
        Map<String, String> metadata = new HashMap<String, String>();
        String filename = part.filename();

        metadata.put("filename", filename);

        MediaType mt = part.headers().getContentType();
        if (mt == null) {
            mt = MediaType.APPLICATION_OCTET_STREAM;
        }

        CompletableFuture<CreateMultipartUploadResponse> uploadRequest = s3client
                .createMultipartUpload(CreateMultipartUploadRequest.builder()
                        .contentType(mt.toString())
                        .key(filekey)
                        .metadata(metadata)
                        .bucket(bucket)
                        .build());

        final UploadState uploadState = new UploadState(bucket, filekey);

        return Mono
                .fromFuture(uploadRequest)
                .flatMapMany((response) -> {
                    checkResult(response);
                    uploadState.setUploadId(response.uploadId());
                    log.info("[I183] uploadId={}", response.uploadId());
                    return part.content();
                })
                .bufferUntil((buffer) -> {
                    uploadState.setBuffered(uploadState.getBuffered() + buffer.readableByteCount());
                    if (uploadState.getBuffered() >= s3config.getMultipartMinPartSize()) {
                        log.info("[I173] bufferUntil: returning true, bufferedBytes={}, partCounter={}, uploadId={}", uploadState.getBuffered(), uploadState.getPartCounter(), uploadState.getUploadId());
                        uploadState.setBuffered(0);
                        return true;
                    } else {
                        return false;
                    }
                })
                .map(UploadService::concatBuffers)
                .flatMap(buffer -> uploadPart(uploadState, buffer))
                .onBackpressureBuffer()
                .reduce(uploadState, (state, completedPart) -> {
                    log.info("[I188] completed: partNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                    state.getCompletedParts().put(completedPart.partNumber(), completedPart);
                    return state;
                })
                .flatMap(this::completeUpload)
                .map(response -> {
                    checkResult(response);
                    return uploadState.getFileKey();
                });
    }

    private static ByteBuffer concatBuffers(List<DataBuffer> buffers) {
        log.info("[I198] creating BytBuffer from {} chunks", buffers.size());

        int partSize = 0;
        for (DataBuffer b : buffers) {
            partSize += b.readableByteCount();
        }

        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach((buffer) -> {
            partData.put(buffer.asByteBuffer());
        });

        partData.rewind();

        log.info("[I208] partData: size={}", partData.capacity());
        return partData;

    }

    private Mono<CompletedPart> uploadPart(UploadState uploadState, ByteBuffer buffer) {
        final int partNumber = uploadState.getPartCounter() + 1;
        log.info("[I218] uploadPart: partNumber={}, contentLength={}", partNumber, buffer.capacity());

        CompletableFuture<UploadPartResponse> request = s3client.uploadPart(UploadPartRequest.builder()
                        .bucket(uploadState.getBucket())
                        .key(uploadState.getFileKey())
                        .partNumber(partNumber)
                        .uploadId(uploadState.getUploadId())
                        .contentLength((long) buffer.capacity())
                        .build(),
                AsyncRequestBody.fromPublisher(Mono.just(buffer)));

        return Mono
                .fromFuture(request)
                .map((uploadPartResult) -> {
                    checkResult(uploadPartResult);
                    log.info("[I230] uploadPart complete: part={}, etag={}", partNumber, uploadPartResult.eTag());
                    return CompletedPart.builder()
                            .eTag(uploadPartResult.eTag())
                            .partNumber(partNumber)
                            .build();
                });
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(UploadState state) {
        log.info("[I202] completeUpload: bucket={}, filekey={}, completedParts.size={}", state.getBucket(), state.getFileKey(), state.getCompletedParts().size());

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(state.getCompletedParts().values())
                .build();

        return Mono.fromFuture(s3client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(state.getBucket())
                .uploadId(state.getUploadId())
                .multipartUpload(multipartUpload)
                .key(state.getFileKey())
                .build()));
    }

    public void checkResult(SdkResponse result) {
        if (result.sdkHttpResponse() == null || !result.sdkHttpResponse().isSuccessful()) {
            throw new UploadFailedException(result);
        }
    }
}
