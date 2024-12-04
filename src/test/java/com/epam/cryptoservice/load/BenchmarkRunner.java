package com.epam.cryptoservice.load;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        OptionsBuilder options = (OptionsBuilder) new OptionsBuilder()
                .include(CalculationServiceBenchmark.class.getSimpleName())
                .forks(1)
                .threads(100) // Number of threads to simulate load
                .warmupIterations(5)
                .measurementIterations(10);

        new Runner(options.build()).run();
    }
}
