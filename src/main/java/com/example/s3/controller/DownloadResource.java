package com.example.s3.controller;

import com.example.s3.S3ClientConfigurarionProperties;
import com.example.s3.exceptions.DownloadFailedException;
import com.example.s3.service.DownloadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/download")
@Slf4j
@AllArgsConstructor
public class DownloadResource {
    private final S3AsyncClient s3client;
    private final S3ClientConfigurarionProperties s3config;
    private final DownloadService downloadService;

    @GetMapping(path = "/{filekey}")
    Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(@PathVariable("filekey") String filekey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3config.getBucket())
                .key(filekey)
                .build();

        return Mono.fromFuture(s3client.getObject(request, new FluxResponseProvider()))
                .map(response -> {
                    downloadService.checkResult(response.sdkResponse);
                    String filename = downloadService.getMetadataItem(response.sdkResponse, "filename", filekey);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, response.sdkResponse.contentType())
                            .header(HttpHeaders.CONTENT_LENGTH, Long.toString(response.sdkResponse.contentLength()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                            .body(response.flux);
                });
    }

    static class FluxResponseProvider implements AsyncResponseTransformer<GetObjectResponse, FluxResponseProvider.FluxResponse> {
        private FluxResponse response;

        @Override
        public CompletableFuture<FluxResponse> prepare() {
            response = new FluxResponse();
            return response.cf;
        }

        @Override
        public void onResponse(GetObjectResponse sdkResponse) {
            this.response.sdkResponse = sdkResponse;
        }

        @Override
        public void onStream(SdkPublisher<ByteBuffer> publisher) {
            response.flux = Flux.from(publisher);
            response.cf.complete(response);
        }

        @Override
        public void exceptionOccurred(Throwable error) {
            response.cf.completeExceptionally(error);
        }

        private static class FluxResponse {
            final CompletableFuture<FluxResponse> cf = new CompletableFuture<>();
            GetObjectResponse sdkResponse;
            Flux<ByteBuffer> flux;
        }

    }
}
