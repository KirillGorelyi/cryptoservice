package com.epam.cryptoservice.service.impl;

import com.epam.cryptoservice.exception.CsvReadingException;
import com.epam.cryptoservice.repository.service.CoinRepositoryService;
import com.epam.cryptoservice.repository.service.PriceRepositoryService;
import com.epam.cryptoservice.schema.entity.CoinEntity;
import com.epam.cryptoservice.schema.entity.PriceEntity;
import com.epam.cryptoservice.service.CsvService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvServiceImpl implements CsvService {
    private final static Object lock = new Object();
    private final PriceRepositoryService priceRepositoryService;
    private final CoinRepositoryService coinRepositoryService;



    @Override
    public void processCsvFile(MultipartFile file) throws CsvReadingException {
        List<PriceEntity> priceEntityList= parseCsvFile(file);
        synchronized (lock) {
            priceRepositoryService.saveAllAvoidExisting(priceEntityList);
        }
    }

    private List<PriceEntity> parseCsvFile(MultipartFile file) throws CsvReadingException {
        List<PriceEntity> dataList = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> allRows = reader.readAll();
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                Long timestamp = Long.valueOf(row[0]);
                String coin = row[1];
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(row[2]));
                PriceEntity dto = new PriceEntity(null, timestamp, fetchOrSaveCoinId(coin), price);
                dataList.add(dto);
            }
        } catch (IOException
                 | CsvException
                 | NumberFormatException
                 | ArrayIndexOutOfBoundsException e) {
            throw new CsvReadingException(e);
        }
        return dataList;
    }

    private synchronized Long fetchOrSaveCoinId(String coin){
        CoinEntity coinEntity = coinRepositoryService.findByName(coin);
        return coinEntity == null
                ? coinRepositoryService.saveCoin(coin).getId()
                : coinEntity.getId();
    }
}
