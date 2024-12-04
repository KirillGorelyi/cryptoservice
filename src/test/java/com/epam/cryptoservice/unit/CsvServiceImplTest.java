package com.epam.cryptoservice.unit;

import com.epam.cryptoservice.exception.CsvReadingException;
import com.epam.cryptoservice.repository.service.CoinRepositoryService;
import com.epam.cryptoservice.repository.service.PriceRepositoryService;
import com.epam.cryptoservice.schema.entity.CoinEntity;
import com.epam.cryptoservice.service.impl.CsvServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvServiceImplTest {
    @Mock
    private CoinRepositoryService coinRepositoryService;

    @Mock
    private PriceRepositoryService priceRepositoryService;

    @InjectMocks
    private CsvServiceImpl csvService;

    @Test
    void testProcessCsvFile_Success() throws Exception {
        String csvContent = """
                timestamp,coin,price
                1622505600,BTC,30000.0
                1622592000,ETH,2000.0
                1622678400,XRP,0.5
                """;

        CoinEntity coinBtc = new CoinEntity(1L, "BTC");
        CoinEntity coinEth = new CoinEntity(2L, "ETH");
        CoinEntity coinXRP = new CoinEntity(3L, "XRP");

        MockMultipartFile mockFile = new MockMultipartFile("file", "prices.csv", "text/csv",
                csvContent.getBytes());

        when(coinRepositoryService.findByName("BTC")).thenReturn(coinBtc);
        when(coinRepositoryService.findByName("ETH")).thenReturn(coinEth);
        when(coinRepositoryService.findByName("XRP")).thenReturn(coinXRP);

        csvService.processCsvFile(mockFile);

        verify(priceRepositoryService, times(1)).saveAllAvoidExisting(anyList());
    }

    @Test
    void testProcessCsvFile_ParseException() {
        String csvContent = """
                timestamp,coin,price
                invalid_timestamp,BTC,30000.0
                """;

        MockMultipartFile mockFile = new MockMultipartFile("file", "prices.csv", "text/csv",
                csvContent.getBytes());

        Exception exception = assertThrows(Exception.class, () -> csvService.processCsvFile(mockFile));

        assertEquals(exception.getCause(), new CsvReadingException(exception).getCause());
    }

    @Test
    void testProcessCsvFile_EmptyFile() throws Exception {
        String csvContent = "timestamp,coin,price\n";

        MockMultipartFile mockFile = new MockMultipartFile("file", "prices.csv", "text/csv",
                csvContent.getBytes());

        csvService.processCsvFile(mockFile);

        verify(priceRepositoryService, times(1)).saveAllAvoidExisting(anyList());
    }
}
