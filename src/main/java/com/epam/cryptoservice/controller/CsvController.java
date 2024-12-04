package com.epam.cryptoservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CsvController {
    ResponseEntity<String> uploadStockData(MultipartFile file) throws Exception;
}
