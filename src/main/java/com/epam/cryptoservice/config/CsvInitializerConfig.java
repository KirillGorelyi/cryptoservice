package com.epam.cryptoservice.config;

import com.epam.cryptoservice.exception.CsvReadingException;
import com.epam.cryptoservice.schema.InitMultipartFile;
import com.epam.cryptoservice.service.CsvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvInitializerConfig {

    private final CsvService csvService;
    private final PathMatchingResourcePatternResolver resourcePatternResolver
            = new PathMatchingResourcePatternResolver();
    @Value("${spring.init.csv}")
    private String CSV_DIR;

    @PostConstruct
    public void init() throws CsvReadingException {
        List<MultipartFile> files = loadCsvFiles();
        if (files.isEmpty())
            log.info("File directory is empty, skipping db initializing");
        log.info("Found {} files in directory: {}", files.size(), CSV_DIR);
        for (MultipartFile file : files) {
            csvService.processCsvFile(file);
        }
        log.info("File initialization completed");
    }

    private List<MultipartFile> loadCsvFiles() {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        try {
            Resource[] resources = getResources();
            for (Resource resource : resources) {
                if (resource.exists() && resource.isReadable()) {
                    multipartFiles.add(convertToMultipartFile(resource));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CSV files", e);
        }
        return multipartFiles;
    }

    private MultipartFile convertToMultipartFile(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return new InitMultipartFile(
                    resource.getFilename(),
                    resource.getFilename(),
                    "text/csv",
                    inputStream.readAllBytes()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to convert resource to MultipartFile: " + resource.getFilename(), e);
        }
    }

    private Resource[] getResources() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            return resolver.getResources(CSV_DIR + "**/*");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resources from path: " + CSV_DIR, e);
        }
    }
}