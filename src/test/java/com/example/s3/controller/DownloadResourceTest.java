package com.example.s3.controller;

import com.example.s3.model.UploadResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DownloadResourceTest {
    @Autowired
    DownloadResource downloadResource;

    @Test
    void download_single_file_success() throws IOException {
        String fileId = "a8a64d09-a723-49fb-851a-eca557aa77e0";

        var responseEntityMono = downloadResource.downloadFile(fileId).block();

        assertEquals(200, responseEntityMono.getStatusCode().value());
    }

}