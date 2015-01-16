package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Snapshot;
import org.LatencyUtils.LatencyStats;
import org.LatencyUtils.PauseDetector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramReservoirTest.assertArrayFuzzyEquals;

public class LatencyStatsReservoirTest {

    private static final long MEASUREMENT_OFFSET = 1_000_000;

    private LatencyStatsReservoir r;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Before
    public void setUp() throws Exception {
        initReservoir();
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdownNow();
    }

    private void initReservoir() {
        r = new LatencyStatsReservoir(new LatencyStats.Builder()
            .pauseDetector(new PauseDetector() {})
            .build());
    }

    @Test
    public void testSnapshotSize() {
        r.update(1);
        r.update(2);
        r.update(3);

        Snapshot snapshot = r.getSnapshot();

        assertEquals(3, snapshot.size());
    }

    @Test
    public void testSnapshotValues() {

        int count = 1000;
        long[] expected = new long[count];
        for (int i = 0; i < count; i++) {
            r.update(i + MEASUREMENT_OFFSET);
            expected[i] = i + MEASUREMENT_OFFSET;
        }

        Snapshot snapshot = r.getSnapshot();

        assertArrayFuzzyEquals(expected, snapshot.getValues(), 0.1);
    }

    @Test
    public void testSnapshotRandomValues() {

        Random random = new Random();
        long seed = random.nextLong();
        random.setSeed(seed);
        System.out.println("Using seed " + seed);

        for (int round = 0; round < 1000; round++) {
            initReservoir();

            int count = 1000 + random.nextInt(10000);
            long[] expected = new long[count];
            for (int j = 0; j < count; j++) {
                long val = random.nextInt(1000_000_000) + MEASUREMENT_OFFSET;
                r.update(val);
                expected[j] = val;
            }

            Snapshot snapshot = r.getSnapshot();

            Arrays.sort(expected);

            assertArrayFuzzyEquals(expected, snapshot.getValues(), 0.1);
        }
    }

    @Test
    public void testConcurrentWrites() throws ExecutionException, InterruptedException {

        int numValues = 10_000;
        int numThreads = 4;

        for (int round = 0; round < 100; round++) {
            initReservoir();

            long[] allValues = new long[numThreads * numValues];

            CountDownLatch latch = new CountDownLatch(numThreads);

            List<Future<long[]>> futures = new ArrayList<>();

            for (int i = 0; i < numThreads; i++) {
                futures.add(executorService.submit(() -> {
                    SplittableRandom random = new SplittableRandom();
                    latch.countDown();
                    latch.await();

                    long[] values = new long[numValues];
                    for (int j = 0; j < numValues; j++) {
                        long randLong = random.nextLong(1_000_000_000) + MEASUREMENT_OFFSET;
                        values[j] = randLong;
                        r.update(randLong);
                    }

                    return values;
                }));
            }

            for (int i = 0; i < futures.size(); i++) {
                System.arraycopy(futures.get(i).get(), 0, allValues, i * numValues, numValues);
            }

            Arrays.sort(allValues);

            assertArrayFuzzyEquals(allValues, r.getSnapshot().getValues(), 0.01);
        }
    }
}