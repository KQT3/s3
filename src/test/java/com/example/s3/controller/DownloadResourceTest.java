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
    //
//    @Autowired
//    private TestRestTemplate restTemplate;
    @Autowired
    DownloadResource downloadResource;

    @LocalServerPort
    private int serverPort;

    @Test
    void download_single_file_success() throws IOException {
        String fileId = "2cb5e5c7-9201-4f9f-ab52-9da6b96e724b";
        String url = "http://localhost:" + serverPort + "/download/" + fileId;

//        ResponseEntity<UploadResult> result = restTemplate.getForEntity(url, UploadResult.class);

        var responseEntityMono = downloadResource.downloadFile(fileId).block();


//        assertEquals(HttpStatus.CREATED, result.getStatusCode(), "Http Code");
//        assertEquals(2, result.getBody().getKeys().length, "File keys");
    }

}