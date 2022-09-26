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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UploadResourceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int serverPort;

    @Test
    void upload_single_file_success() throws IOException {
        String url = "http://localhost:" + serverPort + "/upload";
//        byte[] data = Files.readAllBytes(Paths.get("src/test/resources/testimage1.png"));
        byte[] data = Files.readAllBytes(Paths.get("src/test/resources/song.wav"));

        UploadResult result = restTemplate.postForObject(url, data , UploadResult.class);

        assertEquals(result.getStatus(), HttpStatus.CREATED, "Expected CREATED (202)");
    }

    @Test
    void upload_multiple_files_success() throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        addFileEntity("f1", body, new File("src/test/resources/testimage1.png"));
        addFileEntity("f2", body, new File("src/test/resources/testimage2.png"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body);
        String url = "http://localhost:" + serverPort + "/upload";

        ResponseEntity<UploadResult> result = restTemplate.postForEntity(url, requestEntity, UploadResult.class);

        assertEquals(HttpStatus.CREATED, result.getStatusCode(), "Http Code");
        assertEquals(2, result.getBody().getKeys().length, "File keys");
    }

    private void addFileEntity(String name, MultiValueMap<String, Object> body, File file) throws Exception {

        byte[] data = Files.readAllBytes(file.toPath());
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        ContentDisposition contentDispositionHeader = ContentDisposition.builder("form-data")
                .name(name)
                .filename(file.getName())
                .build();

        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeader.toString());

        HttpEntity<byte[]> fileEntity = new HttpEntity<>(data, headers);
        body.add(name, fileEntity);
    }
}