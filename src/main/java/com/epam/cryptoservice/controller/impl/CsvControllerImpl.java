package com.epam.cryptoservice.controller.impl;

import com.epam.cryptoservice.controller.CsvController;
import com.epam.cryptoservice.exception.CsvReadingException;
import com.epam.cryptoservice.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/csv")
public class CsvControllerImpl implements CsvController {
    private final CsvService csvService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadStockData(
            @RequestParam("file") MultipartFile file) throws CsvReadingException {
        csvService.processCsvFile(file);
        return ResponseEntity.ok("Coin data uploaded and processed successfully.");
    }

}
