package com.epam.cryptoservice.load;

import com.epam.cryptoservice.repository.service.CoinRepositoryService;
import com.epam.cryptoservice.repository.service.PriceRepositoryService;
import com.epam.cryptoservice.service.impl.CalculationServiceImpl;
import org.openjdk.jmh.annotations.*;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class CalculationServiceBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        public CalculationServiceImpl calculationService;
        public PriceRepositoryService priceRepositoryService;
        public CoinRepositoryService coinRepositoryService;

        public String coin = "BTC";
        public LocalDate date = LocalDate.now().minusDays(1);
        public int months = 6;

        @Setup(Level.Trial)
        public void setUp() {
            priceRepositoryService = Mockito.mock(PriceRepositoryService.class);
            coinRepositoryService = Mockito.mock(CoinRepositoryService.class);

            calculationService = new CalculationServiceImpl(priceRepositoryService, coinRepositoryService);
        }
    }

    @Benchmark
    public void benchmarkGetAllCoinsNormalizedRange(BenchmarkState state) {
        state.calculationService.getAllCoinsNormalizedRange();
    }

    @Benchmark
    public void benchmarkGetCoinPriceInfo(BenchmarkState state) throws Exception {
        state.calculationService.getCoinPriceInfo(state.coin);
    }

    @Benchmark
    public void benchmarkGetCoinWithHighestNormalizedRangeForDay(BenchmarkState state) {
        state.calculationService.getCoinWithHighestNormalizedRangeForDay(state.date);
    }

    @Benchmark
    public void benchmarkGetCoinPeriodStats(BenchmarkState state) throws Exception {
        state.calculationService.getCoinPeriodStats(state.coin, state.months);
    }
}
