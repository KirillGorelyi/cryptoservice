package com.epam.cryptoservice.service;

import com.epam.cryptoservice.exception.CsvReadingException;
import org.springframework.web.multipart.MultipartFile;

public interface CsvService {
    void processCsvFile(MultipartFile file) throws CsvReadingException;

}
