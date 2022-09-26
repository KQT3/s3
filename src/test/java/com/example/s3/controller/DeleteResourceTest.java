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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeleteResourceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int serverPort;

    @Test
    void delete_single_file_success() {
        String fileId = "1b03f124-0fb8-4b2a-8418-fb211a9b2939";
        String url = "http://localhost:" + serverPort + "/delete/" + fileId;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        HttpEntity<?> request = new HttpEntity<>(headers);

        var response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fileId, response.getBody());
    }

}