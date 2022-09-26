package com.example.s3.controller;

import com.example.s3.model.UploadResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DownloadResourceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int serverPort;

    @Test
    void upload_single_file_success() throws IOException {
        String url = "http://localhost:" + serverPort + "/inbox";
        byte[] data = Files.readAllBytes(Paths.get("src/test/resources/testimage1.png"));

        UploadResult result = restTemplate.postForObject(url, data , UploadResult.class);

        assertEquals(result.getStatus(), HttpStatus.CREATED, "Expected CREATED (202)");
    }
}